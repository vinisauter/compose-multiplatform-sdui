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
    val id: String = "$component:${generateUUID()}",
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
     * then uses a provided calculation function to transform that string into a state value of the desired type.
     *
     * @see [StateFactoryMarker]: This annotation indicates that the function is a "state factory". It's used internally by Compose to improve the performance of state management.
     *
     * @param key: [String]: This parameter represents a unique key used to identify the state within the stateMap.
     * @param stateMap: [MutableMap]<[String], [String]>: This parameter is a mutable map that stores the state values as strings. The keys in this map correspond to the key parameter.
     * @param calculation: ([String]?) -> [T]: This parameter is a lambda function that takes a nullable string as input and returns a value of type [T]. This function is used to compute the state value based on the string retrieved from the stateMap.
     * @see derivedStateOf { ... }: This creates a derived state. Derived state means that this state's value is calculated based on other state values. In this case, it depends on the value returned by propertyState(key, stateMap).
     *
     * @property text = propertyState(key, stateMap): This line seems to be calling a different overload of the propertyState function. This overload likely retrieves the string value associated with the given key from the stateMap.
     * @return [derivedStateOf] calculation.invoke(text): This line calculates the final state value by invoking the calculation lambda function with the text retrieved from the stateMap.
     * **/
    @StateFactoryMarker
    fun <T> propertyState(
        key: String,
        stateMap: MutableMap<String, String>,
        calculation: (String?) -> T,
    ): State<T> = derivedStateOf {
        val text = propertyState(key, stateMap)
        return@derivedStateOf calculation.invoke(text)
    }

    /**
     * takes a text string, identifies placeholders within it, and replaces those placeholders with values from a state map.
     * If a placeholder's key is not found in the map, it adds the key with an empty value.
     * */
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
        return Json.decodeFromString<JsonArray>(json)
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