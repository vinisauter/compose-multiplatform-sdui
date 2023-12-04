package me.next.serverdriven.compose.provider

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import me.next.serverdriven.compose.SDCLibrary
import me.next.serverdriven.core.library.SDLibrary
import me.next.serverdriven.core.tree.ServerDrivenNode

@Composable
fun SDCStateLayout(
    modifier: Modifier = Modifier,
    node: ServerDrivenNode,
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
                    SDCLibrary.loadMethod(this)?.invoke(node, state)?.run {
                        state[stateName] = this
                    }
                }
            }
        )

        lib.addLibrary(SDLibrary("action")
            .addAction("method") { node, state ->
                node.propertyState("method", state)?.run {
                    SDCLibrary.loadMethod(this)?.invoke(node, state)
                }
            }
        )
    }
    Column(modifier) {
        SDCLibrary.loadComponent(node = node, dataState = stateMap)
//      TODO: verify derivedStateOf for node.properties
//        https://medium.com/androiddevelopers/jetpack-compose-when-should-i-use-derivedstateof-63ce7954c11b

//      TODO: remove states log
//        Column(
//            Modifier.fillMaxSize(),
//            verticalArrangement = Arrangement.Bottom
//        ) {
//            for (entry in stateMap) {
//                Text(
//                    modifier = Modifier.fillMaxWidth().background(color = Color.Yellow),
//                    text = "key: ${entry.key}, value: ${entry.value}"
//                )
//            }
//        }
    }
}