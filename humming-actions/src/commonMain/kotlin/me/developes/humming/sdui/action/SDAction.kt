package me.developes.humming.sdui.action

import kotlinx.serialization.json.jsonPrimitive
import me.developes.humming.sdui.common.SDLibrary
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.getPlatform

class SDActions() : SDLibrary("action") {
    private val localMethods:
            HashMap<String, suspend (ServerDrivenNode, SDUIFormViewModel) -> Unit> = HashMap()

    private fun loadMethod(method: String): suspend (ServerDrivenNode, SDUIFormViewModel) -> Unit {
        return localMethods[method] ?: error("No MethodHandler for method: $method")
    }

    fun registerMethod(
        method: String,
        handler: suspend (ServerDrivenNode, SDUIFormViewModel) -> Unit
    ):SDActions {
        localMethods[method] = handler
        return this
    }

    init {
        registerMethod("getPlatformName") { node, states ->
            val stateName = node.property("state") ?: "platformName"
            states.updateState(stateName, getPlatform().name)
        }
        addAction("method") { node, vm ->
            node.property("invoke")?.run {
                loadMethod(this).invoke(node, vm)
            }
        }
        addAction("update") { node, vm ->
            val stateName = node.property("state")!!
            val valueName = node.property("value")!!
            vm.updateState(stateName, valueName)
        }
        addAction("remove") { node, vm ->
            val statesNames = node.propertyJsonArray("state")!!
            for (stateName in statesNames) {
                stateName.jsonPrimitive.content.let { statesName ->
                    vm.updateState(statesName, null)
                }
            }
        }
        // region Boolean Methods
        addAction("not") { node, vm ->
            val stateName = node.property("state")!!
            val param1 = node.property("param1")?.let { vm.propertyState(it).value } ?: "false"
            vm.updateState(stateName, (!param1.toBoolean()).toString())
        }
        addAction("and") { node, vm ->
            val stateName = node.property("state")!!
            val param1 = node.property("param1")?.let { vm.propertyState(it).value } ?: "false"
            val param2 = node.property("param2")?.let { vm.propertyState(it).value } ?: "false"
            vm.updateState(stateName, (param1.toBoolean() and param2.toBoolean()).toString())
        }
        addAction("or") { node, vm ->
            val stateName = node.property("state")!!
            val param1 = node.property("param1")?.let { vm.propertyState(it).value } ?: "false"
            val param2 = node.property("param2")?.let { vm.propertyState(it).value } ?: "false"
            vm.updateState(stateName, (param1.toBoolean() or param2.toBoolean()).toString())
        }
        addAction("xor") { node, vm ->
            val stateName = node.property("state")!!
            val param1: Boolean =
                (node.property("param1")?.let { vm.propertyState(it).value } ?: "false").toBoolean()
            val param2: Boolean =
                (node.property("param2")?.let { vm.propertyState(it).value } ?: "false").toBoolean()
            vm.updateState(stateName, (param1 xor param2).toString())
        }
        addAction("equals") { node, vm ->
            val stateName = node.property("state")!!
            val param1 = node.property("param1")?.let { vm.propertyState(it).value } ?: ""
            val param2 = node.property("param2")?.let { vm.propertyState(it).value } ?: ""
            vm.updateState(stateName, (param1 == param2).toString())
        }
        addAction("greaterThan") { node, vm ->
            val stateName = node.property("state")!!
            val param1 =
                node.property("param1")?.let { vm.propertyState(it).value }?.toDoubleOrNull() ?: 0.0
            val param2 =
                node.property("param2")?.let { vm.propertyState(it).value }?.toDoubleOrNull() ?: 0.0
            vm.updateState(stateName, (param1 > param2).toString())
        }
        addAction("lessThan") { node, vm ->
            val stateName = node.property("state")!!
            val param1 =
                node.property("param1")?.let { vm.propertyState(it).value }?.toDoubleOrNull() ?: 0.0
            val param2 =
                node.property("param2")?.let { vm.propertyState(it).value }?.toDoubleOrNull() ?: 0.0
            vm.updateState(stateName, (param1 < param2).toString())
        }
        addAction("greaterThanOrEquals") { node, vm ->
            val stateName = node.property("state")!!
            val param1 =
                node.property("param1")?.let { vm.propertyState(it).value }?.toDoubleOrNull() ?: 0.0
            val param2 =
                node.property("param2")?.let { vm.propertyState(it).value }?.toDoubleOrNull() ?: 0.0
            vm.updateState(stateName, (param1 >= param2).toString())
        }
        addAction("lessThanOrEquals") { node, vm ->
            val stateName = node.property("state")!!
            val param1 = node.property("param1")?.let {
                vm.propertyState(it).value
            }?.toDoubleOrNull() ?: 0.0
            val param2 = node.property("param2")?.let {
                vm.propertyState(it).value
            }?.toDoubleOrNull() ?: 0.0
            vm.updateState(stateName, (param1 <= param2).toString())
        }
        addAction("isEmpty") { node, vm ->
            val stateName = node.property("state")!!
            val param1 = node.property("param1")?.let { vm.propertyState(it).value } ?: ""
            vm.updateState(stateName, (param1.isEmpty()).toString())
        }
        addAction("isNotEmpty") { node, vm ->
            val stateName = node.property("state")!!
            val param1 = node.property("param1")?.let { vm.propertyState(it).value } ?: ""
            vm.updateState(stateName, (param1.isNotEmpty()).toString())
        }
        // endregion
        addAction("then") { node, vm ->
            val condition = node.property("condition")?.let { vm.propertyState(it).value } ?: "false"
            val actions = node.propertyNodes("invoke")
            if (condition.toBoolean()) {
                vm.invokeActions(actions)
            }
        }
    }
}