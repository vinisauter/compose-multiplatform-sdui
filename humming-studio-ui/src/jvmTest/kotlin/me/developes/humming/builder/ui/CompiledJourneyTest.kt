package me.developes.humming.builder.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import kotlinx.serialization.json.JsonObject
import me.developes.humming.sdui.HummingSDUI
import me.developes.humming.sdui.action.SDActions
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.initialNodeCatalog
import me.developes.humming.sdui.common.parseNode
import me.developes.humming.sdui.layout.SDLayoutMaterial3
import me.developes.humming.sdui.layout.components.Material3Theme
import me.developes.humming.sdui.navigation.SDNavigation
import me.developes.humming.sdui.studio.AuthorEdge
import me.developes.humming.sdui.studio.AuthoringDocument
import me.developes.humming.sdui.studio.ContractCompiler
import me.developes.humming.sdui.studio.EdgeRole
import me.developes.humming.sdui.studio.LegacyAuthoringCodec
import me.developes.humming.sdui.studio.Placement
import me.developes.humming.sdui.studio.TargetCapabilities
import me.developes.humming.sdui.studio.generateKeyBetween
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.junit.After
import org.junit.Rule
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CompiledJourneyTest {
    @get:Rule val compose = createComposeRule()
    private val mounted = mutableStateOf(true)
    private val owner = object : ViewModelStoreOwner, LifecycleOwner {
        override val viewModelStore = ViewModelStore()
        override val lifecycle = LifecycleRegistry(this)
    }

    @After fun cleanup() {
        compose.runOnIdle { mounted.value = false }
        compose.waitForIdle()
        compose.runOnIdle { owner.lifecycle.currentState = Lifecycle.State.DESTROYED; owner.viewModelStore.clear() }
    }

    @Test fun compiledContractsDriveStateNavigationAndReturnThroughTheRealRuntime() {
        val catalog = initialNodeCatalog()
        val bridge = LegacyAuthoringCodec(catalog)
        val a = bridge.importDocument("""{"component":"layout:column","id":"A","children":[{"component":"layout:text","id":"titleA","properties":{"text":"Primeira tela"}},{"component":"layout:button","id":"open","properties":{"onClick":[{"component":"navigation:goTo","id":"goAB","properties":{"destiny":"placeholder"}},{"component":"action:ifState","id":"condition","properties":{"state":"ready","then":[{"component":"action:method","id":"record","properties":{"invoke":"record"}},{"component":"action:update","id":"visited","properties":{"state":"visited","value":"sim"}}]}}]},"children":[{"component":"layout:text","id":"openLabel","properties":{"text":"Abrir segunda"}}]}]}""", "a").requireValue()
        val b = bridge.importDocument("""{"component":"layout:column","id":"B","children":[{"component":"layout:text","id":"titleB","properties":{"text":"Segunda: #{visited} / #{method}"}},{"component":"layout:button","id":"back","properties":{"onClick":{"component":"navigation:goBack","id":"goBack"}},"children":[{"component":"layout:text","id":"backLabel","properties":{"text":"Voltar"}}]}]}""", "b").requireValue()
        val nodes = (a.nodes + b.nodes).toMutableMap()
        nodes["goAB"] = nodes.getValue("goAB").copy(properties = JsonObject(emptyMap()))
        var previous: String? = null
        nodes.values.filter { it.placement is Placement.Root }.sortedBy { it.id }.forEach { node ->
            val key = generateKeyBetween(previous, null)
            nodes[node.id] = node.copy(placement = Placement.Root(key))
            previous = key
        }
        val edges = (a.edges.values.map { it.copy(id = "a-${it.id}") } + b.edges.values.map { it.copy(id = "b-${it.id}") } +
            AuthorEdge("navigation", EdgeRole.NAVIGATION, "goAB", "navigate", "B", "entry")).associateBy { it.id }
        val document = AuthoringDocument("runtime-integration", entryNodeId = "A", nodes = nodes, edges = edges)
        val compiled = ContractCompiler(catalog).compileJourney(document,
            TargetCapabilities(catalog.capabilities, methods = setOf("record"), providers = setOf("compiled")), "compiled").requireValue()
        val output = File("build/reports/compiled-journey")
        compiled.artifacts.forEach { (destination, source) ->
            File(output, destination).apply { parentFile.mkdirs(); writeText(source) }
        }
        lateinit var vm: SDUIFormViewModel
        val config = HummingSDUI.Config(type = "compiled", destiny = compiled.entryDestination, theme = Material3Theme()) {
            vm = this
            addLibrary(SDActions().registerMethod("record") { _, state -> state.updateStateFromAction("method", "executado") })
            updateState("ready", "true")
            addLibrary(SDLayoutMaterial3())
            addLibrary(SDNavigation())
            addNodeProvider("compiled") { destination ->
                parseNode(checkNotNull(compiled.artifacts[destination]), documentId = destination)
            }
        }
        compose.runOnIdle { owner.lifecycle.currentState = Lifecycle.State.RESUMED }
        compose.setContent {
            if (mounted.value) CompositionLocalProvider(LocalViewModelStoreOwner provides owner, LocalLifecycleOwner provides owner) {
                HummingSDUI().Content(config, savedStateHandleFactory = { SavedStateHandle() })
            }
        }
        awaitText("Primeira tela")
        compose.onNodeWithText("Abrir segunda").performClick()
        awaitText("Segunda: sim / executado")
        compose.runOnIdle { assertEquals("sim", vm.formState["visited"]); assertNull(vm.errorState.value) }
        val screenshot = compose.onRoot().captureToImage()
        Image.makeFromBitmap(screenshot.asSkiaBitmap()).use { image ->
            requireNotNull(image.encodeToData(EncodedImageFormat.PNG)).use { data -> File(output, "second-screen.png").writeBytes(data.bytes) }
        }
        compose.onNodeWithText("Voltar").performClick()
        awaitText("Primeira tela")
        compose.runOnIdle { assertNull(vm.errorState.value) }
    }

    private fun awaitText(text: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().size == 1 }
    }
}
