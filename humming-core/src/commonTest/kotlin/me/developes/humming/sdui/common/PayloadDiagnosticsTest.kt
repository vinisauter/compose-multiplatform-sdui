package me.developes.humming.sdui.common

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PayloadDiagnosticsTest {
    @Test
    fun malformedJsonAndRootHaveStableDiagnostics() {
        val syntax = assertFailsWith<PayloadException> { parseNode("{", documentId = "fixture") }
        assertEquals("INVALID_JSON", syntax.diagnostic.code)
        assertEquals("fixture", syntax.diagnostic.documentId)
        assertEquals("parse", syntax.diagnostic.operation)
        val root = assertFailsWith<PayloadException> { parseNode("[]") }
        assertEquals("EXPECTED_NODE", root.diagnostic.code)
    }

    @Test
    fun invalidComponentTypesAndNamesAreRejected() {
        for (value in listOf("null", "true", "12", "\"plain\"", "\":text\"", "\"a:b:c\"", "\"a: b\"")) {
            val error = assertFailsWith<PayloadException> { parseNode("{\"component\":$value,\"id\":\"invalid\"}") }
            assertTrue(error.diagnostic.code in setOf("INVALID_COMPONENT", "INVALID_IDENTIFIER"))
            assertEquals("invalid", error.diagnostic.nodeId)
            assertEquals("$.component", error.diagnostic.path)
        }
    }

    @Test
    fun invalidChildReportsItsFullPath() {
        val source = """{"component":"a:root","id":"root","children":[{"id":"child"}]}"""
        val error = assertFailsWith<PayloadException> { parseNode(source, documentId = "screen") }
        assertEquals("INVALID_COMPONENT", error.diagnostic.code)
        assertEquals("child", error.diagnostic.nodeId)
        assertEquals("screen", error.diagnostic.documentId)
        assertEquals("$.children[0].component", error.diagnostic.path)
    }

    @Test
    fun invalidStructuralTypesAreIdentified() {
        for ((tail, code) in listOf(
            "\"id\":null" to "INVALID_ID",
            "\"properties\":[]" to "INVALID_PROPERTIES",
            "\"children\":{}" to "INVALID_CHILDREN",
        )) {
            val error = assertFailsWith<PayloadException> { parseNode("{\"component\":\"a:b\",$tail}") }
            assertEquals(code, error.diagnostic.code)
        }
    }

    @Test
    fun rawInputBudgetAcceptsBoundaryAndRejectsExcess() {
        val source = """{"component":"a:b"}"""
        assertEquals("a:b", parseNode(source, PayloadLimits(maxInputChars = source.length)).component)
        val error = assertFailsWith<PayloadException> {
            parseNode(source, PayloadLimits(maxInputChars = source.length - 1))
        }
        assertEquals("INPUT_LIMIT", error.diagnostic.code)
    }

    @Test
    fun nestingBudgetAppliesBeforeParserRecursion() {
        val source = "[".repeat(100) + "0" + "]".repeat(100)
        val error = assertFailsWith<PayloadException> { parseNode(source, PayloadLimits(maxDepth = 8)) }
        assertEquals("DEPTH_LIMIT", error.diagnostic.code)
    }

    @Test
    fun quotedBracketsDoNotCountAsNesting() {
        val source = """{"component":"a:b","properties":{"text":"[[[{{{\""}}"""
        assertEquals("[[[{{{\"", parseNode(source, PayloadLimits(maxDepth = 4)).property("text"))
    }

    @Test
    fun nodeAndValueBudgetsAreIndependent() {
        val tree = """{"component":"a:b","children":[{"component":"a:c","id":"child"}]}"""
        val nodes = assertFailsWith<PayloadException> { parseNode(tree, PayloadLimits(maxNodes = 1)) }
        assertEquals("NODE_LIMIT", nodes.diagnostic.code)
        assertEquals("child", nodes.diagnostic.nodeId)
        val values = assertFailsWith<PayloadException> {
            parseNode("""{"component":"a:b","id":"root"}""", PayloadLimits(maxJsonValues = 2))
        }
        assertEquals("VALUE_LIMIT", values.diagnostic.code)
    }

    @Test
    fun propertyBudgetAppliesToEncodedObjectsAndStrings() {
        for (value in listOf("\"12345678901\"", "[\"123\",\"456\"]")) {
            val error = assertFailsWith<PayloadException> {
                parseNode("{\"component\":\"a:b\",\"properties\":{\"data\":$value}}", PayloadLimits(maxPropertyChars = 10))
            }
            assertEquals("PROPERTY_LIMIT", error.diagnostic.code)
            assertTrue(error.diagnostic.path.contains("data"))
        }
    }

    @Test
    fun invalidEventItemIsNotSilentlyDiscarded() {
        val source = """{"component":"a:button","id":"button","properties":{"onClick":[{"component":"a:action"},false]}}"""
        val node = parseNode(source, documentId = "screen")
        val error = assertFailsWith<PayloadException> { node.propertyNodes("onClick") }
        assertEquals("INVALID_EVENT_NODE", error.diagnostic.code)
        assertEquals("$.properties[\"onClick\"][1]", error.diagnostic.path)
        assertEquals("button", error.diagnostic.nodeId)
        assertEquals("screen", error.diagnostic.documentId)
    }

    @Test
    fun eventBudgetAlsoAppliesToProgrammaticNodes() {
        val node = ServerDrivenNode("a:button", "button", mutableMapOf("onClick" to "[" + "0,".repeat(100_000) + "0]"))
        val error = assertFailsWith<PayloadException> { node.propertyNodes("onClick") }
        assertEquals("VALUE_LIMIT", error.diagnostic.code)
    }

    @Test
    fun whitespaceAndSingleObjectEventsRemainSupported() {
        val node = ServerDrivenNode("a:button", properties = mutableMapOf("onClick" to "  [ {\"component\":\"a:run\",\"id\":\"run\"} ]  "))
        assertEquals("run", node.propertyNodes("onClick").single().id)
        assertEquals("run", node.propertyNode("onClick")?.id)
    }

    @Test
    fun singleSlotRejectsMultipleNodes() {
        val node = ServerDrivenNode("a:node", properties = mutableMapOf("label" to """[{"component":"a:text"},{"component":"a:text"}]"""))
        assertEquals("INVALID_SLOT_CARDINALITY", assertFailsWith<PayloadException> { node.propertyNode("label") }.diagnostic.code)
    }

    @Test
    fun genericJsonNodeListAlsoRejectsInvalidItems() {
        val error = assertFailsWith<PayloadException> { Json.parseToJsonElement("[false]").serverDrivenNode }
        assertEquals("INVALID_EVENT_NODE", error.diagnostic.code)
    }
}
