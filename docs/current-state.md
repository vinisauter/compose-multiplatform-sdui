# Estado atual

Este é o ponto de entrada para **continuar o desenvolvimento**. Reúne o estado observado, o rastreamento das etapas e a próxima ação. O [plano](development-plan.md) define dependências, escopo e critérios de aceite; este documento é a única fonte de status e retomada.

Atualizado após T27: catálogo Material 3 1.8.2 ampliado com 92 adapters e 7 schemas legados, galeria editável e auditoria reproduzível. Relatórios: 341 JVM regulares + 15 integrações reais, sem falhas/erros/ignorados. Metadata, Android e Wasm compilados; UI nova exercitada em Compose/JVM. Sem deploy público.

## Ponto de retomada

| Campo | Registro atual |
| --- | --- |
| Última atualização | 2026-10-25; T25 em 6e3a1ae; T20–T24 em 42d6de8, dc51068, 4cdf7bc, 2f67dd9, 277ad71 e 681f91e; commits locais sem push |
| Última entrega | T27: famílias visuais públicas Material 3 resolvido, galeria, schemas, auditoria e 356 casos no conjunto |
| Tarefa de implementação em execução | Nenhuma; T27 concluída no recorte descrito abaixo |
| Próxima tarefa | T26 para observação dos componentes novos em Android/browser, caso esses targets sejam anunciados; demais gates da matriz permanecem |
| Próxima ação concreta | Abrir Exemplo Material 3 no Studio para QA browser; executar fluxo Android em ambiente compatível; não confundir compile com execução |
| Critério para avançar | Novo recorte deve definir aceite e evidência; não anunciar browser remoto/Apple nem distribuição a partir desta conclusão local |
| Bloqueio de implementação conhecido | Nenhum para qualificação local JVM/Android/Wasm; Apple exige host compatível |
| Limite do próximo recorte | Qualificação local dos componentes; preservar wrappers legados e versão resolvida; sem push/distribuição |

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
| T11 | Jornada, navegação e cache versionado | concluída | Codex: cache opt-in, deep link frio, reload/retry, cancelamento, sessão e codec de definição; 281 testes JVM e exemplo Chrome/Wasm; políticas/limites no fechamento T11 abaixo |
| T12 | Bridge de capacidades nativas | concluída | Codex: registro opt-in, uptime Android real/API35, rejeição/indisponibilidade, cancelamento e recriação; 286 testes JVM, probe com hostRecreation=PASS e tela observada; detalhes abaixo |
| T13 | Documento autoral e importação/exportação | concluída | 21 testes autorais; 135 JVM no conjunto; metadata/JS/Wasm compilados; modelo, índices, codec canônico e ponte legada com diagnósticos |
| T14 | Comandos, chaves de ordem e undo local | concluída | 158 testes JVM no conjunto, metadata/JS/Wasm compilados; midpoint, transações por CAS, inversas e undo/redo com limites e concorrência |
| T15 | Compilador puro do subconjunto legado | concluída | 172 testes JVM aprovados; golden, capacidades, expansão limitada, ciclos e jornada compilada executada pelo renderer real; ver [validação](validation.md) |
| T16 | Preview do catálogo no host Wasm | concluída | Chrome/Wasm observado: edição/binding/update/MISSING_METHOD/recuperação; host sem provider URL; teste JVM de descarte; ver [validação](validation.md) |
| T17 | Editor local e Inspector | concluída | Implementação 2d9d6eb; 183 testes JVM; importação/ação/downloads observados pelo agente e abertura manual/confirmado relatados pelo usuário em 2026-10-24; ver [validação](validation.md) |
| T18 | Seleção, bounds e drop geométrico | concluída | 8a12ea0/04df7c0/75f50f6; 202 testes JVM; commit de drag no Chrome e guia confirmada manualmente pelo usuário em 2026-10-24; ver [validação](validation.md) |
| T19 | Grafo de interações e conectores | concluída | Codex, 2026-10-24: 94 testes autorais + 30 sample; canvas usa bounds reais, cúbicas/setas/hit/zoom/Escape; relações/undo e jornadas exportadas executadas. Chrome observou ida/volta e seleção rosa; pacote com 3 artefatos verificado |
| T20 | Aplicação de servidor e interfaces | concluída | Codex: aplicação pura (5 casos), HTTP real (5 casos), classpath headless, smoke runLocal/401 e 296 testes no conjunto; detalhes abaixo |
| T21 | Rascunhos/revisões em PostgreSQL | concluída | Codex: adapter/migration, CAS e recibos, seis integrações reais incluindo restart/rollback/cancelamento; 304 casos no conjunto; detalhes abaixo |
| T22 | Publicação imutável e capacidades | concluída | Codex: 304 JVM regulares + 8 integrações PostgreSQL/HTTP/Compose; pin e dados pessoais separados; detalhes abaixo |
| T23 | Resolução BFF e isolamento de dados | concluída | Codex: 5 testes de política + 3 HTTP reais, 9 integrações PostgreSQL/HTTP/Compose no conjunto; detalhes abaixo |
| T24 | Edição remota com revisão e recuperação | concluída | Codex: desktop opt-in, Inspector com HTTP/arquivo/PostgreSQL, replay/perda de ack e conflito arquivado; limites/qualificação abaixo |
| T25 | Múltiplos editores e conflitos | concluída | Journal/replay, dois hosts/controllers, presença e undo guardado; 342 casos; evidência abaixo |
| T26 | Qualificação por plataforma e recorte | concluída | Somente recorte local identificado na matriz abaixo; Android/Wasm compilados, CI configurada sem execução remota; demais gates explícitos |
| T27 | Catálogo Compose Material 3 | concluída | 92 adapters novos, 7 schemas legados, 341 JVM + 15 integrações; metadata/Android/Wasm; limites abaixo |

Os requisitos completos e a ordem das dependências estão no [plano](development-plan.md). A numeração identifica tarefas; não significa executar T07–T12 antes de T13–T19. A prioridade inicial é base → schemas/fronteira → autoria local → manipulação visual; capacidades adicionais do cliente podem avançar em paralelo quando elegíveis.

### Evidências históricas — T07 a T19

Os registros desta seção preservam evidências anteriores; não substituem o ponto de retomada no início do documento.

T08 concluída no recorte puro, responsável Codex; sem trabalho parcial ativo. ExpressionSpec exige marcador/versão, AST é privado e o programa reutilizável não guarda contexto/resultados. Operadores e acesso são fechados e tipados; não existem chamadas/reflexão/atribuição. Parsing limita fonte/tokens/profundidade; avaliação limita visitas/texto e propaga cancelamento do chamador. Inteiros fora do intervalo seguro, resultado não finito, divisão por zero e índices inválidos têm diagnóstico por offset. Missing difere de Null; operadores booleanos/coalesce e acesso sobre ausência têm curto-circuito. Formatação do resultado Double não é promessa de texto canônico entre targets; igualdade numérica/testes são por valor.

Comandos: .\gradlew.bat :humming-contract:jvmTest :humming-contract:compileCommonMainKotlinMetadata :humming-contract:compileKotlinJs :humming-contract:compileKotlinWasmJs --console=plain (build/t08/expressions.log); regressões de contrato/core/ações/autoria/sample e Wasm em build/t08/consumers.log; teste final do contrato em build/t08/final-tests.log. BUILD SUCCESSFUL, 43 contrato + 69 core + 4 ações + 94 autoria + 32 sample = 242, zero falhas/erros/ignorados. Dez casos novos cobrem gramática, escopos/chaves/índices, coerção proibida, curto-circuito, versões/chamadas proibidas, números, budgets, reutilização e CancellationException original. Duas expectativas iniciais de fixture foram corrigidas: (1).field para testar acesso escalar e 1/1e-309 para overflow sem atingir primeiro a guarda de inteiro seguro. Não se alteraram dependências.

A linguagem não foi ligada a propriedades, Inspector ou runtime de UI; nenhum binding #{chave} foi reinterpretado. Não há nova prova de execução JS/Wasm do evaluator (somente compilação). Naquele recorte, T09 era a próxima etapa para dependências/reatividade, capacidades, formatação e fallback; não declarar expressão de UI pronta por existir parser.

T07 concluída no recorte local, responsável Codex. Nenhum trabalho parcial ativo. O estado agora tem autoridade tipada e projeção textual viva; Missing remove, Null permanece explícito e strings/IDs não passam por inferência. response/error são slots por execução/owner, herdados por sequências aninhadas e limpos em finally. ISOLATED_ENTRIES é opt-in do host, ativado no preview compilado do Studio; LEGACY_SHARED permanece o padrão compatível. server/nav vêm de entryInputs; state pertence ao owner. Retorno na pilha conserva a entrada e pop limpa valores e cancela jobs. Não há restauração automática após processo/página reiniciados.

Execução: .\gradlew.bat :humming-contract:jvmTest :humming-core:jvmTest :humming-actions:jvmTest :humming-studio-model:jvmTest :sample:composeApp:jvmTest :humming-contract:compileCommonMainKotlinMetadata :humming-contract:compileKotlinJs :sample:composeApp:compileKotlinWasmJs --console=plain (build/t07/full-check-retry.log), mais :humming-core:jvmTest após o caso entre owners (build/t07/owner-context.log). Resultado acumulado atual: 232 testes, zero falhas/erros/ignorados. :humming-core:compileDebugKotlinAndroid :humming-navigation:compileDebugKotlinAndroid :humming-core:compileCommonMainKotlinMetadata :humming-navigation:compileCommonMainKotlinMetadata passaram em build/t07/android-metadata.log. Metadata emitiu warnings de unique_name duplicado de SavedState entre dependências; não se alteraram versões. Isso não comprova execução Android nem binário Apple.

ScreenIsolationTest executou o renderer real: Alpha na primeira entrada, Beta na segunda, erros de método e provider somente na segunda, tarefa aguardando cancelada no pop, Alpha restaurado e terceira visita com valor inicial. Revisão final: navegação resolve provider pelo owner que iniciou a ação, preservando MISSING_PROVIDER e diagnosticando UNKNOWN_ROUTE, sem contaminar o grafo global. Checks completos repetidos em build/t07/provider-owner.log (232 testes, Wasm e navegação Android/metadata aprovados). Testes core verificam slots concorrentes, herança em ramos, limpeza em cancelamento e troca de owner; as regressões legadas permanecem aprovadas. As primeiras tentativas pararam por inferência/import de testes, corrigidos sem mudar dependências.

Chrome/Wasm em 127.0.0.1:8080: Exemplo de estado isolado recebeu edição na primeira visita; Nova visita começou com inicial; segunda recebeu Segunda visita; Voltar à visita anterior restaurou a edição original. Capturas build/t07/browser-first-entry.png, browser-second-fresh.png, browser-second-edited.png e browser-return-preserved.png. A substituição pela textarea oculta se comportou como inserção; foi usado o texto efetivamente desenhado no canvas como prova, não o valor DOM isolado. Duplo clique no campo seguido de paste substituiu o valor da segunda visita. Servidor e logs em build/t07/studio-server.log, ignorados pelo Git. Sem efeitos externos ou push.

T19 concluída no recorte local. Comandos finais: .\gradlew.bat :humming-studio-model:jvmTest :humming-studio-model:compileCommonMainKotlinMetadata :humming-studio-model:compileKotlinJs :sample:composeApp:jvmTest :sample:composeApp:compileKotlinWasmJs --console=plain (build/t19/final-checks.log). BUILD SUCCESSFUL; 94 testes autorais + 30 sample, zero falhas/erros/ignorados. Captura Compose build/reports/interaction-canvas.png no sample: botão interno → borda da tela e curva rosa; teste cobre zoom e Escape. Projeção de interações limita visitas/arestas, trata compartilhamento/ramos sem seguir ciclos de tela. Política de autoconexão visual explícita e testada; navegação de botão para sua própria tela permanece válida.

No Chrome/Wasm: exemplo abriu segunda tela e voltou; canvas desenhou a conexão, clique na curva selecionou navigation:goTo e pintou rosa. Captura final build/t19/browser-final-connector.png confirma rótulos, árvore sem duplicação e Inspector com destino derivado da relação; download real humming-journey.json contém graph.json e 2 screens/*.json. O teste StudioJourneyTest executou o pacote exportado após decode pelo mesmo host. Na revisão foram corrigidos glifos de direção, duplicação de ação raiz na árvore e edição redundante de destiny quando há relação. O teste antigo de inserção passou após rolagem explícita para acomodar os campos de posição. Verificação adicional após preservar viewport entre revisões: build/t19/viewport-check.log, 30 testes sample e Wasm aprovados. Logs de tentativas e correções em build/t19, ignorados pelo Git. Sem nova prova Android/Apple, roteamento de obstáculos, drag para criar conexões ou servidor.

Após a orientação de carregar o exemplo, o usuário respondeu “agora sim”, confirmando a guia que faltava no aceite de T18. Essa é evidência manual, distinta da observação automatizada do commit no Chrome e da captura de pixels em Compose; detalhes e limites estão em [validação](validation.md). Não repetir a verificação sem motivo novo.

Naquele recorte, a retomada era T09, com dependências T05/T08 concluídas. T20–T25 permanecem posteriores; este recorte não criou servidor, transporte HTTP ou persistência remota.

### T09 — recorte de dependências e streams tipados

Codex, 2026-10-24: ExpressionDependencies no contrato analisa chaves raiz estáticas, acessos aninhados, raízes dinâmicas e todos os ramos da AST. expressionValues no core projeta as dependências antes de avaliar, mantém fallback explícito e diagnóstico localizado, recupera após correção e não cria job no ViewModel. Escopos temporários response/error são rejeitados para propriedades. Não há nova interpretação de strings ou schema visual neste recorte.

Executado: descoberta :humming-contract:tasks/:humming-core:tasks --all; :humming-contract:jvmTest e :humming-core:jvmTest; compilação JS/Wasm do contrato e Wasm do core. Logs build/t09/tasks.log, reactivity.log e reactivity-final.log. Seis testes novos: três de análise e três de coleta, edição/remoção, cancelamento, acesso dinâmico, fallback/recuperação e diagnóstico. Contrato 46 e core 72 testes aprovados; outros módulos mantêm a execução anterior, sem nova execução neste recorte. UI/browser e Apple não foram exercitados. T09 permanece em execução; faltam schema opt-in, integração visual, tokens, apresentação e validação.

### Manutenção documental independente da implementação

| Item | Estado | Evidência / próxima ação |
| --- | --- | --- |
| M01 — excluir artefato JSON obsoleto restante | concluída | Em T00, a consulta ao caminho anteriormente bloqueado retornou ausente. A limpeza física restante foi verificada; não foi necessário encerrar processos ou repetir a exclusão nesta execução. |

Os relatórios e planos antigos estão ausentes. O registro de M01 conserva o resultado da verificação e não altera a próxima etapa funcional.

## Escopo implementado

HummingUI é uma POC de cliente SDUI. Interpreta nós JSON, mantém estado tipado com projeção textual legada, resolve componentes e ações por namespace e integra bibliotecas no sample. O editor local integra árvore/Inspector, seleção/drop, canvas de interações e preview/exportação de jornadas. Há aplicação/HTTP local opcional e persistência PostgreSQL; publicação imutável e colaboração ainda não foram implementadas.

| Módulo | Responsabilidade observada |
| --- | --- |
| `humming-contract` | Modelo, valores tipados, codec, diagnósticos e schemas puros; catálogo inicial sem handlers |
| `humming-studio-model` | Documento autoral normalizado, comandos, histórico, compilador por capacidades e codecs; não depende do runtime |
| `humming-core` | Registro, ViewModel, estado, despacho e helpers visuais; exporta o contrato |
| `humming-actions` | Métodos registrados, eventos, atualização e condições |
| `humming-navigation` | Compose Navigation; definições opt-in por jornada/versão/sessão e owners por entrada no modo isolado |
| `humming-layout-material3` | Catálogo de componentes Compose Material 3 e leitura de propriedades |
| `sample:composeApp` | Host, providers e exemplos compartilhados com targets de aplicação |
| `sample:iosApp` | Projeto incluído para integração Apple; execução depende de ambiente compatível |
| `humming-network` | Incluído: adaptador HTTP opt-in, sem engine ou cliente criado pelo módulo |
| `humming-server-application` | Casos de uso/DTOs, grants por documento, interfaces de persistência e edição; sem UI/driver |
| `humming-server-postgres` | JDBC, migrations e revisão/recibo transacionais; sem rotas/UI |
| `humming-server-host` | Composition root HTTP loopback com autenticação e adapter escolhido explicitamente |

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

A referência agregada mais recente é 298 testes JVM regulares e seis integrações PostgreSQL/HTTP após a T21, sem falhas/erros/ignorados. As fixtures de parsing/limites pertencem ao commonTest do contrato; o teste headless comprova ausência de classes UI no classpath. Evidências incrementais estão nos recortes T09; procedimentos em [validação](validation.md).

A dependência Lifecycle do core permanece api porque seu ViewModel aparece na API pública. O host JVM fornece handle de estado em memória por factory explícita; os outros hosts mantêm o caminho padrão. O contrato é exportado pelo core e usa bytecode JVM 17 com toolchain 25.

Gradle 9.5.1 com Java 25.0.3 em Windows 11 x64 executou `projects`, descoberta de tarefas e compilação JVM. O SDK Android foi encontrado, com plataformas instaladas até `android-37.0`; o core Android compilou após T06, sem execução em dispositivo ou qualificação dos demais módulos Android. Workflows em [.github](../.github) foram auditados: ainda referenciam `androidApp`, paths Apple na raiz, JDK 17 e Gradle 7.2 em um workflow. Não foram alterados ou acionados nesta entrega; corrigir/qualificar em T26 antes de uso. Problemas de configuração devem ser reproduzidos antes de alterar versões, source sets ou CI.

## Evolução ainda não implementada

Permanecem pendentes schemas para o restante do catálogo, publicação imutável, BFF, integração remota do Studio, colaboração e qualificação adicional por plataforma. Cache de definições, bridge local e adapter PostgreSQL possuem evidência nos recortes correspondentes. Valores tipados, linguagem pura, formulário reativo opt-in, isolamento por entrada e grafo visual local possuem evidência nos recortes anteriores. Máscaras de domínio adicionais e aritmética decimal exata não são capacidades instaladas; nenhuma propriedade monetária foi declarada. O [plano](development-plan.md) define o escopo restante.

## Histórico incremental — T09 em diante

As próximas ações citadas nos registros abaixo eram válidas na data de cada recorte. A tabela inicial é a única instrução vigente de retomada.

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

### T11 — codec limitado de snapshots de definição

Codex, 2026-10-24: DefinitionSnapshotCodec em commonMain, envelope determinístico com identidade completa/fonte original/idade/formato 1, relógio do host, campos fechados e budgets finitos. Decodificação rejeita outra identidade, expiração e timestamp futuro; admissão permanece validada pelo catálogo do cache. Três testes comuns executados JVM cobrem roundtrip exato e restauração em cache novo, seis partições divergentes, formato/campo extra/idade/clock, limites e fonte inválida não admitida.

Executado: build/t11/snapshot.log — 10 testes navegação, metadata/Wasm/Android navegação, BUILD SUCCESSFUL. Nenhuma escrita em disco/storage remoto ou restauração automática de back stack/formulário. Resta acrescentar exemplo de host e qualificar restauração de definição através do renderer; T11 permanece em execução.

### T11 — fechamento da jornada local e políticas de restauração

Codex, 2026-10-24: VersionedJourneyExample em commonMain e entrada Wasm ?journey. Host local registra apenas fontes declarativas e navegação; duas telas, formulário isolado, deep link, reload com falha solicitada, recuperação, troca de sessão e métricas. Recriar o host restaura envelopes em cache novo pela mesma API/renderer; snapshots são somente memória da composição externa, sem persistência de página/processo. Campos e pilha começam de novo; não são serializados. Troca de sessão descarta store/cache antigos e remove envelopes antigos. O exemplo autoral journeyExample foi preservado integralmente em seu arquivo anterior.

Executado: VersionedJourneyExampleTest com Compose real — edição Ana, detalhe/retorno mantém valor, restauração zera formulário e lê fonte via codec, falha de reload mantém definição, recuperação e sessão nova. build/t11/journey-final.log: sete módulos JVM, 281 casos (52 contrato/72 core/4 ações/95 autoria/7 rede/10 navegação/41 sample), zero falhas/erros/ignorados; Wasm sample compilado. Metadata/Wasm/Android navegação passaram no recorte codec. git diff --check aprovado.

Chrome/Wasm em http://127.0.0.1:8080/?journey: observado detalhe por link frio, restauração com fontes=0/restaurados=1, diálogo DEFINITION_UNAVAILABLE preservando formulário, recuperação com restaurados=2, troca para sessão 2 com fontes=1/restaurados=0 e métricas entradas=1/cargas=1/espera=1ms. Evidências locais ignoradas: build/t11/browser-reload-failure.png e browser-session-ready.png. Servidor de desenvolvimento em loopback; Studio na URL sem query continua como antes. Nenhuma publicação/push.

T11 concluída no escopo cliente sob demanda: definição/dados separados; identidade fixa; TTL/LRU; cold link; erro/retry/reload; volta/entradas repetidas/cancelamento (incluindo evidência T07); troca de sessão e snapshot incompatível/expirado rejeitado. Política de restauração: apenas definição validada, início novo, sem dados/pilha de processo; storage e confiança pertencem ao host. EAGER/PREFETCH não habilitadas. Rede real tem evidência JVM na T10, sem nova qualificação de rede browser/Android. T12 fica pronta; implantação, persistência remota e targets Apple permanecem fora deste fechamento.

### T12 — primeiro recorte: protocolo de capacidades registrado

Codex, 2026-10-24: NativeBridge/Request/Result/Handler em core/commonMain, registro copiado com nomes limitados, versão 1, input/output canônicos com orçamento e cancelamento no chamador. Sucesso, rejeição e indisponibilidade explícitos; falhas de programação propagadas. Nenhum acesso a plataforma/rede ou instalação no Studio/core por padrão.

Executado: build/t12/bridge.log — 74 testes core JVM (dois novos grupos de casos), metadata/Wasm/Android core, BUILD SUCCESSFUL. Cobertura nova: sucesso tipado, registro não alterado externamente, rejeição de argumentos/versão/orçamento, registro vazio, cancelamento/cleanup e defeitos/resultado inválido. Integração sample e probe Android em andamento; isto não conclui T12. Usuário confirmou emulador configurado; Small_API_35 iniciado para teste em modo read-only, sem janela e sem salvar snapshot; Pixel_3a_API_34 do usuário preservado.

### T12 — fechamento: bridge real no host Android e fallback browser

Codex, 2026-10-24: AndroidNativeBridge registra device.uptime por SystemClock.elapsedRealtime, sem permissão/Activity. NativeCapabilityExample usa action:method registrado, resultados tipados e Store próprio descartável; readOnStart é opt-in e ativado apenas no perfil Android nativeExample=true. A entrada padrão permanece App. Wasm ?native recebe registro vazio explícito. Surface/safeDrawingPadding e estilo de barras corrigem contraste/insets observados no Android.

Verificações: build/t12/final-platforms.log — sete módulos JVM, 286 casos (52/74/4/95/7/10/44), zero falhas/erros/ignorados, Wasm sample e APKs debug/instrumentação, BUILD SUCCESSFUL. Core metadata/Wasm/Android passou em bridge.log; host Android compilou e empacotou. Testes Compose adicionais validam sucesso tipado, fallback vazio, recriação limpando resultado/cancelando chamada pendente e onCreate explícito executando novamente no novo formulário. Handlers de fixture desses testes não são a prova nativa.

Prova Android real: APKs locais instalados somente na instância de teste Small_API_35 (API35, emulator-5556, read-only). build/t12/final-device.log: native.result=PASS; real-uptime/argument-rejection/unavailable/version-rejection; native.hostRecreation=PASS após Activity.recreate com dois onCreate e ao menos um onDestroy; INSTRUMENTATION_CODE=-1. O probe não usa JUnit e não está contado nos 286 casos JVM. Nenhuma dependência nova de runner foi adicionada. NativeBridgeInstrumentation é o runner próprio deste recorte; procedimento em validation.md.

Observação do APK final: Activity iniciada com nativeExample=true; onCreate passou pelo executor/adapter real e mostrou NATIVE_SUCCESS, uptime 1027567 ms, Surface legível e barras com contraste. Captura REA somente leitura em build/t12/android-verified/screen.png (720x1280, sha256 5112956e3b1eef05c98b59080a42b2809235be4f77aaaec2e44c4cdd553165d8). Chrome/Wasm ?native mostrou NATIVE_UNAVAILABLE ao clicar em Ler uptime; build/t12/browser-unavailable.png. Bundle final atualizado em final-browser-bundle.log. Screenshots/probes são artefatos locais ignorados, não versões de código.

T12 concluída para a capacidade local escolhida, com callback suspenso, dados limitados, registro explícito, ausência/negação/cancelamento e recriação demonstrados. Não representa suporte a outras APIs/permissões, hardware físico ou Apple. Não houve backend, credenciais, publicação ou push. A instância Pixel_3a_API_34 preexistente não recebeu instalações nem comandos de parada. Próxima etapa T20 pronta: aplicação/host HTTP local; persistência durável somente T21 e sem mudanças remotas implícitas.

Limpeza do recorte: a instância Small_API_35 de teste foi encerrada após confirmar sua identidade via ADB; sem salvar snapshot. Pixel_3a_API_34/emulator-5554 preservado. ADB não foi parado. O servidor loopback e as abas ?journey/?native permanecem disponíveis; não presumir serial de emulador ou sessão de terminal em uma nova retomada. Nenhum APK foi instalado na instância do usuário.

### T20 — aplicação de rascunhos e host HTTP local

Codex, 2026-10-24: humming-server-application (commonMain/target JVM bytecode17) contém DraftApplication, DTOs, DraftRepository, política/grants exatos por documento/tenant/operação, criação revisão0/leitura/validação com codec e compilador existentes. Sem Compose/Lifecycle/driver no classpath. humming-server-host (JVM25) usa HttpServer do JDK, autenticador Bearer injetado e adapter VolatileDraftRepository. Nenhuma dependência de cliente aponta para servidor; nenhum framework/dependência existente foi atualizado.

Executado: descoberta tasks dos dois módulos; aplicação tem cinco casos JVM, host cinco integrações com socket/HttpClient reais. Casos cobrem grants antes de parse/repositório, tenant/documento/permite read sem write, fonte/ID/revisão inválidos, diagnóstico localizado de compilação, create/read/validate, oito inserts concorrentes com um vencedor, volatilidade no restart, UTF-8/tamanho/tipo, 500 preservando causa, cancelamento no close e timeout504 com cleanup. Recuperação de stacktrace das coroutines envolve a causa original; a asserção foi corrigida para verificar a cadeia, sem converter defeito em payload. build/t20-final.log: nove módulos JVM, 296 casos (52 contrato/74 core/4 ações/95 autoria/7 rede/10 navegação/5 aplicação/5 host/44 sample), zero falhas/erros/ignorados, BUILD SUCCESSFUL.

Smoke independente: runLocal com credencial temporária só no ambiente, sem logs/arquivo de token. build/t20-localhost.log registrou host em 127.0.0.1:53145; Invoke-WebRequest real retornou 401 e UNAUTHENTICATED sem credencial. Socket exigiu execução fora do sandbox; nenhuma permissão de rede pública foi ampliada. Host do smoke foi encerrado. Porta efêmera não é configuração futura. git diff --check aprovado.

T20 concluída no recorte autorizado: aplicação/HTTP local e repository volátil explicitamente sem durabilidade. Timeout é cooperativo de suspensão, não garantia de deadline de IO bloqueante para host público. O contrato documenta limites/status/autenticação. Sem banco/GraphQL/federação, deploy, push ou credenciais versionadas. T21 pronta. Preparação: Docker Desktop já instalado estava parado; iniciado por CLI, engine29.9.1 Linux respondeu; download da imagem oficial PostgreSQL18.6 em andamento. Não declarar PostgreSQL validado antes de teste real.

### T21 — persistência PostgreSQL, revisão e recibos transacionais

Codex, 2026-10-24: novo humming-server-postgres, driver pgJDBC42.7.14 e migration V1 com chave composta tenant/documento, FK e recibo por commandId. Migration transacional/idempotente sob advisory lock e checksum normalizado LF. DraftEditingApplication autoriza READ+EDIT antes do parse, canoniza comando com budgets e chama RevisionedDraftRepository. Adapter aplica EditSession existente sob SELECT FOR UPDATE/READ COMMITTED e grava fonte/revisão/recibo na mesma transação; fingerprint diferencia retry de conflito. Cliente/core não dependem de servidor/driver. boundedCanonicalJson tornou-se público sem alterar o algoritmo/formato.

Host: /commands devolve revisão/appliedRevision/replayed e diagnósticos, ou conflito409/erro422. Composition root injeta PostgreSQL quando HUMMING_DB_URL/USER/PASSWORD estão todas presentes; sem elas continua explicitamente volátil (edição501), nunca fallback silencioso de configuração incompleta. JSON, UTF-8, grants e limites anteriores preservados. Não foram adicionadas rotas de publicação, exclusão, RLS de cliente ou colaboração.

Prova real: .\scripts\verify-postgres.ps1 criou banco local humming_test em imagem oficial postgres18.6 por digest sha256:edb0c08dc33befd0262ab3b5cf1c4f3f8cfd8b09e88c05b45adee29d6761ddb6, porta só em127.0.0.1 e credencial aleatória apenas em ambiente/Docker. Cinco testes JDBC e um HTTP (seis casos, zero falhas/erros/ignorados) qualificam migration idempotente, nova instância de adapter, oito revisões concorrentes com um vencedor, quatro retries do mesmo comando com uma aplicação, conflito de identidade, tenant separado, lote inválido, falha forçada entre UPDATE e recibo com rollback, cancelamento durante row lock real sem revisão/recibo, e host HTTP recriado lendo fonte/revisão/recibo. build/t21/integration.log, after-restart.log e final-integration.log: BUILD SUCCESSFUL. restart.log: sete registros antes/depois do reinício do banco, PASS. A primeira tentativa após restart detectou porta dinâmica alterada; script agora redescobre binding e verifica TCP antes do novo ciclo. Nenhum banco substituto foi usado.

Regressão: build/t21-regression.log — dez módulos JVM, 298 casos regulares (52 contrato/74 core/4 ações/95 autoria/7 rede/10 navegação/6 aplicação/5 host/1 adapter/44 sample), sem falhas/erros/ignorados; metadata/Wasm do modelo e Wasm sample compilados. Integrações reais são tarefas dedicadas e adicionais, não casos ignorados: total304 casos distintos. Os testes reais foram repetidos após restart e após os ajustes finais de cancelamento/cleanup. git diff --check aprovado. Não há nova prova UI/Android/Apple neste recorte.

Fixtures Docker criadas pelo script foram paradas no finally; containers e volumes rotulados T21 preservados sem apagar dados. build/t21/environment.json é apenas snapshot sem segredo (último container humming-t21-714dd5db2ec1, volume de mesmo prefixo com sufixo -data); consultar nome/label e redescobrir porta ao reiniciar. Não versionar nem imprimir Config.Env/credenciais. Docker Desktop permaneceu disponível; nada remoto foi provisionado. T21 concluída no escopo local. T22 pronta para snapshot/publicação local de teste; nenhuma autorização de deploy/push está implícita.

## Fechamento T22 — publicação imutável e cliente fixado

- Codex, 2026-10-24. PublishedContracts/Codec, PublicationRequirements e PublishedDefinition no modelo puro; aplicação/perfis/digest injetado no servidor; migration V2 para snapshots/referência ativa e trigger de imutabilidade. Rotas autenticadas locais publicam, negociam e selecionam versão. Rascunho/recibo e V1 preservados.
- Manifesto mínimo inclui definições com versões exatas, métodos em slots, providers de navegação gerados e tokens. Codec fechado/canônico; fonte/versão verificadas no servidor. Cliente confere manifesto/identidade/capacidades e mantém strings fixadas sem compartilhar nós mutáveis. Dados resolvidos de sessão/formulário não são serializados.
- BUILD SUCCESSFUL: build/t22-regression.log, dez jvmTest + metadata comum/Wasm do modelo e Wasm do sample; build/t22-application-final.log confirma a última revisão do teste. XML: contrato 52, core 74, ações 4, modelo 100, rede 7, navegação 10, aplicação 7, host 5, PostgreSQL 1, sample 44 = **304 casos regulares**, zero falhas/erros/ignorados.
- .\scripts\verify-postgres.ps1 -PublishedClient executou **8 integrações** (PostgreSQL 6, HTTP 1, Compose cliente 1) antes e depois do restart: BUILD SUCCESSFUL em build/t21/integration.log e after-restart.log. build/t21/restart.log: 9 registros antes/depois. Fixture própria humming-t21-73b734ec95bc ficou parada, volume preservado; imagem oficial PostgreSQL 18.6 pelo digest já registrado. Não há segredo nos metadados locais.
- Provas: duas versões distintas; primeira intacta; repetição determinística; revisão obsoleta rejeitada também dentro do adapter; UPDATE/DELETE rejeitados pelo banco; versão inexistente não troca seleção; rollback e novo adapter preservam rascunho/revisão. HTTP rejeitou envelope extra/tipo inválido e cliente incompatível. Compose JVM real carregou via HTTP, digitou private-session-value, manteve título da primeira versão após ativar segunda e confirmou ausência do valor pessoal no rascunho/snapshots.
- Limites: integração UI automatizada em JVM, sem nova inspeção visual manual; Wasm compilado, Android/Apple não requalificados neste recorte. PublishedDefinition usa provider legado adaptado e não instala transporte/SDNavigation automaticamente. Hash não é assinatura; host continua loopback, sem TLS/deploy. Studio remoto e BFF ficam nas etapas seguintes. Retomar em T23 conforme tabela superior.

## Fechamento T23 — dados por fonte registrada

- Codex, 2026-10-25. PublicationDataApplication no módulo de aplicação: planos/fontes registrados, seleção tipada limitada de campos, publicação compatível fixada, autorização por fonte e falha parcial. Cache TTL/LRU por tenant/documento/versão/actor/sessão/plano/mapeamento, autorização antes de hit, invalidação com geração, limite global de concorrência e cancelamento do chamador.
- RegisteredHttpDataSource no host: endpoint e GET fixos, identidade do host em headers próprios sem encaminhar Authorization, redirects recusados, await cancelável, limite durante leitura, JSON/UTF-8 estritos. Rota de dados opt-in separa values/issues de qualquer snapshot; sessão só vem do adapter confiável. runLocal não instala fontes automaticamente.
- build/t23-policy-http.log e build/t23-regression.log: BUILD SUCCESSFUL. A revisão final foi executada em build/t23-final.log: aplicação 12, host 8 e sample 44 casos regulares, além das 9 integrações reais (PostgreSQL 6, HTTP 1, cliente Compose 2), zero falhas/erros/ignorados. Com os demais relatórios da T22 preservados, conjunto de **312 JVM regulares + 9 integrações = 321 casos distintos**. Não houve nova alteração do runtime comum ou de versões de dependências.
- 5 testes novos de política cobriram isolamento de tenant/documento/versão/actor/sessão/plano, revogação antes de hit, falha parcial/recuperação sem cachear erro, fonte ausente, JSON/tipo inválido, campos privados excluídos, TTL/invalidação/carga tardia, timeout/cancelamento/semaphore e defeito de programação propagado. 3 testes novos de HTTP real cobriram headers/redirect, streaming limitado/MIME/UTF-8 e timeout/cancelamento.
- O serviço HTTP independente respondeu 503; endpoint de dados devolveu issue e Compose mostrou Data unavailable. Após recuperação, nova tentativa real criou entrada com ScreenInputs.server e exibiu Real local service. Campo não selecionado ficou fora da resposta, e banco/snapshot permaneceram iguais sem dado resolvido. Primeira execução revelou apenas cleanup de Lifecycle INITIALIZED no novo teste; corrigido para owner criado/resumido e suíte repetida com sucesso.
- Docker Desktop estava desligado na primeira tentativa desta data; foi iniciado localmente. .\scripts\verify-postgres.ps1 -PublishedClient passou antes/depois de restart: 10 registros preservados, build/t23-integration-run.log e build/t21/{integration,after-restart,restart}.log. Fixture humming-t21-f8966c7f62d5 e volume próprio preservados/parados; revisão final reutilizou somente essa fixture, redescobrindo porta e sem imprimir segredo.
- Limites: servidor e renderer JVM locais; sem nova QA visual manual ou qualificação Android/browser/Apple. Recuperação usa nova entrada, sem refresh automático de inputs em entrada existente. Cache em memória por processo, sem persistência pessoal/distribuição/coalescência. Templates e scripts não foram adicionados. A integração do Studio com outbox/reconexão permanece T24, próxima etapa.

## Evidência histórica T24 — primeiro recorte, outbox e recuperação

- Codex, 2026-10-25. RemoteEditor puro, checkpoint fechado por identidade/base/comandos/pausa/fonte em conflito; save-before-send, ticket único, replay estável e confirmações ordenadas. LocalWorkspace roteia comandos do Inspector/drag à outbox quando conectado; LocalStudio mostra confirmado/pendente/pausa e exporta recuperação sem fingir storage confirmado. Importar/abrir/novo/undo local ficam desabilitados nesse modo.
- build/t24-outbox-final.log: BUILD SUCCESSFUL; 106 testes do modelo (6 novos), 45 sample (1 novo), metadata comum/Wasm do modelo e Wasm do sample. Testes cobrem confirmação antiga versus edição nova, reinício com recibo perdido, duas versões de documento em conflito, autorização, checkpoint inválido/identidade e propriedade de comandos. Nenhum teste novo de UI é apresentado como QA manual.
- .\scripts\verify-postgres.ps1 -PublishedClient: BUILD SUCCESSFUL antes/depois de restart, 11 registros preservados; build/t24-integration-run.log e build/t21/{integration,after-restart,restart}.log. 10 integrações reais no conjunto: PostgreSQL 6, HTTP 1, sample 3. O terceiro caso usa arquivo real com move atômico, novo host HTTP, reenvio idempotente e disputa com outro editor. Primeira confirmação perdida não duplicou revisão; 409 manteve edição local/pendência e cópia remota. Publicação anterior intacta. Fixture humming-t21-36a704e4951a e volume ficaram preservados/parados.
- Recorte parcial: protocolo/attach do workspace estão disponíveis, mas o host/browser padrão ainda não conecta API/storage nem oferece flush/retry de rede. Não concluir T24/T25 nem chamar o Studio padrão de editor remoto completo. Próximo passo é controller de transporte/storage e entrada de conexão explícita, com integração de UI real. Restore após troca de objetos não é prova de queda de energia ou processo independente.

## Fechamento T24 — controller e entrada desktop explícita

- Codex, 2026-10-25. RemoteStudioHost coordena load/restore, storage antes de envio e depois de cada ack, flush/retry limitado, cancelamento e arquivo obrigatório de pendências antes da troca remota. LocalStudio auto-salva recuperação após revisão local, mostra falha real de storage e fornece Sincronizar pendências/Arquivar e usar remoto com confirmação explícita. HTTP/recuperação são adapters do host, sem dependência servidor no cliente comum.
- JvmRemoteStudioApi limita resposta/timeout, propaga cancelamento, não segue redirects e mantém bearer apenas em memória. JvmRemoteStudioRecovery usa arquivo explícito, UTF-8/limites, force e move atômico, além de arquivo de conflito separado. DesktopRemoteStudio é selecionado somente por --remote-studio e seis variáveis de processo, descritas em docs/studio.md. run/--args foram descobertos em build/t24-compose-run-help.log, sem iniciar publicação/distribuição.
- build/t24-controller-regression.log: BUILD SUCCESSFUL, 48 casos sample (3 novos de controller/storage), Wasm sample compilado. Modelo conserva 106 casos/metadata/Wasm da revisão final em build/t24-outbox-final.log. Relatórios totais: contrato 52, core 74, ações 4, modelo 106, rede 7, navegação 10, aplicação 12, host 8, PostgreSQL 1, sample 48 = **322 JVM regulares**; sem falhas/erros/ignorados.
- build/t24-controller-integration.log executou PostgreSQL 6 e HTTP 1 com sucesso. O novo fluxo UI inicialmente falhou no matcher do teste, que contava simultaneamente badge e mensagem OFFLINE; restringido ao testTag do status. build/t24-controller-integration-final.log: BUILD SUCCESSFUL, 4 casos sample reais, zero falhas/erros/ignorados. Total de **11 integrações** e **333 casos distintos** no conjunto. Fixture própria humming-t21-36a704e4951a reutilizada, porta redescoberta, credenciais só em memória e container parado no finally.
- Prova pelo Inspector real: aplicar UI saved, clicar sincronizar e ler revisão 1 no PostgreSQL; parar host, editar UI offline, gravar pendência/OFFLINE; criar outro host/controller com arquivo existente e sincronizar revisão 2. Outro editor alterou para revisão 3; UI conservou UI conflict ao receber 409. Confirmação Arquivar e substituir criou arquivo separado contendo a pendência e abriu other editor. Bearer não apareceu no checkpoint/arquivo. Token incorreto resultou UNAUTHORIZED sem nova revisão. Snapshot publicado original permaneceu igual.
- Limites: UI automatizada Compose/JVM, sem inspeção manual da janela do composition root. HTTP/storage concretos são JVM loopback; browser padrão continua local e Wasm teve somente compilação do controller/Studio comum. Sem qualificação remota Android/Apple, queda de energia ou restauração de processo cliente independente. Não há sync multiusuário/presença/undo remoto; T25 é a próxima etapa, com política própria para coordenação entre instâncias.

## Fechamento T25 — colaboração local — 2026-10-25

Responsável Codex. Journal imutável e compensações persistidos na mesma transação que revisão/recibo; PostgreSQL é a autoridade compartilhada. Feed paginado de patches, deduplicação, lacunas/ressincronização e presença por actor com TTL. Acompanhar editores é opt-in; polling de 2s não envia pendências automaticamente. Undo exige autoria e postimagem intacta de todos os nós/arestas afetados; conflito conserva intenção recuperável. Checkpoint v2 migra v1 e inclui undo pendente.

Comandos e relatórios: build/t25-complete-regression.log (dez jvmTest, metadata e Wasm do modelo, Wasm do sample), BUILD SUCCESSFUL: contrato 52, core 74, ações 4, autoria 110, rede 7, navegação 10, aplicação servidor 13, host 8, PostgreSQL 1 e sample 48 = 327. build/t25-collaborative-ui-final.log: postgresIntegrationTest 8, postgresHttpIntegrationTest 2, publishedClientIntegrationTest 5 = 15 integrações reais. Todos sem falhas/erros/ignorados. Fixture PostgreSQL 18.6 local, HTTP e arquivos reais; containers de teste parados ao terminar. Restart com 16 registros preservados em build/t25-integration-run.log.

Teste Compose com dois controllers/dois hosts comprovou convergência, presença, undo preservando edição em outro nó, recusa quando o mesmo nó mudou e recuperação após perder ack do undo, sem revisão duplicada. Testes puros incluem feed duplicado/fora de ordem/lacuna e proteção de pendências. Codec compartilhado corrige discriminador polimórfico operationType; rejeita versão ausente/futura. Não são processos JVM separados, teste de carga, CRDT ou entrega exatamente uma vez. Sem inspeção manual nova de janela nativa. Undo da UI abrange o último envio próprio reconhecido nesta conexão; sem redo remoto. Journal anterior à migração exige ressincronização por snapshot. Publicações permanecem imutáveis.

## Qualificação T26 — matriz do recorte local

Recorte: POC local, com runtime/Studio compartilhado e integração remota opt-in exclusivamente JVM loopback. Não é release distribuída nem suporte universal aos targets declarados. Os fechamentos anteriores conservam a prova visual de sua revisão; não são reexecuções nesta data.

| Target / funcionalidade | Evidência atual e alcance | Limite / próximo gate |
| --- | --- | --- |
| JVM: contrato/runtime/modelo/servidor/sample | 327 testes regulares; metadata e Wasm compartilhado no log t25-complete-regression | Janela nativa do modo remoto não foi inspecionada manualmente |
| JVM: PostgreSQL, HTTP, publicação, dados, edição e colaboração | 15 integrações reais; dois controllers e dois hosts; recuperação em arquivo e restart do banco | Instâncias no mesmo JVM; sem carga ou teste de processos cliente independentes |
| Wasm: Studio local | Fluxos visuais anteriores T16–T19 e compilação atual do Studio comum | HTTP/storage/colaboração remotos não instalados no browser; não qualificados |
| Android: runtime e bridge nativa | Probe real API35 e recriação registrados em T12; assembleDebug atual gerou APK | Nenhuma execução visual nova do APK; editor remoto tem adapter somente JVM |
| Apple | Targets declarados nos builds | Não qualificado: Windows não fornece host/toolchain Apple |
| GitHub CI | YAML e estrutura conferidos; tarefas reproduzidas localmente | Não executado no GitHub; nenhum push feito |

Avisos observados: namespaces Android compartilhados entre bibliotecas e avisos Kotlin preexistentes (Elvis redundante/!!). Não causaram falha no assembleDebug; manutenção de namespaces deve preservar consumidores e manifestos e merece recorte próprio. Nenhuma versão de dependência foi alterada para contornar esses avisos.

Fechamento T26 em 2026-10-25, responsável Codex: .\gradlew.bat :sample:composeApp:tasks --all (build/t26-tasks.log) descobriu as tarefas; .\gradlew.bat :sample:composeApp:assembleDebug :sample:composeApp:wasmJsBrowserDistribution --console=plain (build/t26-platform-build.log) terminou BUILD SUCCESSFUL em 5m34s, 217 tarefas. Artefatos locais: sample/composeApp/build/outputs/apk/debug/composeApp-debug.apk e sample/composeApp/build/dist/wasmJs/productionExecutable/index.html com JS/Wasm. Não instalados/publicados neste recorte. Webpack avisou sobre tamanho do bundle; Gradle reportou deprecações incompatíveis com Gradle 10. Não houve atualização de dependências.

CI consolidada em build-release.yml: Windows/JDK25/wrapper, dez jvmTest e metadata/Wasm; dois workflows obsoletos de distribuição removidos. YAML analisado com PyYAML/BaseLoader, permissões read e estrutura conferidas. Links locais revisados sem alvos ausentes; git diff --check passou. Configuração CI não foi executada remotamente. Não restam tarefas obrigatórias de implementação no plano local atual. Gates fora desse recorte: execução manual do composition root remoto, processos cliente independentes/carga, CI remota, Apple e eventual adapter remoto browser/Android. Abrir um desses escopos exige registrar tarefa/aceite e reabrir a qualificação; não reiniciar T00–T25.

## Fechamento T27 — catálogo Material 3 — 2026-10-25

Inspeção inicial: 19 handlers layout, somente 9 schemas; compose.material3 resolve 1.8.2 (build/material3-inventory.log). Entrada material3=1.13.0-alpha02 do catálogo não é utilizada por compose.material3. Preservar wrappers textButton/outlinedButton/iconButton legados; expor equivalentes Material reais com nomes explícitos, sem migração visual silenciosa.

Resultado: 111 handlers layout / 108 schemas (117 definições no catálogo inicial completo). As três extensões image/animatedVisibility/style conservam dispatch legado fora da autoria. Auditoria dos sources resolvidos: 100 nomes públicos composables top-level, todas as 98 famílias visuais cobertas; MaterialTheme/ProvideTextStyle são configuração, e ExposedDropdownMenu é membro adicional coberto. Não se promete paridade de todos os overloads/parâmetros Kotlin. ExpressiveNavigationBar é interno nessa versão e foi excluído após inspeção, sem acesso por reflexão. Inventário e política em material3.md, indexado no README da documentação.

Schemas puros e adapters separados em controles, estrutura, overlays e seletores/gestos. Seleção sem chave explícita preserva estado automático, coberto por teste de rádio. Estado no owner antes do evento; callbacks combinados em sequência única; seleção/receivers verdadeiros, menu ancorado, data UTC, hora controlada e FAB desabilitado acessível. Adicionado Exemplo Material 3 ao Studio; importação/compilação do documento e execução de Salvar testadas. Wrappers textButton/outlinedButton/iconButton preservados; variantes Material exatas com prefixo material nos três nomes. Scaffold recebe slots snackbarHost/floatingActionButton.

Evidência: build/material3-api-audit.log (scripts/audit-material3.py contra sources 1.8.2: nenhuma família visual ausente); build/material3-final-regression.log, BUILD SUCCESSFUL, dez jvmTest e metadata/Wasm/Android Material + Wasm sample. build/material3-final-platform.log, BUILD SUCCESSFUL após revisão final: sample 60 casos, metadata/Android/Wasm do módulo e Wasm do sample. Relatórios atuais: contrato 54, core 74, ações 4, modelo 110, rede 7, navegação 10, aplicação 13, host 8, PostgreSQL 1, sample 60 = 341 regulares; sem falhas/erros/ignorados. São 2 casos novos de contrato, 11 de UI e 1 de compatibilidade/compilação do exemplo. Asserções cobrem receivers, estados, ações, remount/descartes, UTC/hora externa, menu exposto, intervalo e disabled.

.\scripts\verify-postgres.ps1 -PublishedClient: build/material3-postgres.log e build/t21/{integration,after-restart,restart}.log, BUILD SUCCESSFUL antes/depois do restart, 17 registros preservados. 8 PostgreSQL + 2 HTTP + 5 publicação/controller/Compose = 15 integrações; zero falhas/erros/ignorados; fixture própria humming-t21-4bebf631f175 parada no finally. Total 356 casos distintos. Nenhum segredo versionado ou publicação/push.

Captura sample/composeApp/build/reports/material3-gallery.png inspecionada: card, campo Ana, checkbox marcado, switch, slider/progresso, chips, abas e Salvo legíveis em Surface Material. Teste automatizado JVM, sem alegar nova inspeção manual browser/Android/Apple. O smoke monta os 92 adapters, mas não equivale a inspeção visual de cada popup aberto em cada plataforma. Fontes não atualizadas; avisos preexistentes de Kotlin/Skiko/deprecações permanecem. Uma falha incremental Wasm recuperou por fallback de recompilação; builds finais passaram sem alteração de dependências. Falhas intermediárias de matcher de data/hora foram corrigidas usando semântica da API, e a retenção real de TextFieldValue do TimeInput recebeu correção com teste externo. Links locais e diff revisados. Próximo gate é QA de runtime dos componentes novos nos demais targets, não reconstruir o catálogo.
