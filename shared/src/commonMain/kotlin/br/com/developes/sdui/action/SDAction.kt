package br.com.developes.sdui.action

import br.com.developes.sdui.SDLibrary
import br.com.developes.sdui.ServerDrivenNode
import br.com.developes.sdui.action.method.registerBooleanMethods

class SDAction : SDLibrary("action") {
    private val localMethods: HashMap<String,
            suspend (ServerDrivenNode, MutableMap<String, String>) -> Unit> = HashMap()

    private fun loadMethod(method: String): suspend (ServerDrivenNode, MutableMap<String, String>) -> Unit {
        return localMethods[method] ?: error("No MethodHandler for method: $method")
    }

    init {
        registerBooleanMethods(this)
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
        handler: suspend (ServerDrivenNode, MutableMap<String, String>) -> Unit
    ) {
        localMethods[method] = handler
    }
}