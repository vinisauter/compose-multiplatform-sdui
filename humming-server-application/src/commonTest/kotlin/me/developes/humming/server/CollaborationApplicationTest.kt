package me.developes.humming.server

import kotlinx.coroutines.test.runTest
import me.developes.humming.sdui.studio.*
import kotlin.test.*

class CollaborationApplicationTest {
    @Test fun feedPresenceAndUndoAuthorizeBeforeStorageAndMalformedCommandsNeverReachIt() = runTest {
        var calls = 0
        val repository = object : CollaborationRepository {
            override suspend fun updates(key: DocumentKey, afterRevision: Long): DraftUpdateFeed? { calls++; return null }
            override suspend fun heartbeat(key: DocumentKey, actorId: String): DraftPresenceList? { calls++; return null }
            override suspend fun leave(key: DocumentKey, actorId: String) { calls++ }
            override suspend fun undo(key: DocumentKey, actorId: String, canonicalCommand: String): DraftEditResult { calls++; error("unexpected") }
        }
        val app = CollaborationApplication(repository, ExactGrantAuthorizer)
        val key = DocumentKey("tenant", "document")
        val reader = Actor("reader", setOf(DocumentGrant(key, Permission.READ)))
        assertEquals(ApplicationError.FORBIDDEN, assertIs<ApplicationResult.Failure>(app.heartbeat(reader, key)).code)
        assertEquals(ApplicationError.FORBIDDEN, assertIs<ApplicationResult.Failure>(app.leave(reader, key)).code)
        assertEquals(ApplicationError.FORBIDDEN, assertIs<DraftEditResult.Rejected>(app.undo(reader, key, "broken")).code)
        assertEquals(ApplicationError.FORBIDDEN, assertIs<ApplicationResult.Failure>(app.updates(reader, key.copy(tenant = "other"), 0)).code)
        assertEquals(ApplicationError.INVALID_COMMAND, assertIs<ApplicationResult.Failure>(app.updates(reader, key, -1)).code)
        val author = Actor("author", reader.grants + DocumentGrant(key, Permission.EDIT))
        assertEquals(ApplicationError.INVALID_COMMAND, assertIs<DraftEditResult.Rejected>(app.undo(author, key, "{broken")).code)
        assertEquals(0, calls)
        assertEquals(ApplicationError.NOT_FOUND, assertIs<ApplicationResult.Failure>(app.updates(reader, key, 0)).code)
        assertEquals(1, calls)
    }
}
