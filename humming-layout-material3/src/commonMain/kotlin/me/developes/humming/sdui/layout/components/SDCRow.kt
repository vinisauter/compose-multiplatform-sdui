package me.developes.humming.sdui.layout.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.layout.Layout

class SDCRow(node: ServerDrivenNode, viewModel: SDUIFormViewModel) : Layout {
    private val modifier = Modifier.fromNode(node)
    private val horizontalArrangement: Arrangement.Horizontal =
        when (node.property("horizontalArrangement")) {
            "Start" -> Arrangement.Start
            "Center" -> Arrangement.Center
            "End" -> Arrangement.End
            "SpaceAround" -> Arrangement.SpaceAround
            "SpaceBetween" -> Arrangement.SpaceBetween
            "SpaceEvenly" -> Arrangement.SpaceEvenly
            else -> Arrangement.Start
        }
    private val verticalAlignment: Alignment.Vertical =
        when (node.property("verticalAlignment")) {
            "Top" -> Alignment.Top
            "Center" -> Alignment.CenterVertically
            "Bottom" -> Alignment.Bottom
            else -> Alignment.Top
        }
    private val loadChildren: @Composable () -> Unit? = {
        node.children?.let {
            for (serverDrivenNode in it) {
                viewModel.loadComponent(node = serverDrivenNode)?.Content()
            }
        }
    }

    @Composable
    override fun Content() {
        Row(
            modifier = modifier,
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment
        ) {
            loadChildren.invoke()
        }
    }
}
