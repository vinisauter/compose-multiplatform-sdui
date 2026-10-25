package sample.app

import kotlinx.coroutines.*
import me.developes.humming.sdui.common.initialNodeCatalog
import me.developes.humming.sdui.studio.*
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.*

class RemoteStudioHostTest {
    private val catalog = initialNodeCatalog()
    private val codec = AuthoringDocumentCodec(catalog)
    private val identity = RemoteDocument("local-server", "tenant", "document")
    private fun base() = LegacyAuthoringCodec(catalog).importDocument(
        """{"id":"label","component":"layout:text","properties":{"text":"initial"}}""", identity.document).requireValue()
    private class Store : RemoteStudioRecovery {
        var source: String? = null; var fail = false
        val archives = mutableListOf<String>()
        override suspend fun read(): RecoveryRead = RecoveryRead.Saved(source)
        override suspend fun save(source: String): Boolean { if (fail) return false; this.source = source; return true }
        override suspend fun archive(source: String): Boolean { if (fail) return false; archives += source; return true }
    }
    private class Api(var source: String) : RemoteStudioApi {
        var calls = 0
        var action: suspend (EditCommand) -> RemoteStudioSubmit = { RemoteStudioSubmit.Failed(RemotePause.OFFLINE) }
        override suspend fun read(): RemoteStudioRead = RemoteStudioRead.Document(source)
        override suspend fun submit(command: EditCommand): RemoteStudioSubmit { calls++; return action(command) }
    }
    @Test fun failedStoragePreventsSendAndCancellationKeepsTheRecoverableHead() = runBlocking {
        val store = Store(); val api = Api(codec.encode(base()).requireValue())
        val host = RemoteStudioHost.open(identity, catalog, api, store).requireValue()
        host.workspace.putProperty("label", "text", JsonPrimitive("local"))
        store.fail = true; host.synchronize(); assertEquals(0, api.calls); assertEquals(1, host.editor.pendingCount)
        store.fail = false
        val entered = CompletableDeferred<Unit>()
        api.action = { entered.complete(Unit); awaitCancellation() }
        val request = launch { host.synchronize() }; entered.await(); request.cancelAndJoin()
        assertFalse(host.editor.sending); assertEquals(RemotePause.OFFLINE, host.editor.pause)
        val restored = RemoteStudioHost.open(identity, catalog, api, store).requireValue()
        assertEquals(1, restored.editor.pendingCount)
        assertEquals(host.editor.document, restored.editor.document)
    }
    @Test fun programmingFailurePropagatesAndDestructiveResolutionRequiresARecoveryArchive() = runBlocking {
        val store = Store(); val api = Api(codec.encode(base()).requireValue())
        val host = RemoteStudioHost.open(identity, catalog, api, store).requireValue()
        host.workspace.putProperty("label", "text", JsonPrimitive("local"))
        api.action = { error("adapter defect") }
        assertFailsWith<IllegalStateException> { host.synchronize() }
        assertFalse(host.editor.sending); assertEquals(RemotePause.REJECTED, host.editor.pause)
        store.fail = true; host.archiveAndUseRemote(); assertEquals(1, host.editor.pendingCount)
        store.fail = false; host.archiveAndUseRemote()
        assertEquals(1, store.archives.size); assertEquals(0, host.editor.pendingCount)
        assertEquals(JsonPrimitive("local"), RemoteEditor.restore(identity, store.archives.single(), catalog).requireValue().document.nodes.getValue("label").properties["text"])
        assertEquals(JsonPrimitive("initial"), host.workspace.document.nodes.getValue("label").properties["text"])
    }
    @Test fun fileRecoveryRejectsMalformedBytesAndDoesNotOverwriteAStorageFailure() = runBlocking {
        val directory = Files.createTempDirectory(Path.of("build"), "studio-recovery-")
        val path = directory.resolve("checkpoint.json")
        val store = JvmRemoteStudioRecovery(path)
        assertEquals(RecoveryRead.Saved(null), store.read())
        val source = RemoteEditor.open(identity, codec.encode(base()).requireValue(), catalog).requireValue().checkpoint().requireValue()
        assertTrue(store.save(source)); assertEquals(RecoveryRead.Saved(source), store.read())
        assertTrue(store.archive(source)); assertEquals(source, Files.readString(path))
        val unavailable = JvmRemoteStudioRecovery(path.resolve("not-a-directory.json"))
        assertFalse(unavailable.save(source)); assertEquals(source, Files.readString(path))
        Files.write(path, byteArrayOf(0xc3.toByte(), 0x28)); assertEquals(RecoveryRead.Failed, store.read())
    }
}
