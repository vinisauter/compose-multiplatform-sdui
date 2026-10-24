package me.developes.humming.sdui.layout.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonPrimitive
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.layout.Layout
import me.developes.humming.sdui.layout.LayoutDefinitions

/** Edits canonical Text only. Validation/presentation never write derived values into state. */
class SDCValidatedTextField(private val node: ServerDrivenNode, private val viewModel: SDUIFormViewModel) : Layout {
    private val properties = LayoutDefinitions.validatedTextField.parse(node).requireValid()
    private val stateKey = checkNotNull(properties.property("stateKey"))
    private val values = viewModel.onStateUpdated.map { viewModel.stateValue(stateKey) }.distinctUntilChanged()
    private val valid = node.reactiveProperty("valid", ReactiveValueType.BOOLEAN)
    private val validity = viewModel.expressionValues(valid, node, "valid")
    private val enabled = node.property("enabled")?.let { node.reactiveProperty("enabled", ReactiveValueType.BOOLEAN) }
    private val enabledValues = enabled?.let { viewModel.expressionValues(it, node, "enabled") }
    private val modifier = Modifier.fromNode(node)

    @Composable override fun Content() {
        val canonical by values.collectAsState(viewModel.stateValue(stateKey))
        val validation by validity.collectAsState(ExpressionValue(valid.fallback))
        val availability = enabledValues?.collectAsState(ExpressionValue(checkNotNull(enabled).fallback))?.value
        val typeError = canonical !== SduiValue.Missing && canonical !is SduiValue.Text
        val text = (canonical as? SduiValue.Text)?.value.orEmpty()
        val tokens = LocalPresentationTokens.current
        val maskName = properties.property("maskToken")
        val mask = maskName?.let(tokens::mask)
        val maskError = remember(mask, text) { try { mask?.format(text); null } catch (error: PayloadException) { error.diagnostic } }
        val transformation = remember(mask) { mask?.let(::DigitMaskTransformation) ?: VisualTransformation.None }
        val invalid = !(validation.value as SduiValue.BooleanValue).value
        Column(modifier.observeNodeBounds(node)) {
            TextField(value = text, onValueChange = { viewModel.updateValue(stateKey, SduiValue.Text(it)) },
                label = properties.property("label")?.let { label -> { Text(label) } },
                enabled = availability?.let { (it.value as SduiValue.BooleanValue).value } ?: true,
                visualTransformation = transformation,
                isError = invalid || typeError || validation.diagnostic != null || maskError != null || (maskName != null && mask == null),
                supportingText = if (invalid) { { Text(properties.property("errorText").orEmpty()) } } else null)
            if (typeError) Text("FIELD_VALUE_TYPE: ${node.id} ${node.sourcePath}.state[${JsonPrimitive(stateKey)}]", color = Color.Red)
            validation.diagnostic?.let { Text("${it.code}: ${node.id} ${it.path}", color = Color.Red) }
            availability?.diagnostic?.let { Text("${it.code}: ${node.id} ${it.path}", color = Color.Red) }
            if (maskName != null && mask == null) Text("UNKNOWN_MASK_TOKEN: ${node.id} ${node.sourcePath}.properties.maskToken", color = Color.Red)
            maskError?.let { Text("${it.code}: ${node.id} ${node.sourcePath}.properties.maskToken", color = Color.Red) }
        }
    }
}
