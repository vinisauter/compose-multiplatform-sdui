package me.developes.humming.sdui.common

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

/** Standalone theme import/export, independent of project and UI. */
object DesignSystemCodec {
    private val json = Json { encodeDefaults = true }
    private val limits = PayloadLimits(maxInputChars = 100_000, maxPropertyChars = 100_000, maxJsonValues = 5_000)
    fun encode(design: DesignSystem): String = json.encodeToString(design.requireValid()).also { parseBoundedJson(it, limits) }
    fun decode(source: String): DesignSystem = try {
        json.decodeFromJsonElement<DesignSystem>(parseBoundedJson(source, limits)).requireValid()
    } catch (error: SerializationException) {
        throw PayloadException(NodeDiagnostic("INVALID_DESIGN_SYSTEM", "Invalid theme JSON", "designSystemDecode", "$.designSystem"), error)
    }
}
