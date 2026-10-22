package me.developes.humming.sdui.layout.components

import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.layout.Layout

class SDCButton(val node: ServerDrivenNode, val viewModel: SDUIFormViewModel) : Layout {
    private var modifier = Modifier.fromNode(node)
    private val enabled = viewModel.resolveStatePlaceholdersFlow(node.property("enabled")) {
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
        val isEnabled by enabled.collectAsState()

        Button(
            modifier = modifier,
            enabled = isEnabled,
            onClick = {
                viewModel.invokeActions(
                    actions,
                    beforeInvoke = { enabled.value = false },
                    afterInvoke = { enabled.value = true })
            }) {
            loadChildren.invoke()
        }
    }
}
