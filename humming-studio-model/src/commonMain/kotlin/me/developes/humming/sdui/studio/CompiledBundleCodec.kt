package me.developes.humming.sdui.studio

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.developes.humming.sdui.common.*

/** Local transport envelope. Artifacts retain the existing runtime wire format. */
class CompiledBundleCodec {
    @Serializable private data class Bundle(val formatVersion: Int = 1, val contracts: CompiledContracts)
    private val json = Json { encodeDefaults = true }
    private val limits = PayloadLimits(maxInputChars = 8_000_000, maxPropertyChars = 4_000_000)

    fun encode(contracts: CompiledContracts): AuthoringResult<String> {
        val errors = validate(contracts)
        if (errors.isNotEmpty()) return AuthoringResult(null, errors)
        val canonical = contracts.copy(artifacts = contracts.artifacts.entries.sortedBy { it.key }.associate { it.toPair() },
            sourceNodeIds = contracts.sourceNodeIds.entries.sortedBy { it.key }.associate { it.toPair() })
        val source = json.encodeToString(Bundle(contracts = canonical))
        return try {
            parseBoundedJson(source, limits)
            AuthoringResult(source, emptyList())
        } catch (error: PayloadException) { AuthoringResult(null, listOf(error.diagnostic)) }
    }

    fun decode(source: String): AuthoringResult<CompiledContracts> = try {
        parseBoundedJson(source, limits)
        val bundle = json.decodeFromString<Bundle>(source)
        val errors = if (bundle.formatVersion != 1) listOf(diagnostic("Versão do pacote não suportada")) else validate(bundle.contracts)
        AuthoringResult(bundle.contracts.takeIf { errors.isEmpty() }, errors)
    } catch (error: PayloadException) { AuthoringResult(null, listOf(error.diagnostic))
    } catch (error: SerializationException) { AuthoringResult(null, listOf(diagnostic("Pacote inválido: ${error.message}"))) }

    private fun validate(contracts: CompiledContracts): List<NodeDiagnostic> {
        if (contracts.artifacts.isEmpty() || contracts.artifacts.size > 256 ||
            contracts.entryDestination !in contracts.artifacts || contracts.artifacts.keys.any {
                !it.matches(Regex("screen\\.json|graph\\.json|screens/[0-9a-f]+\\.json"))
            } || contracts.artifacts.values.sumOf { it.length.toLong() } > 4_000_000 || contracts.sourceNodeIds.size > 20_000 ||
            contracts.revision < 0 || contracts.documentId.isBlank() ||
            (contracts.providerType == null && (contracts.entryDestination != "screen.json" || contracts.artifacts.size != 1)) ||
            (contracts.providerType != null && (contracts.providerType.isBlank() || contracts.entryDestination != "graph.json")))
            return listOf(diagnostic("Manifesto de artefatos inválido"))
        return contracts.artifacts.flatMap { (path, source) ->
            try { parseNode(source, documentId = path); emptyList() }
            catch (error: PayloadException) { listOf(error.diagnostic) }
        }
    }

    private fun diagnostic(message: String) = NodeDiagnostic("COMPILED_BUNDLE", message, "compiledBundle")
}
