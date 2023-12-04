package me.next.serverdriven.compose.components

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import me.next.serverdriven.compose.SDCLibrary
import me.next.serverdriven.core.tree.ServerDrivenNode
import me.next.serverdriven.interfaces.Layout

class SDCButton(val node: ServerDrivenNode, val state: MutableMap<String, String>) : Layout {
    private val actions = node.propertyNodes("onClick")
    private val loadChildren: @Composable () -> Unit? = {
        node.children?.let {
            for (serverDrivenNode in it) {
                SDCLibrary.loadComponent(node = serverDrivenNode, dataState = state)
            }
        }
    }

    @Composable
    override fun Content() {
        val action = SDCLibrary.loadActions(actions)
        Button(onClick = {
            action.invoke(node, state)
        }) {
            loadChildren.invoke()
        }
    }
}

class SDCButtonText(val node: ServerDrivenNode, val state: MutableMap<String, String>) : Layout {
    private val text = node.propertyState("text", state)!!
    private val actions = node.propertyNodes("onClick")

    @Composable
    override fun Content() {
        val action = SDCLibrary.loadActions(actions)
        Button(onClick = {
            action.invoke(node, state)
        }) {
            Text(text)
        }
    }
}