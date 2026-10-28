package me.developes.humming.builder.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import me.developes.humming.sdui.layout.components.LocalNodeBoundsObserver
import me.developes.humming.sdui.studio.*
import org.junit.Rule
import org.junit.Test
import org.jetbrains.skia.Image
import org.jetbrains.skia.EncodedImageFormat
import java.io.File
import kotlin.test.*

@OptIn(ExperimentalTestApi::class)
class GeometryPreviewTest {
    @get:Rule val compose = createComposeRule()

    @Test fun selectedVisualSlotReceivesAnActualDragAndKeepsExistingSlotChildren() {
        val workspace = LocalWorkspace().apply { importContract("""{"component":"layout:scaffold","id":"root","properties":{"topBar":{"component":"layout:text","id":"heading","properties":{"text":"Cabeçalho"}}},"children":[{"component":"layout:text","id":"moving","properties":{"text":"Novo no slot"}}]}""").requireValue() }
        val document = mutableStateOf(workspace.document)
        val controls = PreviewControls().apply { editing.value = true }
        val mounted = mutableStateOf(true)
        compose.setContent { if (mounted.value) Box(Modifier.size(700.dp, 500.dp)) {
            val compiled = workspace.compile().requireValue()
            EditablePreview(workspace, document.value, compiled.sourceNodeIds, "root", {}, { document.value = workspace.document },
                controls, showControls = false, preferredSlots = mapOf("root" to "topBar")) { CompiledPreview(compiled) }
        } }
        awaitText("Novo no slot")
        val surface = compose.onNodeWithTag("studio-edit-surface")
        val origin = surface.fetchSemanticsNode().boundsInRoot.topLeft
        val start = compose.onNodeWithText("Novo no slot", substring = false).fetchSemanticsNode().boundsInRoot.center - origin
        val end = compose.onNodeWithText("Cabeçalho", substring = false).fetchSemanticsNode().boundsInRoot.center - origin
        surface.performMouseInput { moveTo(start); press(); moveTo(end, 500); release() }
        compose.waitUntil(5_000) { (workspace.document.nodes.getValue("moving").placement as? Placement.Child)?.slot == "topBar" }
        assertEquals(listOf("heading", "moving"), AuthoringIndices(workspace.document).childrenByParentAndSlot.getValue(ParentSlot("root", "topBar")).map { it.id })
        compose.onNodeWithText("Cabeçalho", substring = false).assertExists()
        compose.onNodeWithText("Novo no slot", substring = false).assertExists()
        assertTrue(workspace.compile().isSuccess)
        compose.runOnIdle { assertTrue(workspace.undo().isSuccess); document.value = workspace.document; mounted.value = false }
    }

    @Test fun dragsTextInsideARealButtonWithoutExecutingItsActionAndUndoRestoresParent() {
        val workspace = LocalWorkspace().apply { importContract("""{"component":"layout:column","id":"root","properties":{"width":"320","height":"300"},"children":[{"component":"layout:text","id":"moving","properties":{"text":"Texto arrastado"}},{"component":"layout:button","id":"target","properties":{"width":"240","height":"80","onClick":{"component":"action:update","id":"action","properties":{"state":"clicked","value":"sim"}}},"children":[{"component":"layout:text","id":"label","properties":{"text":"Botão destino"}}]},{"component":"layout:text","id":"result","properties":{"text":"Clique: #{clicked}"}}]}""").requireValue() }
        val mounted = mutableStateOf(true)
        compose.setContent { if (mounted.value) LocalStudio(workspace = workspace) }
        awaitText("Texto arrastado")
        compose.onNodeWithText("Modo edição").performClick()
        val surface = compose.onNodeWithTag("studio-edit-surface")
        val origin = surface.fetchSemanticsNode().boundsInRoot.topLeft
        val start = compose.onNodeWithText("Texto arrastado", substring = false).fetchSemanticsNode().boundsInRoot.center - origin
        val end = compose.onNodeWithText("Botão destino", substring = false).fetchSemanticsNode().boundsInRoot.center + Offset(30f, 0f) - origin
        surface.performMouseInput { moveTo(start); press(); moveTo(end, 500) }
        compose.waitForIdle()
        val guideImage = surface.captureToImage()
        val pixels = guideImage.toPixelMap()
        val pink = buildList {
            for (x in 0 until pixels.width) for (y in 0 until pixels.height) {
                val color = pixels[x, y]
                if (color.red > .6f && color.green < .2f && color.blue > .3f) add(x to y)
            }
        }
        assertTrue(pink.isNotEmpty(), "A guia de inserção deve estar visível")
        assertTrue(pink.maxOf { it.first } - pink.minOf { it.first } <= 5, "Button organiza horizontalmente: a guia deve ser vertical")
        assertTrue(pink.maxOf { it.second } - pink.minOf { it.second } > 10)
        val output = File("build/reports/button-drop-guide.png").apply { parentFile.mkdirs() }
        Image.makeFromBitmap(guideImage.asSkiaBitmap()).use { image ->
            requireNotNull(image.encodeToData(EncodedImageFormat.PNG)).use { output.writeBytes(it.bytes) }
        }
        surface.performMouseInput { release() }
        compose.waitUntil(5_000) { (workspace.document.nodes.getValue("moving").placement as? Placement.Child)?.parentId == "target" }
        assertEquals(listOf("label", "moving"), children(workspace, "target"))
        assertTrue(workspace.compile().isSuccess)
        compose.waitForIdle()
        val buttonBounds = compose.onNode(hasText("Botão destino", substring = true) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)).fetchSemanticsNode().boundsInRoot
        val labelBounds = compose.onAllNodesWithText("Botão destino", substring = false, useUnmergedTree = true).fetchSemanticsNodes().single { buttonBounds.contains(it.boundsInRoot.center) }.boundsInRoot
        val movedBounds = compose.onAllNodesWithText("Texto arrastado", substring = false, useUnmergedTree = true).fetchSemanticsNodes().single { buttonBounds.contains(it.boundsInRoot.center) }.boundsInRoot
        assertTrue(movedBounds.left >= labelBounds.right - 1, "O texto deve aparecer ao lado, conforme a guia vertical")
        compose.onNodeWithText("Clique: ", substring = false).assertExists()
        compose.onNodeWithText("Desfazer").performClick()
        compose.runOnIdle { assertEquals("root", (workspace.document.nodes.getValue("moving").placement as Placement.Child).parentId); mounted.value = false }
    }

    @Test fun observesActualOuterAndContentBoundsIncludingPaddingWithoutLayoutWrappers() {
        val workspace = LocalWorkspace().apply { importContract(DROP_EXAMPLE).requireValue() }
        val compiled = workspace.compile().requireValue()
        val geometry = PreviewGeometry(workspace.document, compiled.sourceNodeIds)
        val mounted = mutableStateOf(true)
        compose.setContent { if (mounted.value) Box(Modifier.size(400.dp, 120.dp).clipToBounds().onGloballyPositioned(geometry::placeViewport)) {
            CompositionLocalProvider(LocalNodeBoundsObserver provides geometry) { CompiledPreview(compiled.artifacts.getValue(compiled.entryDestination)) }
        } }
        compose.waitUntil(10_000) { geometry.snapshot(CanvasViewport()).nodes.size == 7 }
        compose.runOnIdle {
            val snapshot = geometry.snapshot(CanvasViewport())
            val left = snapshot.nodes.getValue("drop-left")
            val content = left.container!!.content
            assertEquals(16.0, left.rect.width - content.width, .01)
            assertEquals(16.0, left.rect.height - content.height, .01)
            assertEquals(left.rect.x + 8, content.x, .01)
            assertEquals(left.rect.y + 8, content.y, .01)
            assertEquals(content.x, snapshot.nodes.getValue("drop-a").rect.x, .01)
            assertTrue(left.clips.single().rect.bottom <= 120.0)
            mounted.value = false
        }
        compose.waitForIdle()
        compose.runOnIdle { assertTrue(geometry.snapshot(CanvasViewport()).nodes.isEmpty()) }
    }

    @Test fun dragsBetweenRealContainersAndUndoRestoresTheExportedOrder() {
        val workspace = LocalWorkspace().apply { importContract(DROP_EXAMPLE).requireValue() }
        val mounted = mutableStateOf(true)
        compose.setContent { if (mounted.value) LocalStudio(workspace = workspace) }
        awaitText("A")
        compose.onNodeWithText("Modo edição").performClick()
        val surface = compose.onNodeWithTag("studio-edit-surface")
        val origin = surface.fetchSemanticsNode().boundsInRoot.topLeft
        val start = compose.onNodeWithText("A", substring = false).fetchSemanticsNode().boundsInRoot.center - origin
        surface.performMouseInput { moveTo(start); press(); moveTo(Offset(180f, 130f)) }
        compose.waitForIdle()
        val guide = surface.captureToImage()
        val pixels = guide.toPixelMap()
        assertTrue((0 until pixels.width).any { x -> (0 until pixels.height).any { y ->
            val color = pixels[x, y]; color.red > .6f && color.green < .2f && color.blue > .3f
        } }, "A guia de drop deve estar pintada antes do release")
        val output = File("build/reports/drop-guide.png")
        output.parentFile.mkdirs()
        Image.makeFromBitmap(guide.asSkiaBitmap()).use { image ->
            requireNotNull(image.encodeToData(EncodedImageFormat.PNG)).use { output.writeBytes(it.bytes) }
        }
        surface.performMouseInput { release() }
        compose.waitUntil(5_000) { (workspace.document.nodes.getValue("drop-a").placement as? Placement.Child)?.parentId == "drop-right" }
        compose.runOnIdle { assertEquals(listOf("drop-a"), children(workspace, "drop-right")) }
        compose.onNodeWithText("Modo execução").assertExists() // editing survives the revision change
        compose.onNodeWithText("Desfazer").performClick()
        compose.runOnIdle { assertEquals(listOf("drop-a", "drop-b"), children(workspace, "drop-left")); assertTrue(children(workspace, "drop-right").isEmpty()) }
        compose.runOnIdle { mounted.value = false }
        compose.waitForIdle()
    }

    @Test fun escapeCancelsAnActualCapturedDragWithoutChangingTheDocument() {
        val workspace = LocalWorkspace().apply { importContract(DROP_EXAMPLE).requireValue() }
        val mounted = mutableStateOf(true)
        compose.setContent { if (mounted.value) LocalStudio(workspace = workspace) }
        awaitText("A")
        compose.onNodeWithText("Modo edição").performClick()
        val before = workspace.document
        val surface = compose.onNodeWithTag("studio-edit-surface")
        val origin = surface.fetchSemanticsNode().boundsInRoot.topLeft
        val start = compose.onNodeWithText("A", substring = false).fetchSemanticsNode().boundsInRoot.center - origin
        surface.performMouseInput { moveTo(start); press(); moveTo(Offset(180f, 130f)) }
        surface.performKeyInput { pressKey(Key.Escape) }
        surface.performMouseInput { release() }
        compose.runOnIdle { assertEquals(before, workspace.document); mounted.value = false }
        compose.waitForIdle()
    }

    @Test fun dragUsesMeasuredCoordinatesAfterZoomAndPan() {
        val workspace = LocalWorkspace().apply { importContract(DROP_EXAMPLE).requireValue() }
        val mounted = mutableStateOf(true)
        compose.setContent { if (mounted.value) LocalStudio(workspace = workspace) }
        awaitText("A")
        compose.onNodeWithText("Zoom +").performClick()
        compose.onNodeWithText("Direita", substring = false).performClick()
        compose.onNodeWithText("Modo edição").performClick()
        val surface = compose.onNodeWithTag("studio-edit-surface")
        val origin = surface.fetchSemanticsNode().boundsInRoot.topLeft
        val start = compose.onNodeWithText("A", substring = false).fetchSemanticsNode().boundsInRoot.center - origin
        surface.performMouseInput { moveTo(start); press(); moveTo(Offset(265f, 162.5f)); release() }
        compose.waitUntil(5_000) { (workspace.document.nodes.getValue("drop-a").placement as? Placement.Child)?.parentId == "drop-right" }
        compose.runOnIdle { mounted.value = false }
        compose.waitForIdle()
    }

    @Test fun losingFocusDuringACapturedDragCancelsIt() {
        val workspace = LocalWorkspace().apply { importContract(DROP_EXAMPLE).requireValue() }
        val mounted = mutableStateOf(true)
        compose.setContent { if (mounted.value) LocalStudio(workspace = workspace) }
        awaitText("A")
        compose.onNodeWithText("Modo edição").performClick()
        val before = workspace.document
        val surface = compose.onNodeWithTag("studio-edit-surface")
        val origin = surface.fetchSemanticsNode().boundsInRoot.topLeft
        val start = compose.onNodeWithText("A", substring = false).fetchSemanticsNode().boundsInRoot.center - origin
        surface.performMouseInput { moveTo(start); press(); moveTo(Offset(180f, 130f)) }
        surface.performKeyInput { pressKey(Key.Tab) }
        surface.performMouseInput { release() }
        compose.runOnIdle { assertEquals(before, workspace.document); mounted.value = false }
        compose.waitForIdle()
    }

    @Test fun editModeSelectsWithoutExecutingTheFormButton() {
        val workspace = LocalWorkspace().apply { importContract("""{"component":"layout:column","id":"root","children":[{"component":"layout:button","id":"button","properties":{"onClick":{"component":"action:update","id":"action","properties":{"state":"status","value":"confirmado"}}},"children":[{"component":"layout:text","id":"label","properties":{"text":"Confirmar"}}]},{"component":"layout:text","id":"status","properties":{"text":"Status: #{status}"}}]}""").requireValue() }
        val mounted = mutableStateOf(true)
        compose.setContent { if (mounted.value) LocalStudio(workspace = workspace) }
        awaitText("Status: ")
        compose.onNodeWithText("Modo edição").performClick()
        compose.onNode(hasText("Confirmar", substring = false) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)).performClick()
        compose.onNodeWithText("Status: ", substring = false).assertExists()
        compose.runOnIdle { assertEquals(0, workspace.document.revision) }
        compose.onNodeWithText("Modo execução").performClick()
        compose.onNode(hasText("Confirmar", substring = false) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)).performClick()
        awaitText("Status: confirmado")
        compose.runOnIdle { mounted.value = false }
        compose.waitForIdle()
    }

    @Test fun emptyScreenExplainsWhyThereIsNothingToDragAndLoadsTheExampleInPlace() {
        val workspace = LocalWorkspace()
        val mounted = mutableStateOf(true)
        compose.setContent { if (mounted.value) LocalStudio(workspace = workspace) }
        compose.onNodeWithTag("studio-empty-preview").assertTextContains("ainda não há elementos para arrastar", substring = true)
        compose.onNodeWithText("Modo edição").performClick()
        compose.onNodeWithText("Carregar exemplo de arraste").performClick()
        awaitText("Arraste A ou B entre os containers")
        compose.onNodeWithTag("studio-empty-preview").assertDoesNotExist()
        compose.onNodeWithText("Modo execução").assertExists()
        compose.runOnIdle {
            assertEquals(listOf("drop-a", "drop-b"), children(workspace, "drop-left"))
            assertTrue(children(workspace, "drop-right").isEmpty())
            mounted.value = false
        }
        compose.waitForIdle()
    }

    private fun awaitText(value: String) { compose.waitUntil(10_000) { compose.onAllNodesWithText(value, substring = false).fetchSemanticsNodes().isNotEmpty() } }
    private fun children(workspace: LocalWorkspace, parent: String) = AuthoringIndices(workspace.document)
        .childrenByParentAndSlot[ParentSlot(parent, "children")].orEmpty().map { it.id }
}
