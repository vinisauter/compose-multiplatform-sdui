package me.developes.humming.sdui.navigation

import androidx.compose.runtime.Composable
import me.developes.humming.sdui.common.SDLibrary
import me.developes.humming.sdui.navigation.components.Graph

class SDNavigation(
    private val preLoadView: (@Composable () -> Unit)? = null
) : SDLibrary("navigation") {
    init {
        addComponent("graph") { graphNode, viewModel ->
            val graph = Graph(graphNode, viewModel, preLoadView)
            addAction("goBack") { _, _ ->
                graph.navigateBack()
            }
            addAction("goTo") { node, _ ->
                val popStack = node.property("popStack")?.toBoolean() ?: false
                val clearStack = node.property("clearStack")?.toBoolean() ?: false
                graph.navigateTo(
                    routeName = node.property("destiny")!!,
                    popStack = popStack,
                    clearStack = clearStack
                )
            }
            graph
        }
    }
}
