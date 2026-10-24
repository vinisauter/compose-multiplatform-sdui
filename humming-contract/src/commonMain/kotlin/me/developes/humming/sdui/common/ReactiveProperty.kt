package me.developes.humming.sdui.common

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*

enum class ReactiveValueType { TEXT, BOOLEAN }

data class ReactiveProperty internal constructor(
    val expression: ExpressionSpec,
    val fallback: SduiValue,
    val type: ReactiveValueType,
)

fun ReactiveValueType.accepts(value: SduiValue): Boolean = when (this) {
    ReactiveValueType.TEXT -> value is SduiValue.Text
    ReactiveValueType.BOOLEAN -> value is SduiValue.BooleanValue
}

/** Explicit property envelope. Ordinary strings and legacy bindings never enter this parser. */
fun ServerDrivenNode.reactiveProperty(name: String, type: ReactiveValueType): ReactiveProperty {
    val path = "$sourcePath.properties[${JsonPrimitive(name)}]"
    fun fail(code: String, message: String): Nothing = throw PayloadException(diagnostic(code, message, "parseReactiveProperty", path))
    val source = property(name) ?: fail("MISSING_PROPERTY", "Reactive property is required")
    if (!source.trimStart().startsWith('{')) fail("REACTIVE_PROPERTY_FORMAT", "Expected a versioned property object")
    val json = parsePayloadJson(source, origin.copy(path = path, nodeId = id)) as? JsonObject
        ?: fail("REACTIVE_PROPERTY_FORMAT", "Expected a versioned property object")
    if (json.keys != setOf("version", "expression", "fallback"))
        fail("REACTIVE_PROPERTY_FORMAT", "Expected exactly version, expression and fallback")
    if (json["version"] != JsonPrimitive(1)) fail("REACTIVE_PROPERTY_VERSION", "Unsupported property version")
    val expression = try { Json.decodeFromJsonElement<ExpressionSpec>(json.getValue("expression")) }
    catch (error: SerializationException) { fail("REACTIVE_PROPERTY_FORMAT", "Invalid expression descriptor") }
    try { compileExpression(expression) } catch (error: PayloadException) {
        throw PayloadException(diagnostic(error.diagnostic.code, error.diagnostic.message, error.diagnostic.operation,
            "$path${error.diagnostic.path.removePrefix("$")}"), error)
    }
    val primitive = json["fallback"] as? JsonPrimitive
    val fallback = when (type) {
        ReactiveValueType.TEXT -> primitive?.takeIf { it.isString }?.let { SduiValue.Text(it.content) }
        ReactiveValueType.BOOLEAN -> primitive?.takeUnless { it.isString }?.booleanOrNull?.let(SduiValue::BooleanValue)
    } ?: fail("REACTIVE_PROPERTY_FALLBACK", "Fallback must be $type")
    return ReactiveProperty(expression, fallback, type)
}
