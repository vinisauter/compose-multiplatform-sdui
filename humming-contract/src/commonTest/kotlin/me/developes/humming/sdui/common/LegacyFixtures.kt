package me.developes.humming.sdui.common

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

internal object LegacyFixtures {
    val document = """
        {
          "component":"layout:column","id":"form",
          "properties":{"paddingAll":16,"enabled":true,"empty":null},
          "children":[
            {"component":"layout:textField","id":"name","properties":{
              "onChangeUpdateState":"name",
              "label":{"component":"layout:text","id":"label","properties":{"text":"Nome"}}
            }},
            {"component":"layout:button","id":"confirm","properties":{
              "onClick":[
                {"component":"action:update","id":"first","properties":{"state":"status","value":"ok"}},
                {"component":"action:method","id":"second","properties":{"invoke":"confirm"}}
              ]
            },"children":[{"component":"layout:text","id":"caption","properties":{"text":"Confirmar"}}]}
          ]
        }
    """.trimIndent()

    fun parse(source: String): ServerDrivenNode = Json.parseToJsonElement(source).jsonObject.toNode()
}
