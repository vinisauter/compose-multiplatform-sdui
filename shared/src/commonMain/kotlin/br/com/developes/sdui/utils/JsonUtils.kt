package br.com.developes.sdui.utils

import br.com.developes.sdui.ServerDrivenNode
import br.com.developes.sdui.generateUUID
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

interface ServerDrivenJson {
    val json: JsonObject
}

class JsonServerDrivenNode(override val json: JsonObject) : ServerDrivenNode(
    id = json["id"]?.jsonPrimitive?.content
        ?: "${json["component"]?.jsonPrimitive?.content!!}:${generateUUID()}",
    component = json["component"]?.jsonPrimitive?.content!!,
    properties = json["properties"]?.let { properties ->
        transformJsonObjectToMapOfString(properties.jsonObject)
    },
    children = json["children"]?.jsonArray?.let { jsonArray ->
        mapValuesToMutableList(jsonArray) {
            it.jsonObject.toNode()
        }
    }
), ServerDrivenJson {
    override fun toString(): String {
        return json.toString()
    }
}

fun JsonObject.toNode(): ServerDrivenNode {
    this["component"]?.jsonPrimitive?.content!!
    return JsonServerDrivenNode(this)
}

fun transformJsonObjectToMapOfString(json: JsonObject): MutableMap<String, String?> {
    return mapValuesToMutableMap(json) {
        when (it.value) {
            is JsonArray -> it.value.jsonArray.toString()
            is JsonObject -> it.value.jsonObject.toString()
            else -> it.value.jsonPrimitive.content
        }
    }
}

/**
 * Transforms a JsonObject into a Kotlin Mutable Map recursively, i.e. this method will transform every JsonElement
 * inside the map into its Kotlin equivalent.
 *
 * @param json the JsonObject to be used as source to create the mutable map.
 * @return the mutable map equivalent to "json".
 */
fun transformJsonObjectToMap(json: JsonObject): MutableMap<String, Any?> {
    return mapValuesToMutableMap(json) { transformJsonElementToKotlinType(it.value) }
}

/**
 * Transforms a JsonArray into a Kotlin Mutable List recursively, i.e. this method will transform every JsonElement
 * inside the list into its Kotlin equivalent.
 *
 * @param json the JsonArray to be used as source to create the mutable list.
 * @return the mutable list equivalent to "json".
 */
fun transformJsonArrayToList(json: JsonArray): List<Any?> {
    return mapValuesToMutableList(json) { transformJsonElementToKotlinType(it) }
}

/**
 * Transforms a JsonPrimitive into its Kotlin equivalent.
 *
 * @param json the JsonPrimitive to be used as source to create the Kotlin primitive.
 * @return the Kotlin primitive equivalent to "json".
 */
fun transformJsonPrimitiveToPrimitive(json: JsonPrimitive): Any? {
    if (json.isString) return json.content
    return json.booleanOrNull ?: json.intOrNull ?: json.longOrNull ?: json.doubleOrNull
}

/**
 * Transforms a JsonElement into its Kotlin equivalent recursively, i.e. if "json" is a JsonArray or JsonMap, this
 * method will transform every JsonElement inside the array or map into its Kotlin equivalent.
 *
 * @param json the JsonElement to be used as source to create the Kotlin type.
 * @return a mutable map, a mutable list or a primitive type equivalent to "json".
 */
fun transformJsonElementToKotlinType(json: JsonElement): Any? {
    if (json is JsonObject) return transformJsonObjectToMap(json.jsonObject)
    if (json is JsonArray) return transformJsonArrayToList(json.jsonArray)
    return transformJsonPrimitiveToPrimitive(json.jsonPrimitive)
}


/**
 * Equivalent to the mapValues operation of a Map, but instead of creating a Map as a result, it creates a MutableMap.
 *
 * Note: this method is important for performance reasons, please don't replace it for less code, but higher cost.
 *
 * @param map the map to have its values mapped.
 * @param iteratee the function to map each value of "map". It receives the entry and must return the new value.
 * @return a mutable map with every value of "map" mapped to the result of "iteratee(entry)".
 */
fun <K, V, T> mapValuesToMutableMap(
    map: Map<K, V>,
    iteratee: (entry: Map.Entry<K, V>) -> T
): MutableMap<K, T> {
    val result = HashMap<K, T>()
    map.forEach { result[it.key] = iteratee(it) }
    return result
}

/**
 * Equivalent to the map operation of a List, but instead of creating a List as a result, it creates a MutableList.
 *
 * Note: this method is important for performance reasons, please don't replace it for less code, but higher cost.
 *
 * @param list the list to map.
 * @param iterate the function to map each item of "list".
 * @return a mutable list with every item of "list" mapped to the result of "iteratee(item)".
 */
fun <T, U> mapValuesToMutableList(list: List<T>, iterate: (item: T) -> U): MutableList<U> {
    val result = ArrayList<U>()
    list.forEach { result.add(iterate(it)) }
    return result
}

fun String.toColorInt(): Long {
    if (this[0] == '#') {
        var color = substring(1).toLong(16)
        if (length == 7) {
            color = color or 0x00000000ff000000L
        } else if (length != 9) {
            throw IllegalArgumentException("Unknown color")
        }
        return color
    }
    throw IllegalArgumentException("Unknown color")
}

val JsonElement.serverDrivenNode: ArrayList<ServerDrivenNode>
    get() = when (this) {
        is JsonArray -> {
            val nodes: ArrayList<ServerDrivenNode> = arrayListOf()
            for (jsonItem in this) {
                if (jsonItem is JsonObject) nodes.add(jsonItem.toNode())
            }
            nodes
        }

        is JsonObject -> {
            arrayListOf(this.toNode())
        }

        else -> {
            arrayListOf()
        }
    }
