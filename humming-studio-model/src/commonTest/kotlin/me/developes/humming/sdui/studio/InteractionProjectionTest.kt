package me.developes.humming.sdui.studio

import kotlin.test.*

class InteractionProjectionTest {
    private fun workspace(): LocalWorkspace = LocalWorkspace().also { w ->
        val root = w.document.entryNodeId
        assertTrue(w.insertVisual(root,"layout:button").isSuccess)
        val button = w.document.nodes.values.single { it.definition == "layout:button" }.id
        assertTrue(w.addAction(button,"onClick","action:ifState").isSuccess)
        val condition = w.document.nodes.values.single { it.definition == "action:ifState" }.id
        assertTrue(w.addAction(condition,"then","navigation:goTo",root).isSuccess)
        val go = w.document.nodes.values.single { it.definition == "navigation:goTo" }.id
        assertTrue(w.connectAction(condition,"else",go).isSuccess)
    }

    @Test fun sharedBranchNavigationIsProjectedOncePerEventWithoutFollowingScreenCycles() {
        val w = workspace()
        val links = InteractionProjection.navigationLinks(w.document).requireValue()
        val link = links.single()
        assertEquals(w.document.entryNodeId,link.targetScreenId)
        assertEquals("onClick",link.trigger)
        assertEquals("layout:button",w.document.nodes.getValue(link.sourceNodeId).definition)
        assertEquals(EdgeRole.NAVIGATION,w.document.edges.getValue(link.navigationEdgeId).role)
        assertEquals(links,InteractionProjection.navigationLinks(w.document.copy(edges=w.document.edges.entries.reversed().associate { it.toPair() })).requireValue())
    }

    @Test fun budgetsFailExplicitlyAndUndoRestoresProjectedRelations() {
        val w = workspace()
        assertFalse(InteractionProjection.navigationLinks(w.document,maxVisits=1).isSuccess)
        val links = InteractionProjection.navigationLinks(w.document).requireValue()
        assertTrue(w.disconnect(links.single().navigationEdgeId).isSuccess)
        assertTrue(InteractionProjection.navigationLinks(w.document).requireValue().isEmpty())
        assertTrue(w.undo().isSuccess)
        assertEquals(links,InteractionProjection.navigationLinks(w.document).requireValue())
    }

    @Test fun editorRejectsVisualSelfConnectionWithoutRejectingNavigationBackToTheContainingScreen() {
        val w = workspace()
        val root=w.document.entryNodeId
        val before=w.document.revision
        assertFalse(w.addAction(root,"onClick","navigation:goTo",root).isSuccess)
        assertEquals(before,w.document.revision)
        assertTrue(w.compilePreview().isSuccess) // Existing button -> containing screen remains valid.
    }
}
