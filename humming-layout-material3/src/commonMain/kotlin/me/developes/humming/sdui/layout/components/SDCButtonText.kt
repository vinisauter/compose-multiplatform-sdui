package me.developes.humming.sdui.layout.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import me.developes.humming.sdui.common.collectAsState
import me.developes.humming.sdui.common.rememberActionInvoker
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.layout.Layout
import me.developes.humming.sdui.layout.dp

class SDCButtonText(val node: ServerDrivenNode, val viewModel: SDUIFormViewModel) : Layout {
    private var modifier = Modifier.fromNode(node)
    private val enabled = viewModel.binding(node.property("enabled")) {
        it?.toBooleanStrictOrNull() ?: true
    }
    private val text = viewModel.binding(node.property("text"))
    private val actions = node.propertyNodes("onClick")
    private val roundedCornerShape = node.property("roundedCornerShape")?.dp ?: 0.dp

    @Composable
    override fun Content() {
        val invoker = rememberActionInvoker(viewModel)
        val isEnabled by enabled.collectAsState()

        Button(
            modifier = modifier, enabled = isEnabled && !invoker.isRunning, onClick = {
                invoker.invoke(actions)
            }, shape = RoundedCornerShape(roundedCornerShape)
        ) {
            val text by text.collectAsState()
            Text(text = text)
        }
    }
}