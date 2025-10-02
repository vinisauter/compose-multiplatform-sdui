package me.developes.humming.sdui.layout.components

import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.layout.Layout

class SDCButton(val node: ServerDrivenNode, val viewModel: SDUIFormViewModel) : Layout {
    private var modifier = Modifier.fromNode(node)
    private val enabled by viewModel.propertyState("enabled") { it?.toBoolean() ?: true }
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
        var isEnabled by remember { mutableStateOf(enabled) }

        Button(
            modifier = modifier,
            enabled = isEnabled,
            onClick = {
                viewModel.invokeActions(
                    actions,
                    beforeInvoke = { isEnabled = false },
                    afterInvoke = { isEnabled = true })
            }) {
            loadChildren.invoke()
        }
    }
}
