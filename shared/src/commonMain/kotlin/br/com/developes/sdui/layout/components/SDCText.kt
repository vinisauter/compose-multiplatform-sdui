package br.com.developes.sdui.layout.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import br.com.developes.sdui.ServerDrivenNode
import br.com.developes.sdui.layout.Layout

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