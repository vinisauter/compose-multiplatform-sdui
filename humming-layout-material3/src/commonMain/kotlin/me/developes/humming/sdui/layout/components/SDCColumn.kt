package me.developes.humming.sdui.layout.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import me.developes.humming.sdui.common.rememberActionInvoker
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.layout.LayoutDefinitions
import me.developes.humming.sdui.layout.Layout

class SDCColumn(val node: ServerDrivenNode, val viewModel: SDUIFormViewModel) : Layout {
    private val properties = LayoutDefinitions.column.parse(node).requireValid()

    private val modifier = Modifier.fromNode(node)
    private val actions = node.propertyNodes("onClick")

    private val verticalArrangement: Arrangement.Vertical =
        when (properties.property("verticalArrangement")) {
            null -> Arrangement.Top
            "Top" -> Arrangement.Top
            "Center" -> Arrangement.Center
            "Bottom" -> Arrangement.Bottom
            "SpaceAround" -> Arrangement.SpaceAround
            "SpaceBetween" -> Arrangement.SpaceBetween
            "SpaceEvenly" -> Arrangement.SpaceEvenly
            else -> error("Unknown value for verticalArrangement ${properties.property("verticalArrangement")}")
        }
    private val horizontalAlignment: Alignment.Horizontal =
        when (properties.property("horizontalAlignment")) {
            null -> Alignment.Start
            "Start" -> Alignment.Start
            "Center" -> Alignment.CenterHorizontally
            "End" -> Alignment.End
            else -> error("Unknown value for horizontalAlignment ${properties.property("horizontalAlignment")}")
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
        val invoker = rememberActionInvoker(viewModel)
        val observedModifier = modifier.observeNodeBounds(node)
        Column(
            modifier = if (actions.isNotEmpty()) observedModifier.clickable {
                invoker.invoke(actions)
            } else observedModifier,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
        ) {
            loadChildren.invoke()
        }
    }
}
