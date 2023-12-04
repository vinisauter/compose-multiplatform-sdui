package me.next.serverdriven.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.next.serverdriven.compose.SDCLibrary
import me.next.serverdriven.core.tree.ServerDrivenNode

@Composable
fun SDCButton(node: ServerDrivenNode, state: MutableMap<String, String>) {
    val actions = node.propertyNodes("onClick")
    val action = SDCLibrary.loadActions(actions)
    Button(onClick = {
        action.invoke(node, state)
    }) {
        node.children?.let {
            for (serverDrivenNode in it) {
                SDCLibrary.loadComponent(node = serverDrivenNode, dataState = state)
            }
        }
    }
}

@Composable
fun SDCButtonText(node: ServerDrivenNode, state: MutableMap<String, String>) {
    val text = node.propertyState("text", state)!!
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        val actions = node.propertyNodes("onClick")
        val action = SDCLibrary.loadActions(actions)
        Button(onClick = {
            action.invoke(node, state)
        }) {
            Text(text)
        }
    }
}