package me.developes.humming.sdui.layout.components

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.common.toColor
import me.developes.humming.sdui.layout.Layout
import me.developes.humming.sdui.layout.dp

class SDCOutlinedButtonText(val node: ServerDrivenNode, val viewModel: SDUIFormViewModel) : Layout {
    private var modifier = Modifier.fromNode(node)
    private val enabled = viewModel.resolveStatePlaceholdersFlow(node.property("enabled")) {
        it?.toBooleanStrictOrNull() ?: true
    }
    private val text = viewModel.resolveStatePlaceholdersFlow(node.property("text"))
    private val lineBorderColor = node.property("lineBorderColor")
    private val actions = node.propertyNodes("onClick")
    private val roundedCornerShape = node.property("roundedCornerShape")?.dp ?: 0.dp

    @Composable
    override fun Content() {
        val isEnabled by enabled.collectAsState()
        Button(
            modifier = modifier
                .border(
                    width = 1.dp,
                    color = lineBorderColor?.toColor() ?: Color.Unspecified,
                    shape = RoundedCornerShape(roundedCornerShape)
                ),
            enabled = isEnabled,
            onClick = {
                viewModel.invokeActions(
                    actions,
                    beforeInvoke = { enabled.value = false },
                    afterInvoke = { enabled.value = true }
                )
            },
            shape = RoundedCornerShape(roundedCornerShape)
        ) {
            val text by text.collectAsState()
            Text(text = text)
        }
    }
}