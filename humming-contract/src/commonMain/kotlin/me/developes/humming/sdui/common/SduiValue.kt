package me.developes.humming.sdui.common

import kotlinx.serialization.json.*

/** Internal values; strings, including numeric-looking IDs, are never inferred as numbers. */
sealed interface SduiValue {
    data object Missing : SduiValue
    data object Null : SduiValue
    data class Text(val value: String) : SduiValue
    data class BooleanValue(val value: Boolean) : SduiValue
    data class NumberValue(val source: String) : SduiValue {
        init { require(validNumber(source)) { "Expected a finite JSON number" } }
    }
    class ObjectValue(values: Map<String, SduiValue>) : SduiValue {
        val values: Map<String, SduiValue> = values.toMap()
        init { require(values.values.none { it === Missing }) { "Missing is absence, not an object member" } }
        override fun equals(other: Any?) = other is ObjectValue && values == other.values
        override fun hashCode() = values.hashCode()
    }
    class ArrayValue(values: List<SduiValue>) : SduiValue {
        val values: List<SduiValue> = values.toList()
        init { require(values.none { it === Missing }) { "Missing is not an array item" } }
        override fun equals(other: Any?) = other is ArrayValue && values == other.values
        override fun hashCode() = values.hashCode()
    }
}

private val numberPattern = Regex("-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?")
private fun validNumber(source: String) = numberPattern.matches(source) && source.toDoubleOrNull()?.isFinite() == true

/** Bounded JSON conversion with path diagnostics. Missing has no JSON representation. */
fun parseSduiValue(source: String, limits: PayloadLimits = PayloadLimits()): SduiValue {
    val element = parseBoundedJson(source, limits)
    fun convert(value: JsonElement, path: String): SduiValue = when (value) {
        JsonNull -> SduiValue.Null
        is JsonObject -> SduiValue.ObjectValue(value.mapValues { (key, item) -> convert(item, "$path[${JsonPrimitive(key)}]") })
        is JsonArray -> SduiValue.ArrayValue(value.mapIndexed { index, item -> convert(item, "$path[$index]") })
        is JsonPrimitive -> when {
            value.isString -> SduiValue.Text(value.content)
            value.booleanOrNull != null -> SduiValue.BooleanValue(value.boolean)
            validNumber(value.content) -> SduiValue.NumberValue(value.content)
            else -> throw PayloadException(NodeDiagnostic("VALUE_NUMBER", "Expected a finite JSON number", "typedValue", path))
        }
    }
    return convert(element, "$")
}

fun SduiValue.toJsonValue(): JsonElement = when (this) {
    SduiValue.Missing -> error("Missing has no JSON representation")
    SduiValue.Null -> JsonNull
    is SduiValue.Text -> JsonPrimitive(value)
    is SduiValue.BooleanValue -> JsonPrimitive(value)
    is SduiValue.NumberValue -> Json.parseToJsonElement(source)
    is SduiValue.ObjectValue -> JsonObject(values.mapValues { it.value.toJsonValue() })
    is SduiValue.ArrayValue -> JsonArray(values.map { it.toJsonValue() })
}

/** Legacy null removes a key; explicit typed Null is displayed as the text "null". */
fun SduiValue.toLegacyText(): String? = when (this) {
    SduiValue.Missing -> null
    is SduiValue.Text -> value
    else -> toJsonValue().toString()
}

enum class ScreenStateMode { LEGACY_SHARED, ISOLATED_ENTRIES }

/** Host inputs are immutable per screen entry; no expression syntax is introduced here. */
data class ScreenInputs(
    val server: SduiValue.ObjectValue = SduiValue.ObjectValue(emptyMap()),
    val nav: SduiValue.ObjectValue = SduiValue.ObjectValue(emptyMap()),
)
