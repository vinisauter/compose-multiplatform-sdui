package me.developes.humming.sdui.navigation

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import me.developes.humming.sdui.common.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class JourneyDefinitionCacheTest {
    private fun key(route: String = "home") = DefinitionKey("journey", "v1", "session-a", route, "compiled", "$route.json")
    private fun source(text: String) = """{"component":"layout:text","id":"label","properties":{"text":"$text"}}"""
    private fun snapshot(key: DefinitionKey, text: String) = DefinitionSnapshot(key, source(text))

    @Test fun hitsAreCopiesAndSessionVersionAndResourcePartitionDefinitions() = runTest {
        val cache = JourneyDefinitionCache(backgroundScope, nowMillis = { testScheduler.currentTime })
        var loads = 0
        try {
            val first = cache.load(key()) { loads++; snapshot(key(), "one") }
            first.properties!!["text"] = "mutated by caller"
            assertEquals("one", cache.load(key()) { error("must hit") }.property("text"))
            for (partition in listOf(key().copy(version = "v2"), key().copy(session = "session-b"), key().copy(resource = "another.json")))
                assertEquals("other", cache.load(partition) { loads++; snapshot(partition, "other") }.property("text"))
            assertEquals(4, loads)
            assertEquals(1, cache.metrics().hits)
            assertEquals(4, cache.metrics().entries)
        } finally { cache.close() }
    }

    @Test fun ttlAndLruAreBoundedAndLatencyIsMeasured() = runTest {
        var now = 0L
        val cache = JourneyDefinitionCache(backgroundScope, policy = DefinitionCachePolicy(ttlMillis = 20, maxEntries = 2), nowMillis = { now })
        var loads = 0
        suspend fun load(route: String) = cache.load(key(route)) { loads++; now += 3; snapshot(key(route), route) }
        try {
            load("a"); load("b"); load("a"); load("c")
            assertEquals(3, loads)
            load("b") // b was least recently used, even though its TTL had not expired.
            assertEquals(4, loads)
            now += 30; load("b")
            assertEquals(5, loads)
            assertTrue(cache.metrics().entries <= 2)
            assertTrue(cache.metrics().evictions >= 3)
            assertEquals(15, cache.metrics().loadMillis)
        } finally { cache.close() }
    }

    @Test fun concurrentWaitersShareLoadAndCancellingOneDoesNotCancelTheOther() = runTest {
        val cache = JourneyDefinitionCache(backgroundScope, nowMillis = { testScheduler.currentTime })
        val gate = CompletableDeferred<Unit>()
        var loads = 0
        try {
            val first = async { cache.load(key()) { loads++; gate.await(); snapshot(key(), "shared") } }
            runCurrent()
            val second = async { cache.load(key()) { error("duplicate loader") } }
            runCurrent()
            first.cancelAndJoin()
            assertTrue(second.isActive)
            gate.complete(Unit)
            assertEquals("shared", second.await().property("text"))
            assertEquals(1, loads)
            assertEquals(1, cache.metrics().coalesced)
            assertEquals(0, cache.metrics().inFlight)
        } finally { cache.close() }
    }

    @Test fun sessionInvalidationCancelsFlightAndCannotResurrectAnOldResult() = runTest {
        val cache = JourneyDefinitionCache(backgroundScope, nowMillis = { testScheduler.currentTime })
        val gate = CompletableDeferred<Unit>()
        try {
            val old = async { cache.load(key()) {
                withContext(NonCancellable) { gate.await() }
                snapshot(key(), "obsolete")
            } }
            runCurrent()
            cache.invalidateSession("session-a")
            assertEquals("fresh", cache.load(key()) { snapshot(key(), "fresh") }.property("text"))
            gate.complete(Unit); runCurrent()
            assertFailsWith<CancellationException> { old.await() }
            assertEquals("fresh", cache.load(key()) { error("must hit fresh") }.property("text"))
            val closing = launch { try { awaitCancellation() } finally { cache.close() } }
            runCurrent(); closing.cancelAndJoin()
            assertEquals(0, cache.metrics().entries)
            assertFailsWith<CancellationException> { cache.load(key()) { error("closed owner") } }
        } finally { gate.complete(Unit); cache.close() }
    }

    @Test fun failedOrIncompatibleSnapshotsAreNeverCachedAndDoNotCancelTheOwner() = runTest {
        val cache = JourneyDefinitionCache(backgroundScope, nowMillis = { testScheduler.currentTime })
        try {
            assertFailsWith<IllegalStateException> { cache.load(key()) { error("provider defect") } }
            val mismatch = assertFailsWith<PayloadException> { cache.load(key()) { snapshot(key().copy(version = "v2"), "wrong") } }
            assertEquals("JOURNEY_SNAPSHOT_MISMATCH", mismatch.diagnostic.code)
            assertEquals("JOURNEY_SNAPSHOT_MISMATCH", assertFailsWith<PayloadException> {
                cache.load(key()) { snapshot(key(), "unknown format").copy(formatVersion = 2) }
            }.diagnostic.code)
            val identity = assertFailsWith<PayloadException> { cache.load(key()) {
                DefinitionSnapshot(key(), """{"component":"layout:text","properties":{"text":"missing id"}}""")
            } }
            assertEquals("DEFINITION_ID_REQUIRED", identity.diagnostic.code)
            assertEquals(0, cache.metrics().entries)
            assertTrue(backgroundScope.isActive)
            assertEquals("recovered", cache.load(key()) { snapshot(key(), "recovered") }.property("text"))
        } finally { cache.close() }
    }

    @Test fun responseAndConcurrencyBudgetsRejectWorkWithoutEvictingValidData() = runTest {
        val size = source("one").length
        val cache = JourneyDefinitionCache(backgroundScope, policy = DefinitionCachePolicy(maxEntryChars = size, maxTotalChars = size, maxInFlight = 1), nowMillis = { testScheduler.currentTime })
        val gate = CompletableDeferred<Unit>()
        try {
            cache.load(key("a")) { snapshot(key("a"), "one") }
            cache.load(key("b")) { snapshot(key("b"), "two") }
            assertEquals(1, cache.metrics().entries)
            val tooLarge = assertFailsWith<PayloadException> { cache.load(key("large")) { snapshot(key("large"), "longer") } }
            assertEquals("DEFINITION_SIZE_LIMIT", tooLarge.diagnostic.code)
            val slow = async { cache.load(key("slow")) { gate.await(); snapshot(key("slow"), "one") } }
            runCurrent()
            val full = assertFailsWith<PayloadException> { cache.load(key("extra")) { error("must not start") } }
            assertEquals("DEFINITION_FLIGHT_LIMIT", full.diagnostic.code)
            gate.complete(Unit); slow.await()
        } finally { gate.complete(Unit); cache.close() }
    }
}
