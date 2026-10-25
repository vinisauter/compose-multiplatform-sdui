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
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import me.developes.humming.sdui.HummingSDUI
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.layout.SDLayoutMaterial3
import me.developes.humming.sdui.layout.components.Material3Theme
import me.developes.humming.sdui.navigation.*
import me.developes.humming.sdui.navigation.components.Graph
import kotlin.time.Clock

/** Local host example: snapshots survive only this composition, never a browser/process restart. */
@Composable
fun VersionedJourneyExample() {
    var session by remember { mutableIntStateOf(1) }
    var generation by remember { mutableIntStateOf(0) }
    val snapshots = remember { mutableMapOf<DefinitionKey, String>() }
    val codec = remember { DefinitionSnapshotCodec(nowMillis = { Clock.System.now().toEpochMilliseconds() }) }
    MaterialTheme {
        key(session, generation) {
            val scope = rememberCoroutineScope()
            val cache = remember { JourneyDefinitionCache(scope) }
            val store = remember { object : ViewModelStoreOwner { override val viewModelStore = ViewModelStore() } }
            var graph by remember { mutableStateOf<Graph?>(null) }
            var root by remember { mutableStateOf<SDUIFormViewModel?>(null) }
            var failNext by remember { mutableStateOf(false) }
            var sources by remember { mutableIntStateOf(0) }
            var restored by remember { mutableIntStateOf(0) }
            var metrics by remember { mutableStateOf("Consulte as métricas após navegar") }
            DisposableEffect(cache, store) {
                onDispose {
                    store.viewModelStore.clear()
                    scope.launch(start = CoroutineStart.UNDISPATCHED) { cache.close() }
                }
            }
            val definitions = remember {
                JourneyDefinitions("local-example", "v1", "session-$session", cache) { requested ->
                    if (failNext) {
                        failNext = false
                        throw PayloadException(NodeDiagnostic("DEFINITION_UNAVAILABLE", "Falha local solicitada pelo host", "loadDefinition"))
                    }
                    snapshots[requested]?.let {
                        val snapshot = codec.decode(it, requested)
                        restored++
                        return@JourneyDefinitions snapshot
                    }
                    sources++
                    DefinitionSnapshot(requested, when (requested.route) {
                        "home" -> JOURNEY_HOME
                        "detail" -> JOURNEY_DETAIL
                        else -> error("Unknown example route")
                    }).also { snapshots[requested] = codec.encode(it) }
                }
            }
            val config = remember {
                HummingSDUI.Config("local", "graph", theme = Material3Theme(),
                    screenStateMode = ScreenStateMode.ISOLATED_ENTRIES,
                    entryInputs = { _, route -> ScreenInputs(nav = SduiValue.ObjectValue(mapOf("route" to SduiValue.Text(route)))) },
                    errorHandler = { error, owner ->
                        AlertDialog(onDismissRequest = owner::dismissError,
                            title = { Text("Falha: ${error.code}") },
                            text = { Text(error.message) },
                            confirmButton = { TextButton(onClick = {
                                owner.dismissError()
                                root?.runTask(block = { graph?.navigateToAwaited("home", clearStack = true) },
                                    onError = { if (it is PayloadException) ErrorData.payload(it) else null })
                            }) { Text("Recuperar início") } },
                        )
                    },
                ) {
                    root = this
                    addLibrary(SDLayoutMaterial3())
                    val navigation = SDNavigation(definitions)
                    val handler = checkNotNull(navigation.getComponent("graph"))
                    navigation.addComponent("graph") { node, owner -> handler(node, owner).also { graph = it as Graph } }
                    addLibrary(navigation)
                    addNodeProvider("local") { parseNode(JOURNEY_GRAPH) }
                }
            }
            Surface {
                Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Jornada local — sessão $session", style = MaterialTheme.typography.titleLarge)
                    Text("Fontes locais: $sources · snapshots restaurados: $restored")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { graph?.openDeepLink("https://journey.example/detail") }) { Text("Link para detalhe") }
                        TextButton(onClick = {
                            root?.runTask(block = { graph?.reloadRoute("home") },
                                onError = { if (it is PayloadException) ErrorData.payload(it) else null })
                        }) { Text("Recarregar início") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { failNext = true }) { Text(if (failNext) "Falha preparada" else "Falhar próxima carga") }
                        TextButton(onClick = { generation++ }) { Text("Restaurar definições") }
                    }
                    Text("Restaurar recria o host e limpa o formulário. Snapshots ficam apenas na memória desta página.")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            snapshots.keys.filter { it.session == "session-$session" }.forEach { snapshots.remove(it) }
                            session++
                        }) { Text("Trocar sessão") }
                        TextButton(onClick = { scope.launch {
                            val value = cache.metrics()
                            metrics = "Hits: ${value.hits} · cargas: ${value.providerLoads} · entradas: ${value.entries} · espera: ${value.loadMillis} ms"
                        } }) { Text("Métricas") }
                    }
                    Text(metrics)
                    CompositionLocalProvider(LocalViewModelStoreOwner provides store) {
                        Box(Modifier.weight(1f)) { HummingSDUI().Content(config, savedStateHandleFactory = { SavedStateHandle() }) }
                    }
                }
            }
        }
    }
}

private const val JOURNEY_GRAPH = """{"id":"graph","component":"navigation:graph","properties":{"startDestination":"home"},"children":[{"id":"home-route","component":"navigation:node","properties":{"name":"home","type":"host","destiny":"home.json"}},{"id":"detail-route","component":"navigation:node","properties":{"name":"detail","type":"host","destiny":"detail.json","deepLinks":["https://journey.example/detail"]}}]}"""
private const val JOURNEY_HOME = """{"id":"home","component":"layout:column","children":[{"id":"name","component":"layout:textField","properties":{"text":"inicial","onChangeUpdateState":"nome"}},{"id":"value","component":"layout:text","properties":{"text":"Nome: #{nome}"}},{"id":"open","component":"layout:button","properties":{"onClick":{"id":"go","component":"navigation:goTo","properties":{"destiny":"detail"}}},"children":[{"id":"open-label","component":"layout:text","properties":{"text":"Abrir detalhe"}}]}]}"""
private const val JOURNEY_DETAIL = """{"id":"detail","component":"layout:column","children":[{"id":"detail-title","component":"layout:text","properties":{"text":"Detalhe carregado"}},{"id":"back","component":"layout:button","properties":{"onClick":{"id":"go-back","component":"navigation:goBack"}},"children":[{"id":"back-label","component":"layout:text","properties":{"text":"Voltar ao formulário"}}]}]}"""
