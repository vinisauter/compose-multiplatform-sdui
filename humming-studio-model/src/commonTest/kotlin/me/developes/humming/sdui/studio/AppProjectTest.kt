package me.developes.humming.sdui.studio

import me.developes.humming.sdui.common.*
import kotlin.test.*

class AppProjectTest {
    @Test fun projectRoundTripsExistingAuthoringWithClosedVersionedMetadata() {
        val doc = LocalWorkspace.blankDocument()
        val project = AppProject("Meu app", ProjectViewport.TABLET, mapOf(doc.entryNodeId to "Início"), doc)
        val codec = AppProjectCodec()
        val source = codec.encode(project).requireValue()
        assertEquals(project, codec.decode(source).requireValue())
        assertEquals(source, codec.encode(codec.decode(source).requireValue()).requireValue())
        assertFalse(codec.decode(source.replace("\"formatVersion\":1", "\"formatVersion\":99")).isSuccess)
        assertFalse(codec.encode(project.copy(name = " ")).isSuccess)
        assertFalse(codec.encode(project.copy(screenNames = mapOf("missing" to "Tela"))).isSuccess)
    }

    @Test fun compositionInsertionIsAtomicAndUndoRestoresNodesEdgesAndOrdering() {
        val catalog = initialNodeCatalog()
        val fragment = LegacyAuthoringCodec(catalog).importDocument("""{"component":"layout:column","id":"group","children":[
          {"component":"layout:button","id":"button","properties":{"onClick":{"component":"action:update","id":"action","properties":{"state":"status","value":"ok"}}},"children":[{"component":"layout:text","id":"label","properties":{"text":"Confirmar"}}]}
        ]}""", "fragment").requireValue()
        val workspace = LocalWorkspace()
        val before = workspace.document
        assertTrue(workspace.insertFragment(fragment, before.entryNodeId).isSuccess)
        assertTrue(workspace.document.edges.isNotEmpty())
        assertTrue(workspace.compile().isSuccess)
        val applied = workspace.document
        assertFalse(workspace.insertFragment(fragment, before.entryNodeId).isSuccess)
        assertEquals(applied, workspace.document)
        assertTrue(workspace.undo().isSuccess)
        assertEquals(before.nodes, workspace.document.nodes); assertEquals(before.edges, workspace.document.edges)
        assertTrue(workspace.redo().isSuccess)
        assertEquals(applied.nodes, workspace.document.nodes)
    }
}
