package me.next.serverdriven.compose.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.next.serverdriven.core.tree.ServerDrivenNode
import me.next.serverdriven.interfaces.Layout

class SDCText(node: ServerDrivenNode, state: MutableMap<String, String>) : Layout {
    private val modifier = Modifier.fromNode(node)
    val text by node.propertyState("text", state) { it ?: "" }

    @Composable
    override fun Content() {
        Text(
            modifier = modifier,
            text = text
        )
    }
}