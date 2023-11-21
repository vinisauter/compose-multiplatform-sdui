package me.next.serverdriven.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import io.github.aakira.napier.Napier
import me.next.serverdriven.core.library.ActionHandler
import me.next.serverdriven.core.library.ComponentHandler
import me.next.serverdriven.core.library.SDLayout
import me.next.serverdriven.core.library.SDLibrary
import me.next.serverdriven.core.tree.ServerDrivenNode

val logger = Napier

@Composable
fun SDCLibrary(
    vararg libraries: SDLibrary, block: @Composable (SDCLibrary) -> Unit
) {
    val defaultLibraries = listOf(
        SDLayout()
    )

    block.invoke(SDCLibrary.instance.apply {
        for (library in defaultLibraries) {
            addLibrary(library)
        }
        for (library in libraries) {
            addLibrary(library)
        }
    })
}

class SDCLibrary private constructor() {
    private val libraries: HashMap<String, SDLibrary> = HashMap()
    fun addLibrary(library: SDLibrary): SDCLibrary {
        if (libraries[library.namespace] == null) {
            libraries[library.namespace] = library
        } else {
            libraries[library.namespace]?.merge(library)
        }
        return this
    }

    fun getAction(
        nodeAction: String,
        className: String
    ): ActionHandler? {
        val split = nodeAction.split(':')
        val libraryNamespace = split[0]
        val componentNamespace = split[1]
        val library = libraries[libraryNamespace]
        return library?.getAction(componentNamespace, className)
    }

    fun getComponent(nodeComponent: String): ComponentHandler? {
        val split = nodeComponent.split(':')
        val libraryNamespace = split[0]
        val componentNamespace = split[1]
        val library = libraries[libraryNamespace]
        return library?.getComponent(componentNamespace)
    }

    companion object {
        private val LocalLib = staticCompositionLocalOf {
            SDCLibrary()
        }
        val instance: SDCLibrary
            @Composable
            get() = LocalLib.current
    }


}

@Composable
fun loadComponent(
    node: ServerDrivenNode,
    dataState: MutableMap<String, String>
) {
    val nodeComponent = node.component
    val component = SDCLibrary.instance.getComponent(nodeComponent)
    if (component != null) {
        component.invoke(node, dataState)
    } else {
        Text(
            text = "UNKNOWN SERVER DRIVEN COMPONENT\n${node.component}",
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .background(Color.Red)
                .fillMaxSize()
        )
    }
}

@Composable
inline fun <reified T> loadAction(node: ServerDrivenNode?):
        ActionHandler {
    val nodeComponent = node?.component ?: return { _, _ -> }
    val action = SDCLibrary.instance.getAction(nodeComponent, T::class.qualifiedName!!)
    if (action != null) {
        return action
    } else {
        Text(
            text = "UNKNOWN SERVER DRIVEN ACTION\n${node.component}",
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .background(Color.Red)
                .fillMaxSize()
        )
        return { _, _ -> }
    }
}


