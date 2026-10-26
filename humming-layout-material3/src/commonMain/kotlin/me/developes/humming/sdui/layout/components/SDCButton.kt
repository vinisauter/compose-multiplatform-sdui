package me.developes.humming.sdui.layout.components

import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import me.developes.humming.sdui.common.collectAsState
import me.developes.humming.sdui.common.rememberActionInvoker
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.layout.LayoutDefinitions
import me.developes.humming.sdui.layout.Layout

class SDCButton(val node: ServerDrivenNode, val viewModel: SDUIFormViewModel) : Layout {
    private val properties = LayoutDefinitions.button.parse(node).requireValid()

    private var modifier = Modifier.fromNode(node)
    private val enabled = viewModel.binding(properties.property("enabled")) {
        it?.toBooleanStrictOrNull() ?: true
    }
    private val actions = node.propertyNodes("onClick")
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
        val isEnabled by enabled.collectAsState()

        Button(
            modifier = modifier.observeNodeBounds(node, NodeContentFlow.HORIZONTAL),
            enabled = isEnabled && !invoker.isRunning,
            onClick = {
                invoker.invoke(actions)
            }) {
            loadChildren.invoke()
        }
    }
}
