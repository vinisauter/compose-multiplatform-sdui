package me.developes.humming.sdui.layout.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.common.toColor
import me.developes.humming.sdui.layout.Layout
import me.developes.humming.sdui.layout.dp

class SDCButtonText(val node: ServerDrivenNode, val viewModel: SDUIFormViewModel) : Layout {
    private var modifier = Modifier.fromNode(node)
    private val enabled by viewModel.propertyState("enabled") { it?.toBoolean() ?: true }
    private val text by viewModel.propertyState("text") { it ?: "" }
    private val actions = node.propertyNodes("onClick")
    private val roundedCornerShape = node.property("roundedCornerShape")?.dp ?: 0.dp
    private val backgroundColor = node.property("backgroundColor").toColor()
    private val color = node.property("color")

    @Composable
    override fun Content() {
        var isEnabled by remember { mutableStateOf(enabled) }

        Button(
            modifier = modifier,
            enabled = isEnabled,
//            colors = ButtonDefaults.buttonColors(
//                backgroundColor = backgroundColor
//            ),
            onClick = {
                viewModel.invokeActions(
                    actions,
                    beforeInvoke = { isEnabled = false },
                    afterInvoke = { isEnabled = true })
            },
            shape = RoundedCornerShape(roundedCornerShape)
        ) {
            Text(
                text = text,
//                color = color
            )
        }
    }
}