package me.developes.humming.sdui.studio

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.developes.humming.sdui.common.PropertyKind
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.common.initialNodeCatalog
import me.developes.humming.sdui.common.parseNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ContractCompilerTest {
    private val catalog = initialNodeCatalog()
    private val compiler = ContractCompiler(catalog)
    private val target = TargetCapabilities(catalog.capabilities, methods = setOf("registered"), providers = setOf("compiled", "local"))
    private val source = """{"component":"layout:column","id":"root","properties":{"paddingAll":16},"children":[{"component":"layout:text","id":"status","properties":{"text":"#{status}"}},{"component":"layout:button","id":"button","properties":{"onClick":[{"component":"action:update","id":"set","properties":{"state":"status","value":"ok"}}]},"children":[{"component":"layout:text","id":"label","properties":{"text":"Confirmar"}}]}]}"""
    private fun imported(source: String = this.source) = LegacyAuthoringCodec(catalog).importDocument(source, "compile").requireValue()

    private fun journey(): AuthoringDocument {
        fun screen(id: String, actionId: String) = imported("""{"component":"layout:column","id":"$id","children":[{"component":"layout:text","id":"title$id","properties":{"text":"Tela $id"}},{"component":"layout:button","id":"button$id","properties":{"onClick":{"component":"navigation:goTo","id":"$actionId","properties":{"destiny":"placeholder"}}},"children":[{"component":"layout:text","id":"label$id","properties":{"text":"Continuar"}}]}]}""")
        val a = screen("A", "goAB")
        val b = screen("B", "goBA")
        val combined = (a.nodes + b.nodes).mapValues { (_, node) ->
            if (node.definition == "navigation:goTo") node.copy(properties = JsonObject(emptyMap())) else node
        }.toMutableMap()
        combined.values.filter { it.placement is Placement.Root }.sortedBy { it.id }.forEachIndexed { index, node ->
            combined[node.id] = node.copy(placement = Placement.Root(importOrderKey(index)))
        }
        val edges = (a.edges.values.map { it.copy(id = "a-${it.id}") } + b.edges.values.map { it.copy(id = "b-${it.id}") } +
            listOf(AuthorEdge("nav-ab", EdgeRole.NAVIGATION, "goAB", "navigate", "B", "entry"),
                AuthorEdge("nav-ba", EdgeRole.NAVIGATION, "goBA", "navigate", "A", "entry"))).associateBy { it.id }
        return AuthoringDocument("journey", entryNodeId = "A", nodes = combined, edges = edges)
    }

    @Test fun treeGoldenPreservesLegacyNamesPropertiesBindingsAndArrayOrder() {
        val doc = imported()
        val result = compiler.compileTree(doc, target).requireValue()
        assertEquals("screen.json", result.entryDestination)
        assertNull(result.providerType)
        assertEquals(Json.parseToJsonElement(source), Json.parseToJsonElement(result.artifacts.getValue("screen.json")))
        assertEquals(doc.nodes.keys.associateWith { it }, result.sourceNodeIds)
    }

    @Test fun navigationCyclesCompileToExistingGraphAndGoToContract() {
        val result = compiler.compileJourney(journey(), target, "compiled").requireValue()
        assertEquals(setOf("graph.json", "screens/41.json", "screens/42.json"), result.artifacts.keys)
        val graph = parseNode(result.artifacts.getValue("graph.json"))
        assertEquals("navigation:graph", graph.component)
        assertEquals("screen-41", graph.property("startDestination"))
        assertEquals(listOf("screen-41", "screen-42"), graph.children!!.map { it.property("name") })
        assertTrue(graph.children!!.all { it.property("type") == "compiled" })
        val a = parseNode(result.artifacts.getValue("screens/41.json"))
        val b = parseNode(result.artifacts.getValue("screens/42.json"))
        assertEquals("screen-42", a.children!![1].propertyNodes("onClick").single().property("destiny"))
        assertEquals("screen-41", b.children!![1].propertyNodes("onClick").single().property("destiny"))
    }

    @Test fun compilationIsDeterministicAcrossShuffledNodeAndEdgeMaps() {
        val doc = journey()
        val shuffled = doc.copy(nodes = doc.nodes.entries.reversed().associate { it.toPair() }, edges = doc.edges.entries.reversed().associate { it.toPair() })
        assertEquals(compiler.compileJourney(doc, target, "compiled"), compiler.compileJourney(shuffled, target, "compiled"))
    }

    @Test fun missingOrDifferentSchemaVersionsAreRejectedByIdentifier() {
        val missing = compiler.compileTree(imported(), target.copy(definitions = target.definitions - "action:update"))
        assertNull(missing.value)
        assertTrue(missing.diagnostics.any { it.code == "UNSUPPORTED_CAPABILITY" && it.nodeId == "set" })
        assertTrue(compiler.compileTree(imported(), target.copy(definitions = target.definitions + ("layout:text" to 2)))
            .diagnostics.any { it.code == "UNSUPPORTED_CAPABILITY" })
    }

    @Test fun methodsAndProvidersMustBeExplicitlyDeclaredByTheTarget() {
        val method = imported("""{"component":"layout:button","id":"root","properties":{"onClick":{"component":"action:method","id":"call","properties":{"invoke":"registered"}}}}""")
        assertTrue(compiler.compileTree(method, target.copy(methods = emptySet())).diagnostics.any { it.code == "UNSUPPORTED_METHOD" })
        assertTrue(compiler.compileTree(method, target).isSuccess)
        assertTrue(compiler.compileJourney(journey(), target.copy(providers = emptySet()), "compiled").diagnostics.any { it.code == "UNSUPPORTED_PROVIDER" })
    }

    @Test fun unknownFieldsExtensionsAndEdgeMetadataAreNotSilentlyDropped() {
        val doc = imported()
        val root = doc.nodes.getValue("root")
        val extraProperty = doc.copy(nodes = doc.nodes + (root.id to root.copy(properties = JsonObject(root.properties + ("unknown" to JsonPrimitive(1))))))
        assertTrue(compiler.compileTree(extraProperty, target).diagnostics.any { it.code == "UNMODELED_PROPERTY" && it.nodeId == "root" })
        val extension = doc.copy(nodes = doc.nodes + (root.id to root.copy(extensions = JsonObject(mapOf("vendor" to JsonPrimitive(true))))))
        assertTrue(compiler.compileTree(extension, target).diagnostics.any { it.code == "UNSUPPORTED_EXTENSION" })
        val edge = doc.edges.values.single()
        val metadata = doc.copy(edges = doc.edges + (edge.id to edge.copy(metadata = JsonObject(mapOf("transition" to JsonPrimitive("ease"))))))
        assertTrue(compiler.compileTree(metadata, target).diagnostics.any { it.code == "UNSUPPORTED_EDGE_METADATA" })
    }

    @Test fun disconnectedNodesRequireAnExplicitReportingPolicy() {
        val doc = imported()
        val extra = AuthorNode("extra", AuthorNodeKind.VISUAL, "layout:text", JsonObject(mapOf("text" to JsonPrimitive("unused"))), Placement.Root("z"))
        val disconnected = doc.copy(nodes = doc.nodes + (extra.id to extra))
        assertFalse(compiler.compileTree(disconnected, target).isSuccess)
        val reported = compiler.compileTree(disconnected, target, unreachable = UnreachablePolicy.REPORT)
        assertTrue(reported.isSuccess)
        assertTrue(reported.diagnostics.any { it.code == "UNREACHABLE_NODE" })
        assertTrue("extra" in disconnected.nodes)
    }

    @Test fun sharedActionsExpandWithUniqueDeterministicIdsAndSourceMapping() {
        val doc = imported("""{"component":"layout:column","id":"root","children":[{"component":"layout:button","id":"one","properties":{"onClick":{"component":"action:update","id":"shared","properties":{"state":"x","value":"1"}}}},{"component":"layout:button","id":"two","properties":{"onClick":{"component":"action:update","id":"other","properties":{"state":"x","value":"1"}}}},{"component":"layout:text","id":"__expanded:shared:2","properties":{"text":"Reserved collision"}}]}""")
        val second = doc.edges.values.single { it.sourceNodeId == "two" }
        val shared = doc.copy(nodes = doc.nodes - "other", edges = doc.edges + (second.id to second.copy(targetNodeId = "shared")))
        val result = compiler.compileTree(shared, target).requireValue()
        assertEquals(2, result.sourceNodeIds.values.count { it == "shared" })
        assertEquals("shared", result.sourceNodeIds["__expanded:shared:3"])
        assertEquals("__expanded:shared:2", result.sourceNodeIds["__expanded:shared:2"])
        fun ids(node: ServerDrivenNode): List<String> = listOf(node.id) + node.children.orEmpty().flatMap { ids(it) } +
            catalog[node.component]!!.properties.filter { it.kind == PropertyKind.NODES }.flatMap { property ->
                node.propertyNodes(property.name).flatMap { ids(it) }
            }
        val emitted = ids(parseNode(result.artifacts.getValue("screen.json")))
        assertEquals(emitted.size, emitted.toSet().size)
        assertEquals(result, compiler.compileTree(shared, target).requireValue())
    }

    @Test fun outputExpansionAndArtifactBudgetsReturnNoPartialBundle() {
        for (limit in listOf(CompilationLimits(maxExpandedNodes = 1), CompilationLimits(maxOutputChars = 20), CompilationLimits(maxDepth = 1))) {
            val result = ContractCompiler(catalog, limits = limit).compileTree(imported(), target)
            assertNull(result.value)
            assertTrue(result.diagnostics.any { it.code == "COMPILATION_BUDGET" })
        }
        assertNull(ContractCompiler(catalog, limits = CompilationLimits(maxArtifacts = 2)).compileJourney(journey(), target, "compiled").value)
    }

    @Test fun explicitNavigationIsAuthoritativeAndCannotCoexistWithRawDestiny() {
        val doc = journey()
        val action = doc.nodes.getValue("goAB")
        val conflicting = doc.copy(nodes = doc.nodes + (action.id to action.copy(properties = JsonObject(mapOf("destiny" to JsonPrimitive("legacy"))))))
        assertTrue(compiler.compileJourney(conflicting, target, "compiled").diagnostics.any { it.code == "DUPLICATE_NAVIGATION_REFERENCE" })
        assertTrue(compiler.compileTree(doc, target).diagnostics.any { it.code == "NAVIGATION_REQUIRES_JOURNEY" })
    }

    @Test fun existingLegacyGraphCompilesOnlyWithItsRegisteredProviders() {
        val graphSource = """{"component":"navigation:graph","id":"graph","properties":{"startDestination":"start"},"children":[{"component":"navigation:node","id":"route","properties":{"name":"start","type":"local","destiny":"start.json"}}]}"""
        val graph = imported(graphSource)
        assertEquals(Json.parseToJsonElement(graphSource), Json.parseToJsonElement(compiler.compileTree(graph, target).requireValue().artifacts.getValue("screen.json")))
        assertTrue(compiler.compileTree(graph, target.copy(providers = emptySet())).diagnostics.any { it.code == "UNSUPPORTED_PROVIDER" })
        assertTrue(compiler.compileJourney(graph, target, "compiled").diagnostics.any { it.code == "NESTED_NAV_GRAPH" })
    }

    @Test fun invalidPortsNeverProduceContracts() {
        val doc = imported()
        val edge = doc.edges.values.single()
        val invalid = doc.copy(edges = doc.edges + (edge.id to edge.copy(sourcePort = "missing")))
        assertNull(compiler.compileTree(invalid, target).value)
        assertTrue(compiler.compileTree(invalid, target).diagnostics.any { it.code == "INVALID_EDGE_PORT" })
    }

    @Test fun actionFlowCycleFailsBeforeProducingAnyArtifact() {
        val root = AuthorNode("root", AuthorNodeKind.SCREEN, "layout:column", JsonObject(emptyMap()), Placement.Root("V"))
        val a = AuthorNode("a", AuthorNodeKind.ACTION, "action:then", JsonObject(emptyMap()), Placement.Root("a"))
        val b = a.copy(id = "b", placement = Placement.Root("b"))
        val edges = listOf(AuthorEdge("ab", EdgeRole.ACTION_FLOW, "a", "invoke", "b", "input", "V"),
            AuthorEdge("ba", EdgeRole.ACTION_FLOW, "b", "invoke", "a", "input", "V"))
        val doc = AuthoringDocument("cycle", entryNodeId = root.id, nodes = listOf(root, a, b).associateBy { it.id }, edges = edges.associateBy { it.id })
        val result = compiler.compileTree(doc, target)
        assertNull(result.value)
        assertTrue(result.diagnostics.any { it.code == "ACTION_CYCLE" })
    }
}
