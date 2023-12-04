package me.next.serverdriven.core.library.navigation.components

import androidx.compose.runtime.Composable
import me.next.serverdriven.compose.SDCStateLayout
import me.next.serverdriven.core.library.interfaces.Screen
import me.next.serverdriven.core.tree.ServerDrivenNode

class SDCNavigationScreen(
    val screenNode: ServerDrivenNode,
    val states: MutableMap<String, String>
) : Screen {

    @Composable
    override fun Content() {
        SDCStateLayout(node = screenNode, stateMap = states)
    }
}