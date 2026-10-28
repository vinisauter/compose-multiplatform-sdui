package me.developes.humming.builder.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.studio.CompiledContracts

internal val themeFixture = """{"component":"layout:column","id":"theme-root","properties":{"fillMaxSize":"1","paddingToken":"md"},"children":[
 {"component":"layout:text","id":"theme-text","properties":{"text":"Seu aplicativo, sua identidade"}},
 {"component":"layout:elevatedButton","id":"theme-button","children":[{"component":"layout:text","id":"theme-label","properties":{"text":"Continuar"}}]},
 {"component":"layout:outlinedTextField","id":"theme-input","properties":{"stateKey":"theme-name","text":"Maria","label":"Seu nome"}},
 {"component":"layout:card","id":"theme-card","properties":{"paddingToken":"sm"},"children":[{"component":"layout:text","id":"theme-card-text","properties":{"text":"Conteúdo com cores, formas e tipografia do projeto."}}]},
 {"component":"layout:switch","id":"theme-switch","properties":{"stateKey":"theme-notification","checked":"true"}},
 {"component":"layout:slider","id":"theme-slider","properties":{"stateKey":"theme-progress","value":"0.6"}}
]}"""

@Composable
internal fun DesignSystemEditor(initial: DesignSystem?, preview: CompiledContracts?, files: StudioFiles?,
                                close: () -> Unit, apply: (DesignSystem) -> Unit) {
    var draft by remember { mutableStateOf(initial ?: DesignSystem()) }
    var history by remember { mutableStateOf<List<DesignSystem>>(emptyList()) }
    fun change(value: DesignSystem) { if (value != draft) { history = (history + draft).takeLast(50); draft = value } }
    var title by remember { mutableStateOf(draft.name) }
    var seed by remember(draft.seed) { mutableStateOf(draft.seed) }
    var section by remember { mutableStateOf("Cores") }
    var scheme by remember { mutableStateOf("Light") }
    var role by remember { mutableStateOf("primary") }
    var color by remember(role, scheme, draft) { mutableStateOf(draft.colors(scheme == "Dark").getValue(role)) }
    var styleName by remember { mutableStateOf("bodyLarge") }
    var shapeName by remember { mutableStateOf("medium") }
    var spacingName by remember { mutableStateOf("md") }
    var useProject by remember { mutableStateOf(false) }
    var narrowPreview by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    Dialog(onDismissRequest = close, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth(.96f).fillMaxHeight(.94f).testTag("design-system-editor"), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Text("Design System do projeto", style = MaterialTheme.typography.headlineSmall)
                Text("Cores light/dark • tipografia • formas • espaçamentos. O editor mantém sua aparência; o tema se aplica ao aplicativo.")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(enabled = title.isNotBlank() && title.length <= 80, onClick = { apply(draft.copy(name = title).requireValid()) }, modifier = Modifier.testTag("theme-save")) { Text("Aplicar ao projeto") }
                    TextButton(onClick = close) { Text("Cancelar") }
                    TextButton(enabled = history.isNotEmpty(), onClick = { draft = history.last(); history = history.dropLast(1); title = draft.name }) { Text("Desfazer ajuste") }
                }
                if (message.isNotEmpty()) Text(message)
                BoxWithConstraints(Modifier.weight(1f)) {
                    val narrow = maxWidth < 800.dp
                    Column {
                        if (narrow) Row { TextButton(onClick = { narrowPreview = false }) { Text("Configurar") }; TextButton(onClick = { narrowPreview = true }) { Text("Preview") } }
                        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (!narrow || !narrowPreview) Column(Modifier.weight(if (narrow) 1f else .42f).fillMaxHeight().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(title, { title = it }, label = { Text("Nome do tema") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                Choice("Modo do aplicativo", draft.mode.name, ThemeMode.entries.map { it.name }) { change(draft.copy(mode = ThemeMode.valueOf(it))) }
                                OutlinedTextField(seed, { seed = it }, label = { Text("Cor de origem #RRGGBB") }, modifier = Modifier.fillMaxWidth().testTag("theme-seed"))
                                Button(enabled = isDesignColor(seed), onClick = {
                                    change(draft.copy(seed = seed, light = generateThemeColors(seed, false), dark = generateThemeColors(seed, true)))
                                    message = "Paletas light/dark regeneradas. Tipografia, formas e espaçamentos preservados."
                                }, modifier = Modifier.testTag("theme-generate")) { Text("Gerar light e dark") }
                                Text("Gerar substitui as duas paletas. Algoritmo Humming v1 em sRGB; não é HCT do Material Theme Builder.", style = MaterialTheme.typography.bodySmall)
                                Choice("Editar", section, listOf("Cores", "Tipografia", "Formas", "Espaçamento", "Arquivo")) { section = it }
                                when (section) {
                                    "Cores" -> {
                                        Choice("Paleta", scheme, listOf("Light", "Dark")) { scheme = it }
                                        Choice("Papel", role, designColorRoles) { role = it }
                                        Box(Modifier.fillMaxWidth().height(40.dp).background(draft.colors(scheme == "Dark").getValue(role).toColor()).border(1.dp, MaterialTheme.colorScheme.outline))
                                        OutlinedTextField(color, { color = it }, label = { Text("Cor de $role") }, modifier = Modifier.fillMaxWidth().testTag("theme-role-color"))
                                        Button(enabled = isDesignColor(color), onClick = {
                                            change(if (scheme == "Dark") draft.copy(dark = draft.dark + (role to color)) else draft.copy(light = draft.light + (role to color)))
                                        }, modifier = Modifier.testTag("theme-apply-color")) { Text("Aplicar cor") }
                                        val colors = draft.colors(scheme == "Dark")
                                        val warnings = designContrastPairs.map { (fg, bg) -> Triple(fg, bg, designContrast(colors.getValue(fg), colors.getValue(bg))) }.filter { it.third < 4.5 }
                                        Text(if (warnings.isEmpty()) "Pares de texto verificados: contraste ≥ 4,5:1" else "Atenção: ${warnings.size} pares abaixo de 4,5:1", style = MaterialTheme.typography.titleSmall)
                                        warnings.forEach { Text("${it.first} / ${it.second}: ${(it.third * 100).toInt() / 100.0}:1") }
                                        Text("Verificação limitada aos pares listados; não certifica toda a acessibilidade da tela.", style = MaterialTheme.typography.bodySmall)
                                    }
                                    "Tipografia" -> {
                                        Choice("Família", draft.fontFamily, listOf("Default", "SansSerif", "Serif", "Monospace")) { change(draft.copy(fontFamily = it)) }
                                        Choice("Estilo", styleName, draft.typography.keys.toList()) { styleName = it }
                                        val style = draft.typography.getValue(styleName)
                                        ThemeNumber("Tamanho sp", style.size, 1f..200f) { change(draft.copy(typography = draft.typography + (styleName to style.copy(size = it, lineHeight = maxOf(it, style.lineHeight))))) }
                                        ThemeNumber("Altura da linha sp", style.lineHeight, style.size..300f) { change(draft.copy(typography = draft.typography + (styleName to style.copy(lineHeight = it)))) }
                                        ThemeNumber("Espaçamento entre letras sp", style.tracking, -10f..30f) { change(draft.copy(typography = draft.typography + (styleName to style.copy(tracking = it)))) }
                                        Choice("Peso", style.weight.toString(), (100..900 step 100).map { it.toString() }) { change(draft.copy(typography = draft.typography + (styleName to style.copy(weight = it.toInt())))) }
                                    }
                                    "Formas" -> {
                                        Choice("Forma", shapeName, draft.shapes.keys.toList()) { shapeName = it }
                                        ThemeNumber("Raio dp", draft.shapes.getValue(shapeName), 0f..200f) { change(draft.copy(shapes = draft.shapes + (shapeName to it))) }
                                        Text("Formas padrão Material; propriedades locais e formas fixas do componente têm precedência.")
                                    }
                                    "Espaçamento" -> {
                                        Choice("Token", spacingName, draft.spacing.keys.toList()) { spacingName = it }
                                        ThemeNumber("Espaçamento dp", draft.spacing.getValue(spacingName), 0f..256f) { change(draft.copy(spacing = draft.spacing + (spacingName to it))) }
                                        Text("Use paddingToken no Inspector do componente: xs, sm, md, lg ou xl. Padding explícito se soma ao token.")
                                    }
                                    "Arquivo" -> {
                                        Text("JSON Humming DesignSystem v1. O tema também acompanha o projeto e o pacote SDUI v2.")
                                        Button(enabled = files != null && title.isNotBlank() && title.length <= 80, onClick = { files?.download("humming-design-system.json", DesignSystemCodec.encode(draft.copy(name = title))); message = "Exportação do tema solicitada" }) { Text("Exportar tema JSON") }
                                        TextButton(enabled = files != null, onClick = { files?.open { result ->
                                            result.content?.let { source -> try { val imported = DesignSystemCodec.decode(source); change(imported); title = imported.name; message = "Tema importado no rascunho" }
                                                catch (error: PayloadException) { message = "${error.diagnostic.code}: ${error.diagnostic.path}" } }
                                            result.error?.let { message = it }
                                        } }) { Text("Importar tema JSON") }
                                        TextButton(onClick = { change(DesignSystem()); title = "Tema do aplicativo" }) { Text("Restaurar padrão no rascunho") }
                                    }
                                }
                            }
                            if (!narrow || narrowPreview) Column(Modifier.weight(if (narrow) 1f else .58f).fillMaxHeight()) {
                                Row { TextButton(enabled = preview != null, onClick = { useProject = !useProject }) { Text(if (useProject) "Mostrar componentes" else "Mostrar tela atual") } }
                                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    for ((label, mode) in listOf("Light" to ThemeMode.LIGHT, "Dark" to ThemeMode.DARK)) Column(Modifier.weight(1f).fillMaxHeight()) {
                                        Text(label, style = MaterialTheme.typography.titleMedium)
                                        val contracts = if (useProject && preview != null) preview else CompiledContracts("theme-sample", 0, "screen.json", null, mapOf("screen.json" to themeFixture), emptyMap())
                                        Box(Modifier.weight(1f).fillMaxWidth().border(1.dp, Color.Gray).clipToBounds().testTag("theme-preview-$label")) {
                                            CompiledPreview(contracts.copy(designSystem = draft.copy(mode = mode)))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun ThemeNumber(label: String, value: Float, range: ClosedFloatingPointRange<Float>, apply: (Float) -> Unit) {
    var text by remember(label, value) { mutableStateOf(value.toString()) }
    OutlinedTextField(text, { text = it }, label = { Text(label) }, singleLine = true, modifier = Modifier.fillMaxWidth())
    val parsed = text.toFloatOrNull()?.takeIf { it.isFinite() && it in range }
    TextButton(enabled = parsed != null, onClick = { parsed?.let(apply) }) { Text("Aplicar $label") }
}
