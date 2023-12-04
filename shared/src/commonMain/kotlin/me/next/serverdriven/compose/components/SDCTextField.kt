package me.next.serverdriven.compose.components

import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import me.next.serverdriven.compose.SDCLibrary
import me.next.serverdriven.core.tree.ServerDrivenNode

@Composable
fun SDCTextField(node: ServerDrivenNode, state: MutableMap<String, String>) {
    val onChangeUpdateState =
        node.property("onChangeUpdateState") ?: "TextField_${node.id}"
    if (state[onChangeUpdateState] == null) {
        node.property("text")?.run {
            state[onChangeUpdateState] = this
        }
    }
    TextField(
        value = state[onChangeUpdateState] ?: "",
        onValueChange = { state[onChangeUpdateState] = it },
        label = {
            node.children?.let {
                for (serverDrivenNode in it) {
                    SDCLibrary.loadComponent(node = serverDrivenNode, dataState = state)
                }
            }
        }
    )
}