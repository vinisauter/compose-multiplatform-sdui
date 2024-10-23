package br.com.developes.sdui.layout.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import br.com.developes.sdui.SDCLibrary
import br.com.developes.sdui.SDCLibrary.Companion.launchHandling
import br.com.developes.sdui.ServerDrivenNode
import br.com.developes.sdui.layout.Layout

class SDCColumn(val node: ServerDrivenNode, val state: MutableMap<String, String>) : Layout {
    private val modifier = Modifier.fromNode(node)
    private val actions = node.propertyNodes("onClick")

    private val verticalArrangement: Arrangement.Vertical =
        when (node.property("verticalArrangement")) {
            null -> Arrangement.Top
            "Top" -> Arrangement.Top
            "Center" -> Arrangement.Center
            "Bottom" -> Arrangement.Bottom
            "SpaceAround" -> Arrangement.SpaceAround
            "SpaceBetween" -> Arrangement.SpaceBetween
            "SpaceEvenly" -> Arrangement.SpaceEvenly
            else -> error("Unknown value for verticalArrangement ${node.property("verticalArrangement")}")
        }
    private val horizontalAlignment: Alignment.Horizontal =
        when (node.property("horizontalAlignment")) {
            null -> Alignment.Start
            "Start" -> Alignment.Start
            "Center" -> Alignment.CenterHorizontally
            "End" -> Alignment.End
            else -> error("Unknown value for horizontalAlignment ${node.property("horizontalAlignment")}")
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
        val action = SDCLibrary.loadActions(actions)
        val scope = rememberCoroutineScope()

        Column(
            modifier = if (actions.isNotEmpty()) modifier.clickable {
            scope.launchHandling {
                action.invoke(node, state)
            } } else modifier,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
        ) {
            loadChildren.invoke()
        }
    }
}
