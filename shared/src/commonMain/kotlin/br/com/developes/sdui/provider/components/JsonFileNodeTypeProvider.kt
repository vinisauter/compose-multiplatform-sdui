package br.com.developes.sdui.provider.components

import br.com.developes.sdui.ServerDrivenNode
import br.com.developes.sdui.provider.NodeTypeProvider
import br.com.developes.sdui.resources.Res
import br.com.developes.sdui.utils.toNode
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * reads a JSON file from the specified resource, parses it into a [JsonObject],
 * and converts it into a [ServerDrivenNode] which represents the root of the server-driven UI.
 * This [ServerDrivenNode] is then accessible through the node property.
 * */
@OptIn(ExperimentalResourceApi::class)
class JsonFileNodeTypeProvider(res: String) : NodeTypeProvider {
    override val node: ServerDrivenNode

    init {
        val json = runBlocking {
            val bytes = Res.readBytes(res)
            bytes.decodeToString()
        }
        node = Json.decodeFromString<JsonObject>(json).toNode()
    }
}