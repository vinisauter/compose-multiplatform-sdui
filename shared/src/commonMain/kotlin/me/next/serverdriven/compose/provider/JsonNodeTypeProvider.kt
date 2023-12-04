package me.next.serverdriven.compose.provider

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import me.next.serverdriven.core.library.interfaces.NodeTypeProvider
import me.next.serverdriven.core.tree.ServerDrivenNode
import me.next.serverdriven.utils.toNode

class JsonNodeTypeProvider(json: String) : NodeTypeProvider {
    override val node: ServerDrivenNode
    init {
        node = Json.decodeFromString<JsonObject>(json).toNode()
    }
}