package sample.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import io.ktor.client.HttpClient
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import me.developes.humming.sdui.action.*
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.layout.SDLayoutMaterial3
import me.developes.humming.sdui.network.*
import me.developes.humming.sdui.studio.*

/** Host-assigned loopback service. Never constructed from an SDUI payload. Host closes HttpClient. */
class CatalogServices(private val client: HttpClient, val documentId: String, private val base: String, private val token: String) {
    init {
        val url = io.ktor.http.Url(base)
        require(url.protocol.name == "http" && url.host in setOf("localhost", "127.0.0.1") && url.user.isNullOrEmpty() && url.password.isNullOrEmpty())
        require(Regex("[A-Za-z0-9][A-Za-z0-9_-]{0,127}").matches(documentId))
        require(url.parameters.names().isEmpty() && url.fragment.isEmpty())
        require(Regex("/v1/tenants/[A-Za-z0-9][A-Za-z0-9_-]{0,127}/documents/${documentId}").matches(url.encodedPath))
        require(token.length in 32..256 && token.all { it.code in 33..126 })
    }
    private fun transport(suffix: String) = SduiHttpTransport(client, mapOf("catalog" to HttpEndpoint(base + suffix,
        methods = setOf("GET", "POST"), headers = mapOf("Authorization" to "Bearer $token"))), HttpPolicy(maxResponseBytes = 8_000_000))
    val draftTransport: SduiHttpTransport get() = transport("")
    private suspend fun call(suffix: String, method: String = "GET", body: SduiValue = SduiValue.Missing): JsonElement {
        return when (val result = transport(suffix).request("catalog", method, body)) {
            is HttpOutcome.Success -> result.body.toJsonValue()
            is HttpOutcome.Failure -> throw CatalogServiceFailure(result.code, result.status)
        }
    }
    /** Creates only when absent, preserving existing user drafts. Never replaces an existing document. */
    suspend fun prepareDraft() {
        try { call("") } catch (error: CatalogServiceFailure) {
            if (error.status != 404) throw error
            val document = OfficialCatalog.serverExample.authoring().copy(documentId = documentId)
            call("", "POST", parseSduiValue(AuthoringDocumentCodec(initialNodeCatalog()).encode(document).requireValue()))
        }
    }
    suspend fun publish(): PublishedDefinition {
        prepareDraft()
        val revision = call("").jsonObject.getValue("revision").jsonPrimitive.long
        val result = call("/publications", "POST", parseSduiValue("""{"expectedRevision":$revision,"profile":"local-tree"}"""))
        val envelope = PublishedContractsCodec().decode(result.toString()).requireValue()
        val loaded = call("/publications/${envelope.version}/clients/local-tree")
        return PublishedDefinition.decode(loaded.toString(), envelope.version, documentId, initialNodeCatalog(),
            TargetCapabilities(initialNodeCatalog().capabilities, methods = setOf("getPlatformName"), colorTokens = defaultPresentationTokens().colorNames,
                maskTokens = defaultPresentationTokens().maskNames)).requireValue()
    }
    suspend fun data(version: String): SduiValue.ObjectValue {
        require(Regex("[a-f0-9]{64}").matches(version))
        val result = call("/publications/$version/data/local-tree/catalog-system").jsonObject
        return parseSduiValue(result.getValue("values").toString()) as SduiValue.ObjectValue
    }
}

class CatalogServiceFailure(val code: String, val status: Int?) : Exception("$code${status?.let { " ($it)" }.orEmpty()}")

@Composable fun CatalogServicesPanel(services: CatalogServices) {
    val transport = remember(services) { services.draftTransport }
    val scope = rememberCoroutineScope()
    var pinned by remember { mutableStateOf<PublishedDefinition?>(null) }
    var values by remember { mutableStateOf<SduiValue.ObjectValue?>(null) }
    var message by remember { mutableStateOf("Conexão loopback configurada pelo host; nenhum pedido automático") }
    var busy by remember { mutableStateOf(false) }
    var generation by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Text("Servidor, publicação e dados", style = MaterialTheme.typography.titleLarge)
        Text("Cria somente se ausente; publica o rascunho configurado sem substituí-lo.", style = MaterialTheme.typography.bodySmall)
        Text(message)
        FlowRow {
            Button(enabled = !busy, onClick = { scope.launch {
                busy = true
                try { pinned = services.publish(); values = null; generation++; message = "Publicação fixada: ${pinned!!.version}" }
                catch (error: CatalogServiceFailure) { message = error.message.orEmpty() }
                finally { busy = false }
            } }) { Text("Preparar e publicar") }
            TextButton(enabled = !busy && pinned != null, onClick = { scope.launch {
                busy = true
                try { values = services.data(pinned!!.version); generation++; message = "Dados de sessão recebidos; nova entrada preserva o snapshot" }
                catch (error: CatalogServiceFailure) { message = "${error.message}; BFF exige runLocal --args=--catalog-demo" }
                finally { busy = false }
            } }) { Text("Carregar dados BFF") }
        }
        values?.let { Text("Dados reais: ${it.toJsonValue()}") }
        val definition = pinned
        definition?.let { Text("Versão fixada: ${it.version}", style = MaterialTheme.typography.bodySmall) }
        if (definition != null) Box(Modifier.weight(1f)) { key(definition.version, generation) { CatalogPinnedPreview(definition, values) } }
        else Box(Modifier.weight(1f)) { CatalogHttpExample(transport) }
    }
}

@Composable private fun CatalogPinnedPreview(definition: PublishedDefinition, values: SduiValue.ObjectValue?) {
    val store = remember { ViewModelStore() }
    val vm = remember {
        SDUIFormViewModel(SavedStateHandle(), listOf(SDLayoutMaterial3(), SDActions()),
            screenInputs = ScreenInputs(server = values ?: SduiValue.ObjectValue(emptyMap()))).also {
            store.put("published", it); it.addNodeProvider("published", definition::node)
        }
    }
    DisposableEffect(store) { onDispose { store.clear() } }
    var content by remember { mutableStateOf<ComposableContent?>(null) }
    LaunchedEffect(vm) { content = vm.loadComponent("published", definition.entryDestination) }
    content?.Content()
}

@Composable fun CatalogHttpExample(transport: SduiHttpTransport) {
    val compiled = remember { catalogHttpCompiled() }
    val store = remember { ViewModelStore() }
    val vm = remember(transport) { SDUIFormViewModel(SavedStateHandle(), listOf(SDLayoutMaterial3(), SDNetwork(transport),
        SDActions().registerMethod("catalogHttpSuccess") { _, owner -> owner.updateStateFromAction("http", owner.actionValues().response.toJsonValue().toString()) }
            .registerMethod("catalogHttpFailure") { _, owner -> owner.updateStateFromAction("http", owner.actionValues().error.toJsonValue().toString()) }
    )).also { owner -> store.put("http", owner); owner.addNodeProvider("catalog-http") { path ->
        parseNode(checkNotNull(compiled.artifacts[path]), documentId = compiled.documentId)
    } } }
    DisposableEffect(store) { onDispose { store.clear() } }
    var content by remember(vm) { mutableStateOf<ComposableContent?>(null) }
    LaunchedEffect(vm) { content = vm.loadComponent("catalog-http", compiled.entryDestination) }
    content?.Content()
}

internal fun catalogHttpCompiled(): CompiledContracts {
    val catalog = NodeCatalog(initialNodeCatalog().definitions + NetworkDefinitions.request)
    val document = LegacyAuthoringCodec(catalog).importDocument(CATALOG_HTTP_EXAMPLE, "catalog-http").requireValue()
    return ContractCompiler(catalog).compileTree(document, TargetCapabilities(catalog.capabilities,
        methods = setOf("catalogHttpSuccess", "catalogHttpFailure"))).requireValue()
}

internal const val CATALOG_HTTP_EXAMPLE = """{"component":"layout:column","id":"http-form","children":[
 {"component":"layout:button","id":"http-call","properties":{"onClick":{"component":"network:request","id":"http-request","properties":{"endpoint":"catalog","method":"GET",
 "onSuccess":{"component":"action:method","id":"http-success","properties":{"invoke":"catalogHttpSuccess"}},
 "onFailure":{"component":"action:method","id":"http-failure","properties":{"invoke":"catalogHttpFailure"}}}}},"children":[{"component":"layout:text","id":"http-label","properties":{"text":"Consultar rascunho por HTTP SDUI"}}]},
 {"component":"layout:text","id":"http-result","properties":{"text":"HTTP: #{http}"}}
]}"""
