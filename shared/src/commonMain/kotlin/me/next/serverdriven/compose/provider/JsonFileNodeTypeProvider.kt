package me.next.serverdriven.compose.provider

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import me.next.serverdriven.core.library.interfaces.NodeTypeProvider
import me.next.serverdriven.core.tree.ServerDrivenNode
import me.next.serverdriven.resources.Res
import me.next.serverdriven.utils.toNode
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