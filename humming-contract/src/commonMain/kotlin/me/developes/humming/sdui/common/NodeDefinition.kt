package me.developes.humming.sdui.common

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

enum class NodeRole { COMPONENT, ACTION, NAVIGATION_METADATA }
enum class PropertyKind { TEXT, COLOR, BOOLEAN, INTEGER, LONG, FLOAT, ENUM, NODES, STRING_ARRAY, OPAQUE }
enum class InvalidPropertyPolicy { REJECT, LEGACY_FALLBACK, LEGACY_BOOLEAN, PRESENCE }
enum class ValidationMode { LEGACY, AUTHORING }

data class IntegerPropertyOrder(
    val lower: String, val upper: String, val defaultLower: Int, val defaultUpper: Int,
    val ignoredWhenTrue: String? = null,
)

data class ChildKeyDefinition(val keyProperty: String, val referenceProperty: String)

/** Describes wire values, without Compose/Lifecycle types or executable payload code. */
data class PropertyDefinition(
    val name: String,
    val kind: PropertyKind = PropertyKind.TEXT,
    val required: Boolean = false,
    val defaultValue: String? = null,
    val defaultDescription: String? = null,
    val enumValues: Set<String> = emptySet(),
    val allowsBinding: Boolean = false,
    val invalidPolicy: InvalidPropertyPolicy = InvalidPropertyPolicy.REJECT,
    val nodeRole: NodeRole? = null,
    val minimum: Double? = null,
    val editable: Boolean = true,
) {
    init {
        require(name.isNotEmpty())
        require(kind != PropertyKind.ENUM || enumValues.isNotEmpty())
        require((kind == PropertyKind.NODES) == (nodeRole != null))
    }
}

data class NodeDefinition(
    val identifier: String,
    val role: NodeRole,
    val properties: List<PropertyDefinition> = emptyList(),
    val childrenRole: NodeRole? = null,
    val version: Int = 1,
    val propertyOrders: List<IntegerPropertyOrder> = emptyList(),
    val childKey: ChildKeyDefinition? = null,
) {
    init {
        require(identifier.split(':').let { it.size == 2 && it.all { part -> part.isNotBlank() && part.none(Char::isWhitespace) } })
        require(version > 0)
        require(properties.map { it.name }.distinct().size == properties.size)
    }

    /** Retains raw strings: defaults/coercions remain identical to the legacy handlers. */
    fun parse(node: ServerDrivenNode, mode: ValidationMode = ValidationMode.LEGACY): ParsedNodeProperties {
        val diagnostics = mutableListOf<NodeDiagnostic>()
        if (node.component != identifier) diagnostics += node.diagnostic(
            "DEFINITION_MISMATCH", "Expected $identifier", "schema", "${node.origin.path}.component")
        val known = properties.associateBy { it.name }
        for (property in properties) {
            val value = node.property(property.name)
            val path = "${node.origin.path}.properties[${JsonPrimitive(property.name)}]"
            fun report(code: String, message: String, warning: Boolean = false) {
                diagnostics += node.diagnostic(code, message, "schema", path).copy(
                    severity = if (warning && mode == ValidationMode.LEGACY) DiagnosticSeverity.WARNING else DiagnosticSeverity.ERROR)
            }
            if (value == null) {
                if (property.required) report("MISSING_PROPERTY", "Required property is missing")
                continue
            }
            if (value.length > node.origin.limits.maxPropertyChars) {
                report("PROPERTY_LIMIT", "Property character budget exceeded")
                continue
            }
            if (mode == ValidationMode.AUTHORING && !property.editable) {
                report("UNMODELED_PROPERTY", "Property is preserved by the legacy runtime but not supported for authoring")
            }
            if (property.kind == PropertyKind.NODES) {
                try { node.propertyNodes(property.name) } catch (error: PayloadException) {
                    diagnostics += error.diagnostic
                }
                continue
            }
            if (property.allowsBinding && value.contains("#{")) continue
            val numeric = when (property.kind) {
                PropertyKind.INTEGER -> value.toIntOrNull()?.toDouble()
                PropertyKind.LONG -> value.toLongOrNull()?.toDouble()
                PropertyKind.FLOAT -> value.toFloatOrNull()?.takeIf { it.isFinite() }?.toDouble()
                else -> null
            }
            val valid = when (property.kind) {
                PropertyKind.COLOR -> value.removePrefix("#").let { hex ->
                    hex.length in setOf(3, 4, 6, 8) && hex.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
                }
                PropertyKind.BOOLEAN -> value == "true" || value == "false"
                PropertyKind.INTEGER, PropertyKind.LONG, PropertyKind.FLOAT -> numeric != null
                PropertyKind.ENUM -> value in property.enumValues
                PropertyKind.STRING_ARRAY -> try {
                    val array = parsePayloadJson(value, node.origin.copy(path = path, nodeId = node.id)) as? JsonArray
                    array != null && array.all { it is JsonPrimitive &&
                        (mode == ValidationMode.LEGACY || it.isString && it.contentOrNull != null) }
                } catch (error: PayloadException) {
                    diagnostics += error.diagnostic
                    true // The bounded parser has already reported the failure at this field.
                }
                else -> true
            }
            if (!valid) report("INVALID_PROPERTY", "Expected ${property.kind}",
                warning = property.invalidPolicy != InvalidPropertyPolicy.REJECT)
            if (numeric != null && property.minimum != null && numeric < property.minimum) {
                report("PROPERTY_RANGE", "Value must be at least ${property.minimum}")
            }
        }
        for (name in node.properties.orEmpty().keys) if (name !in known) {
            diagnostics += node.diagnostic("UNMODELED_PROPERTY", "Property has no schema", "schema",
                "${node.origin.path}.properties[${JsonPrimitive(name)}]").copy(
                severity = if (mode == ValidationMode.LEGACY) DiagnosticSeverity.WARNING else DiagnosticSeverity.ERROR)
        }
        for (order in propertyOrders) {
            if (order.ignoredWhenTrue?.let { node.property(it).toBoolean() } == true) continue
            val lower = node.property(order.lower)?.toIntOrNull() ?: order.defaultLower
            val upper = node.property(order.upper)?.toIntOrNull() ?: order.defaultUpper
            if (lower > upper) diagnostics += node.diagnostic("PROPERTY_ORDER",
                "${order.upper} must be at least ${order.lower}", "schema",
                "${node.origin.path}.properties[${JsonPrimitive(order.upper)}]")
        }
        childKey?.let { identity ->
            val names = mutableSetOf<String>()
            node.children.orEmpty().forEachIndexed { index, child ->
                child.property(identity.keyProperty)?.let { name ->
                    if (!names.add(name)) diagnostics += child.diagnostic("DUPLICATE_CHILD_KEY",
                        "Child key must be unique", "schema",
                        "${node.origin.path}.children[$index].properties[${JsonPrimitive(identity.keyProperty)}]")
                }
            }
            node.property(identity.referenceProperty)?.let { reference ->
                if (reference !in names) diagnostics += node.diagnostic("UNKNOWN_CHILD_REFERENCE",
                    "Reference does not identify a child", "schema",
                    "${node.origin.path}.properties[${JsonPrimitive(identity.referenceProperty)}]")
            }
        }
        return ParsedNodeProperties(node, node.properties.orEmpty().toMap(), diagnostics)
    }
}

class ParsedNodeProperties internal constructor(
    private val node: ServerDrivenNode,
    private val values: Map<String, String?>,
    val diagnostics: List<NodeDiagnostic>,
) {
    fun property(name: String): String? = values[name]
    fun requireAllowedValue(name: String, allowed: Set<String>, code: String = "INVALID_PROPERTY") {
        val value = property(name) ?: return
        if (value !in allowed) throw PayloadException(node.diagnostic(code, "Value is not registered", "schema",
            "${node.origin.path}.properties[${JsonPrimitive(name)}]"))
    }
    fun requireValid(): ParsedNodeProperties {
        diagnostics.firstOrNull { it.severity == DiagnosticSeverity.ERROR }?.let { throw PayloadException(it) }
        return this
    }
}

/** Immutable snapshot of registered schemas. Missing definitions never imply authoring support. */
class NodeCatalog(definitions: Collection<NodeDefinition>) {
    private val entries = definitions.map { definition -> definition.copy(
        properties = definition.properties.map { it.copy(enumValues = it.enumValues.toSet()) },
        propertyOrders = definition.propertyOrders.toList()) }
        .associateBy { it.identifier }
    init { require(entries.size == definitions.size) { "Duplicate definition identifier" } }
    val definitions: List<NodeDefinition> get() = entries.values.sortedBy { it.identifier }
    val capabilities: Map<String, Int> get() = definitions.associate { it.identifier to it.version }
    operator fun get(identifier: String): NodeDefinition? = entries[identifier]

    fun validate(
        node: ServerDrivenNode,
        expectedRole: NodeRole = NodeRole.COMPONENT,
        mode: ValidationMode = ValidationMode.LEGACY,
        maxNodes: Int = 10_000,
        maxDepth: Int = 64,
    ): List<NodeDiagnostic> {
        require(maxNodes > 0 && maxDepth in 1..256)
        val diagnostics = mutableListOf<NodeDiagnostic>()
        var count = 0
        var exhausted = false
        fun visit(current: ServerDrivenNode, expected: NodeRole, path: String, depth: Int) {
            if (exhausted) return
            if (++count > maxNodes || depth > maxDepth) {
                diagnostics += current.diagnostic("SCHEMA_BUDGET", "Schema traversal budget exceeded", "schema", path)
                exhausted = true
                return
            }
            val definition = entries[current.component]
            if (definition == null) {
                diagnostics += current.diagnostic("UNMODELED_DEFINITION", "Node has no schema", "schema", "$path.component").copy(
                    severity = if (mode == ValidationMode.LEGACY) DiagnosticSeverity.WARNING else DiagnosticSeverity.ERROR)
                return
            }
            if (definition.role != expected) diagnostics += current.diagnostic(
                "INVALID_NODE_ROLE", "Expected $expected, found ${definition.role}", "schema", "$path.component")
            diagnostics += definition.parse(current, mode).diagnostics.map { diagnostic ->
                diagnostic.copy(path = path + diagnostic.path.removePrefix(current.origin.path))
            }
            val children = current.children.orEmpty()
            if (definition.childrenRole == null && children.isNotEmpty()) diagnostics += current.diagnostic(
                "UNEXPECTED_CHILDREN", "Definition does not consume children", "schema", "$path.children").copy(
                severity = if (mode == ValidationMode.LEGACY) DiagnosticSeverity.WARNING else DiagnosticSeverity.ERROR)
            val childRole = definition.childrenRole
            if (childRole != null) for ((index, child) in children.withIndex()) {
                visit(child, childRole, "$path.children[$index]", depth + 1)
                if (exhausted) break
            }
            for (property in definition.properties) if (property.kind == PropertyKind.NODES) {
                val nodes = try { current.propertyNodes(property.name) } catch (_: PayloadException) { continue }
                for ((index, child) in nodes.withIndex()) {
                    visit(child, property.nodeRole!!, "$path.properties[${JsonPrimitive(property.name)}][$index]", depth + 1)
                    if (exhausted) break
                }
                if (exhausted) break
            }
        }
        visit(node, expectedRole, node.origin.path, 1)
        return diagnostics
    }
}
