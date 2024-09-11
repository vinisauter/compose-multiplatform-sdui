package br.com.developes.sdui.action.method

import br.com.developes.sdui.action.SDAction

fun registerBooleanMethods(actionLib: SDAction) {
    actionLib.apply {
        registerMethod("not") { node, states ->
            val stateName = node.property("state")!!
            val param1 = node.propertyState("param1", states) ?: "false"
            states[stateName] = (!param1.toBoolean()).toString()
        }
        registerMethod("and") { node, states ->
            val stateName = node.property("state")!!
            val param1 = node.propertyState("param1", states) ?: "false"
            val param2 = node.propertyState("param2", states) ?: "false"
            states[stateName] = (param1.toBoolean() and param2.toBoolean()).toString()
        }
        registerMethod("or") { node, states ->
            val stateName = node.property("state")!!
            val param1 = node.propertyState("param1", states) ?: "false"
            val param2 = node.propertyState("param2", states) ?: "false"
            states[stateName] = (param1.toBoolean() or param2.toBoolean()).toString()
        }
        registerMethod("xor") { node, states ->
            val stateName = node.property("state")!!
            val param1 = node.propertyState("param1", states) ?: "false"
            val param2 = node.propertyState("param2", states) ?: "false"
            states[stateName] = (param1.toBoolean() xor param2.toBoolean()).toString()
        }
        registerMethod("equals") { node, states ->
            val stateName = node.property("state")!!
            val param1 = node.propertyState("param1", states)
            val param2 = node.propertyState("param2", states)
            states[stateName] = param1.equals(param2).toString()
        }
    }
}