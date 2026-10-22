package me.developes.humming.sdui.layout.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.map
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.layout.Layout


class SDCAnimatedVisibility(node: ServerDrivenNode, private val viewModel: SDUIFormViewModel) :
    Layout {
    private val modifier = Modifier.fromNode(node)
    private val propertyVisible = node.property("visible")
    private val state = (node.property("state")
        ?: viewModel.resolveStateKeys(propertyVisible).firstOrNull()
        ?: "SDCAnimatedVisibility_${node.id}")

    private val visible = viewModel.getStateFlow(state, "true").map {
        it.toBooleanStrictOrNull() ?: true
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
        val visible by visible.collectAsState(true)
        AnimatedVisibility(visible, modifier = modifier) {
            loadChildren.invoke()
        }
    }
}