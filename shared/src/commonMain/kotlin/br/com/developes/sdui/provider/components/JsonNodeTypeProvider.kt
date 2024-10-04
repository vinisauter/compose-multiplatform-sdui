package br.com.developes.sdui.provider.components

import br.com.developes.sdui.ServerDrivenNode
import br.com.developes.sdui.provider.NodeTypeProvider
import br.com.developes.sdui.utils.toNode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * takes a JSON string as input, parses it into a JSON object, and then transforms that object into a [ServerDrivenNode].
 * This [ServerDrivenNode] is then accessible through the [node] property.
 * */
class JsonNodeTypeProvider(json: String) : NodeTypeProvider {
    override val node: ServerDrivenNode
    init {
        node = Json.decodeFromString<JsonObject>(json).toNode()
    }
}