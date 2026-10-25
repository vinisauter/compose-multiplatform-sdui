package me.developes.humming.sdui.action

import me.developes.humming.sdui.common.*

/** Optional library schema; not installed by initialNodeCatalog. */
object NetworkDefinitions {
    val request = NodeDefinition("network:request", NodeRole.ACTION, listOf(
        PropertyDefinition("endpoint", required = true),
        PropertyDefinition("method", PropertyKind.ENUM, defaultValue = "GET", enumValues = setOf("GET", "POST")),
        PropertyDefinition("bodyJson", defaultDescription = "Literal serialized JSON, without binding interpolation"),
        PropertyDefinition("bodyState"),
        PropertyDefinition("onSuccess", PropertyKind.NODES, nodeRole = NodeRole.ACTION),
        PropertyDefinition("onFailure", PropertyKind.NODES, nodeRole = NodeRole.ACTION),
    ))
}
