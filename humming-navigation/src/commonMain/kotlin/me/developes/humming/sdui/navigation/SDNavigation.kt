package me.developes.humming.sdui.navigation

import androidx.compose.runtime.Composable
import me.developes.humming.sdui.common.SDLibrary
import me.developes.humming.sdui.navigation.components.Graph

class SDNavigation(
    private val preLoadView: (@Composable () -> Unit)? = null
) : SDLibrary("navigation") {
    init {
        NavigationDefinitions.all.forEach { addDefinition(it) }
        addComponent("graph") { graphNode, viewModel ->
            val graph = Graph(graphNode, viewModel, preLoadView)
            addAction("goBack") { _, _ ->
                graph.navigateBack()
            }
            addAction("goTo") { node, owner ->
                val properties = NavigationDefinitions.goTo.parse(node).requireValid()
                val popStack = properties.property("popStack")?.toBoolean() ?: false
                val clearStack = properties.property("clearStack")?.toBoolean() ?: false
                graph.navigateToAwaited(
                    routeName = properties.property("destiny")!!,
                    popStack = popStack,
                    clearStack = clearStack,
                    owner = owner,
                )
            }
            graph
        }
    }
}
