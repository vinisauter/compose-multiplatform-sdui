package br.com.developes.sdui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import br.com.developes.sdui.SDCLibrary
import br.com.developes.sdui.ServerDrivenNode
import br.com.developes.sdui.show_states

@Composable
fun StateLayout(
    node: ServerDrivenNode,
    modifier: Modifier = Modifier,
    stateMap: MutableMap<String, String> = remember { mutableStateMapOf() }
) {
    Column(Modifier.fillMaxSize()) {
        if (show_states) {
            Column(
                modifier = Modifier.background(color = Color.Yellow)
            ) {
                for (entry in stateMap) {
                    Text(
                        modifier = Modifier,
                        text = "key: ${entry.key}, value: ${entry.value}"
                    )
                }
            }
        }
        Column(modifier = modifier) {
//      TODO: verify derivedStateOf for node.properties
//        https://medium.com/androiddevelopers/jetpack-compose-when-should-i-use-derivedstateof-63ce7954c11b
            SDCLibrary.loadComponent(node = node, dataState = stateMap)
        }
    }
}