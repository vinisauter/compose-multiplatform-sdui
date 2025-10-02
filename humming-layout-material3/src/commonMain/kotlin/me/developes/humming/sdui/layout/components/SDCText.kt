package me.developes.humming.sdui.layout.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.common.toColor
import me.developes.humming.sdui.layout.Layout

class SDCText(node: ServerDrivenNode, viewModel: SDUIFormViewModel) : Layout {
    val text by viewModel.propertyState("text") { it ?: "" }
    private val modifier = Modifier.fromNode(node)
    private val fontSize = node.property("fontSize")?.toFloatOrNull()?.sp ?: 16.sp
    private val color = node.property("color")
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
            color = color?.toColor() ?: Color.Unspecified
        )
    }
}