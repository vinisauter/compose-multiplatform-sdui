package me.developes.humming.sdui.layout.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.layout.Layout
import me.developes.humming.sdui.layout.LayoutDefinitions

class SDCReactiveButton(private val node: ServerDrivenNode, private val viewModel: SDUIFormViewModel) : Layout {
    init { LayoutDefinitions.reactiveButton.parse(node).requireValid() }
    private val enabled = node.reactiveProperty("enabled", ReactiveValueType.BOOLEAN)
    private val enabledValues = viewModel.expressionValues(enabled, node, "enabled")
    private val actions = node.propertyNodes("onClick")
    private val modifier = Modifier.fromNode(node)

    @Composable override fun Content() {
        val result by enabledValues.collectAsState(ExpressionValue(enabled.fallback))
        val invoker = rememberActionInvoker(viewModel)
        Column(modifier.observeNodeBounds(node)) {
            Button(enabled = (result.value as SduiValue.BooleanValue).value && !invoker.isRunning,
                onClick = { invoker.invoke(actions) }) {
                node.children.orEmpty().forEach { viewModel.loadComponent(it)?.Content() }
            }
            result.diagnostic?.let { Text("${it.code}: ${node.id} ${it.path}", color = Color.Red) }
        }
    }
}
