package me.developes.humming.sdui.layout.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import me.developes.humming.sdui.common.ServerDrivenNode

enum class NodeBoundary { OUTER, CONTENT }

/** Optional host observation. No layout wrapper, authoring dependency or persisted coordinates. */
interface NodeBoundsObserver {
    fun placed(node: ServerDrivenNode, boundary: NodeBoundary, coordinates: LayoutCoordinates)
    fun removed(nodeId: String)
}

val LocalNodeBoundsObserver = staticCompositionLocalOf<NodeBoundsObserver?> { null }

@Composable
internal fun Modifier.observeNodeBounds(node: ServerDrivenNode): Modifier {
    val observer = LocalNodeBoundsObserver.current ?: return this
    DisposableEffect(node.id, observer) { onDispose { observer.removed(node.id) } }
    return Modifier.onGloballyPositioned { observer.placed(node, NodeBoundary.OUTER, it) }
        .then(this).onGloballyPositioned { observer.placed(node, NodeBoundary.CONTENT, it) }
}
