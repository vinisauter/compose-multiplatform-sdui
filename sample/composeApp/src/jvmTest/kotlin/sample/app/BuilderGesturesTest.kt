package sample.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.studio.*
import org.junit.Rule
import org.junit.Test
import kotlin.test.*

@OptIn(ExperimentalTestApi::class)
class BuilderGesturesTest {
    @get:Rule val compose = createComposeRule()
    private val target = TargetCapabilities(initialNodeCatalog().capabilities, providers = setOf("compiled-preview"),
        colorTokens = defaultPresentationTokens().colorNames, maskTokens = defaultPresentationTokens().maskNames)

    @Test fun disconnectedScreensRemainVisibleAndPointerCreatesAnUndoableNavigation() {
        val workspace = LocalWorkspace()
        val first = workspace.document.entryNodeId
        workspace.insertVisual(first, "layout:button").requireSuccess()
        val button = workspace.document.nodes.values.single { it.definition == "layout:button" }.id
        workspace.addScreen(CanvasPoint(400.0, 0.0)).requireSuccess()
        workspace.addScreen(CanvasPoint(800.0, 0.0)).requireSuccess()
        val second = AuthoringIndices(workspace.document).roots.filter { it.kind == AuthorNodeKind.SCREEN }[1].id
        val initial = workspace.document
        val all = compileBuilderCanvas(workspace, target).requireValue()
        val roots = all.artifacts.values.map { parseNode(it).id }.toSet()
        assertTrue(initial.nodes.values.filter { it.kind == AuthorNodeKind.SCREEN }.all { it.id in roots })
        var document by mutableStateOf(initial)
        var ports = emptyList<Triple<String, String, Point2>>()
        var frames = emptyMap<String, Rect2>()
        var mounted by mutableStateOf(true)
        compose.setContent { if (mounted) MaterialTheme { Box(Modifier.size(1000.dp, 650.dp)) {
            InteractionCanvas(document, compileBuilderCanvas(workspace, target).requireValue(), null, select = { _, _ -> },
                connect = { source, port, destination -> workspace.addAction(source, port, "navigation:goTo", destination).requireSuccess(); document = workspace.document },
                observePorts = { p, f -> ports = p; frames = f })
        } } }
        compose.waitUntil(10_000) { ports.any { it.first == button } && frames.size == 3 }
        val canvas = compose.onNodeWithTag("studio-interaction-canvas")
        fun start() = ports.first { it.first == button }.third.let { Offset(it.x.toFloat(), it.y.toFloat()) }
        fun end() = frames.getValue(second).let { Offset((it.x + it.width / 2).toFloat(), (it.y + it.height / 2).toFloat()) }
        canvas.performMouseInput { moveTo(start()); press(); moveTo(end(), 500); release() }
        compose.waitUntil(5_000) { document.edges.values.any { it.role == EdgeRole.NAVIGATION } }
        assertEquals(second, InteractionProjection.navigationLinks(document).requireValue().single().targetScreenId)
        compose.runOnIdle { workspace.undo().requireSuccess(); document = workspace.document }
        compose.waitUntil(5_000) { document.edges.isEmpty() }
        canvas.performMouseInput { moveTo(start()); press(); moveTo(end(), 500) }
        canvas.performKeyInput { pressKey(Key.Escape) }
        canvas.performMouseInput { release() }
        compose.runOnIdle { assertTrue(document.edges.isEmpty()) }
        canvas.performMouseInput { moveTo(start()); press(); moveTo(Offset(-20f, -20f), 500); release() }
        compose.runOnIdle { assertTrue(document.edges.isEmpty()); mounted = false }
    }

    @Test fun palettePointerDropsIntoMeasuredAutoLayoutAndCancelsOutside() {
        val workspace = LocalWorkspace()
        var document by mutableStateOf(workspace.document)
        val initial = document
        val drag = PaletteDragState()
        val controls = PreviewControls().apply { editing.value = true }
        var mounted by mutableStateOf(true)
        compose.setContent { if (mounted) MaterialTheme { Row(Modifier.size(900.dp, 600.dp)) {
            Box(Modifier.width(220.dp)) { BuilderPaletteCard("text", drag, click = {}, drop = { name, plan ->
                insertBuilderComponent(workspace, plan.targetId, plan.slot, name, plan).requireSuccess(); document = workspace.document
            }) }
            Box(Modifier.weight(1f)) { EditablePreview(workspace, document, workspace.compile().requireValue().sourceNodeIds,
                document.entryNodeId, {}, {}, controls, showControls = false, palette = drag) { CompiledPreview(workspace.compile().requireValue()) } }
        } } }
        val card = compose.onNodeWithTag("builder-add-text")
        val cardBounds = card.fetchSemanticsNode().boundsInRoot
        val surface = compose.onNodeWithTag("studio-edit-surface").fetchSemanticsNode().boundsInRoot
        val destination = Offset(surface.left + 70, surface.top + 90) - cardBounds.topLeft
        card.performMouseInput { moveTo(center); press(); moveTo(destination, 600) }
        compose.waitUntil(5_000) { drag.plan != null }
        assertEquals(0, drag.plan!!.targetIndex)
        assertEquals(initial.nodes, workspace.document.nodes)
        card.performMouseInput { release() }
        compose.waitUntil(5_000) { workspace.document.nodes.size == 2 }
        compose.runOnIdle { workspace.undo().requireSuccess(); document = workspace.document }
        assertEquals(initial.nodes, workspace.document.nodes)
        card.performMouseInput { moveTo(center); press(); moveTo(Offset(-20f, -20f), 400); release() }
        compose.runOnIdle { assertEquals(initial.nodes, workspace.document.nodes); mounted = false }
    }

    private fun EditOutcome.requireSuccess() { assertTrue(isSuccess, diagnostics.toString()) }
}
