package me.next.serverdriven.utils

import generateUUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import me.next.serverdriven.core.tree.ServerDrivenNode

interface ServerDrivenJson {
    val json: JsonObject
}

@Serializable
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

internal fun JsonElement.toAny(): Any? = when (this) {
    is JsonObject -> this.mapValues { it.value.toAny() }
    is JsonArray -> this.map { it.toAny() }
    is JsonPrimitive -> {
        when {
            isString -> this.content
            this is JsonNull -> null
            else -> booleanOrNull ?: intOrNull ?: longOrNull ?: doubleOrNull
            ?: error("cannot decode $this")
        }
    }

    else -> error("cannot convert $this to Any")
}

inline fun <reified T> Any?.cast() = this as? T

fun Any?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is JsonElement -> this
    is Map<*, *> -> JsonObject(this.cast<Map<String, *>>()!!.mapValues { it.value.toJsonElement() })
    is Collection<*> -> JsonArray(map { it.toJsonElement() })
    is Array<*> -> JsonArray(map { it.toJsonElement() })
    is ByteArray -> JsonArray(map { JsonPrimitive(it) })
    is CharArray -> JsonPrimitive(this.toString())
    is ShortArray -> JsonArray(map { JsonPrimitive(it) })
    is IntArray -> JsonArray(map { JsonPrimitive(it) })
    is LongArray -> JsonArray(map { JsonPrimitive(it) })
    is FloatArray -> JsonArray(map { JsonPrimitive(it) })
    is DoubleArray -> JsonArray(map { JsonPrimitive(it) })
    is BooleanArray -> JsonArray(map { JsonPrimitive(it) })
    is Boolean -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is String -> JsonPrimitive(this)
    is Enum<*> -> JsonPrimitive(this.toString())
    is JsonServerDrivenNode -> json
    else -> throw IllegalStateException("Can't serialize unknown type: ${this::class} - $this")
}