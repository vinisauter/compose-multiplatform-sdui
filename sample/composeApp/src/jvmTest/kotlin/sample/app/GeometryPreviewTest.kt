package sample.app

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

    private fun awaitText(value: String) { compose.waitUntil(10_000) { compose.onAllNodesWithText(value, substring = false).fetchSemanticsNodes().isNotEmpty() } }
    private fun children(workspace: LocalWorkspace, parent: String) = AuthoringIndices(workspace.document)
        .childrenByParentAndSlot[ParentSlot(parent, "children")].orEmpty().map { it.id }
}
