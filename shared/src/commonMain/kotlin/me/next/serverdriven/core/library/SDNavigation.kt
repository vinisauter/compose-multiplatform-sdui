package me.next.serverdriven.core.library

import me.next.serverdriven.compose.navigation.LocalNavigator
import me.next.serverdriven.compose.navigation.NavigationHost
import me.next.serverdriven.compose.navigation.Navigator

class SDNavigation : SDLibrary("navigation") {
    init {
        addComponent("graph") { graphNode, _ ->
            val navigator = Navigator(graphNode)
            NavigationHost(navigator)
        }
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