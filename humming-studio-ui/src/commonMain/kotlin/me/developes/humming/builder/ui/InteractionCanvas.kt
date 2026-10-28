package me.developes.humming.builder.ui

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
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import me.developes.humming.sdui.common.parseNode
import me.developes.humming.sdui.common.PropertyKind
import me.developes.humming.sdui.common.NodeRole
import me.developes.humming.sdui.common.initialNodeCatalog
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
                               viewportState: MutableState<CanvasViewport>? = null,
                               connect: ((String, String, String) -> Unit)? = null,
                               screenNames: Map<String, String> = emptyMap(),
                               frameViewport: ProjectViewport? = null, compactControls: Boolean = false, onNewScreen: (()->Unit)? = null, onPlay: (()->Unit)? = null,
                               observePorts: (List<Triple<String, String, Point2>>, Map<String, Rect2>) -> Unit = { _, _ -> }) {
    val frameWidth = frameViewport?.width?.toDouble() ?: 280.0
    val frameHeight = frameViewport?.height?.toDouble() ?: 240.0
    fun position(index: Int, screen: AuthorNode): CanvasPoint {
        val stored = screen.canvasPosition ?: CanvasPoint(index * 400.0, 0.0)
        return if (frameViewport == null) stored else CanvasPoint(stored.x / 400 * (frameWidth + 120), stored.y / 300 * (frameHeight + 80))
    }
    val localView = remember { mutableStateOf(CanvasViewport(zoom = .5)) }
    var view by (viewportState ?: localView)
    val viewport = view.copy(density = LocalDensity.current.density.toDouble())
    val geometry = remember(document, compiled) { PreviewGeometry(document, compiled.sourceNodeIds) }
    val frames = remember(document) { mutableStateMapOf<String, Rect>() }
    var origin by remember { mutableStateOf(Offset.Zero) }
    var area by remember { mutableStateOf(IntSize.Zero) }
    var focused by remember { mutableStateOf(false) }
    var pressed by remember { mutableStateOf(false) }
    var noodleStart by remember { mutableStateOf<Offset?>(null) }
    var noodleEnd by remember { mutableStateOf<Offset?>(null) }
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
    val currentConnect by rememberUpdatedState(connect)
    val currentFrames by rememberUpdatedState(frames.toMap())
    val currentOrigin by rememberUpdatedState(origin)
    val catalog = remember { initialNodeCatalog() }
    val handles = if (connect == null) emptyList() else snapshot.nodes.values.mapNotNull { measured ->
        val node = document.nodes[measured.id] ?: return@mapNotNull null
        val ports = catalog[node.definition]?.properties.orEmpty().filter { it.kind == PropertyKind.NODES && it.nodeRole == NodeRole.ACTION }
        val port = ports.firstOrNull { it.name == "onClick" } ?: ports.firstOrNull() ?: return@mapNotNull null
        if (!measured.visible) return@mapNotNull null
        Triple(node.id, port.name, viewport.toScreen(Point2(measured.rect.right, measured.rect.y + measured.rect.height / 2)))
    }
    val currentHandles by rememberUpdatedState(handles)
    SideEffect { observePorts(handles, frames.mapValues { (_, rect) -> Rect2((rect.left-origin.x).toDouble(), (rect.top-origin.y).toDouble(), rect.width.toDouble(), rect.height.toDouble()) }) }
    val screens = document.nodes.values.filter { it.kind == AuthorNodeKind.SCREEN }.sortedWith(compareBy({ it.placement.orderKey }, { it.id }))
    fun fitScreens() {
        if (area.width == 0 || area.height == 0) return
        val positions = screens.mapIndexed { index, node -> position(index, node) }
        val left = positions.minOfOrNull { it.x } ?: 0.0
        val top = positions.minOfOrNull { it.y } ?: 0.0
        val width = (positions.maxOfOrNull { it.x + frameWidth } ?: frameWidth) - left
        val height = (positions.maxOfOrNull { it.y + frameHeight } ?: frameHeight) - top
        val zoom = minOf((area.width - 32) / (width * viewport.density), (area.height - 32) / (height * viewport.density), .85).coerceAtLeast(.05)
        view = CanvasViewport(zoom, Point2(16 - left * zoom * viewport.density, 16 - top * zoom * viewport.density), revision = view.revision + 1)
    }
    LaunchedEffect(screens.map { it.id }, area, frameViewport) { if (connect != null) fitScreens() }
    LaunchedEffect(windowFocused) { if (!windowFocused) { pressed = false; noodleStart = null; noodleEnd = null } }
    Column(Modifier.fillMaxSize()) {
        if (!compactControls) FlowRow {
            TextButton(onClick = { pressed = false; view = view.copy(zoom=(view.zoom*1.25).coerceAtMost(3.0)) }) { Text("Canvas +") }
            TextButton(onClick = { pressed = false; view = view.copy(zoom=(view.zoom/1.25).coerceAtLeast(.15)) }) { Text("Canvas −") }
            TextButton(onClick = { pressed = false; if (connect != null) fitScreens() else view = CanvasViewport(zoom=.5) }) { Text("Enquadrar") }
            TextButton(onClick = { pressed = false; view = view.copy(pan=Point2(view.pan.x-80,view.pan.y)) }) { Text("Canvas esquerda") }
            TextButton(onClick = { pressed = false; view = view.copy(pan=Point2(view.pan.x+80,view.pan.y)) }) { Text("Canvas direita") }
            TextButton(onClick = { pressed = false; view = view.copy(pan=Point2(view.pan.x,view.pan.y-80)) }) { Text("Canvas acima") }
            TextButton(onClick = { pressed = false; view = view.copy(pan=Point2(view.pan.x,view.pan.y+80)) }) { Text("Canvas abaixo") }
        }
        if (!compactControls) Text(if (connect == null) "${connections.size} conexões possíveis • selecione curva ou componente; Escape cancela"
            else "${connections.size} conexões • puxe uma bolinha azul até outra tela • Escape cancela")
        projection.diagnostics.forEach { Text("${it.code}: ${it.message}") }
        if (projection.value.orEmpty().any { it.sourceNodeId == it.targetScreenId })
            Text("SELF_CONNECTION_UNSUPPORTED: autoconexão importada exige roteamento de loop")
        if (connections.any { !it.flattened.toleranceMet }) Text("CONNECTOR_TOLERANCE: limite de aproximação; seleção imprecisa desativada")
        Box(Modifier.weight(1f).fillMaxWidth().clipToBounds().background(if(compactControls) Color(0xFFF8F9FA) else Color(0xFFF1F0F5))
            .onGloballyPositioned { origin = it.positionInRoot(); area = it.size; geometry.placeViewport(it) }) {
            if(compactControls) Canvas(Modifier.fillMaxSize()) { val spacing=16.dp.toPx();for(x in 0..(size.width/spacing).toInt()) for(y in 0..(size.height/spacing).toInt()) drawCircle(Color(0xFFD1D5DB),1.dp.toPx(),Offset(x*spacing,y*spacing)) }
            CompositionLocalProvider(LocalNodeBoundsObserver provides geometry) {
                Box(Modifier.fillMaxSize().graphicsLayer {
                    scaleX=viewport.zoom.toFloat(); scaleY=viewport.zoom.toFloat()
                    translationX=viewport.pan.x.toFloat(); translationY=viewport.pan.y.toFloat()
                    transformOrigin=TransformOrigin(0f,0f)
                }) {
                    document.nodes.values.filter { it.kind == AuthorNodeKind.SCREEN }
                        .sortedWith(compareBy({ it.placement.orderKey }, { it.id })).forEachIndexed { index, screen ->
                        val position = position(index, screen)
                        val source = screenSources[screen.id]
                        if (source != null) key(screen.id) {
                            Column(Modifier.offset { IntOffset((position.x*viewport.density).roundToInt(),(position.y*viewport.density).roundToInt()) }
                                .wrapContentSize(Alignment.TopStart, unbounded = true).requiredSize(frameWidth.dp,frameHeight.dp)
                                .background(Color.White).border(1.dp,if(compactControls) Color(0xFFE5E7EB) else Color(0xFF65558F))
                                .onGloballyPositioned { coordinates ->
                                    val a = coordinates.localToRoot(Offset.Zero)
                                    val b = coordinates.localToRoot(Offset(coordinates.size.width.toFloat(),coordinates.size.height.toFloat()))
                                    val rect = Rect(a,b)
                                    if (frames[screen.id] != rect) frames[screen.id] = rect
                                }) {
                                val name = screenNames[screen.id] ?: "Tela ${index + 1}"
                                Row(Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    if (screen.id == document.entryNodeId) Canvas(Modifier.size(26.dp, 18.dp)
                                        .testTag("studio-entry-marker-${screen.id}").semantics { contentDescription = "Tela inicial: $name" }) {
                                        val color = Color(0xFF15803D)
                                        val middle = size.height / 2
                                        drawLine(color, Offset(0f, middle), Offset(size.width - 5.dp.toPx(), middle), strokeWidth = 2.dp.toPx())
                                        drawPath(Path().apply {
                                            moveTo(size.width - 2.dp.toPx(), middle)
                                            lineTo(size.width - 10.dp.toPx(), middle - 5.dp.toPx())
                                            lineTo(size.width - 10.dp.toPx(), middle + 5.dp.toPx()); close()
                                        }, color)
                                    }
                                    Text(if (screen.id == document.entryNodeId) "$name · Inicial" else name)
                                }
                                Box(Modifier.fillMaxSize().clipToBounds()) { CompiledPreview(source) }
                            }
                        }
                    }
                }
            }
            Canvas(Modifier.fillMaxSize().testTag("studio-interaction-canvas").focusRequester(focus)
                .onFocusChanged { focused = it.isFocused; if (!it.isFocused) { pressed = false; noodleStart = null; noodleEnd = null } }.focusable()
                .onPreviewKeyEvent { if (it.key == Key.Escape) { pressed=false; noodleStart=null; noodleEnd=null; true } else false }
                .pointerInput(document,geometry) {
                    awaitEachGesture {
                        val down = awaitFirstDown(false,PointerEventPass.Initial)
                        down.consume(); focus.requestFocus(); pressed=true
                        val start = down.position
                        val handle = currentHandles.asReversed().firstOrNull { (_, _, point) ->
                            (Offset(point.x.toFloat(), point.y.toFloat()) - start).getDistance() <= 12 * currentViewport.density.toFloat()
                        }
                        if (handle != null) { noodleStart = Offset(handle.third.x.toFloat(), handle.third.y.toFloat()); noodleEnd = start }
                        var moved = false
                        try {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                if (event.changes.count { it.pressed } > 1) pressed=false
                                val pointer = event.changes.firstOrNull { it.id == down.id } ?: break
                                pointer.consume()
                                if (handle != null && pressed) noodleEnd = pointer.position
                                if ((pointer.position-start).getDistance() > viewConfiguration.touchSlop) moved=true
                                if (!pointer.pressed) {
                                    if (pressed && focused && moved && handle != null) {
                                        val target = currentFrames.entries.firstOrNull { (id, rect) ->
                                            id != handle.first && rect.contains(pointer.position + currentOrigin)
                                        }?.key
                                        if (target != null) currentConnect?.invoke(handle.first, handle.second, target)
                                    }
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
                        } finally { pressed=false; noodleStart=null; noodleEnd=null }
                    }
                }) {
                connections.forEach { connection ->
                    val color = if (connection.link.navigationEdgeId == selectedEdge) Color(0xFFE91E63) else if(compactControls) Color(0xFF2563EB) else Color(0xFF65558F)
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
                handles.forEach { (_, _, point) ->
                    val center = Offset(point.x.toFloat(), point.y.toFloat())
                    drawCircle(Color.White, 8f * viewport.density.toFloat(), center)
                    drawCircle(Color(0xFF4F46E5), 6f * viewport.density.toFloat(), center)
                }
                val from = noodleStart; val to = noodleEnd
                if (from != null && to != null) {
                    val bend = maxOf(40f, kotlin.math.abs(to.x - from.x) / 2)
                    drawPath(Path().apply { moveTo(from.x, from.y); cubicTo(from.x + bend, from.y, to.x - bend, to.y, to.x, to.y) },
                        Color(0xFFE91E63), style = Stroke(3f))
                    drawCircle(Color(0xFFE91E63), 5f, to)
                }
            }
            if(compactControls) Row(Modifier.align(Alignment.BottomCenter).padding(16.dp).background(Color.White,androidx.compose.foundation.shape.RoundedCornerShape(10.dp)).padding(8.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                StitchButton("Select",active=true,click={pressed=false})
                StitchButton("Fit canvas",click={fitScreens()})
                onNewScreen?.let { StitchButton("+ Screen",click=it) }
                onPlay?.let { StitchButton("Play Flow",primary=true,click=it) }
                StitchButton("−",click={view=view.copy(zoom=(view.zoom/1.2).coerceAtLeast(.1))})
                Text("${(view.zoom*100).toInt()}%",Modifier.align(Alignment.CenterVertically),color=Color(0xFF64748B))
                StitchButton("+",click={view=view.copy(zoom=(view.zoom*1.2).coerceAtMost(3.0))})
            }

        }
    }
}
