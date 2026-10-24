package me.developes.humming.sdui.studio

import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.*

class LocalWorkspaceTest {
    @Test fun reactiveComponentsStartWithValidEnvelopesAndDistinctCanonicalFieldKeys() {
        val workspace = LocalWorkspace()
        val root = workspace.document.entryNodeId
        val text = insert(workspace, root, "layout:reactiveText")
        val button = insert(workspace, root, "layout:reactiveButton")
        val first = insert(workspace, root, "layout:validatedTextField")
        val second = insert(workspace, root, "layout:validatedTextField")
        val nodes = workspace.document.nodes
        assertNotEquals(nodes.getValue(first).properties["stateKey"], nodes.getValue(second).properties["stateKey"])
        assertTrue(nodes.getValue(text).properties["value"] is kotlinx.serialization.json.JsonObject)
        assertTrue(workspace.document.nodes.values.any { (it.placement as? Placement.Child)?.parentId == button })
        assertNotNull(workspace.compile().value)
        assertTrue(workspace.undo().isSuccess)
        assertFalse(second in workspace.document.nodes)
        assertTrue(workspace.redo().isSuccess)
        assertNotNull(workspace.compile().value)
    }

    @Test fun createsEditsUndoesSavesReopensAndCompilesARealContract() {
        val workspace = LocalWorkspace()
        val root = workspace.document.entryNodeId
        val field = insert(workspace, root, "layout:textField")
        val text = insert(workspace, root, "layout:text")
        assertTrue(workspace.putProperty(text, "text", JsonPrimitive("Nome: #{nome} / #{status}")).isSuccess)
        val button = insert(workspace, root, "layout:button")
        assertTrue(workspace.addUpdate(button, "onClick").isSuccess)
        assertTrue(workspace.putProperty(field, "text", JsonPrimitive("Pessoa editada")).isSuccess)
        assertTrue(workspace.undo().isSuccess)
        assertEquals(JsonPrimitive("Visitante"), workspace.document.nodes.getValue(field).properties["text"])
        assertTrue(workspace.redo().isSuccess)
        val saved = workspace.save().requireValue()
        val reopened = LocalWorkspace()
        assertTrue(reopened.openAuthoring(saved).isSuccess)
        assertEquals(saved, reopened.save().requireValue())
        assertFalse(reopened.session.canUndo)
        val compiled = reopened.compile().requireValue()
        val wire = compiled.artifacts.getValue(compiled.entryDestination)
        val imported = LocalWorkspace()
        assertTrue(imported.importContract(wire).isSuccess)
        assertEquals(wire, imported.compile().requireValue().artifacts.values.single())
        assertTrue(wire.contains("Nome: #{nome} / #{status}"))
    }

    @Test fun invalidOpenKeepsCurrentDocumentAndUndoHistory() {
        val workspace = LocalWorkspace()
        insert(workspace, workspace.document.entryNodeId, "layout:text")
        val before = workspace.save().requireValue()
        assertFalse(workspace.openAuthoring("{}").isSuccess)
        assertFalse(workspace.importContract("not JSON").isSuccess)
        assertEquals(before, workspace.save().requireValue())
        assertTrue(workspace.session.canUndo)
    }

    @Test fun schemasRejectInvalidEditsAndPreserveMissingVersusEmpty() {
        val workspace = LocalWorkspace()
        val root = workspace.document.entryNodeId
        val text = insert(workspace, root, "layout:text")
        val before = workspace.document
        assertFalse(workspace.putProperty(text, "text", null).isSuccess)
        assertFalse(workspace.putProperty(root, "horizontalAlignment", JsonPrimitive("invalid")).isSuccess)
        assertFalse(workspace.putProperty(root, "paddingAll", JsonPrimitive("infinite")).isSuccess)
        assertEquals(before, workspace.document)
        assertTrue(workspace.putProperty(text, "text", JsonPrimitive("")).isSuccess)
        assertEquals(JsonPrimitive(""), workspace.document.nodes.getValue(text).properties["text"])
        assertTrue(workspace.putProperty(root, "paddingAll", null).isSuccess)
        assertFalse("paddingAll" in workspace.document.nodes.getValue(root).properties)
    }

    @Test fun unknownDataIsSavedReadOnlyAndStopsCompilation() {
        val workspace = LocalWorkspace()
        assertTrue(workspace.importContract("""{"component":"layout:column","id":"root","properties":{"future":{"a":1}},"children":[{"component":"custom:widget","id":"unknown"}]}""").isSuccess)
        val before = workspace.save().requireValue()
        assertFalse(workspace.putProperty("root", "future", JsonPrimitive("overwrite")).isSuccess)
        assertFalse(workspace.putProperty("unknown", "text", JsonPrimitive("overwrite")).isSuccess)
        assertEquals(before, workspace.save().requireValue())
        assertFalse(workspace.compile().isSuccess)
        val reopened = LocalWorkspace()
        assertTrue(reopened.openAuthoring(before).isSuccess)
        assertEquals(before, reopened.save().requireValue())
    }

    @Test fun reorderAndMoveChangeTheCompiledOrderAndUndoRestoresIt() {
        val workspace = LocalWorkspace()
        val root = workspace.document.entryNodeId
        val first = insert(workspace, root, "layout:text")
        val second = insert(workspace, root, "layout:column")
        assertTrue(workspace.reorder(second, -1).isSuccess)
        assertEquals(listOf(second, first), children(workspace, root))
        assertTrue(workspace.move(first, second, "children").isSuccess)
        assertEquals(listOf(first), children(workspace, second))
        val before = workspace.document
        assertFalse(workspace.move(second, second, "children").isSuccess)
        assertEquals(before, workspace.document)
        assertTrue(workspace.undo().isSuccess)
        assertEquals(listOf(second, first), children(workspace, root))
        assertTrue(workspace.compile().isSuccess)
    }

    @Test fun actionReorderingUsesEdgeOrderAndInvalidPortsAreAtomic() {
        val workspace = LocalWorkspace()
        val button = insert(workspace, workspace.document.entryNodeId, "layout:button")
        assertTrue(workspace.addUpdate(button, "onClick").isSuccess)
        assertTrue(workspace.addUpdate(button, "onClick").isSuccess)
        val edges = AuthoringIndices(workspace.document).edgesBySourcePort.getValue(SourcePort(button, "onClick"))
        assertTrue(workspace.reorder(edges[1].targetNodeId, -1, edges[1].id).isSuccess)
        assertEquals(edges.reversed().map { it.id }, AuthoringIndices(workspace.document).edgesBySourcePort.getValue(SourcePort(button, "onClick")).map { it.id })
        val before = workspace.document
        assertFalse(workspace.addUpdate(button, "missing").isSuccess)
        assertEquals(before, workspace.document)
        assertTrue(workspace.compile().isSuccess)
    }

    @Test fun removedButtonDoesNotSilentlyDeleteOrDropItsActions() {
        val workspace = LocalWorkspace()
        val button = insert(workspace, workspace.document.entryNodeId, "layout:button")
        assertTrue(workspace.addUpdate(button, "onClick").isSuccess)
        val action = workspace.document.edges.values.single().targetNodeId
        assertTrue(workspace.remove(button).isSuccess)
        assertTrue(action in workspace.document.nodes)
        assertTrue(workspace.tree().any { it.node.id == action && it.depth == 0 })
        assertFalse(workspace.compile().isSuccess)
        assertTrue(workspace.remove(action).isSuccess)
        assertTrue(workspace.compile().isSuccess)
    }

    @Test fun unavailableEffectsAreNeverMadeExecutableByImport() {
        val workspace = LocalWorkspace()
        assertTrue(workspace.importContract("""{"component":"layout:button","id":"root","properties":{"onClick":{"component":"action:method","id":"method","properties":{"invoke":"getPlatformName"}}}}""").isSuccess)
        assertTrue(workspace.compile().diagnostics.any { it.code == "UNSUPPORTED_METHOD" })
        assertEquals(setOf("root", "method"), workspace.tree().map { it.node.id }.toSet())
        workspace.newDocument()
        assertFalse(workspace.session.canUndo)
        assertTrue(workspace.compile().isSuccess)
    }

    private fun insert(workspace: LocalWorkspace, parent: String, definition: String): String {
        val before = workspace.document.nodes.keys
        assertTrue(workspace.insertVisual(parent, definition).isSuccess)
        return workspace.document.nodes.values.first { it.id !in before && it.definition == definition &&
            (it.placement as? Placement.Child)?.parentId == parent }.id
    }
    private fun children(workspace: LocalWorkspace, parent: String) =
        AuthoringIndices(workspace.document).childrenByParentAndSlot[ParentSlot(parent, "children")].orEmpty().map { it.id }
}
