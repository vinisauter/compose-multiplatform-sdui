package me.developes.humming.sdui.action

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import kotlinx.serialization.json.jsonPrimitive
import me.developes.humming.sdui.common.ComposableContent
import me.developes.humming.sdui.common.SDLibrary
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.StateEventToken
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.getPlatform

class SDActions : SDLibrary("action") {
    private val localMethods:
            HashMap<String, suspend (ServerDrivenNode, SDUIFormViewModel) -> Unit> = HashMap()

    private fun loadMethod(method: String): suspend (ServerDrivenNode, SDUIFormViewModel) -> Unit {
        return localMethods[method] ?: error("No MethodHandler for method: $method")
    }

    fun registerMethod(
        method: String,
        handler: suspend (ServerDrivenNode, SDUIFormViewModel) -> Unit
    ): SDActions {
        localMethods[method] = handler
        return this
    }

    init {
        ActionDefinitions.all.forEach { addDefinition(it) }
        registerMethod("getPlatformName") { node, states ->
            val stateName = node.property("state") ?: "platformName"
            states.updateStateFromAction(stateName, getPlatform().name)
        }
        addAction("method") { node, vm ->
            val properties = ActionDefinitions.method.parse(node).requireValid()
            properties.requireAllowedValue("invoke", localMethods.keys, "MISSING_METHOD")
            properties.property("invoke")?.run {
                loadMethod(this).invoke(node, vm)
            }
        }
        addAction("update") { node, vm ->
            val properties = ActionDefinitions.update.parse(node).requireValid()
            val stateName = properties.property("state")!!
            val valueName = properties.property("value")!!
            val delayMillis = properties.property("delay")?.toLong()
            delayMillis?.let { milliseconds -> delay(milliseconds) }
            vm.updateStateFromAction(stateName, valueName)
        }
        addAction("remove") { node, vm ->
            val statesNames = node.propertyJsonArray("state")!!
            for (stateName in statesNames) {
                stateName.jsonPrimitive.content.let { statesName ->
                    vm.updateStateFromAction(statesName, null)
                }
            }
        }
        // region Boolean Methods
        addAction("not") { node, vm ->
            val stateName = node.property("state")!!
            val param1 = vm.getState(stateName, node.property("param1")).toBoolean()
            vm.updateStateFromAction(stateName, (!param1).toString())
        }
        addAction("and") { node, vm ->
            val stateName = node.property("state")!!
            val param1 = vm.getState(stateName, node.property("param1")).toBoolean()
            val param2 = vm.getState(stateName, node.property("param2")).toBoolean()
            vm.updateStateFromAction(stateName, (param1 and param2).toString())
        }
        addAction("or") { node, vm ->
            val stateName = node.property("state")!!
            val param1 = vm.getState(stateName, node.property("param1")).toBoolean()
            val param2 = vm.getState(stateName, node.property("param2")).toBoolean()
            vm.updateStateFromAction(stateName, (param1 or param2).toString())
        }
        addAction("xor") { node, vm ->
            val stateName = node.property("state")!!
            val param1 = vm.getState(stateName, node.property("param1")).toBoolean()
            val param2 = vm.getState(stateName, node.property("param2")).toBoolean()
            vm.updateStateFromAction(stateName, (param1 xor param2).toString())
        }
        addAction("equals") { node, vm ->
            val stateName = node.property("state")!!
            val param1 = vm.getState(stateName, node.property("param1")).toBoolean()
            val param2 = vm.getState(stateName, node.property("param2")).toBoolean()
            vm.updateStateFromAction(stateName, (param1 == param2).toString())
        }
        addAction("greaterThan") { node, vm ->
            val stateName = node.property("state")!!
            val param1 =
                node.property("param1")?.let { vm.getState(it) }?.toDoubleOrNull() ?: 0.0
            val param2 =
                node.property("param2")?.let { vm.getState(it) }?.toDoubleOrNull() ?: 0.0
            vm.updateStateFromAction(stateName, (param1 > param2).toString())
        }
        addAction("lessThan") { node, vm ->
            val stateName = node.property("state")!!
            val param1 =
                node.property("param1")?.let { vm.getState(it) }?.toDoubleOrNull() ?: 0.0
            val param2 =
                node.property("param2")?.let { vm.getState(it) }?.toDoubleOrNull() ?: 0.0
            vm.updateStateFromAction(stateName, (param1 < param2).toString())
        }
        addAction("greaterThanOrEquals") { node, vm ->
            val stateName = node.property("state")!!
            val param1 =
                node.property("param1")?.let { vm.getState(it) }?.toDoubleOrNull() ?: 0.0
            val param2 =
                node.property("param2")?.let { vm.getState(it) }?.toDoubleOrNull() ?: 0.0
            vm.updateStateFromAction(stateName, (param1 >= param2).toString())
        }
        addAction("lessThanOrEquals") { node, vm ->
            val stateName = node.property("state")!!
            val param1 = node.property("param1")?.let {
                vm.getState(it)
            }?.toDoubleOrNull() ?: 0.0
            val param2 = node.property("param2")?.let {
                vm.getState(it)
            }?.toDoubleOrNull() ?: 0.0
            vm.updateStateFromAction(stateName, (param1 <= param2).toString())
        }
        addAction("isEmpty") { node, vm ->
            val stateName = node.property("state")!!
            val param1 = node.property("param1")?.let { vm.getState(it) } ?: ""
            vm.updateStateFromAction(stateName, (param1.isEmpty()).toString())
        }
        addAction("isNotEmpty") { node, vm ->
            val stateName = node.property("state")!!
            val param1 = node.property("param1")?.let { vm.getState(it) } ?: ""
            vm.updateStateFromAction(stateName, (param1.isNotEmpty()).toString())
        }
        // endregion
        addAction("then") { node, vm ->
            val condition =
                (node.property("condition")?.let { vm.getState(it) } ?: "false").toBoolean()
            val actions = node.propertyNodes("invoke")
            if (condition) {
                vm.executeActions(actions)
            }
        }
        addAction("ifState") { node, vm ->
            val properties = ActionDefinitions.ifState.parse(node).requireValid()
            val value = vm.getState(properties.property("state")!!, "false")
            val equals = vm.resolveStatePlaceholders(properties.property("equals")).ifEmpty { "true" }
            val condition: Boolean = equals.let { equalsValue ->
                value == equalsValue
            }
            if (condition) {
                node.propertyNodes("then").let { actions ->
                    if (actions.isNotEmpty()) {
                        vm.executeActions(actions)
                    }
                }
            } else {
                node.propertyNodes("else").let { actions ->
                    if (actions.isNotEmpty()) {
                        vm.executeActions(actions)
                    }
                }
            }
        }
        addComponent("event") { node, vm ->
            object : ComposableContent {
                val onCreateActions = node.propertyNodes("onCreate").ifEmpty { null }
                val onStateChangeActions = node.propertyNodes("onStateChange").ifEmpty { null }

                @Composable
                override fun Content() {
                    OnCreate()
                    OnStateChange()
                }

                @Composable
                fun OnCreate() {
                    LaunchedEffect(node.id, vm) {
                        onCreateActions?.let { vm.executeActions(it) }
                    }
                }

                @Composable
                fun OnStateChange() {
                    val listener = remember { StateEventToken() }
                    LaunchedEffect(node.id, vm) {
                        onStateChangeActions?.let { actions ->
                            vm.onStateUpdated.collect { revision ->
                                vm.executeStateChangeActions(listener, revision, actions)
                            }
                        }
                    }
                }
            }
        }
    }
}
