package sample.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import me.developes.humming.sdui.studio.*

private data class TreeRowBounds(val row: EditorTreeRow, val bounds: Rect)

@Composable
internal fun EditorTree(workspace: LocalWorkspace, document: AuthoringDocument, rows: List<EditorTreeRow>, selectedId: String,
                        select: (EditorTreeRow) -> Unit, changed: (EditOutcome) -> Unit, modifier: Modifier = Modifier,
                        dark: Boolean = false, tagPrefix: String = "studio-node-", treeTag: String = "studio-tree",
                        preferredSlots: Map<String, String> = emptyMap(), showDetails: Boolean = false) {
    val measured = remember { mutableMapOf<String, TreeRowBounds>() }
    val listState = rememberLazyListState()
    var area by remember { mutableStateOf(Rect.Zero) }
    var source by remember { mutableStateOf<String?>(null) }
    var point by remember { mutableStateOf(Offset.Zero) }
    var plan by remember { mutableStateOf<TreeDropPlan?>(null) }
    var hint by remember { mutableStateOf("") }
    var epoch by remember { mutableIntStateOf(0) }
    val focus = remember { FocusRequester() }
    val latestSelect by rememberUpdatedState(select)
    val latestChanged by rememberUpdatedState(changed)
    val slots by rememberUpdatedState(preferredSlots)
    fun cancel() { source = null; plan = null; hint = ""; epoch++ }
    fun resolve(at: Offset) {
        point = at
        val id = source ?: return
        val hit = if (area.contains(at)) measured.values.firstOrNull { it.bounds.contains(at) } else null
        if (hit == null) { plan = null; hint = ""; return }
        val ratio = (at.y - hit.bounds.top) / hit.bounds.height
        val canContain = workspace.catalog[hit.row.node.definition]?.let(DropGeometry::visualSlots).orEmpty().isNotEmpty()
        val position = when { ratio < .25 -> TreeDropPosition.BEFORE; ratio > .75 -> TreeDropPosition.AFTER
            canContain -> TreeDropPosition.INSIDE; ratio < .5 -> TreeDropPosition.BEFORE; else -> TreeDropPosition.AFTER }
        val result = TreeDrop.plan(document, id, hit.row.node.id, position, workspace.catalog, slots[hit.row.node.id])
        plan = result.value
        hint = plan?.let { when (it.position) { TreeDropPosition.BEFORE -> "Antes de"; TreeDropPosition.AFTER -> "Depois de"; TreeDropPosition.INSIDE -> "Dentro de" } + " ${hit.row.node.definition.substringAfter(':')} · ${it.operation.destination.let { d -> (d as NodeContainer.Slot).slot }}" }
            ?: result.diagnostics.firstOrNull()?.message.orEmpty()
    }
    val density = LocalDensity.current.density
    val windowFocused = LocalWindowInfo.current.isWindowFocused
    LaunchedEffect(windowFocused) { if (!windowFocused) cancel() }
    val scrollDirection = if (source == null || !area.contains(point)) 0 else when {
        point.y < area.top + 28 * density -> -1; point.y > area.bottom - 28 * density -> 1; else -> 0
    }
    LaunchedEffect(source, scrollDirection) {
        while (source != null && scrollDirection != 0 &&
            (if (scrollDirection < 0) listState.canScrollBackward else listState.canScrollForward)) {
            listState.scrollBy(scrollDirection * 10 * density)
            withFrameNanos { }
            resolve(point)
        }
    }
    Column(modifier) {
        Text(hint.ifEmpty { "Arraste: bordas ordenam; centro insere no container" },
            color = if (dark) Color(0xFFE9D5FF) else Color(0xFF6750A4),
            modifier = Modifier.fillMaxWidth().height(40.dp).testTag("$treeTag-drop-hint"))
        LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth().testTag(treeTag)
            .onGloballyPositioned { area = it.boundsInRoot() }.focusRequester(focus).focusable()
            .onFocusChanged { if (!it.isFocused) cancel() }
            .onPreviewKeyEvent { if (it.key == Key.Escape) { cancel(); true } else false }
            .pointerInput(document.documentId, document.revision) {
                awaitEachGesture {
                    val down = awaitFirstDown(false, PointerEventPass.Initial)
                    val hit = measured.values.firstOrNull { it.bounds.contains(area.topLeft + down.position) } ?: return@awaitEachGesture
                    down.consume(); focus.requestFocus(); latestSelect(hit.row)
                    val capturedEpoch = epoch
                    val id = hit.row.node.id
                    var moved = false
                    val movable = hit.row.node.kind == AuthorNodeKind.VISUAL && hit.row.node.placement is Placement.Child
                    try {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val pointer = event.changes.firstOrNull { it.id == down.id } ?: break
                            pointer.consume()
                            if (event.changes.count { it.pressed } > 1 || epoch != capturedEpoch) break
                            if ((pointer.position - down.position).getDistance() > viewConfiguration.touchSlop) moved = true
                            if (movable && moved) { source = id; resolve(area.topLeft + pointer.position) }
                            if (!pointer.pressed) {
                                if (movable && moved && epoch == capturedEpoch) plan?.let { latestChanged(TreeDrop.commit(workspace, it)) }
                                break
                            }
                        }
                    } finally { cancel() }
                }
            }) {
            items(rows, key = { "${it.node.id}-${it.edgeId}" }) { row ->
                val key = "${row.node.id}-${row.edgeId}"
                DisposableEffect(key) { onDispose { measured.remove(key) } }
                val target = plan?.takeIf { it.targetId == row.node.id }
                Column(Modifier.fillMaxWidth().onGloballyPositioned { measured[key] = TreeRowBounds(row, it.boundsInRoot()) }.drawWithContent {
                    drawContent()
                    if (target?.position == TreeDropPosition.BEFORE || target?.position == TreeDropPosition.AFTER) {
                        val y = if (target.position == TreeDropPosition.BEFORE) 1.5.dp.toPx() else size.height - 1.5.dp.toPx()
                        drawLine(Color(0xFFDB2777), Offset(0f, y), Offset(size.width, y), 3.dp.toPx())
                    }
                }) {
                    Text(if (showDetails) "${row.relation} · ${row.node.definition.substringAfter(':')} · ${row.node.id.take(8)}"
                        else row.node.definition.substringAfter(':') + if (row.relation == "raiz" || row.relation == "children") "" else " · ${row.relation}",
                        color = if (dark) Color(0xFFE2E8F0) else Color.Unspecified,
                        modifier = Modifier.fillMaxWidth().background(when {
                            target?.position == TreeDropPosition.INSIDE -> if (dark) Color(0xFF703A77) else Color(0xFFF3D8F0)
                            selectedId == row.node.id -> if (dark) Color(0xFF454371) else Color(0xFFE8DEF8)
                            else -> Color.Transparent }, RoundedCornerShape(6.dp))
                            .clickable { latestSelect(row) }.padding(start = (8 + row.depth.coerceAtMost(8) * 10).dp, top = 9.dp, bottom = 9.dp)
                            .testTag("$tagPrefix${row.node.id}"))
                }
            }
        }
    }
}
