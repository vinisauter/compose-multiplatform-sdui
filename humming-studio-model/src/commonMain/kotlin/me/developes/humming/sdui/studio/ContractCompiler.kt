package me.developes.humming.sdui.studio

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.developes.humming.sdui.common.DiagnosticSeverity
import me.developes.humming.sdui.common.NodeCatalog
import me.developes.humming.sdui.common.NodeDiagnostic
import me.developes.humming.sdui.common.NodeRole
import me.developes.humming.sdui.common.PayloadException
import me.developes.humming.sdui.common.PayloadLimits
import me.developes.humming.sdui.common.PropertyKind
import me.developes.humming.sdui.common.ValidationMode
import me.developes.humming.sdui.common.parseNode
import me.developes.humming.sdui.common.validateBoundedJson

@Serializable
data class TargetCapabilities(
    val definitions: Map<String, Int>,
    val methods: Set<String> = emptySet(),
    val providers: Set<String> = emptySet(),
    val colorTokens: Set<String> = emptySet(),
    val maskTokens: Set<String> = emptySet(),
)

enum class UnreachablePolicy { REJECT, REPORT }

data class CompilationLimits(
    val maxExpandedNodes: Int = 20_000,
    val maxArtifacts: Int = 256,
    val maxDepth: Int = 64,
    val maxOutputChars: Int = 4_000_000,
) {
    init { require(maxExpandedNodes > 0 && maxArtifacts > 0 && maxDepth in 1..256 && maxOutputChars > 0) }
}

@Serializable
data class CompiledContracts(
    val documentId: String,
    val revision: Long,
    val entryDestination: String,
    val providerType: String?,
    val artifacts: Map<String, String>,
    val sourceNodeIds: Map<String, String>,
)

/** Pure compiler. Declared capabilities never install handlers, providers or effects. */
class ContractCompiler(
    private val catalog: NodeCatalog,
    private val authoringLimits: AuthoringLimits = AuthoringLimits(),
    private val payloadLimits: PayloadLimits = PayloadLimits(),
    private val limits: CompilationLimits = CompilationLimits(),
) {
    fun compileTree(document: AuthoringDocument, target: TargetCapabilities,
                    rootId: String = document.entryNodeId, unreachable: UnreachablePolicy = UnreachablePolicy.REJECT): AuthoringResult<CompiledContracts> =
        compile(document, target, rootId, null, unreachable)

    /** The host must register providerType to resolve the returned artifact map. */
    fun compileJourney(document: AuthoringDocument, target: TargetCapabilities, providerType: String,
                       unreachable: UnreachablePolicy = UnreachablePolicy.REJECT): AuthoringResult<CompiledContracts> =
        compile(document, target, document.entryNodeId, providerType, unreachable)

    private fun compile(document: AuthoringDocument, target: TargetCapabilities, rootId: String,
                        provider: String?, unreachable: UnreachablePolicy): AuthoringResult<CompiledContracts> {
        val diagnostics = validateAuthoringDocument(document, catalog, authoringLimits).toMutableList()
        fun errors() = diagnostics.any { it.severity == DiagnosticSeverity.ERROR }
        fun report(code: String, message: String, id: String? = rootId,
                   path: String = "$.nodes[${JsonPrimitive(id)}]", severity: DiagnosticSeverity = DiagnosticSeverity.ERROR) {
            diagnostics += NodeDiagnostic(code, message, "compile", path, id, document.documentId, severity)
        }
        fun capability(identifier: String, id: String) {
            val definition = catalog[identifier]
            if (definition == null) report("UNMODELED_DEFINITION", "Definition is unavailable to the compiler", id)
            else if (target.definitions[identifier] != definition.version) report("UNSUPPORTED_CAPABILITY",
                "Target must declare the exact schema version of $identifier", id)
        }
        val root = document.nodes[rootId]
        if (root == null) report("MISSING_COMPILE_ROOT", "Compile root does not exist")
        if (root != null && (root.kind !in setOf(AuthorNodeKind.SCREEN, AuthorNodeKind.VISUAL) || root.placement !is Placement.Root))
            report("INVALID_COMPILE_ROOT", "Compile root must be a visual/screen root")
        if (provider != null) {
            if (provider.isBlank() || provider !in target.providers) report("UNSUPPORTED_PROVIDER", "Journey provider is not declared by the target")
            if (root?.definition == "navigation:graph") report("NESTED_NAV_GRAPH", "Use tree compilation for an existing navigation graph")
            capability("navigation:graph", rootId)
            capability("navigation:node", rootId)
        }
        if (errors()) return AuthoringResult(null, diagnostics)
        val indices = AuthoringIndices(document)
        val structural = document.nodes.values.filter { it.placement is Placement.Child }
            .groupBy { (it.placement as Placement.Child).parentId }
        val outgoing = document.edges.values.groupBy { it.sourceNodeId }
        val navBySource = document.edges.values.filter { it.role == EdgeRole.NAVIGATION }.groupBy { it.sourceNodeId }
        val screens = document.nodes.values.filter { it.kind == AuthorNodeKind.SCREEN && it.placement is Placement.Root }
            .associateBy { it.id }
        fun hex(value: String): String = value.encodeToByteArray().joinToString("") { byte ->
            val number = byte.toInt() and 255
            "0123456789abcdef"[number / 16].toString() + "0123456789abcdef"[number % 16]
        }
        val routes = screens.keys.associateWith { "screen-${hex(it)}" }
        val screenByRoute = routes.entries.associate { it.value to it.key }
        fun property(node: AuthorNode, name: String): String? = node.properties[name]?.let {
            if (it is JsonPrimitive) it.content else it.toString()
        }
        val reached = mutableSetOf<String>()
        val screenQueue = mutableListOf(rootId)
        val compiledScreens = linkedSetOf<String>()
        val destinations = mutableMapOf<String, String>()
        var cursor = 0
        while (cursor < screenQueue.size) {
            val screenId = screenQueue[cursor++]
            if (!compiledScreens.add(screenId)) continue
            if (provider != null && (screenId !in screens || document.nodes.getValue(screenId).definition == "navigation:graph")) {
                report("INVALID_SCREEN", "Navigation target must be a root screen with visual content", screenId)
                continue
            }
            val pending = mutableListOf(screenId)
            while (pending.isNotEmpty()) {
                val id = pending.removeAt(pending.lastIndex)
                if (!reached.add(id)) continue
                val node = document.nodes.getValue(id)
                capability(node.definition, id)
                for (definition in catalog[node.definition]?.properties.orEmpty()) {
                    val names = when (definition.kind) {
                        PropertyKind.COLOR_TOKEN -> target.colorTokens
                        PropertyKind.MASK_TOKEN -> target.maskTokens
                        else -> continue
                    }
                    property(node, definition.name)?.let { token -> if (token !in names)
                        report("UNSUPPORTED_PRESENTATION_TOKEN", "Target does not declare token $token", id,
                            "$.nodes[${JsonPrimitive(id)}].properties[${JsonPrimitive(definition.name)}]") }
                }
                if (node.extensions.isNotEmpty()) report("UNSUPPORTED_EXTENSION", "Extra wire fields have no target schema", id)
                if (node.definition == "navigation:graph" && (provider != null || id != rootId)) report(
                    "NESTED_NAV_GRAPH", "Nested navigation graphs are outside the initial compiler subset", id)
                if (node.definition == "action:method") property(node, "invoke")?.let { method ->
                    if (method !in target.methods) report("UNSUPPORTED_METHOD", "Invoked method is not declared by the target", id,
                        "$.nodes[${JsonPrimitive(id)}].properties.invoke")
                }
                if (node.definition == "navigation:node") property(node, "type")?.let { type ->
                    if (type !in target.providers) report("UNSUPPORTED_PROVIDER", "Route provider is not declared by the target", id)
                }
                for (edge in outgoing[id].orEmpty().sortedBy { it.id }) if (edge.metadata.isNotEmpty()) report(
                    "UNSUPPORTED_EDGE_METADATA", "Edge metadata needs an explicit target encoding", id, "$.edges[${JsonPrimitive(edge.id)}].metadata")
                val navigation = navBySource[id].orEmpty()
                if (navigation.isNotEmpty() && node.definition != "navigation:goTo") report(
                    "INVALID_NAVIGATION_SOURCE", "Navigation edges require a navigation:goTo action", id)
                if (node.definition in setOf("navigation:goTo", "navigation:goBack") && provider == null) report(
                    "NAVIGATION_REQUIRES_JOURNEY", "Navigation actions require a compiled journey", id)
                if (node.definition == "navigation:goTo" && provider != null) {
                    val literal = property(node, "destiny")
                    if (navigation.size > 1) report("NAVIGATION_CARDINALITY", "goTo requires exactly one destination", id)
                    val destination = navigation.singleOrNull()?.targetNodeId ?: literal?.let { screenByRoute[it] }
                    if (navigation.isNotEmpty() && literal != null) report("DUPLICATE_NAVIGATION_REFERENCE",
                        "Explicit navigation edge is authoritative; remove the raw destiny property", id)
                    if (destination == null || destination !in screens) report("UNRESOLVED_NAVIGATION", "Navigation destination is not a root screen", id)
                    else { destinations[id] = routes.getValue(destination); screenQueue += destination }
                }
                pending += structural[id].orEmpty().sortedByDescending { it.id }.map { it.id }
                pending += outgoing[id].orEmpty().filter { it.role != EdgeRole.NAVIGATION }.sortedByDescending { it.id }.map { it.targetNodeId }
            }
        }
        for (id in (document.nodes.keys - reached).sorted()) report("UNREACHABLE_NODE", "Node is disconnected from the selected compilation root", id,
            severity = if (unreachable == UnreachablePolicy.REJECT) DiagnosticSeverity.ERROR else DiagnosticSeverity.WARNING)
        for (edge in document.edges.values.filter { it.sourceNodeId !in reached }.sortedBy { it.id }) report(
            "UNREACHABLE_EDGE", "Edge is disconnected from the selected compilation root", edge.sourceNodeId,
            "$.edges[${JsonPrimitive(edge.id)}]", if (unreachable == UnreachablePolicy.REJECT) DiagnosticSeverity.ERROR else DiagnosticSeverity.WARNING)
        if (provider != null && compiledScreens.size + 1 > limits.maxArtifacts) report("COMPILATION_BUDGET", "Artifact budget exceeded")
        if (errors()) return AuthoringResult(null, stableDiagnostics(diagnostics))

        val reserved = document.nodes.keys.toMutableSet()
        val used = mutableSetOf<String>()
        val occurrences = mutableMapOf<String, Int>()
        val sources = linkedMapOf<String, String>()
        val artifacts = linkedMapOf<String, String>()
        var expanded = 0
        var outputChars = 0L
        fun fail(code: String, message: String, id: String = rootId): Nothing = throw PayloadException(
            NodeDiagnostic(code, message, "compile", nodeId = id, documentId = document.documentId))
        fun emittedId(source: String): String {
            var count = occurrences[source] ?: 0
            var result: String
            do { count++; result = if (count == 1) source else "__expanded:$source:$count" }
            while (result in used || count > 1 && result in reserved)
            occurrences[source] = count
            used += result
            sources[result] = source
            return result
        }
        fun syntheticId(base: String, source: String): String {
            var result = base
            var suffix = 0
            while (result in reserved || result in used) result = "$base:${++suffix}"
            used += result
            sources[result] = source
            return result
        }
        fun countNode(id: String, depth: Int) {
            if (++expanded > limits.maxExpandedNodes || depth > limits.maxDepth) fail("COMPILATION_BUDGET", "Expansion/depth budget exceeded", id)
        }
        fun materialize(id: String, depth: Int): JsonObject {
            countNode(id, depth)
            val node = document.nodes.getValue(id)
            val definition = catalog[node.definition]!!
            val wire = linkedMapOf<String, JsonElement>("component" to JsonPrimitive(node.definition), "id" to JsonPrimitive(emittedId(id)))
            val properties = node.properties.toMutableMap()
            destinations[id]?.let { properties["destiny"] = JsonPrimitive(it) }
            for (slot in definition.properties.filter { it.kind == PropertyKind.NODES }.sortedBy { it.name }) {
                val related = if (slot.nodeRole == NodeRole.ACTION)
                    indices.edgesBySourcePort[SourcePort(id, slot.name)].orEmpty().filter { it.role != EdgeRole.NAVIGATION }.map { it.targetNodeId }
                else indices.childrenByParentAndSlot[ParentSlot(id, slot.name)].orEmpty().map { it.id }
                val shape = node.nodeListShapes[slot.name]
                if (related.isEmpty() && shape == null) continue
                val children = related.map { materialize(it, depth + 1) }
                val element: JsonElement = when (shape) {
                    NodeListShape.OBJECT, NodeListShape.STRING_OBJECT -> children.single()
                    else -> JsonArray(children)
                }
                properties[slot.name] = if (shape in setOf(NodeListShape.STRING_OBJECT, NodeListShape.STRING_ARRAY))
                    JsonPrimitive(boundedCanonicalJson(element, payloadLimits.maxPropertyChars, payloadLimits.maxDepth,
                        "compile", document.documentId)) else element
            }
            if (node.hasProperties || properties.isNotEmpty()) wire["properties"] = JsonObject(properties)
            val children = indices.childrenByParentAndSlot[ParentSlot(id, "children")].orEmpty()
            if (node.hasChildren || children.isNotEmpty()) wire["children"] = JsonArray(children.map { materialize(it.id, depth + 1) })
            return JsonObject(wire)
        }
        fun emit(destination: String, tree: JsonObject) {
            validateBoundedJson(tree, payloadLimits, document.documentId)
            val remaining = minOf(payloadLimits.maxInputChars.toLong(), limits.maxOutputChars - outputChars).coerceAtLeast(0).toInt()
            val encoded = boundedCanonicalJson(tree, remaining, payloadLimits.maxDepth, "compile", document.documentId,
                if (remaining < payloadLimits.maxInputChars) "COMPILATION_BUDGET" else "INPUT_LIMIT")
            outputChars += encoded.length
            if (outputChars > limits.maxOutputChars || artifacts.size >= limits.maxArtifacts) fail("COMPILATION_BUDGET", "Output/artifact budget exceeded")
            val parsed = parseNode(encoded, payloadLimits, destination)
            diagnostics += catalog.validate(parsed, mode = ValidationMode.AUTHORING).map { diagnostic -> diagnostic.copy(
                operation = "compile", documentId = document.documentId,
                nodeId = sources[diagnostic.nodeId] ?: diagnostic.nodeId,
                path = "$.artifacts[${JsonPrimitive(destination)}]" + diagnostic.path.removePrefix("$"),
            ) }
            artifacts[destination] = encoded
        }
        return try {
            if (provider == null) emit("screen.json", materialize(rootId, 1))
            else {
                for (screenId in compiledScreens.sorted()) emit("screens/${hex(screenId)}.json", materialize(screenId, 1))
                val routeNodes = compiledScreens.sorted().map { screenId ->
                    countNode(screenId, 2)
                    JsonObject(mapOf("component" to JsonPrimitive("navigation:node"),
                        "id" to JsonPrimitive(syntheticId("__route:${hex(screenId)}", screenId)),
                        "properties" to JsonObject(mapOf("name" to JsonPrimitive(routes.getValue(screenId)),
                            "type" to JsonPrimitive(provider), "destiny" to JsonPrimitive("screens/${hex(screenId)}.json")))))
                }
                countNode(rootId, 1)
                emit("graph.json", JsonObject(mapOf("component" to JsonPrimitive("navigation:graph"),
                    "id" to JsonPrimitive(syntheticId("__graph", rootId)),
                    "properties" to JsonObject(mapOf("startDestination" to JsonPrimitive(routes.getValue(rootId)))),
                    "children" to JsonArray(routeNodes))))
            }
            val value = if (errors()) null else CompiledContracts(document.documentId, document.revision,
                if (provider == null) "screen.json" else "graph.json", provider,
                artifacts.keys.sorted().associateWith { artifacts.getValue(it) }, sources.keys.sorted().associateWith { sources.getValue(it) })
            AuthoringResult(value, stableDiagnostics(diagnostics))
        } catch (error: PayloadException) { AuthoringResult(null, stableDiagnostics(diagnostics + error.diagnostic)) }
    }

    private fun stableDiagnostics(values: List<NodeDiagnostic>): List<NodeDiagnostic> = values.distinct().sortedWith(
        compareBy({ it.path }, { it.nodeId ?: "" }, { it.code }, { it.message }))
}
