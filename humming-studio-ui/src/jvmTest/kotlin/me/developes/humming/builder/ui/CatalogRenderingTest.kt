package me.developes.humming.builder.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import me.developes.humming.sdui.action.SDActions
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.ValidationMode
import me.developes.humming.sdui.common.parseNode
import me.developes.humming.sdui.layout.SDLayoutMaterial3
import org.junit.After
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CatalogRenderingTest {
    @get:Rule val compose = createComposeRule()
    private val store = ViewModelStore()
    private val mounted = mutableStateOf(true)

    @After fun cleanup() {
        compose.runOnIdle { mounted.value = false }
        compose.waitForIdle()
        compose.runOnIdle { store.clear() }
    }

    @Test fun modeledLayoutAndActionSubsetRendersAndExecutesTheSameWireContract() {
        lateinit var vm: SDUIFormViewModel
        var commits = 0
        compose.runOnIdle {
            vm = SDUIFormViewModel(SavedStateHandle(), listOf(SDLayoutMaterial3(), SDActions()
                .registerMethod("commit") { _, state -> commits++; state.updateStateFromAction("status", "salvo") }))
            store.put("test", vm)
            vm.updateState("ready", "true")
        }
        val root = parseNode("""{
          "component":"layout:column","id":"form","properties":{"paddingAll":16,"horizontalAlignment":"Center"},"children":[
            {"component":"layout:row","properties":{"horizontalArrangement":"Center"},"children":[
              {"component":"layout:box","properties":{"contentAlignment":"Center","paddingAll":4},"children":[
                {"component":"layout:text","properties":{"text":"Cadastro","fontWeight":"Bold","fontSize":18}}
              ]}
            ]},
            {"component":"layout:textField","id":"name","properties":{"text":"Visitante","onChangeUpdateState":"name","keyboardType":"Text","label":{"component":"layout:text","properties":{"text":"Nome"}}}},
            {"component":"layout:button","properties":{"enabled":"#{ready}","onClick":{"component":"action:ifState","properties":{"state":"ready","then":[{"component":"action:method","properties":{"invoke":"commit"}},{"component":"action:update","properties":{"state":"last","value":"confirmado"}}]}}},"children":[{"component":"layout:text","properties":{"text":"Salvar"}}]},
            {"component":"layout:text","properties":{"text":"#{name}: #{status} / #{last}"}}
          ]
        }""", documentId = "catalog-form")
        assertEquals(emptyList(), vm.catalog().validate(root, mode = ValidationMode.AUTHORING))
        compose.setContent { if (mounted.value) vm.loadComponent(root)?.Content() }
        awaitText("Cadastro")
        awaitText("Visitante")
        compose.onNodeWithText("Visitante").performTextReplacement("Ana")
        compose.onNodeWithText("Salvar").performClick()
        awaitText("Ana: salvo / confirmado")
        compose.runOnIdle { assertEquals(1, commits); assertNull(vm.errorState.value) }
    }

    private fun awaitText(text: String) {
        compose.waitUntil(5_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().size == 1 }
    }
}
