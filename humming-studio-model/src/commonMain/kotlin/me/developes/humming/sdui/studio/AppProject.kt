package me.developes.humming.sdui.studio

import kotlinx.serialization.json.*
import kotlinx.serialization.SerializationException
import me.developes.humming.sdui.common.*

enum class ProjectViewport(val width: Int, val height: Int) { PHONE(390, 844), TABLET(820, 1180), DESKTOP(1280, 800) }

/** Project metadata wraps the existing authoring document; it does not duplicate nodes/history. */
data class AppProject(val name: String, val viewport: ProjectViewport, val screenNames: Map<String, String>, val document: AuthoringDocument, val designSystem: DesignSystem? = null)

class AppProjectCodec(private val catalog: NodeCatalog = initialNodeCatalog()) {
    private val documentCodec = AuthoringDocumentCodec(catalog)
    private val limits = PayloadLimits(maxInputChars = 8_000_000, maxPropertyChars = 8_000_000, maxJsonValues = 200_000)
    private fun validate(project: AppProject) {
        fun fail(path: String): Nothing = throw PayloadException(NodeDiagnostic("INVALID_PROJECT", "Invalid project metadata", "project", path, documentId = project.document.documentId))
        project.designSystem?.requireValid()
        if (project.name.isBlank() || project.name.length > 80) fail("$.name")
        val screens = project.document.nodes.values.filter { it.kind == AuthorNodeKind.SCREEN && it.placement is Placement.Root }.map { it.id }.toSet()
        if (project.screenNames.keys.any { it !in screens } || project.screenNames.values.any { it.isBlank() || it.length > 80 } ||
            project.screenNames.values.toSet().size != project.screenNames.size) fail("$.screenNames")
    }
    fun encode(project: AppProject): AuthoringResult<String> = try {
        validate(project)
        val document = documentCodec.encode(project.document)
        if (!document.isSuccess) AuthoringResult(null, document.diagnostics)
        else AuthoringResult(boundedCanonicalJson(buildJsonObject {
            put("formatVersion", if (project.designSystem == null) 1 else 2); put("name", project.name); put("viewport", project.viewport.name)
            put("screenNames", JsonObject(project.screenNames.mapValues { JsonPrimitive(it.value) }))
            project.designSystem?.let { put("designSystem", Json.encodeToJsonElement(it)) }
            put("document", parseBoundedJson(document.requireValue(), limits))
        }, limits.maxInputChars, operation = "projectEncode"), emptyList())
    } catch (error: PayloadException) { AuthoringResult(null, listOf(error.diagnostic)) }

    fun decode(source: String): AuthoringResult<AppProject> = try {
        fun fail(): Nothing = throw PayloadException(NodeDiagnostic("INVALID_PROJECT", "Expected project formatVersion 1 or 2", "projectDecode"))
        val root = parseBoundedJson(source, limits) as? JsonObject ?: fail()
        val version = (root["formatVersion"] as? JsonPrimitive)?.intOrNull ?: fail()
        val keys = setOf("formatVersion", "name", "viewport", "screenNames", "document")
        if (version !in 1..2 || root.keys != if (version == 1) keys else keys + "designSystem") fail()
        val design = if (version == 2) Json.decodeFromJsonElement<DesignSystem>(root.getValue("designSystem")).requireValid() else null
        fun string(value: JsonElement?): String = (value as? JsonPrimitive)?.takeIf { it.isString }?.content ?: fail()
        val device = ProjectViewport.entries.firstOrNull { it.name == string(root["viewport"]) } ?: fail()
        val names = (root["screenNames"] as? JsonObject)?.mapValues { string(it.value) } ?: fail()
        val document = documentCodec.decode(root.getValue("document").toString())
        if (!document.isSuccess) AuthoringResult(null, document.diagnostics)
        else {
            val project = AppProject(string(root["name"]), device, names, document.requireValue(), design)
            validate(project); AuthoringResult(project, emptyList())
        }
    } catch (error: PayloadException) { AuthoringResult(null, listOf(error.diagnostic))
    } catch (error: SerializationException) { AuthoringResult(null, listOf(NodeDiagnostic("INVALID_PROJECT", "Invalid design system JSON", "projectDecode", "$.designSystem"))) }
}
