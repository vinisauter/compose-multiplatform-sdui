package me.developes.humming.sdui.layout.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.layout.Layout

class SDCColumn(val node: ServerDrivenNode, val viewModel: SDUIFormViewModel) : Layout {
    private val modifier = Modifier.fromNode(node)
    private val actions = node.propertyNodes("onClick")

    private val verticalArrangement: Arrangement.Vertical =
        when (node.property("verticalArrangement")) {
            null -> Arrangement.Top
            "Top" -> Arrangement.Top
            "Center" -> Arrangement.Center
            "Bottom" -> Arrangement.Bottom
            "SpaceAround" -> Arrangement.SpaceAround
            "SpaceBetween" -> Arrangement.SpaceBetween
            "SpaceEvenly" -> Arrangement.SpaceEvenly
            else -> error("Unknown value for verticalArrangement ${node.property("verticalArrangement")}")
        }
    private val horizontalAlignment: Alignment.Horizontal =
        when (node.property("horizontalAlignment")) {
            null -> Alignment.Start
            "Start" -> Alignment.Start
            "Center" -> Alignment.CenterHorizontally
            "End" -> Alignment.End
            else -> error("Unknown value for horizontalAlignment ${node.property("horizontalAlignment")}")
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
        Column(
            modifier = if (actions.isNotEmpty()) modifier.clickable {
                viewModel.invokeActions(actions)
            } else modifier,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
        ) {
            loadChildren.invoke()
        }
    }
}
