package me.next.serverdriven.core.tree

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.snapshots.StateFactoryMarker
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

interface ServerDrivenNode {
    val id: String
    val component: String
    val properties: MutableMap<String, Any?>?
    val children: MutableList<ServerDrivenNode>?
    fun propertyString(key: String): String? {
        return properties?.get(key)?.toString()
    }

    @StateFactoryMarker
    fun <T> propertyState(
        key: String,
        stateMap: MutableMap<String, String>,
        calculation: (String?) -> T,
    ): State<T> = derivedStateOf {
        val text = propertyState(key, stateMap)
        return@derivedStateOf calculation.invoke(text)
    }

    fun propertyState(
        key: String,
        stateMap: MutableMap<String, String>
    ): String? {
        var text = propertyString(key) ?: return null
        val keys = Regex("#\\{([^}]+)\\}").findAll(text)
        for (occurrence in keys) {
            val stateKey = occurrence.groupValues[1]
            val value = stateMap[stateKey] ?: run {
                stateMap[stateKey] = ""
                ""
            }
            text = text.replace(occurrence.value, value)
        }
        return text
    }

    fun propertyJson(key: String): JsonObject? {
        val json = propertyString(key) ?: return null
        return Json.decodeFromString<JsonObject>(json)
    }

    fun propertyNode(key: String): ServerDrivenNode? {
//        val json = propertyJson(key) ?: return null
//        return json.toNode()
        return null
    }
}