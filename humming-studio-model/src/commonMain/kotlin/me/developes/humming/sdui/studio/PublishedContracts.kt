package me.developes.humming.sdui.studio

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import me.developes.humming.sdui.common.*

/** Definition only. Session/form/server inputs are deliberately not fields in this closed envelope. */
@Serializable
data class PublishedContracts(
    val formatVersion: Int,
    val version: String,
    val profile: String,
    val required: TargetCapabilities,
    val contracts: CompiledContracts,
)

class PublishedContractsCodec {
    private val json = Json { encodeDefaults = true }
    private val bundle = CompiledBundleCodec()
    private val limits = PayloadLimits(maxInputChars = 12_000_000, maxPropertyChars = 4_000_000, maxJsonValues = 150_000)

    fun encode(value: PublishedContracts): AuthoringResult<String> = encode(value, includeVersion = true)
    /** Canonical content-address input excludes only version. The caller supplies its cryptographic implementation. */
    fun identitySource(value: PublishedContracts): AuthoringResult<String> = encode(value, includeVersion = false)
    private fun encode(value: PublishedContracts, includeVersion: Boolean): AuthoringResult<String> {
        val errors = validate(value)
        if (errors.isNotEmpty()) return AuthoringResult(null, errors)
        val required = value.required.copy(methods = value.required.methods.sorted().toSet(),
            providers = value.required.providers.sorted().toSet(), colorTokens = value.required.colorTokens.sorted().toSet(),
            maskTokens = value.required.maskTokens.sorted().toSet())
        return try {
            val element = json.encodeToJsonElement(PublishedContracts.serializer(), value.copy(required = required)).jsonObject
            val payload = if (includeVersion) element else JsonObject(element - "version")
            validateBoundedJson(payload, limits, value.contracts.documentId)
            AuthoringResult(boundedCanonicalJson(payload, limits.maxInputChars, limits.maxDepth, "publishedEncode", value.contracts.documentId), emptyList())
        } catch (error: PayloadException) { AuthoringResult(null, listOf(error.diagnostic)) }
    }
    fun decode(source: String, expectedVersion: String? = null, expectedDocumentId: String? = null): AuthoringResult<PublishedContracts> = try {
        val element = parseBoundedJson(source, limits)
        val value = json.decodeFromJsonElement(PublishedContracts.serializer(), element)
        val errors = validate(value).toMutableList()
        if ((expectedVersion != null && value.version != expectedVersion) ||
            (expectedDocumentId != null && value.contracts.documentId != expectedDocumentId))
            errors += diagnostic("PUBLISHED_IDENTITY", "Publication does not match the requested version/document", value.contracts.documentId)
        AuthoringResult(value.takeIf { errors.isEmpty() }, errors)
    } catch (error: PayloadException) { AuthoringResult(null, listOf(error.diagnostic))
    } catch (_: SerializationException) { AuthoringResult(null, listOf(diagnostic("PUBLISHED_FORMAT", "Invalid publication envelope"))) }

    private fun validate(value: PublishedContracts): List<NodeDiagnostic> {
        if (value.formatVersion != 1 || !Regex("[0-9a-f]{64}").matches(value.version) ||
            !Regex("[a-z][a-z0-9-]{0,63}").matches(value.profile) || value.required.definitions.size > 256 ||
            value.required.definitions.any { it.key.isBlank() || it.key.length > 256 || it.value < 1 } ||
            listOf(value.required.methods, value.required.providers, value.required.colorTokens, value.required.maskTokens).any {
                it.size > 256 || it.any { name -> name.isBlank() || name.length > 256 }
            }) return listOf(diagnostic("PUBLISHED_FORMAT", "Invalid publication identity or capability manifest", value.contracts.documentId))
        if (value.contracts.artifacts.values.any { it.length > 1_000_000 })
            return listOf(diagnostic("PUBLISHED_BUDGET", "Artifact exceeds the published definition budget", value.contracts.documentId))
        return bundle.encode(value.contracts).diagnostics
    }
    private fun diagnostic(code: String, message: String, document: String? = null) =
        NodeDiagnostic(code, message, "publishedBundle", documentId = document)
}

/** Required versions are checked independently; no global schema number implies installed capabilities. */
fun publicationCompatibility(required: TargetCapabilities, available: TargetCapabilities, documentId: String): List<NodeDiagnostic> {
    val errors = mutableListOf<NodeDiagnostic>()
    fun missing(path: String, message: String) { errors += NodeDiagnostic("INCOMPATIBLE_CLIENT", message, "loadPublication", path, documentId = documentId) }
    required.definitions.toSortedMapPortable().forEach { (name, version) ->
        if (available.definitions[name] != version) missing("$.required.definitions[${JsonPrimitive(name)}]", "Client does not declare the required version of $name")
    }
    for ((name, needs, supports) in listOf(
        Triple("methods", required.methods, available.methods), Triple("providers", required.providers, available.providers),
        Triple("colorTokens", required.colorTokens, available.colorTokens), Triple("maskTokens", required.maskTokens, available.maskTokens)))
        needs.sorted().filterNot { it in supports }.forEach { missing("$.required.$name[${JsonPrimitive(it)}]", "Client does not declare required $name capability: $it") }
    return errors
}
private fun <V> Map<String, V>.toSortedMapPortable(): Map<String, V> = entries.sortedBy { it.key }.associate { it.toPair() }

/** A pinned definition source. Parse anew per access; mutable runtime nodes never share a cache instance. */
class PublishedDefinition private constructor(val version: String, val documentId: String,
    val entryDestination: String, private val artifacts: Map<String, String>) {
    fun node(destination: String): ServerDrivenNode = parseNode(
        artifacts[destination] ?: throw PayloadException(NodeDiagnostic("PUBLISHED_RESOURCE", "Resource is not part of the pinned publication", "loadPublication", documentId = documentId)),
        documentId = destination)
    companion object {
        fun decode(source: String, version: String, documentId: String, catalog: NodeCatalog,
            available: TargetCapabilities): AuthoringResult<PublishedDefinition> {
            val decoded = PublishedContractsCodec().decode(source, version, documentId)
            val value = decoded.value ?: return AuthoringResult(null, decoded.diagnostics)
            val collected = PublicationRequirements(catalog).collect(value.contracts)
            val required = collected.value ?: return AuthoringResult(null, collected.diagnostics)
            if (required != value.required) return AuthoringResult(null, listOf(NodeDiagnostic("PUBLISHED_MANIFEST",
                "Manifest does not match compiled definitions", "loadPublication", documentId = documentId)))
            val errors = publicationCompatibility(required, available, documentId)
            return AuthoringResult(if (errors.isEmpty()) PublishedDefinition(version, documentId,
                value.contracts.entryDestination, value.contracts.artifacts.toMap()) else null, errors)
        }
    }
}

/** Collect from compiled artifacts, including generated navigation nodes and schema-declared event slots. */
class PublicationRequirements(private val catalog: NodeCatalog) {
    fun collect(contracts: CompiledContracts): AuthoringResult<TargetCapabilities> {
        val definitions = linkedMapOf<String, Int>()
        val methods = linkedSetOf<String>(); val providers = linkedSetOf<String>()
        val colors = linkedSetOf<String>(); val masks = linkedSetOf<String>()
        fun visit(node: ServerDrivenNode) {
            val definition = catalog[node.component] ?: throw PayloadException(node.diagnostic(
                "UNMODELED_DEFINITION", "Published definition is not in the compiler catalog", "publish"))
            definitions[node.component] = definition.version
            if (node.component == "action:method") node.property("invoke")?.let(methods::add)
            if (node.component == "navigation:node") node.property("type")?.let(providers::add)
            definition.properties.forEach { property -> when (property.kind) {
                PropertyKind.NODES -> node.propertyNodes(property.name).forEach(::visit)
                PropertyKind.COLOR_TOKEN -> node.property(property.name)?.let(colors::add)
                PropertyKind.MASK_TOKEN -> node.property(property.name)?.let(masks::add)
                else -> Unit
            } }
            node.children.orEmpty().forEach(::visit)
        }
        return try {
            contracts.artifacts.entries.sortedBy { it.key }.forEach { (path, source) -> visit(parseNode(source, documentId = path)) }
            AuthoringResult(TargetCapabilities(definitions, methods, providers, colors, masks), emptyList())
        } catch (error: PayloadException) { AuthoringResult(null, listOf(error.diagnostic)) }
    }
}
