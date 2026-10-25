package me.developes.humming.server

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.studio.*

/** Host-owned registry, never supplied by an untrusted publication payload. */
data class PublicationProfile(val id: String, val capabilities: TargetCapabilities, val journeyProvider: String? = null)
fun interface PublicationDigest { fun sha256(source: String): String }
data class StoredPublication(val key: DocumentKey, val version: String, val revision: Long, val source: String)
interface PublicationRepository {
    /** Check draft revision and insert immutable content atomically. Does not change the active pointer. */
    suspend fun publish(snapshot: StoredPublication): ApplicationResult<StoredPublication>
    suspend fun publication(key: DocumentKey, version: String): StoredPublication?
    suspend fun activePublication(key: DocumentKey): StoredPublication?
    /** Select an existing version, without rewriting a snapshot or a draft. */
    suspend fun selectPublication(key: DocumentKey, version: String): Boolean
}

class PublicationApplication(
    private val drafts: DraftRepository,
    private val publications: PublicationRepository,
    private val authorizer: DocumentAuthorizer,
    private val catalog: NodeCatalog,
    profiles: List<PublicationProfile>,
    private val digest: PublicationDigest,
) {
    private val codec = PublishedContractsCodec()
    private val profiles = profiles.map { it.copy(capabilities = it.capabilities.copy(
        definitions = it.capabilities.definitions.toMap(), methods = it.capabilities.methods.toSet(),
        providers = it.capabilities.providers.toSet(), colorTokens = it.capabilities.colorTokens.toSet(),
        maskTokens = it.capabilities.maskTokens.toSet())) }.associateBy { it.id }
    init {
        require(this.profiles.size == profiles.size)
        require(profiles.all { Regex("[a-z][a-z0-9-]{0,63}").matches(it.id) })
    }
    private fun allowed(actor: Actor, key: DocumentKey, permission: Permission) = authorizer.allows(actor, key, permission)

    suspend fun publish(actor: Actor, key: DocumentKey, expectedRevision: Long, profileId: String): ApplicationResult<StoredPublication> {
        currentCoroutineContext().ensureActive()
        if (!allowed(actor, key, Permission.PUBLISH) || !allowed(actor, key, Permission.READ)) return ApplicationResult.Failure(ApplicationError.FORBIDDEN)
        val profile = profiles[profileId] ?: return ApplicationResult.Failure(ApplicationError.UNKNOWN_PROFILE)
        if (expectedRevision < 0) return ApplicationResult.Failure(ApplicationError.INVALID_PUBLICATION)
        val draft = drafts.find(key) ?: return ApplicationResult.Failure(ApplicationError.NOT_FOUND)
        check(draft.key == key)
        if (draft.revision != expectedRevision) return ApplicationResult.Failure(ApplicationError.REVISION_CONFLICT)
        val document = AuthoringDocumentCodec(catalog).decode(draft.source).requireValue()
        check(document.documentId == key.document && document.revision == draft.revision)
        val compiler = ContractCompiler(catalog)
        val compiled = if (profile.journeyProvider == null) compiler.compileTree(document, profile.capabilities)
            else compiler.compileJourney(document, profile.capabilities, profile.journeyProvider)
        val contracts = compiled.value ?: return ApplicationResult.Failure(ApplicationError.INVALID_PUBLICATION, compiled.diagnostics)
        val required = PublicationRequirements(catalog).collect(contracts).requireValue()
        val candidate = PublishedContracts(1, "0".repeat(64), profile.id, required, contracts)
        val identity = codec.identitySource(candidate)
        val identitySource = identity.value ?: return ApplicationResult.Failure(ApplicationError.INVALID_PUBLICATION, identity.diagnostics)
        val version = digest.sha256(identitySource)
        check(Regex("[0-9a-f]{64}").matches(version)) { "Digest adapter must return lowercase SHA-256" }
        val snapshot = StoredPublication(key, version, draft.revision, codec.encode(candidate.copy(version = version)).requireValue())
        currentCoroutineContext().ensureActive()
        // Compilation and hashing are outside the database row lock. Repository rechecks the revision.
        return publications.publish(snapshot)
    }

    suspend fun read(actor: Actor, key: DocumentKey, version: String?, clientProfile: String): ApplicationResult<StoredPublication> {
        currentCoroutineContext().ensureActive()
        if (!allowed(actor, key, Permission.READ)) return ApplicationResult.Failure(ApplicationError.FORBIDDEN)
        val client = profiles[clientProfile] ?: return ApplicationResult.Failure(ApplicationError.UNKNOWN_PROFILE)
        if (version != null && !Regex("[0-9a-f]{64}").matches(version)) return ApplicationResult.Failure(ApplicationError.INVALID_PUBLICATION)
        val stored = if (version == null) publications.activePublication(key) else publications.publication(key, version)
        if (stored == null) return ApplicationResult.Failure(ApplicationError.NOT_FOUND)
        check(stored.key == key && (version == null || stored.version == version))
        val value = codec.decode(stored.source, stored.version, key.document).requireValue()
        check(value.contracts.revision == stored.revision)
        check(digest.sha256(codec.identitySource(value).requireValue()) == stored.version) { "Stored publication content hash mismatch" }
        // A corrupt or understated manifest is a storage/programming defect, not a client compatibility error.
        check(PublicationRequirements(catalog).collect(value.contracts).requireValue() == value.required)
        val errors = publicationCompatibility(value.required, client.capabilities, key.document)
        currentCoroutineContext().ensureActive()
        return if (errors.isEmpty()) ApplicationResult.Success(stored)
            else ApplicationResult.Failure(ApplicationError.INCOMPATIBLE_CLIENT, errors)
    }

    suspend fun select(actor: Actor, key: DocumentKey, version: String): ApplicationResult<String> {
        currentCoroutineContext().ensureActive()
        if (!allowed(actor, key, Permission.PUBLISH)) return ApplicationResult.Failure(ApplicationError.FORBIDDEN)
        if (!Regex("[0-9a-f]{64}").matches(version)) return ApplicationResult.Failure(ApplicationError.INVALID_PUBLICATION)
        return if (publications.selectPublication(key, version)) ApplicationResult.Success(version)
            else ApplicationResult.Failure(ApplicationError.NOT_FOUND)
    }
}
