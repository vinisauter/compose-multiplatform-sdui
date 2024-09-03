package me.next.serverdriven.core.library.action

import me.next.serverdriven.core.library.SDLibrary
import me.next.serverdriven.core.library.action.method.registerBooleanMethods
import me.next.serverdriven.core.library.action.method.registerNativeMethods
import me.next.serverdriven.core.tree.ServerDrivenNode

class SDAction : SDLibrary("action") {
    private val localMethods: HashMap<String,
                (ServerDrivenNode, MutableMap<String, String>) -> Unit> = HashMap()

    private fun loadMethod(method: String): (ServerDrivenNode, MutableMap<String, String>) -> Unit {
        return localMethods[method] ?: error("No MethodHandler for method: $method")
    }

    init {
        registerBooleanMethods(this)
        registerNativeMethods(this)

        addAction("update") { node, states ->
            val stateName = node.propertyState("state", states)!!
            node.propertyState("value", states)?.run {
                states[stateName] = this
            }
            node.property("method")?.run {
                loadMethod(this).invoke(node, states)
            }
        }
        addAction("method") { node, state ->
            node.property("method")?.run {
                loadMethod(this).invoke(node, state)
            }
        }
    }

    fun registerMethod(
        method: String,
        handler: (ServerDrivenNode, MutableMap<String, String>) -> Unit
    ) {
        localMethods[method] = handler
    }
}