package me.next.serverdriven.apollo

import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonNumber
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import com.apollographql.apollo3.api.json.writeArray
import com.apollographql.apollo3.api.json.writeObject
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.next.serverdriven.utils.ServerDrivenJson
import kotlin.jvm.JvmField

@JvmField
val CustomObjectAdapter = object : Adapter<Any> {

    override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): Any {
        return reader.readAny()
    }

    override fun toJson(
        writer: JsonWriter,
        customScalarAdapters: CustomScalarAdapters,
        value: Any
    ) {
        writer.writeAny(value)
    }


    fun JsonReader.readAny(): JsonElement {
        return when (val token = peek()) {
            JsonReader.Token.NULL -> JsonPrimitive(nextNull())
            JsonReader.Token.BOOLEAN -> JsonPrimitive(nextBoolean())
            JsonReader.Token.LONG, JsonReader.Token.NUMBER -> {
                try {
                    return JsonPrimitive(nextInt())
                } catch (_: Exception) {
                }
                try {
                    return JsonPrimitive(nextLong())
                } catch (_: Exception) {
                }
                try {
                    return JsonPrimitive(nextDouble())
                } catch (_: Exception) {
                }
                return JsonPrimitive(nextNumber().value)
            }

            JsonReader.Token.STRING -> JsonPrimitive(nextString())
            JsonReader.Token.BEGIN_OBJECT -> {
                beginObject()
                val result = mutableMapOf<String, JsonElement>()
                while (hasNext()) {
                    result[nextName()] = readAny()
                }
                endObject()
                JsonObject(result)
            }

            JsonReader.Token.BEGIN_ARRAY -> {
                beginArray()
                val result = mutableListOf<JsonElement>()
                while (hasNext()) {
                    result.add(readAny())
                }
                endArray()
                JsonArray(result)
            }

            else -> error("unknown token $token")
        }
    }

    fun JsonWriter.writeAny(value: Any?) {
        when (value) {
            null -> nullValue()
            is Map<*, *> -> {
                writeObject {
                    value.forEach { (key, value) ->
                        name(key.toString())
                        writeAny(value)
                    }
                }
            }

            is List<*> -> {
                writeArray {
                    value.forEach {
                        writeAny(it)
                    }
                }
            }

            is Boolean -> value(value)
            is Int -> value(value)
            is Long -> value(value)
            is Double -> value(value)
            is JsonNumber -> value(value)
            is String -> value(value)
            is JsonPrimitive -> value(value.content)
            is ServerDrivenJson -> writeAny(value.json)
            else -> error("Cannot write ${value::class} - $value to Json")
        }
    }
}