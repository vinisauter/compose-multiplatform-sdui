package me.developes.humming.sdui.layout.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.layout.Layout


class SDCAnimatedVisibility(node: ServerDrivenNode, viewModel: SDUIFormViewModel) : Layout {
    private val modifier = Modifier.fromNode(node)
    private val visible by viewModel.propertyState("visible") {
        it?.toBoolean() ?: true
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
        AnimatedVisibility(visible, modifier = modifier) {
            loadChildren.invoke()
        }
    }
}