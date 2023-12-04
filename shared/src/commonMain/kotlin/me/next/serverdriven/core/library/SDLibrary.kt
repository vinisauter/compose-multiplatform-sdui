package me.next.serverdriven.core.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.next.serverdriven.compose.produceUiState
import me.next.serverdriven.compose.provider.SDCLoaderLayout
import me.next.serverdriven.core.tree.ServerDrivenNode

typealias ComponentHandler = @Composable (ServerDrivenNode, MutableMap<String, String>) -> Unit

typealias ActionHandler = (ServerDrivenNode, MutableMap<String, String>) -> Unit

open class SDLibrary(val namespace: String = "") {
    private val components = HashMap<String, ComponentHandler>()
    private val actions = HashMap<String, ActionHandler>()

    fun addComponent(name: String, component: SDComponent): SDLibrary {
        components[name] = component.handler
        return this
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

interface SDComponent {
    val handler: ComponentHandler
}

open class SDComponentLoader<T>(
    private val load: suspend (ServerDrivenNode, MutableMap<String, String>) -> T,
    private val loading: @Composable (Modifier) -> Unit = defaultLoading,
    private val error: @Composable (modifier: Modifier, throwable: Throwable) -> Unit = defaultError,
    private val into: @Composable (T) -> Unit,
) : SDComponent {
    override val handler: ComponentHandler
        get() = { node, dataState ->
            val uiState by produceUiState {
                load.invoke(node, dataState)
            }
            SDCLoaderLayout(state = uiState, loading = loading, error = error) {
                into.invoke(it)
            }
        }
}