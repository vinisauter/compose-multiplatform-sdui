package me.developes.humming.sdui.common

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HeadlessContractTest {
    private val source = """{"component":"layout:column","id":"form","children":[{"component":"layout:textField","id":"field","properties":{"text":"Ana","onChangeUpdateState":"name"}},{"component":"layout:button","id":"submit","properties":{"onClick":{"component":"action:update","id":"update","properties":{"state":"status","value":"ok"}}},"children":[{"component":"layout:text","id":"caption","properties":{"text":"Confirmar"}}]}]}"""

    @Test fun parseValidateAndSerializeWithoutInstallingAnyRenderer() {
        val node = parseNode(source, documentId = "headless")
        val catalog = initialNodeCatalog()
        assertTrue(catalog.validate(node, mode = ValidationMode.AUTHORING).isEmpty())
        val encoded = Json.encodeToString(ServerDrivenNode.serializer(), node)
        val restored = parseNode(encoded, documentId = "roundtrip")
        assertTrue(catalog.validate(restored, mode = ValidationMode.AUTHORING).isEmpty())
        assertEquals("form", restored.id)
        assertEquals(listOf("field", "submit"), restored.children!!.map { it.id })
        assertEquals("ok", restored.children!![1].propertyNodes("onClick").single().property("value"))
        assertEquals(18, catalog.capabilities.size)
    }

    @Test fun schemasReportInvalidPayloadFieldsWithoutViewModel() {
        val node = parseNode("""{"component":"layout:textField","id":"field","properties":{"keyboardType":"Unknown"}}""", documentId = "invalid")
        val error = initialNodeCatalog().validate(node, mode = ValidationMode.AUTHORING).single()
        assertEquals("INVALID_PROPERTY", error.code)
        assertEquals("$.properties[\"keyboardType\"]", error.path)
        assertEquals("field", error.nodeId)
        assertEquals("invalid", error.documentId)
    }

    @Test fun serializationOfEditedModelUsesItsCurrentProperties() {
        val node = parseNode(source)
        node.children!![0].properties!!["text"] = "Edited"
        val encoded = Json.encodeToString(ServerDrivenNode.serializer(), node)
        assertEquals("Edited", parseNode(encoded).children!![0].property("text"))
        // The legacy wrapper's toString contract is intentionally unchanged.
        assertEquals("Ana", parseNode(node.toString()).children!![0].property("text"))
    }
}
