package me.developes.humming.builder.ui

import kotlinx.serialization.json.*

internal fun catalogActionSource(name: String): String {
    var sequence = 0
    fun action(type: String, properties: JsonObject) = buildJsonObject {
        put("component", "action:$type"); put("id", "catalog-action-$name-${sequence++}"); put("properties", properties)
    }
    fun update(key: String, value: String) = action("update", buildJsonObject { put("state", key); put("value", value) })
    val command = when (name) {
        "method" -> action("method", buildJsonObject { put("invoke", "getPlatformName") })
        "update" -> update("result", "Atualizado pelo handler")
        "remove" -> action("remove", buildJsonObject { put("state", JsonArray(listOf(JsonPrimitive("temporary")))) })
        "ifState" -> action("ifState", buildJsonObject { put("state", "flag"); put("equals", "true")
            put("then", update("result", "Ramo verdadeiro")); put("else", update("result", "Ramo falso")) })
        "then" -> action("then", buildJsonObject { put("condition", "flag"); put("invoke", update("result", "Ramo aguardado")) })
        else -> action(name, buildJsonObject {
            put("state", if (name in setOf("not", "and", "or", "xor", "equals")) "flag" else "result")
            put("param1", if (name in setOf("isEmpty", "isNotEmpty")) "temporary" else if (name in setOf("not", "and", "or", "xor", "equals")) "true" else "left")
            put("param2", if (name in setOf("not", "and", "or", "xor", "equals")) "false" else "right")
        })
    }
    val event = buildJsonObject { put("component", "action:event"); put("id", "catalog-action-$name-init")
        put("properties", buildJsonObject { put("onCreate", JsonArray(listOf(update("flag", "true"), update("temporary", "Presente"), update("left", "7"), update("right", "3")))) }) }
    fun text(id: String, value: String) = buildJsonObject { put("component", "layout:text"); put("id", "catalog-action-$name-$id"); put("properties", buildJsonObject { put("text", value) }) }
    val button = buildJsonObject { put("component", "layout:button"); put("id", "catalog-action-$name-run")
        put("properties", buildJsonObject { put("onClick", command) }); put("children", JsonArray(listOf(text("label", "Executar $name")))) }
    return buildJsonObject { put("component", "layout:column"); put("id", "catalog-action-$name-root")
        put("children", JsonArray(listOf(event, button, text("result", "Resultado: #{result}"), text("flag", "Flag: #{flag}"),
            text("temporary", "Temporário: #{temporary}"), text("platform", "Plataforma: #{platformName}"))))
    }.toString()
}
