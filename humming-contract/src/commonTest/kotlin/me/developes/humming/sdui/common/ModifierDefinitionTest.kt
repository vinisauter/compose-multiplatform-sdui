package me.developes.humming.sdui.common

import me.developes.humming.sdui.layout.LayoutDefinitions
import kotlin.test.*

class ModifierDefinitionTest {
    @Test fun allVisualDefinitionsShareUniqueModifierPropertiesWithoutChangingImageAlpha() {
        val modifiers = LayoutDefinitions.modifiers.map { it.name }.toSet()
        assertEquals(59, modifiers.size)
        LayoutDefinitions.all.forEach { definition ->
            assertTrue(definition.properties.map { it.name }.containsAll(modifiers), definition.identifier)
            assertEquals(definition.properties.size, definition.properties.map { it.name }.toSet().size)
        }
        assertTrue(LayoutDefinitions.all.first { it.identifier == "layout:image" }.properties.any { it.name == "alpha" })
        assertFalse("alpha" in modifiers)
    }

    @Test fun newDimensionsTransformsAndBrushesRejectBadValuesWithFieldDiagnostics() {
        val schema = NodeDefinition("layout:box", NodeRole.COMPONENT, LayoutDefinitions.modifiers)
        for ((key, value) in mapOf("size" to "-1", "aspectRatio" to "0", "modifierAlpha" to "1.1",
            "rotation" to "NaN", "clip" to "yes", "wrapContentWidth" to "Bottom")) {
            val diagnostics = schema.parse(ServerDrivenNode("layout:box", "probe", mutableMapOf(key to value)), ValidationMode.AUTHORING).diagnostics
            assertTrue(diagnostics.any { it.severity == DiagnosticSeverity.ERROR && key in it.path && it.nodeId == "probe" }, "$key=$value")
        }
        for (gradient in listOf("{}", "[]", "{\"type\":\"radial\",\"radius\":0,\"colors\":[\"#000\",\"#fff\"]}",
            "{\"type\":\"linear\",\"colors\":[\"#000\",\"#fff\"],\"startOffset\":{\"x\":1}}")) {
            assertTrue(schema.parse(ServerDrivenNode("layout:box", "probe", mutableMapOf("backgroundGradient" to gradient))).diagnostics.any {
                it.code == "INVALID_PROPERTY" && "backgroundGradient" in it.path
            })
        }
    }

    @Test fun allFiveBrushesAndLegacyScrollPresenceRemainValid() {
        for (type in listOf("linear", "radial", "sweep", "vertical", "horizontal")) {
            val node = ServerDrivenNode("layout:box", "probe", mutableMapOf("backgroundGradient" to
                """{"type":"$type","colors":["#000000","#ffffff"],"tileMode":"Mirror"}""", "verticalScroll" to "false"))
            assertTrue(LayoutDefinitions.box.parse(node, ValidationMode.AUTHORING).diagnostics.isEmpty())
        }
    }

    @Test fun imageFiltersHaveFiniteTypedSchemas() {
        val schema = LayoutDefinitions.all.first { it.identifier == "layout:image" }
        for (filter in listOf("""{"type":"tint","color":"#f00"}""", """{"type":"lighting","mul":"#ffffff","add":"#000000"}""")) {
            assertTrue(schema.parse(ServerDrivenNode("layout:image", "image", mutableMapOf("colorFilter" to filter)), ValidationMode.AUTHORING).diagnostics.isEmpty())
        }
        for (filter in listOf("{}", """{"type":"tint","color":"orange"}""", """{"type":"colorMatrix","matrix":[1,2]}""")) {
            assertTrue(schema.parse(ServerDrivenNode("layout:image", "image", mutableMapOf("colorFilter" to filter)), ValidationMode.AUTHORING).diagnostics.any { it.code == "INVALID_PROPERTY" })
        }
    }

    @Test fun constraintRangesRejectInvertedFractionalBounds() {
        for ((lower, upper) in listOf("widthInMin" to "widthInMax", "heightInMin" to "heightInMax",
            "requiredMinWidth" to "requiredMaxWidth", "requiredMinHeight" to "requiredMaxHeight")) {
            val node = ServerDrivenNode("layout:box", "range", mutableMapOf(lower to "80.5", upper to "80.25"))
            assertTrue(LayoutDefinitions.box.parse(node, ValidationMode.AUTHORING).diagnostics.any { it.code == "PROPERTY_ORDER" && upper in it.path })
        }
    }
}
