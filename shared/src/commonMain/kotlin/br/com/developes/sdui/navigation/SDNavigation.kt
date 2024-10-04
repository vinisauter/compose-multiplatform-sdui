package br.com.developes.sdui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import br.com.developes.sdui.SDLibrary
import br.com.developes.sdui.layout.components.fromNode
import br.com.developes.sdui.navigation.components.LocalNavigator
import br.com.developes.sdui.navigation.components.NavigationHost
import br.com.developes.sdui.navigation.components.SDCNavigator
import br.com.developes.sdui.utils.LoaderLayout
import br.com.developes.sdui.utils.produceUiState

class SDNavigation : SDLibrary("navigation") {
    init {
        addComponent("graph") { graphNode, state ->
            var modifierGen: Modifier? = null
            val uiState by produceUiState {
                modifierGen = Modifier.fromNode(graphNode)
                SDCNavigator(graphNode)
            }
            LoaderLayout(
                modifier = modifierGen ?: Modifier,
                state = uiState
            ) { navigator ->
                NavigationHost(navigator)
            }
        }
        addAction("goBack") { _, _ ->
            val navigator = LocalNavigator!!
            navigator.navigateBack()
        }
        addAction("goTo") { node, _ ->
            val navigator = LocalNavigator!!
            val popStack = node.property("popStack")?.let { it.toBoolean() } ?: false
            navigator.navigateTo(routeName = node.property("destiny")!!, popStack = popStack)
        }
        addAction("goToUrl") { node, _ ->
            val navigator = LocalNavigator!!
            navigator.handleLink(node.property("url")!!)
        }
    }
}