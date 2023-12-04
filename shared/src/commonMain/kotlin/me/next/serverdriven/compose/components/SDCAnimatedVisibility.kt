package me.next.serverdriven.compose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import me.next.serverdriven.compose.SDCLibrary
import me.next.serverdriven.core.tree.ServerDrivenNode

@Composable
fun SDCAnimatedVisibility(node: ServerDrivenNode, state: MutableMap<String, String>) {
    val visible by node.propertyState("visible", state) {
        it?.toBoolean() ?: true
    }

    AnimatedVisibility(visible) {
        node.children?.let {
            for (serverDrivenNode in it) {
                SDCLibrary.loadComponent(node = serverDrivenNode, dataState = state)
            }
        }
    }
}