package me.next.serverdriven.compose.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.next.serverdriven.core.tree.ServerDrivenNode

@Composable
fun SDCText(node: ServerDrivenNode, state: MutableMap<String, String>) {
    val text by node.propertyState("text", state) { it ?: "" }
    Text(
        modifier = Modifier.fromNode(node),
        text = text
    )
}