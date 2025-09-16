package br.com.developes.sdui.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.developes.sdui.SDCLibrary
import br.com.developes.sdui.ServerDrivenNode
import br.com.developes.sdui.resources.Res
import br.com.developes.sdui.resources.ic_eye
import br.com.developes.sdui.show_states
import br.com.developes.sdui.states_visibility
import org.jetbrains.compose.resources.painterResource

@Composable
fun StateLayout(
    node: ServerDrivenNode,
    modifier: Modifier = Modifier,
    stateMap: MutableMap<String, String> = remember { mutableStateMapOf() }
) {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Column(modifier = modifier) {
//      TODO: verify derivedStateOf for node.properties
//        https://medium.com/androiddevelopers/jetpack-compose-when-should-i-use-derivedstateof-63ce7954c11b
                SDCLibrary.loadComponent(node = node, dataState = stateMap)
            }
        }
        if (show_states) {
            Column(
                modifier = Modifier.background(color = Color.Yellow)
                    .align(Alignment.TopEnd)
            ) {
                var statesVisibility by remember { mutableStateOf(states_visibility) }
                Image(
                    painter = painterResource(Res.drawable.ic_eye),
                    contentDescription = "Settings",
                    modifier = Modifier.size(24.dp).clickable {
                        statesVisibility = !statesVisibility
                        states_visibility = statesVisibility
                    }
                )
                if (statesVisibility) {
                    for (entry in stateMap) {
                        Text(
                            modifier = Modifier,
                            text = "key: ${entry.key}, value: ${entry.value}"
                        )
                    }
                }
            }
        }
    }
}