package me.next.serverdriven.core.library

import androidx.compose.runtime.Composable
import me.next.serverdriven.compose.provider.SDCStateLayout
import me.next.serverdriven.core.tree.ServerDrivenNode
import me.next.serverdriven.interfaces.Screen

class SDCNavigationScreen(
    val screenNode: ServerDrivenNode,
    val states: MutableMap<String, String>
) : Screen {

    @Composable
    override fun Content() {
        SDCStateLayout(node = screenNode, stateMap = states)
    }
}