package me.developes.humming.sdui.studio

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import me.developes.humming.sdui.common.DiagnosticSeverity
import me.developes.humming.sdui.common.initialNodeCatalog
import me.developes.humming.sdui.common.PayloadLimits
import me.developes.humming.sdui.common.parseNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthoringDocumentTest {
    private val catalog = initialNodeCatalog()
    private val legacy = LegacyAuthoringCodec(catalog)
    private val codec = AuthoringDocumentCodec(catalog)
    private val source = """{
      "component":"layout:column","id":"root","vendorExtension":{"retained":true},
      "properties":{"paddingAll":16,"future":{"nested":[true,null,"x"]}},"children":[
        {"component":"layout:textField","id":"field","properties":{"text":"Initial","onChangeUpdateState":"name","label":{"component":"layout:text","id":"label","properties":{"text":"Nome"}},"onDone":[]}},
        {"component":"layout:button","id":"button","properties":{"onClick":[{"component":"action:ifState","id":"condition","properties":{"state":"ready","then":{"component":"action:method","id":"method","properties":{"invoke":"commit","extra":[12,false,null]}},"else":[]}}]},"children":[{"component":"layout:text","id":"caption","properties":{"text":"Salvar"}}]}
      ]
    }""".trimIndent()

    private fun document() = legacy.importDocument(source, "fixture").requireValue()
    private fun withNode(document: AuthoringDocument, node: AuthorNode) = document.copy(nodes = document.nodes + (node.id to node))
    private fun codes(document: AuthoringDocument) = validateAuthoringDocument(document, catalog).map { it.code }

    @Test fun legacyRoundTripPreservesIdsOrderRawPropertiesAndRepresentations() {
        val result = legacy.importDocument(source, "fixture")
        assertTrue(result.isSuccess)
        val document = result.requireValue()
        assertEquals(7, document.nodes.size)
        assertEquals(2, document.edges.size)
        assertEquals(Json.parseToJsonElement(source), Json.parseToJsonElement(legacy.exportDocument(document).requireValue()))
        assertTrue(result.diagnostics.any { it.code == "UNMODELED_PROPERTY" && it.severity == DiagnosticSeverity.WARNING })
    }

    @Test fun placementsAndEdgesAreTheOnlyRelationAuthority() {
        val doc = document()
        val indices = AuthoringIndices(doc)
        assertEquals(listOf("field", "button"), indices.childrenByParentAndSlot.getValue(ParentSlot("root", "children")).map { it.id })
        assertEquals(listOf("label"), indices.childrenByParentAndSlot.getValue(ParentSlot("field", "label")).map { it.id })
        assertEquals(listOf("condition"), indices.edgesBySourcePort.getValue(SourcePort("button", "onClick")).map { it.targetNodeId })
        assertEquals(EdgeRole.ACTION_FLOW, indices.incomingEdges.getValue("method").single().role)
        assertTrue(doc.nodes.getValue("condition").placement is Placement.Root)
        assertFalse("label" in doc.nodes.getValue("field").properties)
        assertFalse("onClick" in doc.nodes.getValue("button").properties)
    }

    @Test fun authoringCodecIsDeterministicAcrossMapAndPropertyInsertionOrder() {
        val doc = document()
        val reordered = doc.copy(nodes = doc.nodes.entries.reversed().associate { (id, node) ->
            id to node.copy(properties = JsonObject(node.properties.entries.reversed().associate { it.toPair() }))
        }, edges = doc.edges.entries.reversed().associate { it.toPair() })
        assertEquals(codec.encode(doc).requireValue(), codec.encode(reordered).requireValue())
        assertEquals(legacy.exportDocument(doc).requireValue(), legacy.exportDocument(reordered).requireValue())
        assertEquals(doc, codec.decode(codec.encode(doc).requireValue()).requireValue())
    }

    @Test fun exportReadsEditedAuthorPropertiesInsteadOfLegacyToString() {
        val doc = document()
        val field = doc.nodes.getValue("field")
        val edited = withNode(doc, field.copy(properties = JsonObject(field.properties + ("text" to JsonPrimitive("Edited")))))
        val output = parseNode(legacy.exportDocument(edited).requireValue())
        assertEquals("Edited", output.children!![0].property("text"))
        assertEquals("Initial", doc.nodes.getValue("field").properties.getValue("text").let { (it as JsonPrimitive).content })
    }

    @Test fun generatedIdsArePersistedAndStableAfterImport() {
        val doc = legacy.importDocument("""{"component":"layout:column","children":[{"component":"layout:text","properties":{"text":"x"}}]}""", "generated").requireValue()
        val restored = codec.decode(codec.encode(doc).requireValue()).requireValue()
        assertEquals(doc.nodes.keys, restored.nodes.keys)
        val again = legacy.importDocument(legacy.exportDocument(restored).requireValue(), "generated").requireValue()
        assertEquals(doc.nodes.keys, again.nodes.keys)
    }

    @Test fun repeatedLegacyIdsAreRejectedWithoutReturningPartialDocument() {
        val result = legacy.importDocument("""{"component":"layout:column","id":"same","children":[{"component":"layout:text","id":"same","properties":{"text":"x"}}]}""", "duplicate")
        assertNull(result.value)
        assertTrue(result.diagnostics.any { it.code == "DUPLICATE_NODE_ID" })
    }

    @Test fun duplicateNodeAndEdgeIdsInSerializedListsAreDetectedBeforeMapConstruction() {
        val encoded = Json.parseToJsonElement(codec.encode(document()).requireValue()).jsonObject
        for ((field, code) in listOf("nodes" to "DUPLICATE_NODE_ID", "edges" to "DUPLICATE_EDGE_ID")) {
            val list = encoded.getValue(field).jsonArray
            val corrupt = JsonObject(encoded + (field to JsonArray(list + list.first())))
            val result = codec.decode(corrupt.toString())
            assertNull(result.value)
            assertEquals(code, result.diagnostics.single().code)
        }
    }

    @Test fun missingParentAndIncorrectMapKeyAreRejected() {
        val doc = document()
        val field = doc.nodes.getValue("field")
        assertTrue("MISSING_PARENT" in codes(withNode(doc, field.copy(placement = Placement.Child("absent", "children", "V")))))
        assertTrue("NODE_ID" in codes(doc.copy(nodes = doc.nodes + ("wrong" to field))))
        assertTrue("DUPLICATE_NODE_ID" in codes(doc.copy(nodes = doc.nodes + ("wrong" to field))))
    }

    @Test fun containmentCyclesAndInvalidSlotsAreRejected() {
        val doc = document()
        val root = doc.nodes.getValue("root")
        val field = doc.nodes.getValue("field")
        assertTrue("CONTAINMENT_CYCLE" in codes(withNode(doc, root.copy(placement = Placement.Child("field", "children", "V")))))
        assertTrue("INVALID_SLOT" in codes(withNode(doc, field.copy(placement = Placement.Child("button", "onClick", "V")))))
    }

    @Test fun duplicateSiblingKeysAndInvalidKeyAlphabetAreRejected() {
        val doc = document()
        val field = doc.nodes.getValue("field")
        val button = doc.nodes.getValue("button")
        assertTrue("DUPLICATE_ORDER_KEY" in codes(withNode(doc, button.copy(placement = field.placement))))
        assertTrue("ORDER_KEY" in codes(withNode(doc, field.copy(placement = Placement.Child("root", "children", "invalid-0")))))
    }

    @Test fun missingEdgeEndpointsAndUndeclaredPortsAreRejected() {
        val doc = document()
        val edge = doc.edges.values.first()
        assertTrue("MISSING_EDGE_NODE" in codes(doc.copy(edges = doc.edges + (edge.id to edge.copy(targetNodeId = "absent")))))
        assertTrue("INVALID_EDGE_PORT" in codes(doc.copy(edges = doc.edges + (edge.id to edge.copy(sourcePort = "unknown")))))
    }

    @Test fun objectPortsHaveExactlyOneTargetAndRawRelationsCannotDuplicateEdges() {
        val doc = document()
        assertTrue("PORT_CARDINALITY" in codes(doc.copy(nodes = doc.nodes - "label")))
        val field = doc.nodes.getValue("field")
        assertTrue("DUPLICATE_RELATION" in codes(withNode(doc, field.copy(properties = JsonObject(
            field.properties + ("label" to JsonObject(emptyMap())))))))
    }

    @Test fun unknownNodesAndCustomActionsArePreservedWithoutEditingSupport() {
        val unknown = """{"component":"custom:visual","id":"custom","properties":{"value":{"a":[1,null,true]}}}"""
        val result = legacy.importDocument(unknown, "unknown")
        assertTrue(result.isSuccess)
        assertTrue(result.diagnostics.any { it.code == "UNMODELED_DEFINITION" })
        assertEquals(Json.parseToJsonElement(unknown), Json.parseToJsonElement(legacy.exportDocument(result.requireValue()).requireValue()))
        val action = legacy.importDocument("""{"component":"layout:button","id":"button","properties":{"onClick":{"component":"custom:run","id":"custom-action","properties":{"arg":7}}}}""", "custom-action")
        assertTrue(action.isSuccess)
        assertEquals(AuthorNodeKind.ACTION, action.requireValue().nodes.getValue("custom-action").kind)
    }

    @Test fun encodedStringSlotsRemainStringsAndEmptyArraysRemainPresent() {
        val label = """{"component":"layout:text","id":"label","properties":{"text":"Nome"}}"""
        val root = JsonObject(mapOf("component" to JsonPrimitive("layout:textField"), "id" to JsonPrimitive("field"),
            "properties" to JsonObject(mapOf("label" to JsonPrimitive(label), "onDone" to JsonArray(emptyList())))))
        val doc = legacy.importDocument(root.toString(), "strings").requireValue()
        assertEquals(NodeListShape.STRING_OBJECT, doc.nodes.getValue("field").nodeListShapes["label"])
        val restored = Json.parseToJsonElement(legacy.exportDocument(doc).requireValue()).jsonObject.getValue("properties").jsonObject
        assertTrue(restored.getValue("label") is JsonPrimitive)
        assertEquals(Json.parseToJsonElement(label), Json.parseToJsonElement((restored.getValue("label") as JsonPrimitive).content))
        assertEquals(JsonArray(emptyList()), restored["onDone"])
    }

    @Test fun navigationCyclesAreAllowedButRequireCompilationForLegacyExport() {
        val root = document().nodes.getValue("root").copy(properties = JsonObject(emptyMap()), hasChildren = false)
        val second = root.copy(id = "second", placement = Placement.Root("z"))
        val edges = listOf(AuthorEdge("a", EdgeRole.NAVIGATION, root.id, "navigate", second.id, "entry"),
            AuthorEdge("b", EdgeRole.NAVIGATION, second.id, "navigate", root.id, "entry"))
        val doc = AuthoringDocument("navigation", entryNodeId = root.id, nodes = mapOf(root.id to root, second.id to second), edges = edges.associateBy { it.id })
        assertTrue(validateAuthoringDocument(doc, catalog).isEmpty())
        assertEquals(doc, codec.decode(codec.encode(doc).requireValue()).requireValue())
        assertEquals("NAVIGATION_COMPILATION_REQUIRED", legacy.exportDocument(doc).diagnostics.single().code)
    }

    @Test fun actionFlowCyclesAreRejectedIndependentlyOfContainment() {
        val root = document().nodes.getValue("root").copy(properties = JsonObject(emptyMap()), hasChildren = false)
        val first = AuthorNode("a", AuthorNodeKind.ACTION, "action:then", JsonObject(emptyMap()), Placement.Root("a"))
        val second = first.copy(id = "b", placement = Placement.Root("b"))
        val edges = listOf(AuthorEdge("ab", EdgeRole.ACTION_FLOW, "a", "invoke", "b", "input", "V"),
            AuthorEdge("ba", EdgeRole.ACTION_FLOW, "b", "invoke", "a", "input", "V"))
        val doc = AuthoringDocument("cycle", entryNodeId = root.id, nodes = listOf(root, first, second).associateBy { it.id }, edges = edges.associateBy { it.id })
        assertTrue("ACTION_CYCLE" in codes(doc))
        assertNull(codec.encode(doc).value)
    }

    @Test fun budgetsVersionsAndInvalidCoordinatesAreDiagnosed() {
        val doc = document()
        assertEquals("AUTHORING_BUDGET", validateAuthoringDocument(doc, catalog, AuthoringLimits(maxNodes = 2)).single().code)
        assertTrue("CONTAINMENT_DEPTH" in validateAuthoringDocument(doc, catalog, AuthoringLimits(maxDepth = 2)).map { it.code })
        assertTrue("AUTHORING_VERSION" in codes(doc.copy(formatVersion = 2)))
        val root = doc.nodes.getValue("root")
        assertTrue("CANVAS_POSITION" in codes(withNode(doc, root.copy(canvasPosition = CanvasPoint(Double.NaN, 0.0)))))
    }

    @Test fun selectingOneRootReportsExcludedNodesAndDoesNotDeleteAuthoringData() {
        val doc = document()
        val extra = doc.nodes.getValue("root").copy(id = "extra", placement = Placement.Root("z"), properties = JsonObject(emptyMap()), hasChildren = false)
        val multiRoot = withNode(doc, extra)
        val result = legacy.exportDocument(multiRoot)
        assertNotNull(result.value)
        assertTrue(result.diagnostics.any { it.code == "UNREACHED_NODES" })
        assertTrue("extra" in codec.decode(codec.encode(multiRoot).requireValue()).requireValue().nodes)
    }

    @Test fun sharedActionsPersistInGraphButCannotBeSilentlyDuplicatedInLegacyTree() {
        val doc = document()
        val shared = AuthorEdge("shared", EdgeRole.EVENT, "field", "onDone", "condition", "input", "V")
        val graph = doc.copy(edges = doc.edges + (shared.id to shared))
        assertTrue(validateAuthoringDocument(graph, catalog).isEmpty())
        assertEquals(graph, codec.decode(codec.encode(graph).requireValue()).requireValue())
        val output = legacy.exportDocument(graph)
        assertNull(output.value)
        assertTrue(output.diagnostics.any { it.code == "SHARED_NODE_COMPILATION_REQUIRED" })
    }

    @Test fun malformedAndFutureAuthoringFormatsFailWithDiagnostics() {
        assertEquals("INVALID_JSON", codec.decode("{").diagnostics.single().code)
        assertEquals("AUTHORING_JSON", codec.decode("[]").diagnostics.single().code)
        val encoded = Json.parseToJsonElement(codec.encode(document()).requireValue()).jsonObject
        val future = JsonObject(encoded + ("formatVersion" to JsonPrimitive(2)))
        assertTrue(codec.decode(future.toString()).diagnostics.any { it.code == "AUTHORING_VERSION" })
    }

    @Test fun characterBudgetsApplyToBothAuthoringEncodingAndDecoding() {
        val limited = AuthoringDocumentCodec(catalog, payloadLimits = PayloadLimits(maxInputChars = 100))
        assertEquals("INPUT_LIMIT", limited.encode(document()).diagnostics.last().code)
        assertEquals("INPUT_LIMIT", limited.decode(codec.encode(document()).requireValue()).diagnostics.single().code)
    }
}
