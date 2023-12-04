package me.next.serverdriven.core.library.layout.components

import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.next.serverdriven.compose.SDCLibrary
import me.next.serverdriven.core.library.interfaces.Layout
import me.next.serverdriven.core.tree.ServerDrivenNode

class SDCTextField(node: ServerDrivenNode, val state: MutableMap<String, String>) : Layout {
    private val modifier = Modifier.fromNode(node)
    private val onChangeUpdateState = node.property("onChangeUpdateState") ?: "TextField_${node.id}"
    private val text = node.property("text")
    private val loadChildren: @Composable () -> Unit? = {
        node.children?.let {
            for (serverDrivenNode in it) {
                SDCLibrary.loadComponent(node = serverDrivenNode, dataState = state)
            }
        }
    }

    @Composable
    override fun Content() {
        if (state[onChangeUpdateState] == null) {
            text?.run {
                state[onChangeUpdateState] = this
            }
        }
        TextField(
            modifier = modifier,
            value = state[onChangeUpdateState] ?: "",
            onValueChange = { state[onChangeUpdateState] = it },
            label = {
                loadChildren.invoke()
            }
        )
    }
}