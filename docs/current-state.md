# Estado atual

Este é o ponto de entrada para **continuar o desenvolvimento**. Reúne o estado observado, o rastreamento das etapas e a próxima ação. O [plano](development-plan.md) define dependências, escopo e critérios de aceite; este documento é a única fonte de status e retomada.

Atualizado após T08: linguagem pura opt-in implementada sobre os valores/escopos da T07. Suíte JVM: 43 contrato + 69 core + 4 ações + 94 autoria + 32 sample = 242 casos, zero falhas/erros/ignorados. Expressões compiladas para metadata/JS/Wasm; integração reativa nas propriedades ainda pertence à T09. T07 também foi compilada para Android e observada no Chrome.

## Ponto de retomada

| Campo | Registro atual |
| --- | --- |
| Última atualização | 2026-10-24 — T19 em f85c566, T07 em 43527b7; T08 com código e evidência abaixo; commits locais sem push |
| Última entrega | T09 parcial: dependências (3e6faad), envelope versionado e layout:reactiveText com texto/visibilidade, autoria e execução Compose JVM |
| Tarefa de implementação em execução | T09 — Codex; primeiro recorte: dependências e binding tipado com assinatura pertencente ao consumidor |
| Próxima tarefa | **T09 — propriedades reativas, tokens e validação** |
| Próxima ação concreta | Estender o recorte opt-in para habilitação e validação do formulário; definir tokens e máscaras por capacidade; qualificar no browser |
| Critério para avançar | Mudar estado atualiza propriedades dependentes sem collectors duplicados; descarte cancela assinaturas; UI explicita erros de expressão/validação; dado canônico separado da apresentação e tokens/máscaras com capacidades declaradas |
| Bloqueio de implementação conhecido | Nenhum para T09. Expressões testadas em JVM e compiladas JS/Wasm; execução browser da linguagem e Apple permanecem qualificação própria |
| Limite do próximo recorte | Integração reativa por capacidade do cliente; sem substituir bindings legados, transporte HTTP, backend ou publicação |

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
| T09 | Propriedades reativas, tokens e validação | em execução | Codex: dependências e binding tipado primeiro; formato/capacidade opt-in, apresentação, tokens/máscaras, validação e UI permanecem no escopo da etapa |
| T10 | Transporte HTTP com ownership e resultado | pendente | Implementar métodos reais e integração HTTP local |
| T11 | Jornada, navegação e cache versionado | pendente | Isolar entradas, fixar snapshot e definir políticas |
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
| `humming-network` | Esboço no disco, **fora** dos includes de Gradle |

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
| Rede experimental | [SDNetwork.kt](../humming-network/src/commonMain/kotlin/me/developes/humming/sdui/action/SDNetwork.kt) |
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
| Rede | Esboço cria `HttpClient` por chamada, `post` chama `get` e resposta não integra estado; provider URL do sample também cria cliente por chamada | Introduzir cliente injetado, ownership e resultado explícito |
| Exemplos | Há `event:loader`, `navigation:goToUrl`, providers `link`/`firestore`, recurso ausente e `action:method` com propriedade `method` | Selecionar/corrigir uma fixture local; não assumir que todos os JSONs são funcionais |

Essas limitações descrevem o código, não comportamentos que devam ser perpetuados. O [contrato](contract.md) distingue compatibilidade de defeitos a corrigir.

## Build e evidência disponível

Os builds principais declaram toolchain JVM **25** e compatibilidade Java Android **17**. O [catálogo](../gradle/libs.versions.toml) declara Kotlin `2.4.21`, Compose `1.8.2`, AGP `8.13.2`, Ktor `3.6.0` e Navigation `2.9.2`; aliases JVM/Spring usam Kotlin `2.2.10`. Esses valores não demonstram resolução dos artefatos ou compatibilidade entre versões.

A referência agregada mais recente é 242 testes JVM após T08; o primeiro recorte T09 acrescenta seis casos, com 46 do contrato e 72 do core aprovados novamente. Ações/autoria/sample mantêm os resultados anteriores. As fixtures de parsing/limites pertencem ao commonTest do contrato; o teste headless comprova ausência de classes UI no classpath. Evidências incrementais estão nos recortes acima; procedimentos em [validação](validation.md).

A dependência Lifecycle do core permanece api porque seu ViewModel aparece na API pública. O host JVM fornece handle de estado em memória por factory explícita; os outros hosts mantêm o caminho padrão. O contrato é exportado pelo core e usa bytecode JVM 17 com toolchain 25.

Gradle 9.5.1 com Java 25.0.3 em Windows 11 x64 executou `projects`, descoberta de tarefas e compilação JVM. O SDK Android foi encontrado, com plataformas instaladas até `android-37.0`; o core Android compilou após T06, sem execução em dispositivo ou qualificação dos demais módulos Android. Workflows em [.github](../.github) foram auditados: ainda referenciam `androidApp`, paths Apple na raiz, JDK 17 e Gradle 7.2 em um workflow. Não foram alterados ou acionados nesta entrega; corrigir/qualificar em T26 antes de uso. Problemas de configuração devem ser reproduzidos antes de alterar versões, source sets ou CI.

## Evolução ainda não implementada

Permanecem pendentes a integração visual completa de expressões/tokens/validação da T09, schemas para o restante do catálogo, transporte remoto, políticas de cache, bridge nativa, persistência remota, publicação e colaboração. Valores tipados, linguagem pura, isolamento opt-in e grafo visual local já possuem evidência nos recortes anteriores. O [plano](development-plan.md) define o escopo restante.

### T09 — envelope e texto/visibilidade no catálogo

Codex, 2026-10-24: ReactiveProperty com envelope 1 estrito, fallback Text/Boolean, validação de sintaxe/versionamento e diagnóstico localizado. layout:reactiveText schema 1 é uma capacidade nova; os 15 schemas legados permanecem versão 1. Inspector edita os envelopes como JSON; o botão “Exemplo de expressões” importa uma fixture neutra com campo legado e mensagem reativa. O componente acompanha o estado, oculta/reexibe conteúdo e mostra fallback/diagnóstico recuperável em falha de tipo.

Executado no Windows/JVM: 49 testes contrato, 72 core, 94 autoria e 34 sample aprovados (ações não reexecutadas neste recorte). JS/Wasm do contrato e Wasm do sample compilados. Logs build/t09/visual-final.log e visual-image.log. Três novos testes de contrato e dois Compose cobrem envelope/tipos/versão/offset, importação → compilação → edição → ocultar/reexibir, falha/recuperação e assinaturas únicas/zero após descarte. Imagem build/t09/reactive-form-jvm.png renderizada e inspecionada com fundo branco no host de teste. As primeiras tentativas detectaram expectativa antiga do tamanho do catálogo e classificação do envelope legado; ambas corrigidas e reexecutadas. Sem execução browser/Android/Apple neste recorte.

T09 permanece parcial: habilitação reativa, validação de campo, tokens/máscaras por capacidade e qualificação browser ainda precisam ser entregues. Não promover T10.
