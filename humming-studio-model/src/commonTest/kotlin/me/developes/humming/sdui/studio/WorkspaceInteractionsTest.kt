package me.developes.humming.sdui.studio

import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.*

class WorkspaceInteractionsTest {
    private fun workspace(): LocalWorkspace = LocalWorkspace().also {
        assertTrue(it.importContract("""{"component":"layout:column","id":"screen","children":[{"component":"layout:button","id":"button","properties":{"onClick":[{"component":"action:ifState","id":"condition","properties":{"state":"ready","then":[{"component":"action:update","id":"yes","properties":{"state":"answer","value":"yes"}}],"else":[{"component":"action:update","id":"no","properties":{"state":"answer","value":"no"}}]}},{"component":"navigation:goTo","id":"go","properties":{"destiny":"legacy"}}]}}]}""").isSuccess)
    }

    @Test fun retargetIsAtomicRestoresLegacyValueOnUndoAndCompilesAllScreens() {
        val w = workspace()
        assertTrue(w.addScreen(CanvasPoint(400.0,20.0)).isSuccess)
        val target = w.document.nodes.values.single { it.kind == AuthorNodeKind.SCREEN && it.id != "screen" }
        val before = w.document
        assertTrue(w.setNavigation("go",target.id).isSuccess)
        assertFalse("destiny" in w.document.nodes.getValue("go").properties)
        assertEquals(1,w.document.edges.values.count { it.role == EdgeRole.NAVIGATION })
        val linkedRevision = w.document.revision
        assertFalse(w.putProperty("go","destiny",JsonPrimitive("other")).isSuccess)
        assertEquals(linkedRevision,w.document.revision)
        val compiled = w.compileJourney("compiled").requireValue()
        assertEquals(3,compiled.artifacts.size)
        assertEquals("graph.json",compiled.entryDestination)
        assertTrue(w.undo().isSuccess)
        assertEquals(before.nodes,w.document.nodes)
        assertEquals(before.edges,w.document.edges)
        assertEquals(JsonPrimitive("legacy"),w.document.nodes.getValue("go").properties["destiny"])
        assertTrue(w.redo().isSuccess)
        assertTrue(w.setNavigation("go","screen").isSuccess)
        assertEquals("screen",w.document.edges.values.single { it.role == EdgeRole.NAVIGATION }.targetNodeId)
        assertTrue(w.undo().isSuccess)
        assertEquals(target.id,w.document.edges.values.single { it.role == EdgeRole.NAVIGATION }.targetNodeId)
    }

    @Test fun branchConnectionsKeepPortAndOrderAndDisconnectUndoRestoresTheSameEdge() {
        val w = workspace()
        assertTrue(w.connectAction("condition","then","no").isSuccess)
        val indices = AuthoringIndices(w.document)
        val edges = indices.edgesBySourcePort.getValue(SourcePort("condition","then"))
        assertEquals(listOf("yes","no"),edges.map { it.targetNodeId })
        assertTrue(w.setNavigation("go","screen").isSuccess)
        val artifacts = w.compileJourney("compiled").requireValue().artifacts
        val screen = artifacts.filterKeys { it.startsWith("screens/") }.values.single()
        assertTrue(screen.indexOf("yes") < screen.indexOf("__expanded:no"))
        val edge = edges.last()
        assertTrue(w.disconnect(edge.id).isSuccess)
        assertFalse(edge.id in w.document.edges)
        assertTrue(w.undo().isSuccess)
        assertEquals(edge,w.document.edges[edge.id])
    }

    @Test fun invalidPortsDuplicateConnectionsAndActionCyclesLeaveRevisionUnchanged() {
        val w = workspace()
        var revision = w.document.revision
        assertFalse(w.connectAction("button","hover","yes").isSuccess)
        assertFalse(w.connectAction("condition","then","yes").isSuccess)
        assertFalse(w.connectAction("condition","then","condition").isSuccess)
        assertFalse(w.setNavigation("yes","screen").isSuccess)
        assertFalse(w.setNavigation("go","button").isSuccess)
        assertEquals(revision,w.document.revision)
        assertTrue(w.connectAction("condition","else","go").isSuccess)
        revision = w.document.revision
        assertFalse(w.connectAction("go","invoke","condition").isSuccess)
        assertEquals(revision,w.document.revision)
    }

    @Test fun removingScreenAndUndoRestoresIncidentNavigationAndCanvasPosition() {
        val w = workspace()
        assertTrue(w.addScreen(CanvasPoint(350.0,70.0)).isSuccess)
        val target = w.document.nodes.values.single { it.kind == AuthorNodeKind.SCREEN && it.id != "screen" }
        assertTrue(w.setNavigation("go",target.id).isSuccess)
        val before = w.document
        assertTrue(w.remove(target.id).isSuccess)
        assertFalse(w.document.edges.values.any { it.targetNodeId == target.id })
        assertTrue(w.undo().isSuccess)
        assertEquals(before.nodes,w.document.nodes)
        assertEquals(before.edges,w.document.edges)
        val reopened = LocalWorkspace()
        assertTrue(reopened.openAuthoring(w.save().requireValue()).isSuccess)
        assertEquals(target,reopened.document.nodes[target.id])
    }
}
