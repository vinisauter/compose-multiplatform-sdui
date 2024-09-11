package br.com.developes.sdui.navigation.components

import androidx.compose.runtime.Composable
import br.com.developes.sdui.ServerDrivenNode
import br.com.developes.sdui.navigation.Screen
import br.com.developes.sdui.utils.StateLayout

class SDCNavigationScreen(
    val screenNode: ServerDrivenNode,
    val states: MutableMap<String, String>
) : Screen {

    @Composable
    override fun Content() {
        StateLayout(node = screenNode, stateMap = states)
    }
}