package me.developes.humming.sdui.studio

import me.developes.humming.sdui.common.initialNodeCatalog
import kotlin.test.*

class TreeDropTest {
    private fun workspace() = LocalWorkspace().apply { importContract("""{"component":"layout:column","id":"root","children":[{"component":"layout:text","id":"a","properties":{"text":"A"}},{"component":"layout:button","id":"b","children":[{"component":"layout:text","id":"label","properties":{"text":"B"}}]},{"component":"layout:column","id":"c","children":[{"component":"layout:button","id":"nested"}]}]}""").requireValue() }
    private fun children(w: LocalWorkspace, id: String, slot: String = "children") = AuthoringIndices(w.document).childrenByParentAndSlot[ParentSlot(id, slot)].orEmpty().map { it.id }

    @Test fun reordersAndReparentsInOneUndoableCommand() {
        val w = workspace()
        val original = w.document
        val before = TreeDrop.plan(w.document, "c", "a", TreeDropPosition.BEFORE, w.catalog).requireValue()
        assertTrue(TreeDrop.commit(w, before).isSuccess)
        assertEquals(listOf("c", "a", "b"), children(w, "root"))
        assertTrue(w.undo().isSuccess); assertEquals(original.nodes, w.document.nodes)
        val inside = TreeDrop.plan(w.document, "a", "b", TreeDropPosition.INSIDE, w.catalog).requireValue()
        assertTrue(TreeDrop.commit(w, inside).isSuccess)
        assertEquals(listOf("label", "a"), children(w, "b"))
        assertTrue(w.compile().isSuccess)
        assertTrue(w.undo().isSuccess); assertEquals(original.nodes, w.document.nodes)
        assertTrue(w.redo().isSuccess); assertEquals(listOf("label", "a"), children(w, "b"))
        assertTrue(TreeDrop.commit(w, TreeDrop.plan(w.document, "a", "label", TreeDropPosition.BEFORE, w.catalog).requireValue()).isSuccess)
        assertEquals(listOf("a", "label"), children(w, "b"))
    }

    @Test fun cyclesLeavesRootReparentingAndStalePlansAreRejectedWithoutMutation() {
        val w = workspace()
        val original = w.document
        for ((source, target, position) in listOf(Triple("c", "nested", TreeDropPosition.INSIDE), Triple("a", "label", TreeDropPosition.INSIDE),
            Triple("a", "a", TreeDropPosition.AFTER), Triple("a", "root", TreeDropPosition.BEFORE), Triple("root", "b", TreeDropPosition.INSIDE))) {
            assertFalse(TreeDrop.plan(w.document, source, target, position, w.catalog).isSuccess)
            assertEquals(original, w.document)
        }
        val stale = TreeDrop.plan(w.document, "a", "b", TreeDropPosition.INSIDE, w.catalog).requireValue()
        assertTrue(w.reorder("c", -1).isSuccess)
        val after = w.document
        assertFalse(TreeDrop.commit(w, stale).isSuccess); assertEquals(after, w.document)
    }

    @Test fun visualSlotsAreSelectedFromTheSchemaAndPreserveExistingChildren() {
        val w = LocalWorkspace().apply { importContract("""{"component":"layout:scaffold","id":"root","properties":{"topBar":{"component":"layout:text","id":"heading","properties":{"text":"Topo"}}},"children":[{"component":"layout:text","id":"a","properties":{"text":"A"}}]}""").requireValue() }
        val plan = TreeDrop.plan(w.document, "a", "root", TreeDropPosition.INSIDE, initialNodeCatalog(), "topBar").requireValue()
        assertTrue(TreeDrop.commit(w, plan).isSuccess)
        assertEquals(listOf("heading", "a"), children(w, "root", "topBar"))
        assertTrue(w.compile().isSuccess)
    }
}
