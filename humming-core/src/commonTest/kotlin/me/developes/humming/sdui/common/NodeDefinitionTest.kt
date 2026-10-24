package me.developes.humming.sdui.common

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NodeDefinitionTest : ViewModelTestSupport() {
    private val caption = NodeDefinition("test:caption", NodeRole.COMPONENT,
        listOf(PropertyDefinition("text", required = true), PropertyDefinition("size", PropertyKind.INTEGER)))
    private val action = NodeDefinition("test:action", NodeRole.ACTION)
    private val route = NodeDefinition("test:route", NodeRole.NAVIGATION_METADATA)
    private val group = NodeDefinition("test:group", NodeRole.COMPONENT,
        listOf(PropertyDefinition("onClick", PropertyKind.NODES, nodeRole = NodeRole.ACTION)), NodeRole.COMPONENT)
    private val catalog = NodeCatalog(listOf(caption, action, route, group))

    @Test fun requiredAndMalformedPropertiesIdentifyFieldNodeAndDocument() {
        val node = parseNode("""{"component":"test:caption","id":"title","properties":{"size":"large"}}""", documentId = "fixture")
        val diagnostics = caption.parse(node).diagnostics
        assertEquals(listOf("MISSING_PROPERTY", "INVALID_PROPERTY"), diagnostics.map { it.code })
        assertEquals(listOf("$.properties[\"text\"]", "$.properties[\"size\"]"), diagnostics.map { it.path })
        assertTrue(diagnostics.all { it.nodeId == "title" && it.documentId == "fixture" })
        assertEquals("MISSING_PROPERTY", assertFailsWith<PayloadException> { caption.parse(node).requireValid() }.diagnostic.code)
    }

    @Test fun parserRetainsRawWireStringsAndNeverWritesDefaultsIntoPayload() {
        val definition = caption.copy(properties = caption.properties + PropertyDefinition("enabled", PropertyKind.BOOLEAN,
            allowsBinding = true, defaultValue = "true"))
        val node = ServerDrivenNode("test:caption", "title", mutableMapOf("text" to "#{name}", "enabled" to "#{allowed}"))
        val parsed = definition.parse(node).requireValid()
        assertEquals("#{name}", parsed.property("text"))
        assertEquals("#{allowed}", parsed.property("enabled"))
        assertEquals(null, parsed.property("size"))
        assertEquals<Map<String, String?>?>(mapOf("text" to "#{name}", "enabled" to "#{allowed}"), node.properties)
    }

    @Test fun legacyFallbackIsWarningButAuthoringRejectsInvalidValue() {
        val definition = NodeDefinition("test:caption", NodeRole.COMPONENT,
            listOf(PropertyDefinition("size", PropertyKind.FLOAT, defaultValue = "16",
                invalidPolicy = InvalidPropertyPolicy.LEGACY_FALLBACK)))
        val node = ServerDrivenNode("test:caption", "title", mutableMapOf("size" to "large"))
        assertEquals(DiagnosticSeverity.WARNING, definition.parse(node).diagnostics.single().severity)
        assertEquals("large", definition.parse(node).requireValid().property("size"))
        assertEquals(DiagnosticSeverity.ERROR, definition.parse(node, ValidationMode.AUTHORING).diagnostics.single().severity)
    }

    @Test fun nonFiniteAndOutOfRangeNumbersAreDiagnosed() {
        val definition = NodeDefinition("test:caption", NodeRole.COMPONENT,
            listOf(PropertyDefinition("size", PropertyKind.FLOAT),
                PropertyDefinition("lines", PropertyKind.INTEGER, minimum = 1.0)))
        val node = ServerDrivenNode("test:caption", "title", mutableMapOf("size" to "NaN", "lines" to "0"))
        assertEquals(listOf("INVALID_PROPERTY", "PROPERTY_RANGE"), definition.parse(node).diagnostics.map { it.code })
    }

    @Test fun actionAndMetadataAreRejectedAsVisualChildren() {
        val root = ServerDrivenNode("test:group", "group", children = mutableListOf(
            ServerDrivenNode("test:action", "action"), ServerDrivenNode("test:route", "route")))
        val errors = catalog.validate(root)
        assertEquals(listOf("INVALID_NODE_ROLE", "INVALID_NODE_ROLE"), errors.map { it.code })
        assertEquals(listOf("$.children[0].component", "$.children[1].component"), errors.map { it.path })
    }

    @Test fun eventRoleIsValidatedForObjectAndArrayForms() {
        for (source in listOf("""{"component":"test:caption","properties":{"text":"wrong role"}}""",
            """[{"component":"test:caption","properties":{"text":"wrong role"}}]""")) {
            val root = ServerDrivenNode("test:group", "group", mutableMapOf("onClick" to source))
            assertEquals("INVALID_NODE_ROLE", catalog.validate(root).single().code)
        }
    }

    @Test fun unknownSchemasAndPropertiesAreRetainedOnlyInLegacyMode() {
        val unknown = ServerDrivenNode("custom:component", "custom")
        assertEquals(DiagnosticSeverity.WARNING, catalog.validate(unknown).single().severity)
        assertEquals(DiagnosticSeverity.ERROR, catalog.validate(unknown, mode = ValidationMode.AUTHORING).single().severity)
        val node = ServerDrivenNode("test:caption", "title", mutableMapOf("text" to "ok", "future" to "kept"))
        assertEquals("kept", caption.parse(node).requireValid().property("future"))
        assertEquals("UNMODELED_PROPERTY", caption.parse(node, ValidationMode.AUTHORING).diagnostics.single().code)
    }

    @Test fun traversalBudgetStopsProgrammaticCycles() {
        val root = ServerDrivenNode("test:group", "group", children = mutableListOf())
        root.children!!.add(root)
        assertEquals("SCHEMA_BUDGET", catalog.validate(root, maxDepth = 4).single().code)
    }

    @Test fun traversalBudgetCountsSiblingsAndEventsTogether() {
        val root = ServerDrivenNode("test:group", "group", mutableMapOf("onClick" to """{"component":"test:action"}"""),
            mutableListOf(ServerDrivenNode("test:group", "child")))
        assertEquals("SCHEMA_BUDGET", catalog.validate(root, maxNodes = 2).single().code)
    }

    @Test fun catalogRejectsDuplicatesAndLibraryRejectsForeignNamespace() {
        assertFailsWith<IllegalArgumentException> { NodeCatalog(listOf(caption, caption)) }
        assertFailsWith<IllegalArgumentException> { SDLibrary("other").addDefinition(caption) }
        val library = SDLibrary("test").addDefinition(caption)
        assertFailsWith<IllegalArgumentException> { library.addDefinition(caption) }
        assertEquals(mapOf("test:caption" to 1), NodeCatalog(library.definitions).capabilities)
    }

    @Test fun runtimeRejectsWrongRoleBeforeInvokingHandler() = runTest {
        var called = false
        val library = SDLibrary("test").addDefinition(action).addComponent("action") { _, _ ->
            called = true
            error("Must not be invoked")
        }
        val vm = viewModel(library)
        assertEquals(null, vm.loadComponent(ServerDrivenNode("test:action", "wrong")))
        assertEquals(false, called)
        assertEquals("INVALID_NODE_ROLE", vm.errorState.value?.diagnostic?.code)
    }

    @Test fun customLibrariesWithoutDefinitionsKeepLegacyDispatch() = runTest {
        val vm = viewModel(SDLibrary("custom").addAction("run") { _, state -> state.updateState("result", "ok") })
        assertTrue(vm.executeActions(listOf(ServerDrivenNode("custom:run", "run"))).isSuccess)
        assertEquals("ok", vm.formState["result"])
        assertNotNull(vm.catalog())
    }
}
