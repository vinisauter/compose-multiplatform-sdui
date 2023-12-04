package me.next.serverdriven.core.tree

object IgnoredNode : ServerDrivenNode {
    override val id: String = "ignored"
    override val component: String = "ignored:ignored"
    override val properties: MutableMap<String, String?> = mutableMapOf()
    override val children: MutableList<ServerDrivenNode> = mutableListOf()
}