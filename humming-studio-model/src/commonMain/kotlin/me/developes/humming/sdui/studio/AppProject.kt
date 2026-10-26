package me.developes.humming.sdui.studio

import kotlinx.serialization.json.*
import me.developes.humming.sdui.common.*

enum class ProjectViewport(val width: Int, val height: Int) { PHONE(390, 844), TABLET(820, 1180), DESKTOP(1280, 800) }

/** Project metadata wraps the existing authoring document; it does not duplicate nodes/history. */
data class AppProject(val name: String, val viewport: ProjectViewport, val screenNames: Map<String, String>, val document: AuthoringDocument)

class AppProjectCodec(private val catalog: NodeCatalog = initialNodeCatalog()) {
    private val documentCodec = AuthoringDocumentCodec(catalog)
    private val limits = PayloadLimits(maxInputChars = 8_000_000, maxPropertyChars = 8_000_000, maxJsonValues = 200_000)
    private fun validate(project: AppProject) {
        fun fail(path: String): Nothing = throw PayloadException(NodeDiagnostic("INVALID_PROJECT", "Invalid project metadata", "project", path, documentId = project.document.documentId))
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
            put("formatVersion", 1); put("name", project.name); put("viewport", project.viewport.name)
            put("screenNames", JsonObject(project.screenNames.mapValues { JsonPrimitive(it.value) }))
            put("document", parseBoundedJson(document.requireValue(), limits))
        }, limits.maxInputChars, operation = "projectEncode"), emptyList())
    } catch (error: PayloadException) { AuthoringResult(null, listOf(error.diagnostic)) }

    fun decode(source: String): AuthoringResult<AppProject> = try {
        fun fail(): Nothing = throw PayloadException(NodeDiagnostic("INVALID_PROJECT", "Expected project formatVersion 1", "projectDecode"))
        val root = parseBoundedJson(source, limits) as? JsonObject ?: fail()
        if (root.keys != setOf("formatVersion", "name", "viewport", "screenNames", "document") || root["formatVersion"] != JsonPrimitive(1)) fail()
        fun string(value: JsonElement?): String = (value as? JsonPrimitive)?.takeIf { it.isString }?.content ?: fail()
        val device = ProjectViewport.entries.firstOrNull { it.name == string(root["viewport"]) } ?: fail()
        val names = (root["screenNames"] as? JsonObject)?.mapValues { string(it.value) } ?: fail()
        val document = documentCodec.decode(root.getValue("document").toString())
        if (!document.isSuccess) AuthoringResult(null, document.diagnostics)
        else {
            val project = AppProject(string(root["name"]), device, names, document.requireValue())
            validate(project); AuthoringResult(project, emptyList())
        }
    } catch (error: PayloadException) { AuthoringResult(null, listOf(error.diagnostic)) }
}
