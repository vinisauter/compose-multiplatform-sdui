package br.com.developes.sdui.layout.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import br.com.developes.sdui.ServerDrivenNode
import br.com.developes.sdui.layout.Layout
import br.com.developes.sdui.utils.hexToRgbColor

class SDCText(node: ServerDrivenNode, state: MutableMap<String, String>) : Layout {
    val text by node.propertyState("text", state) { it ?: "" }
    private val modifier = Modifier.fromNode(node)
    private val fontSize = node.property("fontSize")?.toFloatOrNull()?.sp ?: 16.sp
    private val color = node.property("color")?.hexToRgbColor()
    private val textAlign: TextAlign =
        when (node.property("textAlign")) {
            "Start" -> TextAlign.Start
            "Center" -> TextAlign.Center
            "End" -> TextAlign.End
            "Justify" -> TextAlign.Justify
            else -> TextAlign.Start
        }
    private val fontWeight: FontWeight =
        when (node.property("fontWeight")) {
            null -> FontWeight.Normal
            "Bold" -> FontWeight.Bold
            "Thin" -> FontWeight.Thin
            "ExtraBold" -> FontWeight.ExtraBold
            else -> error("Unknown value for fontWeight ${node.property("fontWeight")}")
        }

    @Composable
    override fun Content() {
        Text(
            modifier = modifier,
            text = text,
            fontWeight = fontWeight,
            textAlign = textAlign,
            fontSize = fontSize,
            color = color ?: Color.Unspecified
        )
    }
}