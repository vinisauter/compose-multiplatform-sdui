package me.developes.humming.sdui.studio

import kotlinx.serialization.json.*
import me.developes.humming.sdui.common.*
import kotlin.test.*

class PublishedContractsTest {
    private val catalog = initialNodeCatalog()
    private val codec = PublishedContractsCodec()
    private fun publication(): PublishedContracts {
        val document = LegacyAuthoringCodec(catalog).importDocument(
            """{"id":"label","component":"layout:reactiveText","properties":{"colorToken":"positive","value":{"version":1,"expression":{"language":"humming.expression","version":1,"source":"state.name ?? \"anonymous\""},"fallback":"fallback"}}}""", "document").requireValue()
        val contracts = ContractCompiler(catalog).compileTree(document, TargetCapabilities(catalog.capabilities,
            colorTokens = setOf("positive"))).requireValue()
        return PublishedContracts(1, "a".repeat(64), "full", PublicationRequirements(catalog).collect(contracts).requireValue(), contracts)
    }

    @Test fun requiredVersionsAndTokensAreDerivedOnlyFromActualArtifacts() {
        val publication = publication()
        assertEquals(mapOf("layout:reactiveText" to 2), publication.required.definitions)
        assertEquals(setOf("positive"), publication.required.colorTokens)
        val old = TargetCapabilities(mapOf("layout:reactiveText" to 1, "layout:validatedTextField" to 2), colorTokens = setOf("positive"))
        val errors = publicationCompatibility(publication.required, old, "document")
        assertEquals(1, errors.size)
        assertTrue(errors.single().path.contains("layout:reactiveText"))
        assertTrue(publicationCompatibility(publication.required, publication.required, "document").isEmpty())
        assertEquals(1, publicationCompatibility(publication.required, publication.required.copy(colorTokens = emptySet()), "document").size)
    }

    @Test fun closedEnvelopePinsIdentityAndDoesNotSerializeSessionFormOrServerData() {
        val value = publication()
        val source = codec.encode(value).requireValue()
        val decoded = codec.decode(source, value.version, "document").requireValue()
        assertEquals(value, decoded)
        assertEquals(source, codec.encode(decoded).requireValue())
        assertFalse(codec.decode(source, "b".repeat(64), "document").isSuccess)
        assertFalse(codec.decode(source, value.version, "other").isSuccess)
        val objectValue = Json.parseToJsonElement(source).jsonObject
        assertEquals(setOf("formatVersion", "version", "profile", "required", "contracts"), objectValue.keys)
        assertFalse(codec.decode(JsonObject(objectValue + ("state" to buildJsonObject { put("name", "private") })).toString()).isSuccess)
        assertFalse(codec.decode(source.replace("\"formatVersion\":1", "\"formatVersion\":2")).isSuccess)
        assertEquals(codec.identitySource(value).requireValue(), codec.identitySource(value.copy(version = "b".repeat(64))).requireValue())
    }

    @Test fun nestedActionRequirementsAreNotMissed() {
        val source = """{"id":"root","component":"layout:column","children":[{"id":"button","component":"layout:button","properties":{"onClick":{"id":"call","component":"action:method","properties":{"invoke":"known"}}}}]}"""
        val document = LegacyAuthoringCodec(catalog).importDocument(source, "document").requireValue()
        val contracts = ContractCompiler(catalog).compileTree(document, TargetCapabilities(catalog.capabilities, methods = setOf("known"))).requireValue()
        val required = PublicationRequirements(catalog).collect(contracts).requireValue()
        assertEquals(setOf("known"), required.methods)
        assertEquals(setOf("layout:column", "layout:button", "action:method"), required.definitions.keys)
    }

    @Test fun generatedNavigationRequiresItsRegisteredProvider() {
        val document = LegacyAuthoringCodec(catalog).importDocument(
            """{"id":"root","component":"layout:text","properties":{"text":"Hello"}}""", "document").requireValue()
        val contracts = ContractCompiler(catalog).compileJourney(document,
            TargetCapabilities(catalog.capabilities, providers = setOf("published")), "published").requireValue()
        val required = PublicationRequirements(catalog).collect(contracts).requireValue()
        assertEquals(setOf("published"), required.providers)
        assertTrue(required.definitions.keys.containsAll(setOf("navigation:graph", "navigation:node", "layout:text")))
        assertEquals(1, publicationCompatibility(required, required.copy(providers = emptySet()), "document").size)
    }

    @Test fun pinnedDefinitionChecksManifestAndDoesNotShareMutableNodes() {
        val publication = publication()
        val source = codec.encode(publication).requireValue()
        val pinned = PublishedDefinition.decode(source, publication.version, "document", catalog, publication.required).requireValue()
        assertNotSame(pinned.node(pinned.entryDestination), pinned.node(pinned.entryDestination))
        assertFailsWith<PayloadException> { pinned.node("../other") }
        assertFalse(PublishedDefinition.decode(source, publication.version, "document", catalog, TargetCapabilities(emptyMap())).isSuccess)
        val understated = codec.encode(publication.copy(required = TargetCapabilities(emptyMap()))).requireValue()
        assertEquals("PUBLISHED_MANIFEST", PublishedDefinition.decode(understated, publication.version, "document", catalog, publication.required).diagnostics.single().code)
    }
}
