package br.com.developes.sdui.layout.components

import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import br.com.developes.sdui.ServerDrivenNode
import br.com.developes.sdui.layout.Layout

class SDCDivider(node: ServerDrivenNode, state: MutableMap<String, String>) : Layout {
    private val modifier = Modifier.fromNode(node)
    private val color = node.property("color") ?: "#000000"

    @Composable
    override fun Content() {
        Divider(
            modifier = modifier,
            color = color.hexToColor()
        )
    }
}