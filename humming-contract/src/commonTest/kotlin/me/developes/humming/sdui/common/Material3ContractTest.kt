package me.developes.humming.sdui.common

import me.developes.humming.sdui.layout.Material3Definitions
import kotlin.test.*

class Material3ContractTest {
    @Test fun controlsExposeStateEventsAndTypedSlotsWithoutUiDependencies() {
        val catalog = initialNodeCatalog()
        val root = parseNode("""{"component":"layout:card","id":"form","children":[
          {"component":"layout:checkbox","properties":{"stateKey":"terms","checked":false,"onChange":{"component":"action:update","properties":{"state":"changed","value":"yes"}}}},
          {"component":"layout:outlinedTextField","properties":{"stateKey":"name","label":"Nome"}},
          {"component":"layout:materialOutlinedButton","children":[{"component":"layout:text","properties":{"text":"Salvar"}}]}
        ]}""")
        assertEquals(emptyList(), catalog.validate(root, mode = ValidationMode.AUTHORING))
        assertTrue(Material3Definitions.all.size > 70)
        assertEquals(Material3Definitions.all.size, Material3Definitions.names.size)
        assertTrue(Material3Definitions.all.all { it.version == 1 })
    }

    @Test fun invalidBooleansNumbersAndActionRolesProduceLocatedDiagnostics() {
        val catalog = initialNodeCatalog()
        for ((name, properties, code) in listOf(
            Triple("checkbox", "\"checked\":\"yes\"", "INVALID_PROPERTY"),
            Triple("slider", "\"steps\":-1", "PROPERTY_RANGE"),
            Triple("horizontalUncontainedCarousel", "\"itemWidth\":0", "PROPERTY_RANGE"),
            Triple("alertDialog", "\"confirmButton\":{\"component\":\"action:update\",\"properties\":{\"state\":\"x\",\"value\":\"y\"}}", "INVALID_NODE_ROLE"),
        )) {
            val errors = catalog.validate(parseNode("""{"component":"layout:$name","id":"invalid","properties":{$properties}}"""), mode = ValidationMode.AUTHORING)
            assertTrue(errors.any { it.code == code }, "$name: $errors")
            assertTrue(errors.all { it.nodeId != null && it.path.isNotEmpty() })
        }
    }
}
