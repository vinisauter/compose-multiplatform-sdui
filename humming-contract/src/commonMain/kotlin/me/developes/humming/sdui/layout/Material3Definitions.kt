package me.developes.humming.sdui.layout

import me.developes.humming.sdui.common.*

/** Wire surface for the Material 3 version shipped by the client, independent of Compose. */
object Material3Definitions {
    private fun text(name: String, default: String? = null) = PropertyDefinition(name, defaultValue = default, allowsBinding = true)
    private fun bool(name: String, default: Boolean = false) = PropertyDefinition(name, PropertyKind.BOOLEAN,
        defaultValue = default.toString(), allowsBinding = true)
    private fun number(name: String, default: String, kind: PropertyKind = PropertyKind.FLOAT, min: Double? = null) =
        PropertyDefinition(name, kind, defaultValue = default, minimum = min)
    private fun slot(name: String) = PropertyDefinition(name, PropertyKind.NODES, nodeRole = NodeRole.COMPONENT)
    private fun event(name: String) = PropertyDefinition(name, PropertyKind.NODES, nodeRole = NodeRole.ACTION)
    private val enabled = bool("enabled", true)
    private val state = PropertyDefinition("stateKey")
    private val click = listOf(enabled, event("onClick"))
    private val toggle = click + listOf(state, bool("checked"), event("onChange"))
    private val label = listOf(text("text", ""), slot("icon"))
    private val selected = click + listOf(state, text("value", "true"), bool("selected"))
    private fun definition(name: String, props: List<PropertyDefinition> = emptyList(), children: Boolean = false) =
        NodeDefinition("layout:$name", NodeRole.COMPONENT, LayoutDefinitions.modifiers + props,
            childrenRole = if (children) NodeRole.COMPONENT else null)
    private fun variants(names: String, props: List<PropertyDefinition>, children: Boolean = false) =
        names.split('|').map { definition(it, props, children) }

    // Keep legacy textButton/outlinedButton/iconButton untouched; their historical UI is not the named M3 primitive.
    val all: List<NodeDefinition> = buildList {
        addAll(variants("elevatedButton|filledTonalButton|materialTextButton|materialOutlinedButton", click, true))
        addAll(variants("materialIconButton|filledIconButton|filledTonalIconButton|outlinedIconButton", click + label, true))
        addAll(variants("iconToggleButton|filledIconToggleButton|filledTonalIconToggleButton|outlinedIconToggleButton", toggle + label, true))
        addAll(variants("floatingActionButton|smallFloatingActionButton|largeFloatingActionButton|extendedFloatingActionButton", click + label, true))
        addAll(variants("card|elevatedCard|outlinedCard|surface", click + bool("clickable"), true))
        addAll(variants("checkbox|switch", toggle))
        add(definition("triStateCheckbox", click + state + PropertyDefinition("value", PropertyKind.ENUM,
            defaultValue = "Off", enumValues = setOf("Off", "On", "Indeterminate")) + event("onChange")))
        add(definition("radioButton", selected + event("onChange")))
        addAll(variants("assistChip|elevatedAssistChip|suggestionChip|elevatedSuggestionChip", click + label))
        addAll(variants("filterChip|elevatedFilterChip|inputChip", toggle + label + slot("trailingIcon")))
        addAll(variants("slider|rangeSlider", listOf(enabled, state, text("value", "0"), PropertyDefinition("endStateKey"), text("endValue", "1"),
            number("valueFrom", "0"), number("valueTo", "1"), number("steps", "0", PropertyKind.INTEGER, 0.0), event("onChange"), event("onValueChangeFinished"))))
        addAll(variants("linearProgressIndicator|circularProgressIndicator", listOf(text("progress"), bool("indeterminate", true).copy(
            defaultValue = null, defaultDescription = "absent: indeterminate unless progress is supplied"))))
        addAll(variants("horizontalDivider|verticalDivider", listOf(number("thickness", "1", min = 0.0))))
        add(definition("badge", listOf(text("text", "")), true))
        add(definition("badgedBox", listOf(slot("badge")), true))
        add(definition("listItem", listOf(text("text", ""), slot("headlineContent"), slot("overlineContent"), slot("supportingContent"), slot("leadingContent"), slot("trailingContent"))))
        add(definition("outlinedTextField", listOf(state, text("text", ""), text("label"), text("placeholder"), enabled,
            bool("readOnly"), bool("isError"), bool("singleLine"), event("onChange"))))
        addAll(variants("centerAlignedTopAppBar|mediumTopAppBar|largeTopAppBar", listOf(text("title", ""), slot("navigationIcon"), slot("actions"))))
        addAll(variants("bottomAppBar|navigationBar|navigationRail", emptyList(), true))
        addAll(variants("navigationBarItem|navigationRailItem|navigationDrawerItem", selected + label + slot("badge")))
        addAll(variants("primaryTabRow|secondaryTabRow|tabRow|primaryScrollableTabRow|secondaryScrollableTabRow|scrollableTabRow",
            listOf(state, number("selectedTabIndex", "0", PropertyKind.INTEGER, 0.0)), true))
        addAll(variants("tab|leadingIconTab", selected + label))
        addAll(variants("singleChoiceSegmentedButtonRow|multiChoiceSegmentedButtonRow", emptyList(), true))
        add(definition("segmentedButton", toggle + bool("selected") + text("value", "true") + label))
        addAll(variants("modalNavigationDrawer|dismissibleNavigationDrawer|permanentNavigationDrawer",
            listOf(state, bool("open"), bool("gesturesEnabled", true), slot("drawerContent"), event("onChange")), true))
        addAll(variants("modalDrawerSheet|dismissibleDrawerSheet|permanentDrawerSheet", emptyList(), true))
        addAll(variants("alertDialog|basicAlertDialog|datePickerDialog", listOf(state, bool("visible", true), text("title", ""), text("text", ""),
            slot("confirmButton"), slot("dismissButton"), event("onDismiss")), true))
        add(definition("dropdownMenu", listOf(state, bool("expanded"), event("onDismiss")), true))
        add(definition("dropdownMenuItem", click + label + slot("trailingIcon")))
        add(definition("exposedDropdownMenuBox", listOf(state, bool("expanded"), event("onChange")), true))
        add(definition("exposedDropdownMenu", listOf(state, bool("expanded"), event("onDismiss")), true))
        add(definition("modalBottomSheet", listOf(state, bool("visible", true), event("onDismiss")), true))
        add(definition("bottomSheetScaffold", listOf(slot("sheetContent"), slot("topBar"), number("sheetPeekHeight", "56", min = 0.0)), true))
        add(definition("snackbar", listOf(text("text", ""), slot("action"), slot("dismissAction")), true))
        add(definition("snackbarHost", listOf(text("message", ""), text("actionLabel"), event("onAction"), event("onDismiss"))))
        addAll(variants("searchBar|dockedSearchBar", listOf(state, text("text", ""), PropertyDefinition("expandedStateKey"), bool("expanded"), enabled,
            text("placeholder", ""), event("onChange"), event("onSearch")), true))
        addAll(variants("datePicker|dateRangePicker", listOf(state, text("value"), PropertyDefinition("endStateKey"), text("endValue"), bool("showModeToggle", true), event("onChange"))))
        addAll(variants("timePicker|timeInput", listOf(state, text("value", "00:00"), bool("is24Hour", true), event("onChange"))))
        addAll(variants("tooltipBox|label", listOf(slot("tooltip"), bool("persistent")), true))
        addAll(variants("plainTooltip|richTooltip", listOf(text("text", ""), slot("title"), slot("action")), true))
        add(definition("swipeToDismissBox", listOf(state, text("value", "Settled"), slot("backgroundContent"), bool("gesturesEnabled", true), event("onChange")), true))
        add(definition("pullToRefreshBox", listOf(state, bool("refreshing"), event("onRefresh")), true))
        addAll(variants("horizontalMultiBrowseCarousel|horizontalUncontainedCarousel", listOf(number("itemWidth", "180", min = 1.0), number("itemSpacing", "0", min = 0.0)), true))
    }
    val names: Set<String> = all.map { it.identifier.substringAfter(':') }.toSet()
    fun get(name: String): NodeDefinition = all.first { it.identifier == "layout:$name" }
}

/** Schemas for existing M3 wrappers; preserve their historical wire and rendering. */
object LegacyMaterial3Definitions {
    private fun text(name: String, binding: Boolean = false) = PropertyDefinition(name, allowsBinding = binding)
    private fun event(name: String) = PropertyDefinition(name, PropertyKind.NODES, nodeRole = NodeRole.ACTION)
    private fun slot(name: String) = PropertyDefinition(name, PropertyKind.NODES, nodeRole = NodeRole.COMPONENT)
    private val enabled = PropertyDefinition("enabled", PropertyKind.BOOLEAN, defaultValue = "true", allowsBinding = true,
        invalidPolicy = InvalidPropertyPolicy.LEGACY_FALLBACK)
    private fun component(name: String, props: List<PropertyDefinition>, children: Boolean = false) =
        NodeDefinition("layout:$name", NodeRole.COMPONENT, LayoutDefinitions.modifiers + props,
            childrenRole = if (children) NodeRole.COMPONENT else null)
    val all: List<NodeDefinition> by lazy { listOf(
        component("textButton", listOf(text("text", true), enabled, event("onClick"), PropertyDefinition("roundedCornerShape", PropertyKind.FLOAT))),
        component("outlinedButton", listOf(text("text", true), enabled, event("onClick"), PropertyDefinition("roundedCornerShape", PropertyKind.FLOAT), PropertyDefinition("lineBorderColor", PropertyKind.COLOR))),
        component("iconButton", listOf(text("text"), text("icon"), text("contentDescription"), enabled, event("onClick"))),
        component("icon", listOf(text("icon").copy(required = true), text("contentDescription"), PropertyDefinition("tint", PropertyKind.COLOR))),
        component("divider", listOf(PropertyDefinition("color", PropertyKind.COLOR))),
        component("topAppBar", listOf(text("title", true), text("icon"), text("iconDescription"), enabled, event("onClick"), PropertyDefinition("color", PropertyKind.COLOR))),
        component("scaffold", listOf(slot("topBar"), slot("bottomBar"), slot("snackbarHost"), slot("floatingActionButton"),
            PropertyDefinition("verticalArrangement", PropertyKind.ENUM, enumValues = setOf("Top", "Center", "Bottom", "SpaceAround", "SpaceBetween", "SpaceEvenly")),
            PropertyDefinition("horizontalAlignment", PropertyKind.ENUM, enumValues = setOf("Start", "Center", "End"))), true),
    ) }
}
