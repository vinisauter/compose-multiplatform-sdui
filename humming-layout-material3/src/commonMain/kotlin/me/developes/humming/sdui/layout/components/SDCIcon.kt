package me.developes.humming.sdui.layout.components

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.common.toColor
import me.developes.humming.sdui.layout.Layout

class SDCIcon(val node: ServerDrivenNode, val viewModel: SDUIFormViewModel) : Layout {
    private var modifier = Modifier.fromNode(node)
    private val contentDescription = node.property("contentDescription")
    private val tint = node.property("tint").toColor()
    private val icon = node.property("icon")?.let { Icons.getIcon(it) }!!

    @Composable
    override fun Content() {
        Icon(
            modifier = modifier,
            imageVector = icon,
            tint = tint,
            contentDescription = contentDescription ?: ""
        )
    }
}
