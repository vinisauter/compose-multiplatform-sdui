package me.next.serverdriven.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.next.serverdriven.compose.loadComponent
import me.next.serverdriven.core.tree.ServerDrivenNode

@Composable
fun SDCColumn(node: ServerDrivenNode): Unit {
    val verticalArrangement: Arrangement.Vertical =
        when (node.properties?.get("verticalArrangement")) {
            "Top" -> Arrangement.Top
            "Center" -> Arrangement.Center
            "Bottom" -> Arrangement.Bottom
            "SpaceAround" -> Arrangement.SpaceAround
            "SpaceBetween" -> Arrangement.SpaceBetween
            "SpaceEvenly" -> Arrangement.SpaceEvenly
            else -> Arrangement.Top
        }
    val horizontalAlignment: Alignment.Horizontal =
        when (node.properties?.get("horizontalAlignment")) {
            "Start" -> Alignment.Start
            "Center" -> Alignment.CenterHorizontally
            "End" -> Alignment.End
            else -> Alignment.Start
        }
    Column(
        modifier = Modifier.fromNode(node.properties),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment
    ) {
        node.children?.let {
            for (serverDrivenNode in it) {
                loadComponent(node = serverDrivenNode)
            }
        }
    }
}
