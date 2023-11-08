package me.next.serverdriven.core.library

import androidx.compose.runtime.Composable
import me.next.serverdriven.core.tree.ServerDrivenNode

typealias ComponentHandler = @Composable (ServerDrivenNode) -> Unit

open class SDLibrary(val namespace: String = "") {
    private val components = HashMap<String, ComponentHandler>()
    fun addComponent(name: String, handler: ComponentHandler): SDLibrary {
        components[name] = handler
        return this
    }

    fun getComponent(name: String): ComponentHandler? {
        return components[name]
    }

    fun merge(other: SDLibrary): SDLibrary {
        components.putAll(other.components)
        return this
    }
}