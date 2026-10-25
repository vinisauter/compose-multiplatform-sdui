package sample.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import me.developes.humming.sdui.HummingSDUI
import me.developes.humming.sdui.action.SDActions
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.layout.SDLayoutMaterial3
import me.developes.humming.sdui.layout.components.Material3Theme
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

/** The host explicitly supplies a platform registry; the preview never resolves a native implementation. */
@Composable
fun NativeCapabilityExample(bridge: NativeBridge, readOnStart: Boolean = false) {
    var generation by remember { mutableIntStateOf(0) }
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
                Text("Capacidade nativa local", style = MaterialTheme.typography.titleLarge)
                Text(if ("device.uptime" in bridge.capabilities) "Uptime registrado pelo host" else "Uptime indisponível neste host")
                if (readOnStart) Text("Este exemplo lê o uptime ao abrir o formulário")
                TextButton(onClick = { generation++ }) { Text("Recriar formulário") }
                key(bridge, generation, readOnStart) {
                    val store = remember { object : ViewModelStoreOwner { override val viewModelStore = ViewModelStore() } }
                    DisposableEffect(store) { onDispose { store.viewModelStore.clear() } }
                    val config = remember {
                        HummingSDUI.Config("local", "native", theme = Material3Theme(),
                            errorHandler = { error, owner ->
                                AlertDialog(onDismissRequest = owner::dismissError, title = { Text(error.code) },
                                    text = { Text(error.message) }, confirmButton = {
                                        TextButton(onClick = owner::dismissError) { Text("Fechar") }
                                    })
                            },
                        ) {
                            updateState("result", "Aguardando chamada")
                            addLibrary(SDLayoutMaterial3())
                            addLibrary(SDActions().registerMethod("readUptime") { _, owner ->
                                owner.updateValueFromAction("uptime", SduiValue.Missing)
                                owner.updateValueFromAction("result", SduiValue.Text("NATIVE_PENDING"))
                                when (val result = bridge.invoke(NativeRequest("device.uptime"))) {
                                    is NativeResult.Success -> {
                                        owner.updateValueFromAction("uptime", result.value)
                                        owner.updateValueFromAction("result", SduiValue.Text("NATIVE_SUCCESS"))
                                    }
                                    is NativeResult.Rejected -> owner.updateValueFromAction("result", SduiValue.Text(result.code))
                                    NativeResult.Unavailable -> owner.updateValueFromAction("result", SduiValue.Text("NATIVE_UNAVAILABLE"))
                                }
                            })
                            addNodeProvider("local") {
                                val source = if (!readOnStart) NATIVE_EXAMPLE else {
                                    val root = parseBoundedJson(NATIVE_EXAMPLE) as JsonObject
                                    JsonObject(root + ("children" to JsonArray(
                                        listOf(parseBoundedJson(NATIVE_START)) + (root.getValue("children") as JsonArray)
                                    ))).toString()
                                }
                                parseNode(source)
                            }
                        }
                    }
                    CompositionLocalProvider(LocalViewModelStoreOwner provides store) {
                        HummingSDUI().Content(config, savedStateHandleFactory = { SavedStateHandle() })
                    }
                }
            }
        }
    }
}

private const val NATIVE_EXAMPLE = """{"id":"native","component":"layout:column","children":[{"id":"read","component":"layout:button","properties":{"onClick":{"id":"call","component":"action:method","properties":{"invoke":"readUptime"}}},"children":[{"id":"read-label","component":"layout:text","properties":{"text":"Ler uptime"}}]},{"id":"result","component":"layout:text","properties":{"text":"Resultado: #{result}"}},{"id":"uptime","component":"layout:text","properties":{"text":"Uptime ms: #{uptime}"}}]}"""
private const val NATIVE_START = """{"id":"native-start","component":"action:event","properties":{"onCreate":{"id":"native-start-call","component":"action:method","properties":{"invoke":"readUptime"}}}}"""
