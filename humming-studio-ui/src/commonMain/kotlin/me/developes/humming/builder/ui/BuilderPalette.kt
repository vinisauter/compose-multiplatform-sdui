package me.developes.humming.builder.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.developes.humming.sdui.studio.*

/** Only a projection during pointer capture; the workspace changes on a valid release. */
internal class PaletteDragState {
    var name by mutableStateOf<String?>(null)
    var plan by mutableStateOf<DropPlan?>(null)
    var resolve: ((Offset) -> DropPlan?)? = null
    var cancellation = 0
        private set
    fun move(component: String, root: Offset) { name = component; plan = resolve?.invoke(root) }
    fun cancel() { name = null; plan = null; cancellation++ }
}

@Composable
internal fun BuilderPaletteCard(name: String, drag: PaletteDragState, click: () -> Unit,
                                drop: (String, DropPlan) -> Unit, compact: Boolean = false, active: Boolean = false) {
    val source = remember(name) { CatalogFixture(name, stateOutput = false).source() }
    var origin by remember { mutableStateOf(Offset.Zero) }
    val latestClick by rememberUpdatedState(click)
    val latestDrop by rememberUpdatedState(drop)
    val focus = remember { FocusRequester() }
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp).height(if(compact) 72.dp else 78.dp)
        .background(if(compact) (if(active) Color(0xFFEFF6FF) else MaterialTheme.colorScheme.surface) else Color(0xFF2A3245), RoundedCornerShape(7.dp))
        .border(1.dp, if(compact) (if(active) StitchBlue.copy(alpha=.4f) else StitchBorder) else Color(0xFF3B465D), RoundedCornerShape(10.dp))
        .testTag("builder-add-$name").onGloballyPositioned { origin = it.positionInRoot() }
        .focusRequester(focus).focusable()
        .onPreviewKeyEvent { if (it.key == Key.Escape) { drag.cancel(); true } else false }
        .semantics { onClick("Adicionar $name") { latestClick(); true } }
        .pointerInput(name, drag) {
            awaitEachGesture {
                val down = awaitFirstDown(false, PointerEventPass.Initial)
                down.consume(); focus.requestFocus()
                var moved = false
                var cancelled = false
                val epoch = drag.cancellation
                try {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        if (drag.cancellation != epoch) cancelled = true
                        if (event.changes.count { it.pressed } > 1) { cancelled = true; drag.cancel() }
                        val pointer = event.changes.firstOrNull { it.id == down.id } ?: break
                        pointer.consume()
                        if ((pointer.position - down.position).getDistance() > viewConfiguration.touchSlop) moved = true
                        if (!pointer.pressed) {
                            if (!cancelled) {
                                if (!moved) latestClick()
                                else if (drag.name != null) drag.plan?.let { latestDrop(name, it) }
                            }
                            break
                        }
                        if (moved && !cancelled) drag.move(name, origin + pointer.position)
                    }
                } finally { drag.cancel() }
            }
        }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        if(compact) Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
            StitchPrimitiveGlyph(name,if(name=="outlinedTextField") StitchBlue else Color(0xFFCBD5E1))
            Text(when(name) { "column" -> "V-Stack"; "row" -> "H-Row"; "box" -> "Box Container"; "text" -> "Typography"; "outlinedTextField" -> "Text Input"; "image" -> "Remote Img"; else -> name.replaceFirstChar { it.uppercase() } },style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurface)
        } else {
        Box(Modifier.size(74.dp, 56.dp).clipToBounds().background(Color.White, RoundedCornerShape(6.dp)).clearAndSetSemantics {}.testTag("builder-thumbnail-$name")) {
            Box(Modifier.graphicsLayer { scaleX = .30f; scaleY = .30f; transformOrigin = TransformOrigin(0f, 0f) }
                .wrapContentSize(Alignment.TopStart, unbounded = true).requiredSize(246.dp, 186.dp)) { CompiledPreview(source) }
        }
        Column(Modifier.weight(1f).padding(start = 8.dp)) {
            Text(name, color = Color(0xFFF1F5FF), style = MaterialTheme.typography.labelMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("Arraste ou clique", color = Color(0xFFADBBD5), style = MaterialTheme.typography.labelSmall)
        }
        }
    }
}
