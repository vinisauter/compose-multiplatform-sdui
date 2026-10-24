package sample.app

import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.layout.SDLayoutMaterial3
import me.developes.humming.sdui.studio.*
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.jetbrains.skia.EncodedImageFormat
import java.io.File
import kotlin.test.*

class ReactiveRenderingTest {
    @get:Rule val compose = createComposeRule()
    private val store = ViewModelStore()
    private val mounted = mutableStateOf(true)
    @After fun cleanup() {
        compose.runOnIdle { mounted.value = false }
        compose.waitForIdle()
        compose.runOnIdle { store.clear() }
    }

    @Test fun authoredFixtureCompilesEditsHidesAndReappearsWithCurrentState() {
        val workspace = LocalWorkspace()
        workspace.importContract(REACTIVITY_EXAMPLE).requireValue()
        val result = workspace.compile().requireValue()
        val root = parseNode(result.artifacts.values.single())
        lateinit var vm: SDUIFormViewModel
        compose.runOnIdle {
            vm = SDUIFormViewModel(SavedStateHandle(), listOf(SDLayoutMaterial3()))
            store.put("test", vm)
        }
        compose.setContent { MaterialTheme { Surface(color = Color.White) { if (mounted.value) vm.loadComponent(root)?.Content() } } }
        compose.onNodeWithText("Olá, Visitante").assertExists()
        compose.onNode(hasSetTextAction()).performTextReplacement("Ana")
        compose.onNodeWithText("Olá, Ana").assertExists()
        compose.onNode(hasSetTextAction()).performTextReplacement("ocultar")
        compose.onNodeWithText("Olá, ocultar").assertDoesNotExist()
        compose.onNode(hasSetTextAction()).performTextReplacement("Voltei")
        compose.onNodeWithText("Olá, Voltei").assertExists()
        compose.runOnIdle { assertEquals(SduiValue.Text("Voltei"), vm.stateValue("nome")) }
        val output = File("../../build/t09/reactive-form-jvm.png").also { it.parentFile.mkdirs() }
        org.jetbrains.skia.Image.makeFromBitmap(compose.onRoot().captureToImage().asSkiaBitmap()).use { image ->
            requireNotNull(image.encodeToData(EncodedImageFormat.PNG)).use { output.writeBytes(it.bytes) }
        }
    }

    @Test fun typeFailureDisplaysFallbackAndLocatedDiagnosticThenRecoversAndDisposesCollectors() {
        val node = parseNode("""{"component":"layout:reactiveText","id":"typed-label","properties":{
          "value":{"version":1,"expression":{"language":"humming.expression","version":1,"source":"state.name"},"fallback":"Indisponível"}
        }}""")
        lateinit var vm: SDUIFormViewModel
        compose.runOnIdle {
            vm = SDUIFormViewModel(SavedStateHandle(), listOf(SDLayoutMaterial3()))
            store.put("test", vm); vm.updateState("name", "Inicial")
        }
        val tick = mutableStateOf(0)
        compose.setContent {
            if (mounted.value) {
                tick.value
                vm.loadComponent(node)?.Content()
            }
        }
        compose.onNodeWithText("Inicial").assertExists()
        repeat(4) {
            compose.runOnIdle { tick.value++ }
            compose.waitForIdle()
            compose.runOnIdle { assertEquals(1, vm.onStateUpdated.subscriptionCount.value) }
        }
        compose.runOnIdle { vm.updateValue("name", SduiValue.NumberValue("7")) }
        compose.onNodeWithText("Indisponível").assertExists()
        compose.onNodeWithText("REACTIVE_PROPERTY_TYPE: typed-label", substring = true).assertExists()
        compose.runOnIdle { vm.updateState("name", "Recuperado") }
        compose.onNodeWithText("Recuperado").assertExists()
        compose.onNodeWithText("REACTIVE_PROPERTY_TYPE", substring = true).assertDoesNotExist()
        compose.runOnIdle { mounted.value = false }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(0, vm.onStateUpdated.subscriptionCount.value) }
    }
}
