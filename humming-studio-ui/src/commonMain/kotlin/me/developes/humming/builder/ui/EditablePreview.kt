package me.developes.humming.builder.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
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
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.geometry.Rect
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
                    controls: PreviewControls, showControls: Boolean = true, palette: PaletteDragState? = null,
                    preferredSlots: Map<String, String> = emptyMap(), selectionColor: Color = Color(0xFF6750A4),
                    selectionActions: (@Composable (AuthorNode)->Unit)? = null,
                    content: @Composable () -> Unit) {
    val geometry = remember(document, preferredSlots) { PreviewGeometry(document, sourceIds, workspace.catalog, preferredSlots) }
    val gesture = remember(document) { DropGesture() }
    val focus = remember { FocusRequester() }
    var editing by controls.editing
    var view by controls.viewport
    val density = LocalDensity.current.density.toDouble()
    val viewport = view.copy(density = density)
    val currentViewport by rememberUpdatedState(viewport)
    var surface by remember { mutableStateOf(Rect.Zero) }
    DisposableEffect(palette, document, geometry) {
        val resolver: (Offset) -> DropPlan? = { root ->
            if (!editing || !surface.contains(root)) null else {
                val point = root - surface.topLeft
                DropGeometry.insertion(document, geometry.snapshot(currentViewport), currentViewport,
                    Point2(point.x.toDouble(), point.y.toDouble()), workspace.catalog).value
            }
        }
        palette?.resolve = resolver
        onDispose { if (palette?.resolve === resolver) palette.resolve = null }
    }
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
        if (editing && showControls) Text("Arraste nós visuais. Escape cancela. Containers com fluxo ordenam; os demais recebem no fim.")
        if (diagnostic.isNotEmpty()) Text(diagnostic)
        Box(Modifier.weight(1f).fillMaxWidth().clipToBounds().onGloballyPositioned {
            geometry.placeViewport(it); surface = Rect(it.positionInRoot(), androidx.compose.ui.geometry.Size(it.size.width.toFloat(), it.size.height.toFloat()))
        }) {
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
                    drawRect(selectionColor, Offset(a.x.toFloat(), a.y.toFloat()),
                        androidx.compose.ui.geometry.Size((b.x - a.x).toFloat(), (b.y - a.y).toFloat()), style = Stroke(2f))
                }
                if(selectionActions!=null) snapshot.nodes[selectedId]?.let { measured ->
                    val rect=measured.clips.firstOrNull()?.rect ?: measured.rect
                    val a=viewport.toScreen(Point2(rect.x,rect.y));val b=viewport.toScreen(Point2(rect.right,rect.bottom))
                    for(point in listOf(a,Point2(b.x,a.y),b,Point2(a.x,b.y))) {
                        val origin=Offset(point.x.toFloat()-3f,point.y.toFloat()-3f)
                        drawRect(Color.White,origin,androidx.compose.ui.geometry.Size(6f,6f))
                        drawRect(selectionColor,origin,androidx.compose.ui.geometry.Size(6f,6f),style=Stroke(1f))
                    }
                }
                (palette?.plan ?: guide)?.takeIf { it.layoutRevision == snapshot.revision && it.viewportRevision == viewport.revision }?.let {
                    val start = Offset(it.guideStart.x.toFloat(), it.guideStart.y.toFloat())
                    val end = Offset(it.guideEnd.x.toFloat(), it.guideEnd.y.toFloat())
                    if (it.guideKind == DropGuideKind.CONTAINER) drawRect(Color(0xFFB00070), start,
                        androidx.compose.ui.geometry.Size(end.x - start.x, end.y - start.y), style = Stroke(3f))
                    else drawLine(Color(0xFFB00070), start, end, strokeWidth = 3f)
                }
            }
            if(editing && selectionActions!=null) geometry.snapshot(viewport).nodes[selectedId]?.let { measured ->
                val rect=measured.clips.firstOrNull()?.rect ?: measured.rect
                val point=viewport.toScreen(Point2(rect.x,rect.y))
                val left=(point.x/density).coerceIn(0.0,(surface.width/density-245).coerceAtLeast(0.0))
                val top=(point.y/density-38).coerceAtLeast(40.0)
                document.nodes[selectedId]?.let { selected ->
                    Row(Modifier.offset(left.dp,top.dp).background(Color.White,RoundedCornerShape(20.dp))
                        .border(1.dp,Color(0xFFE5E7EB),RoundedCornerShape(20.dp)).padding(5.dp),horizontalArrangement=Arrangement.spacedBy(4.dp)) { selectionActions(selected) }
                }
            }

        }
    }
}

internal const val DROP_EXAMPLE = """{"component":"layout:column","id":"drop-root","properties":{"width":"320","height":"300","paddingAll":"8"},"children":[{"component":"layout:text","id":"drop-title","properties":{"text":"Arraste A ou B entre os containers"}},{"component":"layout:row","id":"drop-row","children":[{"component":"layout:column","id":"drop-left","properties":{"width":"140","height":"200","paddingAll":"8","backgroundColor":"E7E0EC"},"children":[{"component":"layout:text","id":"drop-a","properties":{"text":"A"}},{"component":"layout:text","id":"drop-b","properties":{"text":"B"}}]},{"component":"layout:column","id":"drop-right","properties":{"width":"140","height":"200","paddingAll":"8","backgroundColor":"D0E8DF"},"children":[]}]}]}"""
