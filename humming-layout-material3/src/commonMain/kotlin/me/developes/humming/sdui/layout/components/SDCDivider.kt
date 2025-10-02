package me.developes.humming.sdui.layout.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.common.toColor
import me.developes.humming.sdui.layout.Layout

class SDCDivider(node: ServerDrivenNode, viewModel: SDUIFormViewModel) : Layout {
    private val modifier = Modifier.fromNode(node)
    private val color = (node.property("color") ?: "#000000").toColor()

    @Composable
    override fun Content() {
        HorizontalDivider(
            modifier = modifier,
            color = color
        )
    }
}