package me.next.serverdriven.core.method

import me.next.serverdriven.compose.SDCLibrary

fun registerBooleanMethods() {
    SDCLibrary.registerMethod("not") { node, map ->
        val param1 = node.propertyState("param1", map) ?: "false"
        (!param1.toBoolean()).toString()
    }
    SDCLibrary.registerMethod("and") { node, map ->
        val param1 = node.propertyState("param1", map) ?: "false"
        val param2 = node.propertyState("param2", map) ?: "false"
        (param1.toBoolean() and param2.toBoolean()).toString()
    }
    SDCLibrary.registerMethod("or") { node, map ->
        val param1 = node.propertyState("param1", map) ?: "false"
        val param2 = node.propertyState("param2", map) ?: "false"
        (param1.toBoolean() or param2.toBoolean()).toString()
    }
    SDCLibrary.registerMethod("xor") { node, map ->
        val param1 = node.propertyState("param1", map) ?: "false"
        val param2 = node.propertyState("param2", map) ?: "false"
        (param1.toBoolean() xor param2.toBoolean()).toString()
    }
    SDCLibrary.registerMethod("equals") { node, map ->
        val param1 = node.propertyState("param1", map)
        val param2 = node.propertyState("param2", map)
        param1.equals(param2).toString()
    }
}