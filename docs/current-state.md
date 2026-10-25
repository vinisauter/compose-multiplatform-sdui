# Estado atual

Este é o ponto de entrada para **continuar o desenvolvimento**. Reúne o estado observado, o rastreamento das etapas e a próxima ação. O [plano](development-plan.md) define dependências, escopo e critérios de aceite; este documento é a única fonte de status e retomada.

Atualizado durante T11: cache versionado integrado opt-in, carga fria e recuperação. Suíte JVM: 52 contrato + 72 core + 4 ações + 95 autoria + 7 rede + 7 navegação + 40 sample = 277 casos, zero falhas/erros/ignorados. Transporte compilado para metadata/JS/Wasm/Android e executado em HTTP loopback JVM. Formulário observado no Chrome/Wasm. Compilação não comprova rede em browser/Android; Apple permanece sem execução.

## Ponto de retomada

| Campo | Registro atual |
| --- | --- |
| Última atualização | 2026-10-24 — T09 em b388bc1 e commits anteriores; T10 com código/evidência abaixo; commits locais sem push |
| Última entrega | T11: cache em 4333931; integração opt-in Graph com JourneyDefinitions e teste Compose de hit/TTL, descrita abaixo |
| Tarefa de implementação em execução | T11 — Codex; integração opt-in do cache no Graph, carga de rota e testes Compose |
| Próxima tarefa | **T11 — jornada, navegação e cache versionado** |
| Próxima ação concreta | Definir codec/restauração de snapshot versionado sem dados personalizados e acrescentar fluxo demonstrável no sample |
| Critério para avançar | Definição e dado personalizado separados; cache particionado por sessão/versão; recuperação e invalidação demonstradas sem refazer o isolamento já entregue |
| Bloqueio de implementação conhecido | Nenhum para iniciar T11 local; fixtures e servidor loopback não dependem de backend remoto |
| Limite do próximo recorte | Navegação/cache do cliente; sem backend remoto, publicação, credenciais no payload ou mudanças de infraestrutura |

### Protocolo quando o pedido for “continue o desenvolvimento”

1. Ler `AGENTS.md`, este ponto de retomada, a linha da etapa e seu detalhe no plano. Conferir o Git e o código para detectar mudanças posteriores ao registro.
2. Se houver tarefa em execução, retomar sua próxima ação registrada. Se houver bloqueio, verificar se ele persiste; não apagar a evidência. Sem tarefa ativa, escolher a próxima pronta cujas dependências estejam concluídas, seguindo a prioridade do plano.
3. Registrar responsável e status **em execução** antes da implementação. Resolver as decisões do recorte, implementar e verificar seus critérios. Não usar a existência deste planejamento como autorização para ações externas fora do escopo solicitado.
4. Antes de encerrar, atualizar a linha da etapa, os fatos afetados, a evidência e esta tabela de retomada. Em trabalho parcial, anotar exatamente o que falta e a próxima ação; em bloqueio, comando/causa e trabalho independente possível.
5. Marcar **concluída** somente com critérios demonstrados. Então promover a próxima etapa elegível a **pronta**. Se código e documento divergirem, reconciliar o estado com inspeção/evidência antes de avançar.

Ausência de evidência não significa conclusão. Não reiniciar etapas concluídas sem mudança ou falha que justifique nova verificação. Não avançar silenciosamente sobre uma etapa necessária bloqueada.

## Rastreamento das funcionalidades e etapas

Estados: **pronta**, **pendente**, **em execução**, **bloqueada**, **concluída**. “Pendente” indica etapa ainda não executada, mesmo quando parte do código já existe. Uma conclusão registra revisão/commit ou arquivos, testes/fluxo executados, plataforma e limites. Use o [modelo de relatório](validation.md) para evidência detalhada e vincule-o na linha correspondente.

| Etapa | Funcionalidade / entrega | Estado | Evidência ou próximo passo |
| --- | --- | --- | --- |
| T00 | Base executável JVM/desktop | concluída | Codex, 2026-10-23: descoberta e build JVM passaram; 1 teste Compose com edição/binding/ação passou; imagem inspecionada. Janela nativa iniciou sem repetir a exceção, mas não foi inspecionada manualmente. Ver [evidência e limites](validation.md) |
| T01 | Fixtures e testes de caracterização | concluída | Codex, 2026-10-23: 30 testes do core em JVM, 0 falhas/erros/ignorados; sample UP-TO-DATE. Runtime preservado; limitações caracterizadas e encaminhadas em [validação](validation.md) |
| T02 | Diagnósticos e resolução de payload | concluída | 49 testes core e 1 Compose aprovados em JVM; parseNode limitado, diagnóstico com localização, falhas de provider/handler distintas e eventos inválidos rejeitados |
| T03 | Executor suspenso e política de falha | concluída | Executor aguardado com CONTINUE/STOP_ON_ERROR, limites, cancelamento e cleanup; 67 core, 3 ações e 2 Compose aprovados, incluindo navegação suspensa e retorno |
| T04 | Lifecycle, loading e bindings legados | concluída | 78 core, 3 ações e 6 Compose aprovados em JVM; descarte/recomposição, loading concorrente, eventos causais, navegação/retorno e falha recuperável; ver [validação](validation.md) |
| T05 | Definições/schema do catálogo inicial | concluída | 15 definições versão 1, diagnóstico por campo/papel e adapters compatíveis; 110 testes JVM aprovados; ver [validação](validation.md) |
| T06 | Fronteira pura e consumo sem UI | concluída | 114 testes JVM aprovados, classpath puro e core Android compilado; migração/recompilação descritas no contrato |
| T07 | Valores tipados e escopos de tela/ação | concluída | Codex, 2026-10-24: 232 testes JVM no conjunto; 33 contrato/69 core/32 sample incluem tipos, concorrência, owner distinto, duas entradas da mesma rota, retorno, erro e cancelamento no pop. Wasm, JS/metadata e core/navegação Android compilados; Chrome confirmou isolamento/retorno |
| T08 | Linguagem de expressões limitada | concluída | Codex, 2026-10-24: 10 testes novos da linguagem; 43 contrato/242 JVM no conjunto; grammar/versão, escopos, precedência, curto-circuito, tipos, números/índices inválidos, limites e cancelamento. Metadata/JS/Wasm compilados; sem integração UI |
| T09 | Propriedades reativas, tokens e validação | concluída | Codex: três componentes opt-in, envelopes 1, schema 2 para tokens/máscara, 258 testes JVM, metadata/JS/Wasm e Android visual compilados; texto/habilitação/visibilidade/ação observados Chrome; evidência e limites nos recortes T09 abaixo |
| T10 | Transporte HTTP com ownership e resultado | concluída | Codex: 7 testes de rede JVM com MockEngine/HTTP loopback e ramos reais; matriz total 265 casos; metadata/JS/Wasm/Android compilados; ownership e exportação de supertipos públicos corrigidos; limites abaixo |
| T11 | Jornada, navegação e cache versionado | em execução | Codex: cache integrado opt-in ao Graph; 7 testes navegação e 36 sample JVM; deep link real/recuperação/reload/cancelamento qualificados em JVM; troca de sessão do host qualificada em JVM; restauração e exemplo interativo ainda pendentes |
| T12 | Bridge de capacidades nativas | pendente | Capacidade real no host e não suporte explícito nos demais |
| T13 | Documento autoral e importação/exportação | concluída | 21 testes autorais; 135 JVM no conjunto; metadata/JS/Wasm compilados; modelo, índices, codec canônico e ponte legada com diagnósticos |
| T14 | Comandos, chaves de ordem e undo local | concluída | 158 testes JVM no conjunto, metadata/JS/Wasm compilados; midpoint, transações por CAS, inversas e undo/redo com limites e concorrência |
| T15 | Compilador puro do subconjunto legado | concluída | 172 testes JVM aprovados; golden, capacidades, expansão limitada, ciclos e jornada compilada executada pelo renderer real; ver [validação](validation.md) |
| T16 | Preview do catálogo no host Wasm | concluída | Chrome/Wasm observado: edição/binding/update/MISSING_METHOD/recuperação; host sem provider URL; teste JVM de descarte; ver [validação](validation.md) |
| T17 | Editor local e Inspector | concluída | Implementação 2d9d6eb; 183 testes JVM; importação/ação/downloads observados pelo agente e abertura manual/confirmado relatados pelo usuário em 2026-10-24; ver [validação](validation.md) |
| T18 | Seleção, bounds e drop geométrico | concluída | 8a12ea0/04df7c0/75f50f6; 202 testes JVM; commit de drag no Chrome e guia confirmada manualmente pelo usuário em 2026-10-24; ver [validação](validation.md) |
| T19 | Grafo de interações e conectores | concluída | Codex, 2026-10-24: 94 testes autorais + 30 sample; canvas usa bounds reais, cúbicas/setas/hit/zoom/Escape; relações/undo e jornadas exportadas executadas. Chrome observou ida/volta e seleção rosa; pacote com 3 artefatos verificado |
| T20 | Aplicação de servidor e interfaces | pendente | Criar fronteiras/casos de uso/host local com autorização |
| T21 | Rascunhos/revisões em PostgreSQL | pendente | Validar persistência/transações em banco real local |
| T22 | Publicação imutável e capacidades | pendente | Publicar snapshot local compatível sem dados de sessão |
| T23 | Resolução BFF e isolamento de dados | pendente | Resolver fontes registradas com limites e isolamento |
| T24 | Edição remota com revisão e recuperação | pendente | Salvar/reabrir/reconectar sem perda silenciosa |
| T25 | Múltiplos editores e conflitos | pendente | Testar concorrência, replay e undo entre processos |
| T26 | Qualificação por plataforma e recorte | pendente | Iniciar após T00; anexar evidência de cada recorte anunciado |

Os requisitos completos e a ordem das dependências estão no [plano](development-plan.md). A numeração identifica tarefas; não significa executar T07–T12 antes de T13–T19. A prioridade inicial é base → schemas/fronteira → autoria local → manipulação visual; capacidades adicionais do cliente podem avançar em paralelo quando elegíveis.

### Trabalho parcial e bloqueios

T08 concluída no recorte puro, responsável Codex; sem trabalho parcial ativo. ExpressionSpec exige marcador/versão, AST é privado e o programa reutilizável não guarda contexto/resultados. Operadores e acesso são fechados e tipados; não existem chamadas/reflexão/atribuição. Parsing limita fonte/tokens/profundidade; avaliação limita visitas/texto e propaga cancelamento do chamador. Inteiros fora do intervalo seguro, resultado não finito, divisão por zero e índices inválidos têm diagnóstico por offset. Missing difere de Null; operadores booleanos/coalesce e acesso sobre ausência têm curto-circuito. Formatação do resultado Double não é promessa de texto canônico entre targets; igualdade numérica/testes são por valor.

Comandos: .\gradlew.bat :humming-contract:jvmTest :humming-contract:compileCommonMainKotlinMetadata :humming-contract:compileKotlinJs :humming-contract:compileKotlinWasmJs --console=plain (build/t08/expressions.log); regressões de contrato/core/ações/autoria/sample e Wasm em build/t08/consumers.log; teste final do contrato em build/t08/final-tests.log. BUILD SUCCESSFUL, 43 contrato + 69 core + 4 ações + 94 autoria + 32 sample = 242, zero falhas/erros/ignorados. Dez casos novos cobrem gramática, escopos/chaves/índices, coerção proibida, curto-circuito, versões/chamadas proibidas, números, budgets, reutilização e CancellationException original. Duas expectativas iniciais de fixture foram corrigidas: (1).field para testar acesso escalar e 1/1e-309 para overflow sem atingir primeiro a guarda de inteiro seguro. Não se alteraram dependências.

A linguagem não foi ligada a propriedades, Inspector ou runtime de UI; nenhum binding #{chave} foi reinterpretado. Não há nova prova de execução JS/Wasm do evaluator (somente compilação). T09 é o próximo recorte para dependências/reatividade, capacidades, formatação e fallback; não declarar expressão de UI pronta por existir parser.

T07 concluída no recorte local, responsável Codex. Nenhum trabalho parcial ativo. O estado agora tem autoridade tipada e projeção textual viva; Missing remove, Null permanece explícito e strings/IDs não passam por inferência. response/error são slots por execução/owner, herdados por sequências aninhadas e limpos em finally. ISOLATED_ENTRIES é opt-in do host, ativado no preview compilado do Studio; LEGACY_SHARED permanece o padrão compatível. server/nav vêm de entryInputs; state pertence ao owner. Retorno na pilha conserva a entrada e pop limpa valores e cancela jobs. Não há restauração automática após processo/página reiniciados.

Execução: .\gradlew.bat :humming-contract:jvmTest :humming-core:jvmTest :humming-actions:jvmTest :humming-studio-model:jvmTest :sample:composeApp:jvmTest :humming-contract:compileCommonMainKotlinMetadata :humming-contract:compileKotlinJs :sample:composeApp:compileKotlinWasmJs --console=plain (build/t07/full-check-retry.log), mais :humming-core:jvmTest após o caso entre owners (build/t07/owner-context.log). Resultado acumulado atual: 232 testes, zero falhas/erros/ignorados. :humming-core:compileDebugKotlinAndroid :humming-navigation:compileDebugKotlinAndroid :humming-core:compileCommonMainKotlinMetadata :humming-navigation:compileCommonMainKotlinMetadata passaram em build/t07/android-metadata.log. Metadata emitiu warnings de unique_name duplicado de SavedState entre dependências; não se alteraram versões. Isso não comprova execução Android nem binário Apple.

ScreenIsolationTest executou o renderer real: Alpha na primeira entrada, Beta na segunda, erros de método e provider somente na segunda, tarefa aguardando cancelada no pop, Alpha restaurado e terceira visita com valor inicial. Revisão final: navegação resolve provider pelo owner que iniciou a ação, preservando MISSING_PROVIDER e diagnosticando UNKNOWN_ROUTE, sem contaminar o grafo global. Checks completos repetidos em build/t07/provider-owner.log (232 testes, Wasm e navegação Android/metadata aprovados). Testes core verificam slots concorrentes, herança em ramos, limpeza em cancelamento e troca de owner; as regressões legadas permanecem aprovadas. As primeiras tentativas pararam por inferência/import de testes, corrigidos sem mudar dependências.

Chrome/Wasm em 127.0.0.1:8080: Exemplo de estado isolado recebeu edição na primeira visita; Nova visita começou com inicial; segunda recebeu Segunda visita; Voltar à visita anterior restaurou a edição original. Capturas build/t07/browser-first-entry.png, browser-second-fresh.png, browser-second-edited.png e browser-return-preserved.png. A substituição pela textarea oculta se comportou como inserção; foi usado o texto efetivamente desenhado no canvas como prova, não o valor DOM isolado. Duplo clique no campo seguido de paste substituiu o valor da segunda visita. Servidor e logs em build/t07/studio-server.log, ignorados pelo Git. Sem efeitos externos ou push.

T19 concluída no recorte local. Comandos finais: .\gradlew.bat :humming-studio-model:jvmTest :humming-studio-model:compileCommonMainKotlinMetadata :humming-studio-model:compileKotlinJs :sample:composeApp:jvmTest :sample:composeApp:compileKotlinWasmJs --console=plain (build/t19/final-checks.log). BUILD SUCCESSFUL; 94 testes autorais + 30 sample, zero falhas/erros/ignorados. Captura Compose build/reports/interaction-canvas.png no sample: botão interno → borda da tela e curva rosa; teste cobre zoom e Escape. Projeção de interações limita visitas/arestas, trata compartilhamento/ramos sem seguir ciclos de tela. Política de autoconexão visual explícita e testada; navegação de botão para sua própria tela permanece válida.

No Chrome/Wasm: exemplo abriu segunda tela e voltou; canvas desenhou a conexão, clique na curva selecionou navigation:goTo e pintou rosa. Captura final build/t19/browser-final-connector.png confirma rótulos, árvore sem duplicação e Inspector com destino derivado da relação; download real humming-journey.json contém graph.json e 2 screens/*.json. O teste StudioJourneyTest executou o pacote exportado após decode pelo mesmo host. Na revisão foram corrigidos glifos de direção, duplicação de ação raiz na árvore e edição redundante de destiny quando há relação. O teste antigo de inserção passou após rolagem explícita para acomodar os campos de posição. Verificação adicional após preservar viewport entre revisões: build/t19/viewport-check.log, 30 testes sample e Wasm aprovados. Logs de tentativas e correções em build/t19, ignorados pelo Git. Sem nova prova Android/Apple, roteamento de obstáculos, drag para criar conexões ou servidor.

Após a orientação de carregar o exemplo, o usuário respondeu “agora sim”, confirmando a guia que faltava no aceite de T18. Essa é evidência manual, distinta da observação automatizada do commit no Chrome e da captura de pixels em Compose; detalhes e limites estão em [validação](validation.md). Não repetir a verificação sem motivo novo.

Retomar por T09, dependências T05/T08 concluídas. T20–T25 permanecem posteriores; este recorte não criou servidor, transporte HTTP ou persistência remota.

### T09 — recorte de dependências e streams tipados

Codex, 2026-10-24: ExpressionDependencies no contrato analisa chaves raiz estáticas, acessos aninhados, raízes dinâmicas e todos os ramos da AST. expressionValues no core projeta as dependências antes de avaliar, mantém fallback explícito e diagnóstico localizado, recupera após correção e não cria job no ViewModel. Escopos temporários response/error são rejeitados para propriedades. Não há nova interpretação de strings ou schema visual neste recorte.

Executado: descoberta :humming-contract:tasks/:humming-core:tasks --all; :humming-contract:jvmTest e :humming-core:jvmTest; compilação JS/Wasm do contrato e Wasm do core. Logs build/t09/tasks.log, reactivity.log e reactivity-final.log. Seis testes novos: três de análise e três de coleta, edição/remoção, cancelamento, acesso dinâmico, fallback/recuperação e diagnóstico. Contrato 46 e core 72 testes aprovados; outros módulos mantêm a execução anterior, sem nova execução neste recorte. UI/browser e Apple não foram exercitados. T09 permanece em execução; faltam schema opt-in, integração visual, tokens, apresentação e validação.

### Manutenção documental independente da implementação

| Item | Estado | Evidência / próxima ação |
| --- | --- | --- |
| M01 — excluir artefato JSON obsoleto restante | concluída | Em T00, a consulta ao caminho anteriormente bloqueado retornou ausente. A limpeza física restante foi verificada; não foi necessário encerrar processos ou repetir a exclusão nesta execução. |

Os relatórios e planos antigos estão ausentes. O registro de M01 conserva o resultado da verificação e não altera a próxima etapa funcional.

## Escopo implementado

HummingUI é uma POC de cliente SDUI. Interpreta nós JSON, mantém estado textual, resolve componentes e ações por namespace e integra bibliotecas no sample. O editor local integra árvore/Inspector, seleção/drop, canvas de interações e preview/exportação de jornadas. Não há backend de contratos, publicação imutável ou colaboração implementados nos módulos incluídos.

| Módulo | Responsabilidade observada |
| --- | --- |
| `humming-contract` | Modelo, valores tipados, codec, diagnósticos e schemas puros; catálogo inicial sem handlers |
| `humming-studio-model` | Documento autoral normalizado, comandos, histórico, compilador por capacidades e codecs; não depende do runtime |
| `humming-core` | Registro, ViewModel, estado, despacho e helpers visuais; exporta o contrato |
| `humming-actions` | Métodos registrados, eventos, atualização e condições |
| `humming-navigation` | Grafo e ações com Compose Navigation; cache de definições por rota e owners por entrada no modo isolado |
| `humming-layout-material3` | Catálogo de componentes Compose Material 3 e leitura de propriedades |
| `sample:composeApp` | Host, providers e exemplos compartilhados com targets de aplicação |
| `sample:iosApp` | Projeto incluído para integração Apple; execução depende de ambiente compatível |
| `humming-network` | Incluído: adaptador HTTP opt-in, sem engine ou cliente criado pelo módulo |

Os includes reais estão em [settings.gradle.kts](../settings.gradle.kts). Não há motivo para renomear os módulos existentes apenas para reproduzir uma arquitetura futura.

## Caminhos para leitura do código

| Fluxo | Entrada e continuação |
| --- | --- |
| Integração | [App.kt](../sample/composeApp/src/commonMain/kotlin/sample/app/App.kt) registra bibliotecas/providers em [HummingSDUI.kt](../humming-core/src/commonMain/kotlin/me/developes/humming/sdui/HummingSDUI.kt) |
| Validação local JVM | [local-smoke.json](../sample/composeApp/src/commonMain/composeResources/files/layout/local-smoke.json) e [LocalSmokeTest.kt](../sample/composeApp/src/jvmTest/kotlin/sample/app/LocalSmokeTest.kt): campo, binding e ação update pelo renderer real |
| Nó e parsing | [ServerDrivenNode.kt](../humming-contract/src/commonMain/kotlin/me/developes/humming/sdui/common/ServerDrivenNode.kt) e [NodeJson.kt](../humming-contract/src/commonMain/kotlin/me/developes/humming/sdui/common/NodeJson.kt) |
| Registro | [SDLibrary.kt](../humming-core/src/commonMain/kotlin/me/developes/humming/sdui/common/SDLibrary.kt) e [NodeProvider.kt](../humming-core/src/commonMain/kotlin/me/developes/humming/sdui/common/NodeProvider.kt) |
| Execução/estado | [SDUIFormViewModel.kt](../humming-core/src/commonMain/kotlin/me/developes/humming/sdui/common/SDUIFormViewModel.kt) e [SDAction.kt](../humming-actions/src/commonMain/kotlin/me/developes/humming/sdui/action/SDAction.kt) |
| Navegação | [SDNavigation.kt](../humming-navigation/src/commonMain/kotlin/me/developes/humming/sdui/navigation/SDNavigation.kt) e [Graph.kt](../humming-navigation/src/commonMain/kotlin/me/developes/humming/sdui/navigation/components/Graph.kt) |
| Componentes | [SDLayoutMaterial3.kt](../humming-layout-material3/src/commonMain/kotlin/me/developes/humming/sdui/layout/SDLayoutMaterial3.kt) e os arquivos `SDC*` de seu catálogo |
| Transporte HTTP opt-in | [SDNetwork.kt](../humming-network/src/commonMain/kotlin/me/developes/humming/sdui/action/SDNetwork.kt) e [SduiHttpTransport.kt](../humming-network/src/commonMain/kotlin/me/developes/humming/sdui/network/SduiHttpTransport.kt) |
| Payloads do sample | [app-navigation.json](../sample/composeApp/src/commonMain/composeResources/files/navigation/app-navigation.json) e [login.json](../sample/composeApp/src/commonMain/composeResources/files/layout/login.json) |

O fluxo básico é host → provider → parsing → resolução de biblioteca/componente → composição → evento → despacho de ações → atualização de estado ou navegação. O core continua visual e exporta humming-contract, que contém modelo/JSON/schemas sem UI. Color permanece no core; consumidores headless dependem diretamente de humming-contract.

## Limitações que exigem trabalho

| Área | Comportamento observado | Consequência para a implementação |
| --- | --- | --- |
| Parsing | `parseNode` valida estrutura, nomes e limites com diagnóstico; propriedades continuam textuais, inclusive JSON null como `"null"` | T07: adaptação tipada preservando o legado |
| Serialização | O wrapper de JSON pode retornar o JSON original em `toString()` depois de mutações no nó | Editor usa codecs explícitos e testes de round trip; não usa o toString legado |
| Identidade | ID ausente é gerado durante parsing e persistido na importação autoral | Exportações posteriores preservam a identidade; expansão compartilhada usa IDs determinísticos e mapa de origem do compilador |
| Registro | Nome inválido e handler/provider ausente possuem diagnóstico com localização | Schemas do subconjunto validam campo/papel; ampliar somente com fixtures |
| Ações | `executeActions` aguarda ramos e providers; `invokeActions` continua sendo lançamento independente | Handlers customizados devem aguardar a API suspensa |
| Falha/cancelamento | Cancelamento propaga, contadores finalizam, callbacks preservam causa; ações da UI cancelam ao sair da composição | Novos efeitos devem preservar essas garantias |
| Estado/bindings | Estado tipado e isolamento opt-in por entrada; bindings legados frios e expressionValues por dependência; leitura legada pode inicializar chave vazia | Completar T09 visual; extensões imperativas precisam descartar seus bindings |
| Eventos | Emissão inicial preservada; idempotência e origem causal impedem ciclos no catálogo | Métodos customizados devem usar updateStateFromAction; revisões são confladas |
| Navegação | Cache de definições por rota; modo legado compartilhado ou ISOLATED_ENTRIES opt-in; retorno preserva entrada anterior | Qualificar restauração e carregamento/deep links posteriores |
| Rede | HttpClient/endpoints do host; GET/POST reais, limites e resultados por execução; App recebe urlProvider opcional | Qualificar execução nas plataformas restantes e integrar navegação/cache na T11 |
| Exemplos | Há `event:loader`, `navigation:goToUrl`, providers `link`/`firestore`, recurso ausente e `action:method` com propriedade `method` | Selecionar/corrigir uma fixture local; não assumir que todos os JSONs são funcionais |

Essas limitações descrevem o código, não comportamentos que devam ser perpetuados. O [contrato](contract.md) distingue compatibilidade de defeitos a corrigir.

## Build e evidência disponível

Os builds principais declaram toolchain JVM **25** e compatibilidade Java Android **17**. O [catálogo](../gradle/libs.versions.toml) declara Kotlin `2.4.21`, Compose `1.8.2`, AGP `8.13.2`, Ktor `3.6.0` e Navigation `2.9.2`; aliases JVM/Spring usam Kotlin `2.2.10`. Esses valores não demonstram resolução dos artefatos ou compatibilidade entre versões.

A referência agregada mais recente é 271 testes JVM após o recorte de cache da T11, sem falhas/erros/ignorados. As fixtures de parsing/limites pertencem ao commonTest do contrato; o teste headless comprova ausência de classes UI no classpath. Evidências incrementais estão nos recortes T09; procedimentos em [validação](validation.md).

A dependência Lifecycle do core permanece api porque seu ViewModel aparece na API pública. O host JVM fornece handle de estado em memória por factory explícita; os outros hosts mantêm o caminho padrão. O contrato é exportado pelo core e usa bytecode JVM 17 com toolchain 25.

Gradle 9.5.1 com Java 25.0.3 em Windows 11 x64 executou `projects`, descoberta de tarefas e compilação JVM. O SDK Android foi encontrado, com plataformas instaladas até `android-37.0`; o core Android compilou após T06, sem execução em dispositivo ou qualificação dos demais módulos Android. Workflows em [.github](../.github) foram auditados: ainda referenciam `androidApp`, paths Apple na raiz, JDK 17 e Gradle 7.2 em um workflow. Não foram alterados ou acionados nesta entrega; corrigir/qualificar em T26 antes de uso. Problemas de configuração devem ser reproduzidos antes de alterar versões, source sets ou CI.

## Evolução ainda não implementada

Permanecem pendentes schemas para o restante do catálogo, políticas de cache, bridge nativa, persistência remota, publicação e colaboração. Valores tipados, linguagem pura, formulário reativo opt-in, isolamento por entrada e grafo visual local possuem evidência nos recortes anteriores. Máscaras de domínio adicionais e aritmética decimal exata não são capacidades instaladas; nenhuma propriedade monetária foi declarada. O [plano](development-plan.md) define o escopo restante.

### T09 — envelope e texto/visibilidade no catálogo

Codex, 2026-10-24: ReactiveProperty com envelope 1 estrito, fallback Text/Boolean, validação de sintaxe/versionamento e diagnóstico localizado. layout:reactiveText schema 1 é uma capacidade nova; os 15 schemas legados permanecem versão 1. Inspector edita os envelopes como JSON; o botão “Exemplo de expressões” importa uma fixture neutra com campo legado e mensagem reativa. O componente acompanha o estado, oculta/reexibe conteúdo e mostra fallback/diagnóstico recuperável em falha de tipo.

Executado no Windows/JVM: 49 testes contrato, 72 core, 94 autoria e 34 sample aprovados (ações não reexecutadas neste recorte). JS/Wasm do contrato e Wasm do sample compilados. Logs build/t09/visual-final.log e visual-image.log. Três novos testes de contrato e dois Compose cobrem envelope/tipos/versão/offset, importação → compilação → edição → ocultar/reexibir, falha/recuperação e assinaturas únicas/zero após descarte. Imagem build/t09/reactive-form-jvm.png renderizada e inspecionada com fundo branco no host de teste. As primeiras tentativas detectaram expectativa antiga do tamanho do catálogo e classificação do envelope legado; ambas corrigidas e reexecutadas. Sem execução browser/Android/Apple neste recorte.

T09 permanece parcial: habilitação reativa, validação de campo, tokens/máscaras por capacidade e qualificação browser ainda precisam ser entregues. Não promover T10.

### T09 — habilitação e validação de campo

Codex, 2026-10-24: reactiveButton e validatedTextField, ambos schema 1, ampliam o catálogo para 18 definições sem alterar os 15 schemas legados. A fixture “Exemplo de expressões” usa campo canônico Text, erro imediato para nome vazio, botão habilitado por regra, ação real action:update e mensagem de confirmação. Campo não inicializa Missing, não converte Number/Null em Text nem grava apresentação derivada; tipo incompatível exibe diagnóstico e edição explícita recupera o formulário.

Executado: :humming-contract:jvmTest, :humming-core:jvmTest, :humming-actions:jvmTest, :humming-studio-model:jvmTest, :sample:composeApp:jvmTest e :sample:composeApp:compileKotlinWasmJs; build/t09/form-final.log, BUILD SUCCESSFUL. 49 + 72 + 4 + 94 + 34 = 253 testes JVM sem falhas. O teste Compose do formulário cobre estado Missing preservado, validação vazia, habilitação, ação de confirmação, ocultar/reexibir e Number inválido preservado até edição corretiva; outros testes verificam recuperação e descarte. A primeira compilação encontrou colisão de nome interno com Layout.key; corrigida para stateKey sem mudar API pública. Sem browser/Android/Apple neste recorte.

Próximo recorte T09: tokens de tema e máscaras declarados como capacidades, testes da apresentação separada do valor canônico e qualificação browser. T09 ainda não concluída.

### T09 — conclusão: tokens, máscaras e qualificação

Codex, 2026-10-24: PresentationTokens imutável, AsciiDigitMask pura limitada e mapas de cursor monótonos. TargetCapabilities declara colorTokens/maskTokens e o compilador rejeita nomes ausentes. LocalWorkspace compartilha seu registro com CompiledPreview. reactiveText e validatedTextField passam a schema 2 para colorToken/maskToken; reactiveButton e os 15 schemas legados permanecem versão 1. Envelope continua versão 1. Novos componentes também podem ser inseridos pela paleta com defaults válidos; campos geram chaves canônicas distintas e histórico/compilação são preservados.

Evidência JVM: 52 contrato + 72 core + 4 ações + 95 autoria + 35 sample = 258 testes, zero failures/errors/skipped nos XMLs. Logs build/t09/tokens.log, final-check.log e editor-components-final.log. Testes adicionais cobrem zeros à esquerda, overflow sem truncamento, limites, caracteres inválidos, cursores/roundtrip, cópia do registro, capacidades ausentes, edição mascarada preservando canônico, recuperação e criação/undo/redo dos novos componentes. Full check compilou metadata contrato/core/layout, Android debug da biblioteca visual e Wasm do sample. tokens.log também compilou JS/Wasm do contrato. O ajuste final de autoria foi reexecutado em JVM e compilado no Wasm; uma referência local de stateKey inicialmente fora do escopo foi corrigida antes do resultado final. Warning SavedState KLIB duplicado e warnings legados permanecem sem atualização de dependências.

Chrome/Wasm, servidor de desenvolvimento somente em 127.0.0.1:8080: carregado “Exemplo de expressões”; vazio exibiu “Preencha o nome” e botão desabilitado; entrada Ana atualizou “Olá, Ana” e habilitou Confirmar; clique executou action:update e mostrou “confirmado” com token positive; valor ocultar removeu a mensagem e desabilitou o botão; alteração posterior reexibiu a mensagem com o valor corrente. Capturas build/t09/browser-initial.png, browser-confirmed.png, browser-hidden.png e browser-restored.png. A tentativa de substituir o último texto anexou Pronto ao valor anterior; a captura comprova o valor real ocultarPronto e sua mensagem correspondente, sem alegar substituição exata. Substituição exata também passou nos testes Compose JVM. Na primeira observação Chrome houve timeout de leitura de frame durante carga; a captura posterior e o fluxo real concluíram normalmente.

Limites: execução de máscaras/erros de tipo e descarte comprovada em Compose JVM; o fluxo browser comprovou texto, visibilidade, habilitação, validação, ação e cor, sem qualificar toda a matriz de máscaras no browser. Compilação Android não é execução; Apple não executado. Não há aritmética monetária, rede, backend ou publicação nesta entrega. O restante da POC segue o plano: T09 concluída, T10 pronta e próxima ação na tabela inicial.

### T10 — conclusão: transporte injetado e HTTP real

Codex, 2026-10-24: humming-network incluído com build mínimo (sem Compose/engine/publicação no módulo), SduiHttpTransport, HttpEndpoint/Policy/Outcome e SDNetwork(transport). Endpoint/métodos/headers são capacidades fixadas pelo host; request não aceita URL/credenciais do payload. Corpo via bodyState tipado ou bodyJson serializado, POST real, ramos aguardados no mesmo owner e limpeza dos resultados antes/depois da execução. Falha sem onFailure participa do executor; com ramo é dado tratado. App recebe urlProvider opcional em vez de instalar provider remoto que criava HttpClient a cada chamada.

Executado: :humming-network:tasks --all; testes de todos os seis módulos JVM; metadata/JS/Wasm/Android do transporte e Wasm do sample. build/t10/final-contract.log: BUILD SUCCESSFUL, 128 tarefas; XMLs: 52 contrato + 72 core + 4 ações + 95 autoria + 7 rede + 35 sample = 265 casos, zero failures/errors/skipped. Testes de rede com MockEngine e HttpServer real em 127.0.0.1/porta efêmera cobrem GET/POST, corpo canônico e literal, 503/302, JSON/UTF-8 inválidos, tipo de conteúdo, limites antes/depois de serializar, timeout próprio, cancelamento externo em voo, IO seguro, defeito de programação, oito POSTs concorrentes, reuso após falha/timeout, ramo aguardado, ausência de resultado antigo e cleanup dos slots. Servidor, cliente, engine compartilhada e executor fechados pelos respectivos donos em finally. Logs intermediários http-tests-final.log, transport-check.log e exported-owner.log também registram os recortes.

Metadata inicialmente falhou por supertipo público Lifecycle ausente no consumidor. Investigação dos sources já resolvidos mostrou EmptyFile.kt no commonMain do wrapper JetBrains 2.9.6 e ViewModel real em AndroidX 2.9.4, já dependência interna da navegação. Exportar api de ViewModel/SavedStateHandle no core com a mesma versão resolveu metadata sem atualizar versões resolvidas; a dependência direta experimental no adaptador foi removida. Primeiros testes JUnit retornavam valor em vez de Unit e foram corrigidos antes da execução final. Ktor 3.6 usa readBuffer e kotlinx.io.IOException; não foram mantidos novos usos deprecated.

Limites: HTTP foi executado em JVM com OkHttp e MockEngine, não no browser/Android/Apple. JS/Wasm/Android foram compilados; metadata não qualifica Apple. Studio local não instala rede e nenhum backend remoto, credencial, distribuição ou push foi usado. O provider URL antigo exige injeção explícita do host. Não há GET/POST implícito por renderização, retry instalado, cache, autenticação automática ou persistência de resposta; cache/versão e jornada são T11. T10 concluída nesse escopo; T11 pronta.

### T11 — recorte de cache de definições, ainda sem ativação em Graph

Codex, 2026-10-24: JourneyDefinitionCache, DefinitionKey/Snapshot/Policy/Metrics em commonMain da navegação. Partição por jornada/versão/sessão/rota/provider/recurso; source imutável com cópia por parse no hit; snapshot formato 1, schemas conhecidos e IDs explícitos/únicos; TTL/LRU e limites de caracteres/concorrência; cargas coalescidas em SupervisorJob do host; invalidação/close com cleanup não cancelável e proteção contra resultado antigo. coroutines-test saiu de commonMain para commonTest; tipos públicos core/coroutines passaram a api no módulo navigation.

Executado: descoberta :humming-navigation:tasks --all; seis testes novos em commonTest executados JVM, cobrindo cópias e partições, TTL/LRU/latência, duas esperas com cancelamento de uma, invalidação durante provider não cooperativo sem ressurreição, falha/versão/formato/ID e recuperação, fechamento em finally cancelado e limites de tamanho/concorrência. build/t11/cache-final.log: sete módulos de testes JVM, metadata/Wasm/Android navegação e Wasm sample, BUILD SUCCESSFUL. Total 271 testes: 52 contrato + 72 core + 4 ações + 95 autoria + 7 rede + 6 navegação + 35 sample; zero failures/errors/skipped. Ajuste final de providerLoads no início real do loader passou novamente em JVM/metadata/Wasm: build/t11/cache-metrics.log.

Naquele recorte o cache ainda não estava conectado ao Graph e T11 não foi concluída. Graph mantinha componentNodes por rota e usava componentNodes[routeName]!! na composição; deep link frio e restauração precisam de carga explícita/estado de falha. Próximo trabalho: fixar contexto de jornada/sessão no host, integrar cache opt-in preservando a API legada, substituir a suposição de preload por carregamento cancelável ao entrar na rota, e verificar reload/troca de sessão/retorno/deep link/provider com Compose real. Também falta codec/validação de restauração persistida; DefinitionSnapshot atual é apenas DTO. Não promover T12. Nenhum novo fluxo UI/browser foi qualificado pela existência do cache.

### T11 — integração opt-in do cache à navegação

Codex, 2026-10-24: JourneyDefinitions fixa jornada/versão/sessão e recebe cache/loader do host. SDNavigation mantém os construtores legados e adiciona overload opt-in. Graph consulta TTL na navegação versionada, publica somente definição carregada com espera ativa e substitui a suposição de preload obrigatório por carga no owner da entrada. Startup e carga fria encaminham falhas com causa/diagnóstico ao owner; descarte da composição cancela a espera. Graph.reloadRoute invalida e aguarda substituição validada, preservando conteúdo anterior em falha.

Executado: build/t11/graph-integration.log — navegação/sample JVM, metadata/Wasm navegação; build/t11/graph-compose.log — 7 testes navegação e 36 sample, Android navegação e Wasm sample, BUILD SUCCESSFUL. O teste Compose usa HummingSDUI/Navigation reais sem provider de rotas instalado: primeira visita carrega, segunda consulta hit, terceira após TTL carrega novamente; ordenação suspensa legada e retorno continuam aprovados. O teste comum verifica identidade completa, partições e invalidação do adaptador. Outros módulos não foram reexecutados neste recorte (último agregado era 271; dois casos adicionados). git diff --check sem erros.

Limites: nenhuma observação nova no browser. A carga fria agora tem implementação, mas deep link real, falha/retry/reload no fluxo Compose e descarte/troca de sessão ainda precisam de testes próprios. Nenhum cache de formulário, restauração persistida, logout automático, transporte implícito ou política EAGER/PREFETCH foi instalado. T11 permanece em execução; não promover T12.

### T11 — deep link frio, recuperação e cancelamento de entrada

Codex, 2026-10-24: Graph recebe diretamente JourneyDefinitions por overload para hosts com ComponentHandler próprio e expõe openDeepLink(String) usando NavController.handleDeepLink real. Três testes novos Compose/HummingSDUI qualificam: startup com falha preserva causa/diagnóstico de rota e admite retry sem recriar host; deep link sem cache/preload carrega a rota e reload inválido mantém o conteúdo anterior antes da recuperação; sair da rota cancela o waiter enquanto a carga compartilhada termina no host e atende a visita seguinte via cache. URI sem padrão correspondente retorna false.

Executado: build/t11/cold-focused.log, três testes aprovados. build/t11/cold-final.log, sete módulos JVM e Wasm sample, BUILD SUCCESSFUL; total 276 casos (52/72/4/95/7/7/39), zero falhas/erros/ignorados. Metadata/Wasm/Android navegação compilaram em build/t11/cold-load.log, cuja primeira execução de testes falhou apenas por assertSame ignorar o wrapper de recuperação de stacktrace das coroutines; teste corrigido para verificar a exceção original na cadeia de causas. Nenhuma atualização de dependências. git diff --check aprovado.

Limites: deep link é entrada da API do host, sem integração de URL com SO/browser; teste usa Compose JVM, sem nova observação Chrome. Retry/reload são decisões do host, sem ação nova de payload, botão automático ou persistência. Falta demonstrar troca de sessão descartando owners e definir restauração compatível; T11 continua em execução.

### T11 — fronteira de troca de sessão no host

Codex, 2026-10-24: teste JourneySessionTest com HummingSDUI, Compose Navigation e modo ISOLATED_ENTRIES reais. O host desmonta a jornada, destrói/limpa ViewModelStore, invalida sessão a e só então monta sessão b com novo owner/identidade. Campo editado e inputs de a são limpos, tarefa suspensa da entrada é cancelada, Graph descartado recusa deep link, b inicia com campo inicial/input próprio e o cache mantém apenas sua definição. A mesma fonte declarativa é carregada em ambas as partições; nenhum dado de tela é serializado.

Executado: build/t11/session-focused.log, teste novo aprovado. Regressão completa do sample em build/t11/session-final.log: 40 casos JVM, zero falhas/erros/ignorados, BUILD SUCCESSFUL. Outros módulos mantêm a última evidência do recorte anterior. Não há logout automático no core nem obrigação de usar backend; o protocolo de lifecycle pertence ao host e está descrito no contrato. Restauração persistida e exemplo interativo de jornada ainda pendentes; não concluir T11.
