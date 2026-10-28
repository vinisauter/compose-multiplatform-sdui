package me.developes.humming.builder.ui

import me.developes.humming.sdui.action.ActionDefinitions
import me.developes.humming.sdui.action.SDActions
import me.developes.humming.sdui.common.DiagnosticSeverity
import me.developes.humming.sdui.common.NodeCatalog
import me.developes.humming.sdui.common.NodeRole
import me.developes.humming.sdui.common.ValidationMode
import me.developes.humming.sdui.common.parseNode
import me.developes.humming.sdui.layout.LayoutDefinitions
import me.developes.humming.sdui.layout.SDLayoutMaterial3
import me.developes.humming.sdui.navigation.NavigationDefinitions
import me.developes.humming.sdui.navigation.SDNavigation
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CatalogCompatibilityTest {
    private val catalog = NodeCatalog(SDLayoutMaterial3().definitions + SDActions().definitions + SDNavigation().definitions)

    @Test fun materialGalleryIsEditableAndCompilesWithRegisteredHandlers() {
        val workspace = me.developes.humming.sdui.studio.LocalWorkspace()
        workspace.importContract(MATERIAL3_EXAMPLE).requireValue()
        val compiled = workspace.compile().requireValue()
        assertTrue(compiled.artifacts.values.single().contains("layout:outlinedTextField"))
        val library = SDLayoutMaterial3()
        library.definitions.forEach { assertTrue(library.getComponent(it.identifier.substringAfter(':')) != null, it.identifier) }
    }

    @Test fun initialCapabilitiesAreVersionedAndUnmodeledRenderersStayOutsideTheEditableSubset() {
        assertEquals(18 + me.developes.humming.sdui.layout.Material3Definitions.all.size + me.developes.humming.sdui.layout.LegacyMaterial3Definitions.all.size + me.developes.humming.sdui.action.ActionDefinitions.legacy.size, catalog.capabilities.size)
        assertEquals(2, catalog.capabilities["layout:reactiveText"])
        assertEquals(2, catalog.capabilities["layout:validatedTextField"])
        assertTrue(catalog.capabilities.filterKeys { it !in setOf("layout:reactiveText", "layout:validatedTextField") }.values.all { it == 1 })
        assertTrue(catalog["layout:textField"] != null)
        assertTrue(SDLayoutMaterial3().getComponent("image") != null)
        assertTrue("layout:image" in catalog.capabilities)
        assertEquals(emptyList(), catalog.validate(parseNode("""{"component":"layout:image","properties":{"asset":"catalog-mark"}}"""), mode = ValidationMode.AUTHORING))
    }

    @Test fun legacyFieldBindingsSlotsEnumsAndImeActionsRemainUnchanged() {
        val node = parseNode("""{"component":"layout:textField","id":"name","properties":{"text":"#{name}","onChangeUpdateState":"name","enabled":"#{enabled}","visualMask":"###-##","keyboardType":"Number","imeAction":"Done","label":[{"component":"layout:text","properties":{"text":"Nome"}}],"onDone":{"component":"action:method","properties":{"invoke":"commit"}}}}""")
        assertEquals(emptyList(), catalog.validate(node, mode = ValidationMode.AUTHORING))
        val parsed = LayoutDefinitions.textField.parse(node).requireValid()
        for (name in node.properties.orEmpty().keys) assertEquals(node.property(name), parsed.property(name))
        assertEquals("#{name}", parsed.property("text"))
        assertEquals("###-##", parsed.property("visualMask"))
        assertEquals("commit", node.propertyNodes("onDone").single().property("invoke"))
    }

    @Test fun defaultsDescribeEffectiveValuesWithoutInjectingThemIntoWireProperties() {
        val text = LayoutDefinitions.text.properties.associateBy { it.name }
        assertEquals("16", text.getValue("fontSize").defaultValue)
        assertEquals("Start", text.getValue("textAlign").defaultValue)
        assertEquals("absent: FontWeight.Normal", text.getValue("fontWeight").defaultDescription)
        val node = parseNode("""{"component":"layout:text","properties":{"text":"Hello"}}""")
        assertEquals(null, LayoutDefinitions.text.parse(node).requireValid().property("fontSize"))
        assertEquals("Top", LayoutDefinitions.column.properties.single { it.name == "verticalArrangement" }.defaultValue)
        assertEquals("Text", LayoutDefinitions.textField.properties.single { it.name == "keyboardType" }.defaultValue)
    }

    @Test fun rowFallbackAndColumnRejectionMatchLegacyHandlers() {
        val row = parseNode("""{"component":"layout:row","properties":{"horizontalArrangement":"unknown"}}""")
        val column = parseNode("""{"component":"layout:column","properties":{"verticalArrangement":"unknown"}}""")
        assertEquals(DiagnosticSeverity.WARNING, catalog.validate(row).single().severity)
        assertEquals(DiagnosticSeverity.ERROR, catalog.validate(column).single().severity)
        assertEquals(DiagnosticSeverity.ERROR, catalog.validate(row, mode = ValidationMode.AUTHORING).single().severity)
    }

    @Test fun actionNamesConditionalEventsAndInvokeArePreserved() {
        val node = parseNode("""{"component":"action:ifState","properties":{"state":"ready","equals":"#{expected}","then":[{"component":"action:method","properties":{"invoke":"commit"}},{"component":"action:update","properties":{"state":"status","value":"ok","delay":5}}],"else":{"component":"action:then","properties":{"condition":"retry","invoke":{"component":"action:method","properties":{"invoke":"retry"}}}}}}""")
        assertEquals(emptyList(), catalog.validate(node, NodeRole.ACTION, ValidationMode.AUTHORING))
        assertEquals(listOf("action:method", "action:update"), node.propertyNodes("then").map { it.component })
        assertEquals("#{expected}", ActionDefinitions.ifState.parse(node).requireValid().property("equals"))
        val absentInvoke = parseNode("""{"component":"action:method"}""")
        assertTrue(ActionDefinitions.method.parse(absentInvoke).diagnostics.isEmpty())
    }

    @Test fun navigationMetadataBelongsInsideGraphAndActionsInsideEvents() {
        val graph = parseNode("""{"component":"navigation:graph","properties":{"startDestination":"start"},"children":[{"component":"navigation:node","properties":{"name":"start","type":"local","destiny":"start","deepLinks":["app://start"]}}]}""")
        assertTrue(catalog.validate(graph, mode = ValidationMode.AUTHORING).isEmpty())
        assertEquals(NodeRole.NAVIGATION_METADATA, NavigationDefinitions.node.role)
        val metadata = graph.children!!.single()
        assertEquals("INVALID_NODE_ROLE", catalog.validate(metadata).single().code)
        val button = parseNode("""{"component":"layout:button","properties":{"onClick":{"component":"navigation:node","properties":{"name":"x","type":"local","destiny":"x"}}}}""")
        assertEquals("INVALID_NODE_ROLE", catalog.validate(button).single().code)
    }

    @Test fun advancedBrushesAreValidatedForAuthoring() {
        val box = parseNode("""{"component":"layout:box","properties":{"backgroundGradient":{"type":"vertical","colors":["#000000","#ffffff"]}}}""")
        assertTrue(catalog.validate(box).isEmpty())
        assertTrue(catalog.validate(box, mode = ValidationMode.AUTHORING).isEmpty())
    }

    @Test fun fieldRangesColorsAndMissingOrDuplicateRoutesHaveFieldDiagnostics() {
        val field = parseNode("""{"component":"layout:textField","properties":{"minLines":4,"maxLines":2,"color":"orange"}}""")
        assertEquals(setOf("INVALID_PROPERTY", "PROPERTY_ORDER"), catalog.validate(field).map { it.code }.toSet())
        val graph = parseNode("""{"component":"navigation:graph","properties":{"startDestination":"missing"},"children":[{"component":"navigation:node","properties":{"name":"duplicate","type":"local","destiny":"one"}},{"component":"navigation:node","properties":{"name":"duplicate","type":"local","destiny":"two"}}]}""")
        val errors = catalog.validate(graph)
        assertEquals(setOf("DUPLICATE_CHILD_KEY", "UNKNOWN_CHILD_REFERENCE"), errors.map { it.code }.toSet())
        assertTrue(errors.all { ".properties[" in it.path })
    }

    @Test fun deepLinkPrimitiveCoercionStaysLegacyOnlyAndNestedInputIsBounded() {
        val node = parseNode("""{"component":"navigation:node","properties":{"name":"start","type":"local","destiny":"start","deepLinks":[12,null,"app://start"]}}""")
        assertTrue(catalog.validate(node, NodeRole.NAVIGATION_METADATA).isEmpty())
        assertEquals("INVALID_PROPERTY", catalog.validate(node, NodeRole.NAVIGATION_METADATA, ValidationMode.AUTHORING).single().code)
        val nested = me.developes.humming.sdui.common.ServerDrivenNode("navigation:node", "start",
            mutableMapOf("name" to "start", "type" to "local", "destiny" to "start", "deepLinks" to "[".repeat(70) + "0" + "]".repeat(70)))
        assertEquals("DEPTH_LIMIT", catalog.validate(nested, NodeRole.NAVIGATION_METADATA).single().code)
    }
}
