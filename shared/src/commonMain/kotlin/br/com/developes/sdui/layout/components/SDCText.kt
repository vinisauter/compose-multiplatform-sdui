package br.com.developes.sdui.layout.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import br.com.developes.sdui.ServerDrivenNode
import br.com.developes.sdui.layout.Layout

class SDCText(node: ServerDrivenNode, state: MutableMap<String, String>) : Layout {
    private val modifier = Modifier.fromNode(node)
    val text by node.propertyState("text", state) { it ?: "" }
    private val textColor = node.property("textColor")?.hexToRgbColor() ?: Color.Black

    private val fontWeight: FontWeight =
        when (node.property("fontWeight")) {
            null -> FontWeight.Normal
            "Bold" -> FontWeight.Bold
            "ExtraBold" -> FontWeight.ExtraBold
            else -> error("Unknown value for fontWeight ${node.property("fontWeight")}")
        }

    @Composable
    override fun Content() {
        Text(
            modifier = modifier,
            text = text,
            fontWeight = fontWeight,
            color = textColor
        )
    }
}