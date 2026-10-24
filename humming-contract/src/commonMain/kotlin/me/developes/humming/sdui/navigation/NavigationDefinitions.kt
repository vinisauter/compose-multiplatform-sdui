package me.developes.humming.sdui.navigation

import me.developes.humming.sdui.common.InvalidPropertyPolicy
import me.developes.humming.sdui.common.ChildKeyDefinition
import me.developes.humming.sdui.common.NodeDefinition
import me.developes.humming.sdui.common.NodeRole
import me.developes.humming.sdui.common.PropertyDefinition
import me.developes.humming.sdui.common.PropertyKind

object NavigationDefinitions {
    val graph = NodeDefinition("navigation:graph", NodeRole.COMPONENT,
        listOf(PropertyDefinition("startDestination", required = true)), childrenRole = NodeRole.NAVIGATION_METADATA,
        childKey = ChildKeyDefinition("name", "startDestination"))
    val node = NodeDefinition("navigation:node", NodeRole.NAVIGATION_METADATA,
        listOf("name", "type", "destiny").map { PropertyDefinition(it, required = true) } +
            PropertyDefinition("deepLinks", PropertyKind.STRING_ARRAY,
                defaultDescription = "absent: empty; primitive entries use contentOrNull, null entries are ignored"))
    val goTo = NodeDefinition("navigation:goTo", NodeRole.ACTION,
        listOf(PropertyDefinition("destiny", required = true)) + listOf("popStack", "clearStack").map {
            PropertyDefinition(it, PropertyKind.BOOLEAN, defaultValue = "false", invalidPolicy = InvalidPropertyPolicy.LEGACY_BOOLEAN)
        })
    val goBack = NodeDefinition("navigation:goBack", NodeRole.ACTION)
    val all: List<NodeDefinition> = listOf(graph, node, goTo, goBack)
}
