package me.next.serverdriven.core.library.navigation

import me.next.serverdriven.core.library.SDLibrary
import me.next.serverdriven.core.library.navigation.components.LocalNavigator
import me.next.serverdriven.core.library.navigation.components.NavigationHost
import me.next.serverdriven.core.library.navigation.components.SDCNavigator

class SDNavigation : SDLibrary("navigation") {
    init {
        addComponent("graph") { graphNode, _ ->
            val navigator = SDCNavigator(graphNode)
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