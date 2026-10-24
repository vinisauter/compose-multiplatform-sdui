# Estado atual

Este é o ponto de entrada para **continuar o desenvolvimento**. Reúne o estado observado, o rastreamento das etapas e a próxima ação. O [plano](development-plan.md) define dependências, escopo e critérios de aceite; este documento é a única fonte de status e retomada.

Atualizado em 2026-10-23 após T01. O core possui 30 testes de caracterização aprovados em JVM; o sample conserva seu teste de integração aprovado em T00, com imagem inspecionada. A janela nativa foi iniciada em T00, mas não foi inspecionada pela ferramenta de automação, que falhou ao inicializar. Demais plataformas e fluxos seguem sem qualificação. Atualize os fatos e o rastreamento ao concluir cada recorte.

## Ponto de retomada

| Campo | Registro atual |
| --- | --- |
| Última atualização | 2026-10-23 — T01 sobre HEAD `855acce`; alterações locais ainda sem commit |
| Última entrega | T01: commonTest configurado, fixtures e 30 testes de caracterização aprovados; evidência em [validação](validation.md) |
| Tarefa de implementação em execução | T04 — Codex: bindings frios, ownership de composição e eventos de estado |
| Próxima tarefa | **T04 — lifecycle, loading e bindings legados** |
| Próxima ação concreta | Migrar bindings do catálogo para assinaturas da composição e tarefas UI para scopes canceláveis; definir eventos sem realimentação causal |
| Critério para avançar | Cumprir os casos válidos/inválidos e de limite de T02, com diagnóstico e política de fallback explícitos; depois iniciar T03 |
| Bloqueio de implementação conhecido | Nenhum para T02. Inspeção manual desktop e demais targets permanecem pendentes de qualificação |
| Limite do próximo recorte | Parsing, resolução e diagnóstico; execução estruturada e lifecycle ficam em T03–T04. Sem servidor/editor/publicação |

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
| T04 | Lifecycle, loading e bindings legados | em execução | Bindings com coleta por composição, cancelamento de ações UI, loading e origem causal de eventos; legado imperativo terá descarte explícito |
| T05 | Definições/schema do catálogo inicial | pendente | Compartilhar metadados/parser com preservação do legado |
| T06 | Fronteira pura e consumo sem UI | pendente | Separar modelo/codec; extrair módulo somente se necessário |
| T07 | Valores tipados e escopos de tela/ação | pendente | Isolar dados de tela e resultados concorrentes |
| T08 | Linguagem de expressões limitada | pendente | Especificar gramática/tipos/limites e executar suíte pura |
| T09 | Propriedades reativas, tokens e validação | pendente | Integrar evaluator com lifecycle e componentes |
| T10 | Transporte HTTP com ownership e resultado | pendente | Implementar métodos reais e integração HTTP local |
| T11 | Jornada, navegação e cache versionado | pendente | Isolar entradas, fixar snapshot e definir políticas |
| T12 | Bridge de capacidades nativas | pendente | Capacidade real no host e não suporte explícito nos demais |
| T13 | Documento autoral e importação/exportação | pendente | Implementar modelo normalizado e round trip legado |
| T14 | Comandos, chaves de ordem e undo local | pendente | Implementar mutações atômicas, inversas e testes de ordem |
| T15 | Compilador puro do subconjunto legado | pendente | Produzir contrato determinístico e executá-lo no cliente |
| T16 | Preview do catálogo no host Wasm | pendente | Provar browser e renderer com efeitos controlados |
| T17 | Editor local e Inspector | pendente | Criar/editar/desfazer/salvar/exportar sem servidor |
| T18 | Seleção, bounds e drop geométrico | pendente | Validar Row/Column, transformações e commit por revisão |
| T19 | Grafo de interações e conectores | pendente | Portas/curvas/ações compiláveis e navegação observada |
| T20 | Aplicação de servidor e interfaces | pendente | Criar fronteiras/casos de uso/host local com autorização |
| T21 | Rascunhos/revisões em PostgreSQL | pendente | Validar persistência/transações em banco real local |
| T22 | Publicação imutável e capacidades | pendente | Publicar snapshot local compatível sem dados de sessão |
| T23 | Resolução BFF e isolamento de dados | pendente | Resolver fontes registradas com limites e isolamento |
| T24 | Edição remota com revisão e recuperação | pendente | Salvar/reabrir/reconectar sem perda silenciosa |
| T25 | Múltiplos editores e conflitos | pendente | Testar concorrência, replay e undo entre processos |
| T26 | Qualificação por plataforma e recorte | pendente | Iniciar após T00; anexar evidência de cada recorte anunciado |

Os requisitos completos e a ordem das dependências estão no [plano](development-plan.md). A numeração identifica tarefas; não significa executar T07–T12 antes de T13–T19. A prioridade inicial é base → schemas/fronteira → autoria local → manipulação visual; capacidades adicionais do cliente podem avançar em paralelo quando elegíveis.

### Trabalho parcial e bloqueios

Nenhuma tarefa está em execução após a entrega de T01. Ao iniciar uma, registrar aqui somente o que a tabela não comportar, neste formato, substituindo o registro ao avançar:

```text
Etapa / responsável / data / revisão:
Implementado neste recorte:
Verificado (comandos, resultado, plataforma, evidência):
Pendente para cumprir o aceite:
Bloqueio reproduzido e causa observada:
Próxima ação executável:
```

Após concluir, conservar o vínculo da evidência na tabela e retirar daqui o registro transitório. Não duplicar o checklist completo da tarefa.

### Manutenção documental independente da implementação

| Item | Estado | Evidência / próxima ação |
| --- | --- | --- |
| M01 — excluir artefato JSON obsoleto restante | concluída | Em T00, a consulta ao caminho anteriormente bloqueado retornou ausente. A limpeza física restante foi verificada; não foi necessário encerrar processos ou repetir a exclusão nesta execução. |

Os relatórios e planos antigos estão ausentes. O registro de M01 conserva o resultado da verificação e não altera a próxima etapa funcional.

## Escopo implementado

HummingUI é uma POC de cliente SDUI. Interpreta nós JSON, mantém estado textual, resolve componentes e ações por namespace e integra bibliotecas no sample. Não há editor visual, backend de contratos, publicação imutável ou colaboração implementados nos módulos incluídos.

| Módulo | Responsabilidade observada |
| --- | --- |
| `humming-core` | Modelo de nó, parsing/helpers, registro, ViewModel, estado e despacho |
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
| Nó e parsing | [ServerDrivenNode.kt](../humming-core/src/commonMain/kotlin/me/developes/humming/sdui/common/ServerDrivenNode.kt) e [ServerDrivenJson.kt](../humming-core/src/commonMain/kotlin/me/developes/humming/sdui/common/ServerDrivenJson.kt) |
| Registro | [SDLibrary.kt](../humming-core/src/commonMain/kotlin/me/developes/humming/sdui/common/SDLibrary.kt) e [NodeProvider.kt](../humming-core/src/commonMain/kotlin/me/developes/humming/sdui/common/NodeProvider.kt) |
| Execução/estado | [SDUIFormViewModel.kt](../humming-core/src/commonMain/kotlin/me/developes/humming/sdui/common/SDUIFormViewModel.kt) e [SDAction.kt](../humming-actions/src/commonMain/kotlin/me/developes/humming/sdui/action/SDAction.kt) |
| Navegação | [SDNavigation.kt](../humming-navigation/src/commonMain/kotlin/me/developes/humming/sdui/navigation/SDNavigation.kt) e [Graph.kt](../humming-navigation/src/commonMain/kotlin/me/developes/humming/sdui/navigation/components/Graph.kt) |
| Componentes | [SDLayoutMaterial3.kt](../humming-layout-material3/src/commonMain/kotlin/me/developes/humming/sdui/layout/SDLayoutMaterial3.kt) e os arquivos `SDC*` de seu catálogo |
| Rede experimental | [SDNetwork.kt](../humming-network/src/commonMain/kotlin/me/developes/humming/sdui/action/SDNetwork.kt) |
| Payloads do sample | [app-navigation.json](../sample/composeApp/src/commonMain/composeResources/files/navigation/app-navigation.json) e [login.json](../sample/composeApp/src/commonMain/composeResources/files/layout/login.json) |

O fluxo básico é host → provider → parsing → resolução de biblioteca/componente → composição → evento → despacho de ações → atualização de estado ou navegação. O core contém dependências visuais, Lifecycle e Navigation; também há helpers de `Color` no arquivo de JSON. Ele ainda não é um módulo de contrato independente de UI.

## Limitações que exigem trabalho

| Área | Comportamento observado | Consequência para a implementação |
| --- | --- | --- |
| Parsing | `component` é exigido com acesso não nulo; propriedades são convertidas para texto; JSON null vira a string `"null"` | Caracterizar fixtures antes de mudar validação/coerção |
| Serialização | O wrapper de JSON pode retornar o JSON original em `toString()` depois de mutações no nó | Exportação do editor precisa de codec explícito e teste de round trip |
| Identidade | ID ausente é gerado durante parsing | Importação autoral precisa persistir identidade estável |
| Registro | Namespace inválido/desconhecido pode falhar fora do tratamento esperado | Diagnóstico deve conter nó, caminho e resolução tentada |
| Ações | `invokeActions` lança coroutine; condições e sequências aninhadas podem retornar antes dos efeitos | Separar executor suspenso de ponto de entrada da UI |
| Falha/cancelamento | Capturas amplas podem consumir cancelamento; callbacks podem impedir decremento de loading em caminhos sem `finally` | Testar falha, callbacks e cancelamento; `loadComponent(type,destiny)` já possui `finally` |
| Estado/bindings | Strings no ViewModel; leitura pode inicializar chave ausente; collectors vivem no escopo do ViewModel | Definir ownership e descarte; evitar collectors repetidos a cada composição |
| Eventos | `onStateChange` reage à emissão inicial e pode realimentar atualizações | Definir semântica de disparo e proteção contra ciclos |
| Navegação | Cache em memória por rota; estado compartilhado no ViewModel do grafo; deep link pressupõe nó disponível | Definir carregamento, erros, isolamento por entrada e restauração |
| Rede | Esboço cria `HttpClient` por chamada, `post` chama `get` e resposta não integra estado; provider URL do sample também cria cliente por chamada | Introduzir cliente injetado, ownership e resultado explícito |
| Exemplos | Há `event:loader`, `navigation:goToUrl`, providers `link`/`firestore`, recurso ausente e `action:method` com propriedade `method` | Selecionar/corrigir uma fixture local; não assumir que todos os JSONs são funcionais |

Essas limitações descrevem o código, não comportamentos que devam ser perpetuados. O [contrato](contract.md) distingue compatibilidade de defeitos a corrigir.

## Build e evidência disponível

Os builds principais declaram toolchain JVM **25** e compatibilidade Java Android **17**. O [catálogo](../gradle/libs.versions.toml) declara Kotlin `2.4.21`, Compose `1.8.2`, AGP `8.13.2`, Ktor `3.6.0` e Navigation `2.9.2`; aliases JVM/Spring usam Kotlin `2.2.10`. Esses valores não demonstram resolução dos artefatos ou compatibilidade entre versões.

O [build do core](../humming-core/build.gradle.kts) configura `commonTest` com kotlin-test e coroutines-test; coroutines-test foi retirado de produção, onde não havia uso. [LegacyParsingTest](../humming-core/src/commonTest/kotlin/me/developes/humming/sdui/common/LegacyParsingTest.kt), [LegacyResolutionTest](../humming-core/src/commonTest/kotlin/me/developes/humming/sdui/common/LegacyResolutionTest.kt) e [LegacyStateTest](../humming-core/src/commonTest/kotlin/me/developes/humming/sdui/common/LegacyStateTest.kt) executaram 30 casos em JVM. Providers e handlers de fixture testam fronteiras do core, sem comprovar rede ou UI reais. O sample possui um teste Compose aprovado em T00, mantido UP-TO-DATE em T01.

A dependência Lifecycle do core permanece `api`, pois seu ViewModel aparece na API pública. O host JVM fornece handle de estado em memória por factory explícita; os outros hosts mantêm o caminho padrão. T01 não alterou o runtime: comportamentos frágeis encontrados ficam mapeados para correção/migração nas tarefas posteriores.

Gradle 9.5.1 com Java 25.0.3 em Windows 11 x64 executou `projects`, descoberta de tarefas e compilação JVM. O SDK Android foi encontrado, com plataformas instaladas até `android-37.0`; nenhum build Android foi executado neste recorte. Workflows em [.github](../.github) foram auditados: ainda referenciam `androidApp`, paths Apple na raiz, JDK 17 e Gradle 7.2 em um workflow. Não foram alterados ou acionados nesta entrega; corrigir/qualificar em T26 antes de uso. Problemas de configuração devem ser reproduzidos antes de alterar versões, source sets ou CI.

## Evolução ainda não implementada

Schemas de componentes, valores tipados, linguagem de expressões, isolamento por entrada de back stack, transporte completo, políticas de cache, bridge nativa, modelo autoral normalizado, compilador, Studio, persistência, publicação e colaboração são entregas futuras. O [plano](development-plan.md) define recortes que podem ser validados separadamente.
