package sample.app

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.studio.*

sealed interface RemoteStudioRead {
    data class Document(val source: String) : RemoteStudioRead
    data class Failed(val reason: RemotePause) : RemoteStudioRead
}
sealed interface RemoteStudioSubmit {
    data class Acknowledged(val source: String, val appliedRevision: Long) : RemoteStudioSubmit
    data class Failed(val reason: RemotePause) : RemoteStudioSubmit
}
interface RemoteStudioApi {
    suspend fun read(): RemoteStudioRead
    suspend fun submit(command: EditCommand): RemoteStudioSubmit
}
interface RemoteStudioCollaborationApi {
    suspend fun updates(afterRevision: Long): DraftUpdateFeed?
    suspend fun heartbeat(): DraftPresenceList?
    suspend fun leave()
    suspend fun undo(command: RemoteUndoCommand): RemoteStudioSubmit
}
sealed interface RecoveryRead {
    data class Saved(val source: String?) : RecoveryRead
    data object Failed : RecoveryRead
}
interface RemoteStudioRecovery {
    suspend fun read(): RecoveryRead
    suspend fun save(source: String): Boolean
    suspend fun archive(source: String): Boolean
}

/** Confined to the UI owner. IO adapters suspend; no background work or implicit retries are created here. */
class RemoteStudioHost private constructor(val editor: RemoteEditor, val workspace: LocalWorkspace,
    private val api: RemoteStudioApi, private val recovery: RemoteStudioRecovery) {
    private val mutex = Mutex()
    val supportsCollaboration: Boolean get() = api is RemoteStudioCollaborationApi
    var presence: DraftPresenceList = DraftPresenceList(emptyList())
        private set
    var presenceAvailable: Boolean = false
        private set
    var lastOwnCommandId: String? = null
        private set
    private suspend fun saveLatest(): Boolean {
        repeat(8) {
            val source = editor.checkpoint().requireValue()
            if (!recovery.save(source)) return false
            if (editor.persisted(source)) return true
        }
        return false // Continuous edits are visible as unsaved; never send an unpersisted command.
    }
    suspend fun saveRecovery(): Boolean = mutex.withLock { saveLatest() }
    suspend fun synchronize(): String = mutex.withLock {
        if (!editor.retry()) return@withLock "Conflito/rejeição: recuperação preservada; resolução explícita necessária"
        if (!saveLatest()) return@withLock "Falha ao salvar recuperação; nenhum novo comando enviado"
        var sent = 0
        while (editor.pendingCount > 0 && sent++ < 128) {
            val undo = editor.beginUndo()
            val ticket = if (undo == null) editor.beginSend() else null
            if (ticket == null && undo == null) break
            fun fail(reason: RemotePause) { if (undo != null) editor.failedUndo(undo, reason) else editor.failed(ticket!!, reason) }
            try {
                val submitted = if (undo != null) (api as? RemoteStudioCollaborationApi)?.undo(undo.command)
                    ?: RemoteStudioSubmit.Failed(RemotePause.REJECTED) else api.submit(ticket!!.command)
                when (val response = submitted) {
                    is RemoteStudioSubmit.Acknowledged -> {
                        val result = if (undo != null) editor.acknowledgeUndo(undo, response.source, response.appliedRevision)
                            else editor.acknowledge(ticket!!, response.source, response.appliedRevision)
                        if (!result.isSuccess) fail(RemotePause.REJECTED)
                        else if (result.value == true) lastOwnCommandId = if (undo == null) ticket!!.command.commandId else null
                    }
                    is RemoteStudioSubmit.Failed -> {
                        fail(response.reason)
                        if (response.reason == RemotePause.CONFLICT) when (val remote = api.read()) {
                            is RemoteStudioRead.Document -> editor.observeConflict(remote.source)
                            is RemoteStudioRead.Failed -> Unit // Keep the outbox and conflict even if the follow-up GET fails.
                        }
                    }
                }
            } catch (cancelled: CancellationException) {
                fail(RemotePause.OFFLINE)
                throw cancelled
            } catch (failure: Throwable) {
                fail(RemotePause.REJECTED)
                throw failure
            }
            workspace.refreshRemote()
            if (!saveLatest()) return@withLock "Confirmação recebida; recuperação ainda não salva, envio pausado"
            if (editor.pause != RemotePause.NONE) return@withLock "Envio pausado: ${editor.pause}; alterações locais preservadas"
        }
        if (editor.pendingCount == 0) "Salvo no servidor na revisão ${editor.confirmedRevision}"
        else "Pendências preservadas; sincronização incompleta"
    }
    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    suspend fun undoLast(): String {
        val enqueued = mutex.withLock {
            val original = lastOwnCommandId ?: return@withLock false
            if (!supportsCollaboration || editor.pendingCount > 0 || editor.sending) return@withLock false
            editor.enqueueUndo(RemoteUndoCommand("undo-${kotlin.uuid.Uuid.random()}", editor.confirmedRevision, original)).isSuccess
        }
        return if (enqueued) synchronize() else "Nenhum envio próprio elegível para desfazer"
    }
    suspend fun pollUpdates(): String = mutex.withLock {
        val collaboration = api as? RemoteStudioCollaborationApi ?: return@withLock "Atualizações não instaladas"
        val members = collaboration.heartbeat()
        presenceAvailable = members != null
        presence = members ?: DraftPresenceList(emptyList())
        if (editor.pendingCount > 0 || editor.sending) return@withLock "Pendências locais preservadas"
        val feed = collaboration.updates(editor.confirmedRevision) ?: return@withLock "Atualizações indisponíveis"
        val applied = editor.receiveUpdates(feed)
        var changed = applied.value == true
        if (!applied.isSuccess) {
            when (val snapshot = api.read()) {
                is RemoteStudioRead.Document -> {
                    val adopted = editor.adoptClean(snapshot.source)
                    if (!adopted.isSuccess) return@withLock "Ressincronização rejeitada"
                    changed = adopted.value == true
                }
                is RemoteStudioRead.Failed -> return@withLock "Ressincronização indisponível"
            }
        }
        if (changed) workspace.refreshRemote()
        if (editor.needsPersistence && !saveLatest()) "Atualização recebida; recuperação ainda não salva" else "Atualizações verificadas"
    }
    suspend fun leavePresence() = mutex.withLock { (api as? RemoteStudioCollaborationApi)?.leave(); presence = DraftPresenceList(emptyList()); presenceAvailable = false }
    suspend fun archiveAndUseRemote(): String = mutex.withLock {
        if (editor.sending) return@withLock "Aguarde o request atual"
        val latest = api.read()
        if (latest !is RemoteStudioRead.Document) return@withLock "Não foi possível ler a versão remota; pendências preservadas"
        val source = editor.checkpoint().requireValue()
        if (!recovery.archive(source)) return@withLock "Não foi possível arquivar pendências; documento preservado"
        if (source != editor.checkpoint().requireValue()) return@withLock "Documento mudou durante o arquivo; tente novamente"
        val result = editor.discardPendingAndOpen(latest.source)
        if (!result.isSuccess) return@withLock "Versão remota rejeitada; pendências preservadas"
        workspace.refreshRemote()
        lastOwnCommandId = null
        if (!saveLatest()) "Versão remota aberta; recuperação ainda não salva, arquivo anterior preservado"
        else "Pendências arquivadas; versão remota aberta na revisão ${editor.confirmedRevision}"
    }
    companion object {
        suspend fun open(identity: RemoteDocument, catalog: NodeCatalog, api: RemoteStudioApi,
            recovery: RemoteStudioRecovery): AuthoringResult<RemoteStudioHost> {
            val saved = recovery.read()
            if (saved is RecoveryRead.Failed) return failure("REMOTE_STORAGE", identity)
            val checkpoint = (saved as RecoveryRead.Saved).source
            val result = if (checkpoint != null) RemoteEditor.restore(identity, checkpoint, catalog) else when (val remote = api.read()) {
                is RemoteStudioRead.Document -> RemoteEditor.open(identity, remote.source, catalog)
                is RemoteStudioRead.Failed -> return failure("REMOTE_${remote.reason}", identity)
            }
            val editor = result.value ?: return AuthoringResult(null, result.diagnostics)
            val workspace = LocalWorkspace(catalog = catalog)
            workspace.attachRemote(editor).requireValue()
            val host = RemoteStudioHost(editor, workspace, api, recovery)
            if (!host.saveRecovery()) return failure("REMOTE_STORAGE", identity)
            return AuthoringResult(host, emptyList())
        }
        private fun failure(code: String, identity: RemoteDocument): AuthoringResult<RemoteStudioHost> = AuthoringResult(null,
            listOf(NodeDiagnostic(code, "Remote editor could not open; recovery was not overwritten", "connectStudio", documentId = identity.document)))
    }
}
