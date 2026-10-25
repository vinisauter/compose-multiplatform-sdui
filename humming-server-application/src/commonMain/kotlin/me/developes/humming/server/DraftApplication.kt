package me.developes.humming.server

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.studio.*

data class DocumentKey(val tenant: String, val document: String) {
    init { require(listOf(tenant, document).all { Regex("[A-Za-z0-9][A-Za-z0-9_-]{0,127}").matches(it) }) }
}
enum class Permission { CREATE, READ, VALIDATE, EDIT, PUBLISH }
data class DocumentGrant(val key: DocumentKey, val permission: Permission)
class Actor(val id: String, grants: Set<DocumentGrant>) {
    val grants = grants.toSet()
    init { require(id.isNotBlank() && id.length <= 128) }
}
fun interface DocumentAuthorizer {
    fun allows(actor: Actor, key: DocumentKey, permission: Permission): Boolean
}
object ExactGrantAuthorizer : DocumentAuthorizer {
    override fun allows(actor: Actor, key: DocumentKey, permission: Permission) = DocumentGrant(key, permission) in actor.grants
}

/** Canonical, immutable authoring source. No mutable node map crosses the repository boundary. */
data class StoredDraft(val key: DocumentKey, val revision: Long, val source: String)
interface DraftRepository {
    /** Atomic insert, false on an existing key. Never overwrite a draft. */
    suspend fun create(draft: StoredDraft): Boolean
    suspend fun find(key: DocumentKey): StoredDraft?
}
enum class ApplicationError { FORBIDDEN, NOT_FOUND, ALREADY_EXISTS, INVALID_DOCUMENT, ID_MISMATCH, INITIAL_REVISION, INVALID_COMMAND, REVISION_CONFLICT, COMMAND_REPLAY_CONFLICT, UNKNOWN_PROFILE, INCOMPATIBLE_CLIENT, INVALID_PUBLICATION }
sealed interface ApplicationResult<out T> {
    data class Success<T>(val value: T) : ApplicationResult<T>
    data class Failure(val code: ApplicationError, val diagnostics: List<NodeDiagnostic> = emptyList()) : ApplicationResult<Nothing>
}
data class DraftValidation(val valid: Boolean, val diagnostics: List<NodeDiagnostic>)

/** Application policy only. Authentication, HTTP, storage and deployment belong to adapters. */
class DraftApplication(
    private val repository: DraftRepository,
    private val authorizer: DocumentAuthorizer,
    private val catalog: NodeCatalog,
    target: TargetCapabilities,
    private val journeyProvider: String? = null,
) {
    private val codec = AuthoringDocumentCodec(catalog)
    private val compiler = ContractCompiler(catalog)
    private val target = target.copy(definitions = target.definitions.toMap(), methods = target.methods.toSet(),
        providers = target.providers.toSet(), colorTokens = target.colorTokens.toSet(), maskTokens = target.maskTokens.toSet())

    fun isAllowed(actor: Actor, key: DocumentKey, permission: Permission) = authorizer.allows(actor, key, permission)

    suspend fun create(actor: Actor, key: DocumentKey, source: String): ApplicationResult<StoredDraft> {
        currentCoroutineContext().ensureActive()
        if (!authorizer.allows(actor, key, Permission.CREATE)) return ApplicationResult.Failure(ApplicationError.FORBIDDEN)
        val decoded = codec.decode(source)
        val document = decoded.value ?: return ApplicationResult.Failure(ApplicationError.INVALID_DOCUMENT, decoded.diagnostics)
        if (document.documentId != key.document) return ApplicationResult.Failure(ApplicationError.ID_MISMATCH)
        if (document.revision != 0L) return ApplicationResult.Failure(ApplicationError.INITIAL_REVISION)
        val encoded = codec.encode(document)
        val canonical = encoded.value ?: return ApplicationResult.Failure(ApplicationError.INVALID_DOCUMENT, encoded.diagnostics)
        val draft = StoredDraft(key, document.revision, canonical)
        currentCoroutineContext().ensureActive()
        return if (repository.create(draft)) ApplicationResult.Success(draft)
        else ApplicationResult.Failure(ApplicationError.ALREADY_EXISTS)
    }

    suspend fun read(actor: Actor, key: DocumentKey): ApplicationResult<StoredDraft> {
        currentCoroutineContext().ensureActive()
        if (!authorizer.allows(actor, key, Permission.READ)) return ApplicationResult.Failure(ApplicationError.FORBIDDEN)
        val stored = repository.find(key) ?: return ApplicationResult.Failure(ApplicationError.NOT_FOUND)
        check(stored.key == key) { "Repository returned another document" }
        currentCoroutineContext().ensureActive()
        return ApplicationResult.Success(stored)
    }

    suspend fun validate(actor: Actor, key: DocumentKey): ApplicationResult<DraftValidation> {
        currentCoroutineContext().ensureActive()
        if (!authorizer.allows(actor, key, Permission.VALIDATE)) return ApplicationResult.Failure(ApplicationError.FORBIDDEN)
        val stored = repository.find(key) ?: return ApplicationResult.Failure(ApplicationError.NOT_FOUND)
        check(stored.key == key) { "Repository returned another document" }
        val document = checkNotNull(codec.decode(stored.source).value) { "Repository returned an invalid document" }
        check(document.documentId == key.document && document.revision == stored.revision) { "Repository metadata does not match its source" }
        currentCoroutineContext().ensureActive()
        val compiled = if (journeyProvider == null) compiler.compileTree(document, target)
            else compiler.compileJourney(document, target, journeyProvider)
        currentCoroutineContext().ensureActive()
        return ApplicationResult.Success(DraftValidation(compiled.value != null, compiled.diagnostics))
    }
}
