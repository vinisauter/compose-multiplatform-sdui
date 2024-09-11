package br.com.developes.sdui.layout.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import br.com.developes.sdui.SDCLibrary
import br.com.developes.sdui.ServerDrivenNode
import br.com.developes.sdui.layout.Layout


class SDCAnimatedVisibility(node: ServerDrivenNode, state: MutableMap<String, String>) : Layout {
    private val modifier = Modifier.fromNode(node)
    private val visible by node.propertyState("visible", state) {
        it?.toBoolean() ?: true
    }
    private val loadChildren: @Composable () -> Unit? = {
        node.children?.let {
            for (serverDrivenNode in it) {
                SDCLibrary.loadComponent(node = serverDrivenNode, dataState = state)
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