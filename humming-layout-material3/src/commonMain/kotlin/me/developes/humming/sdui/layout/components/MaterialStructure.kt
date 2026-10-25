@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package me.developes.humming.sdui.layout.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.developes.humming.sdui.common.*

private val LocalRow = staticCompositionLocalOf<RowScope?> { null }
private val LocalSingleSegments = staticCompositionLocalOf<SingleChoiceSegmentedButtonRowScope?> { null }
private val LocalMultiSegments = staticCompositionLocalOf<MultiChoiceSegmentedButtonRowScope?> { null }
private data class TabSelection(val index: Int, val select: (Int) -> Unit)
private val LocalTabSelection = staticCompositionLocalOf<TabSelection?> { null }
private val LocalItemIndex = staticCompositionLocalOf { 0 }
private val LocalItemCount = staticCompositionLocalOf { 1 }

@Composable private fun SDCMaterial3.indexedChildren() {
    node.children.orEmpty().forEachIndexed { index, child ->
        CompositionLocalProvider(LocalItemIndex provides index, LocalItemCount provides node.children.orEmpty().size) {
            vm.loadComponent(child)?.Content()
        }
    }
}

@Composable internal fun SDCMaterial3.MaterialStructure(enabled: Boolean, click: () -> Unit, change: () -> Unit, invoker: ActionInvoker) {
    val m = modifier.observeNodeBounds(node)
    when (name) {
        "centerAlignedTopAppBar" -> CenterAlignedTopAppBar({ Text(bound("title")) }, m, navigationIcon = { slot("navigationIcon") }, actions = { slot("actions") })
        "mediumTopAppBar" -> MediumTopAppBar({ Text(bound("title")) }, m, navigationIcon = { slot("navigationIcon") }, actions = { slot("actions") })
        "largeTopAppBar" -> LargeTopAppBar({ Text(bound("title")) }, m, navigationIcon = { slot("navigationIcon") }, actions = { slot("actions") })
        "bottomAppBar" -> BottomAppBar(m) { CompositionLocalProvider(LocalRow provides this) { children() } }
        "navigationBar" -> NavigationBar(m) { CompositionLocalProvider(LocalRow provides this) { children() } }
        "navigationRail" -> NavigationRail(m) { children() }
        "navigationBarItem", "navigationRailItem", "navigationDrawerItem", "tab", "leadingIconTab" -> {
            val group = LocalTabSelection.current
            val index = LocalItemIndex.current
            val selectedValue = bound("value", "true")
            val selected = if (group != null) group.index == index else state("selected", "false").let { if (has("stateKey") || has("value")) it == selectedValue else it.toBoolean() }
            val select = { if (enabled) { group?.select?.invoke(index); if (group == null) write(selectedValue, "selected"); click() } }
            when (name) {
                "navigationBarItem" -> LocalRow.current?.run { NavigationBarItem(selected, select, { icon() }, m, enabled, label = { Text(bound("text")) }) }
                    ?: Text("MATERIAL_SCOPE: ${node.id} requires navigationBar | ${node.sourcePath} | material3.scope")
                "navigationRailItem" -> NavigationRailItem(selected, select, { icon() }, m, enabled, label = { Text(bound("text")) })
                "navigationDrawerItem" -> NavigationDrawerItem({ Text(bound("text")) }, selected, select, m, icon = optionalSlot("icon"), badge = optionalSlot("badge"))
                "leadingIconTab" -> LeadingIconTab(selected, select, { Text(bound("text")) }, { icon() }, m, enabled)
                else -> Tab(selected, select, m, enabled, text = { Text(bound("text")) }, icon = optionalSlot("icon"))
            }
        }
        "primaryTabRow", "secondaryTabRow", "tabRow", "primaryScrollableTabRow", "secondaryScrollableTabRow", "scrollableTabRow" -> {
            val count = node.children.orEmpty().size
            if (count > 0) {
                val index = state("selectedTabIndex", "0").toIntOrNull()?.coerceIn(0, count - 1) ?: 0
                CompositionLocalProvider(LocalTabSelection provides TabSelection(index) { write(it.toString(), "selectedTabIndex") }) {
                    when (name) {
                        "primaryTabRow" -> PrimaryTabRow(index, m) { indexedChildren() }
                        "secondaryTabRow" -> SecondaryTabRow(index, m) { indexedChildren() }
                        "tabRow" -> TabRow(index, m) { indexedChildren() }
                        "primaryScrollableTabRow" -> PrimaryScrollableTabRow(index, m) { indexedChildren() }
                        "secondaryScrollableTabRow" -> SecondaryScrollableTabRow(index, m) { indexedChildren() }
                        else -> ScrollableTabRow(index, m) { indexedChildren() }
                    }
                }
            }
        }
        "singleChoiceSegmentedButtonRow" -> SingleChoiceSegmentedButtonRow(m) { CompositionLocalProvider(LocalSingleSegments provides this) { indexedChildren() } }
        "multiChoiceSegmentedButtonRow" -> MultiChoiceSegmentedButtonRow(m) { CompositionLocalProvider(LocalMultiSegments provides this) { indexedChildren() } }
        "segmentedButton" -> {
            val shape = SegmentedButtonDefaults.itemShape(LocalItemIndex.current, LocalItemCount.current)
            val single = LocalSingleSegments.current; val multi = LocalMultiSegments.current
            if (single != null) {
                val selectedValue = bound("value", "true")
                val selected = state("selected", "false").let { if (has("stateKey") || has("value")) it == selectedValue else it.toBoolean() }
                single.SegmentedButton(selected, { write(selectedValue, "selected"); click() }, shape, m, enabled, label = { Text(bound("text")) })
            } else if (multi != null) {
                val checked = state("checked", "false").toBoolean()
                multi.SegmentedButton(checked, { write(it.toString(), "checked"); change() }, shape, m, enabled, label = { Text(bound("text")) })
            } else Text("MATERIAL_SCOPE: ${node.id} requires segmentedButtonRow | ${node.sourcePath} | material3.scope")
        }
        "modalNavigationDrawer", "dismissibleNavigationDrawer", "permanentNavigationDrawer" -> {
            val open = state("open", "false").toBoolean()
            val drawer = rememberDrawerState(if (open) DrawerValue.Open else DrawerValue.Closed)
            LaunchedEffect(open) { if (open) drawer.open() else drawer.close() }
            LaunchedEffect(drawer) { snapshotFlow { drawer.currentValue }.collect {
                val actual = it == DrawerValue.Open
                if (actual != vm.getState(raw("stateKey", "material3:${node.id}:open"), "false").toBoolean()) { write(actual.toString(), "open"); change() }
            } }
            when (name) {
                "modalNavigationDrawer" -> ModalNavigationDrawer({ slot("drawerContent") }, m, drawer, bound("gesturesEnabled", "true").toBoolean()) { children() }
                "dismissibleNavigationDrawer" -> DismissibleNavigationDrawer({ slot("drawerContent") }, m, drawer, bound("gesturesEnabled", "true").toBoolean()) { children() }
                else -> PermanentNavigationDrawer({ slot("drawerContent") }, m) { children() }
            }
        }
        "modalDrawerSheet" -> ModalDrawerSheet(m) { children() }
        "dismissibleDrawerSheet" -> DismissibleDrawerSheet(m) { children() }
        "permanentDrawerSheet" -> PermanentDrawerSheet(m) { children() }
        "bottomSheetScaffold" -> BottomSheetScaffold(sheetContent = { slot("sheetContent") }, modifier = m,
            sheetPeekHeight = number("sheetPeekHeight", 56f).dp, topBar = { slot("topBar") }) { padding -> Box(Modifier.padding(padding)) { children() } }
        else -> MaterialOverlay(enabled, click, change, invoker)
    }
}
