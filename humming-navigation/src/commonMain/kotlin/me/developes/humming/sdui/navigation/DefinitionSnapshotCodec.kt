package me.developes.humming.sdui.navigation

import kotlinx.serialization.json.*
import me.developes.humming.sdui.common.*

/** Portable definition envelope. Storage, clock and trust of the storage medium belong to the host. */
class DefinitionSnapshotCodec(
    private val nowMillis: () -> Long,
    private val maxAgeMillis: Long = 300_000,
    private val maxSourceChars: Int = 1_000_000,
) {
    init { require(maxAgeMillis in 1..86_400_000 && maxSourceChars in 1..8_000_000) }
    private val limits = PayloadLimits(
        maxInputChars = 6 * (maxSourceChars + 6 * 4096) + 512,
        maxDepth = 3, maxJsonValues = 16, maxPropertyChars = maxOf(maxSourceChars, 4096),
    )

    fun encode(snapshot: DefinitionSnapshot): String {
        require(snapshot.formatVersion == 1) { "Only definition snapshot format 1 can be encoded" }
        require(snapshot.source.length <= maxSourceChars) { "Definition exceeds snapshot source budget" }
        val now = nowMillis()
        require(now >= 0) { "Snapshot clock must be nonnegative epoch milliseconds" }
        return buildJsonObject {
            put("formatVersion", 1)
            put("storedAtMillis", now)
            put("key", buildJsonObject {
                put("journey", snapshot.key.journey); put("version", snapshot.key.version)
                put("session", snapshot.key.session); put("route", snapshot.key.route)
                put("provider", snapshot.key.provider); put("resource", snapshot.key.resource)
            })
            put("source", snapshot.source)
        }.toString()
    }

    /** Decoding checks identity/age; cache admission must still validate the source against its catalog. */
    fun decode(source: String, expectedKey: DefinitionKey): DefinitionSnapshot {
        fun fail(code: String, path: String, message: String): Nothing = throw PayloadException(
            NodeDiagnostic(code, message, "restoreDefinition", path, documentId = expectedKey.journey))
        val envelope = try { parseBoundedJson(source, limits, expectedKey.journey) } catch (error: PayloadException) {
            throw PayloadException(error.diagnostic.copy(operation = "restoreDefinition"), error)
        } as? JsonObject ?: fail("SNAPSHOT_FORMAT", "$", "Snapshot must be an object")
        if (envelope.keys != setOf("formatVersion", "storedAtMillis", "key", "source"))
            fail("SNAPSHOT_FORMAT", "$", "Snapshot fields do not match format 1")
        val format = envelope["formatVersion"] as? JsonPrimitive
        if (format == null || format.isString || format.intOrNull != 1)
            fail("SNAPSHOT_FORMAT", "$.formatVersion", "Unsupported snapshot format")
        val stored = envelope["storedAtMillis"] as? JsonPrimitive
        val timestamp = stored?.takeUnless { it.isString }?.longOrNull
            ?: fail("SNAPSHOT_TIME", "$.storedAtMillis", "Snapshot timestamp must be an integer")
        val now = nowMillis()
        require(now >= 0) { "Snapshot clock must be nonnegative epoch milliseconds" }
        if (timestamp < 0 || timestamp > now || now - timestamp >= maxAgeMillis)
            fail("SNAPSHOT_EXPIRED", "$.storedAtMillis", "Snapshot expired or clock moved backwards")
        val key = envelope["key"] as? JsonObject
            ?: fail("SNAPSHOT_FORMAT", "$.key", "Snapshot key must be an object")
        val expected = linkedMapOf("journey" to expectedKey.journey, "version" to expectedKey.version,
            "session" to expectedKey.session, "route" to expectedKey.route,
            "provider" to expectedKey.provider, "resource" to expectedKey.resource)
        if (key.keys != expected.keys) fail("SNAPSHOT_FORMAT", "$.key", "Snapshot key fields do not match format 1")
        expected.forEach { (field, value) ->
            val actual = key[field] as? JsonPrimitive
            if (actual == null || !actual.isString || actual.content != value)
                fail("JOURNEY_SNAPSHOT_MISMATCH", "$.key.$field", "Snapshot identity does not match requested definition")
        }
        val definition = envelope["source"] as? JsonPrimitive
        if (definition == null || !definition.isString) fail("SNAPSHOT_FORMAT", "$.source", "Definition source must be text")
        if (definition.content.length > maxSourceChars) fail("DEFINITION_SIZE_LIMIT", "$.source", "Definition exceeds snapshot source budget")
        return DefinitionSnapshot(expectedKey, definition.content)
    }
}
