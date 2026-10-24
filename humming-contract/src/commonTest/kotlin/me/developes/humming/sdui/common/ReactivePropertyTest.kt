package me.developes.humming.sdui.common

import kotlin.test.*

class ReactivePropertyTest {
    private fun node(value: String) = parseNode("""{"component":"layout:reactiveText","id":"label","properties":{"value":$value}}""")
    @Test fun explicitEnvelopePreservesTextFallbackAndRejectsLegacyStrings() {
        val property = node("""{"version":1,"expression":{"language":"humming.expression","version":1,"source":"state.name"},"fallback":"0007"}""")
            .reactiveProperty("value", ReactiveValueType.TEXT)
        assertEquals(SduiValue.Text("0007"), property.fallback)
        assertEquals("state.name", property.expression.source)
        val legacy = assertFailsWith<PayloadException> { node("\"#{name}\"").reactiveProperty("value", ReactiveValueType.TEXT) }
        assertEquals("REACTIVE_PROPERTY_FORMAT", legacy.diagnostic.code)
        assertEquals("label", legacy.diagnostic.nodeId)
    }

    @Test fun schemaRejectsUnsupportedVersionUnknownFieldsAndWrongFallbackTypes() {
        fun error(value: String) = initialNodeCatalog().validate(node(value), mode = ValidationMode.AUTHORING).first { it.severity == DiagnosticSeverity.ERROR }.code
        val expression = """"expression":{"language":"humming.expression","version":1,"source":"true"}"""
        assertEquals("REACTIVE_PROPERTY_VERSION", error("""{"version":2,$expression,"fallback":"safe"}"""))
        assertEquals("REACTIVE_PROPERTY_FORMAT", error("""{"version":1,$expression,"fallback":"safe","ignored":true}"""))
        assertEquals("REACTIVE_PROPERTY_FALLBACK", error("""{"version":1,$expression,"fallback":false}"""))
    }

    @Test fun expressionSyntaxErrorsRetainTheNodeAndPropertyOffset() {
        val error = assertFailsWith<PayloadException> {
            node("""{"version":1,"expression":{"language":"humming.expression","version":1,"source":"state."},"fallback":"safe"}""")
                .reactiveProperty("value", ReactiveValueType.TEXT)
        }.diagnostic
        assertEquals("label", error.nodeId)
        assertTrue(error.path.startsWith("$.properties[\"value\"].expression.source["))
    }
}
