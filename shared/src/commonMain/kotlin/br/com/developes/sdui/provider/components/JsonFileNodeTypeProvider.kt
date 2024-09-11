package br.com.developes.sdui.provider.components

import br.com.developes.sdui.ServerDrivenNode
import br.com.developes.sdui.provider.NodeTypeProvider
import br.com.developes.sdui.resources.Res
import br.com.developes.sdui.utils.toNode
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.compose.resources.ExperimentalResourceApi

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