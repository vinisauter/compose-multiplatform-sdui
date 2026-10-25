package me.developes.humming.server

import kotlinx.coroutines.test.runTest
import me.developes.humming.sdui.common.initialNodeCatalog
import me.developes.humming.sdui.studio.*
import kotlin.test.*

class PublicationApplicationTest {
    private val key = DocumentKey("tenant", "document")
    private val catalog = initialNodeCatalog()
    private class Repository : DraftRepository, PublicationRepository {
        var calls = 0
        override suspend fun create(draft: StoredDraft): Boolean = error("Unexpected write")
        override suspend fun find(key: DocumentKey): StoredDraft? { calls++; return null }
        override suspend fun publish(snapshot: StoredPublication): ApplicationResult<StoredPublication> = error("Unexpected publish")
        override suspend fun publication(key: DocumentKey, version: String): StoredPublication? { calls++; return null }
        override suspend fun activePublication(key: DocumentKey): StoredPublication? { calls++; return null }
        override suspend fun selectPublication(key: DocumentKey, version: String): Boolean { calls++; return false }
    }
    private fun app(repo: Repository) = PublicationApplication(repo, repo, ExactGrantAuthorizer, catalog,
        listOf(PublicationProfile("full", TargetCapabilities(catalog.capabilities))), PublicationDigest { error("Unexpected hash") })

    @Test fun authorizationAndProfileValidationPrecedeStorage() = runTest {
        val repo = Repository(); val app = app(repo)
        val denied = Actor("denied", emptySet())
        assertEquals(ApplicationError.FORBIDDEN, assertIs<ApplicationResult.Failure>(app.publish(denied, key, -1, "missing")).code)
        assertEquals(ApplicationError.FORBIDDEN, assertIs<ApplicationResult.Failure>(app.read(denied, key, null, "missing")).code)
        assertEquals(ApplicationError.FORBIDDEN, assertIs<ApplicationResult.Failure>(app.select(denied, key, "broken")).code)
        val onlyPublisher = Actor("publisher", setOf(DocumentGrant(key, Permission.PUBLISH)))
        assertEquals(ApplicationError.FORBIDDEN, assertIs<ApplicationResult.Failure>(app.publish(onlyPublisher, key, 0, "full")).code)
        val actor = Actor("author", Permission.entries.map { DocumentGrant(key, it) }.toSet())
        assertEquals(ApplicationError.UNKNOWN_PROFILE, assertIs<ApplicationResult.Failure>(app.publish(actor, key, 0, "missing")).code)
        assertEquals(ApplicationError.INVALID_PUBLICATION, assertIs<ApplicationResult.Failure>(app.read(actor, key, "broken", "full")).code)
        assertEquals(0, repo.calls)
    }
}
