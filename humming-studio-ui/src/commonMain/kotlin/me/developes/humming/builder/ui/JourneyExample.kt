package me.developes.humming.builder.ui

import kotlinx.serialization.json.JsonPrimitive
import me.developes.humming.sdui.studio.*

internal fun journeyExample(): AuthoringDocument {
    val w = LocalWorkspace()
    fun insert(parent: String, definition: String): String {
        val before = w.document.nodes.keys
        check(w.insertVisual(parent,definition).isSuccess)
        return w.document.nodes.values.single { it.id !in before && it.definition == definition }.id
    }
    fun button(screen: String, label: String): String {
        val id = insert(screen,"layout:button")
        val text = w.document.nodes.values.single { (it.placement as? Placement.Child)?.parentId == id }
        check(w.putProperty(text.id,"text",JsonPrimitive(label)).isSuccess)
        return id
    }
    val first = w.document.entryNodeId
    val title = insert(first,"layout:text")
    check(w.putProperty(title,"text",JsonPrimitive("Primeira tela")).isSuccess)
    val next = button(first,"Abrir segunda")
    val before = w.document.nodes.keys
    check(w.addScreen(CanvasPoint(400.0,0.0)).isSuccess)
    val second = w.document.nodes.values.single { it.id !in before }.id
    val secondTitle = insert(second,"layout:text")
    check(w.putProperty(secondTitle,"text",JsonPrimitive("Segunda tela")).isSuccess)
    val back = button(second,"Voltar")
    check(w.addAction(next,"onClick","navigation:goTo",second).isSuccess)
    check(w.addAction(back,"onClick","navigation:goBack").isSuccess)
    return w.document
}
