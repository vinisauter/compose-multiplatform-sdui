package sample.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.layout.*
import me.developes.humming.sdui.layout.components.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import kotlin.test.*

class OfficialCatalogTest {
    @get:Rule val compose = createComposeRule()

    @Test fun everyRegisteredLayoutHasAnEditableCompilableDeterministicExample() {
        assertEquals(LayoutDefinitions.all.map { it.identifier }.toSet(), OfficialCatalog.components.map { it.component }.toSet())
        assertEquals(111, OfficialCatalog.components.size)
        for (entry in OfficialCatalog.all) {
            val document = entry.authoring()
            if (entry.component != null) assertTrue(document.nodes.values.any { it.definition == entry.component }, entry.id)
            assertEquals(emptyList(), initialNodeCatalog().validate(parseNode(entry.source), mode = ValidationMode.AUTHORING), entry.id)
            val compiled = entry.compiled()
            assertTrue(compiled.artifacts.isNotEmpty(), entry.id)
            if (entry.component != null) assertEquals(entry.source, CatalogFixture(entry.component.substringAfter(':')).source(), entry.id)
            if (entry.id.startsWith("action-")) assertEquals(entry.source, catalogActionSource(entry.id.removePrefix("action-")), entry.id)
        }
    }

    @Test fun compiledCatalogMountsEveryExampleWithRealProvidersAndScopes() {
        val current = mutableStateOf(OfficialCatalog.all.first())
        val mounted = mutableStateOf(true)
        compose.setContent { MaterialTheme { Surface(Modifier.size(900.dp, 760.dp)) {
            CompositionLocalProvider(LocalImageSources provides mapOf("catalog-mark" to @Composable { rememberVectorPainter(Icons.getIcon("Star")!!) })) {
                if (mounted.value) key(current.value.id) { CompiledPreview(current.value.compiled()) }
            }
        } } }
        for (entry in OfficialCatalog.all) {
            compose.runOnIdle { current.value = entry }
            compose.waitForIdle()
            compose.onAllNodesWithText("MATERIAL_SCOPE", substring = true).assertCountEquals(0)
            compose.onAllNodesWithText("MISSING_IMAGE_SOURCE", substring = true).assertCountEquals(0)
            compose.onAllNodesWithText("Limpar erro do preview").assertCountEquals(0)
            if (entry.id.startsWith("action-")) {
                val name = entry.id.removePrefix("action-")
                compose.onNodeWithText("Executar $name").performClick()
                val expected = when (name) {
                    "not", "xor" -> "Flag: false"
                    "and", "or", "equals" -> "Flag: true"
                    "greaterThan", "greaterThanOrEquals", "isNotEmpty" -> "Resultado: true"
                    "lessThan", "lessThanOrEquals", "isEmpty" -> "Resultado: false"
                    "update" -> "Resultado: Atualizado pelo handler"
                    "ifState" -> "Resultado: Ramo verdadeiro"
                    "then" -> "Resultado: Ramo aguardado"
                    "remove" -> "Temporário: "
                    "method" -> "Plataforma: ${me.developes.humming.sdui.getPlatform().name}"
                    else -> error(name)
                }
                compose.onNodeWithText(expected).assertExists()
            }
        }
        compose.runOnIdle { mounted.value = false }
    }

    @Test fun officialAppSearchRunsSourceAndOpensTheSameDocumentInStudio() {
        val exports = File("build/reports/official-catalog/exports").also { it.mkdirs() }
        val files = object : StudioFiles {
            override fun open(result: (StudioFileResult) -> Unit) { result(StudioFileResult(content = File(exports, "component-checkbox.json").readText())) }
            override fun download(name: String, content: String) { File(exports, name).writeText(content) }
        }
        compose.setContent { Box(Modifier.size(1300.dp, 900.dp)) { OfficialCatalogApp(files) } }
        compose.onNodeWithText("HummingUI • Catálogo oficial").assertExists()
        screenshot("catalog-overview.png")
        compose.onNodeWithTag("catalog-search").performTextReplacement("checkbox")
        compose.onNodeWithTag("catalog-entry-component-checkbox").performClick()
        compose.onNodeWithTag("catalog-preview").onChildren().fetchSemanticsNodes()
        compose.onNode(isToggleable()).performClick().assertIsOn()
        compose.onNodeWithText("Exportar documento").performClick()
        val exported = me.developes.humming.sdui.studio.AuthoringDocumentCodec(initialNodeCatalog())
            .decode(File(exports, "component-checkbox.json").readText()).requireValue()
        assertTrue(exported.nodes.values.any { it.definition == "layout:checkbox" })
        screenshot("catalog-checkbox.png")
        compose.onNodeWithText("JSON SDUI").performClick()
        screenshot("catalog-source.png")
        compose.onNodeWithTag("catalog-edit").performClick()
        compose.onNodeWithText("Voltar ao catálogo").assertExists()
        screenshot("catalog-studio.png")
    }

    @Test fun compactCatalogKeepsSearchAndInteractivePreviewAccessible() {
        compose.setContent { Box(Modifier.size(500.dp, 760.dp)) { OfficialCatalogApp(initialExampleId = "component-checkbox") } }
        compose.onNodeWithTag("catalog-search").performTextReplacement("checkbox")
        compose.onNode(isToggleable()).performClick().assertIsOn()
        screenshot("catalog-compact.png")
    }

    private fun screenshot(name: String) {
        val output = File("build/reports/official-catalog/$name").also { it.parentFile.mkdirs() }
        org.jetbrains.skia.Image.makeFromBitmap(compose.onRoot().captureToImage().asSkiaBitmap()).use { image ->
            requireNotNull(image.encodeToData(org.jetbrains.skia.EncodedImageFormat.PNG)).use { output.writeBytes(it.bytes) }
        }
    }
}
