package me.developes.humming.builder.ui

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

    @Test fun entryArrowFollowsTheActualEntryScreenAndUndo() {
        val workspace = LocalWorkspace(journeyExample())
        var document by mutableStateOf(workspace.document)
        val first = document.entryNodeId
        val second = document.nodes.values.first { it.kind == AuthorNodeKind.SCREEN && it.id != first }.id
        var mounted by mutableStateOf(true)
        compose.setContent { if (mounted) MaterialTheme { Box(Modifier.size(900.dp, 650.dp)) {
            InteractionCanvas(document, compileBuilderCanvas(workspace, target).requireValue(), null, select = { _, _ -> },
                screenNames = mapOf(first to "Início", second to "Detalhes"))
        } } }
        compose.onNodeWithTag("studio-entry-marker-$first").assertExists()
        compose.onNodeWithContentDescription("Tela inicial: Início").assertExists()
        compose.onNodeWithTag("studio-entry-marker-$second").assertDoesNotExist()
        compose.runOnIdle { workspace.apply(listOf(EditOperation.SetEntry(second))).requireSuccess(); document = workspace.document }
        compose.onNodeWithTag("studio-entry-marker-$first").assertDoesNotExist()
        compose.onNodeWithContentDescription("Tela inicial: Detalhes").assertExists()
        compose.runOnIdle { workspace.undo().requireSuccess(); document = workspace.document }
        compose.onNodeWithTag("studio-entry-marker-$first").assertExists()
        compose.onNodeWithTag("studio-entry-marker-$second").assertDoesNotExist()
        compose.runOnIdle { mounted = false }
    }

    @Test fun paletteTextCanBeDroppedInsideButtonChildren() {
        val workspace = LocalWorkspace().apply { importContract("""{"component":"layout:column","id":"root","properties":{"width":"400","height":"500"},"children":[{"component":"layout:button","id":"target","properties":{"width":"240","height":"80"},"children":[{"component":"layout:text","id":"label","properties":{"text":"Destino"}}]}]}""").requireValue() }
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
                "target", {}, {}, controls, showControls = false, palette = drag) { CompiledPreview(workspace.compile().requireValue()) } }
        } } }
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Destino", substring = false).fetchSemanticsNodes().isNotEmpty() }
        val card = compose.onNodeWithTag("builder-add-text")
        val origin = card.fetchSemanticsNode().boundsInRoot.topLeft
        val end = compose.onNodeWithText("Destino", substring = false).fetchSemanticsNode().boundsInRoot.center - origin
        card.performMouseInput { moveTo(center); press(); moveTo(end, 500) }
        compose.waitUntil(5_000) { drag.plan?.targetId == "target" }
        assertEquals("children", drag.plan!!.slot)
        assertEquals(initial, workspace.document)
        card.performMouseInput { release() }
        compose.waitUntil(5_000) { workspace.document.nodes.size == initial.nodes.size + 1 }
        val added = workspace.document.nodes.values.single { it.id !in initial.nodes }
        assertEquals("target", (added.placement as Placement.Child).parentId)
        compose.runOnIdle { workspace.undo().requireSuccess(); document = workspace.document; assertEquals(initial.nodes, document.nodes); mounted = false }
    }

    @Test fun actionFramesFollowSelectedViewportRatioAndKeepConnectionsMeasured() {
        val workspace = LocalWorkspace(journeyExample())
        val original = workspace.document
        var device by mutableStateOf(ProjectViewport.PHONE)
        var frames = emptyMap<String, Rect2>()
        var curves = emptyList<DrawnConnection>()
        var mounted by mutableStateOf(true)
        compose.setContent { if (mounted) MaterialTheme { Box(Modifier.size(900.dp, 650.dp)) {
            InteractionCanvas(original, compileBuilderCanvas(workspace, target).requireValue(), null, select = { _, _ -> },
                connect = { _, _, _ -> }, frameViewport = device, observe = { curves = it }, observePorts = { _, f -> frames = f })
        } } }
        for (preset in ProjectViewport.entries) {
            compose.runOnIdle { device = preset }
            val ratio = preset.width.toDouble() / preset.height
            compose.waitUntil(10_000) { frames.size == 2 && frames.values.all { kotlin.math.abs(it.width / it.height - ratio) < .005 } && curves.size == 1 }
            assertTrue(curves.single().flattened.toleranceMet)
            val rects = frames.values.sortedBy { it.x }
            assertTrue(rects[0].right < rects[1].x)
            assertEquals(original, workspace.document)
        }
        compose.runOnIdle { mounted = false }
    }

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
