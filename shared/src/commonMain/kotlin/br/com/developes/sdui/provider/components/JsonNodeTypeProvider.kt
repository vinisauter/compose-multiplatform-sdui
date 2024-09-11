package br.com.developes.sdui.provider.components

import br.com.developes.sdui.ServerDrivenNode
import br.com.developes.sdui.provider.NodeTypeProvider
import br.com.developes.sdui.utils.toNode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class JsonNodeTypeProvider(json: String) : NodeTypeProvider {
    override val node: ServerDrivenNode
    init {
        node = Json.decodeFromString<JsonObject>(json).toNode()
    }
}