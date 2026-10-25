package me.developes.humming.server

import kotlinx.coroutines.test.runTest
import kotlin.test.*

class DraftEditingApplicationTest {
    @Test fun authorizationPrecedesDecodeAndEquivalentObjectOrderHasOneCanonicalCommand() = runTest {
        val key = DocumentKey("tenant", "document")
        val commands = mutableListOf<String>()
        val repository = object : RevisionedDraftRepository {
            override suspend fun create(draft: StoredDraft) = false
            override suspend fun find(key: DocumentKey): StoredDraft? = null
            override suspend fun edit(key: DocumentKey, canonicalCommand: String): DraftEditResult {
                commands += canonicalCommand
                return DraftEditResult.Rejected(ApplicationError.NOT_FOUND)
            }
        }
        val app = DraftEditingApplication(repository, ExactGrantAuthorizer)
        assertEquals(ApplicationError.FORBIDDEN, (app.edit(Actor("reader", emptySet()), key, "{") as DraftEditResult.Rejected).code)
        assertTrue(commands.isEmpty())
        val editOnly = Actor("writer", setOf(DocumentGrant(key, Permission.EDIT)))
        assertEquals(ApplicationError.FORBIDDEN, (app.edit(editOnly, key, "{") as DraftEditResult.Rejected).code)
        val actor = Actor("author", setOf(DocumentGrant(key, Permission.EDIT), DocumentGrant(key, Permission.READ)))
        assertEquals(ApplicationError.INVALID_COMMAND, (app.edit(actor, key, "{") as DraftEditResult.Rejected).code)
        val a = """{"commandId":"one","expectedRevision":0,"operations":[{"operationType":"putProperty","nodeId":"label","name":"text","value":"one"}]}"""
        val b = """{"operations":[{"value":"one","name":"text","nodeId":"label","operationType":"putProperty"}],"expectedRevision":0,"commandId":"one"}"""
        app.edit(actor, key, a); app.edit(actor, key, b)
        assertEquals(2, commands.size)
        assertEquals(commands.first(), commands.last())
    }
}
