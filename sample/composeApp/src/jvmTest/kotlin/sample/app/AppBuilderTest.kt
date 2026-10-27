package sample.app

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.junit.After
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import me.developes.humming.sdui.common.initialNodeCatalog
import me.developes.humming.sdui.studio.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import kotlin.test.*

@OptIn(ExperimentalTestApi::class)
class AppBuilderTest {
    @get:Rule val compose = createComposeRule()
    private val owner = object : LifecycleOwner { override val lifecycle = LifecycleRegistry(this) }
    @After fun closeOwner() { compose.runOnIdle { if (owner.lifecycle.currentState == Lifecycle.State.INITIALIZED) owner.lifecycle.currentState = Lifecycle.State.CREATED; owner.lifecycle.currentState = Lifecycle.State.DESTROYED } }

    @Test fun builderTreeDragIsSavedAndUndoRestoresTheOriginalParent() {
        val initial = LegacyAuthoringCodec(initialNodeCatalog()).importDocument("""{"component":"layout:column","id":"root","properties":{"fillMaxSize":"1"},"children":[{"component":"layout:text","id":"moving","properties":{"text":"Mover pela árvore"}},{"component":"layout:button","id":"target","children":[{"component":"layout:text","id":"label","properties":{"text":"Destino"}}]}]}""", "tree-project").requireValue()
        var saved = ""
        val files = object : StudioFiles {
            override fun open(result: (StudioFileResult) -> Unit) = Unit
            override fun download(name: String, content: String) { saved = content }
        }
        val mounted = mutableStateOf(true)
        compose.runOnIdle { owner.lifecycle.currentState = Lifecycle.State.RESUMED }
        compose.setContent { CompositionLocalProvider(LocalLifecycleOwner provides owner) { if (mounted.value) Box(Modifier.size(1400.dp, 950.dp)) { AppBuilder(files, initial) } } }
        val tree = compose.onNodeWithTag("builder-tree")
        val origin = tree.fetchSemanticsNode().boundsInRoot.topLeft
        val start = compose.onNodeWithTag("builder-node-moving").fetchSemanticsNode().boundsInRoot.center - origin
        val end = compose.onNodeWithTag("builder-node-target").fetchSemanticsNode().boundsInRoot.center - origin
        tree.performMouseInput { moveTo(start); press(); moveTo(end, 400) }
        compose.onNodeWithTag("builder-node-target").assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.StateDescription, "Dentro de button · children"))
        tree.performMouseInput { release() }
        compose.onNodeWithText("Salvar projeto", substring = false).performClick()
        assertEquals("target", (AppProjectCodec().decode(saved).requireValue().document.nodes.getValue("moving").placement as Placement.Child).parentId)
        compose.onNodeWithText("Desfazer", substring = false).performClick()
        compose.onNodeWithText("Salvar projeto", substring = false).performClick()
        assertEquals("root", (AppProjectCodec().decode(saved).requireValue().document.nodes.getValue("moving").placement as Placement.Child).parentId)
        compose.runOnIdle { mounted.value = false }
    }

    @Test fun builderSelectionDuringPointerCaptureDoesNotCancelMovingTextIntoButton() {
        val initial = LegacyAuthoringCodec(initialNodeCatalog()).importDocument("""{"component":"layout:column","id":"root","properties":{"fillMaxSize":"1"},"children":[{"component":"layout:text","id":"moving","properties":{"text":"Texto para mover"}},{"component":"layout:button","id":"target","properties":{"width":"240","height":"80"},"children":[{"component":"layout:text","id":"label","properties":{"text":"Destino"}}]}]}""", "drag-project").requireValue()
        var saved = ""
        val files = object : StudioFiles {
            override fun open(result: (StudioFileResult) -> Unit) = Unit
            override fun download(name: String, content: String) { saved = content }
        }
        val mounted = mutableStateOf(true)
        compose.runOnIdle { owner.lifecycle.currentState = Lifecycle.State.RESUMED }
        compose.setContent { CompositionLocalProvider(LocalLifecycleOwner provides owner) { if (mounted.value) Box(Modifier.size(1400.dp, 950.dp)) { AppBuilder(files, initial) } } }
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Texto para mover", substring = false).fetchSemanticsNodes().isNotEmpty() }
        val surface = compose.onNodeWithTag("studio-edit-surface")
        val origin = surface.fetchSemanticsNode().boundsInRoot.topLeft
        val start = compose.onNodeWithText("Texto para mover", substring = false).fetchSemanticsNode().boundsInRoot.center - origin
        val end = compose.onNodeWithText("Destino", substring = false).fetchSemanticsNode().boundsInRoot.center - origin
        surface.performMouseInput { moveTo(start); press(); moveTo(end, 500); release() }
        compose.waitForIdle()
        compose.onNodeWithText("Salvar projeto", substring = false).performClick()
        val document = AppProjectCodec().decode(saved).requireValue().document
        assertEquals("target", (document.nodes.getValue("moving").placement as Placement.Child).parentId)
        compose.runOnIdle { mounted.value = false }
    }

    @Test fun paletteCompositionsKeepTheirScopesIsolateIdsAndUndoAsOneDelivery() {
        for (entry in OfficialCatalog.components) {
            val workspace = LocalWorkspace()
            val before = workspace.document
            val outcome = insertBuilderComponent(workspace, before.entryNodeId, "children", entry.component!!.substringAfter(':'))
            assertTrue(outcome.isSuccess, "${entry.component}: ${outcome.diagnostics}")
            assertEquals(before.revision + 1, workspace.document.revision)
            assertTrue(workspace.compile().isSuccess, "${entry.component}: ${workspace.compile().diagnostics}")
            assertTrue(workspace.undo().isSuccess)
            assertEquals(before.nodes, workspace.document.nodes)
            assertEquals(before.edges, workspace.document.edges)
        }
        val workspace = LocalWorkspace()
        repeat(2) { val result = insertBuilderComponent(workspace, workspace.document.entryNodeId, "children", "checkbox"); assertTrue(result.isSuccess, result.diagnostics.toString()) }
        val keys = workspace.document.nodes.values.filter { it.definition == "layout:checkbox" }.map { it.properties["stateKey"].toString() }
        assertEquals(2, keys.toSet().size)
    }

    @Test fun createsDesignsConnectsRunsAndRecoversARealProjectFile() {
        val directory = File("build/reports/app-builder").also { it.mkdirs() }
        val files = object : StudioFiles {
            override fun open(result: (StudioFileResult) -> Unit) { result(StudioFileResult(content = File(directory, "humming-app-project.json").readText())) }
            override fun download(name: String, content: String) { File(directory, name).writeText(content) }
        }
        val mounted = mutableStateOf(true)
        compose.runOnIdle { owner.lifecycle.currentState = Lifecycle.State.RESUMED }
        compose.setContent { CompositionLocalProvider(LocalLifecycleOwner provides owner) { if (mounted.value) Box(Modifier.size(1400.dp, 950.dp)) { AppBuilder(files, LocalWorkspace.blankDocument()) } } }
        compose.onNodeWithText("Novo", substring = false).performClick()
        compose.onNodeWithTag("builder-new-name").performTextReplacement("Meu primeiro app")
        compose.onNodeWithText("Em branco", substring = false).performClick()
        compose.onNodeWithText("Criar projeto", substring = false).performClick()
        add("text", "Olá do meu app")
        compose.onNodeWithText("Telas", substring = false).performClick()
        compose.onNodeWithTag("builder-add-screen").performClick()
        add("text", "Segunda tela pronta")
        add("button", "Voltar")
        compose.onNodeWithText("Ações", substring = false).performClick()
        compose.onNodeWithTag("builder-back").performScrollTo().performClick()
        compose.onNodeWithText("Propriedades", substring = false).performClick()
        compose.onNodeWithText("Telas", substring = false).performClick()
        compose.onNodeWithText("Inicial · Tela 1", substring = false).performClick()
        add("button")
        screenshot("builder-design.png", directory)
        compose.onNodeWithText("Ações", substring = false).performClick()
        compose.onNodeWithTag("builder-connect").performScrollTo().performClick()
        compose.onNodeWithTag("builder-step-2").performClick()
        screenshot("builder-actions.png", directory)
        compose.onNodeWithText("Executar app", substring = false).performClick()
        compose.onNodeWithText("Olá do meu app", substring = false).assertExists()
        compose.onNodeWithText("Confirmar", substring = false).performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Segunda tela pronta", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
        screenshot("builder-preview.png", directory)
        compose.onNodeWithText("Voltar", substring = false).performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Olá do meu app", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("builder-step-4").performClick()
        compose.onNodeWithText("Salvar projeto completo", substring = false).performClick()
        compose.onNodeWithText("Exportar jornada SDUI", substring = false).performClick()
        val project = AppProjectCodec().decode(File(directory, "humming-app-project.json").readText()).requireValue()
        assertEquals("Meu primeiro app", project.name)
        assertEquals(2, project.document.nodes.values.count { it.kind == AuthorNodeKind.SCREEN })
        assertTrue(project.document.edges.values.any { it.role == EdgeRole.NAVIGATION })
        assertTrue(CompiledBundleCodec().decode(File(directory, "humming-app-contracts.json").readText()).requireValue().artifacts.size >= 3)
        screenshot("builder-export.png", directory)
        compose.onNodeWithText("Abrir projeto", substring = false).performClick()
        compose.onNodeWithText("Continuar", substring = false).performClick()
        compose.onNodeWithTag("builder-title").assertTextContains("Meu primeiro app")
        compose.runOnIdle { mounted.value = false }
    }

    private fun add(name: String, text: String? = null) {
        compose.onNodeWithText("Elementos", substring = false).performClick()
        compose.onNodeWithTag("builder-search").performTextReplacement(name)
        compose.onNodeWithTag("builder-add-$name").performClick()
        if (text != null) {
            if (name == "button") {
                compose.onNodeWithText("Telas", substring = false).performClick()
                compose.onAllNodesWithText("text", substring = false).onLast().performClick()
            }
            compose.onNodeWithTag("studio-property-value").performScrollTo().performTextReplacement(text)
            compose.onNodeWithText("Aplicar propriedade", substring = false).performScrollTo().performClick()
            if (name == "button") compose.onAllNodesWithText("button", substring = false).onLast().performClick()
        }
    }
    private fun screenshot(name: String, directory: File) {
        org.jetbrains.skia.Image.makeFromBitmap(compose.onRoot().captureToImage().asSkiaBitmap()).use { image ->
            requireNotNull(image.encodeToData(org.jetbrains.skia.EncodedImageFormat.PNG)).use { File(directory, name).writeBytes(it.bytes) }
        }
    }
}
