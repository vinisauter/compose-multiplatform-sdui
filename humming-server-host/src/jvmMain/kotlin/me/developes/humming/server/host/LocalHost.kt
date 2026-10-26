package me.developes.humming.server.host

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.*
import me.developes.humming.server.*
import me.developes.humming.server.postgres.PostgresDraftRepository
import me.developes.humming.server.postgres.postgresDataSource
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.studio.TargetCapabilities
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.nio.charset.CharacterCodingException
import java.security.MessageDigest
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean

/** T20 adapter only: data is lost on close/restart. No database claim or implicit disk storage. */
class VolatileDraftRepository : DraftRepository {
    private val drafts = ConcurrentHashMap<DocumentKey, StoredDraft>()
    override suspend fun create(draft: StoredDraft) = drafts.putIfAbsent(draft.key, draft) == null
    override suspend fun find(key: DocumentKey) = drafts[key]
}
fun interface RequestAuthenticator { fun authenticate(authorization: String?): Actor? }
class BearerAuthenticator(token: String, private val actor: Actor) : RequestAuthenticator {
    private val expected = token.toByteArray(Charsets.UTF_8)
    init { require(token.length in 32..256 && token.all { it.code in 33..126 }) }
    override fun authenticate(authorization: String?): Actor? {
        val supplied = authorization?.takeIf { it.startsWith("Bearer ") && it.length <= 263 }?.substring(7) ?: return null
        return actor.takeIf { MessageDigest.isEqual(expected, supplied.toByteArray(Charsets.UTF_8)) }
    }
}

/** A bounded local development host. Not an Internet-facing deployment or a production authentication service. */
class DraftHttpHost(
    private val application: DraftApplication,
    private val authenticator: RequestAuthenticator,
    port: Int = 0,
    private val maxBodyBytes: Int = 2_000_000,
    private val operationTimeoutMillis: Long = 10_000,
    private val editing: DraftEditingApplication? = null,
    private val publications: PublicationApplication? = null,
    private val data: PublicationDataApplication? = null,
    private val dataIdentity: (Actor) -> DataIdentity? = { null },
    private val collaboration: CollaborationApplication? = null,
    private val onUnexpectedFailure: (Throwable) -> Unit = { System.err.println("Draft host failed: ${it::class.simpleName}") },
) : AutoCloseable {
    private val job = SupervisorJob()
    private val closed = AtomicBoolean()
    private val executor = ThreadPoolExecutor(4, 4, 0, TimeUnit.MILLISECONDS, ArrayBlockingQueue(32),
        ThreadFactory { task -> Thread(task, "humming-local-http").apply { isDaemon = true } }, ThreadPoolExecutor.AbortPolicy())
    private val server: HttpServer
    val port: Int get() = server.address.port
    init {
        require(port in 0..65535 && maxBodyBytes in 1..8_000_000 && operationTimeoutMillis in 1..120_000)
        server = HttpServer.create(InetSocketAddress("127.0.0.1", port), 32)
        server.executor = executor
        server.createContext("/") { exchange ->
            try {
                runBlocking(job + Dispatchers.Unconfined) { withTimeout(operationTimeoutMillis) { handle(exchange) } }
            } catch (_: TimeoutCancellationException) {
                try { respond(exchange, 504, error("OPERATION_TIMEOUT")) } catch (_: java.io.IOException) { }
            } catch (_: CancellationException) {
                // Closing the host cancels requests; no fake payload failure or response after cancellation.
            } catch (_: java.io.IOException) {
                // A disconnected client or host shutdown cannot receive another response.
            } catch (failure: Exception) {
                onUnexpectedFailure(failure)
                try { respond(exchange, 500, error("INTERNAL_ERROR")) } catch (_: java.io.IOException) { }
            } finally { exchange.close() }
        }
        server.start()
    }

    private suspend fun handle(exchange: HttpExchange) {
        val authorization = exchange.requestHeaders["Authorization"]
        val actor = authenticator.authenticate(authorization?.singleOrNull())
        if (actor == null) { respond(exchange, 401, error("UNAUTHENTICATED")); return }
        if (exchange.requestURI.rawQuery != null) { respond(exchange, 400, error("INVALID_ROUTE")); return }
        if (handleCollaboration(exchange, actor) || handleData(exchange, actor) || handlePublication(exchange, actor)) return
        val route = Regex("/v1/tenants/([A-Za-z0-9][A-Za-z0-9_-]{0,127})/documents/([A-Za-z0-9][A-Za-z0-9_-]{0,127})(/validate|/commands)?")
            .matchEntire(exchange.requestURI.rawPath)
        if (route == null) { respond(exchange, 404, error("ROUTE_NOT_FOUND")); return }
        val key = DocumentKey(route.groupValues[1], route.groupValues[2])
        val validation = route.groupValues[3] == "/validate"
        val command = route.groupValues[3] == "/commands"
        val special = validation || command
        val method = exchange.requestMethod
        if ((special && method != "POST") || (!special && method !in setOf("GET", "POST"))) {
            exchange.responseHeaders.set("Allow", if (special) "POST" else "GET, POST")
            respond(exchange, 405, error("METHOD_NOT_ALLOWED")); return
        }
        val permission = if (command) Permission.EDIT else if (validation) Permission.VALIDATE else if (method == "GET") Permission.READ else Permission.CREATE
        if (!application.isAllowed(actor, key, permission)) { respond(exchange, 403, error("FORBIDDEN")); return }
        if (command && !application.isAllowed(actor, key, Permission.READ)) { respond(exchange, 403, error("FORBIDDEN")); return }
        if (command) {
            val editor = editing ?: run { respond(exchange, 501, error("EDIT_UNAVAILABLE")); return }
            val source = jsonBody(exchange) ?: return
            when (val result = editor.edit(actor, key, source)) {
                is DraftEditResult.Accepted -> respond(exchange, 200, buildJsonObject {
                    put("revision", result.draft.revision); put("appliedRevision", result.appliedRevision)
                    put("replayed", result.replayed); put("document", Json.parseToJsonElement(result.draft.source))
                    put("diagnostics", JsonArray(result.diagnostics.map(::diagnostic)))
                })
                is DraftEditResult.Rejected -> respond(exchange, when (result.code) {
                    ApplicationError.FORBIDDEN -> 403
                    ApplicationError.NOT_FOUND -> 404
                    ApplicationError.REVISION_CONFLICT, ApplicationError.COMMAND_REPLAY_CONFLICT -> 409
                    else -> 422
                }, buildJsonObject {
                    put("code", result.code.name); result.currentRevision?.let { put("currentRevision", it) }
                    put("diagnostics", JsonArray(result.diagnostics.map(::diagnostic)))
                })
            }
            return
        }
        val result = when {
            validation -> application.validate(actor, key)
            method == "GET" -> application.read(actor, key)
            else -> {
                val source = jsonBody(exchange) ?: return
                application.create(actor, key, source)
            }
        }
        currentCoroutineContext().ensureActive()
        when (result) {
            is ApplicationResult.Failure -> respond(exchange, when (result.code) {
                ApplicationError.FORBIDDEN -> 403
                ApplicationError.NOT_FOUND -> 404
                ApplicationError.ALREADY_EXISTS -> 409
                else -> 422
            }, buildJsonObject {
                put("code", result.code.name)
                put("diagnostics", JsonArray(result.diagnostics.map(::diagnostic)))
            })
            is ApplicationResult.Success<*> -> when (val value = result.value) {
                is StoredDraft -> respond(exchange, if (method == "POST") 201 else 200, buildJsonObject {
                    put("revision", value.revision)
                    put("document", Json.parseToJsonElement(value.source))
                })
                is DraftValidation -> respond(exchange, 200, buildJsonObject {
                    put("valid", value.valid); put("diagnostics", JsonArray(value.diagnostics.map(::diagnostic)))
                })
                else -> kotlin.error("Unknown application result")
            }
        }
    }

    private suspend fun handleCollaboration(exchange: HttpExchange, actor: Actor): Boolean {
        val route = Regex("/v1/tenants/([A-Za-z0-9][A-Za-z0-9_-]{0,127})/documents/([A-Za-z0-9][A-Za-z0-9_-]{0,127})/(updates/[0-9]{1,19}|presence|presence/leave|undo)")
            .matchEntire(exchange.requestURI.rawPath) ?: return false
        val key = DocumentKey(route.groupValues[1], route.groupValues[2]); val operation = route.groupValues[3]
        val reading = operation.startsWith("updates/")
        val method = if (reading) "GET" else "POST"
        if (exchange.requestMethod != method) { exchange.responseHeaders.set("Allow", method); respond(exchange, 405, error("METHOD_NOT_ALLOWED")); return true }
        if (!application.isAllowed(actor, key, Permission.READ) || (!reading && !application.isAllowed(actor, key, Permission.EDIT))) {
            respond(exchange, 403, error("FORBIDDEN")); return true
        }
        val app = collaboration ?: run { respond(exchange, 501, error("COLLABORATION_UNAVAILABLE")); return true }
        if (operation == "undo") {
            val source = jsonBody(exchange) ?: return true
            when (val result = app.undo(actor, key, source)) {
                is DraftEditResult.Accepted -> respond(exchange, 200, buildJsonObject {
                    put("revision", result.draft.revision); put("appliedRevision", result.appliedRevision); put("replayed", result.replayed)
                    put("document", Json.parseToJsonElement(result.draft.source)); put("diagnostics", JsonArray(result.diagnostics.map(::diagnostic)))
                })
                is DraftEditResult.Rejected -> respond(exchange, when (result.code) {
                    ApplicationError.FORBIDDEN -> 403
                    ApplicationError.NOT_FOUND -> 404
                    ApplicationError.REVISION_CONFLICT, ApplicationError.COMMAND_REPLAY_CONFLICT, ApplicationError.UNDO_CONFLICT -> 409
                    else -> 422
                }, buildJsonObject {
                    put("code", result.code.name); result.currentRevision?.let { put("currentRevision", it) }
                    put("diagnostics", JsonArray(result.diagnostics.map(::diagnostic)))
                })
            }
            return true
        }
        val result: ApplicationResult<*> = when {
            reading -> {
                val cursor = operation.removePrefix("updates/").toLongOrNull()
                if (cursor == null) { respond(exchange, 422, error("INVALID_CURSOR")); return true }
                app.updates(actor, key, cursor)
            }
            operation == "presence" -> app.heartbeat(actor, key)
            else -> app.leave(actor, key)
        }
        when (result) {
            is ApplicationResult.Failure -> respond(exchange, when (result.code) {
                ApplicationError.FORBIDDEN -> 403
                ApplicationError.NOT_FOUND -> 404
                else -> 422
            }, error(result.code.name))
            is ApplicationResult.Success<*> -> when (val value = result.value) {
                is me.developes.humming.sdui.studio.DraftUpdateFeed -> respond(exchange, 200, Json.parseToJsonElement(
                    me.developes.humming.sdui.studio.DraftUpdateFeedCodec().encode(value).requireValue()).jsonObject)
                is me.developes.humming.sdui.studio.DraftPresenceList -> respond(exchange, 200, Json.encodeToJsonElement(me.developes.humming.sdui.studio.DraftPresenceList.serializer(), value).jsonObject)
                is Boolean -> respond(exchange, 200, buildJsonObject { put("left", value) })
                else -> kotlin.error("Unknown collaboration result")
            }
        }
        return true
    }

    private suspend fun handleData(exchange: HttpExchange, actor: Actor): Boolean {
        val route = Regex("/v1/tenants/([A-Za-z0-9][A-Za-z0-9_-]{0,127})/documents/([A-Za-z0-9][A-Za-z0-9_-]{0,127})/publications/([0-9a-f]{64})/data/([a-z][a-z0-9-]{0,63})/([a-zA-Z][a-zA-Z0-9_-]{0,63})")
            .matchEntire(exchange.requestURI.rawPath) ?: return false
        if (exchange.requestMethod != "GET") { exchange.responseHeaders.set("Allow", "GET"); respond(exchange, 405, error("METHOD_NOT_ALLOWED")); return true }
        val key = DocumentKey(route.groupValues[1], route.groupValues[2])
        if (!application.isAllowed(actor, key, Permission.READ)) { respond(exchange, 403, error("FORBIDDEN")); return true }
        val resolver = data ?: run { respond(exchange, 501, error("DATA_UNAVAILABLE")); return true }
        val identity = dataIdentity(actor) ?: run { respond(exchange, 401, error("SESSION_REQUIRED")); return true }
        check(identity.actor === actor) { "Session adapter returned another principal" }
        when (val result = resolver.resolve(identity, key, route.groupValues[3], route.groupValues[4], route.groupValues[5])) {
            is ApplicationResult.Success -> respond(exchange, 200, buildJsonObject {
                put("version", result.value.version); put("documentId", key.document)
                put("values", result.value.values.toJsonValue())
                put("issues", JsonArray(result.value.issues.map { buildJsonObject {
                    put("mapping", it.mapping); put("code", it.code.name); put("path", it.path); put("operation", it.operation)
                } }))
            })
            is ApplicationResult.Failure -> respond(exchange, when (result.code) {
                ApplicationError.FORBIDDEN -> 403
                ApplicationError.NOT_FOUND -> 404
                else -> 422
            }, buildJsonObject { put("code", result.code.name); put("diagnostics", JsonArray(result.diagnostics.map(::diagnostic))) })
        }
        return true
    }

    private suspend fun handlePublication(exchange: HttpExchange, actor: Actor): Boolean {
        val route = Regex("/v1/tenants/([A-Za-z0-9][A-Za-z0-9_-]{0,127})/documents/([A-Za-z0-9][A-Za-z0-9_-]{0,127})/publications(?:/([0-9a-f]{64}|active)/(select|clients/[a-z][a-z0-9-]{0,63}))?")
            .matchEntire(exchange.requestURI.rawPath) ?: return false
        val key = DocumentKey(route.groupValues[1], route.groupValues[2])
        val version = route.groupValues[3]
        val operation = route.groupValues[4]
        val read = operation.startsWith("clients/")
        val method = if (read) "GET" else "POST"
        if (exchange.requestMethod != method || (version == "active" && !read)) {
            exchange.responseHeaders.set("Allow", method); respond(exchange, 405, error("METHOD_NOT_ALLOWED")); return true
        }
        val permission = if (read) Permission.READ else Permission.PUBLISH
        if (!application.isAllowed(actor, key, permission) ||
            (!read && operation != "select" && !application.isAllowed(actor, key, Permission.READ))) {
            respond(exchange, 403, error("FORBIDDEN")); return true
        }
        val publisher = publications ?: run { respond(exchange, 501, error("PUBLICATION_UNAVAILABLE")); return true }
        val result: ApplicationResult<*> = when {
            read -> publisher.read(actor, key, version.takeUnless { it == "active" }, operation.removePrefix("clients/"))
            operation == "select" -> publisher.select(actor, key, version)
            else -> {
                val source = jsonBody(exchange) ?: return true
                val item = try { parseBoundedJson(source).jsonObject } catch (_: PayloadException) { null } catch (_: IllegalArgumentException) { null }
                val revision = (item?.get("expectedRevision") as? JsonPrimitive)?.takeUnless { it.isString }?.longOrNull
                val profile = (item?.get("profile") as? JsonPrimitive)?.takeIf { it.isString }?.content
                if (item?.keys != setOf("expectedRevision", "profile") || revision == null || profile == null) {
                    respond(exchange, 422, error("INVALID_PUBLICATION")); return true
                }
                publisher.publish(actor, key, revision, profile)
            }
        }
        currentCoroutineContext().ensureActive()
        when (result) {
            is ApplicationResult.Failure -> respond(exchange, when (result.code) {
                ApplicationError.FORBIDDEN -> 403
                ApplicationError.NOT_FOUND -> 404
                ApplicationError.REVISION_CONFLICT -> 409
                else -> 422
            }, buildJsonObject {
                put("code", result.code.name); put("diagnostics", JsonArray(result.diagnostics.map(::diagnostic)))
            })
            is ApplicationResult.Success<*> -> when (val value = result.value) {
                is StoredPublication -> respond(exchange, if (read) 200 else 201, Json.parseToJsonElement(value.source).jsonObject)
                is String -> respond(exchange, 200, buildJsonObject { put("version", value) })
                else -> kotlin.error("Unknown publication result")
            }
        }
        return true
    }

    private fun jsonBody(exchange: HttpExchange): String? {
        val contentType = exchange.requestHeaders.getFirst("Content-Type")?.lowercase()?.replace(" ", "")
        if (contentType !in setOf("application/json", "application/json;charset=utf-8")) {
            respond(exchange, 415, error("JSON_REQUIRED")); return null
        }
        val bytes = exchange.requestBody.readNBytes(maxBodyBytes + 1)
        if (bytes.size > maxBodyBytes) { respond(exchange, 413, error("BODY_LIMIT")); return null }
        return try {
            Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString()
        } catch (_: CharacterCodingException) { respond(exchange, 400, error("INVALID_UTF8")); null }
    }

    private fun diagnostic(value: NodeDiagnostic) = buildJsonObject {
        put("code", value.code); put("message", value.message); put("operation", value.operation)
        put("path", value.path); put("severity", value.severity.name)
        value.nodeId?.let { put("nodeId", it) }; value.documentId?.let { put("documentId", it) }
    }
    private fun error(code: String) = buildJsonObject { put("code", code) }
    private fun respond(exchange: HttpExchange, status: Int, body: JsonObject) {
        val bytes = body.toString().toByteArray(Charsets.UTF_8)
        exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
        exchange.responseHeaders.set("Cache-Control", "no-store")
        exchange.sendResponseHeaders(status, bytes.size.toLong())
        exchange.responseBody.write(bytes)
    }
    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        job.cancel()
        server.stop(0)
        executor.shutdownNow()
        check(executor.awaitTermination(20, TimeUnit.SECONDS)) { "Local HTTP workers did not stop" }
    }
}

fun main(args: Array<String>) {
    val token = requireNotNull(System.getenv("HUMMING_LOCAL_TOKEN")) { "Set HUMMING_LOCAL_TOKEN (32..256 printable characters)" }
    val key = DocumentKey("local", "example")
    val actor = Actor(System.getenv("HUMMING_LOCAL_ACTOR") ?: "local-author", Permission.entries.map { DocumentGrant(key, it) }.toSet())
    val catalog = initialNodeCatalog()
    val database = listOf("HUMMING_DB_URL", "HUMMING_DB_USER", "HUMMING_DB_PASSWORD").map(System::getenv)
    require(database.all { it == null } || database.all { !it.isNullOrBlank() }) { "Supply all three HUMMING_DB variables, or none for volatile mode" }
    val repository: DraftRepository = if (database.first() == null) VolatileDraftRepository() else {
        PostgresDraftRepository(postgresDataSource(database[0]!!, database[1]!!, database[2]!!), catalog)
            .also { runBlocking { it.migrate() } }
    }
    val application = DraftApplication(repository, ExactGrantAuthorizer, catalog,
        TargetCapabilities(catalog.capabilities))
    val editing = (repository as? RevisionedDraftRepository)?.let { DraftEditingApplication(it, ExactGrantAuthorizer) }
    val publications = (repository as? PublicationRepository)?.let { publicationApplication(repository, it, catalog) }
    val collaboration = (repository as? CollaborationRepository)?.let { CollaborationApplication(it, ExactGrantAuthorizer) }
    val catalogData = if ("--catalog-demo" in args) {
        requireNotNull(publications) { "Catalog services require PostgreSQL configuration" }
        catalogDataApplication(publications)
    } else null
    val host = DraftHttpHost(application, BearerAuthenticator(token, actor), editing = editing, publications = publications, collaboration = collaboration,
        data = catalogData, dataIdentity = { if (catalogData != null) DataIdentity(it, "catalog-local") else null })
    Runtime.getRuntime().addShutdownHook(Thread { host.close() })
    println("Draft host: http://127.0.0.1:${host.port}/v1/tenants/local/documents/example (storage=${if (editing == null) "volatile" else "postgresql"})")
    CountDownLatch(1).await()
}

/** Explicit profiles for this local composition root. Native methods remain unregistered. */
fun publicationApplication(drafts: DraftRepository, publications: PublicationRepository, catalog: NodeCatalog): PublicationApplication {
    val tokens = defaultPresentationTokens()
    val target = TargetCapabilities(catalog.capabilities, methods = setOf("getPlatformName"), providers = setOf("published"),
        colorTokens = tokens.colorNames, maskTokens = tokens.maskNames)
    return PublicationApplication(drafts, publications, ExactGrantAuthorizer, catalog, listOf(
        PublicationProfile("local-tree", target), PublicationProfile("local-journey", target, "published"),
        PublicationProfile("empty-client", TargetCapabilities(emptyMap()))),
        PublicationDigest { source -> java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(source.toByteArray(Charsets.UTF_8))) })
}
