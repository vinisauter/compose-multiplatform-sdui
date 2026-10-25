package me.developes.humming.sdui.navigation

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonPrimitive
import me.developes.humming.sdui.common.*
import kotlin.time.TimeSource

data class DefinitionKey(val journey: String, val version: String, val session: String,
                         val route: String, val provider: String, val resource: String) {
    init { require(listOf(journey, version, session, route, provider, resource).all { it.isNotBlank() && it.length <= 4096 }) }
}

/** A versioned definition only: screen inputs/form values are never part of this envelope. */
data class DefinitionSnapshot(val key: DefinitionKey, val source: String, val formatVersion: Int = 1)

data class DefinitionCachePolicy(val ttlMillis: Long = 300_000, val maxEntries: Int = 64,
                                 val maxEntryChars: Int = 1_000_000, val maxTotalChars: Int = 4_000_000,
                                 val maxInFlight: Int = 16) {
    init { require(ttlMillis in 1..86_400_000 && maxEntries > 0 && maxEntryChars > 0 &&
        maxEntryChars <= maxTotalChars && maxInFlight > 0) }
}

data class DefinitionCacheMetrics(val hits: Long, val misses: Long, val coalesced: Long, val providerLoads: Long,
                                  val evictions: Long, val loadMillis: Long, val entries: Int, val inFlight: Int)

/** Host-scope-owned loads. A cancelled waiter cannot cancel a load another waiter still needs. */
class JourneyDefinitionCache(
    private val scope: CoroutineScope,
    private val catalog: NodeCatalog = initialNodeCatalog(),
    private val policy: DefinitionCachePolicy = DefinitionCachePolicy(),
    private val nowMillis: () -> Long = monotonicClock(),
) {
    private val ownerJob = SupervisorJob(requireNotNull(scope.coroutineContext[Job]) { "Definition cache requires a host-owned Job" })
    private val loadsScope = CoroutineScope(scope.coroutineContext + ownerJob)
    private data class Entry(val source: String, val storedAt: Long)
    private sealed interface Lookup {
        data class Ready(val source: String) : Lookup
        data class Pending(val flight: Deferred<String>) : Lookup
    }
    private val mutex = Mutex()
    private val entries = linkedMapOf<DefinitionKey, Entry>()
    private val flights = mutableMapOf<DefinitionKey, Deferred<String>>()
    private var closed = false
    private var characters = 0L
    private var hits = 0L
    private var misses = 0L
    private var coalesced = 0L
    private var loads = 0L
    private var evictions = 0L
    private var elapsed = 0L

    suspend fun load(key: DefinitionKey, loader: suspend () -> DefinitionSnapshot): ServerDrivenNode {
        currentCoroutineContext().ensureActive()
        val lookup = mutex.withLock {
            if (closed || !scope.isActive || !ownerJob.isActive) throw CancellationException("Definition cache owner is closed")
            entries.remove(key)?.let { entry ->
                val age = nowMillis() - entry.storedAt
                if (age >= 0 && age < policy.ttlMillis) {
                    entries[key] = entry; hits++
                    return@withLock Lookup.Ready(entry.source)
                }
                characters -= entry.source.length
                evictions++
            }
            flights[key]?.let { coalesced++; return@withLock Lookup.Pending(it) }
            if (flights.size >= policy.maxInFlight) fail(key, "DEFINITION_FLIGHT_LIMIT", "Concurrent definition load limit reached")
            misses++
            lateinit var flight: Deferred<String>
            flight = loadsScope.async(start = CoroutineStart.LAZY) {
                val started = nowMillis()
                try {
                    mutex.withLock { currentCoroutineContext().ensureActive(); loads++ }
                    val snapshot = loader()
                    currentCoroutineContext().ensureActive()
                    if (snapshot.formatVersion != 1 || snapshot.key != key)
                        fail(key, "JOURNEY_SNAPSHOT_MISMATCH", "Definition snapshot does not match the requested format/session/version")
                    validate(key, snapshot.source)
                    mutex.withLock {
                        currentCoroutineContext().ensureActive()
                        if (closed || flights[key] !== flight) throw CancellationException("Definition load invalidated")
                        entries.remove(key)?.let { characters -= it.source.length }
                        entries[key] = Entry(snapshot.source, nowMillis()); characters += snapshot.source.length
                        while (entries.size > policy.maxEntries || characters > policy.maxTotalChars) {
                            val oldest = entries.keys.first()
                            characters -= entries.remove(oldest)!!.source.length
                            evictions++
                        }
                    }
                    snapshot.source
                } finally {
                    withContext(NonCancellable) {
                        mutex.withLock {
                            if (flights[key] === flight) flights.remove(key)
                            elapsed += (nowMillis() - started).coerceAtLeast(0)
                        }
                    }
                }
            }
            flights[key] = flight
            Lookup.Pending(flight)
        }
        val source = when (lookup) {
            is Lookup.Ready -> lookup.source
            is Lookup.Pending -> { lookup.flight.start(); lookup.flight.await() }
        }
        currentCoroutineContext().ensureActive()
        return parseNode(source, documentId = key.journey, limits = limits())
    }

    private fun limits() = PayloadLimits(maxInputChars = policy.maxEntryChars)

    private fun validate(key: DefinitionKey, source: String) {
        if (source.length > policy.maxEntryChars) fail(key, "DEFINITION_SIZE_LIMIT", "Definition exceeds cache entry budget")
        val root = parseNode(source, documentId = key.journey, limits = limits())
        catalog.validate(root, mode = ValidationMode.AUTHORING).firstOrNull { it.severity == DiagnosticSeverity.ERROR }
            ?.let { throw PayloadException(it.copy(operation = "loadDefinition")) }
        val seen = mutableSetOf<String>()
        fun visit(node: ServerDrivenNode) {
            if ((node as? ServerDrivenJson)?.json?.containsKey("id") != true)
                throw PayloadException(node.diagnostic("DEFINITION_ID_REQUIRED", "Cached definitions require explicit IDs", "loadDefinition"))
            if (!seen.add(node.id)) throw PayloadException(node.diagnostic("DUPLICATE_NODE_ID", "Definition IDs must be unique", "loadDefinition"))
            node.children.orEmpty().forEach(::visit)
            catalog[node.component]?.properties.orEmpty().filter { it.kind == PropertyKind.NODES }.forEach { property ->
                node.propertyNodes(property.name).forEach(::visit)
            }
        }
        visit(root)
    }

    suspend fun invalidate(key: DefinitionKey) = invalidateWhere { it == key }
    suspend fun invalidateSession(session: String) = invalidateWhere { it.session == session }
    suspend fun invalidateAll() = invalidateWhere { true }
    suspend fun close() = withContext(NonCancellable) {
        mutex.withLock { closed = true }
        invalidateAll()
        ownerJob.cancel()
    }
    private suspend fun invalidateWhere(predicate: (DefinitionKey) -> Boolean) = withContext(NonCancellable) {
        val cancelled = mutex.withLock {
            entries.keys.filter(predicate).forEach { key -> characters -= entries.remove(key)!!.source.length }
            flights.keys.filter(predicate).mapNotNull { flights.remove(it) }
        }
        cancelled.forEach { it.cancel(CancellationException("Definition load invalidated")) }
    }
    suspend fun metrics(): DefinitionCacheMetrics = mutex.withLock {
        DefinitionCacheMetrics(hits, misses, coalesced, loads, evictions, elapsed, entries.size, flights.size)
    }
    private fun fail(key: DefinitionKey, code: String, message: String): Nothing = throw PayloadException(
        NodeDiagnostic(code, message, "loadDefinition", "$.journey.routes[${JsonPrimitive(key.route)}]", documentId = key.journey))
}

private fun monotonicClock(): () -> Long {
    val origin = TimeSource.Monotonic.markNow()
    return { origin.elapsedNow().inWholeMilliseconds }
}
