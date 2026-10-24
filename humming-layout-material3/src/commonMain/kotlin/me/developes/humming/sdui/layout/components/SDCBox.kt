package me.developes.humming.sdui.layout.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.layout.LayoutDefinitions
import me.developes.humming.sdui.layout.Layout

class SDCBox(val node: ServerDrivenNode, private val viewModel: SDUIFormViewModel) : Layout {
    private val properties = LayoutDefinitions.box.parse(node).requireValid()

    private var modifier = Modifier.fromNode(node)

    private val contentAlignment: Alignment =
        when (properties.property("contentAlignment")) {
            null -> Alignment.TopStart
            "TopStart" -> Alignment.TopStart
            "TopCenter" -> Alignment.TopCenter
            "TopEnd" -> Alignment.TopEnd
            "CenterStart" -> Alignment.CenterStart
            "Center" -> Alignment.Center
            "CenterEnd" -> Alignment.CenterEnd
            "BottomStart" -> Alignment.BottomStart
            "BottomCenter" -> Alignment.BottomCenter
            "BottomEnd" -> Alignment.BottomEnd
            else -> error("Unknown value for horizontalAlignment ${properties.property("horizontalAlignment")}")
        }

    private val propagateMinConstraints: Boolean? =
        properties.property("propagateMinConstraints")?.toBoolean()

    private val loadChildren: @Composable () -> Unit? = {
        node.children?.let {
            for (serverDrivenNode in it) {
                viewModel.loadComponent(node = serverDrivenNode)?.Content()
            }
        }
    }

    @Composable
    override fun Content() {
        Box(
            modifier = modifier,
            contentAlignment = contentAlignment,
            propagateMinConstraints = propagateMinConstraints ?: false,
        ) {
            loadChildren.invoke()
        }
    }
}
