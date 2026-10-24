package sample.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import me.developes.humming.sdui.common.parseNode
import me.developes.humming.sdui.layout.components.LocalNodeBoundsObserver
import me.developes.humming.sdui.studio.*
import kotlin.math.roundToInt

internal data class DrawnConnection(val link: NavigationLink, val curve: CubicConnector,
                                     val flattened: FlattenedConnector, val arrow: ConnectorArrow?)

/** Renderer measurements and drawing are derived; only canvasPosition belongs to the document. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun InteractionCanvas(document: AuthoringDocument, compiled: CompiledContracts,
                               selectedEdge: String?, select: (String, String?) -> Unit,
                               observe: (List<DrawnConnection>) -> Unit = {},
                               viewportState: MutableState<CanvasViewport>? = null) {
    val localView = remember { mutableStateOf(CanvasViewport(zoom = .5)) }
    var view by (viewportState ?: localView)
    val viewport = view.copy(density = LocalDensity.current.density.toDouble())
    val geometry = remember(document, compiled) { PreviewGeometry(document, compiled.sourceNodeIds) }
    val frames = remember(document) { mutableStateMapOf<String, Rect>() }
    var origin by remember { mutableStateOf(Offset.Zero) }
    var focused by remember { mutableStateOf(false) }
    var pressed by remember { mutableStateOf(false) }
    val windowFocused = LocalWindowInfo.current.isWindowFocused
    val focus = remember { FocusRequester() }
    val projection = remember(document) { InteractionProjection.navigationLinks(document) }
    val screenSources = remember(compiled) { compiled.artifacts.values.associateBy { parseNode(it).id } }
    val snapshot = geometry.snapshot(viewport)
    fun world(rect: Rect): Rect2 {
        val a = viewport.toWorld(Point2((rect.left-origin.x).toDouble(), (rect.top-origin.y).toDouble()))
        val b = viewport.toWorld(Point2((rect.right-origin.x).toDouble(), (rect.bottom-origin.y).toDouble()))
        return Rect2(a.x,a.y,maxOf(0.0,b.x-a.x),maxOf(0.0,b.y-a.y))
    }
    val connections = projection.value.orEmpty().mapNotNull { link ->
        val source = snapshot.nodes[link.sourceNodeId]?.rect ?: return@mapNotNull null
        val target = frames[link.targetScreenId]?.let(::world) ?: return@mapNotNull null
        val curve = ConnectorGeometry.between(link.sourceNodeId,source,link.targetScreenId,target) ?: return@mapNotNull null
        DrawnConnection(link,curve,ConnectorGeometry.flatten(curve,viewport),ConnectorGeometry.arrow(curve,viewport))
    }
    SideEffect { observe(connections) }
    val currentConnections by rememberUpdatedState(connections)
    val currentSnapshot by rememberUpdatedState(snapshot)
    val currentViewport by rememberUpdatedState(viewport)
    val currentSelect by rememberUpdatedState(select)
    LaunchedEffect(windowFocused) { if (!windowFocused) pressed = false }
    Column(Modifier.fillMaxSize()) {
        FlowRow {
            TextButton(onClick = { pressed = false; view = view.copy(zoom=(view.zoom*1.25).coerceAtMost(3.0)) }) { Text("Canvas +") }
            TextButton(onClick = { pressed = false; view = view.copy(zoom=(view.zoom/1.25).coerceAtLeast(.15)) }) { Text("Canvas −") }
            TextButton(onClick = { pressed = false; view = CanvasViewport(zoom=.5) }) { Text("Enquadrar") }
            TextButton(onClick = { pressed = false; view = view.copy(pan=Point2(view.pan.x-80,view.pan.y)) }) { Text("Canvas esquerda") }
            TextButton(onClick = { pressed = false; view = view.copy(pan=Point2(view.pan.x+80,view.pan.y)) }) { Text("Canvas direita") }
            TextButton(onClick = { pressed = false; view = view.copy(pan=Point2(view.pan.x,view.pan.y-80)) }) { Text("Canvas acima") }
            TextButton(onClick = { pressed = false; view = view.copy(pan=Point2(view.pan.x,view.pan.y+80)) }) { Text("Canvas abaixo") }
        }
        Text("${connections.size} conexões possíveis • selecione curva ou componente; Escape cancela")
        projection.diagnostics.forEach { Text("${it.code}: ${it.message}") }
        if (projection.value.orEmpty().any { it.sourceNodeId == it.targetScreenId })
            Text("SELF_CONNECTION_UNSUPPORTED: autoconexão importada exige roteamento de loop")
        if (connections.any { !it.flattened.toleranceMet }) Text("CONNECTOR_TOLERANCE: limite de aproximação; seleção imprecisa desativada")
        Box(Modifier.weight(1f).fillMaxWidth().clipToBounds().background(Color(0xFFF1F0F5))
            .onGloballyPositioned { origin = it.positionInRoot(); geometry.placeViewport(it) }) {
            CompositionLocalProvider(LocalNodeBoundsObserver provides geometry) {
                Box(Modifier.fillMaxSize().graphicsLayer {
                    scaleX=viewport.zoom.toFloat(); scaleY=viewport.zoom.toFloat()
                    translationX=viewport.pan.x.toFloat(); translationY=viewport.pan.y.toFloat()
                    transformOrigin=TransformOrigin(0f,0f)
                }) {
                    document.nodes.values.filter { it.kind == AuthorNodeKind.SCREEN }
                        .sortedWith(compareBy({ it.placement.orderKey }, { it.id })).forEachIndexed { index, screen ->
                        val position = screen.canvasPosition ?: CanvasPoint(index*400.0,0.0)
                        val source = screenSources[screen.id]
                        if (source != null) key(screen.id) {
                            Column(Modifier.offset { IntOffset((position.x*viewport.density).roundToInt(),(position.y*viewport.density).roundToInt()) }
                                .size(280.dp,240.dp).background(Color.White).border(1.dp,Color(0xFF65558F))
                                .onGloballyPositioned { coordinates ->
                                    val a = coordinates.localToRoot(Offset.Zero)
                                    val b = coordinates.localToRoot(Offset(coordinates.size.width.toFloat(),coordinates.size.height.toFloat()))
                                    val rect = Rect(a,b)
                                    if (frames[screen.id] != rect) frames[screen.id] = rect
                                }) {
                                Text("Tela ${screen.id.take(8)}", Modifier.padding(6.dp))
                                Box(Modifier.fillMaxSize().clipToBounds()) { CompiledPreview(source) }
                            }
                        }
                    }
                }
            }
            Canvas(Modifier.fillMaxSize().testTag("studio-interaction-canvas").focusRequester(focus)
                .onFocusChanged { focused = it.isFocused; if (!it.isFocused) pressed = false }.focusable()
                .onPreviewKeyEvent { if (it.key == Key.Escape) { pressed=false; true } else false }
                .pointerInput(document,geometry) {
                    awaitEachGesture {
                        val down = awaitFirstDown(false,PointerEventPass.Initial)
                        down.consume(); focus.requestFocus(); pressed=true
                        val start = down.position
                        var moved = false
                        try {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                if (event.changes.count { it.pressed } > 1) pressed=false
                                val pointer = event.changes.firstOrNull { it.id == down.id } ?: break
                                pointer.consume()
                                if ((pointer.position-start).getDistance() > viewConfiguration.touchSlop) moved=true
                                if (!pointer.pressed) {
                                    if (pressed && focused && !moved) {
                                        val point = Point2(pointer.position.x.toDouble(),pointer.position.y.toDouble())
                                        val connection = currentConnections.asReversed().firstOrNull { it.flattened.toleranceMet && it.flattened.hit(point) }
                                        if (connection != null) {
                                            val edge = document.edges.getValue(connection.link.navigationEdgeId)
                                            currentSelect(edge.sourceNodeId,edge.id)
                                        } else DropGeometry.hit(document,currentSnapshot,currentViewport,point)?.let { currentSelect(it,null) }
                                    }
                                    break
                                }
                            }
                        } finally { pressed=false }
                    }
                }) {
                connections.forEach { connection ->
                    val color = if (connection.link.navigationEdgeId == selectedEdge) Color(0xFFE91E63) else Color(0xFF65558F)
                    fun Point2.offset() = Offset(x.toFloat(),y.toFloat())
                    val c = connection.curve
                    val path = Path().apply {
                        val a=viewport.toScreen(c.p0); val b=viewport.toScreen(c.p1)
                        val d=viewport.toScreen(c.p2); val e=viewport.toScreen(c.p3)
                        moveTo(a.x.toFloat(),a.y.toFloat())
                        cubicTo(b.x.toFloat(),b.y.toFloat(),d.x.toFloat(),d.y.toFloat(),e.x.toFloat(),e.y.toFloat())
                    }
                    drawPath(path,color,style=Stroke(2.5f))
                    drawCircle(color,4f,viewport.toScreen(c.p0).offset())
                    connection.arrow?.let { arrow -> drawPath(Path().apply {
                        moveTo(arrow.tip.x.toFloat(),arrow.tip.y.toFloat()); lineTo(arrow.left.x.toFloat(),arrow.left.y.toFloat())
                        lineTo(arrow.right.x.toFloat(),arrow.right.y.toFloat()); close()
                    },color) }
                }
            }
        }
    }
}
