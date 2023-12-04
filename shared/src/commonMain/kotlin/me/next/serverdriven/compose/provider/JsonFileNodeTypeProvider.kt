package me.next.serverdriven.compose.provider

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import me.next.serverdriven.core.tree.ServerDrivenNode
import me.next.serverdriven.interfaces.NodeTypeProvider
import me.next.serverdriven.utils.toNode
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource

@OptIn(ExperimentalResourceApi::class)
class JsonFileNodeTypeProvider(res: String) : NodeTypeProvider {
    override val node: ServerDrivenNode

    init {
        val json = runBlocking {
            val resource = resource(res)
            val bytes: ByteArray = resource.readBytes()
            bytes.decodeToString()
        }
        node = Json.decodeFromString<JsonObject>(json).toNode()
    }
}