package me.next.serverdriven.apollo

import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.next.serverdriven.utils.toJsonElement
import kotlin.jvm.JvmField


@JvmField
val JsonAdapter = object : Adapter<JsonElement> {
    override fun fromJson(
        reader: JsonReader,
        customScalarAdapters: CustomScalarAdapters
    ): JsonElement {
        if (reader.peek() == JsonReader.Token.NULL) {
            reader.nextNull()
            return JsonObject(mapOf())
        }
        return reader.nextString().toJsonElement()
    }

    override fun toJson(
        writer: JsonWriter,
        customScalarAdapters: CustomScalarAdapters,
        value: JsonElement
    ) {
        writer.value(value.toString())
    }
}