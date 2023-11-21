package me.next.serverdriven.compose.provider

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import me.next.serverdriven.compose.loadComponent
import me.next.serverdriven.core.tree.ServerDrivenNode

@Composable
fun SDCStateLayout(
    modifier: Modifier = Modifier,
    node: ServerDrivenNode
) {
    val stateMap = remember { mutableStateMapOf<String, String>() }
    Column(modifier) {
        loadComponent(node = node, dataState = stateMap)
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