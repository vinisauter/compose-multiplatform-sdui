package me.next.serverdriven.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import me.next.serverdriven.core.library.SDLibrary
import me.next.serverdriven.core.tree.ServerDrivenNode

@Composable
fun SDCStateLayout(
    node: ServerDrivenNode,
    modifier: Modifier = Modifier,
    stateMap: MutableMap<String, String> = remember { mutableStateMapOf() }
) {
    SDCLibrary.instance.let { lib ->
        lib.addLibrary(SDLibrary("state")
            .addAction("update") { node, state ->
                val stateName = node.property("state")!!
                node.propertyState("value", state)?.run {
                    state[stateName] = this
                }
                node.propertyState("method", state)?.run {
                    SDCLibrary.loadMethod(this).invoke(node, state).run {
                        state[stateName] = this
                    }
                }
            }
        )

        lib.addLibrary(SDLibrary("action")
            .addAction("method") { node, state ->
                node.propertyState("method", state)?.run {
                    SDCLibrary.loadMethod(this).invoke(node, state)
                }
            }
        )
    }
    Box(Modifier.fillMaxSize()) {
        if (DEBUG) {
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