package me.next.serverdriven.core.tree

interface ServerDrivenAction : ServerDrivenNode {
    val handler: (event: ServerDrivenAction) -> Unit
}