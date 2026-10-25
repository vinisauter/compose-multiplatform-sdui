package sample.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import me.developes.humming.sdui.action.SDActions
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.layout.*
import org.junit.Test
import org.junit.After
import org.junit.Rule
import kotlin.test.*

class Material3RenderingTest {
    @get:Rule val compose = createComposeRule()
    private val store = ViewModelStore()
    private val mounted = mutableStateOf(true)
    private lateinit var vm: SDUIFormViewModel
    @After fun cleanup() {
        compose.runOnIdle { mounted.value = false }; compose.waitForIdle()
        compose.runOnIdle { store.clear() }
    }
    private fun setup() { compose.runOnIdle {
        vm = SDUIFormViewModel(SavedStateHandle(), listOf(SDLayoutMaterial3(), SDActions()
            .registerMethod("snapshotAmount") { _, model -> model.updateStateFromAction("seen", model.getState("amount")) }
            .registerMethod("snapshotFilter") { _, model -> model.updateStateFromAction("first", model.getState("filter")) }
            .registerMethod("snapshotFirst") { _, model -> model.updateStateFromAction("second", model.getState("first")) })); store.put("material", vm)
    } }
    private fun show(source: String) {
        setup(); val node = parseNode(source)
        compose.setContent { MaterialTheme { androidx.compose.material3.Surface(Modifier.size(650.dp, 700.dp)) {
            if (mounted.value) Column(Modifier.width(600.dp)) { vm.loadComponent(node)?.Content() }
        } } }
    }
    @Test fun checkboxSwitchAndFieldUpdateOwnedStateAndAwaitedActionEvents() {
        show("""{"component":"layout:column","children":[
          {"component":"layout:checkbox","id":"terms","properties":{"stateKey":"terms","onChange":{"component":"action:update","properties":{"state":"status","value":"changed"}}}},
          {"component":"layout:switch","id":"switch","properties":{"stateKey":"switch","enabled":"#{enabled}"}},
          {"component":"layout:outlinedTextField","id":"field","properties":{"stateKey":"name","label":"Nome"}},
          {"component":"layout:materialTextButton","id":"enable","properties":{"onClick":{"component":"action:update","properties":{"state":"enabled","value":"true"}}},"children":[{"component":"layout:text","properties":{"text":"Habilitar"}}]}
        ]}""")
        compose.onNodeWithTag("terms").assertIsOff().performClick().assertIsOn()
        compose.onNodeWithTag("switch").assertIsNotEnabled()
        compose.onNodeWithText("Habilitar").performClick()
        compose.onNodeWithTag("switch").assertIsEnabled().performClick().assertIsOn()
        compose.onNodeWithTag("field").performTextReplacement("Ana")
        compose.runOnIdle {
            assertEquals("true", vm.getState("terms")); assertEquals("changed", vm.getState("status")); assertEquals("Ana", vm.getState("name"))
        }
    }
    @Test fun tabsAndSegmentedButtonsShareSelectionWithoutLosingPeerValues() {
        show("""{"component":"layout:column","children":[
          {"component":"layout:primaryTabRow","properties":{"stateKey":"tab"},"children":[
            {"component":"layout:tab","id":"tab-a","properties":{"text":"Primeira"}},
            {"component":"layout:tab","id":"tab-b","properties":{"text":"Segunda"}}]},
          {"component":"layout:singleChoiceSegmentedButtonRow","children":[
            {"component":"layout:segmentedButton","id":"seg-a","properties":{"stateKey":"segment","value":"a","text":"A"}},
            {"component":"layout:segmentedButton","id":"seg-b","properties":{"stateKey":"segment","value":"b","text":"B"}}]}
        ]}""")
        compose.onNodeWithTag("tab-b").performClick().assertIsSelected()
        compose.onNodeWithTag("tab-a").assertIsNotSelected()
        compose.onNodeWithTag("seg-a").performClick().assertIsSelected()
        compose.onNodeWithTag("seg-b").performClick().assertIsSelected()
        compose.onNodeWithTag("seg-a").assertIsNotSelected()
        compose.runOnIdle { assertEquals("1", vm.getState("tab")); assertEquals("b", vm.getState("segment")) }
    }
    @Test fun dialogAndMenuDismissThroughRegisteredActionsAndRecoverFromExternalState() {
        show("""{"component":"layout:column","children":[
          {"component":"layout:alertDialog","id":"dialog","properties":{"stateKey":"dialog","title":"Confirmar","text":"Conteúdo",
            "confirmButton":{"component":"layout:materialTextButton","id":"confirm","properties":{"onClick":{"component":"action:update","properties":{"state":"dialog","value":"false"}}},"children":[{"component":"layout:text","properties":{"text":"Fechar"}}]}}},
          {"component":"layout:dropdownMenu","id":"menu","properties":{"stateKey":"menu"},"children":[
            {"component":"layout:dropdownMenuItem","id":"item","properties":{"text":"Escolher","onClick":[
              {"component":"action:update","properties":{"state":"selection","value":"chosen"}},
              {"component":"action:update","properties":{"state":"menu","value":"false"}}]}}]}
        ]}""")
        compose.onNodeWithText("Confirmar").assertExists()
        compose.onNodeWithTag("confirm").performClick()
        compose.onNodeWithText("Confirmar").assertDoesNotExist()
        compose.runOnIdle { vm.updateState("menu", "true") }
        compose.onNodeWithText("Escolher").performClick()
        compose.onNodeWithText("Escolher").assertDoesNotExist()
        compose.runOnIdle { assertEquals("chosen", vm.getState("selection")); vm.updateState("dialog", "true") }
        compose.onNodeWithText("Confirmar").assertExists()
    }

    @Test fun everyNewAdapterRendersAgainstTheShippedMaterialApiAndDisposes() {
        setup()
        val current = mutableStateOf<ServerDrivenNode?>(null)
        compose.setContent { MaterialTheme { if (mounted.value) Box(Modifier.size(650.dp, 700.dp)) {
            current.value?.let { node -> key(node.component) { vm.loadComponent(node)?.Content() } }
        } } }
        for (name in Material3Definitions.names) {
            val leaf = ServerDrivenNode("layout:text", properties = mutableMapOf("text" to "Item"))
            val node = ServerDrivenNode("layout:$name", id = "smoke-$name", properties = mutableMapOf(
                "visible" to "false", "expanded" to "false"), children = null)
            // Supply only properties declared by each schema. Slots and receiver-sensitive children get valid parents below.
            node.properties!!.keys.retainAll(Material3Definitions.get(name).properties.map { it.name }.toSet())
            val wrapped = when (name) {
                "navigationBarItem" -> ServerDrivenNode("layout:navigationBar", children = mutableListOf(node))
                "segmentedButton" -> ServerDrivenNode("layout:singleChoiceSegmentedButtonRow", children = mutableListOf(node))
                "plainTooltip", "richTooltip" -> ServerDrivenNode("layout:tooltipBox", properties = mutableMapOf("tooltip" to """{"component":"layout:$name","properties":{"text":"Hint"}}"""), children = mutableListOf(leaf))
                "exposedDropdownMenu" -> ServerDrivenNode("layout:exposedDropdownMenuBox", children = mutableListOf(node))
                else -> node
            }
            compose.runOnIdle { current.value = wrapped }
            compose.waitForIdle()
            compose.runOnIdle { assertNull(vm.errorState.value, name) }
        }
    }

    @Test fun sliderAndChipCommitValuesBeforeTheirActionSequences() {
        show("""{"component":"layout:column","children":[
          {"component":"layout:slider","id":"slider","properties":{"stateKey":"amount","value":"0.25","onChange":{"component":"action:method","properties":{"invoke":"snapshotAmount"}}}},
          {"component":"layout:filterChip","id":"chip","properties":{"text":"Filtro","stateKey":"filter",
            "onChange":{"component":"action:method","properties":{"invoke":"snapshotFilter"}},
            "onClick":{"component":"action:method","properties":{"invoke":"snapshotFirst"}}}}
        ]}""")
        compose.onNodeWithTag("slider").performSemanticsAction(SemanticsActions.SetProgress) { it(.75f) }
        compose.onNodeWithTag("chip").performClick().assertIsSelected()
        compose.runOnIdle {
            assertEquals("0.75", vm.getState("amount")); assertEquals("0.75", vm.getState("seen"))
            assertEquals("true", vm.getState("first")); assertEquals("true", vm.getState("second"))
        }
    }

    @Test fun datePickerWritesUtcMillisAndTimeInputAcceptsExternalState() {
        setup()
        val current = mutableStateOf(parseNode("""{"component":"layout:datePicker","id":"date","properties":{"stateKey":"date","value":"1705276800000","showModeToggle":false}}"""))
        compose.setContent { MaterialTheme { if (mounted.value) Box(Modifier.size(650.dp, 700.dp)) {
            key(current.value.component) { vm.loadComponent(current.value)?.Content() }
        } } }
        java.io.File("build/reports/material-date-semantics.txt").also { it.parentFile.mkdirs(); it.writeText(compose.onRoot(useUnmergedTree = true).printToString()) }
        val days = compose.onAllNodes(isSelectable() and hasClickAction())
        val selectedDay = days.fetchSemanticsNodes().indexOfFirst { it.config[SemanticsProperties.Selected] }
        assertTrue(selectedDay >= 0)
        days[selectedDay + 1].performClick()
        compose.runOnIdle { assertEquals("1705363200000", vm.getState("date")) }
        compose.runOnIdle { current.value = parseNode("""{"component":"layout:timeInput","id":"time","properties":{"stateKey":"time","value":"08:30"}}""") }
        compose.onAllNodes(hasSetTextAction())[0].performTextReplacement("09")
        compose.runOnIdle { assertEquals("09:30", vm.getState("time")); vm.updateState("time", "23:59") }
        compose.onNode(hasText("23") and hasSetTextAction()).assertExists(); compose.onNode(hasText("59") and hasSetTextAction()).assertExists()
    }

    @Test fun studioGalleryRunsTheAuthoredControlsAndSaveAction() {
        show(MATERIAL3_EXAMPLE)
        compose.onNodeWithTag("material-name").performTextReplacement("Ana")
        compose.onNodeWithTag("material-check").performClick().assertIsOn()
        compose.onNodeWithTag("material-submit").performScrollTo().performClick()
        compose.onNodeWithText("Salvo").assertExists()
        compose.runOnIdle { assertEquals("Ana", vm.getState("material-name")) }
        val output = java.io.File("build/reports/material3-gallery.png").also { it.parentFile.mkdirs() }
        org.jetbrains.skia.Image.makeFromBitmap(compose.onRoot().captureToImage().asSkiaBitmap()).use { image ->
            requireNotNull(image.encodeToData(org.jetbrains.skia.EncodedImageFormat.PNG)).use { output.writeBytes(it.bytes) }
        }
    }

    @Test fun exposedMenuUsesItsAnchorAndSharedExpandedState() {
        show("""{"component":"layout:exposedDropdownMenuBox","id":"box","properties":{"stateKey":"expanded"},"children":[
          {"component":"layout:outlinedTextField","id":"anchor","properties":{"stateKey":"query","text":"Escolha","readOnly":true}},
          {"component":"layout:exposedDropdownMenu","id":"options","properties":{"stateKey":"expanded"},"children":[
            {"component":"layout:dropdownMenuItem","id":"option","properties":{"text":"Opção","onClick":[
              {"component":"action:update","properties":{"state":"query","value":"Escolhido"}},
              {"component":"action:update","properties":{"state":"expanded","value":"false"}}]}}
          ]}
        ]}""")
        compose.onNodeWithText("Escolha").performClick()
        compose.onNodeWithText("Opção").assertIsDisplayed().performClick()
        compose.onNodeWithText("Opção").assertDoesNotExist()
        compose.onNodeWithText("Escolhido").assertExists()
        compose.runOnIdle { assertEquals("false", vm.getState("expanded")) }
    }

    @Test fun rangeSliderKeepsBothEndpointsAndUpdatesTheSecondThumb() {
        show("""{"component":"layout:rangeSlider","id":"range","properties":{"stateKey":"start","endStateKey":"end","value":"0.2","endValue":"0.8"}}""")
        val thumbs = compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
        thumbs.assertCountEquals(2)
        thumbs[1].performSemanticsAction(SemanticsActions.SetProgress) { it(.9f) }
        compose.runOnIdle {
            assertEquals(.2, vm.getState("start").toDouble(), .001)
            assertEquals(.9, vm.getState("end").toDouble(), .001)
        }
    }

    @Test fun disabledFloatingActionButtonExposesDisabledSemanticsAndDoesNotDispatch() {
        show("""{"component":"layout:floatingActionButton","id":"fab","properties":{"enabled":false,
          "onClick":{"component":"action:update","properties":{"state":"fired","value":"yes"}}},
          "children":[{"component":"layout:text","properties":{"text":"Adicionar"}}]}""")
        compose.onNodeWithTag("fab").assertIsNotEnabled().performClick()
        compose.runOnIdle { assertEquals(SduiValue.Missing, vm.stateValue("fired")) }
    }

    @Test fun radioWithoutExplicitKeyRetainsItsGeneratedOwnedSelection() {
        show("""{"component":"layout:radioButton","id":"radio"}""")
        compose.onNodeWithTag("radio").assertIsNotSelected().performClick().assertIsSelected()
        compose.runOnIdle { assertEquals("true", vm.getState("material3:radio:selected")) }
    }
}
