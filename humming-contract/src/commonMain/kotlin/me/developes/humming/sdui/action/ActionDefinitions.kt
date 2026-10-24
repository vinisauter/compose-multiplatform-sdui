package me.developes.humming.sdui.action

import me.developes.humming.sdui.common.NodeDefinition
import me.developes.humming.sdui.common.NodeRole
import me.developes.humming.sdui.common.PropertyDefinition
import me.developes.humming.sdui.common.PropertyKind

object ActionDefinitions {
    private fun text(name: String, required: Boolean = false, binding: Boolean = false, default: String? = null) =
        PropertyDefinition(name, required = required, allowsBinding = binding, defaultValue = default)
    private fun actions(name: String) = PropertyDefinition(name, PropertyKind.NODES, nodeRole = NodeRole.ACTION)
    val update = NodeDefinition("action:update", NodeRole.ACTION, listOf(text("state", true), text("value", true),
        PropertyDefinition("delay", PropertyKind.LONG, defaultDescription = "absent or non-positive: no delay")))
    // Missing invoke is a no-op in the legacy handler, not an implicit method name.
    val method = NodeDefinition("action:method", NodeRole.ACTION, listOf(text("invoke")),
        // Custom methods may consume extra properties; legacy mode retains them.
    )
    val ifState = NodeDefinition("action:ifState", NodeRole.ACTION,
        listOf(text("state", true), text("equals", binding = true, default = "true"), actions("then"), actions("else")))
    val then = NodeDefinition("action:then", NodeRole.ACTION,
        listOf(text("condition"), actions("invoke")))
    val event = NodeDefinition("action:event", NodeRole.COMPONENT,
        listOf(actions("onCreate"), actions("onStateChange")))
    val all: List<NodeDefinition> = listOf(update, method, ifState, then, event)
}
