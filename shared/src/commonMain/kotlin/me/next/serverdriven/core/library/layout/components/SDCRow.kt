package me.next.serverdriven.core.library.layout.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.next.serverdriven.compose.SDCLibrary
import me.next.serverdriven.core.library.interfaces.Layout
import me.next.serverdriven.core.tree.ServerDrivenNode

class SDCRow(node: ServerDrivenNode, dataState: MutableMap<String, String>) : Layout {
    private val modifier = Modifier.fromNode(node)
    private val horizontalArrangement: Arrangement.Horizontal =
        when (node.property("horizontalArrangement")) {
            "Start" -> Arrangement.Start
            "Center" -> Arrangement.Center
            "End" -> Arrangement.End
            "SpaceAround" -> Arrangement.SpaceAround
            "SpaceBetween" -> Arrangement.SpaceBetween
            "SpaceEvenly" -> Arrangement.SpaceEvenly
            else -> Arrangement.Start
        }
    private val verticalAlignment: Alignment.Vertical =
        when (node.property("verticalAlignment")) {
            "Top" -> Alignment.Top
            "Center" -> Alignment.CenterVertically
            "Bottom" -> Alignment.Bottom
            else -> Alignment.Top
        }
    private val loadChildren: @Composable () -> Unit? = {
        node.children?.let {
            for (serverDrivenNode in it) {
                SDCLibrary.loadComponent(node = serverDrivenNode, dataState = dataState)
            }
        }
    }

    @Composable
    override fun Content() {
        Row(
            modifier = modifier,
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment
        ) {
            loadChildren.invoke()
        }
    }
}
