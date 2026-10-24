package me.developes.humming.sdui.studio

import kotlin.test.*

class CompiledBundleCodecTest {
    private val codec = CompiledBundleCodec()
    @Test fun treeRoundtripKeepsWireAndIsDeterministic() {
        val contracts = LocalWorkspace().compile().requireValue()
        val encoded = codec.encode(contracts).requireValue()
        assertEquals(contracts,codec.decode(encoded).requireValue())
        assertEquals(encoded,codec.encode(codec.decode(encoded).requireValue()).requireValue())
    }

    @Test fun malformedVersionEntryPathsAndPayloadsAreRejected() {
        val contracts = LocalWorkspace().compile().requireValue()
        assertFalse(codec.decode("{}").isSuccess)
        val encoded = codec.encode(contracts).requireValue()
        assertFalse(codec.decode(encoded.replace("\"formatVersion\":1","\"formatVersion\":2")).isSuccess)
        assertFalse(codec.encode(contracts.copy(entryDestination="missing.json")).isSuccess)
        assertFalse(codec.encode(contracts.copy(artifacts=mapOf("../screen.json" to "{}"))).isSuccess)
        assertFalse(codec.encode(contracts.copy(artifacts=mapOf("screen.json" to "not JSON"))).isSuccess)
    }

    @Test fun typedActionInsertionIsAtomicAndSupportsBranchesAndNavigation() {
        val w = LocalWorkspace()
        val root = w.document.entryNodeId
        assertTrue(w.insertVisual(root,"layout:button").isSuccess)
        val button = w.document.nodes.values.single { it.definition == "layout:button" }.id
        val revision = w.document.revision
        assertFalse(w.addAction(button,"hover","action:ifState").isSuccess)
        assertFalse(w.addAction(button,"onClick","navigation:goTo","missing").isSuccess)
        assertFalse(w.addAction(button,"onClick","action:method").isSuccess)
        assertEquals(revision,w.document.revision)
        assertTrue(w.addAction(button,"onClick","action:ifState").isSuccess)
        val condition = w.document.nodes.values.single { it.definition == "action:ifState" }.id
        assertTrue(w.addAction(condition,"then","action:update").isSuccess)
        assertTrue(w.addAction(condition,"else","navigation:goTo",root).isSuccess)
        val compiled = w.compilePreview().requireValue()
        assertEquals("compiled-preview",compiled.providerType)
        assertEquals(compiled,codec.decode(codec.encode(compiled).requireValue()).requireValue())
        assertTrue(w.undo().isSuccess)
        assertFalse(w.document.nodes.values.any { it.definition == "navigation:goTo" })
        assertFalse(w.document.edges.values.any { it.role == EdgeRole.NAVIGATION })
        assertTrue(w.compilePreview().isSuccess)
    }
}
