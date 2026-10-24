package me.developes.humming.sdui.layout.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.layout.Layout
import me.developes.humming.sdui.layout.LayoutDefinitions

/** Separate identifier keeps all legacy layout:text payloads and coercions unchanged. */
class SDCReactiveText(private val node: ServerDrivenNode, viewModel: SDUIFormViewModel) : Layout {
    private val properties = LayoutDefinitions.reactiveText.parse(node).requireValid()
    private val value = node.reactiveProperty("value", ReactiveValueType.TEXT)
    private val values = viewModel.expressionValues(value, node, "value")
    private val visibility = node.property("visible")?.let { node.reactiveProperty("visible", ReactiveValueType.BOOLEAN) }
    private val visibleValues = visibility?.let { viewModel.expressionValues(it, node, "visible") }
    private val modifier = Modifier.fromNode(node)

    @Composable override fun Content() {
        val tokens = LocalPresentationTokens.current
        val colorName = properties.property("colorToken")
        val tokenColor = colorName?.let(tokens::color)
        val visible = visibleValues?.collectAsState(ExpressionValue(checkNotNull(visibility).fallback))?.value
        val visibilityError = visible?.diagnostic
        if (visibilityError != null) Text("${visibilityError.code}: ${node.id} ${visibilityError.path}", color = Color.Red)
        if (visible != null && !(visible.value as SduiValue.BooleanValue).value) return
        val result by values.collectAsState(ExpressionValue(value.fallback))
        Column(modifier.observeNodeBounds(node)) {
            Text((result.value as SduiValue.Text).value,
                fontSize = (properties.property("fontSize")?.toFloatOrNull()?.takeIf { it.isFinite() } ?: 16f).sp,
                color = tokenColor?.toColor() ?: properties.property("color")?.toColor() ?: Color.Unspecified)
            if (colorName != null && tokenColor == null)
                Text("UNKNOWN_COLOR_TOKEN: ${node.id} ${node.sourcePath}.properties.colorToken", color = Color.Red)
            result.diagnostic?.let { Text("${it.code}: ${node.id} ${it.path}", color = Color.Red) }
        }
    }
}
