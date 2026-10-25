package me.developes.humming.server

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.studio.*
import kotlin.test.*

class DraftApplicationTest {
    private val catalog = initialNodeCatalog()
    private val codec = AuthoringDocumentCodec(catalog)
    private val key = DocumentKey("tenant-a", "document")
    private fun actor(vararg permissions: Permission) = Actor("author", permissions.map { DocumentGrant(key, it) }.toSet())
    private fun source(id: String = key.document, revision: Long = 0): String {
        val document = LegacyAuthoringCodec(catalog).importDocument(
            """{"id":"label","component":"layout:text","properties":{"text":"Hello"}}""", id).requireValue().copy(revision = revision)
        return codec.encode(document).requireValue()
    }
    private class Repository : DraftRepository {
        val drafts = mutableMapOf<DocumentKey, StoredDraft>()
        var calls = 0
        override suspend fun create(draft: StoredDraft): Boolean { calls++; if (draft.key in drafts) return false; drafts[draft.key] = draft; return true }
        override suspend fun find(key: DocumentKey): StoredDraft? { calls++; return drafts[key] }
    }

    @Test fun createReadValidateAndConflictPreserveCanonicalDefinition() = runTest {
        val repository = Repository()
        val app = DraftApplication(repository, ExactGrantAuthorizer, catalog, TargetCapabilities(catalog.capabilities))
        val author = actor(*Permission.entries.toTypedArray())
        assertIs<ApplicationResult.Success<StoredDraft>>(app.create(author, key, source()))
        val read = assertIs<ApplicationResult.Success<StoredDraft>>(app.read(author, key)).value
        assertEquals(key, read.key)
        assertEquals("Hello", codec.decode(read.source).requireValue().nodes.values.single().properties["text"]?.let { (it as kotlinx.serialization.json.JsonPrimitive).content })
        assertTrue(assertIs<ApplicationResult.Success<DraftValidation>>(app.validate(author, key)).value.valid)
        assertEquals(ApplicationError.ALREADY_EXISTS, assertIs<ApplicationResult.Failure>(app.create(author, key, source())).code)
        assertEquals(read, repository.drafts[key])
    }

    @Test fun authorizationPrecedesParsingAndRepositoryAndDoesNotTrustTenantAlone() = runTest {
        val repository = Repository()
        val app = DraftApplication(repository, ExactGrantAuthorizer, catalog, TargetCapabilities(catalog.capabilities))
        for (target in listOf(key, key.copy(document = "other"), key.copy(tenant = "tenant-b"))) {
            val denied = actor(Permission.READ)
            assertEquals(ApplicationError.FORBIDDEN, assertIs<ApplicationResult.Failure>(app.create(denied, target, "broken json")).code)
            assertEquals(ApplicationError.FORBIDDEN, assertIs<ApplicationResult.Failure>(app.validate(denied, target)).code)
        }
        assertEquals(ApplicationError.FORBIDDEN, assertIs<ApplicationResult.Failure>(app.read(actor(Permission.READ), key.copy(tenant = "tenant-b"))).code)
        assertEquals(0, repository.calls)
        assertEquals(ApplicationError.NOT_FOUND, assertIs<ApplicationResult.Failure>(app.read(actor(Permission.READ), key)).code)
    }

    @Test fun invalidSourceIdentityRevisionAndCompilerDiagnosticsRemainDistinct() = runTest {
        val repository = Repository()
        val app = DraftApplication(repository, ExactGrantAuthorizer, catalog, TargetCapabilities(emptyMap()))
        val author = actor(*Permission.entries.toTypedArray())
        val malformed = assertIs<ApplicationResult.Failure>(app.create(author, key, "{"))
        assertEquals(ApplicationError.INVALID_DOCUMENT, malformed.code)
        assertTrue(malformed.diagnostics.isNotEmpty())
        assertEquals(ApplicationError.ID_MISMATCH, assertIs<ApplicationResult.Failure>(app.create(author, key, source("other"))).code)
        assertEquals(ApplicationError.INITIAL_REVISION, assertIs<ApplicationResult.Failure>(app.create(author, key, source(revision = 1))).code)
        assertEquals(0, repository.calls)
        app.create(author, key, source())
        val result = assertIs<ApplicationResult.Success<DraftValidation>>(app.validate(author, key)).value
        assertFalse(result.valid)
        assertTrue(result.diagnostics.any { it.code == "UNSUPPORTED_CAPABILITY" && it.nodeId == "label" })
    }

    @Test fun cancellationAndBrokenRepositoryAreNotInvalidPayloads() = runTest {
        val entered = CompletableDeferred<Unit>()
        val cleaned = CompletableDeferred<Unit>()
        val repository = object : DraftRepository {
            override suspend fun create(draft: StoredDraft): Boolean { entered.complete(Unit); try { awaitCancellation() } finally { cleaned.complete(Unit) } }
            override suspend fun find(key: DocumentKey): StoredDraft = error("database bug")
        }
        val app = DraftApplication(repository, ExactGrantAuthorizer, catalog, TargetCapabilities(catalog.capabilities))
        val author = actor(*Permission.entries.toTypedArray())
        val pending = launch { app.create(author, key, source()) }
        entered.await(); pending.cancelAndJoin()
        assertTrue(cleaned.isCompleted)
        assertFailsWith<IllegalStateException> { app.read(author, key) }
    }
}
