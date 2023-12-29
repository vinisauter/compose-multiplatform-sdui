package me.next.serverdriven.core.library.navigation

import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.next.serverdriven.compose.SDCLoaderLayout
import me.next.serverdriven.compose.produceUiState
import me.next.serverdriven.core.library.SDLibrary
import me.next.serverdriven.core.library.layout.components.fromNode
import me.next.serverdriven.core.library.navigation.components.LocalNavigator
import me.next.serverdriven.core.library.navigation.components.NavigationHost
import me.next.serverdriven.core.library.navigation.components.SDCNavigator

class SDNavigation : SDLibrary("navigation") {
    init {
        addComponent("graph") { graphNode, state ->
            var modifierGen: Modifier? = null
            val uiState by produceUiState {
                modifierGen = Modifier.fromNode(graphNode)
                SDCNavigator(graphNode)
            }
            SDCLoaderLayout(
                modifier = modifierGen ?: Modifier,
                state = uiState
            ) { navigator ->
                NavigationHost(navigator)
            }
        }
//        addComponent("graph") { graphNode, state ->
//            val scope = rememberCoroutineScope()
//            var navigator: SDCNavigator? = null
//            scope.launchHandling {
//                navigator = SDCNavigator(graphNode)
//            }
//            navigator?.let { NavigationHost(it) }
//        }
        addAction("goBack") { _, _ ->
            val navigator = LocalNavigator!!
            navigator.navigateBack()
        }
        addAction("goTo") { node, _ ->
            val navigator = LocalNavigator!!
            navigator.navigateTo(node.property("destiny")!!)
        }
    }
}