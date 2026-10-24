package me.developes.humming.sdui.common

import me.developes.humming.sdui.action.ActionDefinitions
import me.developes.humming.sdui.layout.LayoutDefinitions
import me.developes.humming.sdui.navigation.NavigationDefinitions

/** Schema inventory only. It never installs runtime handlers, providers or side effects. */
fun initialNodeCatalog(): NodeCatalog =
    NodeCatalog(LayoutDefinitions.all + ActionDefinitions.all + NavigationDefinitions.all)
