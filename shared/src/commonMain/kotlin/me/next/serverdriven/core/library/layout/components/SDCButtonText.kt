package me.next.serverdriven.core.library.layout.components

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.next.serverdriven.compose.SDCLibrary
import me.next.serverdriven.core.library.interfaces.Layout
import me.next.serverdriven.core.tree.ServerDrivenNode

class SDCButtonText(val node: ServerDrivenNode, val state: MutableMap<String, String>) : Layout {
    private var modifier = Modifier.fromNode(node)
    private val text = node.propertyState("text", state)!!
    private val actions = node.propertyNodes("onClick")

    @Composable
    override fun Content() {
        val action = SDCLibrary.loadActions(actions)
        Button(modifier = modifier, onClick = {
            action.invoke(node, state)
        }) {
            Text(text)
        }
    }
}