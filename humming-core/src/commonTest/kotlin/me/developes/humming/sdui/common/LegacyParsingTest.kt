package me.developes.humming.sdui.common

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class LegacyParsingTest {
    @Test
    fun explicitIdentityCaseAndChildOrderArePreserved() {
        val node = LegacyFixtures.parse(LegacyFixtures.document)
        assertEquals("form", node.id)
        assertEquals("layout:column", node.component)
        assertEquals(listOf("name", "confirm"), node.children!!.map { it.id })
        assertEquals("layout:textField", node.children!![0].component)
    }

    @Test
    fun omittedIdentityIsGeneratedPerParse() {
        val source = """{"component":"layout:text"}"""
        val first = LegacyFixtures.parse(source).id
        val second = LegacyFixtures.parse(source).id
        assertTrue(first.startsWith("layout:text:"))
        assertTrue(first.length > "layout:text:".length)
        assertNotEquals(first, second)
    }

    @Test
    fun primitivePropertiesBecomeStrings() {
        val node = LegacyFixtures.parse("""{"component":"test:node","properties":{"text":"abc","number":12,"enabled":true}}""")
        assertEquals("abc", node.property("text"))
        assertEquals("12", node.property("number"))
        assertEquals("true", node.property("enabled"))
        assertEquals(null, node.property("absent"))
    }

    @Test
    fun objectSlotIsParsedAsOneNode() {
        val field = LegacyFixtures.parse(LegacyFixtures.document).children!![0]
        val label = field.propertyNodes("label").single()
        assertEquals("label", label.id)
        assertEquals("Nome", label.property("text"))
        assertEquals(Json.parseToJsonElement(field.property("label")!!).jsonObject, field.propertyJsonObject("label"))
    }

    @Test
    fun actionArrayPreservesOrderAndInvokeProperty() {
        val button = LegacyFixtures.parse(LegacyFixtures.document).children!![1]
        val actions = button.propertyNodes("onClick")
        assertEquals(listOf("first", "second"), actions.map { it.id })
        assertEquals("confirm", actions[1].property("invoke"))
        assertTrue(button.propertyNodes("unknownEvent").isEmpty())
        assertEquals("caption", button.children!!.single().id)
    }

    @Test
    fun singleActionObjectIsAccepted() {
        val node = LegacyFixtures.parse("""{"component":"layout:button","properties":{"onClick":{"component":"action:update","id":"update"}}}""")
        assertEquals("update", node.propertyNodes("onClick").single().id)
    }

    @Test
    fun uneditedJsonRoundTripPreservesIdentityOrderAndNestedProperties() {
        val first = LegacyFixtures.parse(LegacyFixtures.document)
        val second = LegacyFixtures.parse(first.toString())
        assertEquals(Json.parseToJsonElement(LegacyFixtures.document), Json.parseToJsonElement(second.toString()))
        assertEquals(listOf("name", "confirm"), second.children!!.map { it.id })
        assertEquals(listOf("first", "second"), second.children!![1].propertyNodes("onClick").map { it.id })
    }

    @Test
    fun typedNodeSerializerRoundTripPreservesTextualModel() {
        val node = ServerDrivenNode(
            component = "test:node", id = "root",
            properties = mutableMapOf("text" to "abc", "absent" to null),
            children = mutableListOf(ServerDrivenNode("test:child", "child")),
        )
        val encoded = Json.encodeToString(ServerDrivenNode.serializer(), node)
        val decoded = Json.decodeFromString(ServerDrivenNode.serializer(), encoded)
        assertEquals(node.id, decoded.id)
        assertEquals(node.properties, decoded.properties)
        assertEquals("child", decoded.children!!.single().id)
    }

    @Test
    fun invalidJsonAndStructuralTypesFailBeforeRendering() {
        val invalid = listOf(
            "{", "[]", "{}",
            """{"component":"test:node","properties":12}""",
            """{"component":"test:node","children":["invalid"]}""",
        )
        for (source in invalid) {
            assertFails("Entrada deveria falhar: $source") { LegacyFixtures.parse(source) }
        }
    }

    // Caracterização de limitações observadas; não são garantias desejadas.
    // Ao corrigir, substituir estas expectativas com a migração da tarefa indicada.
    @Test
    fun observedLegacyJsonNullBecomesLiteralText() {
        assertEquals("null", LegacyFixtures.parse(LegacyFixtures.document).property("empty"))
    }

    @Test
    fun invalidEventArrayRejectsNonObjectsWithDiagnostic() {
        val node = LegacyFixtures.parse("""{"component":"test:node","properties":{"onClick":[false,null,3,{"component":"test:action","id":"kept"}]}}""")
        val error = assertFailsWith<PayloadException> { node.propertyNodes("onClick") }
        assertEquals("INVALID_EVENT_NODE", error.diagnostic.code)
        assertTrue(error.diagnostic.path.endsWith("[0]"))
    }

    @Test
    fun observedLegacyToStringRetainsOriginalJsonAfterMutation() {
        val source = """{"component":"test:node","id":"root","properties":{"text":"original"}}"""
        val node = LegacyFixtures.parse(source)
        node.properties!!["text"] = "edited"
        assertEquals("edited", node.property("text"))
        assertEquals("original", Json.parseToJsonElement(node.toString()).jsonObject.toNode().property("text"))
    }
}
