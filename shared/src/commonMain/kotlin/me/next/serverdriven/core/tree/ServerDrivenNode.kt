package me.next.serverdriven.core.tree

interface ServerDrivenNode {
    val id: String
    val component: String
    val properties: MutableMap<String, Any?>?
    // val state: Map<String, () -> Any?>?// todo state machine e callback// method mirror
    val children: MutableList<ServerDrivenNode>?
}