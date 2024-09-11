@file:Suppress("unused", "MemberVisibilityCanBePrivate")
package br.com.developes.sdui
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.snapshots.StateFactoryMarker
import br.com.developes.sdui.utils.toNode
import br.com.developes.sdui.utils.transformJsonObjectToMapOfString
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

object IgnoredNode : ServerDrivenNode(id = "ignored", component = "ignored:ignored")

@Serializable
open class ServerDrivenNode(
    val component: String,
    val id: String = "$component:${generateUUID()}",
    val properties: MutableMap<String, String?>? = null,
    val children: MutableList<ServerDrivenNode>? = null
) {
    fun property(key: String): String? {
        return properties?.get(key)
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
        var text = property(key) ?: return null
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

    fun propertyJsonObject(key: String): JsonObject? {
        val json = property(key) ?: return null
        return Json.decodeFromString<JsonObject>(json)
    }

    fun propertyJsonArray(key: String): JsonArray? {
        val json = property(key) ?: return null
        return Json.decodeFromString<JsonArray>(json)
    }

    fun propertyNode(key: String): ServerDrivenNode? {
        val json = property(key) ?: return null
        return Json.decodeFromString<JsonObject>(json).toNode()
    }

    fun propertyNodes(key: String): ArrayList<ServerDrivenNode> {
        val json = property(key) ?: return arrayListOf()
        val nodes: ArrayList<ServerDrivenNode> = arrayListOf()
        if (json.startsWith("[")) {
            for (jsonItem in Json.decodeFromString<JsonArray>(json)) {
                if (jsonItem is JsonObject) nodes.add(jsonItem.toNode())
            }
        } else {
            nodes.add(Json.decodeFromString<JsonObject>(json).toNode())
        }
        return nodes
    }

    fun propertyMap(key: String): MutableMap<String, String?> {
        val json = propertyJsonObject(key) ?: return mutableMapOf()
        return transformJsonObjectToMapOfString(json)
    }
}