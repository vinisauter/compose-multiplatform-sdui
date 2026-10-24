package me.developes.humming.sdui.studio

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.developes.humming.sdui.common.DiagnosticSeverity
import me.developes.humming.sdui.common.NodeCatalog
import me.developes.humming.sdui.common.NodeDiagnostic
import me.developes.humming.sdui.common.PayloadException
import me.developes.humming.sdui.common.PayloadLimits
import me.developes.humming.sdui.common.parseBoundedJson
import me.developes.humming.sdui.common.validateBoundedJson

@Serializable
private data class WireDocument(
    val documentId: String,
    val formatVersion: Int,
    val revision: Long,
    val entryNodeId: String,
    val nodes: List<AuthorNode>,
    val edges: List<AuthorEdge>,
)

/** Checks the character budget while writing, before a large expansion allocates its full JSON string. */
internal fun boundedCanonicalJson(
    element: JsonElement, maxChars: Int, maxDepth: Int = 64,
    operation: String = "authoringEncode", documentId: String? = null, code: String = "INPUT_LIMIT",
): String {
    require(maxChars >= 0 && maxDepth > 0)
    val output = StringBuilder(minOf(maxChars, 4096))
    fun fail(budgetCode: String = code): Nothing = throw PayloadException(NodeDiagnostic(
        budgetCode, "JSON encoding budget exceeded", operation, documentId = documentId))
    fun append(value: String) {
        if (output.length.toLong() + value.length > maxChars) fail()
        output.append(value)
    }
    fun primitive(value: kotlinx.serialization.json.JsonPrimitive) {
        if (output.length.toLong() + value.content.length > maxChars) fail()
        append(value.toString())
    }
    fun write(value: JsonElement, depth: Int) {
        if (depth > maxDepth) fail("DEPTH_LIMIT")
        when (value) {
            is JsonObject -> {
                append("{")
                value.keys.sorted().forEachIndexed { index, key ->
                    if (index > 0) append(",")
                    primitive(kotlinx.serialization.json.JsonPrimitive(key))
                    append(":")
                    write(value.getValue(key), depth + 1)
                }
                append("}")
            }
            is JsonArray -> {
                append("[")
                value.forEachIndexed { index, child ->
                    if (index > 0) append(",")
                    write(child, depth + 1)
                }
                append("]")
            }
            is kotlinx.serialization.json.JsonPrimitive -> primitive(value)
        }
    }
    write(element, 1)
    return output.toString()
}

class AuthoringDocumentCodec(
    private val catalog: NodeCatalog,
    private val limits: AuthoringLimits = AuthoringLimits(),
    private val payloadLimits: PayloadLimits = PayloadLimits(),
) {
    private val json = Json { encodeDefaults = true; classDiscriminator = "placementType" }

    fun encode(document: AuthoringDocument): AuthoringResult<String> {
        val diagnostics = validateAuthoringDocument(document, catalog, limits)
        if (diagnostics.any { it.severity == DiagnosticSeverity.ERROR }) return AuthoringResult(null, diagnostics)
        return try {
            val wire = WireDocument(document.documentId, document.formatVersion, document.revision, document.entryNodeId,
                document.nodes.values.sortedBy { it.id }, document.edges.values.sortedBy { it.id })
            val element = json.encodeToJsonElement(WireDocument.serializer(), wire)
            validateBoundedJson(element, payloadLimits, document.documentId)
            val encoded = boundedCanonicalJson(element, payloadLimits.maxInputChars, payloadLimits.maxDepth,
                documentId = document.documentId)
            if (encoded.length > payloadLimits.maxInputChars) return AuthoringResult(null, diagnostics + NodeDiagnostic(
                "INPUT_LIMIT", "Encoded document exceeds character budget", "authoringEncode", documentId = document.documentId))
            AuthoringResult(encoded, diagnostics)
        } catch (error: PayloadException) { AuthoringResult(null, diagnostics + error.diagnostic) }
        catch (_: SerializationException) { AuthoringResult(null, diagnostics + codecError("AUTHORING_ENCODING", document.documentId)) }
    }

    fun decode(source: String): AuthoringResult<AuthoringDocument> {
        return try {
            val wire = json.decodeFromJsonElement(WireDocument.serializer(), parseBoundedJson(source, payloadLimits))
            val diagnostics = mutableListOf<NodeDiagnostic>()
            if (wire.nodes.size > limits.maxNodes || wire.edges.size > limits.maxEdges) return AuthoringResult(null,
                listOf(codecError("AUTHORING_BUDGET", wire.documentId)))
            val nodes = linkedMapOf<String, AuthorNode>()
            val edges = linkedMapOf<String, AuthorEdge>()
            wire.nodes.forEachIndexed { index, node ->
                if (nodes.put(node.id, node) != null) diagnostics += NodeDiagnostic("DUPLICATE_NODE_ID", "Duplicate node ID",
                    "authoringDecode", "$.nodes[$index].id", node.id, wire.documentId)
            }
            wire.edges.forEachIndexed { index, edge ->
                if (edges.put(edge.id, edge) != null) diagnostics += NodeDiagnostic("DUPLICATE_EDGE_ID", "Duplicate edge ID",
                    "authoringDecode", "$.edges[$index].id", edge.sourceNodeId, wire.documentId)
            }
            if (diagnostics.isNotEmpty()) return AuthoringResult(null, diagnostics)
            val document = AuthoringDocument(wire.documentId, wire.formatVersion, wire.revision, wire.entryNodeId, nodes, edges)
            diagnostics += validateAuthoringDocument(document, catalog, limits)
            AuthoringResult(document.takeIf { diagnostics.none { it.severity == DiagnosticSeverity.ERROR } }, diagnostics)
        } catch (error: PayloadException) { AuthoringResult(null, listOf(error.diagnostic)) }
        catch (_: SerializationException) { AuthoringResult(null, listOf(codecError("AUTHORING_JSON", null))) }
    }

    private fun codecError(code: String, documentId: String?) = NodeDiagnostic(code,
        "Authoring document could not be encoded/decoded within its declared format and limits", "authoringCodec", documentId = documentId)
}
