package me.next.serverdriven.core.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.next.serverdriven.compose.SDCLoaderLayout
import me.next.serverdriven.compose.produceUiState
import me.next.serverdriven.core.library.interfaces.Layout
import me.next.serverdriven.core.library.layout.components.fromNode
import me.next.serverdriven.core.tree.ServerDrivenNode

typealias ComponentHandler = @Composable (ServerDrivenNode, MutableMap<String, String>) -> Unit
typealias ActionHandler = suspend (ServerDrivenNode, MutableMap<String, String>) -> Unit

open class SDLibrary(val namespace: String = "") {
    private val components = HashMap<String, ComponentHandler>()
    private val actions = HashMap<String, ActionHandler>()

    fun addComponentLayout(
        name: String,
        load: suspend (ServerDrivenNode, MutableMap<String, String>) -> Layout
    ): SDLibrary {
        return addComponent(name) { node, state ->
            var modifierGen: Modifier? = null
            val uiState by produceUiState {
                modifierGen = Modifier.fromNode(node)
                load.invoke(node, state)
            }
            SDCLoaderLayout(
                modifier = modifierGen ?: Modifier,
                state = uiState
            ) {
                it.Content()
            }
        }
    }

    fun addComponent(name: String, handler: ComponentHandler): SDLibrary {
        components[name] = handler
        return this
    }

    fun getComponent(name: String): ComponentHandler? {
        return components[name]
    }

    fun addAction(name: String, action: ActionHandler): SDLibrary {
        actions[name] = action
        return this
    }

    fun getAction(
        name: String
    ): ActionHandler? {
        return actions[name]
    }

    fun merge(other: SDLibrary): SDLibrary {
        components.putAll(other.components)
        return this
    }
}