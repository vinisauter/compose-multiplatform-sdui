package me.developes.humming.sdui.studio

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonNull
import me.developes.humming.sdui.common.initialNodeCatalog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EditSessionTest {
    private val catalog = initialNodeCatalog()
    private val source = """{"component":"layout:column","id":"root","children":[{"component":"layout:column","id":"p","properties":{"onClick":{"component":"action:update","id":"action","properties":{"state":"status","value":"ok"}}},"children":[{"component":"layout:text","id":"a","properties":{"text":"A"}},{"component":"layout:text","id":"b","properties":{"text":"B"}}]},{"component":"layout:box","id":"q","children":[{"component":"layout:text","id":"c","properties":{"text":"C"}}]}]}"""
    private fun document() = LegacyAuthoringCodec(catalog).importDocument(source, "edits").requireValue()
    private fun command(id: String, revision: Long, vararg operations: EditOperation) = EditCommand(id, revision, operations.toList())
    private fun properties(value: String) = JsonObject(mapOf("tag" to JsonPrimitive(value)))
    private fun children(doc: AuthoringDocument, parent: String) = AuthoringIndices(doc)
        .childrenByParentAndSlot[ParentSlot(parent, "children")].orEmpty().map { it.id }
    private fun sameContent(expected: AuthoringDocument, actual: AuthoringDocument) {
        assertEquals(expected.copy(revision = actual.revision), actual)
    }

    @Test fun movingWithinOneParentChangesOnlyMovedPlacementAndSupportsUndoRedo() {
        val original = document()
        val session = EditSession(original, catalog)
        val moved = session.execute(command("move", 0, EditOperation.MoveNode("b", NodeContainer.Slot("p", "children"), nextId = "a")))
        assertTrue(moved.isSuccess)
        assertEquals(listOf("b", "a"), children(session.snapshot(), "p"))
        assertEquals(setOf("b"), moved.forward!!.nodes.keys)
        assertEquals(original.nodes.getValue("a").placement, session.snapshot().nodes.getValue("a").placement)
        sameContent(original, session.undo("undo", 1).document!!)
        sameContent(moved.document!!, session.redo("redo", 2).document!!)
        assertEquals(3, session.snapshot().revision)
    }

    @Test fun movingAcrossContainersPreservesOtherSiblingKeys() {
        val original = document()
        val session = EditSession(original, catalog)
        val moved = session.execute(command("move", 0,
            EditOperation.MoveNode("a", NodeContainer.Slot("q", "children"), previousId = "c")))
        assertTrue(moved.isSuccess)
        assertEquals(listOf("b"), children(session.snapshot(), "p"))
        assertEquals(listOf("c", "a"), children(session.snapshot(), "q"))
        for (id in listOf("b", "c")) assertEquals(original.nodes.getValue(id).placement, session.snapshot().nodes.getValue(id).placement)
        sameContent(original, session.undo("undo", 1).document!!)
    }

    @Test fun removeAndUndoRestoreEntireSubtreeIncidentEdgesAndWireShape() {
        val original = document()
        val session = EditSession(original, catalog)
        assertTrue(session.execute(command("remove", 0, EditOperation.RemoveNode("p"))).isSuccess)
        assertTrue(listOf("p", "a", "b").none { it in session.snapshot().nodes })
        assertTrue("action" in session.snapshot().nodes) // Action roots are not silently deleted with their former trigger.
        assertTrue(session.snapshot().edges.isEmpty())
        sameContent(original, session.undo("undo", 1).document!!)
        assertEquals(NodeListShape.OBJECT, session.snapshot().nodes.getValue("p").nodeListShapes["onClick"])
    }

    @Test fun failedBatchPublishesNeitherEarlierOperationsNorHistory() {
        val original = document()
        val session = EditSession(original, catalog)
        val result = session.execute(command("batch", 0, EditOperation.SetProperties("root", properties("uncommitted")),
            EditOperation.MoveNode("a", NodeContainer.Slot("missing", "children"))))
        assertFalse(result.isSuccess)
        assertEquals("NODE_NOT_FOUND", result.diagnostics.single().code)
        assertEquals(original, session.snapshot())
        assertFalse(session.canUndo)
        assertTrue(session.execute(command("batch", 0, EditOperation.SetProperties("root", properties("committed")))).isSuccess)
    }

    @Test fun ancestryAndStaleNeighborsAreRejectedAtomically() {
        val original = document()
        val nested = original.nodes.getValue("p").copy(id = "nested", placement = Placement.Child("p", "children", "z"),
            properties = JsonObject(emptyMap()), nodeListShapes = emptyMap(), hasChildren = false)
        val session = EditSession(original.copy(nodes = original.nodes + (nested.id to nested)), catalog)
        val before = session.snapshot()
        val cycle = session.execute(command("cycle", 0, EditOperation.MoveNode("p", NodeContainer.Slot("nested", "children"))))
        assertTrue(cycle.diagnostics.any { it.code == "CONTAINMENT_CYCLE" })
        assertEquals(before, session.snapshot())
        val stale = session.execute(command("stale", 0, EditOperation.MoveNode("a", NodeContainer.Slot("q", "children"), previousId = "b")))
        assertEquals("STALE_NEIGHBOR", stale.diagnostics.single().code)
        assertEquals(before, session.snapshot())
    }

    @Test fun revisionConflictsAndRetriesNeverApplyTwiceOrReturnAnOldDocument() {
        val session = EditSession(document(), catalog)
        val first = command("one", 0, EditOperation.SetProperties("root", properties("one")))
        assertTrue(session.execute(first).isSuccess)
        assertEquals("REVISION_CONFLICT", session.execute(command("stale", 0, EditOperation.SetProperties("root", properties("stale")))).diagnostics.single().code)
        assertTrue(session.execute(command("two", 1, EditOperation.SetProperties("root", properties("two")))).isSuccess)
        val retry = session.execute(first)
        assertTrue(retry.replayed)
        assertEquals(1, retry.appliedRevision)
        assertEquals(2, retry.document!!.revision)
        assertEquals(properties("two"), retry.document!!.nodes.getValue("root").properties)
        assertEquals("COMMAND_REPLAY_CONFLICT", session.execute(command("one", 0, EditOperation.SetProperties("root", properties("different")))).diagnostics.single().code)
    }

    @Test fun aNewEditAfterUndoClearsRedo() {
        val session = EditSession(document(), catalog)
        assertTrue(session.execute(command("one", 0, EditOperation.SetCanvasPosition("root", CanvasPoint(10.0, 20.0)))).isSuccess)
        assertTrue(session.undo("undo", 1).isSuccess)
        assertTrue(session.canRedo)
        assertTrue(session.execute(command("two", 2, EditOperation.SetCanvasPosition("root", CanvasPoint(30.0, 40.0)))).isSuccess)
        assertFalse(session.canRedo)
        assertEquals("NOTHING_TO_REDO", session.redo("redo", 3).diagnostics.single().code)
    }

    @Test fun entryReplacementAndInsertionCanBeCommittedAsOneBatch() {
        val original = document()
        val session = EditSession(original, catalog)
        val newRoot = original.nodes.getValue("root").copy(id = "new", placement = Placement.Root("V"), hasChildren = false)
        val lastRoot = AuthoringIndices(original).roots.last().id
        val result = session.execute(command("replace", 0, EditOperation.InsertNode(newRoot, previousId = lastRoot),
            EditOperation.SetEntry("new"), EditOperation.RemoveNode("root")))
        assertTrue(result.isSuccess)
        assertEquals("new", session.snapshot().entryNodeId)
        assertEquals(setOf("new", "action"), session.snapshot().nodes.keys)
        sameContent(original, session.undo("undo", 1).document!!)
    }

    @Test fun addingAndRemovingEdgesMaintainsObjectArrayHintsAndUndoRestoresThem() {
        val original = document()
        val session = EditSession(original, catalog)
        val template = original.nodes.getValue("action").copy(id = "second")
        val edge = original.edges.values.single().copy(id = "second-edge", targetNodeId = "second")
        val result = session.execute(command("add", 0, EditOperation.InsertNode(template, previousId = "action"),
            EditOperation.InsertEdge(edge, previousId = original.edges.values.single().id)))
        assertTrue(result.isSuccess)
        assertEquals(NodeListShape.ARRAY, session.snapshot().nodes.getValue("p").nodeListShapes["onClick"])
        sameContent(original, session.undo("undo", 1).document!!)
        assertTrue(session.execute(command("remove-edge", 2, EditOperation.RemoveEdge(original.edges.keys.single()))).isSuccess)
        assertNull(session.snapshot().nodes.getValue("p").nodeListShapes["onClick"])
        sameContent(original, session.undo("undo-edge", 3).document!!)
    }

    @Test fun historyAndReceiptBudgetsAreExplicitAndNeverPartiallyCommit() {
        val original = document()
        val history = EditSession(original, catalog, sessionLimits = EditSessionLimits(maxHistoryChars = 1))
        assertEquals("HISTORY_BUDGET", history.execute(command("large", 0, EditOperation.SetProperties("root", properties("changed")))).diagnostics.single().code)
        assertEquals(original, history.snapshot())
        val receipts = EditSession(original, catalog, sessionLimits = EditSessionLimits(maxReceiptChars = 5))
        assertEquals("RECEIPT_BUDGET", receipts.execute(command("large", 0, EditOperation.SetProperties("root", properties("changed")))).diagnostics.single().code)
        assertEquals(original, receipts.snapshot())
    }

    @Test fun boundedHistoryTrimsOldestEntriesWithoutInventingUndo() {
        val session = EditSession(document(), catalog, sessionLimits = EditSessionLimits(maxHistoryEntries = 2))
        repeat(3) { index -> assertTrue(session.execute(command("edit$index", index.toLong(),
            EditOperation.SetCanvasPosition("root", CanvasPoint(index.toDouble(), 0.0)))).isSuccess) }
        assertTrue(session.undo("undo1", 3).isSuccess)
        assertTrue(session.undo("undo2", 4).isSuccess)
        assertEquals(CanvasPoint(0.0, 0.0), session.snapshot().nodes.getValue("root").canvasPosition)
        assertEquals("NOTHING_TO_UNDO", session.undo("undo3", 5).diagnostics.single().code)
    }

    @Test fun callerOwnedMapsAndCommandListsCannotMutateCommittedSnapshots() {
        val original = document()
        val mutableNodes = original.nodes.toMutableMap()
        val session = EditSession(original.copy(nodes = mutableNodes), catalog)
        mutableNodes.clear()
        assertEquals(original.nodes.size, session.snapshot().nodes.size)
        val mutableProperties = mutableMapOf("tag" to JsonPrimitive("owned"))
        val operations = mutableListOf<EditOperation>(EditOperation.SetProperties("root", JsonObject(mutableProperties)))
        val command = EditCommand("owned", 0, operations)
        assertTrue(session.execute(command).isSuccess)
        mutableProperties["tag"] = JsonPrimitive("external mutation")
        operations.clear()
        assertEquals(properties("owned"), session.snapshot().nodes.getValue("root").properties)
    }

    @Test fun keyExhaustionLeavesDocumentAndHistoryUnchanged() {
        val root = AuthorNode("root", AuthorNodeKind.SCREEN, "layout:column", JsonObject(emptyMap()), Placement.Root("V"))
        val first = AuthorNode("a", AuthorNodeKind.VISUAL, "layout:text", JsonObject(mapOf("text" to JsonPrimitive("A"))), Placement.Child("root", "children", "V"))
        val second = first.copy(id = "b", placement = Placement.Child("root", "children", "W"))
        val doc = AuthoringDocument("short", entryNodeId = "root", nodes = listOf(root, first, second).associateBy { it.id })
        val session = EditSession(doc, catalog, AuthoringLimits(maxOrderKeyLength = 1))
        val newNode = first.copy(id = "new")
        val result = session.execute(command("insert", 0, EditOperation.InsertNode(newNode, "a", "b")))
        assertEquals("KEY_SPACE_EXHAUSTED", result.diagnostics.single().code)
        assertEquals(doc, session.snapshot())
        assertFalse(session.canUndo)
    }

    @Test fun propertyEditsPreserveOtherFieldsAndDistinguishJsonNullFromRemoval() {
        val original = document()
        val root = original.nodes.getValue("root").copy(properties = properties("keep"))
        val session = EditSession(original.copy(nodes = original.nodes + (root.id to root)), catalog)
        assertTrue(session.execute(command("put", 0, EditOperation.PutProperty("root", "nullable", JsonNull))).isSuccess)
        assertEquals(JsonNull, session.snapshot().nodes.getValue("root").properties["nullable"])
        assertEquals(JsonPrimitive("keep"), session.snapshot().nodes.getValue("root").properties["tag"])
        assertTrue(session.execute(command("remove", 1, EditOperation.RemoveProperty("root", "nullable"))).isSuccess)
        assertFalse("nullable" in session.snapshot().nodes.getValue("root").properties)
        assertTrue(session.undo("undo-remove", 2).isSuccess)
        assertEquals(JsonNull, session.snapshot().nodes.getValue("root").properties["nullable"])
        assertTrue(session.undo("undo-put", 3).isSuccess)
        assertEquals(properties("keep"), session.snapshot().nodes.getValue("root").properties)
    }

    @Test fun retryOutsideTheBoundedReceiptWindowConflictsInsteadOfApplyingAgain() {
        val session = EditSession(document(), catalog, sessionLimits = EditSessionLimits(maxReceipts = 1))
        val first = command("first", 0, EditOperation.PutProperty("root", "tag", JsonPrimitive("first")))
        assertTrue(session.execute(first).isSuccess)
        assertTrue(session.execute(command("second", 1, EditOperation.PutProperty("root", "tag", JsonPrimitive("second")))).isSuccess)
        assertEquals("REVISION_CONFLICT", session.execute(first).diagnostics.single().code)
        assertEquals(2L, session.snapshot().revision)
    }

    @Test fun invalidInsertedDefinitionCannotBypassPayloadIdentifierRules() {
        val original = document()
        val session = EditSession(original, catalog)
        val template = original.nodes.getValue("root").copy(id = "invalid", definition = "unqualified",
            kind = AuthorNodeKind.UNKNOWN, hasChildren = false)
        val result = session.execute(command("invalid", 0, EditOperation.InsertNode(template, previousId = "action")))
        assertTrue(result.diagnostics.any { it.code == "INVALID_IDENTIFIER" && it.nodeId == "invalid" })
        assertEquals(original, session.snapshot())
        assertFalse(session.canUndo)
    }
}
