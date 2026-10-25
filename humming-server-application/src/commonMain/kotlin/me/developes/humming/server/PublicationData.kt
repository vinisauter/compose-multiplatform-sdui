package me.developes.humming.server

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import me.developes.humming.sdui.common.*

/** Session is assigned by the authenticated host, never read from a payload or arbitrary header. */
data class DataIdentity(val actor: Actor, val session: String) {
    init { require(session.isNotBlank() && session.length <= 256) }
}
data class DataSourceContext(val identity: DataIdentity, val document: DocumentKey, val version: String)
enum class DataFailure { SOURCE_MISSING, FORBIDDEN, TIMEOUT, IO, HTTP_STATUS, INVALID_RESPONSE, BODY_LIMIT, INVALID_MAPPING }
sealed interface SourceData {
    data class Success(val json: String) : SourceData
    data class Failure(val code: DataFailure) : SourceData
}
fun interface RegisteredDataSource { suspend fun load(context: DataSourceContext): SourceData }
fun interface DataSourceAuthorizer { fun allows(context: DataSourceContext, source: String): Boolean }
enum class DataKind { TEXT, NUMBER, BOOLEAN, OBJECT, ARRAY, ANY }
data class DataField(val path: List<String>, val kind: DataKind)
data class DataMapping(val name: String, val source: String, val fields: Map<String, DataField>)
data class DataPlan(val id: String, val mappings: List<DataMapping>)
data class DataPolicy(val timeoutMillis: Long = 2_000, val parallelism: Int = 4, val cacheEntries: Int = 128,
    val ttlMillis: Long = 5_000, val maxSourceChars: Int = 65_536) {
    init { require(timeoutMillis in 1..120_000 && parallelism in 1..32 && cacheEntries in 0..4096 &&
        ttlMillis in 0..300_000 && maxSourceChars in 1..1_000_000) }
}
fun interface DataClock { fun millis(): Long }
data class DataIssue(val mapping: String, val code: DataFailure) {
    val path: String get() = "$.values.$mapping"
    val operation: String get() = "resolvePublicationData"
}
data class ResolvedPublicationData(val document: DocumentKey, val version: String,
    val values: SduiValue.ObjectValue, val issues: List<DataIssue>)

/** Definition validation precedes IO. Partial failure is explicit; programming defects and cancellation propagate. */
class PublicationDataApplication(
    private val publications: PublicationApplication,
    sources: Map<String, RegisteredDataSource>,
    plans: List<DataPlan>,
    private val authorizer: DataSourceAuthorizer,
    private val clock: DataClock,
    private val policy: DataPolicy = DataPolicy(),
) {
    private val sources = sources.toMap()
    private val plans = plans.map { plan -> plan.copy(mappings = plan.mappings.map { mapping ->
        mapping.copy(fields = mapping.fields.mapValues { (_, field) -> field.copy(path = field.path.toList()) })
    }) }.associateBy { it.id }
    private val permits = Semaphore(policy.parallelism)
    private val mutex = Mutex()
    private data class CacheKey(val document: DocumentKey, val version: String, val actor: String, val session: String, val plan: String, val mapping: String)
    private data class Entry(val expiresAt: Long, val value: SduiValue.ObjectValue)
    private val cache = linkedMapOf<CacheKey, Entry>()
    private var generation = 0L
    init {
        fun valid(value: String) = Regex("[a-zA-Z][a-zA-Z0-9_-]{0,63}").matches(value)
        require(this.plans.size == plans.size && plans.size <= 128 && sources.size <= 128)
        require(sources.keys.all(::valid))
        require(plans.all { plan -> valid(plan.id) && plan.mappings.size in 1..16 &&
            plan.mappings.map { it.name }.toSet().size == plan.mappings.size && plan.mappings.all { mapping ->
                valid(mapping.name) && valid(mapping.source) && mapping.fields.size in 1..32 && mapping.fields.all { (name, field) ->
                    valid(name) && field.path.size in 1..8 && field.path.all { it.isNotBlank() && it.length <= 128 }
                }
            } })
    }

    suspend fun resolve(identity: DataIdentity, key: DocumentKey, version: String, clientProfile: String, planId: String): ApplicationResult<ResolvedPublicationData> {
        currentCoroutineContext().ensureActive()
        when (val snapshot = publications.read(identity.actor, key, version, clientProfile)) {
            is ApplicationResult.Failure -> return snapshot
            is ApplicationResult.Success -> Unit
        }
        val plan = plans[planId] ?: return ApplicationResult.Failure(ApplicationError.INVALID_DATA_PLAN)
        val context = DataSourceContext(identity, key, version)
        val results = coroutineScope { plan.mappings.map { mapping -> async {
            mapping.name to resolveMapping(context, plan, mapping)
        } }.awaitAll() }
        val values = linkedMapOf<String, SduiValue>()
        val issues = mutableListOf<DataIssue>()
        results.forEach { (name, result) -> when (result) {
            is Mapped.Value -> values[name] = result.value
            is Mapped.Failure -> issues += DataIssue(name, result.code)
        } }
        currentCoroutineContext().ensureActive()
        return ApplicationResult.Success(ResolvedPublicationData(key, version, SduiValue.ObjectValue(values), issues))
    }

    private sealed interface Mapped {
        data class Value(val value: SduiValue.ObjectValue) : Mapped
        data class Failure(val code: DataFailure) : Mapped
    }
    private suspend fun resolveMapping(context: DataSourceContext, plan: DataPlan, mapping: DataMapping): Mapped {
        // Recheck authorization even on cache hits, including after a grant was revoked.
        if (!authorizer.allows(context, mapping.source)) return Mapped.Failure(DataFailure.FORBIDDEN)
        val source = sources[mapping.source] ?: return Mapped.Failure(DataFailure.SOURCE_MISSING)
        val key = CacheKey(context.document, context.version, context.identity.actor.id, context.identity.session, plan.id, mapping.name)
        val (cached, startedGeneration) = mutex.withLock {
            val prior = cache.remove(key)
            val hit = prior?.takeIf { clock.millis() < it.expiresAt }
            if (hit != null) cache[key] = hit
            hit?.value to generation
        }
        if (cached != null) return Mapped.Value(cached)
        val loaded = withTimeoutOrNull(policy.timeoutMillis) {
            permits.withPermit {
                when (val result = source.load(context)) {
                    is SourceData.Failure -> Mapped.Failure(result.code)
                    is SourceData.Success -> map(mapping, result.json)
                }
            }
        } ?: return Mapped.Failure(DataFailure.TIMEOUT)
        currentCoroutineContext().ensureActive()
        if (loaded is Mapped.Value && policy.cacheEntries > 0 && policy.ttlMillis > 0) mutex.withLock {
            currentCoroutineContext().ensureActive()
            if (generation == startedGeneration) {
                cache.remove(key)
                cache[key] = Entry(clock.millis() + policy.ttlMillis, loaded.value)
                while (cache.size > policy.cacheEntries) cache.remove(cache.keys.first())
            }
        }
        return loaded
    }
    private fun map(mapping: DataMapping, json: String): Mapped {
        val root = try { parseSduiValue(json, PayloadLimits(maxInputChars = policy.maxSourceChars,
            maxPropertyChars = policy.maxSourceChars, maxJsonValues = 10_000)) }
        catch (_: PayloadException) { return Mapped.Failure(DataFailure.INVALID_RESPONSE) }
        val values = linkedMapOf<String, SduiValue>()
        for ((name, field) in mapping.fields) {
            var value: SduiValue = root
            for (segment in field.path) value = (value as? SduiValue.ObjectValue)?.values?.get(segment) ?: SduiValue.Missing
            val valid = when (field.kind) {
                DataKind.TEXT -> value is SduiValue.Text
                DataKind.NUMBER -> value is SduiValue.NumberValue
                DataKind.BOOLEAN -> value is SduiValue.BooleanValue
                DataKind.OBJECT -> value is SduiValue.ObjectValue
                DataKind.ARRAY -> value is SduiValue.ArrayValue
                DataKind.ANY -> value !== SduiValue.Missing
            }
            if (!valid) return Mapped.Failure(DataFailure.INVALID_MAPPING)
            values[name] = value
        }
        return Mapped.Value(SduiValue.ObjectValue(values))
    }
    /** Logout/invalidation also prevents an older in-flight request from repopulating this cache. */
    suspend fun invalidate(identity: DataIdentity) = mutex.withLock {
        generation++
        cache.keys.filter { it.actor == identity.actor.id && it.session == identity.session }.forEach(cache::remove)
    }
}
