package me.developes.humming.sdui.common

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.developes.humming.sdui.getPlatform

/** Bounded entry point for untrusted JSON text. Lengths are UTF-16 character counts. */
fun parseNode(
    source: String,
    limits: PayloadLimits = PayloadLimits(),
    documentId: String? = null,
): ServerDrivenNode {
    val origin = NodeOrigin(documentId = documentId, limits = limits)
    return decodeNode(parsePayloadJson(source, origin), origin)
}

internal fun payloadFailure(
    code: String, message: String, origin: NodeOrigin, nodeId: String? = origin.nodeId,
    path: String = origin.path, cause: Throwable? = null,
): Nothing = throw PayloadException(NodeDiagnostic(code, message, "parse", path, nodeId, origin.documentId), cause)

internal fun parsePayloadJson(source: String, origin: NodeOrigin): JsonElement {
    if (source.length > origin.limits.maxInputChars) {
        payloadFailure("INPUT_LIMIT", "Input character budget exceeded", origin)
    }
    // Bound nesting before the JSON parser allocates a recursive tree. Quoted braces do not count.
    var quoted = false
    var escaped = false
    var depth = 0
    for (char in source) {
        if (quoted) {
            if (escaped) escaped = false
            else if (char == '\\') escaped = true
            else if (char == '"') quoted = false
        } else when (char) {
            '"' -> quoted = true
            '{', '[' -> {
                depth++
                if (depth > origin.limits.maxDepth) {
                    payloadFailure("DEPTH_LIMIT", "JSON nesting budget exceeded", origin)
                }
            }
            '}', ']' -> depth--
        }
    }
    return try {
        Json.parseToJsonElement(source)
    } catch (error: SerializationException) {
        payloadFailure("INVALID_JSON", "Malformed JSON", origin, cause = error)
    }
}

internal data class PreparedNode(
    val json: JsonObject,
    val component: String,
    val id: String,
    val properties: MutableMap<String, String?>?,
    val children: MutableList<ServerDrivenNode>?,
    val origin: NodeOrigin,
)

internal fun decodeNode(element: JsonElement, origin: NodeOrigin): ServerDrivenNode {
    validateJsonBudget(element, origin)
    return JsonServerDrivenNode(prepareNode(element, origin))
}

internal fun prepareRoot(json: JsonObject): PreparedNode {
    val origin = NodeOrigin()
    validateJsonBudget(json, origin)
    return prepareNode(json, origin)
}

private fun prepareNode(element: JsonElement, origin: NodeOrigin): PreparedNode {
    val json = element as? JsonObject
        ?: payloadFailure("EXPECTED_NODE", "Expected a node object", origin)
    val knownId = (json["id"] as? JsonPrimitive)?.takeIf { it.isString }?.content
    val component = (json["component"] as? JsonPrimitive)?.takeIf { it.isString }?.content
        ?: payloadFailure("INVALID_COMPONENT", "component must be a string", origin, knownId, "${origin.path}.component")
    qualifiedName(component, origin, knownId)
    if (json.containsKey("id") && knownId.isNullOrBlank()) {
        payloadFailure("INVALID_ID", "id must be a non-empty string", origin, knownId, "${origin.path}.id")
    }
    val id = knownId ?: "$component:${getPlatform().generateUUID()}"
    val properties = json["properties"]?.let { value ->
        val obj = value as? JsonObject
            ?: payloadFailure("INVALID_PROPERTIES", "properties must be an object", origin, id, "${origin.path}.properties")
        obj.forEach { (key, property) ->
            val length = if (property is JsonPrimitive) property.content.length else property.toString().length
            if (length > origin.limits.maxPropertyChars) {
                payloadFailure("PROPERTY_LIMIT", "Property character budget exceeded", origin, id, "${origin.path}.properties[${JsonPrimitive(key)}]")
            }
        }
        transformJsonObjectToMapOfString(obj)
    }
    val children = json["children"]?.let { value ->
        val array = value as? JsonArray
            ?: payloadFailure("INVALID_CHILDREN", "children must be an array", origin, id, "${origin.path}.children")
        array.mapIndexed { index, child ->
            JsonServerDrivenNode(prepareNode(child, origin.copy(path = "${origin.path}.children[$index]", nodeId = id)))
        }.toMutableList<ServerDrivenNode>()
    }
    return PreparedNode(json, component, id, properties, children, origin)
}

private fun validateJsonBudget(element: JsonElement, origin: NodeOrigin) {
    var values = 0
    var nodes = 0
    fun visit(value: JsonElement, path: String, depth: Int, parentId: String?) {
        values++
        val id = (value as? JsonObject)?.get("id")?.let { it as? JsonPrimitive }
            ?.takeIf { it.isString }?.content ?: parentId
        if (values > origin.limits.maxJsonValues) {
            payloadFailure("VALUE_LIMIT", "JSON value budget exceeded", origin, id, path)
        }
        if (depth > origin.limits.maxDepth) {
            payloadFailure("DEPTH_LIMIT", "JSON nesting budget exceeded", origin, id, path)
        }
        when (value) {
            is JsonObject -> {
                // Also count node-shaped objects inside slots/events, without interpreting business data.
                if (value.containsKey("component") && ++nodes > origin.limits.maxNodes) {
                    payloadFailure("NODE_LIMIT", "Node object budget exceeded", origin, id, path)
                }
                value.forEach { (key, child) -> visit(child, "$path[${JsonPrimitive(key)}]", depth + 1, id) }
            }
            is JsonArray -> value.forEachIndexed { index, child -> visit(child, "$path[$index]", depth + 1, id) }
            is JsonPrimitive -> if (value.content.length > origin.limits.maxPropertyChars) {
                payloadFailure("PROPERTY_LIMIT", "String character budget exceeded", origin, id, path)
            }
        }
    }
    visit(element, origin.path, 1, null)
}

internal fun ServerDrivenNode.decodePropertyNodes(key: String): ArrayList<ServerDrivenNode> {
    val text = property(key) ?: return arrayListOf()
    val propertyOrigin = origin.copy(path = "${origin.path}.properties[${JsonPrimitive(key)}]", nodeId = id)
    val element = parsePayloadJson(text, propertyOrigin)
    return decodeNodeList(element, propertyOrigin)
}

internal fun decodeNodeList(element: JsonElement, propertyOrigin: NodeOrigin): ArrayList<ServerDrivenNode> {
    validateJsonBudget(element, propertyOrigin)
    val entries = if (element is JsonArray) element else listOf(element)
    val nodes = arrayListOf<ServerDrivenNode>()
    entries.forEachIndexed { index, value ->
        val entryOrigin = if (element is JsonArray) propertyOrigin.copy(path = "${propertyOrigin.path}[$index]") else propertyOrigin
        if (value !is JsonObject) {
            payloadFailure("INVALID_EVENT_NODE", "Expected a node object in slot/event", entryOrigin)
        }
        nodes += decodeNode(value, entryOrigin)
    }
    return nodes
}
