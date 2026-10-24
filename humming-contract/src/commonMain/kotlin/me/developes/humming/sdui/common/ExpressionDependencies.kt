package me.developes.humming.sdui.common

/** A null key set means the entire scope. An absent scope has no dependency. */
class ExpressionDependencies internal constructor(scopes: Map<String, Set<String>?>) {
    private val scopes = scopes.mapValues { (_, keys) -> keys?.toSet() }

    fun readsScope(scope: String): Boolean = scopes.containsKey(scope)
    fun readsAll(scope: String): Boolean = readsScope(scope) && scopes[scope] == null
    fun keys(scope: String): Set<String> = scopes[scope]?.toSet().orEmpty()
}

internal fun expressionDependencies(root: ExpressionNode): ExpressionDependencies {
    val scopes = mutableMapOf<String, MutableSet<String>?>()
    fun record(scope: String, key: String?) {
        if (key == null) scopes[scope] = null
        else if (!scopes.containsKey(scope)) scopes[scope] = mutableSetOf(key)
        else scopes[scope]?.add(key)
    }
    fun visit(node: ExpressionNode) {
        when (node) {
            is ExpressionNode.Scope -> record(node.name, null)
            is ExpressionNode.Access -> {
                val base = node.base
                if (base is ExpressionNode.Scope) {
                    val key = (node.key as? ExpressionNode.Literal)?.value as? SduiValue.Text
                    record(base.name, key?.value)
                } else visit(base)
                visit(node.key)
            }
            is ExpressionNode.Unary -> visit(node.child)
            is ExpressionNode.Binary -> { visit(node.left); visit(node.right) }
            is ExpressionNode.Literal -> Unit
        }
    }
    visit(root)
    return ExpressionDependencies(scopes)
}
