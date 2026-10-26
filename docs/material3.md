# Catálogo Compose Material 3

A referência deste catálogo é a API pública visual do artefato **org.jetbrains.compose.material3:material3:1.8.2**, resolvido por compose.material3 no build existente. A versão declarada em libs.versions.material3 não configura esse accessor. Não houve upgrade para implementar o catálogo. Conferir a resolução com dependencyInsight antes de ampliar o inventário.

Fontes de referência: [relação entre Compose e Compose Multiplatform](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-and-jetpack-compose.html), [API Material 3](https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary) e sources.jar da versão resolvida. A documentação online pode descrever APIs posteriores; o source resolvido e a compilação prevalecem. ExpressiveNavigationBar e NavigationItem são internos em 1.8.2 e não são expostos por reflexão ou imitação.

## Registro e fronteira

[Material3Definitions](../humming-contract/src/commonMain/kotlin/me/developes/humming/sdui/layout/Material3Definitions.kt) declara 92 novos componentes e 10 schemas para wrappers existentes. LayoutDefinitions exporta o conjunto no catálogo puro; SDLayoutMaterial3 instala os handlers na biblioteca visual. O core não depende de Material. O namespace é layout e os nomes abaixo são case-sensitive. As versões dos novos schemas são 1; schemas antigos mantêm suas versões. O registro tem 111 handlers layout e 111 schemas. image, animatedVisibility e style também possuem schemas para o catálogo autoral; propriedades opacas legadas como colorFilter não ganham edição irrestrita por esse acréscimo.

## Inventário API → wire

Todos os identificadores wire abaixo levam prefixo layout:. Variantes em uma linha correspondem às respectivas funções Material com inicial maiúscula, exceto os nomes explicitamente indicados.

| Família / API | Nomes wire | Conteúdo / comportamento |
| --- | --- | --- |
| Button e variantes | button, elevatedButton, filledTonalButton, materialOutlinedButton, materialTextButton | children visuais; onClick e enabled; materialOutlinedButton → OutlinedButton, materialTextButton → TextButton |
| IconButton e variantes | materialIconButton, filledIconButton, filledTonalIconButton, outlinedIconButton | icon como slot visual ou children; materialIconButton → IconButton |
| IconToggleButton e variantes | iconToggleButton, filledIconToggleButton, filledTonalIconToggleButton, outlinedIconToggleButton | checked/stateKey, onChange; icon ou children |
| FloatingActionButton | floatingActionButton, smallFloatingActionButton, largeFloatingActionButton, extendedFloatingActionButton | onClick; icon/children; extended aceita text |
| Card / Surface | card, elevatedCard, outlinedCard, surface | children; clickable/onClick opcionais, enabled |
| Checkbox / Switch | checkbox, switch, triStateCheckbox | checked ou value, stateKey, onChange; tri-state Off/On/Indeterminate |
| RadioButton | radioButton | stateKey compartilhado + value; selected sem chave; onChange e onClick |
| Chips | assistChip, elevatedAssistChip, suggestionChip, elevatedSuggestionChip, filterChip, elevatedFilterChip, inputChip | text, icon; filtros/input têm checked/stateKey, trailingIcon, onChange e onClick |
| Slider | slider, rangeSlider | value/stateKey; range acrescenta endValue/endStateKey; valueFrom/valueTo, steps, onChange/onValueChangeFinished |
| Progress | linearProgressIndicator, circularProgressIndicator | progress com binding, indeterminate; valor final limitado a 0..1 |
| Divider | horizontalDivider, verticalDivider; divider legado | thickness; cores do tema; divider preserva color legado |
| Badge | badge, badgedBox | text/children; badgedBox recebe slot badge e children |
| ListItem | listItem | headlineContent ou text, overlineContent, supportingContent, leadingContent, trailingContent |
| Text / Icon | text, icon | wrappers existentes; icon exige nome registrado no cliente |
| Campos | textField, outlinedTextField | textField preserva contrato legado; outlined usa stateKey/text/label/placeholder/readOnly/isError/singleLine/onChange |
| TopAppBar | topAppBar, centerAlignedTopAppBar, mediumTopAppBar, largeTopAppBar | topAppBar preserva wire legado; novos recebem title, navigationIcon e actions visuais |
| Barras inferiores | bottomAppBar, navigationBar, navigationRail | children no escopo Material correspondente |
| Itens de navegação | navigationBarItem, navigationRailItem, navigationDrawerItem | text/icon, selected ou stateKey+value, onClick; drawer também badge |
| Abas | primaryTabRow, secondaryTabRow, tabRow, primaryScrollableTabRow, secondaryScrollableTabRow, scrollableTabRow, tab, leadingIconTab | row controla selectedTabIndex/stateKey; clicar no tab altera índice do pai antes de onClick |
| Segmentados | singleChoiceSegmentedButtonRow, multiChoiceSegmentedButtonRow, segmentedButton | shape calculado por índice/contagem; seleção única por stateKey/value compartilhados; múltipla por checked/stateKey por item |
| Drawers | modalNavigationDrawer, dismissibleNavigationDrawer, permanentNavigationDrawer | drawerContent e children; modais/dismissible controlam open/stateKey/gesturesEnabled/onChange |
| Sheets de drawer | modalDrawerSheet, dismissibleDrawerSheet, permanentDrawerSheet | children visuais |
| Dialogs | alertDialog, basicAlertDialog, datePickerDialog | visible/stateKey, onDismiss; alert title/text ou children; confirmButton/dismissButton como slots |
| Menus | dropdownMenu, dropdownMenuItem, exposedDropdownMenuBox, exposedDropdownMenu | expanded/stateKey, onDismiss; item text/icon/trailingIcon/onClick; box/menu exposto compartilham chave |
| Bottom sheets | modalBottomSheet, bottomSheetScaffold | modal visible/stateKey/onDismiss; scaffold sheetContent/topBar/children/sheetPeekHeight |
| Scaffold | scaffold | children com padding de conteúdo; topBar, bottomBar, snackbarHost e floatingActionButton |
| Snackbar | snackbar, snackbarHost | snackbar text/children/action/dismissAction; host message/actionLabel/onAction/onDismiss |
| Search | searchBar, dockedSearchBar | text/stateKey, expanded/expandedStateKey, placeholder, enabled, onChange/onSearch, children de resultados |
| Datas | datePicker, dateRangePicker | value/stateKey em millis UTC, range endValue/endStateKey, showModeToggle, onChange |
| Hora | timePicker, timeInput | value/stateKey em HH:mm, is24Hour, onChange |
| Tooltips / Label | tooltipBox, label, plainTooltip, richTooltip | tooltip como slot; children são âncora; plain/rich text ou children; rich title/action |
| Swipe | swipeToDismissBox | value/stateKey Settled/StartToEnd/EndToStart; backgroundContent, children, gesturesEnabled, onChange |
| Refresh | pullToRefreshBox | refreshing/stateKey e onRefresh; children roláveis fornecidos pelo documento |
| Carousel | horizontalMultiBrowseCarousel, horizontalUncontainedCarousel | children são itens; itemWidth/itemSpacing; estado/gestos Material; altura mínima 160dp |

## Estado, eventos e escopos

stateKey nomeia estado do owner de tela; sem chave, usa material3:<node.id>:<propriedade>. Isso exige IDs estáveis para preservar identidade ao recompilar. endStateKey e expandedStateKey têm defaults independentes. Chaves são literais; não interpolar bindings em nomes de estado. Valores iniciais são usados somente quando o estado ainda não existe; remontagem não apaga edição. Campos usam estado textual compatível e bindings #{chave} existentes, sem reinterpretar o contrato.

Controles escrevem o valor antes de onChange. Quando há onChange e onClick na mesma interação de chip/tri-state/radio, executam uma única sequência ordenada. A execução usa ActionInvoker e a política/cancelamento existentes; não há fila ilimitada: enquanto uma sequência está em andamento, novos disparos são recusados, embora um controle que aceite edição continue atualizando seu valor. A saída da composição cancela ações. action:update mantém seu contrato literal; templates não se tornam nova linguagem de ação.

Abas requerem row; segmentedButton requer singleChoiceSegmentedButtonRow ou multiChoiceSegmentedButtonRow; navigationBarItem requer navigationBar; exposedDropdownMenu requer exposedDropdownMenuBox; plainTooltip/richTooltip requerem tooltipBox ou label. Parents fornecem receivers reais, não containers de aparência semelhante. Falta de receiver mostra MATERIAL_SCOPE com ID, caminho e operação. Schemas verificam papéis/slots, mas não representam esses receivers como regras novas no core. Drag/drop geométrico permanece no subconjunto de containers já qualificado; inserir componentes pelo Inspector não comprova novo alvo de drop para cada container Material.

No menu exposto, cada filho de entrada recebe menuAnchor; o popup não recebe âncora. Use a mesma stateKey no box e no menu, e outra chave para o texto do campo. onClick do item deve atualizar seleção e fechar expanded explicitamente. Dialogs/sheets escrevem false antes de onDismiss; confirmButton define sua própria ação de fechar. Remontar com visible=true inicial não reabre estado que já contém false; para reabrir atualize a chave.

Datas aceitam seleção de 1900-01-01 até 2100-12-31 UTC; valores inválidos/dinâmicos fora do intervalo aparecem sem seleção. Intervalo final inválido é omitido. Time aceita HH:mm, limita hora/minuto a 0..23/0..59 e usa 00:00 quando inválido. Alterações externas sincronizam o picker sem emitir onChange artificial. TimeInput 1.8.2 guarda texto interno: o adapter remonta somente esse subtree em mudança externa, sem perder foco a cada digitação do usuário. RangeSlider limita valores ao intervalo configurado; valueFrom precisa ser menor que valueTo e steps é limitado a 10.000. Dois campos de um intervalo são atualizados em sequência antes do evento, sem promessa de transação atômica multiestado.

Refresh escreve true e chama onRefresh; a ação/host deve restaurar false ao terminar, inclusive em erro. Drawer aberto/fechado é sincronizado com estado e gestos; o drawer permanente não tem abertura. SnackbarHost mostra mensagem não vazia no lifecycle da composição, com duração Material padrão; repetição idêntica sem mudança não cria fila. Configurações de tema, cores, animações, insets e overloads Kotlin não são expostos integralmente como propriedades wire.

## Compatibilidade e uso

textButton, outlinedButton e iconButton legados mantêm sua apresentação histórica baseada em Button. Use materialTextButton, materialOutlinedButton e materialIconButton para as primitivas Material correspondentes. Não houve troca visual silenciosa de payloads antigos. As definições legadas acrescentam validação de seus formatos suportados; schemas novos não autorizam métodos arbitrários.

No Studio, **Exemplo Material 3** carrega uma galeria local editável com card, campo, seleção, chips, slider, progresso, abas e ação de salvar. Novas definições aparecem no Inspector e são exportadas pelo compilador genérico. MaterialTheme/style e ProvideTextStyle são configuração de tema/estilo, e objetos Defaults/State/Colors/Tokens, factories remember*, renderers internos e overloads obsoletos não são componentes wire distintos. A cobertura é das famílias visuais públicas da versão resolvida, não de toda função Kotlin nem de versões posteriores.

Ver o [estado atual](current-state.md) para resultados, regressões, plataformas e limitações; a presença nesta tabela não é prova de QA manual em cada target.

## Repetir a auditoria

1. Executar .\gradlew.bat :humming-layout-material3:dependencyInsight --dependency material3 --configuration jvmCompileClasspath para identificar o artefato efetivo.
2. Localizar o sources.jar **dessa versão**, sem usar sources de outra release presente no cache. Executar python scripts/audit-material3.py <caminho-do-sources.jar>. O script não baixa nem extrai arquivos; retorna erro quando encontra uma família visual pública sem mapeamento. Audita nomes top-level, não assinaturas/overloads ou parâmetros. ExposedDropdownMenu é membro de receiver e tem conferência explícita pelo adapter/teste de âncora.
3. Executar os testes de contrato e sample; montar os componentes com os parents exigidos, verificar estado/eventos/descartes e compilar common metadata, Android e Wasm. O nome da função não comprova funcionamento.

A auditoria da versão 1.8.2 considera 100 nomes públicos top-level: 98 famílias visuais e duas configurações de estilo. O catálogo também inclui ExposedDropdownMenu do receiver. Os resultados correntes e exceções ficam no estado atual.

## Catálogo oficial executável

O [README do app](../sample/README.md) reúne um exemplo por handler layout e pelos handlers de ação. O catálogo mostra fonte SDUI, documento, pacote compilado e schema; abre o mesmo documento no Studio. Assets locais são capacidades explícitas em LocalImageSources: layout:image seleciona asset por chave registrada pelo host, sem path arbitrário do payload. A fixture usa catalog-mark; URLs legadas continuam opt-in pelo formato existente.
