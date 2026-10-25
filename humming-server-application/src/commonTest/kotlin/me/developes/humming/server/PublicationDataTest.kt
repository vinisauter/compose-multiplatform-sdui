package me.developes.humming.server

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.studio.*
import kotlin.test.*

class PublicationDataTest {
    private val catalog = initialNodeCatalog()
    private val field = mapOf("name" to DataField(listOf("name"), DataKind.TEXT))
    private fun mapping(name: String = "account", source: String = "account") = DataMapping(name, source, field)
    private class Repository : DraftRepository, PublicationRepository {
        val drafts = mutableMapOf<DocumentKey, StoredDraft>()
        val snapshots = mutableMapOf<Pair<DocumentKey, String>, StoredPublication>()
        override suspend fun create(draft: StoredDraft): Boolean { drafts[draft.key] = draft; return true }
        override suspend fun find(key: DocumentKey) = drafts[key]
        override suspend fun publish(snapshot: StoredPublication): ApplicationResult<StoredPublication> { snapshots[snapshot.key to snapshot.version] = snapshot; return ApplicationResult.Success(snapshot) }
        override suspend fun publication(key: DocumentKey, version: String) = snapshots[key to version]
        override suspend fun activePublication(key: DocumentKey): StoredPublication? = error("Resolver must use an explicit version")
        override suspend fun selectPublication(key: DocumentKey, version: String): Boolean = error("Resolver must not change selection")
    }
    private class Fixture(val publications: PublicationApplication, val actor: Actor, val versions: Map<DocumentKey, String>, val repository: Repository) {
        val key get() = versions.keys.first()
        val identity get() = DataIdentity(actor, "session-one")
    }
    private suspend fun fixture(): Fixture {
        val repo = Repository()
        val keys = listOf(DocumentKey("tenantA", "document"), DocumentKey("tenantB", "document"), DocumentKey("tenantA", "other"))
        val actor = Actor("author", keys.map { DocumentGrant(it, Permission.READ) } .toSet() + keys.map { DocumentGrant(it, Permission.PUBLISH) })
        // Deterministic unit-test digest only; the PostgreSQL/HTTP suite uses SHA-256 in the real host.
        val app = PublicationApplication(repo, repo, ExactGrantAuthorizer, catalog,
            listOf(PublicationProfile("full", TargetCapabilities(catalog.capabilities))),
            PublicationDigest { it.hashCode().toUInt().toString(16).padStart(64, '0') })
        val versions = keys.associateWith { key ->
            val source = AuthoringDocumentCodec(catalog).encode(LegacyAuthoringCodec(catalog).importDocument(
                """{"id":"root","component":"layout:text","properties":{"text":"Definition"}}""", key.document).requireValue()).requireValue()
            repo.create(StoredDraft(key, 0, source))
            assertIs<ApplicationResult.Success<StoredPublication>>(app.publish(actor, key, 0, "full")).value.version
        }
        return Fixture(app, actor, versions, repo)
    }
    private suspend fun PublicationDataApplication.resolve(f: Fixture, identity: DataIdentity = f.identity, key: DocumentKey = f.key, plan: String = "screen") =
        assertIs<ApplicationResult.Success<ResolvedPublicationData>>(resolve(identity, key, f.versions.getValue(key), "full", plan)).value

    @Test fun cacheSeparatesTenantDocumentActorSessionAndPlanAndRechecksAuthorization() = runTest {
        val f = fixture(); var calls = 0; var granted = true
        val source = RegisteredDataSource { context -> calls++; SourceData.Success("""{"name":"${context.document.tenant}-${context.document.document}-${context.identity.actor.id}-${context.identity.session}-${context.version}"}""") }
        val app = PublicationDataApplication(f.publications, mapOf("account" to source),
            listOf(DataPlan("screen", listOf(mapping())), DataPlan("other", listOf(mapping()))), DataSourceAuthorizer { _, _ -> granted }, DataClock { 0 })
        val first = app.resolve(f); assertEquals(first, app.resolve(f)); assertEquals(1, calls)
        assertNotEquals(first.values, app.resolve(f, DataIdentity(f.actor, "session-two")).values)
        for (key in f.versions.keys.drop(1)) assertNotEquals(first.values, app.resolve(f, key = key).values)
        val secondActor = Actor("other-author", f.actor.grants)
        assertNotEquals(first.values, app.resolve(f, DataIdentity(secondActor, "session-one")).values)
        app.resolve(f, plan = "other"); assertEquals(6, calls)
        val draft = f.repository.drafts.getValue(f.key)
        val updated = AuthoringDocumentCodec(catalog).decode(draft.source).requireValue().copy(revision = 1)
        f.repository.drafts[f.key] = draft.copy(revision = 1, source = AuthoringDocumentCodec(catalog).encode(updated).requireValue())
        val newVersion = assertIs<ApplicationResult.Success<StoredPublication>>(f.publications.publish(f.actor, f.key, 1, "full")).value.version
        val latest = assertIs<ApplicationResult.Success<ResolvedPublicationData>>(app.resolve(f.identity, f.key, newVersion, "full", "screen")).value
        assertNotEquals(first.values, latest.values); assertEquals(7, calls)
        granted = false
        assertEquals(DataFailure.FORBIDDEN, app.resolve(f).issues.single().code); assertEquals(7, calls)
        val denied = DataIdentity(Actor("denied", emptySet()), "session-one")
        assertEquals(ApplicationError.FORBIDDEN, assertIs<ApplicationResult.Failure>(app.resolve(denied, f.key, f.versions.getValue(f.key), "full", "screen")).code)
        assertEquals(7, calls)
    }
    @Test fun partialFailuresAreExplicitAndNeverCachedAndInvalidResponsesDoNotLeakFields() = runTest {
        val f = fixture(); var fail = true; var goodCalls = 0; var failingCalls = 0
        val app = PublicationDataApplication(f.publications, mapOf(
            "account" to RegisteredDataSource { goodCalls++; SourceData.Success("""{"name":"ok","secret":"not selected"}""") },
            "flaky" to RegisteredDataSource { failingCalls++; if (fail) SourceData.Failure(DataFailure.IO) else SourceData.Success("""{"name":"recovered"}""") },
            "invalid" to RegisteredDataSource { SourceData.Success("{broken") },
            "wrong" to RegisteredDataSource { SourceData.Success("""{"name":42}""") }),
            listOf(DataPlan("screen", listOf(mapping(), mapping("flaky", "flaky"), mapping("missing", "missing"), mapping("invalid", "invalid"), mapping("wrong", "wrong")))),
            DataSourceAuthorizer { _, _ -> true }, DataClock { 0 })
        val first = app.resolve(f)
        assertEquals(setOf(DataFailure.IO, DataFailure.SOURCE_MISSING, DataFailure.INVALID_RESPONSE, DataFailure.INVALID_MAPPING), first.issues.map { it.code }.toSet())
        assertEquals(setOf("name"), (first.values.values.getValue("account") as SduiValue.ObjectValue).values.keys)
        fail = false
        val next = app.resolve(f)
        assertTrue("flaky" in next.values.values); assertEquals(1, goodCalls); assertEquals(2, failingCalls)
        assertEquals(ApplicationError.INVALID_DATA_PLAN, assertIs<ApplicationResult.Failure>(app.resolve(f.identity, f.key, f.versions.getValue(f.key), "full", "unregistered")).code)
    }
    @Test fun timeoutCancellationAndParallelismReleasePermitsWithoutCachingFailures() = runTest {
        val f = fixture(); var active = 0; var maxActive = 0; var cleaned = 0; var block = true
        val source = RegisteredDataSource {
            active++; maxActive = maxOf(maxActive, active)
            try { if (block) awaitCancellation() else { delay(10); SourceData.Success("""{"name":"ok"}""") } }
            finally { active--; cleaned++ }
        }
        val app = PublicationDataApplication(f.publications, mapOf("account" to source),
            listOf(DataPlan("screen", listOf(mapping("a"), mapping("b"), mapping("c"), mapping("d")))),
            DataSourceAuthorizer { _, _ -> true }, DataClock { 0 }, DataPolicy(timeoutMillis = 100, parallelism = 2))
        assertTrue(app.resolve(f).issues.all { it.code == DataFailure.TIMEOUT })
        assertEquals(2, maxActive); assertEquals(0, active)
        val cancelled = launch { app.resolve(f) }
        testScheduler.runCurrent(); cancelled.cancelAndJoin()
        assertEquals(0, active); assertTrue(cleaned >= 4)
        block = false
        assertTrue(app.resolve(f).issues.isEmpty()); assertEquals(0, active)
    }
    @Test fun invalidationPreventsLateCacheAdmissionAndTtlRequiresFreshLoad() = runTest {
        val f = fixture(); var now = 0L; var calls = 0; var wait = true
        val entered = CompletableDeferred<Unit>(); val released = CompletableDeferred<Unit>()
        val source = RegisteredDataSource { calls++; if (wait) { entered.complete(Unit); released.await() }; SourceData.Success("""{"name":"value-$calls"}""") }
        val app = PublicationDataApplication(f.publications, mapOf("account" to source), listOf(DataPlan("screen", listOf(mapping()))),
            DataSourceAuthorizer { _, _ -> true }, DataClock { now }, DataPolicy(ttlMillis = 100))
        val late = async { app.resolve(f) }; entered.await(); app.invalidate(f.identity); wait = false; released.complete(Unit); late.await()
        app.resolve(f); assertEquals(2, calls)
        app.resolve(f); assertEquals(2, calls)
        now = 100; app.resolve(f); assertEquals(3, calls)
        app.invalidate(f.identity); app.resolve(f); assertEquals(4, calls)
    }
    @Test fun sourceProgrammingFailureRemainsAProgrammingFailure() = runTest {
        val f = fixture()
        val app = PublicationDataApplication(f.publications, mapOf("account" to RegisteredDataSource { error("source bug") }),
            listOf(DataPlan("screen", listOf(mapping()))), DataSourceAuthorizer { _, _ -> true }, DataClock { 0 })
        assertFailsWith<IllegalStateException> { app.resolve(f) }
    }
}
