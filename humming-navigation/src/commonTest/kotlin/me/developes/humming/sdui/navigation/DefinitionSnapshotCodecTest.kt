package me.developes.humming.sdui.navigation

import kotlinx.coroutines.test.runTest
import me.developes.humming.sdui.common.PayloadException
import kotlin.test.*

class DefinitionSnapshotCodecTest {
    private val key = DefinitionKey("journey", "v1", "session", "home", "host", "home.json")
    private val snapshot = DefinitionSnapshot(key, """{ "component":"layout:text", "id":"title", "properties":{"text":"Restored"}, "extension":"0007" }""")

    @Test fun deterministicEnvelopeRestoresSourceExactlyIntoAFreshCache() = runTest {
        val codec = DefinitionSnapshotCodec(nowMillis = { 1000 })
        val encoded = codec.encode(snapshot)
        assertEquals(encoded, codec.encode(snapshot))
        assertEquals(snapshot, codec.decode(encoded, key))
        val cache = JourneyDefinitionCache(backgroundScope)
        try {
            val definitions = JourneyDefinitions(key.journey, key.version, key.session, cache) { codec.decode(encoded, it) }
            assertEquals("Restored", definitions.load(key.route, key.provider, key.resource).property("text"))
            assertEquals(1, cache.metrics().entries)
        } finally { cache.close() }
    }

    @Test fun rejectsOtherIdentitiesUnknownFieldsVersionsAndExpiredOrFutureTime() {
        var now = 1000L
        val codec = DefinitionSnapshotCodec(nowMillis = { now }, maxAgeMillis = 20)
        val encoded = codec.encode(snapshot)
        for (other in listOf(key.copy(journey = "other"), key.copy(version = "v2"), key.copy(session = "other"),
            key.copy(route = "other"), key.copy(provider = "other"), key.copy(resource = "other"))) {
            val error = assertFailsWith<PayloadException> { codec.decode(encoded, other) }
            assertEquals("JOURNEY_SNAPSHOT_MISMATCH", error.diagnostic.code)
            assertEquals("restoreDefinition", error.diagnostic.operation)
        }
        assertFailsWith<PayloadException> { codec.decode(encoded.replace("\"formatVersion\":1", "\"formatVersion\":2"), key) }
        assertFailsWith<PayloadException> { codec.decode(encoded.dropLast(1) + ",\"state\":{}}", key) }
        now = 1020
        assertEquals("SNAPSHOT_EXPIRED", assertFailsWith<PayloadException> { codec.decode(encoded, key) }.diagnostic.code)
        now = 999
        assertEquals("SNAPSHOT_EXPIRED", assertFailsWith<PayloadException> { codec.decode(encoded, key) }.diagnostic.code)
    }

    @Test fun boundsEnvelopeAndSourceAndStillRequiresCatalogValidationOnAdmission() = runTest {
        val codec = DefinitionSnapshotCodec(nowMillis = { 1000 }, maxSourceChars = 16)
        assertFailsWith<IllegalArgumentException> { codec.encode(snapshot) }
        assertFailsWith<PayloadException> { codec.decode(" ".repeat(150_000), key) }
        val invalid = DefinitionSnapshot(key, "{}")
        val restored = codec.decode(codec.encode(invalid), key)
        val cache = JourneyDefinitionCache(backgroundScope)
        try {
            assertFailsWith<PayloadException> { cache.load(key) { restored } }
            assertEquals(0, cache.metrics().entries)
        } finally { cache.close() }
    }
}
