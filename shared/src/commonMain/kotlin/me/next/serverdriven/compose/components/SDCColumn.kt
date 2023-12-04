package me.next.serverdriven.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.next.serverdriven.compose.SDCLibrary
import me.next.serverdriven.core.tree.ServerDrivenNode
import me.next.serverdriven.interfaces.Layout

class SDCColumn(node: ServerDrivenNode, state: MutableMap<String, String>) : Layout {
    private val modifier = Modifier.fromNode(node)
    private val verticalArrangement: Arrangement.Vertical =
        when (node.property("verticalArrangement")) {
            "Top" -> Arrangement.Top
            "Center" -> Arrangement.Center
            "Bottom" -> Arrangement.Bottom
            "SpaceAround" -> Arrangement.SpaceAround
            "SpaceBetween" -> Arrangement.SpaceBetween
            "SpaceEvenly" -> Arrangement.SpaceEvenly
            else -> Arrangement.Top
        }
    private val horizontalAlignment: Alignment.Horizontal =
        when (node.property("horizontalAlignment")) {
            "Start" -> Alignment.Start
            "Center" -> Alignment.CenterHorizontally
            "End" -> Alignment.End
            else -> Alignment.Start
        }
    private val loadChildren: @Composable () -> Unit? = {
        node.children?.let {
            for (serverDrivenNode in it) {
                SDCLibrary.loadComponent(node = serverDrivenNode, dataState = state)
            }
        }
    }

    @Composable
    override fun Content() {
        Column(
            modifier = modifier,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment
        ) {
            loadChildren.invoke()
        }
    }
}
