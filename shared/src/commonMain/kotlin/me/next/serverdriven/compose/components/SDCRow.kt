package me.next.serverdriven.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.next.serverdriven.compose.SDCLibrary
import me.next.serverdriven.core.tree.ServerDrivenNode

@Composable
fun SDCRow(node: ServerDrivenNode, dataState: MutableMap<String, String>) {
    val horizontalArrangement: Arrangement.Horizontal =
        when (node.property("horizontalArrangement")) {
            "Start" -> Arrangement.Start
            "Center" -> Arrangement.Center
            "End" -> Arrangement.End
            "SpaceAround" -> Arrangement.SpaceAround
            "SpaceBetween" -> Arrangement.SpaceBetween
            "SpaceEvenly" -> Arrangement.SpaceEvenly
            else -> Arrangement.Start
        }
    val verticalAlignment: Alignment.Vertical =
        when (node.property("verticalAlignment")) {
            "Top" -> Alignment.Top
            "Center" -> Alignment.CenterVertically
            "Bottom" -> Alignment.Bottom
            else -> Alignment.Top
        }
    Row(
        modifier = Modifier.fromNode(node),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment
    ) {
        node.children?.let {
            for (serverDrivenNode in it) {
                SDCLibrary.loadComponent(node = serverDrivenNode, dataState = dataState)
            }
        }
    }
}
