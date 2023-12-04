package me.next.serverdriven.core.library.layout.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.next.serverdriven.core.library.interfaces.Layout
import me.next.serverdriven.core.tree.ServerDrivenNode

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