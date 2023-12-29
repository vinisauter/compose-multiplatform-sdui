package me.next.serverdriven.core.library.layout.components

import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import me.next.serverdriven.compose.SDCLibrary
import me.next.serverdriven.compose.SDCLibrary.Companion.launchHandling
import me.next.serverdriven.core.library.interfaces.Layout
import me.next.serverdriven.core.tree.ServerDrivenNode

class SDCButton(val node: ServerDrivenNode, val state: MutableMap<String, String>) : Layout {
    private var modifier = Modifier.fromNode(node)
    private val enabled = node.propertyState("enabled", state)?.toBoolean()
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
        var isEnabled by remember { mutableStateOf(enabled ?: true) }
        val action = SDCLibrary.loadActions(actions)
        // Creates a CoroutineScope bound to the Content's lifecycle
        val scope = rememberCoroutineScope()
        Button(modifier = modifier,
            enabled = isEnabled,
            onClick = {
                isEnabled = false
                scope.launchHandling(after = { isEnabled = true }) {
                    action.invoke(node, state)
                }
            }) {
            loadChildren.invoke()
        }
    }
}
