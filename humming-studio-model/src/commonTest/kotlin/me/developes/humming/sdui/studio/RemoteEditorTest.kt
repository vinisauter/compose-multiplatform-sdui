package me.developes.humming.sdui.studio

import kotlinx.serialization.json.*
import me.developes.humming.sdui.common.initialNodeCatalog
import kotlin.test.*

class RemoteEditorTest {
    private val catalog = initialNodeCatalog()
    private val codec = AuthoringDocumentCodec(catalog)
    private val identity = RemoteDocument("local-server", "tenant", "document")
    private fun base() = LegacyAuthoringCodec(catalog).importDocument(
        """{"id":"label","component":"layout:text","properties":{"text":"initial"}}""", "document").requireValue()
    private fun command(id: String, revision: Long, text: String) = EditCommand(id, revision, listOf(EditOperation.PutProperty("label", "text", JsonPrimitive(text))))
    private fun open() = RemoteEditor.open(identity, codec.encode(base()).requireValue(), catalog).requireValue()
    private fun save(editor: RemoteEditor): String = editor.checkpoint().requireValue().also { assertTrue(editor.persisted(it)) }
    private fun text(document: AuthoringDocument) = document.nodes.getValue("label").properties["text"]

    @Test fun persistenceGatesSendingAndAnOldAckCannotOverwriteNewerLocalEdits() {
        val editor = open(); val server = EditSession(base(), catalog)
        val first = command("one", 0, "first"); val second = command("two", 1, "second")
        assertTrue(editor.enqueue(first).isSuccess); assertNull(editor.beginSend())
        val oldCheckpoint = save(editor); val ticket = assertNotNull(editor.beginSend())
        assertTrue(editor.enqueue(second).isSuccess)
        val ack = server.execute(first)
        assertTrue(editor.acknowledge(ticket, codec.encode(ack.document!!).requireValue(), ack.appliedRevision!!).requireValue())
        assertEquals(JsonPrimitive("second"), text(editor.document)); assertEquals(2, editor.document.revision)
        assertEquals(1, editor.confirmedRevision); assertEquals(1, editor.pendingCount)
        assertFalse(editor.persisted(oldCheckpoint)); assertNull(editor.beginSend())
        save(editor); val next = assertNotNull(editor.beginSend())
        assertFalse(editor.acknowledge(ticket, codec.encode(ack.document).requireValue(), 1).requireValue())
        val final = server.execute(next.command)
        assertTrue(editor.acknowledge(next, codec.encode(final.document!!).requireValue(), 2).requireValue())
        assertEquals(0, editor.pendingCount); assertEquals(2, editor.confirmedRevision)
        assertTrue(editor.needsPersistence); save(editor); assertFalse(editor.needsPersistence)
    }
    @Test fun restartAfterLostReceiptReplaysTheSameCommandBeforeSendingItsSuccessor() {
        val editor = open(); val server = EditSession(base(), catalog)
        editor.enqueue(command("one", 0, "first")); editor.enqueue(command("two", 1, "second"))
        val checkpoint = save(editor); val lost = assertNotNull(editor.beginSend())
        val applied = server.execute(lost.command); assertEquals(1, applied.appliedRevision)
        val recovered = RemoteEditor.restore(identity, checkpoint, catalog).requireValue()
        val retry = assertNotNull(recovered.beginSend()); assertEquals(lost.command, retry.command)
        val receipt = server.execute(retry.command); assertTrue(receipt.replayed)
        recovered.acknowledge(retry, codec.encode(receipt.document!!).requireValue(), receipt.appliedRevision!!).requireValue()
        save(recovered); val next = assertNotNull(recovered.beginSend())
        val last = server.execute(next.command)
        recovered.acknowledge(next, codec.encode(last.document!!).requireValue(), last.appliedRevision!!).requireValue()
        assertEquals(2, server.snapshot().revision); assertEquals(0, recovered.pendingCount)
        assertFalse(recovered.acknowledge(lost, codec.encode(last.document).requireValue(), 2).requireValue())
    }
    @Test fun newerServerSnapshotPausesWithoutDiscardingTheRemainingLocalWork() {
        val editor = open(); val server = EditSession(base(), catalog)
        editor.enqueue(command("one", 0, "ours-one")); editor.enqueue(command("two", 1, "ours-two")); save(editor)
        val ticket = assertNotNull(editor.beginSend())
        server.execute(ticket.command); val other = server.execute(command("other", 1, "theirs"))
        editor.acknowledge(ticket, codec.encode(other.document!!).requireValue(), 1).requireValue()
        assertEquals(RemotePause.CONFLICT, editor.pause); assertEquals(1, editor.pendingCount)
        assertEquals(JsonPrimitive("ours-two"), text(editor.document))
        assertEquals(JsonPrimitive("theirs"), text(codec.decode(editor.observedRemote!!).requireValue()))
        assertFalse(editor.retry()); assertNull(editor.beginSend())
        val restored = RemoteEditor.restore(identity, save(editor), catalog).requireValue()
        assertEquals(RemotePause.CONFLICT, restored.pause); assertEquals(editor.document, restored.document)
        assertTrue(restored.discardPendingAndOpen(editor.observedRemote!!).requireValue())
        assertEquals(0, restored.pendingCount); assertEquals(JsonPrimitive("theirs"), text(restored.document))
    }
    @Test fun authorizationFailureAndConflictRemainVisibleAndRetainTheOutbox() {
        val editor = open(); editor.enqueue(command("one", 0, "ours")); save(editor)
        val ticket = assertNotNull(editor.beginSend()); assertTrue(editor.failed(ticket, RemotePause.UNAUTHORIZED))
        assertEquals(1, editor.pendingCount); save(editor); assertNull(editor.beginSend())
        assertTrue(editor.retry()); save(editor); val retry = assertNotNull(editor.beginSend())
        assertFalse(editor.failed(ticket, RemotePause.OFFLINE))
        assertTrue(editor.failed(retry, RemotePause.CONFLICT)); assertFalse(editor.retry())
        val server = EditSession(base(), catalog); server.execute(command("other", 0, "theirs"))
        assertTrue(editor.observeConflict(codec.encode(server.snapshot()).requireValue()).requireValue())
        assertEquals(JsonPrimitive("ours"), text(editor.document)); assertEquals(1, editor.pendingCount)
    }
    @Test fun malformedCrossConnectionAndInvalidSequenceCheckpointsAreRejected() {
        val editor = open(); editor.enqueue(command("one", 0, "ours")); val source = save(editor)
        assertFalse(RemoteEditor.restore(identity.copy(connection = "another-server"), source, catalog).isSuccess)
        assertFalse(RemoteEditor.restore(identity, source.replace("\"expectedRevision\":0", "\"expectedRevision\":9"), catalog).isSuccess)
        val root = Json.parseToJsonElement(source).jsonObject
        assertFalse(RemoteEditor.restore(identity, JsonObject(root + ("token" to JsonPrimitive("forbidden"))).toString(), catalog).isSuccess)
        assertFalse(RemoteEditor.open(identity.copy(document = "other"), codec.encode(base()).requireValue(), catalog).isSuccess)
        assertFalse(RemoteEditor.open(identity.copy(connection = "https://user:secret@server"), codec.encode(base()).requireValue(), catalog).isSuccess)
    }
    @Test fun commandsAreOwnedAndInvalidAcknowledgementsDoNotConsumeTheHead() {
        val editor = open(); val operations = mutableListOf<EditOperation>(EditOperation.PutProperty("label", "text", JsonPrimitive("owned")))
        assertTrue(editor.enqueue(EditCommand("one", 0, operations)).isSuccess); operations.clear(); save(editor)
        val ticket = assertNotNull(editor.beginSend()); assertEquals(1, ticket.command.operations.size)
        assertFalse(editor.acknowledge(ticket, codec.encode(base()).requireValue(), 1).isSuccess)
        assertEquals(1, editor.pendingCount); assertTrue(editor.sending)
        editor.failed(ticket, RemotePause.REJECTED); assertFalse(editor.retry())
    }
    @Test fun updatesCannotOverwritePendingWorkOrRewriteAnExistingRevision() {
        val editor = open(); editor.enqueue(command("mine", 0, "local"))
        val server = EditSession(base(), catalog); val changed = server.execute(command("peer", 0, "remote"))
        val feed = DraftUpdateFeed("document", 0, 1, listOf(DraftUpdate(1, "peer", "other", changed.forward!!)), false, false)
        assertFalse(editor.receiveUpdates(feed).requireValue())
        assertEquals(JsonPrimitive("local"), text(editor.document)); assertEquals(1, editor.pendingCount)
        val clean = open()
        val rewritten = changed.document!!.copy(revision = 0)
        assertFalse(clean.adoptClean(codec.encode(rewritten).requireValue()).isSuccess)
        assertEquals(JsonPrimitive("initial"), text(clean.document))
        assertTrue(clean.receiveUpdates(feed).requireValue())
        assertFalse(clean.receiveUpdates(feed).requireValue())
        assertEquals(JsonPrimitive("remote"), text(clean.document))
    }
    @Test fun pendingUndoIsPersistedAndReplayedWithoutAllowingAnEditToBeLostDuringItsReceipt() {
        val server = EditSession(base(), catalog); val own = server.execute(command("one", 0, "own"))
        val editor = RemoteEditor.open(identity, codec.encode(own.document!!).requireValue(), catalog).requireValue()
        val undo = RemoteUndoCommand("undo-one", 1, "one")
        assertTrue(editor.enqueueUndo(undo).requireValue()); assertNull(editor.beginUndo())
        assertFalse(editor.enqueue(command("must-wait", 1, "local edit")).isSuccess)
        val checkpoint = save(editor)
        assertEquals(2, Json.parseToJsonElement(checkpoint).jsonObject.getValue("formatVersion").jsonPrimitive.int)
        val beforeRestart = assertNotNull(editor.beginUndo())
        val recovered = RemoteEditor.restore(identity, checkpoint, catalog).requireValue()
        val retry = assertNotNull(recovered.beginUndo()); assertEquals(beforeRestart.command, retry.command)
        val reverted = server.undo("undo-one", 1); assertTrue(reverted.isSuccess)
        val newer = server.execute(command("peer", 2, "newer peer"))
        assertTrue(recovered.acknowledgeUndo(retry, codec.encode(newer.document!!).requireValue(), 2).requireValue())
        assertEquals(0, recovered.pendingCount); assertEquals(3, recovered.confirmedRevision)
        assertEquals(JsonPrimitive("newer peer"), text(recovered.document))
        assertFalse(recovered.acknowledgeUndo(beforeRestart, codec.encode(newer.document).requireValue(), 2).requireValue())
    }
    @Test fun versionOneRecoveryMigratesToVersionTwoAndMixedUndoAndEditsAreRejected() {
        val editor = open(); val current = Json.parseToJsonElement(save(editor)).jsonObject
        val legacy = JsonObject((current - "pendingUndo") + ("formatVersion" to JsonPrimitive(1))).toString()
        val restored = RemoteEditor.restore(identity, legacy, catalog).requireValue()
        assertEquals(editor.document, restored.document)
        assertEquals(2, Json.parseToJsonElement(restored.checkpoint().requireValue()).jsonObject.getValue("formatVersion").jsonPrimitive.int)
        restored.enqueue(command("edit", 0, "local"))
        val source = Json.parseToJsonElement(save(restored)).jsonObject
        val mixed = JsonObject(source + ("pendingUndo" to buildJsonObject {
            put("commandId", "undo"); put("expectedRevision", 0); put("undoOfCommandId", "other")
        })).toString()
        assertFalse(RemoteEditor.restore(identity, mixed, catalog).isSuccess)
    }
}
