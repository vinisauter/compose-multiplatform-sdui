package me.developes.humming.sdui.layout.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.layout.Layout

class SDCButtonIcon(val node: ServerDrivenNode, val viewModel: SDUIFormViewModel) : Layout {
    private var modifier = Modifier.fromNode(node)
    private val enabled = viewModel.resolveStatePlaceholdersFlow(node.property("enabled")) {
        it?.toBooleanStrictOrNull() ?: true
    }
    private val text = node.property("text")
    private val contentDescription = node.property("contentDescription")
    private val actions = node.propertyNodes("onClick")
    private val icon = node.property("icon")?.let { Icons.getIcon(it) }

    @Composable
    override fun Content() {
        val isEnabled by enabled.collectAsState()

        Button(
            modifier = modifier,
            enabled = isEnabled,
            onClick = {
                viewModel.invokeActions(
                    actions,
                    beforeInvoke = { enabled.value = false },
                    afterInvoke = { enabled.value = true }
                )
            }) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = contentDescription ?: text ?: ""
                    )
                }
                text?.let { Text(it) }
            }
        }
    }
}
