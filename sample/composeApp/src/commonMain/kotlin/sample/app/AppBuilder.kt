@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, kotlin.uuid.ExperimentalUuidApi::class)

package sample.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import br.com.developes.sdui.resources.Res
import br.com.developes.sdui.resources.ic_eye
import kotlinx.serialization.json.*
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.layout.LayoutDefinitions
import me.developes.humming.sdui.layout.components.LocalImageSources
import me.developes.humming.sdui.studio.*
import org.jetbrains.compose.resources.painterResource
import kotlin.uuid.Uuid

internal fun insertBuilderComponent(workspace: LocalWorkspace, parent: String, slot: String, name: String, insertion: DropPlan? = null): EditOutcome {
    if ("layout:$name" in LocalWorkspace.visualDefinitions) return workspace.insertVisual(parent, "layout:$name", slot, insertion)
    val prefix = "builder-${Uuid.random()}-"
    val source = CatalogFixture(name, stateOutput = false).source().replace("catalog-$name-", prefix)
    val imported = LegacyAuthoringCodec(workspace.catalog).importDocument(source, prefix).requireValue()
    val fragment = imported.copy(edges = imported.edges.values.associate { edge -> val copy = edge.copy(id = prefix + edge.id); copy.id to copy })
    return workspace.insertFragment(fragment, parent, slot, insertion)
}

@Composable
fun AppBuilder(files: StudioFiles? = null, initialDocument: AuthoringDocument? = null, onExit: (() -> Unit)? = null) {
    var workspace by remember { mutableStateOf(LocalWorkspace(initialDocument ?: journeyExample())) }
    var document by remember { mutableStateOf(workspace.document) }
    var title by remember { mutableStateOf("Meu aplicativo") }
    var viewport by remember { mutableStateOf(ProjectViewport.PHONE) }
    var names by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var screen by remember { mutableStateOf(document.entryNodeId) }
    var selected by remember { mutableStateOf(screen) }
    var step by remember { mutableIntStateOf(1) }
    var leftTab by remember { mutableStateOf("Telas") }
    var inspector by remember { mutableStateOf("Propriedades") }
    var search by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var lastExport by remember { mutableStateOf(AppProjectCodec().encode(AppProject("Meu aplicativo", ProjectViewport.PHONE, emptyMap(), workspace.document)).requireValue()) }
    var replace by remember { mutableStateOf<(() -> Unit)?>(null) }
    var newDialog by remember { mutableStateOf(false) }
    var advanced by remember { mutableStateOf(false) }
    val codec = remember { AppProjectCodec() }
    val controls = remember { PreviewControls().apply { editing.value = true } }
    val graphViewport = remember { mutableStateOf(CanvasViewport(zoom = .6)) }
    val palette = remember { PaletteDragState() }
    var selectedEdge by remember { mutableStateOf<String?>(null) }
    val assets = remember { mapOf("catalog-mark" to @Composable { painterResource(Res.drawable.ic_eye) }) }
    val screens = remember(document) { AuthoringIndices(document).roots.filter { it.kind == AuthorNodeKind.SCREEN } }
    fun screenName(id: String) = names[id] ?: "Tela ${screens.indexOfFirst { it.id == id } + 1}"
    fun snapshot() = AppProject(title, viewport, names.filterKeys { id -> screens.any { it.id == id } }, document)
    fun serialized() = codec.encode(snapshot())
    val dirty = serialized().value != lastExport
    fun change(result: EditOutcome) {
        message = result.diagnostics.joinToString("\n") { "${it.code} • ${it.nodeId.orEmpty()} • ${it.path} • ${it.message}" }
        if (result.isSuccess) {
            document = workspace.document
            if (screen !in document.nodes) screen = document.entryNodeId
            if (selected !in document.nodes) selected = screen
        }
    }
    fun load(project: AppProject) {
        workspace = LocalWorkspace(project.document); document = workspace.document
        title = project.name; viewport = project.viewport; names = project.screenNames.toMap()
        screen = document.entryNodeId; selected = screen; step = 1
        lastExport = codec.encode(project).requireValue(); message = "Projeto aberto"
    }
    fun request(action: () -> Unit) { if (dirty) replace = action else action() }
    val target = remember { TargetCapabilities(initialNodeCatalog().capabilities, providers = setOf("compiled-preview"),
        colorTokens = defaultPresentationTokens().colorNames, maskTokens = defaultPresentationTokens().maskNames) }
    val preview = remember(document, screen) { ContractCompiler(workspace.catalog).compileJourney(document.copy(entryNodeId = screen), target,
        "compiled-preview", UnreachablePolicy.REPORT) }
    val output = remember(document) { workspace.compileJourney("compiled-preview") }
    val graph = remember(document) { compileBuilderCanvas(workspace, target) }
    val node = document.nodes[selected]
    val slots = workspace.catalog[node?.definition.orEmpty()]?.let { definition ->
        (if (definition.childrenRole == NodeRole.COMPONENT) listOf("children") else emptyList()) +
            definition.properties.filter { it.kind == PropertyKind.NODES && it.nodeRole == NodeRole.COMPONENT && it.editable }.map { it.name }
    }.orEmpty()
    var chosenSlot by remember(selected) { mutableStateOf("children") }
    val insertSlot = chosenSlot.takeIf { it in slots } ?: slots.firstOrNull() ?: "children"
    val tree = remember(document, screen) { workspace.tree().filter { row ->
        var current = row.node
        while (current.placement is Placement.Child) current = document.nodes.getValue((current.placement as Placement.Child).parentId)
        current.id == screen
    } }
    fun exportProject() {
        val encoded = serialized()
        message = encoded.diagnostics.joinToString { "${it.code}: ${it.path}" }
        encoded.value?.let { files?.download("humming-app-project.json", it); message = "Exportação do projeto solicitada; preserve o arquivo. Reabra-o para confirmar a recuperação." }
    }
    CompositionLocalProvider(LocalImageSources provides assets) {
        MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF4F46E5), secondary = Color(0xFF475569))) {
            if (advanced) Column { TextButton(onClick = { advanced = false; document = workspace.document }) { Text("Voltar ao criador") }; Box(Modifier.weight(1f)) { LocalStudio(files, workspace) } }
            else Surface(Modifier.fillMaxSize(), color = Color(0xFFF3F5F9)) {
                Column(Modifier.fillMaxSize()) {
                    FlowRow(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.Center) {
                        Text("H / Studio", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(end = 8.dp))
                        OutlinedTextField(title, { if (it.length <= 80) title = it }, label = { Text("Nome do aplicativo") }, singleLine = true,
                            modifier = Modifier.width(210.dp).testTag("builder-title"))
                        TextButton(onClick = { request { newDialog = true } }) { Text("Novo") }
                        TextButton(enabled = files != null, onClick = { files?.open { result ->
                            result.content?.let { source ->
                                val parsed = codec.decode(source)
                                if (parsed.isSuccess) request { load(parsed.requireValue()) }
                                else message = parsed.diagnostics.joinToString { "${it.code}: ${it.path}" }
                            }; result.error?.let { message = it }
                        } }) { Text("Abrir projeto") }
                        TextButton(enabled = files != null, onClick = ::exportProject) { Text("Salvar projeto") }
                        TextButton(enabled = workspace.session.canUndo, onClick = { change(workspace.undo()) }) { Text("Desfazer") }
                        TextButton(enabled = workspace.session.canRedo, onClick = { change(workspace.redo()) }) { Text("Refazer") }
                        Button(onClick = { step = 3 }) { Text("Executar app") }
                        TextButton(onClick = { step = 4 }) { Text("Exportar") }
                        if (onExit != null) TextButton(onClick = { request(onExit) }) { Text("Catálogo e serviços") }
                    }
                    FlowRow(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 12.dp)) {
                        listOf("Estrutura", "Design", "Ações", "Testar", "Exportar").forEachIndexed { index, label ->
                            FilterChip(step == index, { step = index; if (index == 0) leftTab = "Telas"; if (index == 1) leftTab = "Elementos"; if (index == 2) inspector = "Ações" }, { Text("${index + 1}. $label") }, modifier = Modifier.padding(end = 6.dp).testTag("builder-step-$index"))
                        }
                        Text("${screens.size} telas • revisão ${document.revision}${if (dirty) " • alterações locais" else ""}", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                    }
                    Text(if (palette.name != null) "Arrastando ${palette.name} — solte sobre um Auto Layout; Escape cancela" else "Design: arraste elementos • Ações: puxe as bolinhas azuis para conectar telas", Modifier.padding(horizontal = 12.dp), style = MaterialTheme.typography.bodySmall)
                    if (message.isNotEmpty()) Text(message, Modifier.fillMaxWidth().heightIn(max = 70.dp).verticalScroll(rememberScrollState()).padding(8.dp).testTag("builder-message"))
                    BoxWithConstraints(Modifier.weight(1f)) {
                        val narrow = maxWidth < 850.dp
                        Row(Modifier.fillMaxSize()) {
                            Column(Modifier.width(if (narrow) 185.dp else 235.dp).fillMaxHeight().background(Color(0xFF1E2433)).padding(12.dp)) {
                                    Button(onClick = {
                                        val before = document.nodes.keys; change(workspace.addScreen(CanvasPoint((screens.size % 3) * 400.0, (screens.size / 3) * 300.0)))
                                        document.nodes.values.firstOrNull { it.id !in before && it.kind == AuthorNodeKind.SCREEN }?.let { screen = it.id; selected = it.id; if (step == 3 || step == 4) step = 1 }
                                    }, modifier = Modifier.fillMaxWidth().testTag("builder-add-screen")) { Text("+ Nova tela") }
                                Row { TextButton(onClick = { leftTab = "Telas" }) { Text("Telas", color = Color.White) }; TextButton(onClick = { leftTab = "Elementos" }) { Text("Elementos", color = Color.White) } }
                                if (leftTab == "Elementos") {
                                    OutlinedTextField(search, { search = it }, placeholder = { Text("Buscar elemento") }, singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth().testTag("builder-search"))
                                    LazyColumn(Modifier.weight(1f)) {
                                        items(LayoutDefinitions.all.filter { it.identifier.contains(search, true) }, key = { it.identifier }) { definition ->
                                            BuilderPaletteCard(definition.identifier.substringAfter(':'), palette, click = {
                                                val parent = node?.takeIf { slots.isNotEmpty() }?.id ?: screen
                                                val before = document.nodes.keys
                                                change(insertBuilderComponent(workspace, parent, if (parent == selected) insertSlot else "children", definition.identifier.substringAfter(':')))
                                                selected = document.nodes.values.firstOrNull { it.id !in before && it.definition == definition.identifier }?.id ?: selected
                                            }, drop = { name, plan ->
                                                val before = document.nodes.keys
                                                change(insertBuilderComponent(workspace, plan.targetId, plan.slot, name, plan))
                                                selected = document.nodes.values.firstOrNull { it.id !in before && it.definition == "layout:$name" }?.id ?: selected
                                            })
                                        }
                                    }
                                } else {
                                    Column(Modifier.heightIn(max = 180.dp).verticalScroll(rememberScrollState())) {
                                    screens.forEach { page ->
                                        Text("${if (page.id == document.entryNodeId) "Inicial · " else ""}${screenName(page.id)}", color = Color.White,
                                            modifier = Modifier.fillMaxWidth().background(if (screen == page.id) Color(0xFF454371) else Color.Transparent, RoundedCornerShape(8.dp))
                                                .clickable { screen = page.id; selected = page.id }.padding(10.dp).testTag("builder-screen-${page.id}"))
                                    }
                                    }
                                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                                    Text("ÁRVORE DE ELEMENTOS", color = Color(0xFF94A3B8), style = MaterialTheme.typography.labelSmall)
                                    LazyColumn(Modifier.weight(1f)) {
                                        items(tree, key = { "${it.node.id}-${it.edgeId}" }) { row ->
                                            Text(row.node.definition.substringAfter(':'), color = Color(0xFFE2E8F0), modifier = Modifier.fillMaxWidth()
                                                .background(if (selected == row.node.id) Color(0xFF454371) else Color.Transparent, RoundedCornerShape(6.dp))
                                                .clickable { selected = row.node.id }.padding(start = (8 + row.depth.coerceAtMost(8) * 10).dp, top = 9.dp, bottom = 9.dp)
                                                .testTag("builder-node-${row.node.id}"))
                                        }
                                    }
                                }
                                TextButton(onClick = { advanced = true }) { Text("Studio avançado", color = Color(0xFFC4B5FD)) }
                            }
                            Column(Modifier.weight(1f).fillMaxHeight().padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(screenName(screen), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                    Choice("Viewport", viewport.name, ProjectViewport.entries.map { it.name }) { viewport = ProjectViewport.valueOf(it) }
                                    TextButton(onClick = { controls.viewport.value = controls.viewport.value.let { it.copy(zoom = (it.zoom / 1.2).coerceAtLeast(.2), revision = it.revision + 1) } }) { Text("−") }
                                    TextButton(onClick = { controls.viewport.value = controls.viewport.value.let { it.copy(zoom = (it.zoom * 1.2).coerceAtMost(3.0), revision = it.revision + 1) } }) { Text("+") }
                                }
                                when (step) {
                                    4 -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                                        Text("Preparar entrega", style = MaterialTheme.typography.headlineSmall)
                                        Text("${screens.size} telas • ${document.nodes.size} nós • ${document.edges.size} relações")
                                        Text(if (output.isSuccess) "✓ Jornada compilada e capacidades verificadas" else "Corrija as pendências antes de exportar")
                                        output.diagnostics.forEach { Text("${it.code} • ${it.nodeId} • ${it.path}") }
                                        Button(enabled = files != null, onClick = ::exportProject) { Text("Salvar projeto completo") }
                                        Button(enabled = files != null && output.isSuccess, onClick = { files?.download("humming-app-contracts.json", CompiledBundleCodec().encode(output.requireValue()).requireValue()); message = "Exportação do pacote SDUI solicitada" }) { Text("Exportar jornada SDUI") }
                                        TextButton(enabled = files != null, onClick = { files?.download("humming-document.json", workspace.save().requireValue()) }) { Text("Exportar documento autoral") }
                                        Text("O pacote é consumido por um host HummingUI com as capacidades declaradas. Não é um APK/IPA nem um deploy.")
                                    }
                                    2 -> if (graph.isSuccess) InteractionCanvas(document, graph.requireValue(), selectedEdge,
                                        select = { id, edge -> selected = id; selectedEdge = edge }, viewportState = graphViewport,
                                        screenNames = screens.associate { it.id to screenName(it.id) },
                                        frameViewport = viewport,
                                        connect = { source, port, destination ->
                                            val duplicate = InteractionProjection.navigationLinks(document).value.orEmpty().any { it.sourceNodeId == source && it.targetScreenId == destination }
                                            if (duplicate) message = "Essa conexão já existe"
                                            else change(workspace.addAction(source, port, "navigation:goTo", destination))
                                        }) else Column { graph.diagnostics.forEach { Text("${it.code}: ${it.message}") } }
                                    else -> {
                                        val result = if (step == 3) output else preview
                                        if (!result.isSuccess) Column { result.diagnostics.forEach { Text("${it.code}: ${it.message}") } }
                                        else BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xFFE8ECF3), RoundedCornerShape(12.dp))) {
                                            Canvas(Modifier.fillMaxSize()) { for (x in 0..(size.width / 20).toInt()) for (y in 0..(size.height / 20).toInt()) drawCircle(Color(0xFFCAD2E0), 1f, Offset(x * 20f, y * 20f)) }
                                            val fit = minOf(maxWidth.value / viewport.width, (maxHeight.value - 28).coerceAtLeast(30f) / viewport.height).toDouble().coerceIn(.25, 1.0)
                                            val density = LocalDensity.current.density
                                            LaunchedEffect(viewport, fit, step) {
                                                controls.viewport.value = CanvasViewport(zoom = fit, pan = Point2((maxWidth.value - viewport.width * fit) * density / 2, 8.0 * density))
                                                controls.editing.value = step != 3
                                            }
                                            key(document.documentId, document.revision, screen, step) {
                                                EditablePreview(workspace, document, result.requireValue().sourceNodeIds, selected, { selected = it }, ::change, controls, showControls = false, palette = palette) {
                                                    Box(Modifier.wrapContentSize(Alignment.TopStart, unbounded = true).requiredSize(viewport.width.dp, viewport.height.dp).clip(RoundedCornerShape(24.dp)).background(Color.White).border(5.dp, Color(0xFF202534), RoundedCornerShape(24.dp)).padding(12.dp)) {
                                                        CompiledPreview(result.requireValue())
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (!narrow && step != 3 && step != 4) Column(Modifier.width(290.dp).fillMaxHeight().background(Color.White).padding(12.dp).verticalScroll(rememberScrollState())) {
                                Row { TextButton(onClick = { inspector = "Propriedades" }) { Text("Propriedades") }; TextButton(onClick = { inspector = "Ações" }) { Text("Ações") } }
                                node?.let { item ->
                                    Text(item.definition, style = MaterialTheme.typography.titleSmall)
                                    if (inspector == "Propriedades") {
                                        if (slots.isNotEmpty()) Choice("Adicionar em", insertSlot, slots) { chosenSlot = it }
                                        if (item.kind == AuthorNodeKind.SCREEN) {
                                            var label by remember(item.id, names) { mutableStateOf(screenName(item.id)) }
                                            OutlinedTextField(label, { if (it.length <= 80) label = it }, label = { Text("Nome da tela") })
                                            TextButton(onClick = { if (label.isNotBlank() && screens.filter { it.id != item.id }.none { screenName(it.id) == label }) names = names + (item.id to label) }) { Text("Renomear tela") }
                                            TextButton(onClick = { change(workspace.apply(listOf(EditOperation.SetEntry(item.id)))) }) { Text("Iniciar nesta tela") }
                                        }
                                        Row { TextButton(onClick = { change(workspace.reorder(item.id, -1)) }) { Text("Antes") }; TextButton(onClick = { change(workspace.reorder(item.id, 1)) }) { Text("Depois") }
                                            TextButton(enabled = item.id != document.entryNodeId, onClick = { change(workspace.remove(item.id)) }) { Text("Remover") } }
                                        PropertyInspector(item, workspace.catalog[item.definition]) { key, value -> change(workspace.putProperty(item.id, key, value)) }
                                    } else {
                                        val ports = workspace.catalog[item.definition]?.properties.orEmpty().filter { it.kind == PropertyKind.NODES && it.nodeRole == NodeRole.ACTION }.map { it.name }
                                        if (ports.isNotEmpty()) {
                                            var port by remember(item.id) { mutableStateOf(ports.first()) }
                                            Choice("Evento", port, ports) { port = it }
                                            Button(onClick = { change(workspace.addUpdate(item.id, port)) }) { Text("+ Atualizar estado") }
                                            TextButton(onClick = { change(workspace.addAction(item.id, port, "navigation:goBack")) }, modifier = Modifier.testTag("builder-back")) { Text("+ Voltar na jornada") }
                                            val destinations = screens.filter { it.id != screen }
                                            if (destinations.isNotEmpty()) {
                                                var destination by remember(item.id, document.nodes.keys) { mutableStateOf(destinations.first().id) }
                                                destinations.forEach { page -> TextButton(onClick = { destination = page.id }) { Text("${if (destination == page.id) "● " else ""}${screenName(page.id)}") } }
                                                Button(onClick = { change(workspace.addAction(item.id, port, "navigation:goTo", destination)) }, modifier = Modifier.testTag("builder-connect")) { Text("Conectar navegação") }
                                            }
                                        }
                                        document.edges.values.filter { it.sourceNodeId == item.id && it.role != EdgeRole.NAVIGATION }.forEach { edge ->
                                            TextButton(onClick = { selected = edge.targetNodeId }) { Text("${edge.sourcePort} → ${document.nodes[edge.targetNodeId]?.definition}") }
                                            TextButton(onClick = { change(workspace.disconnect(edge.id)) }) { Text("Desconectar") }
                                        }
                                        if (item.kind == AuthorNodeKind.ACTION) PropertyInspector(item, workspace.catalog[item.definition]) { key, value -> change(workspace.putProperty(item.id, key, value)) }
                                        if (ports.isEmpty() && item.kind != AuthorNodeKind.ACTION) Text("Este elemento não declara eventos. Selecione um botão ou container com ações.")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (newDialog) {
                var name by remember { mutableStateOf("Novo aplicativo") }
                var template by remember { mutableStateOf("Jornada") }
                AlertDialog(onDismissRequest = { newDialog = false }, title = { Text("Criar aplicativo") }, text = { Column {
                    OutlinedTextField(name, { if (it.length <= 80) name = it }, label = { Text("Nome do projeto") }, modifier = Modifier.testTag("builder-new-name"))
                    listOf("Em branco", "Formulário", "Jornada").forEach { value -> TextButton(onClick = { template = value }) { Text("${if (template == value) "● " else ""}$value") } }
                } }, confirmButton = { Button(enabled = name.isNotBlank(), onClick = {
                    val doc = when (template) { "Em branco" -> LocalWorkspace.blankDocument(); "Formulário" -> LegacyAuthoringCodec(initialNodeCatalog()).importDocument(MATERIAL3_EXAMPLE, "app-${Uuid.random()}").requireValue(); else -> journeyExample() }
                    load(AppProject(name.trim(), ProjectViewport.PHONE, emptyMap(), doc)); lastExport = ""; newDialog = false
                }) { Text("Criar projeto") } }, dismissButton = { TextButton(onClick = { newDialog = false }) { Text("Cancelar") } })
            }
            replace?.let { action -> AlertDialog(onDismissRequest = { replace = null }, title = { Text("Descartar alterações locais?") }, text = { Text("Exporte o projeto atual se precisar preservar as alterações locais.") },
                confirmButton = { TextButton(onClick = { replace = null; action() }) { Text("Continuar") } }, dismissButton = { TextButton(onClick = { replace = null }) { Text("Cancelar") } }) }
        }
    }
}
