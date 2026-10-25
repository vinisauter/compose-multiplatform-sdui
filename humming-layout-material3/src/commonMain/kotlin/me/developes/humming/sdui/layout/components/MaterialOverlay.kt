@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package me.developes.humming.sdui.layout.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import me.developes.humming.sdui.common.*

private val LocalExposedMenu = staticCompositionLocalOf<ExposedDropdownMenuBoxScope?> { null }
private val LocalTooltip = staticCompositionLocalOf<TooltipScope?> { null }

@Composable internal fun SDCMaterial3.MaterialOverlay(enabled: Boolean, click: () -> Unit, change: () -> Unit, invoker: ActionInvoker) {
    val m = modifier.observeNodeBounds(node)
    when (name) {
        "alertDialog", "basicAlertDialog", "datePickerDialog", "modalBottomSheet" -> {
            val visible = state("visible", "true").toBoolean()
            val dismiss = { write("false", "visible"); invoker.invoke(node.propertyNodes("onDismiss")); Unit }
            if (visible) when (name) {
                "alertDialog" -> AlertDialog(dismiss, { slot("confirmButton") }, m,
                    dismissButton = optionalSlot("dismissButton"), title = { Text(bound("title")) }, text = { if (node.children.isNullOrEmpty()) Text(bound("text")) else children() })
                "basicAlertDialog" -> BasicAlertDialog(dismiss, m) { Surface { Column { children() } } }
                "datePickerDialog" -> DatePickerDialog(dismiss, { slot("confirmButton") }, m, dismissButton = optionalSlot("dismissButton")) { children() }
                else -> ModalBottomSheet(dismiss, m) { children() }
            }
        }
        "dropdownMenu", "exposedDropdownMenu" -> {
            val expanded = state("expanded", "false").toBoolean()
            val dismiss = { write("false", "expanded"); invoker.invoke(node.propertyNodes("onDismiss")); Unit }
            val exposed = LocalExposedMenu.current
            if (name == "dropdownMenu") DropdownMenu(expanded, dismiss, m) { children() }
            else if (exposed != null) exposed.ExposedDropdownMenu(expanded, dismiss, m) { children() }
            else Text("MATERIAL_SCOPE: ${node.id} requires exposedDropdownMenuBox | ${node.sourcePath} | material3.scope")
        }
        "exposedDropdownMenuBox" -> {
            val expanded = state("expanded", "false").toBoolean()
            ExposedDropdownMenuBox(expanded, { write(it.toString(), "expanded"); change() }, m) {
                CompositionLocalProvider(LocalExposedMenu provides this) {
                    // Anchor only the input, never the popup. Shared stateKey links box and menu.
                    node.children.orEmpty().forEach { child ->
                        if (child.component == "layout:exposedDropdownMenu") vm.loadComponent(child)?.Content()
                        else Box(Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled)) { vm.loadComponent(child)?.Content() }
                    }
                }
            }
        }
        "dropdownMenuItem" -> DropdownMenuItem({ Text(bound("text")) }, click, m, leadingIcon = optionalSlot("icon"), trailingIcon = optionalSlot("trailingIcon"), enabled = enabled)
        "snackbar" -> Snackbar(m, action = optionalSlot("action"), dismissAction = optionalSlot("dismissAction")) {
            if (node.children.isNullOrEmpty()) Text(bound("text")) else children()
        }
        "snackbarHost" -> {
            val host = remember { SnackbarHostState() }
            val message = bound("message"); val action = bound("actionLabel")
            LaunchedEffect(message, action) {
                if (message.isNotEmpty()) {
                    when (host.showSnackbar(message, action.takeIf { it.isNotEmpty() }, withDismissAction = true)) {
                        SnackbarResult.ActionPerformed -> invoker.invoke(node.propertyNodes("onAction"))
                        SnackbarResult.Dismissed -> invoker.invoke(node.propertyNodes("onDismiss"))
                    }
                }
            }
            SnackbarHost(host, m)
        }
        "searchBar", "dockedSearchBar" -> {
            val query = state("text", ""); val expanded = state("expanded", "false", "expandedStateKey").toBoolean()
            val update: (String) -> Unit = { write(it, "text"); change() }
            val expand: (Boolean) -> Unit = { write(it.toString(), "expanded", "expandedStateKey") }
            val search: (String) -> Unit = { invoker.invoke(node.propertyNodes("onSearch")) }
            val input: @Composable () -> Unit = {
                SearchBarDefaults.InputField(query, update, search, expanded, expand, enabled = enabled,
                    placeholder = { Text(bound("placeholder")) })
            }
            if (name == "searchBar") SearchBar(input, expanded, expand, m) { children() }
            else DockedSearchBar(input, expanded, expand, m) { children() }
        }
        "tooltipBox" -> {
            val tooltipState = rememberTooltipState(isPersistent = bound("persistent", "false").toBoolean())
            TooltipBox(TooltipDefaults.rememberPlainTooltipPositionProvider(), {
                CompositionLocalProvider(LocalTooltip provides this) { slot("tooltip") }
            }, tooltipState, m) { children() }
        }
        "label" -> Label(label = { CompositionLocalProvider(LocalTooltip provides this) { slot("tooltip") } }, modifier = m,
            isPersistent = bound("persistent", "false").toBoolean()) { children() }
        "plainTooltip", "richTooltip" -> {
            val scope = LocalTooltip.current
            if (scope == null) Text("MATERIAL_SCOPE: ${node.id} requires tooltipBox or label | ${node.sourcePath} | material3.scope")
            else if (name == "plainTooltip") scope.PlainTooltip(m) { if (node.children.isNullOrEmpty()) Text(bound("text")) else children() }
            else scope.RichTooltip(m, title = optionalSlot("title"), action = optionalSlot("action")) { if (node.children.isNullOrEmpty()) Text(bound("text")) else children() }
        }
        else -> MaterialPickersAndGestures(change, invoker)
    }
}
