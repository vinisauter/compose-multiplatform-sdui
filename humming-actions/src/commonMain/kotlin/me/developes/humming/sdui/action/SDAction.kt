package me.developes.humming.sdui.action

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import kotlinx.serialization.json.jsonPrimitive
import me.developes.humming.sdui.common.ComposableContent
import me.developes.humming.sdui.common.SDLibrary
import me.developes.humming.sdui.common.SDUIFormViewModel
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
            val delayMillis = node.property("delay")?.toLong()
            delayMillis?.let { milliseconds -> delay(milliseconds) }
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
            val param1 = vm.getState(stateName, node.property("param1")).toBoolean()
            vm.updateState(stateName, (!param1).toString())
        }
        addAction("and") { node, vm ->
            val stateName = node.property("state")!!
            val param1 = vm.getState(stateName, node.property("param1")).toBoolean()
            val param2 = vm.getState(stateName, node.property("param2")).toBoolean()
            vm.updateState(stateName, (param1 and param2).toString())
        }
        addAction("or") { node, vm ->
            val stateName = node.property("state")!!
            val param1 = vm.getState(stateName, node.property("param1")).toBoolean()
            val param2 = vm.getState(stateName, node.property("param2")).toBoolean()
            vm.updateState(stateName, (param1 or param2).toString())
        }
        addAction("xor") { node, vm ->
            val stateName = node.property("state")!!
            val param1 = vm.getState(stateName, node.property("param1")).toBoolean()
            val param2 = vm.getState(stateName, node.property("param2")).toBoolean()
            vm.updateState(stateName, (param1 xor param2).toString())
        }
        addAction("equals") { node, vm ->
            val stateName = node.property("state")!!
            val param1 = vm.getState(stateName, node.property("param1")).toBoolean()
            val param2 = vm.getState(stateName, node.property("param2")).toBoolean()
            vm.updateState(stateName, (param1 == param2).toString())
        }
        addAction("greaterThan") { node, vm ->
            val stateName = node.property("state")!!
            val param1 =
                node.property("param1")?.let { vm.getState(it) }?.toDoubleOrNull() ?: 0.0
            val param2 =
                node.property("param2")?.let { vm.getState(it) }?.toDoubleOrNull() ?: 0.0
            vm.updateState(stateName, (param1 > param2).toString())
        }
        addAction("lessThan") { node, vm ->
            val stateName = node.property("state")!!
            val param1 =
                node.property("param1")?.let { vm.getState(it) }?.toDoubleOrNull() ?: 0.0
            val param2 =
                node.property("param2")?.let { vm.getState(it) }?.toDoubleOrNull() ?: 0.0
            vm.updateState(stateName, (param1 < param2).toString())
        }
        addAction("greaterThanOrEquals") { node, vm ->
            val stateName = node.property("state")!!
            val param1 =
                node.property("param1")?.let { vm.getState(it) }?.toDoubleOrNull() ?: 0.0
            val param2 =
                node.property("param2")?.let { vm.getState(it) }?.toDoubleOrNull() ?: 0.0
            vm.updateState(stateName, (param1 >= param2).toString())
        }
        addAction("lessThanOrEquals") { node, vm ->
            val stateName = node.property("state")!!
            val param1 = node.property("param1")?.let {
                vm.getState(it)
            }?.toDoubleOrNull() ?: 0.0
            val param2 = node.property("param2")?.let {
                vm.getState(it)
            }?.toDoubleOrNull() ?: 0.0
            vm.updateState(stateName, (param1 <= param2).toString())
        }
        addAction("isEmpty") { node, vm ->
            val stateName = node.property("state")!!
            val param1 = node.property("param1")?.let { vm.getState(it) } ?: ""
            vm.updateState(stateName, (param1.isEmpty()).toString())
        }
        addAction("isNotEmpty") { node, vm ->
            val stateName = node.property("state")!!
            val param1 = node.property("param1")?.let { vm.getState(it) } ?: ""
            vm.updateState(stateName, (param1.isNotEmpty()).toString())
        }
        // endregion
        addAction("then") { node, vm ->
            val condition =
                (node.property("condition")?.let { vm.getState(it) } ?: "false").toBoolean()
            val actions = node.propertyNodes("invoke")
            if (condition) {
                vm.invokeActions(actions)
            }
        }
        addAction("ifState") { node, vm ->
            val value = vm.getState(node.property("state")!!, "false")
            val equals = vm.resolveStatePlaceholders(node.property("equals")).ifEmpty { "true" }
            val condition: Boolean = equals.let { equalsValue ->
                value == equalsValue
            }
            if (condition) {
                node.propertyNodes("then").let { actions ->
                    if (actions.isNotEmpty()) {
                        vm.invokeActions(actions)
                    }
                }
            } else {
                node.propertyNodes("else").let { actions ->
                    if (actions.isNotEmpty()) {
                        vm.invokeActions(actions)
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
                    val invoked = remember { mutableStateOf(false) }
                    if (!invoked.value) {
                        invoked.value = true
                        onCreateActions?.let { actions ->
                            if (actions.isNotEmpty()) {
                                vm.invokeActions(actions)
                            }
                        }
                    }
                }

                @Composable
                fun OnStateChange() {
                    val states by vm.onStateUpdated.collectAsState()
                    LaunchedEffect(states) {
                        onStateChangeActions?.let { actions ->
                            if (actions.isNotEmpty()) {
                                vm.invokeActions(actions)
                            }
                        }
                    }
                }
            }
        }
    }
}