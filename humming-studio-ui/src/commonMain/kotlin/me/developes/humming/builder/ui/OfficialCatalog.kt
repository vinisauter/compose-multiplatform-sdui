package me.developes.humming.builder.ui

import kotlinx.serialization.json.*
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.layout.LayoutDefinitions
import me.developes.humming.sdui.action.ActionDefinitions
import me.developes.humming.sdui.studio.*

data class CatalogExample(val id: String, val title: String, val category: String, val description: String,
    val source: String, val component: String? = null, val methods: Set<String> = emptySet()) {
    fun authoring(): AuthoringDocument = LegacyAuthoringCodec(initialNodeCatalog()).importDocument(source, id).requireValue()
    fun compiled(): CompiledContracts {
        val catalog = initialNodeCatalog(); val tokens = defaultPresentationTokens()
        return ContractCompiler(catalog).compileTree(authoring(), TargetCapabilities(catalog.capabilities,
            methods = methods, colorTokens = tokens.colorNames, maskTokens = tokens.maskNames)).requireValue()
    }
}

/** Fixtures are explicit working compositions, not screenshots or direct Compose replacements. */
object OfficialCatalog {
    val components: List<CatalogExample> by lazy { LayoutDefinitions.all.sortedBy { it.identifier }.map { definition ->
        val name = definition.identifier.substringAfter(':')
        CatalogExample("component-$name", name, category(name),
            "${definition.identifier} • schema ${definition.version} • interaja com o exemplo e observe o estado.",
            CatalogFixture(name).source(), definition.identifier)
    } }
    val flows: List<CatalogExample> by lazy { listOf(
        CatalogExample("flow-form", "Formulário Material", "Fluxos SDUI", "Campos, seleção, progresso, abas e confirmação pelo executor.", MATERIAL3_EXAMPLE),
        CatalogExample("flow-expressions", "Expressões e validação", "Fluxos SDUI", "Valores tipados, dependências, tokens, fallback e máscara no mesmo owner.", REACTIVITY_EXAMPLE),
        CatalogExample("flow-actions", "Ações e lifecycle", "Fluxos SDUI", "onCreate, sequência aguardada, condição e atualização de estado.", actionFlow()),
        CatalogExample("flow-drag", "Arraste e bounds", "Fluxos SDUI", "Abra no Studio e ative Modo edição para arrastar entre containers e desfazer.", DROP_EXAMPLE),
    ) }
    val actions: List<CatalogExample> by lazy { ActionDefinitions.all.filter { it.role == NodeRole.ACTION }.map { definition ->
        val name = definition.identifier.substringAfter(':')
        CatalogExample("action-$name", definition.identifier, "Ações", "Execute o handler SDUI registrado; operadores legados preservam as regras descritas no README.",
            catalogActionSource(name), methods = if (name == "method") setOf("getPlatformName") else emptySet())
    } }
    val serverExample: CatalogExample by lazy {
        val root = parseBoundedJson(MATERIAL3_EXAMPLE).jsonObject
        val output = parseBoundedJson("""{"component":"layout:reactiveText","id":"catalog-server-runtime","properties":{"value":{"version":1,"expression":{"language":"humming.expression","version":1,"source":"\"Runtime do servidor: \" + (server.system.runtime ?? \"aguardando dados\")"},"fallback":"Dados indisponíveis"}}}""")
        CatalogExample("flow-publication", "Snapshot e dados de sessão", "Fluxos SDUI", "A definição é imutável; dados BFF chegam como ScreenInputs.server no catálogo desktop.",
            JsonObject(root + ("children" to JsonArray(root.getValue("children").jsonArray + output))).toString())
    }
    val all: List<CatalogExample> get() = components + actions + flows + serverExample
    private fun category(name: String): String = when {
        "Button" in name || "Chip" in name || name in setOf("button", "checkbox", "switch", "radioButton", "triStateCheckbox", "slider", "rangeSlider") -> "Controles"
        "Dialog" in name || "Menu" in name || "Tooltip" in name || "Sheet" in name || "snackbar" in name || name == "label" -> "Overlays"
        "navigation" in name || "Drawer" in name || "Tab" in name || name in setOf("tab", "leadingIconTab") -> "Navegação"
        "Picker" in name || "Input" in name || "TextField" in name || name == "textField" || "SearchBar" in name || name == "searchBar" -> "Entrada de dados"
        else -> "Conteúdo e layout"
    }
}

internal class CatalogFixture(private val name: String, private val stateOutput: Boolean = true) {
    private var serial = 0
    private val valueKey = "catalog-$name-value"
    private val openKey = "catalog-$name-open"
    private val statusKey = "catalog-$name-status"
    private fun node(type: String, props: Map<String, JsonElement> = emptyMap(), children: List<JsonObject> = emptyList()): JsonObject = buildJsonObject {
        put("component", "layout:$type"); put("id", "catalog-$name-${serial++}")
        if (props.isNotEmpty()) put("properties", JsonObject(props))
        if (children.isNotEmpty()) put("children", JsonArray(children))
    }
    private fun s(value: String) = JsonPrimitive(value)
    private fun text(value: String) = node("text", mapOf("text" to s(value)))
    private fun update(key: String, value: String) = buildJsonObject {
        put("component", "action:update"); put("id", "catalog-$name-${serial++}")
        put("properties", buildJsonObject { put("state", key); put("value", value) })
    }
    private fun button(label: String, key: String = statusKey, value: String = "Acionado") =
        node("materialTextButton", mapOf("onClick" to update(key, value)), listOf(text(label)))
    private fun icon() = node("icon", mapOf("icon" to s("Star"), "tint" to s("#5B46B3"), "contentDescription" to s("Estrela")))
    private fun tabs() = listOf(node("tab", mapOf("text" to s("Primeira"))), node("tab", mapOf("text" to s("Segunda"))))
    private fun segment(label: String) = node("segmentedButton", mapOf("text" to s(label), "stateKey" to s(valueKey), "value" to s(label)))
    private fun sheet(type: String) = node(type, children = listOf(text("Menu do catálogo"), button("Fechar menu", openKey, "false")))

    fun source(): String {
        if (name in setOf("reactiveText", "reactiveButton", "validatedTextField")) return REACTIVITY_EXAMPLE
        val definition = LayoutDefinitions.all.first { it.identifier == "layout:$name" }
        val props = linkedMapOf<String, JsonElement>()
        val fields = definition.properties.associateBy { it.name }
        if ("text" in fields) props["text"] = s("Exemplo $name")
        if ("stateKey" in fields) props["stateKey"] = s(valueKey)
        if ("onChangeUpdateState" in fields) props["onChangeUpdateState"] = s(valueKey)
        if ("onClick" in fields) props["onClick"] = update(statusKey, "Acionado")
        if ("onChange" in fields) props["onChange"] = update(statusKey, "Alterado")
        if ("icon" in fields) props["icon"] = if (fields.getValue("icon").kind == PropertyKind.NODES) icon() else s("Star")
        if ("title" in fields) props["title"] = if (fields.getValue("title").kind == PropertyKind.NODES) text("Título") else s("Exemplo $name")
        if ("label" in fields) props["label"] = if (fields.getValue("label").kind == PropertyKind.NODES) text("Nome") else s("Nome")
        var children = if (definition.childrenRole == NodeRole.COMPONENT) listOf(text("Conteúdo $name")) else emptyList()
        var before = emptyList<JsonObject>()
        var wrap: ((JsonObject) -> JsonObject)? = null
        when {
            name == "image" -> { props["asset"] = s("catalog-mark"); props["width"] = s("160"); props["height"] = s("100"); props["contentDescription"] = s("Imagem local registrada") }
            name == "style" -> { props["primary"] = s("#006B5E"); children = listOf(text("Tema fornecido pelo SDUI"), button("Ação com o tema")) }
            name == "animatedVisibility" -> { props["state"] = s(openKey); before = listOf(button("Ocultar", openKey, "false"), button("Mostrar", openKey, "true")) }
            name == "row" || name == "column" -> children = listOf(text("Primeiro item"), button("Segundo item"))
            name == "box" -> children = listOf(node("column", children = listOf(text("Conteúdo do Box"), button("Ação no Box"))))
            name == "topAppBar" -> props["color"] = s("#1A1A1A")
            name == "icon" -> props["width"] = s("40")
            name == "textField" || name == "outlinedTextField" -> { props["text"] = s("Edite aqui"); props["fillMaxWidth"] = s("1") }
            name == "slider" || name == "rangeSlider" -> { props["value"] = s("0.25"); props["endValue"] = s("0.75"); if (name == "slider") props.remove("endValue"); else props["endStateKey"] = s("$valueKey-end"); props["width"] = s("440") }
            name.endsWith("ProgressIndicator") -> { props["progress"] = s("0.65"); props["indeterminate"] = JsonPrimitive(false) }
            name == "verticalDivider" -> props["height"] = s("80")
            name == "badge" -> props["text"] = s("8")
            name == "badgedBox" -> { props["badge"] = node("badge", mapOf("text" to s("8"))); children = listOf(icon()) }
            name == "listItem" -> { props["text"] = s("Título do item"); props["supportingContent"] = text("Descrição de apoio"); props["leadingContent"] = icon() }
            name == "scaffold" -> { props["topBar"] = node("centerAlignedTopAppBar", mapOf("title" to s("Scaffold SDUI"))); props["bottomBar"] = node("bottomAppBar", children = listOf(button("Ação inferior"))); props["floatingActionButton"] = node("smallFloatingActionButton", mapOf("onClick" to update(statusKey, "FAB")), listOf(icon())); props["height"] = s("400"); children = listOf(text("Conteúdo com insets do Scaffold")) }
            name.endsWith("TopAppBar") && name != "topAppBar" -> { props["navigationIcon"] = button("Voltar"); props["actions"] = button("Ação") }
            name == "navigationBar" || name == "navigationRail" -> children = listOf(
                node(if (name == "navigationBar") "navigationBarItem" else "navigationRailItem", mapOf("text" to s("Início"), "icon" to icon(), "stateKey" to s(valueKey), "value" to s("home"))),
                node(if (name == "navigationBar") "navigationBarItem" else "navigationRailItem", mapOf("text" to s("Perfil"), "icon" to icon(), "stateKey" to s(valueKey), "value" to s("profile"))))
            name == "navigationBarItem" -> wrap = { node("navigationBar", children = listOf(it)) }
            name == "navigationRailItem" -> wrap = { node("navigationRail", children = listOf(it)) }
            name.endsWith("TabRow") || name == "tabRow" -> { children = tabs(); props["width"] = s("460") }
            name == "tab" || name == "leadingIconTab" -> wrap = { node("primaryTabRow", mapOf("width" to s("460")), listOf(it, node("tab", mapOf("text" to s("Outra aba"))))) }
            name.endsWith("SegmentedButtonRow") -> children = listOf(segment("A"), if (name.startsWith("multi")) node("segmentedButton", mapOf("text" to s("B"), "stateKey" to s("$valueKey-b"))) else segment("B"))
            name == "segmentedButton" -> { props["value"] = s("A"); wrap = { node("singleChoiceSegmentedButtonRow", children = listOf(it, segment("B"))) } }
            name.endsWith("NavigationDrawer") -> { props["drawerContent"] = sheet(if (name.startsWith("modal")) "modalDrawerSheet" else if (name.startsWith("dismissible")) "dismissibleDrawerSheet" else "permanentDrawerSheet"); props["height"] = s("360"); if (!name.startsWith("permanent")) { props["stateKey"] = s(openKey); before = listOf(button("Abrir menu", openKey, "true")) } }
            name.endsWith("DrawerSheet") -> { props["height"] = s("280"); children = listOf(text("Itens do menu"), node("navigationDrawerItem", mapOf("text" to s("Início"), "selected" to JsonPrimitive(true), "icon" to icon(), "onClick" to update(statusKey, "Menu selecionado")))) }
            name.endsWith("Dialog") || name == "modalBottomSheet" -> {
                props["visible"] = JsonPrimitive(false); props["stateKey"] = s(openKey); props["onDismiss"] = update(statusKey, "Fechado")
                before = listOf(button("Abrir exemplo", openKey, "true"))
                if (name != "modalBottomSheet") { props["confirmButton"] = button("Confirmar", openKey, "false"); props["dismissButton"] = button("Cancelar", openKey, "false") }
                children = if (name == "datePickerDialog") listOf(node("datePicker", mapOf("stateKey" to s(valueKey)))) else listOf(text("Conteúdo do diálogo"), button("Fechar", openKey, "false"))
            }
            name == "dropdownMenu" -> { props["stateKey"] = s(openKey); before = listOf(button("Abrir menu", openKey, "true")); children = listOf(node("dropdownMenuItem", mapOf("text" to s("Selecionar"), "onClick" to update(openKey, "false")))) }
            name == "exposedDropdownMenuBox" || name == "exposedDropdownMenu" -> {
                val field = node("outlinedTextField", mapOf("text" to s("Escolha uma opção"), "stateKey" to s(valueKey), "readOnly" to JsonPrimitive(true)))
                val options = listOf(node("dropdownMenuItem", mapOf("text" to s("Opção A"), "onClick" to JsonArray(listOf(update(valueKey, "Opção A"), update(openKey, "false"))))))
                props["stateKey"] = s(openKey)
                if (name == "exposedDropdownMenuBox") children = listOf(field, node("exposedDropdownMenu", mapOf("stateKey" to s(openKey)), options))
                else { children = options; wrap = { node("exposedDropdownMenuBox", mapOf("stateKey" to s(openKey)), listOf(field, it)) } }
            }
            name == "bottomSheetScaffold" -> { props["height"] = s("400"); props["sheetContent"] = node("column", children = listOf(text("Arraste a folha para expandir"), button("Ação da folha"))); children = listOf(text("Conteúdo principal")) }
            name == "snackbar" -> { props["action"] = button("Confirmar"); props["text"] = s("Mensagem do catálogo") }
            name == "snackbarHost" -> { props["message"] = s("#{$openKey}"); props["actionLabel"] = s("Confirmar"); props["onAction"] = update(statusKey, "Confirmado"); before = listOf(button("Exibir mensagem", openKey, "Mensagem do catálogo")) }
            name == "searchBar" || name == "dockedSearchBar" -> { props["text"] = s(""); props["placeholder"] = s("Digite para buscar"); props["expandedStateKey"] = s(openKey); props["onSearch"] = update(statusKey, "Busca enviada"); children = listOf(button("Resultado local", openKey, "false")) }
            name == "datePicker" || name == "dateRangePicker" -> { props["value"] = s("1705276800000"); if (name == "dateRangePicker") { props["endStateKey"] = s("$valueKey-end"); props["endValue"] = s("1705536000000") }; props["height"] = s("500") }
            name == "timePicker" || name == "timeInput" -> props["value"] = s("08:30")
            name == "tooltipBox" || name == "label" -> { props["tooltip"] = node("plainTooltip", mapOf("text" to s("Dica do catálogo"))); children = listOf(button("Mantenha pressionado para ver a dica")) }
            name == "plainTooltip" || name == "richTooltip" -> { if (name == "richTooltip") { props["title"] = text("Título da dica"); props["action"] = button("Entendi") }; wrap = { node("tooltipBox", mapOf("tooltip" to it), listOf(button("Mantenha pressionado para ver a dica"))) } }
            name == "swipeToDismissBox" -> { props["backgroundContent"] = node("column", mapOf("backgroundColor" to s("#FFE0E0")), listOf(text("Solte para dispensar"))); children = listOf(node("card", mapOf("height" to s("80"), "fillMaxWidth" to s("1")), listOf(text("Arraste para o lado")))); before = listOf(button("Restaurar", valueKey, "Settled")) }
            name == "pullToRefreshBox" -> {
                props["height"] = s("320")
                val finish = buildJsonObject {
                    put("component", "action:update"); put("id", "catalog-$name-${serial++}")
                    put("properties", buildJsonObject { put("state", valueKey); put("value", "false"); put("delay", 400) })
                }
                props["onRefresh"] = JsonArray(listOf(update(valueKey, "true"), finish))
                children = listOf(node("column", mapOf("verticalScroll" to s("true")), (1..15).map { text("Puxe para atualizar • item $it") }))
            }
            name.endsWith("Carousel") -> { props["height"] = s("180"); children = (1..6).map { node("card", mapOf("width" to s("180"), "height" to s("150")), listOf(text("Cartão $it"), icon())) } }
        }
        val main = node(name, props, children)
        return node("column", mapOf("paddingAll" to s("12")), before + listOf(wrap?.invoke(main) ?: main) +
            if (stateOutput) listOf(text("Estado: #{$valueKey}"), text("Resultado: #{$statusKey}")) else emptyList()).toString()
    }
}

private fun actionFlow() = """{"component":"layout:column","id":"actions-screen","children":[
 {"component":"action:event","id":"on-create","properties":{"onCreate":{"component":"action:update","id":"init","properties":{"state":"status","value":"Criado"}},"onStateChange":{"component":"action:update","id":"observe","properties":{"state":"observed","value":"Mudança observada"}}}},
 {"component":"layout:button","id":"run-actions","properties":{"onClick":[
   {"component":"action:update","id":"set-condition","properties":{"state":"ready","value":"true","delay":100}},
   {"component":"action:ifState","id":"branch","properties":{"state":"ready","equals":"true","then":{"component":"action:then","id":"then","properties":{"condition":"ready","invoke":{"component":"action:update","id":"done","properties":{"state":"status","value":"Sequência concluída"}}}}}}
 ]},"children":[{"component":"layout:text","id":"run-label","properties":{"text":"Executar sequência"}}]},
 {"component":"layout:text","id":"action-result","properties":{"text":"Status: #{status}"}},
 {"component":"layout:text","id":"action-observer","properties":{"text":"Observer: #{observed}"}}
]}"""
