package me.developes.humming.sdui.layout.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import me.developes.humming.sdui.common.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.common.toColor
import me.developes.humming.sdui.layout.LayoutDefinitions
import me.developes.humming.sdui.layout.Layout

class SDCText(node: ServerDrivenNode, viewModel: SDUIFormViewModel) : Layout {
    private val properties = LayoutDefinitions.text.parse(node).requireValid()

    private val text = viewModel.binding(properties.property("text")!!)
    private val modifier = Modifier.fromNode(node)
    private val fontSize = properties.property("fontSize")?.toFloatOrNull()?.sp ?: 16.sp
    private val color = properties.property("color")
    private val textAlign: TextAlign =
        when (properties.property("textAlign")) {
            "Start" -> TextAlign.Start
            "Center" -> TextAlign.Center
            "End" -> TextAlign.End
            "Justify" -> TextAlign.Justify
            else -> TextAlign.Start
        }
    private val fontWeight: FontWeight =
        when (properties.property("fontWeight")) {
            null -> FontWeight.Normal
            "Bold" -> FontWeight.Bold
            "Thin" -> FontWeight.Thin
            "ExtraBold" -> FontWeight.ExtraBold
            else -> error("Unknown value for fontWeight ${properties.property("fontWeight")}")
        }

    @Composable
    override fun Content() {
        val text by text.collectAsState()
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