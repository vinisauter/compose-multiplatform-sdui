# Estado atual

Este é o ponto de entrada para **continuar o desenvolvimento**. Reúne o estado observado, o rastreamento das etapas e a próxima ação. O [plano](development-plan.md) define dependências, escopo e critérios de aceite; este documento é a única fonte de status e retomada.

Atualizado após T19: autoria local, drop e grafo de interações integrados. Última execução: 94 testes autorais e 30 sample aprovados (124 casos, zero falhas/erros/ignorados); metadata/JS e host Wasm compilados. Chrome confirmou navegação/retorno, curva selecionada e download completo. Demais módulos não foram reexecutados neste recorte.

## Ponto de retomada

| Campo | Registro atual |
| --- | --- |
| Última atualização | 2026-10-24 — base e05985b (T13), anteriores c6a1e46 e 6441ec0; T14 no commit local bde7c23 e T15 em eb8726e e T16 em b6c1ff3 e implementação T17 em 2d9d6eb, aceite ce2bd7e e geometria T18 em 8a12ea0 e integração em 04df7c0 e orientação de tela vazia em 75f50f6; sem push |
| Última entrega | T19: geometria 28a3140, relações ca81ddc, jornadas d57e219 e canvas/qualificação neste recorte; commits locais sem push |
| Tarefa de implementação em execução | Nenhuma; T19 concluída |
| Próxima tarefa | **T07 — valores tipados e escopos de tela/ação** |
| Próxima ação concreta | Ler T07 e execução/navegação atuais; definir valores ausente/null/tipos e lifetime de server/nav/state por entrada e response/error por execução; preservar adapter textual e adicionar fixtures de isolamento antes da integração |
| Critério para avançar | Duas entradas da mesma rota isoladas; resultados concorrentes independentes; descarte/retorno liberam recursos; compatibilidade textual e distinção null/ausente testadas |
| Bloqueio de implementação conhecido | Nenhum para iniciar T07; qualificação Android/Apple continua separada de JVM/Wasm |
| Limite do próximo recorte | Modelo/escopos locais do cliente; sem backend, infraestrutura remota ou publicação |

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
| T07 | Valores tipados e escopos de tela/ação | pronta | Próxima etapa após autoria/manipulação local; definir stores e integrar com fixtures de isolamento |
| T08 | Linguagem de expressões limitada | pendente | Especificar gramática/tipos/limites e executar suíte pura |
| T09 | Propriedades reativas, tokens e validação | pendente | Integrar evaluator com lifecycle e componentes |
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

T19 concluída no recorte local. Comandos finais: .\gradlew.bat :humming-studio-model:jvmTest :humming-studio-model:compileCommonMainKotlinMetadata :humming-studio-model:compileKotlinJs :sample:composeApp:jvmTest :sample:composeApp:compileKotlinWasmJs --console=plain (build/t19/final-checks.log). BUILD SUCCESSFUL; 94 testes autorais + 30 sample, zero falhas/erros/ignorados. Captura Compose build/reports/interaction-canvas.png no sample: botão interno → borda da tela e curva rosa; teste cobre zoom e Escape. Projeção de interações limita visitas/arestas, trata compartilhamento/ramos sem seguir ciclos de tela. Política de autoconexão visual explícita e testada; navegação de botão para sua própria tela permanece válida.

No Chrome/Wasm: exemplo abriu segunda tela e voltou; canvas desenhou a conexão, clique na curva selecionou navigation:goTo e pintou rosa. Captura final build/t19/browser-final-connector.png confirma rótulos, árvore sem duplicação e Inspector com destino derivado da relação; download real humming-journey.json contém graph.json e 2 screens/*.json. O teste StudioJourneyTest executou o pacote exportado após decode pelo mesmo host. Na revisão foram corrigidos glifos de direção, duplicação de ação raiz na árvore e edição redundante de destiny quando há relação. O teste antigo de inserção passou após rolagem explícita para acomodar os campos de posição. Verificação adicional após preservar viewport entre revisões: build/t19/viewport-check.log, 30 testes sample e Wasm aprovados. Logs de tentativas e correções em build/t19, ignorados pelo Git. Sem nova prova Android/Apple, roteamento de obstáculos, drag para criar conexões ou servidor.

Após a orientação de carregar o exemplo, o usuário respondeu “agora sim”, confirmando a guia que faltava no aceite de T18. Essa é evidência manual, distinta da observação automatizada do commit no Chrome e da captura de pixels em Compose; detalhes e limites estão em [validação](validation.md). Não repetir a verificação sem motivo novo.

Retomar por T07, dependências concluídas. T20–T25 permanecem posteriores; este recorte não criou servidor nem persistência remota.

### Manutenção documental independente da implementação

| Item | Estado | Evidência / próxima ação |
| --- | --- | --- |
| M01 — excluir artefato JSON obsoleto restante | concluída | Em T00, a consulta ao caminho anteriormente bloqueado retornou ausente. A limpeza física restante foi verificada; não foi necessário encerrar processos ou repetir a exclusão nesta execução. |

Os relatórios e planos antigos estão ausentes. O registro de M01 conserva o resultado da verificação e não altera a próxima etapa funcional.

## Escopo implementado

HummingUI é uma POC de cliente SDUI. Interpreta nós JSON, mantém estado textual, resolve componentes e ações por namespace e integra bibliotecas no sample. O editor local integra árvore/Inspector, seleção/drop, canvas de interações e preview/exportação de jornadas. Não há backend de contratos, publicação imutável ou colaboração implementados nos módulos incluídos.

| Módulo | Responsabilidade observada |
| --- | --- |
| `humming-contract` | Modelo, codec, diagnósticos e schemas puros; catálogo inicial sem handlers |
| `humming-studio-model` | Documento autoral normalizado, comandos, histórico, compilador por capacidades e codecs; não depende do runtime |
| `humming-core` | Registro, ViewModel, estado, despacho e helpers visuais; exporta o contrato |
| `humming-actions` | Métodos registrados, eventos, atualização e condições |
| `humming-navigation` | Grafo e ações com Compose Navigation; cache de nós por rota em memória |
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
| Estado/bindings | Catálogo usa bindings frios; APIs imperativas oferecem releaseBinding; leitura legada pode inicializar chave vazia | T07/T11: tipos e isolamento por entrada; extensões imperativas precisam descartar seus bindings |
| Eventos | Emissão inicial preservada; idempotência e origem causal impedem ciclos no catálogo | Métodos customizados devem usar updateStateFromAction; revisões são confladas |
| Navegação | Cache em memória por rota; estado compartilhado no ViewModel do grafo; deep link pressupõe nó disponível | Definir carregamento, erros, isolamento por entrada e restauração |
| Rede | Esboço cria `HttpClient` por chamada, `post` chama `get` e resposta não integra estado; provider URL do sample também cria cliente por chamada | Introduzir cliente injetado, ownership e resultado explícito |
| Exemplos | Há `event:loader`, `navigation:goToUrl`, providers `link`/`firestore`, recurso ausente e `action:method` com propriedade `method` | Selecionar/corrigir uma fixture local; não assumir que todos os JSONs são funcionais |

Essas limitações descrevem o código, não comportamentos que devam ser perpetuados. O [contrato](contract.md) distingue compatibilidade de defeitos a corrigir.

## Build e evidência disponível

Os builds principais declaram toolchain JVM **25** e compatibilidade Java Android **17**. O [catálogo](../gradle/libs.versions.toml) declara Kotlin `2.4.21`, Compose `1.8.2`, AGP `8.13.2`, Ktor `3.6.0` e Navigation `2.9.2`; aliases JVM/Spring usam Kotlin `2.2.10`. Esses valores não demonstram resolução dos artefatos ou compatibilidade entre versões.

A suíte atual tem 201 casos aprovados em JVM, distribuídos entre contrato puro, core, ações e sample. As fixtures originais de parsing/limites foram movidas para commonTest do contrato; regressões de runtime e schemas permanecem nos consumidores. O teste headless comprova ausência de classes UI no classpath. A execução detalhada e os resultados históricos estão em [validação](validation.md).

A dependência Lifecycle do core permanece api porque seu ViewModel aparece na API pública. O host JVM fornece handle de estado em memória por factory explícita; os outros hosts mantêm o caminho padrão. O contrato é exportado pelo core e usa bytecode JVM 17 com toolchain 25.

Gradle 9.5.1 com Java 25.0.3 em Windows 11 x64 executou `projects`, descoberta de tarefas e compilação JVM. O SDK Android foi encontrado, com plataformas instaladas até `android-37.0`; o core Android compilou após T06, sem execução em dispositivo ou qualificação dos demais módulos Android. Workflows em [.github](../.github) foram auditados: ainda referenciam `androidApp`, paths Apple na raiz, JDK 17 e Gradle 7.2 em um workflow. Não foram alterados ou acionados nesta entrega; corrigir/qualificar em T26 antes de uso. Problemas de configuração devem ser reproduzidos antes de alterar versões, source sets ou CI.

## Evolução ainda não implementada

Schemas para o restante do catálogo, valores tipados, linguagem de expressões, isolamento por entrada de back stack, transporte completo, políticas de cache, bridge nativa, grafo visual de interações, persistência, publicação e colaboração são entregas futuras. O [plano](development-plan.md) define recortes que podem ser validados separadamente.
