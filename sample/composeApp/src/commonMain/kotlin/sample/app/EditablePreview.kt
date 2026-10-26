package sample.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import me.developes.humming.sdui.layout.components.LocalNodeBoundsObserver
import me.developes.humming.sdui.studio.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class PreviewControls {
    val editing = mutableStateOf(false)
    val viewport = mutableStateOf(CanvasViewport())
}

@OptIn(ExperimentalUuidApi::class, ExperimentalLayoutApi::class)
@Composable
internal fun EditablePreview(workspace: LocalWorkspace, document: AuthoringDocument, sourceIds: Map<String, String>,
                    selectedId: String, select: (String) -> Unit, commit: (EditOutcome) -> Unit,
                    controls: PreviewControls, showControls: Boolean = true, content: @Composable () -> Unit) {
    val geometry = remember(document) { PreviewGeometry(document, sourceIds) }
    val gesture = remember(document) { DropGesture() }
    val focus = remember { FocusRequester() }
    var editing by controls.editing
    var view by controls.viewport
    val density = LocalDensity.current.density.toDouble()
    val viewport = view.copy(density = density)
    val currentViewport by rememberUpdatedState(viewport)
    val onSelect by rememberUpdatedState(select)
    val onCommit by rememberUpdatedState(commit)
    var guide by remember { mutableStateOf<DropPlan?>(null) }
    var diagnostic by remember { mutableStateOf("") }
    fun cancel() { gesture.cancel(); guide = null }
    val window = LocalWindowInfo.current
    LaunchedEffect(window.isWindowFocused) { if (!window.isWindowFocused) cancel() }
    Column(Modifier.fillMaxSize()) {
        if (showControls) FlowRow {
            TextButton(onClick = { cancel(); editing = !editing }) { Text(if (editing) "Modo execução" else "Modo edição") }
            TextButton(onClick = { cancel(); view = view.copy(zoom = minOf(3.0, view.zoom * 1.25), revision = view.revision + 1) }) { Text("Zoom +") }
            TextButton(onClick = { cancel(); view = view.copy(zoom = maxOf(.25, view.zoom / 1.25), revision = view.revision + 1) }) { Text("Zoom −") }
            TextButton(onClick = { cancel(); view = CanvasViewport(revision = view.revision + 1) }) { Text("Centralizar") }
            TextButton(onClick = { cancel(); view = view.copy(pan = Point2(view.pan.x - 40, view.pan.y), revision = view.revision + 1) }) { Text("Esquerda") }
            TextButton(onClick = { cancel(); view = view.copy(pan = Point2(view.pan.x + 40, view.pan.y), revision = view.revision + 1) }) { Text("Direita") }
            TextButton(onClick = { cancel(); view = view.copy(pan = Point2(view.pan.x, view.pan.y - 40), revision = view.revision + 1) }) { Text("Acima") }
            TextButton(onClick = { cancel(); view = view.copy(pan = Point2(view.pan.x, view.pan.y + 40), revision = view.revision + 1) }) { Text("Abaixo") }
        }
        if (editing && showControls) Text("Arraste nós visuais. Escape cancela. Row/Column sem wrap; Box insere no fim.")
        if (diagnostic.isNotEmpty()) Text(diagnostic)
        Box(Modifier.weight(1f).fillMaxWidth().clipToBounds().onGloballyPositioned(geometry::placeViewport)) {
            CompositionLocalProvider(LocalNodeBoundsObserver provides geometry, LocalLayoutDirection provides LayoutDirection.Ltr) {
                Box(Modifier.graphicsLayer {
                    scaleX = viewport.zoom.toFloat(); scaleY = viewport.zoom.toFloat()
                    translationX = viewport.pan.x.toFloat(); translationY = viewport.pan.y.toFloat()
                    transformOrigin = TransformOrigin(0f, 0f)
                }) { content() }
            }
            if (editing) Canvas(Modifier.fillMaxSize().testTag("studio-edit-surface")
                .focusRequester(focus).onFocusChanged { if (!it.isFocused) cancel() }.focusable()
                .onPreviewKeyEvent { if (it.key == Key.Escape) { cancel(); true } else false }
                .pointerInput(document, editing, geometry) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        down.consume()
                        focus.requestFocus()
                        val start = down.position
                        val initial = geometry.snapshot(currentViewport)
                        val source = DropGeometry.hit(document, initial, currentViewport, Point2(start.x.toDouble(), start.y.toDouble()))
                        source?.let { onSelect(it) }
                        if (source != null) gesture.begin(down.id.value, source, document)
                        var moved = false
                        try {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val pointer = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (event.changes.count { it.pressed } > 1) { cancel(); break }
                                pointer.consume()
                                if (!pointer.pressed) {
                                    if (moved) gesture.finish(down.id.value, workspace.session, geometry.revision,
                                        currentViewport.revision, Uuid.random().toString())?.let { onCommit(it) }
                                    break
                                }
                                if ((pointer.position - start).getDistance() > viewConfiguration.touchSlop) moved = true
                                if (moved && gesture.isActive) {
                                    val result = gesture.move(down.id.value, document, geometry.snapshot(currentViewport), currentViewport,
                                        Point2(pointer.position.x.toDouble(), pointer.position.y.toDouble()), workspace.catalog)
                                    guide = result.value
                                    diagnostic = result.diagnostics.joinToString { "${it.code}: ${it.message}" }
                                }
                            }
                        } finally { cancel() }
                    }
                }) {
                val snapshot = geometry.snapshot(viewport)
                snapshot.nodes[selectedId]?.let { node ->
                    val visible = node.clips.firstOrNull()?.rect ?: node.rect
                    val a = viewport.toScreen(Point2(visible.x, visible.y))
                    val b = viewport.toScreen(Point2(visible.right, visible.bottom))
                    drawRect(Color(0xFF6750A4), Offset(a.x.toFloat(), a.y.toFloat()),
                        androidx.compose.ui.geometry.Size((b.x - a.x).toFloat(), (b.y - a.y).toFloat()), style = Stroke(2f))
                }
                guide?.takeIf { it.layoutRevision == snapshot.revision && it.viewportRevision == viewport.revision }?.let {
                    drawLine(Color(0xFFB00070), Offset(it.guideStart.x.toFloat(), it.guideStart.y.toFloat()),
                        Offset(it.guideEnd.x.toFloat(), it.guideEnd.y.toFloat()), strokeWidth = 3f)
                }
            }
        }
    }
}

internal const val DROP_EXAMPLE = """{"component":"layout:column","id":"drop-root","properties":{"width":"320","height":"300","paddingAll":"8"},"children":[{"component":"layout:text","id":"drop-title","properties":{"text":"Arraste A ou B entre os containers"}},{"component":"layout:row","id":"drop-row","children":[{"component":"layout:column","id":"drop-left","properties":{"width":"140","height":"200","paddingAll":"8","backgroundColor":"E7E0EC"},"children":[{"component":"layout:text","id":"drop-a","properties":{"text":"A"}},{"component":"layout:text","id":"drop-b","properties":{"text":"B"}}]},{"component":"layout:column","id":"drop-right","properties":{"width":"140","height":"200","paddingAll":"8","backgroundColor":"D0E8DF"},"children":[]}]}]}"""
