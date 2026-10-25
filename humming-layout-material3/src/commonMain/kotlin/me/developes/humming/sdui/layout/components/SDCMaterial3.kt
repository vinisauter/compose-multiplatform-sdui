@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package me.developes.humming.sdui.layout.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.layout.Layout
import me.developes.humming.sdui.layout.Material3Definitions

/** M3 adapters live in the UI library; wire definitions remain in the pure contract. */
internal class SDCMaterial3(internal val node: ServerDrivenNode, internal val vm: SDUIFormViewModel) : Layout {
    internal val name = node.component.substringAfter(':')
    init {
        Material3Definitions.get(name).parse(node).requireValid()
        if (name == "slider" || name == "rangeSlider") {
            if (number("valueFrom", 0f) >= number("valueTo", 1f)) throw PayloadException(node.diagnostic(
                "PROPERTY_RANGE", "valueFrom must be smaller than valueTo", "material3", "properties.valueTo"))
        }
    }
    internal val modifier = Modifier.fromNode(node).testTag(node.id)
    internal fun raw(key: String, default: String = "") = node.property(key) ?: default
    @Composable internal fun bound(key: String, default: String = ""): String {
        val binding = remember(node, key) { vm.binding(raw(key, default)) }
        return binding.collectAsState().value
    }
    @Composable internal fun state(property: String, default: String, keyProperty: String = "stateKey"): String {
        val key = raw(keyProperty, "material3:${node.id}:$property")
        val binding = remember(node, key, property) { vm.stateBinding(key, raw(property, default)) }
        return binding.collectAsState().value
    }
    internal fun write(value: String, property: String, keyProperty: String = "stateKey") =
        vm.updateState(raw(keyProperty, "material3:${node.id}:$property"), value)
    @Composable internal fun children() { node.children.orEmpty().forEach { vm.loadComponent(it)?.Content() } }
    @Composable internal fun slot(key: String) { node.propertyNodes(key).forEach { vm.loadComponent(it)?.Content() } }
    internal fun has(key: String) = node.property(key) != null
    @Composable internal fun label() { if (has("text")) Text(bound("text")) else children() }
    @Composable internal fun icon() { if (has("icon")) slot("icon") else children() }
    @Composable internal fun optionalSlot(key: String): (@Composable () -> Unit)? =
        if (has(key)) ({ slot(key) }) else null
    internal fun number(key: String, default: Float) = raw(key).toFloatOrNull()?.takeIf { it.isFinite() } ?: default

    @Composable override fun Content() {
        val invoker = rememberActionInvoker(vm)
        val enabled = bound("enabled", "true").toBooleanStrictOrNull() == true && !invoker.isRunning
        val click = { if (enabled) invoker.invoke(node.propertyNodes("onClick")); Unit }
        val change = { invoker.invoke(node.propertyNodes("onChange")); Unit }
        val changeAndClick = { invoker.invoke(node.propertyNodes("onChange") + node.propertyNodes("onClick")); Unit }
        val m = modifier.observeNodeBounds(node)
        val clickModifier = if (enabled) m else m.semantics { disabled() }
        when (name) {
            "elevatedButton" -> ElevatedButton(click, m, enabled) { children() }
            "filledTonalButton" -> FilledTonalButton(click, m, enabled) { children() }
            "materialTextButton" -> TextButton(click, m, enabled) { children() }
            "materialOutlinedButton" -> OutlinedButton(click, m, enabled) { children() }
            "materialIconButton" -> IconButton(click, m, enabled) { icon() }
            "filledIconButton" -> FilledIconButton(click, m, enabled) { icon() }
            "filledTonalIconButton" -> FilledTonalIconButton(click, m, enabled) { icon() }
            "outlinedIconButton" -> OutlinedIconButton(click, m, enabled) { icon() }
            "iconToggleButton", "filledIconToggleButton", "filledTonalIconToggleButton", "outlinedIconToggleButton" -> {
                val checked = state("checked", "false").toBooleanStrictOrNull() == true
                val update: (Boolean) -> Unit = { write(it.toString(), "checked"); change() }
                when (name) {
                    "iconToggleButton" -> IconToggleButton(checked, update, m, enabled) { icon() }
                    "filledIconToggleButton" -> FilledIconToggleButton(checked, update, m, enabled) { icon() }
                    "filledTonalIconToggleButton" -> FilledTonalIconToggleButton(checked, update, m, enabled) { icon() }
                    else -> OutlinedIconToggleButton(checked, update, m, enabled) { icon() }
                }
            }
            "floatingActionButton" -> FloatingActionButton(click, clickModifier) { icon() }
            "smallFloatingActionButton" -> SmallFloatingActionButton(click, clickModifier) { icon() }
            "largeFloatingActionButton" -> LargeFloatingActionButton(click, clickModifier) { icon() }
            "extendedFloatingActionButton" -> ExtendedFloatingActionButton(onClick = click, modifier = clickModifier) {
                if (has("icon")) slot("icon"); label()
            }
            "card", "elevatedCard", "outlinedCard", "surface" -> {
                val clickable = bound("clickable", "false").toBoolean() || has("onClick")
                when (name) {
                    "card" -> if (clickable) Card(click, m, enabled) { children() } else Card(m) { children() }
                    "elevatedCard" -> if (clickable) ElevatedCard(click, m, enabled) { children() } else ElevatedCard(m) { children() }
                    "outlinedCard" -> if (clickable) OutlinedCard(click, m, enabled) { children() } else OutlinedCard(m) { children() }
                    else -> if (clickable) Surface(click, m, enabled) { children() } else Surface(m) { children() }
                }
            }
            "checkbox", "switch" -> {
                val checked = state("checked", "false").toBooleanStrictOrNull() == true
                val update: (Boolean) -> Unit = { write(it.toString(), "checked"); change() }
                if (name == "checkbox") Checkbox(checked, update, m, enabled) else Switch(checked, update, m, enabled = enabled)
            }
            "triStateCheckbox" -> {
                val value = state("value", "Off")
                val checked = when (value) { "On" -> ToggleableState.On; "Indeterminate" -> ToggleableState.Indeterminate; else -> ToggleableState.Off }
                TriStateCheckbox(checked, { write(if (checked == ToggleableState.On) "Off" else "On", "value"); changeAndClick() }, m, enabled)
            }
            "radioButton" -> {
                val selectedValue = bound("value", "true")
                val selected = state("selected", "false").let { if (has("stateKey") || has("value")) it == selectedValue else it.toBoolean() }
                RadioButton(selected, { write(selectedValue, "selected"); changeAndClick() }, m, enabled)
            }
            "assistChip", "elevatedAssistChip", "suggestionChip", "elevatedSuggestionChip" -> when (name) {
                "assistChip" -> AssistChip(click, { label() }, m, enabled, leadingIcon = optionalSlot("icon"))
                "elevatedAssistChip" -> ElevatedAssistChip(click, { label() }, m, enabled, leadingIcon = optionalSlot("icon"))
                "suggestionChip" -> SuggestionChip(click, { label() }, m, enabled, icon = optionalSlot("icon"))
                else -> ElevatedSuggestionChip(click, { label() }, m, enabled, icon = optionalSlot("icon"))
            }
            "filterChip", "elevatedFilterChip", "inputChip" -> {
                val checked = state("checked", "false").toBoolean()
                val update = { write((!checked).toString(), "checked"); changeAndClick() }
                when (name) {
                    "filterChip" -> FilterChip(checked, update, { label() }, m, enabled, leadingIcon = optionalSlot("icon"), trailingIcon = optionalSlot("trailingIcon"))
                    "elevatedFilterChip" -> ElevatedFilterChip(checked, update, { label() }, m, enabled, leadingIcon = optionalSlot("icon"), trailingIcon = optionalSlot("trailingIcon"))
                    else -> InputChip(checked, update, { label() }, m, enabled, leadingIcon = optionalSlot("icon"), trailingIcon = optionalSlot("trailingIcon"))
                }
            }
            "slider", "rangeSlider" -> {
                val from = number("valueFrom", 0f); val to = number("valueTo", 1f)
                val value = state("value", from.toString()).toFloatOrNull()?.takeIf { it.isFinite() }?.coerceIn(from, to) ?: from
                val steps = raw("steps", "0").toInt().coerceAtMost(10_000)
                val finish = { invoker.invoke(node.propertyNodes("onValueChangeFinished")); Unit }
                if (name == "slider") Slider(value, { write(it.toString(), "value"); change() }, m, enabled, valueRange = from..to, steps = steps, onValueChangeFinished = finish)
                else {
                    val end = state("endValue", to.toString(), "endStateKey").toFloatOrNull()?.takeIf { it.isFinite() }?.coerceIn(value, to) ?: to
                    RangeSlider(value..end, { write(it.start.toString(), "value"); write(it.endInclusive.toString(), "endValue", "endStateKey"); change() }, m, enabled, valueRange = from..to, steps = steps, onValueChangeFinished = finish)
                }
            }
            "linearProgressIndicator", "circularProgressIndicator" -> {
                val progress = bound("progress").toFloatOrNull()?.takeIf { it.isFinite() }?.coerceIn(0f, 1f) ?: 0f
                val indeterminate = bound("indeterminate", (!has("progress")).toString()).toBoolean()
                if (name == "linearProgressIndicator") {
                    if (indeterminate) LinearProgressIndicator(m) else LinearProgressIndicator(progress = { progress }, modifier = m)
                } else { if (indeterminate) CircularProgressIndicator(m) else CircularProgressIndicator(progress = { progress }, modifier = m) }
            }
            "horizontalDivider" -> HorizontalDivider(m, number("thickness", 1f).dp)
            "verticalDivider" -> VerticalDivider(m, number("thickness", 1f).dp)
            "badge" -> Badge(m) { label() }
            "badgedBox" -> BadgedBox({ slot("badge") }, m) { children() }
            "listItem" -> ListItem(headlineContent = { if (has("headlineContent")) slot("headlineContent") else label() }, modifier = m,
                overlineContent = optionalSlot("overlineContent"), supportingContent = optionalSlot("supportingContent"), leadingContent = optionalSlot("leadingContent"), trailingContent = optionalSlot("trailingContent"))
            "outlinedTextField" -> {
                val text = state("text", "")
                OutlinedTextField(text, { write(it, "text"); change() }, m, enabled, readOnly = bound("readOnly", "false").toBoolean(),
                    label = if (has("label")) ({ Text(bound("label")) }) else null,
                    placeholder = if (has("placeholder")) ({ Text(bound("placeholder")) }) else null,
                    isError = bound("isError", "false").toBoolean(), singleLine = bound("singleLine", "false").toBoolean())
            }
            else -> MaterialStructure(enabled, click, change, invoker)
        }
    }
}
