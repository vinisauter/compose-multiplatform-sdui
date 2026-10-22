@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package me.developes.humming.sdui.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import me.developes.humming.sdui.getPlatform

/**
 * fundamental building block for a server-driven UI system.
 *
 * @property component: [String] A string identifier for the type of UI component this node represents (e.g., "button", "text", "image").
 * @property id: [String] A unique identifier for the node. If not provided, it's generated using the component name and a UUID.
 * @property properties: [MutableMap]<[String], [String]?>? A map to store arbitrary key-value pairs associated with the node (e.g., "text" -> "Hello", "color" -> "red").
 * @property children: [MutableList]<[ServerDrivenNode]>? A list of child nodes, allowing for hierarchical UI structures.
 * */
@Serializable
open class ServerDrivenNode(
    val component: String,
    val id: String = "$component:${getPlatform().generateUUID()}",
    val properties: MutableMap<String, String?>? = null,
    val children: MutableList<ServerDrivenNode>? = null
) {

    /**
     * provides a safe way to access properties from a Properties object,
     * returning null if either the properties object itself or the value for the given key is not found.
     * */
    fun property(key: String): String? {
        return properties?.get(key)
    }

    /**
     * retrieves a string value from a map based on a provided key,
     * then uses a provided calculation function to transform that string into a value of the desired type.
     *
     * @param key: [String]: This parameter represents a unique key used to identify the property within the properties map.
     * @param calculation: ([String]?) -> [T]: This parameter is a lambda function that takes a nullable string as input and returns a value of type [T]. This function is used to compute the final value based on the string retrieved from the properties map.
     *
     * @return [T] This line calculates the final value by invoking the calculation lambda function with the text retrieved from the properties map.
     * **/
    fun <T> property(
        key: String,
        calculation: (String?) -> T
    ): T {
        return calculation(properties?.get(key))
    }

    /**
     * tries to get a JSON string from a property. If successful, it deserializes the string into a JsonObject and returns it.
     * If it fails to get the JSON string, it returns null.
     * */
    fun propertyJsonObject(key: String): JsonObject? {
        val json = property(key) ?: return null
        return Json.decodeFromString<JsonObject>(json)
    }

    /**
     * tries to find a property associated with a given key. If found, it parses the property value as a JSON array and returns it.
     * If the property is not found, it returns null.
     * */
    fun propertyJsonArray(key: String): JsonArray? {
        val json = property(key) ?: return null
        return try {
            Json.decodeFromString<JsonArray>(json)
        } catch (e: Exception) {
            val jsonObject = Json.decodeFromString<JsonObject>(json)
            JsonArray(listOf(jsonObject))
        }
    }

    /**
     * retrieves a JSON string associated with a given key, deserializes it into a JSON object,
     * and then transforms that object into a ServerDrivenNode which is presumably used to construct a UI element.
     * */
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