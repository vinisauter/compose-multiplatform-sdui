package sample.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import br.com.developes.sdui.resources.Res
import br.com.developes.sdui.resources.ic_eye
import kotlinx.serialization.json.*
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.layout.components.LocalImageSources
import me.developes.humming.sdui.studio.*
import org.jetbrains.compose.resources.painterResource

@Composable
fun OfficialCatalogApp(files: StudioFiles? = null, bridge: NativeBridge? = null,
    remoteContent: (@Composable () -> Unit)? = null, servicesContent: (@Composable () -> Unit)? = null,
    initialExampleId: String = "flow-form") {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(initialExampleId) }
    var category by remember { mutableStateOf("Todos") }
    var generation by remember { mutableIntStateOf(0) }
    var editor by remember { mutableStateOf<LocalWorkspace?>(null) }
    var editorRevision by remember { mutableLongStateOf(0) }
    var leavingEditor by remember { mutableStateOf(false) }
    val native = bridge ?: remember { NativeBridge(emptyMap()) }
    val assets = remember { mapOf("catalog-mark" to @Composable { painterResource(Res.drawable.ic_eye) }) }
    CompositionLocalProvider(LocalImageSources provides assets) {
        MaterialTheme {
            Surface(Modifier.fillMaxSize()) {
                val workspace = editor
                if (workspace != null) Column {
                    TextButton(onClick = { if (workspace.document.revision != editorRevision) leavingEditor = true else editor = null }) { Text("Voltar ao catálogo") }
                    Box(Modifier.weight(1f)) { LocalStudio(files, workspace) }
                    if (leavingEditor) AlertDialog(onDismissRequest = { leavingEditor = false }, title = { Text("Sair do Studio?") },
                        text = { Text("Há alterações nesta sessão. Exporte o documento no Studio para preservá-las.") },
                        confirmButton = { TextButton(onClick = { leavingEditor = false; editor = null }) { Text("Sair sem salvar") } },
                        dismissButton = { TextButton(onClick = { leavingEditor = false }) { Text("Continuar editando") } })
                } else BoxWithConstraints {
                    val compact = maxWidth < 720.dp
                    val entries = OfficialCatalog.all
                    val special = listOf("studio" to "Studio e arquivos", "journey" to "Navegação e conectores", "isolation" to "Estado por entrada", "cache" to "Cache e recuperação",
                        "native" to "Capacidades nativas", "services" to "Publicação, dados e HTTP", "remote" to "Edição remota e colaboração")
                    val filtered = entries.filter { (category == "Todos" || it.category == category) &&
                        (it.title.contains(query, true) || it.component.orEmpty().contains(query, true) || it.description.contains(query, true)) }
                    Column(Modifier.fillMaxSize().padding(16.dp)) {
                        Text("HummingUI • Catálogo oficial", style = MaterialTheme.typography.headlineSmall)
                        Text("${OfficialCatalog.components.size} componentes • ${OfficialCatalog.actions.size} ações • exemplos executados pelo SDUI", style = MaterialTheme.typography.bodySmall)
                        OutlinedTextField(query, { query = it }, label = { Text("Buscar componente ou recurso") }, singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("catalog-search"))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            (listOf("Todos") + entries.map { it.category }.distinct()).forEach { group ->
                                FilterChip(category == group, { category = group }, { Text(group) })
                            }
                        }
                        @Composable fun navigation(modifier: Modifier) {
                            LazyColumn(modifier.testTag("catalog-list")) {
                                item { Text("Componentes e fluxos", style = MaterialTheme.typography.titleSmall) }
                                items(filtered, key = { it.id }) { entry ->
                                    TextButton(onClick = { selected = entry.id; generation = 0 }, modifier = Modifier.fillMaxWidth().testTag("catalog-entry-${entry.id}")) {
                                        Text((if (selected == entry.id) "• " else "") + entry.title, modifier = Modifier.fillMaxWidth())
                                    }
                                }
                                item { HorizontalDivider(); Text("Integrações", style = MaterialTheme.typography.titleSmall) }
                                items(special) { (id, title) -> TextButton(onClick = { selected = id; generation = 0 }, modifier = Modifier.testTag("catalog-special-$id")) { Text(title) } }
                            }
                        }
                        @Composable fun detail(modifier: Modifier) {
                            Box(modifier.testTag("catalog-detail")) {
                                key(selected, generation) {
                                    val entry = entries.firstOrNull { it.id == selected }
                                    if (entry != null) CatalogExampleDetail(entry, files, onReset = { generation++ }, onEdit = {
                                        editor = LocalWorkspace(entry.authoring(), methods = entry.methods); editorRevision = editor!!.document.revision
                                    }) else when (selected) {
                                        "studio" -> LocalStudio(files = files)
                                        "native" -> NativeCapabilityExample(native)
                                        "cache" -> VersionedJourneyExample()
                                        "journey", "isolation" -> {
                                            val doc = remember { if (selected == "journey") journeyExample() else isolationExample() }
                                            val w = remember { LocalWorkspace(doc) }
                                            Column {
                                                TextButton(onClick = { editor = w; editorRevision = w.document.revision }) { Text("Editar jornada e conectores no Studio") }
                                                Box(Modifier.weight(1f)) { CompiledPreview(remember { w.compilePreview().requireValue() }) }
                                            }
                                        }
                                        "remote" -> if (remoteContent != null) remoteContent() else CapabilityInstructions("Edição remota e colaboração",
                                            "Este host não instalou HTTP/storage remoto. No desktop, rode --catalog com HUMMING_STUDIO_* conforme sample/README.md. A conexão usa outbox, revisão, arquivo, presença e undo reais.")
                                        "services" -> if (servicesContent != null) servicesContent() else CapabilityInstructions("Publicação, dados e HTTP",
                                            "Configure o servidor PostgreSQL e o catálogo desktop. O host fornece endpoints e credenciais; payloads não escolhem URLs. Veja sample/README.md para publicar, fixar versão e carregar dados reais.")
                                    }
                                }
                            }
                        }
                        if (compact) {
                            navigation(Modifier.fillMaxWidth().height(150.dp))
                            HorizontalDivider(); detail(Modifier.fillMaxWidth().weight(1f))
                        } else Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            navigation(Modifier.width(240.dp).fillMaxHeight())
                            VerticalDivider(); detail(Modifier.weight(1f).fillMaxHeight())
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun CapabilityInstructions(title: String, message: String) {
    Column(Modifier.padding(16.dp)) { Text(title, style = MaterialTheme.typography.titleLarge); Text(message) }
}

@Composable private fun CatalogExampleDetail(entry: CatalogExample, files: StudioFiles?, onReset: () -> Unit, onEdit: () -> Unit) {
    val document = remember(entry) { entry.authoring() }
    val compiled = remember(entry) { entry.compiled() }
    var tab by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("") }
    val codec = remember { AuthoringDocumentCodec(initialNodeCatalog()) }
    val pretty = remember { Json { prettyPrint = true } }
    Column(Modifier.fillMaxSize()) {
        Text(entry.component ?: entry.title, style = MaterialTheme.typography.titleLarge)
        Text(entry.description, style = MaterialTheme.typography.bodySmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TextButton(onClick = onReset, modifier = Modifier.testTag("catalog-reset")) { Text("Reiniciar exemplo") }
            TextButton(onClick = onEdit, modifier = Modifier.testTag("catalog-edit")) { Text("Abrir no Studio") }
            TextButton(enabled = files != null, onClick = {
                files?.download("${entry.id}.json", codec.encode(document).requireValue()); message = "Download do documento solicitado"
            }) { Text("Exportar documento") }
            TextButton(enabled = files != null, onClick = {
                files?.download("${entry.id}-contracts.json", CompiledBundleCodec().encode(compiled).requireValue()); message = "Download dos contratos solicitado"
            }) { Text("Exportar contratos") }
        }
        if (message.isNotBlank()) Text(message)
        ScrollableTabRow(tab) { listOf("Executar", "JSON SDUI", "Documento", "Contrato", "Schema").forEachIndexed { index, label ->
            Tab(tab == index, { tab = index }, text = { Text(label) })
        } }
        Box(Modifier.fillMaxWidth().weight(1f).testTag("catalog-preview")) {
            if (tab == 0) CompiledPreview(compiled)
            else {
                val source = when (tab) {
                    1 -> pretty.encodeToString(JsonElement.serializer(), parseBoundedJson(entry.source))
                    2 -> pretty.encodeToString(JsonElement.serializer(), parseBoundedJson(codec.encode(document).requireValue()))
                    3 -> CompiledBundleCodec().encode(compiled).requireValue()
                    else -> initialNodeCatalog()[entry.component.orEmpty()]?.let { schema ->
                        "${schema.identifier} • versão ${schema.version}\nchildren: ${schema.childrenRole}\n" + schema.properties.joinToString("\n") {
                            "${it.name}: ${it.kind}${if (it.required) " (obrigatório)" else ""} • default=${it.defaultValue ?: it.defaultDescription ?: "ausente"}"
                        }
                    } ?: "Fluxo composto; consulte os schemas de cada nó na aba JSON SDUI."
                }
                SelectionContainer { Text(source, modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)) }
            }
        }
    }
}
