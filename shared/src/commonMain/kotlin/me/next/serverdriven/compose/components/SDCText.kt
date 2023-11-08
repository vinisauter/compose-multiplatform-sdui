package me.next.serverdriven.compose.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.next.serverdriven.core.tree.ServerDrivenNode

@Composable
fun SDCText(node: ServerDrivenNode): Unit {
    val text: String = node.properties?.get("text").toString()
    Text(
        modifier = Modifier.fromNode(node.properties),
        text = text
    )
}