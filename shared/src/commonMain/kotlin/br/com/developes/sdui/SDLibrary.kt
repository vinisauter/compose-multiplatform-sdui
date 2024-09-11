package br.com.developes.sdui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import br.com.developes.sdui.layout.Layout
import br.com.developes.sdui.layout.components.fromNode
import br.com.developes.sdui.utils.LoaderLayout
import br.com.developes.sdui.utils.produceUiState

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
            LoaderLayout(
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