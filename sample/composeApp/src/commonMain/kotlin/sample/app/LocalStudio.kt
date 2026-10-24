@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package sample.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import me.developes.humming.sdui.HummingSDUI
import me.developes.humming.sdui.action.SDActions
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.layout.SDLayoutMaterial3
import me.developes.humming.sdui.layout.components.Material3Theme
import me.developes.humming.sdui.navigation.SDNavigation
import me.developes.humming.sdui.studio.*

@Composable
fun LocalStudio(files: StudioFiles? = null, workspace: LocalWorkspace = remember { LocalWorkspace() }) {
    var document by remember(workspace) { mutableStateOf(workspace.document) }
    var openedVersion by remember(workspace) { mutableStateOf(0L) }
    var selectedId by remember(workspace) { mutableStateOf(document.entryNodeId) }
    var selectedEdge by remember { mutableStateOf<String?>(null) }
    var messages by remember { mutableStateOf("") }
    var dirty by remember { mutableStateOf(false) }
    var replace by remember { mutableStateOf<(() -> Unit)?>(null) }
    var jsonDialog by remember { mutableStateOf(false) }
    var jsonText by remember { mutableStateOf("") }
    var showInteractions by remember { mutableStateOf(false) }
    var active by remember { mutableStateOf(true) }
    DisposableEffect(Unit) { onDispose { active = false } }
    val compilation = remember(document) { workspace.compilePreview() }
    val treeRows = remember(document) { workspace.tree() }
    val previewControls = remember { PreviewControls() }
    val interactionViewport = remember { mutableStateOf(CanvasViewport(zoom=.5)) }
    val selected = document.nodes[selectedId]
    fun diagnostics(items: List<NodeDiagnostic>) = items.joinToString("\n") {
        "${it.code} | nó=${it.nodeId ?: "—"} | ${it.path} | ${it.message}"
    }
    fun changed(outcome: EditOutcome) {
        messages = diagnostics(outcome.diagnostics)
        if (outcome.isSuccess) {
            document = workspace.document
            dirty = true
            if (selectedId !in document.nodes) { selectedId = document.entryNodeId; selectedEdge = null }
        }
    }
    fun replaced(result: AuthoringResult<AuthoringDocument>) {
        messages = diagnostics(result.diagnostics)
        if (result.isSuccess) {
            openedVersion++
            document = workspace.document; selectedId = document.entryNodeId; selectedEdge = null; dirty = false
        }
    }
    fun requestReplace(action: () -> Unit) { if (dirty) replace = action else action() }
    MaterialTheme {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(12.dp)) {
            Text("HummingUI Studio — autoria local", style = MaterialTheme.typography.titleLarge)
            Text("Revisão ${document.revision}${if (dirty) " • alterações locais" else ""} • preview sem rede ou métodos do aplicativo")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { requestReplace {
                    workspace.newDocument(); openedVersion++; document = workspace.document; selectedId = document.entryNodeId
                    selectedEdge = null; dirty = false; messages = "Documento novo"
                } }) { Text("Novo") }
                Button(enabled = workspace.session.canUndo, onClick = { changed(workspace.undo()) }) { Text("Desfazer") }
                Button(enabled = workspace.session.canRedo, onClick = { changed(workspace.redo()) }) { Text("Refazer") }
                Button(enabled = files != null, onClick = {
                    files?.open { result -> if (active) {
                        when {
                            result.error != null -> messages = result.error
                            result.content != null -> requestReplace { replaced(workspace.openAuthoring(result.content)) }
                        }
                    } }
                }) { Text("Abrir documento") }
                Button(enabled = files != null, onClick = {
                    val result = workspace.save()
                    messages = diagnostics(result.diagnostics)
                    result.value?.let { files?.download("humming-document.json", it); messages = "Download do documento solicitado" }
                }) { Text("Salvar documento") }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { jsonText = ""; jsonDialog = true }) { Text("Importar JSON") }
                OutlinedButton(onClick = { requestReplace { replaced(workspace.importContract(DROP_EXAMPLE)) } }) { Text("Exemplo de arraste") }
                OutlinedButton(onClick = { requestReplace {
                    replaced(workspace.openAuthoring(AuthoringDocumentCodec(workspace.catalog).encode(journeyExample()).requireValue()))
                } }) { Text("Exemplo de navegação") }
                OutlinedButton(onClick = { requestReplace {
                    replaced(workspace.openAuthoring(AuthoringDocumentCodec(workspace.catalog).encode(isolationExample()).requireValue()))
                } }) { Text("Exemplo de estado isolado") }
                OutlinedButton(onClick = { showInteractions = !showInteractions }) {
                    Text(if (showInteractions) "Executar preview" else "Canvas de interações")
                }
                OutlinedButton(onClick = {
                    val before = document.nodes.keys
                    changed(workspace.addScreen(CanvasPoint(document.nodes.values.count { it.kind == AuthorNodeKind.SCREEN } * 400.0, 0.0)))
                    document.nodes.values.firstOrNull { it.id !in before && it.kind == AuthorNodeKind.SCREEN }?.let {
                        selectedId = it.id; selectedEdge = null
                    }
                }) { Text("+ Tela") }
                OutlinedButton(enabled = files != null && compilation.isSuccess, onClick = {
                    val result = workspace.compilePreview()
                    messages = diagnostics(result.diagnostics)
                    result.value?.let { compiled ->
                        if (compiled.providerType == null) {
                            files?.download("humming-contract.json", compiled.artifacts.getValue(compiled.entryDestination))
                            messages = "Download do contrato solicitado"
                        } else {
                            val bundle = CompiledBundleCodec().encode(compiled)
                            messages = diagnostics(bundle.diagnostics)
                            bundle.value?.let { files?.download("humming-journey.json", it); messages = "Download da jornada completa solicitado" }
                        }
                    }
                }) { Text("Exportar contrato") }
                Text("Selecione um nó na árvore. Arquivos permanecem no seu computador.")
            }
            if (messages.isNotEmpty()) Text(messages, Modifier.heightIn(max = 90.dp).verticalScroll(rememberScrollState()).testTag("studio-message"))
            Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(.28f).fillMaxHeight()) {
                    Text("Árvore", style = MaterialTheme.typography.titleMedium)
                    LazyColumn(Modifier.fillMaxSize().testTag("studio-tree")) {
                        itemsIndexed(treeRows) { _, row ->
                            Text("${row.relation} · ${row.node.definition.substringAfter(':')} · ${row.node.id.take(8)}",
                                Modifier.fillMaxWidth().background(if (selectedId == row.node.id) Color(0xFFE7E0EC) else Color.Transparent)
                                    .clickable { selectedId = row.node.id; selectedEdge = row.edgeId }
                                    .padding(start = (minOf(row.depth, 10) * 12).dp, top = 9.dp, bottom = 9.dp)
                                    .testTag("studio-node-${row.node.id}"))
                        }
                    }
                }
                Column(Modifier.weight(.34f).fillMaxHeight().verticalScroll(rememberScrollState())) {
                    Text("Inspector", style = MaterialTheme.typography.titleMedium)
                    selected?.let { node ->
                        Text(node.definition)
                        Text("ID: ${node.id}", style = MaterialTheme.typography.bodySmall)
                        Row {
                            TextButton(onClick = { changed(workspace.reorder(node.id, -1, selectedEdge)) }) { Text("Subir") }
                            TextButton(onClick = { changed(workspace.reorder(node.id, 1, selectedEdge)) }) { Text("Descer") }
                            TextButton(enabled = node.id != document.entryNodeId, onClick = { changed(workspace.remove(node.id)) }) { Text("Remover") }
                        }
                        val linkedNavigation = document.edges.values.any { it.sourceNodeId == node.id && it.role == EdgeRole.NAVIGATION }
                        if (linkedNavigation) Text("Destino definido pela conexão de navegação")
                        PropertyInspector(node, workspace.catalog[node.definition],
                            hiddenProperties = if (linkedNavigation) setOf("destiny") else emptySet()) { name, value ->
                            changed(workspace.putProperty(node.id, name, value))
                        }
                        if (node.kind == AuthorNodeKind.SCREEN) {
                            var x by remember(node.id,node.canvasPosition) { mutableStateOf((node.canvasPosition?.x ?: 0.0).toString()) }
                            var y by remember(node.id,node.canvasPosition) { mutableStateOf((node.canvasPosition?.y ?: 0.0).toString()) }
                            OutlinedTextField(x,{ x=it },label={ Text("Canvas x") })
                            OutlinedTextField(y,{ y=it },label={ Text("Canvas y") })
                            OutlinedButton(onClick = {
                                val px=x.toDoubleOrNull(); val py=y.toDoubleOrNull()
                                if (px == null || py == null || !px.isFinite() || !py.isFinite()) messages="Posição do canvas exige números finitos"
                                else changed(workspace.apply(listOf(EditOperation.SetCanvasPosition(node.id,CanvasPoint(px,py)))))
                            }) { Text("Posicionar tela") }
                        }
                        val definition = workspace.catalog[node.definition]
                        val slots = buildList {
                            if (definition?.childrenRole == NodeRole.COMPONENT) add("children")
                            definition?.properties?.filter { it.kind == PropertyKind.NODES && it.nodeRole == NodeRole.COMPONENT && it.editable }
                                ?.forEach { add(it.name) }
                        }
                        if (slots.isNotEmpty()) {
                            var slot by remember(node.id) { mutableStateOf(slots.first()) }
                            Choice("Slot", slot, slots) { slot = it }
                            Text("Inserir em ${node.definition.substringAfter(':')}: $slot")
                            LocalWorkspace.visualDefinitions.chunked(3).forEach { chunk -> Row {
                                chunk.forEach { name -> TextButton(onClick = {
                                    val before = workspace.document.nodes.keys
                                    val outcome = workspace.insertVisual(node.id, name, slot)
                                    changed(outcome)
                                    if (outcome.isSuccess) {
                                        selectedId = workspace.document.nodes.values.first {
                                            it.id !in before && it.definition == name &&
                                                (it.placement as? Placement.Child)?.parentId == node.id
                                        }.id
                                        selectedEdge = null
                                    }
                                }) { Text("+ ${name.substringAfter(':')}") } }
                            } }
                        }
                        val ports = definition?.properties.orEmpty().filter {
                            it.editable && it.kind == PropertyKind.NODES && it.nodeRole == NodeRole.ACTION
                        }.map { it.name }
                        if (ports.isNotEmpty()) {
                            var port by remember(node.id) { mutableStateOf(ports.first()) }
                            Choice(if (node.kind == AuthorNodeKind.ACTION) "Ramo" else "Evento", port, ports) { port = it }
                            LocalWorkspace.actionDefinitions.filter { it != "navigation:goTo" }.forEach { action ->
                                OutlinedButton(onClick = { changed(workspace.addAction(node.id, port, action)) }) { Text("+ $action") }
                            }
                            val screens = document.nodes.values.filter { it.kind == AuthorNodeKind.SCREEN && it.placement is Placement.Root }.map { it.id }.sorted()
                            var destination by remember(node.id) { mutableStateOf(screens.firstOrNull().orEmpty()) }
                            Choice("Tela de destino", destination, screens) { destination = it }
                            OutlinedButton(onClick = { changed(workspace.addAction(node.id, port, "navigation:goTo", destination)) }) { Text("+ navigation:goTo") }
                            var target by remember(node.id) { mutableStateOf("") }
                            OutlinedTextField(target, { target = it }, label = { Text("ID da ação existente") },
                                modifier = Modifier.testTag("studio-action-target"))
                            OutlinedButton(onClick = { changed(workspace.connectAction(node.id, port, target)) }) {
                                Text("Conectar ação")
                            }
                            document.edges.values.filter {
                                it.sourceNodeId == node.id && it.sourcePort == port && it.role != EdgeRole.NAVIGATION
                            }.sortedWith(compareBy({ it.orderKey }, { it.id })).forEach { edge ->
                                OutlinedButton(onClick = { changed(workspace.disconnect(edge.id)) },
                                    modifier = Modifier.testTag("studio-disconnect-${edge.id}")) {
                                    Text("Desconectar ${edge.targetNodeId}")
                                }
                            }
                        }
                        if (node.definition == "navigation:goTo") {
                            val screens = document.nodes.values.filter { it.kind == AuthorNodeKind.SCREEN && it.placement is Placement.Root }.map { it.id }.sorted()
                            val current = document.edges.values.firstOrNull { it.sourceNodeId == node.id && it.role == EdgeRole.NAVIGATION }?.targetNodeId
                            var destination by remember(node.id, current) { mutableStateOf(current ?: screens.firstOrNull().orEmpty()) }
                            Choice("Tela de destino", destination, screens) { destination = it }
                            OutlinedButton(onClick = { changed(workspace.setNavigation(node.id, destination)) }) { Text("Aplicar destino") }
                            document.edges.values.filter { it.sourceNodeId == node.id && it.role == EdgeRole.NAVIGATION }.forEach { edge ->
                                OutlinedButton(onClick = { changed(workspace.disconnect(edge.id)) }) { Text("Remover conexão de navegação") }
                            }
                        }
                        if (node.id != document.entryNodeId && node.placement is Placement.Child) {
                            var parent by remember(node.id) { mutableStateOf(document.entryNodeId) }
                            var slot by remember(node.id) { mutableStateOf("children") }
                            OutlinedTextField(parent, { parent = it }, label = { Text("ID do container de destino") })
                            OutlinedTextField(slot, { slot = it }, label = { Text("Slot de destino") })
                            OutlinedButton(onClick = { changed(workspace.move(node.id, parent, slot)) }) { Text("Mover para container") }
                        }
                    }
                }
                Column(Modifier.weight(.38f).fillMaxHeight()) {
                    Text(if (showInteractions) "Canvas de interações" else "Preview executável", style = MaterialTheme.typography.titleMedium)
                    val emptyScreen = document.nodes[document.entryNodeId]?.definition in setOf("layout:column", "layout:row", "layout:box") &&
                        document.nodes.values.none { it.placement is Placement.Child }
                    if (emptyScreen) {
                        Text("Tela vazia: ainda não há elementos para arrastar.", Modifier.testTag("studio-empty-preview"))
                        Text("Crie um componente no Inspector ou carregue os dois containers do exemplo.")
                        OutlinedButton(onClick = { requestReplace { replaced(workspace.importContract(DROP_EXAMPLE)) } }) {
                            Text("Carregar exemplo de arraste")
                        }
                    }
                    if (!compilation.isSuccess) {
                        Text("Preview indisponível: corrija os diagnósticos.")
                        Text(diagnostics(compilation.diagnostics), Modifier.verticalScroll(rememberScrollState()).testTag("studio-compile-error"))
                    } else key(openedVersion, document.documentId, document.revision) {
                        val compiled = compilation.requireValue()
                        Box(Modifier.fillMaxSize().background(Color.White)) {
                            if (showInteractions) {
                                InteractionCanvas(document,compiled,selectedEdge,select = { id, edge -> selectedId=id; selectedEdge=edge },
                                    viewportState=interactionViewport)
                            } else if (compiled.providerType != null) {
                                CompiledPreview(compiled)
                            } else {
                                EditablePreview(workspace, document, compiled.sourceNodeIds, selectedId,
                                    select = { selectedId = it; selectedEdge = null }, commit = ::changed,
                                    controls = previewControls) { CompiledPreview(compiled) }
                            }
                        }
                    }
                }
            }
        }
        replace?.let { action -> AlertDialog(onDismissRequest = { replace = null }, title = { Text("Substituir documento?") },
            text = { Text("Há alterações locais. Salve o documento antes de substituí-lo se precisar preservá-las.") },
            confirmButton = { TextButton(onClick = { replace = null; action() }) { Text("Substituir") } },
            dismissButton = { TextButton(onClick = { replace = null }) { Text("Cancelar") } }) }
        if (jsonDialog) AlertDialog(onDismissRequest = { jsonDialog = false }, title = { Text("Importar JSON local") },
            text = { OutlinedTextField(jsonText, { if (it.length <= PayloadLimits().maxInputChars) jsonText = it },
                Modifier.height(300.dp).testTag("studio-json"), label = { Text("JSON de documento ou contrato") }) },
            confirmButton = { Column {
                TextButton(onClick = { jsonDialog = false; requestReplace { replaced(workspace.importContract(jsonText)) } }) { Text("Importar contrato") }
                TextButton(onClick = { jsonDialog = false; requestReplace { replaced(workspace.openAuthoring(jsonText)) } }) { Text("Importar documento") }
            } }, dismissButton = { TextButton(onClick = { jsonDialog = false }) { Text("Cancelar") } })
    }
}

@Composable
private fun PropertyInspector(node: AuthorNode, definition: NodeDefinition?, hiddenProperties: Set<String> = emptySet(),
                              change: (String, JsonElement?) -> Unit) {
    val properties = definition?.properties.orEmpty().filter {
        it.name !in hiddenProperties && it.editable && it.kind !in setOf(PropertyKind.NODES, PropertyKind.OPAQUE)
    }
    if (properties.isNotEmpty()) {
        var name by remember(node.id) { mutableStateOf(
            listOf("text", "state", "paddingAll").firstOrNull { candidate -> properties.any { it.name == candidate } } ?: properties.first().name
        ) }
        val property = properties.first { it.name == name }
        var text by remember(node.id, name, node.properties[name]) {
            mutableStateOf(node.properties[name]?.let { if (it is JsonPrimitive) it.content else it.toString() } ?: "")
        }
        var parseError by remember(node.id, name) { mutableStateOf("") }
        Choice("Propriedade", name, properties.map { it.name }) { name = it }
        Text("${property.kind}${if (property.required) " • obrigatória" else " • opcional"}${if (property.allowsBinding) " • aceita #{chave}" else ""}")
        property.defaultDescription?.let { Text("Padrão: $it") }
        property.defaultValue?.let { Text("Padrão quando ausente: $it") }
        if (property.enumValues.isNotEmpty()) Text("Valores: ${property.enumValues.joinToString()}")
        Text(if (name in node.properties) "Valor explícito" else "Propriedade ausente")
        OutlinedTextField(text, { text = it }, label = { Text("Valor de $name") }, modifier = Modifier.fillMaxWidth().testTag("studio-property-value"))
        Row {
            Button(onClick = {
                try {
                    val value = if (property.kind == PropertyKind.STRING_ARRAY) parseBoundedJson(text) else JsonPrimitive(text)
                    parseError = ""; change(name, value)
                } catch (error: PayloadException) { parseError = "${error.diagnostic.code}: ${error.diagnostic.message}" }
            }) { Text("Aplicar propriedade") }
            TextButton(onClick = { change(name, null) }) { Text("Remover valor") }
        }
        if (parseError.isNotEmpty()) Text(parseError)
    }
    val readonly = node.properties.filterKeys { key -> properties.none { it.name == key } }
    if (definition == null) Text("Nó sem schema: preservado, somente leitura.")
    readonly.forEach { (key, value) -> Text("Somente leitura: $key = $value") }
}

@Composable
private fun Choice(label: String, selected: String, values: List<String>, choose: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) { Text("$label: $selected") }
        DropdownMenu(expanded, onDismissRequest = { expanded = false }, Modifier.heightIn(max = 320.dp)) {
            values.forEach { value -> DropdownMenuItem(text = { Text(value) }, onClick = { choose(value); expanded = false }) }
        }
    }
}

@Composable
internal fun CompiledPreview(json: String) {
    CompiledPreview(CompiledContracts("preview", 0, "screen.json", null, mapOf("screen.json" to json), emptyMap()))
}

@Composable
internal fun CompiledPreview(compiled: CompiledContracts) {
    val owner = remember { object : ViewModelStoreOwner { override val viewModelStore = ViewModelStore() } }
    DisposableEffect(owner) { onDispose { owner.viewModelStore.clear() } }
    CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
        val provider = compiled.providerType ?: "compiled-preview"
        HummingSDUI().Content(HummingSDUI.Config(provider, compiled.entryDestination, theme = Material3Theme(),
            screenStateMode = ScreenStateMode.ISOLATED_ENTRIES,
            placeholder = { Text("Carregando preview…") }, errorHandler = { error, vm -> Column {
                Text("${error.code}: ${error.message}")
                error.diagnostic?.let { Text("${it.nodeId} | ${it.path}") }
                TextButton(onClick = vm::dismissError) { Text("Limpar erro do preview") }
            } }) {
            addLibrary(SDActions()); addLibrary(SDLayoutMaterial3())
            if (compiled.providerType != null) addLibrary(SDNavigation())
            addNodeProvider(provider) { path -> parseNode(checkNotNull(compiled.artifacts[path]) { "Artefato ausente: $path" }, documentId = path) }
        }, savedStateHandleFactory = { SavedStateHandle() })
    }
}
