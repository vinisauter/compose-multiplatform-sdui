# Validação e evidências

Este documento define como verificar e registrar uma entrega. O [plano de desenvolvimento](development-plan.md) define tarefas/dependências/aceite. O [estado atual](current-state.md) é a única fonte de status, evidência por etapa e ponto de retomada; o [contrato](contract.md) define compatibilidade. Não marque uma tarefa como concluída apenas por criar código ou documentação.

Situação inicial em **2026-10-23**: esta reorganização foi documental. Nenhum comando Gradle, teste ou fluxo visual foi executado por ela. Os comandos abaixo são procedimentos a confirmar, não resultados obtidos.

## Mudanças somente documentais

1. Revise conteúdo e terminologia contra o código e os builds citados. Diferencie comportamento existente, proposta e resultado executado.
2. Confira os destinos dos links locais, nomes de arquivos, caminhos de código e referências entre documentos. Um link existente não comprova a afirmação a que ele dá suporte.
3. Revise `git diff` e `git diff --check`; para arquivos novos ainda não rastreados, revise também seu conteúdo completo, pois eles não aparecem no diff comum. Preserve alterações preexistentes.
4. Informe os arquivos alterados, as verificações documentais e as pendências. Não execute build apenas para validar texto.

Atualize o documento responsável pelo assunto, conforme o [índice](README.md), sem criar um segundo plano ou repetir checklists de implementação aqui.

## Antes de alterar comportamento

Leia [AGENTS.md](../AGENTS.md), a tarefa e suas dependências, o contrato e as [decisões](decisions.md). Inspecione o estado do Git, o fluxo afetado, os payloads e os builds. Defina o comportamento esperado e o limite de compatibilidade antes da edição.

Escolha um fluxo do sample com recursos e providers disponíveis. A existência de um JSON não garante que seus nomes, destinos ou handlers estejam registrados. Registre quais partes serão observadas e quais ficam fora do recorte.

Consulte [settings.gradle.kts](../settings.gradle.kts), o build de cada módulo afetado e o [catálogo de versões](../gradle/libs.versions.toml). `humming-network` está fora dos includes; não invoque tarefas desse módulo como se ele já estivesse integrado. Toolchain JVM 25 e compatibilidade Java Android 17 são configurações distintas. Investigue uma falha antes de alterar versões ou plataformas.

### Descoberta pelo wrapper

Execute no PowerShell, na raiz do repositório, quando a tarefa exigir validação de runtime:

```powershell
.\gradlew.bat --version
.\gradlew.bat projects
.\gradlew.bat :humming-core:tasks --all
.\gradlew.bat :sample:composeApp:tasks --all
```

Repita a consulta `:modulo:tasks --all` para outros módulos efetivamente afetados e incluídos. Registre a saída relevante, o exit code e a configuração usada. Se a própria configuração Gradle falhar, registre esse bloqueio antes de alegar disponibilidade de qualquer tarefa.

Depois da descoberta, selecione as tarefas reais de compilação, teste e execução. Os nomes abaixo são candidatos encontrados na orientação do projeto; **a disponibilidade não foi confirmada**:

| Finalidade | Tarefa candidata |
| --- | --- |
| Compilar o core para JVM | `:humming-core:compileKotlinJvm` |
| Abrir o sample desktop | `:sample:composeApp:run` |
| Gerar o sample Android de depuração | `:sample:composeApp:assembleDebug` |
| Executar o sample no browser JS | `:sample:composeApp:jsBrowserDevelopmentRun` |
| Executar o sample no browser Wasm | `:sample:composeApp:wasmJsBrowserDevelopmentRun` |

Não presuma que `check`, `allTests` ou uma tarefa de agregação execute todos os targets. Identifique as tarefas e os relatórios de testes realmente produzidos. Uma tarefa com zero testes não comprova os cenários esperados. `run` pode permanecer ativo; abrir a janela não encerra a observação do fluxo.

## Testes e fixtures compartilhadas

Antes de escrever testes, confirme os source sets, as dependências e o runner. T01 habilitou `commonTest` do core com kotlin-test e coroutines-test, validado pela descoberta de tarefas e pelos XMLs de 30 testes em JVM. Não assuma execução em outros targets apenas por criar código compartilhado.

Coloque comportamento compartilhável e seus testes no source set apropriado. `commonTest` deve cobrir lógica portável; integração com engines, navegação nativa, recursos e UI exige testes ou observação na plataforma correspondente. Rodar os testes compartilhados em JVM é um primeiro resultado, não prova de execução em todos os targets.

Use as fases abaixo conforme o impacto da tarefa. Elas orientam a verificação e não substituem os critérios de aceite do plano.

| Fase | Evidência esperada |
| --- | --- |
| Caracterização | Fixtures do comportamento existente, com resultado ou diagnóstico esperado antes da mudança; defeitos identificados separadamente dos contratos preservados |
| Unidade compartilhada | Casos válidos, inválidos e de limite executados para parser, resolução, estado ou algoritmo alterado |
| Assincronismo | Ordem observável dos eventos, cancelamento durante suspensão, falha de callbacks/providers e finalização de loading/recursos; tempo virtual quando apropriado |
| Integração local | Fronteiras reais do recorte, como cliente HTTP contra servidor local controlado ou persistência contra PostgreSQL real |
| Fluxo visual | Passos reproduzíveis no sample ou editor, plataforma identificada, resultado observado e limitações |
| Compatibilidade | Fixtures antigas e novas verificadas; migração, alias ou rejeição explícita para o que não tiver equivalente |

Cada fixture deve ter entrada, objetivo e resultado esperado. Reutilize os payloads representativos do sample, mas crie casos mínimos para isolar falhas. Inclua apenas cenários pertinentes à mudança:

- Parsing e codec: objeto/array em properties, null e ausência, ID explícito/gerado, campo obrigatório ausente e serialização depois de editar o nó.
- Resolução: identificador malformado, namespace desconhecido, handler ausente e nomes case-sensitive. O diagnóstico deve indicar nó, caminho e operação.
- Eventos e ações: conteúdo visual separado de eventos, sequência aninhada, erro tratado/não tratado, cancelamento e descarte do owner.
- Estado e providers: bindings legados, emissão inicial, atualização observável, isolamento de telas/execuções, resposta inválida e falha durante carregamento.
- Navegação e transporte: destinos legados, retorno, tipos do corpo/resposta, erro HTTP, falha de transporte e request cancelado, sem dados de uma execução vazarem para outra.

Compare round trips pela semântica contratada, incluindo identidade e ordem relevantes; igualdade textual do JSON não é um requisito geral. Quando uma correção mudar comportamento defeituoso, registre o resultado novo e seu impacto em vez de congelar o defeito como compatibilidade obrigatória.

Fakes, fixtures e transporte controlado permitem testar falhas e concorrência. Identifique-os no relatório. Eles não comprovam integração HTTP, persistência, efeito nativo ou execução visual reais. Não use serviços de produção ou dados pessoais como fixtures.

## Evidência por entrega

| Entrega | Evidência mínima adequada ao recorte |
| --- | --- |
| Cliente SDUI | Testes do contrato e comportamento alterados; fluxo local no sample com renderização, input/evento e resultado observados; falha/cancelamento quando afetados |
| Domínio autoral e compilador | Testes sem UI para IDs/parentesco, ordem, comandos/inversas, importação/exportação semântica, compilação determinística e diagnósticos; entrada permanece inalterada |
| Editor local | Fluxo observado de criar/selecionar/editar/desfazer/exportar/reproduzir; testes de geometria para recursos implementados; preview com contexto de execução declarado |
| Servidor e publicação | Integração local de revisão, transação, isolamento por tenant, snapshot imutável e rejeição de capacidades incompatíveis; PostgreSQL real para garantias de persistência; fontes HTTP locais para agregação |
| Colaboração | Dois clientes, revisão concorrente, comando duplicado, desconexão e recuperação; queda entre commit e broadcast; sequência persistida como autoridade e política de undo declarada |

Um preview simulado comprova a interação de autoria e o que seu executor controlado implementa. Para afirmar execução pelo cliente, exporte o contrato e observe o player no target escolhido. Reutilizar o renderer não prova igualdade de fontes, inputs, recursos ou capacidades entre plataformas.

Build local, testes, observação de UI, publicação de snapshot e deploy são resultados distintos. Nenhum comando deste procedimento autoriza distribuição, infraestrutura ou uso de credenciais externas. Métricas de desempenho exigem cenário, volume de nós/expressões, ambiente, método de medição e resultado; percepção visual isolada não comprova uma meta numérica.

## Matriz inicial de plataformas

Matriz atualizada com T00/T01 em 2026-10-23. Targets declarados indicam configuração a investigar, não suporte funcional completo. Acrescente data e referência à evidência quando um recorte for executado; mantenha seu status de implementação no estado atual.

| Alvo | Situação conhecida por leitura | Compilação | Testes | Fluxo observado |
| --- | --- | --- | --- | --- |
| Cliente JVM/desktop | T00/T01; subconjunto caracterizado | Core/sample aprovados | 30 casos do core executados em T01; 1 caso Compose de T00 preservado UP-TO-DATE | Renderer do teste inspecionado em T00; sem nova inspeção visual em T01 |
| Cliente Android | Target e sample declarados; exige SDK e dispositivo/emulador compatíveis | Não validada | Não validados | Não validado |
| Cliente browser JS | Target e sample declarados; tarefas/dependências a confirmar | Não validada | Não validados | Não validado |
| Cliente browser Wasm | Target e sample declarados; tarefas/dependências a confirmar | Não validada | Não validados | Não validado |
| Cliente iOS | Targets e integração declarados; exige ambiente Apple compatível | Não validada | Não validados | Não validado |
| Biblioteca macOS | Targets no core; não equivalem a um sample nativo macOS observado | Não validada | Não validados | Não validado |
| Studio Wasm | Host planejado; ainda não implementado | Não validada | Não validados | Não validado |
| Servidor local | Camada planejada; ainda não implementada | Não validada | Não validados | Não validado |
| Colaboração | Capacidade planejada; ainda não implementada | Não validada | Não validados | Não validado |

Registre host/SO, JDK, versões relevantes, target, dispositivo/browser e modo de execução. Uma indisponibilidade de ambiente deixa a respectiva linha sem validação; não a converta em aprovação nem atribua automaticamente a falha ao código. A evidência deve identificar o comando que falhou, a causa observada e o que continuou verificável.

## Registro de uma tarefa

Vincule o relatório à etapa no estado atual e aos critérios da tarefa no plano. Guarde logs e artefatos úteis em local identificável, sem segredos ou caminhos locais de SDK em arquivos versionados. Inclua revisão/commit quando houver; imagens e gravações ajudam a reproduzir UI, mas não substituem os testes de comportamento.

```text
Tarefa e recorte:
Revisão/data/ambiente:
Mudanças e contratos afetados:
Decisões e compatibilidade:
Comandos executados, exit codes e relatórios:
Fixtures/casos executados e resultados:
Plataforma e passos do fluxo observado:
Limitações, bloqueios e verificações não executadas:
Critérios atendidos e referência da evidência no estado atual:
```

Declare um teste aprovado somente após sua execução e confira o relatório correspondente. Quando não houver execução, escreva “não executado” e explique o alcance; uma proposta de comando continua sendo procedimento pendente.

## Registro da revisão documental — 2026-10-23

Recorte: consolidação do índice, contratos, arquitetura, mecânicas, decisões, etapas de implementação e retomada. Base Git `855acce`, com alterações locais preexistentes preservadas; sem commit criado nesta revisão.

- Revisão estática dos modelos, handlers, ViewModel, catálogo, navegação, sample e builds citados. Remoção de documentos duplicados e relatórios antigos, com a exceção física M01 registrada no estado atual.
- Auditoria local aprovada dos dez documentos de entrada/projeto: 86 links locais com destinos/capitalização corretos, descoberta de todos os documentos a partir de `AGENTS.md`/README, fences e whitespace válidos, ausência de referências documentais obsoletas e correspondência das 27 tarefas com o rastreamento.
- Verificação do grafo de dependências: IDs definidos, sem ciclos, com detalhe e critério de aceite por etapa. Status concentrados no estado atual; plano sem cópia concorrente de status.
- Revisão cruzada da geometria: vizinhos do slot completo para chaves, revisão de layout/viewport antes do commit e distância ao segmento no flatten de curvas. Um script de referência verificou cinco vetores e 5.000 inserções determinísticas de chaves, preservando validade e ordem. São verificações da especificação, sem implementação ou teste Kotlin nesta entrega.
- `git diff --check` e inspeção do conjunto de arquivos: sem mudanças em código Kotlin, builds ou catálogo. Arquivos novos também revisados fora do diff comum.

Ao concluir aquela revisão documental, Gradle, testes, sample e demais targets ainda não haviam sido executados; a próxima tarefa era T00. A execução posterior de T00 está registrada abaixo. A revisão documental isolada não qualifica nenhuma plataforma.

## T00 — base JVM executável — 2026-10-23

Base `855acce` com alterações locais preservadas, sem commit novo. Ambiente observado: Windows 11 x64, Java JetBrains 25.0.3, Gradle wrapper 9.5.1 e plugin Kotlin 2.4.21 declarado. SDK Android presente, plataformas de android-33 a android-37.0 e build-tools até 36.0.0; Android não foi compilado. Nenhuma versão foi alterada.

### Comandos e resultados

| Comando | Resultado |
| --- | --- |
| `java -version` | Java 25.0.3 |
| `.\gradlew.bat --version` | Inicialmente bloqueado pelo sandbox no lock do cache; passou com acesso ao cache do usuário |
| `.\gradlew.bat projects --console=plain` | Exit 0; includes confirmados |
| `.\gradlew.bat :humming-core:tasks :sample:composeApp:tasks --all --console=plain` | Exit 0; tarefas JVM/test/browser descobertas |
| `.\gradlew.bat :humming-core:compileKotlinJvm :sample:composeApp:compileKotlinJvm --console=plain` | Inicialmente exit 1 no sample; após correções, exit 0 |
| `.\gradlew.bat :sample:composeApp:run --args=--local-smoke --console=plain` | Primeira execução falhou por owner ausente no SavedStateHandle; após correção, permaneceu em execução sem repetir a exceção, até interrupção deliberada. Não houve término natural com exit 0 |
| `.\gradlew.bat :sample:composeApp:jvmTest --console=plain` | Execução final exit 0; XML confirmou 1 teste, 0 falhas/erros e 0 ignorados |

Correções: Lifecycle exposto como dependência `api` do core; imports Compose do sample; factory opcional de SavedStateHandle no Content, fornecida pelo host JVM; modo local de entrada; dependências de teste JVM e teste de integração. JSON e nomes de handlers/bindings existentes foram preservados. O teste inicialmente precisou de owner explícito no seu próprio host Compose; adicionou-se `ViewModelStoreOwner` de teste com cleanup ao final, sem alterar o runtime para ocultar esse requisito.

### Fluxo e artefatos

O teste [LocalSmokeTest.kt](../sample/composeApp/src/jvmTest/kotlin/sample/app/LocalSmokeTest.kt) usa o App, provider de recursos, parser, catálogo Material 3 e ação registrados reais:

1. Carrega [local-smoke.json](../sample/composeApp/src/commonMain/composeResources/files/layout/local-smoke.json).
2. Aguarda `Nome informado: Visitante`.
3. Substitui o campo por `Pessoa de teste` e verifica a atualização do binding.
4. Aciona `Confirmar` e verifica `Status: confirmado`.
5. Verifica uma única criação de handle durante o fluxo e gera PNG; o conteúdo renderizado foi inspecionado visualmente.

Artefatos locais gerados, ignorados pelo Git e recriáveis pelo comando de teste:

- Relatório JUnit: `sample/composeApp/build/reports/tests/jvmTest/index.html`.
- Resultado XML: `sample/composeApp/build/test-results/jvmTest/TEST-sample.app.LocalSmokeTest.xml`.
- Imagem do renderer de teste: `sample/composeApp/build/reports/local-smoke.png`.
- Logs de descoberta, compilação, execução e teste em `build/t00/`.

Os artefatos de build podem desaparecer com limpeza; este resumo conserva resultados e procedimento. Não publicar logs brutos com caminhos do SDK ou credenciais.

### Alcance e limites

T00 foi aceita no recorte JVM com compilação, execução do processo, teste de integração do sample e inspeção visual da imagem renderizada. Não foi uma inspeção manual da janela nativa: a ferramenta computer-use falhou ao iniciar o kernel (`failed to write kernel assets`, erro de caminho 3), repetindo após reset. Não se usou automação alternativa do sistema para contornar a falha. A janela nativa e o fluxo padrão com recursos remotos permanecem sem qualificação manual em T26.

Não executados: testes de parsing/registro/estado do core (T01), browser, Android, Apple, servidor, distribuição ou publicação. O `SavedStateHandle` do desktop é em memória; não prova persistência do formulário. Warnings de source sets não usados, `!!` desnecessário e acesso nativo do JDK permanecem registrados nos logs, sem impedir o recorte aprovado.

Auditoria de workflows: referências a módulos `androidApp` ausentes, caminhos Apple incompatíveis com a árvore atual e configuração antiga de JDK/Gradle. Nenhum workflow foi acionado ou corrigido naquele recorte local; sua atualização faz parte da qualificação T26. Ao concluir T00, a próxima implementação era T01, registrada abaixo.

## T01 — caracterização do core — 2026-10-23

Recorte concluído em JVM, sobre a mesma base `855acce` e alterações locais preservadas. Nenhum arquivo de runtime Kotlin foi alterado em T01. O build do core passou a configurar `commonTest`; a dependência coroutines-test, sem imports de produção, foi movida de `commonMain` para `commonTest`. Não houve troca de versões ou de contrato JSON.

### Procedimento e resultados

| Comando | Resultado observado |
| --- | --- |
| `.\gradlew.bat :humming-core:tasks --all --console=plain` | Exit 0; `jvmTest` e `compileTestKotlinJvm` confirmadas |
| `.\gradlew.bat :humming-core:jvmTest :sample:composeApp:jvmTest --console=plain` | Primeira tentativa encontrou inferência de tipo em uma asserção nova; após corrigir o teste, exit 0. Core executado; sample UP-TO-DATE |
| `.\gradlew.bat :humming-core:dependencyInsight --configuration jvmRuntimeClasspath --dependency kotlinx-coroutines-test --console=plain` | Exit 0; nenhuma dependência correspondente no runtime de produção |
| `.\gradlew.bat :humming-core:dependencyInsight --configuration jvmTestRuntimeClasspath --dependency kotlinx-coroutines-test --console=plain` | Exit 0; coroutines-test 1.11.0 disponível para testes |

Relatórios XML confirmaram **30 testes executados, 0 falhas, 0 erros e 0 ignorados**:

| Suíte | Casos | Cobertura |
| --- | --- | --- |
| [LegacyParsingTest](../humming-contract/src/commonTest/kotlin/me/developes/humming/sdui/common/LegacyParsingTest.kt) | 12 | Identidade explícita/gerada, nomes case-sensitive, ordem, propriedades textuais/nested, slots, eventos objeto/array, JSON inválido, round trips e limitações do codec |
| [LegacyResolutionTest](../humming-core/src/commonTest/kotlin/me/developes/humming/sdui/common/LegacyResolutionTest.kt) | 10 | Namespaces, handlers, providers suspensos, destino repassado, falhas, sequência direta e identificadores malformados |
| [LegacyStateTest](../humming-core/src/commonTest/kotlin/me/developes/humming/sdui/common/LegacyStateTest.kt) | 8 | Inicialização, edição/remoção, vazio versus ausência, múltiplos placeholders, atualização de flows e isolamento entre instâncias de ViewModel |

[LegacyFixtures](../humming-contract/src/commonTest/kotlin/me/developes/humming/sdui/common/LegacyFixtures.kt) concentra um payload com IDs estáveis, eventos e conteúdo visual separados. [ViewModelTestSupport](../humming-core/src/commonTest/kotlin/me/developes/humming/sdui/common/ViewModelTestSupport.kt) instala dispatcher Main de teste, mantém ownership dos ViewModels e limpa stores/jobs antes de restaurar o dispatcher. Os handlers/providers de teste são doubles das extensões do core; não comprovam backend, catálogo Material 3 ou plataforma nativa.

O caso Compose do sample continua aprovado pelo resultado anterior de T00. O Gradle o considerou UP-TO-DATE nesta execução; não houve nova execução nem nova inspeção da imagem nesta etapa. Logs de descoberta, tentativa inicial, execução corrigida e classpaths ficam em `build/t01/`; XMLs em `humming-core/build/test-results/jvmTest/` e HTML em `humming-core/build/reports/tests/jvmTest/index.html`. São artefatos locais recriáveis, ignorados pelo Git.

### Limitações caracterizadas e trabalho posterior

Testes com prefixo `observedLegacy` capturam comportamento observado, não uma garantia de comportamento desejado. Ao corrigir/migrar a área, substituir a expectativa pela semântica escolhida e acrescentar a regressão correspondente; não manter o defeito apenas para deixar a suíte verde.

| Caso observado | Próxima decisão/correção |
| --- | --- |
| JSON null vira texto `"null"` no parser manual | T02/T07: definir validação/compatibilidade e representação tipada; não mudar coerção silenciosamente |
| Array de eventos ignora elementos que não são objetos | T02: diagnóstico por elemento/caminho e política de fallback/migração |
| `toString()` conserva JSON original após editar propriedades | T13/T15: codec explícito de exportação e round trip após edição; não usar o wrapper como serializer mutável |
| Identificador sem separador escapa ao tratamento do componente | T02: resolver com diagnóstico estruturado e nó/caminho |
| Segmento adicional em `test:text:extra` é ignorado | T02: definir validação completa de identificadores e impacto sobre aceitação legada |
| Binding ausente cria chave vazia durante leitura | T04/T07: explicitar efeito de leitura e ownership; preservar ou migrar conscientemente |

O teste de sequência comprova somente handlers **diretos**; ramos aninhados, falha intermediária, callbacks que falham, cancelamento e loading ainda exigem T03/T04. Isolamento entre instâncias de ViewModel não comprova isolamento entre rotas do mesmo grafo. Bindings continuam textuais e de uma passagem; uma linguagem recursiva/tipada não foi implementada.

Não executados: testes desses source sets em Android, Apple, JS ou Wasm; nenhuma nova UI nativa, rede real ou publicação. A próxima etapa é T02; status e próxima ação permanecem exclusivamente no estado atual.

## T02 — diagnósticos e entrada limitada — 2026-10-23

Implementados `NodeDiagnostic`, `PayloadException`, `PayloadLimits` e `parseNode`. O sample usa a entrada textual limitada em seus providers. Diagnósticos incluem código, severidade, operação, caminho, nó quando conhecido e documento opcional. `ErrorData` conserva causa e distingue payload, provider e handler; falhas não viram sucesso silencioso. Fallback padrão: rejeitar nó/evento inválido, retornar null no despacho e expor erro ao host. Erros de programação do tipo Error propagam; Exception do handler continua reportada como falha de handler, com causa preservada.

Limites padrão: 2.000.000 caracteres UTF-16 de entrada textual, profundidade JSON 64, 10.000 objetos com chave component, 100.000 valores JSON e 65.536 caracteres por propriedade/string. A pré-checagem de profundidade ignora delimitadores dentro de strings; orçamento da árvore é verificado antes da materialização. Dados de negócio com chave component também contam conservadoramente no orçamento. Entradas JsonObject já construídas usam os limites da árvore; para limitar o texto antes do parser, usar parseNode.

Migração: nomes precisam de exatamente dois segmentos não vazios sem espaços. Identificadores antes truncados ou sem namespace são inválidos. Eventos com elementos não objeto deixam de ser ignorados e rejeitam o evento com caminho do elemento. Objeto único/array válido, ordem, IDs e propriedades textuais permanecem aceitos; JSON null continua texto no legado. O helper de slot único rejeita múltiplos nós. Limitações de exportação do wrapper continuam para T13/T15.

Executados, exit 0: compilação core/sample JVM e `.\gradlew.bat :humming-core:jvmTest :sample:composeApp:jvmTest --console=plain`. XMLs: 49 casos core e 1 Compose, sem falhas/erros/ignorados. Os testes incluem limites de entrada/profundidade/nós/valores/propriedades, strings com delimitadores escapados, tipos inválidos, caminho de filhos/eventos, provider cancelado, preservação de causa, handler com Error e erro de componente não sobrescrito por fallback de provider. Logs em `build/t02/`. Outros targets não foram executados; nenhuma publicação.

## T03 — execução estruturada — 2026-10-23

`executeActions` é suspenso, retorna tentativas/erros e herda política/orçamento nas chamadas aninhadas. CONTINUE é o padrão legado; STOP_ON_ERROR é seleção explícita do host, ainda sem nova sintaxe JSON. Limites padrão: 10.000 tentativas e profundidade 64; excedê-los interrompe mesmo CONTINUE. Contexto por execução e contadores protegidos por Mutex mantêm resultados/políticas separados em execuções concorrentes. Os handlers customizados devem aguardar `executeActions`; usar `invokeActions` dentro de handler continua sendo um lançamento independente, fora dessa garantia.

`invokeActions` e `runTask` continuam entradas de lançamento pelo ViewModel e agora retornam Job. Cancelamento propaga, contadores finalizam em finally e cleanup de callback usa contexto não cancelável. Causas de falha e cleanup são preservadas, inclusive exceptions recuperadas por coroutines. Erros de programação não são convertidos em sucesso. Ações reais then/ifState e goTo usam caminhos aguardados. `Graph.navigateTo` mantém a fachada assíncrona; a ação usa `navigateToAwaited`.

Execução final exit 0: `.\gradlew.bat :humming-core:jvmTest :humming-actions:jvmTest :sample:composeApp:jvmTest --console=plain`. XMLs: 67 core, 3 ações e 2 Compose, sem falhas/erros/ignorados. Incluem ramos reais, políticas, erro intermediário, limites, recursão, jobs simultâneos, cancelamento, callbacks e mapper que falham/cancelam. O teste de navegação usa provider suspenso por barreira, verifica ausência do efeito seguinte enquanto bloqueado, libera a carga e navega/volta pelo renderer real. O host de teste fornece LifecycleRegistry e ViewModelStoreOwner reais; dependências de Lifecycle adicionadas só aos testes usam a versão já catalogada.

Logs e tentativas anteriores em `build/t03/`; relatório de ações em `humming-actions/build/reports/tests/jvmTest/`. Houve correções de asserção para recuperação de stack trace de coroutines e configuração dos owners do host de teste. Não houve publicação, novo backend nem qualificação Android/Apple/browser. T04 trata assinaturas, ownership de componentes e eventos; esta etapa não afirma isolamento de estado por back stack ou solução completa de lifecycle.

## T04 — lifecycle, bindings e eventos — 2026-10-23

`NodeBinding` observa mudanças somente enquanto coletado. O catálogo usa collectAsState da composição; constantes não assinam o formulário. As quatro variantes imperativas mantêm compatibilidade e recebem releaseBinding; encerramento do ViewModel cancela assinaturas restantes e tarefas. ActionInvoker pertence à composição, impede repetição enquanto ocupado e combina busy com enabled, sem sobrescrever o valor do binding. Botões, IME do campo, topAppBar, Column e imagens usam esse caminho.

Loading usa atraso cancelável somente para aparecer e esconde ao finalizar a última operação, sem atraso. Eventos usam LaunchedEffect cancelável, emissão inicial e identidade estável de observador. Ações do catálogo propagam origem causal com updateStateFromAction; updates iguais não emitem. Métodos customizados precisam da mesma API; updates externos reabrem uma cadeia. Não existe promessa de entrega de cada revisão conflada ou de estado isolado por entrada de navegação.

Comando final, exit 0: `.\gradlew.bat :humming-core:jvmTest :humming-actions:jvmTest :sample:composeApp:jvmTest --console=plain`, log `build/t04/final.log`. XMLs: **78 core + 3 ações + 6 Compose = 87 testes, zero falhas, erros ou ignorados**. Core e sample executaram na última chamada; ações estavam UP-TO-DATE após a execução aprovada em fifth.log. Novos casos: 7 BindingLifecycleTest, 4 StateEventTest, 3 BindingCompositionTest e 1 ActionRecoveryTest. Cobrem assinatura fria/constante, edição/remoção, descarte explícito, fechamento de owner, tarefas concorrentes/canceladas, emissão inicial, ciclos entre observadores, recomposição, busy durante mudança de binding e cancelamento ao remover componente. Recuperação usa o host real, ação registrada que falha, dispensa de erro pela UI e nova tentativa bem-sucedida.

A regressão inicial de navegação foi diagnosticada: o dispatcher da composição do host de teste chamou Lifecycle fora de Main. executeActions agora usa Main.immediate preservando o Job chamador. O teste original voltou a passar, aguardando provider e executando a ação seguinte, além de navegar/voltar. O encerramento do teste desmonta a composição antes de destruir o Lifecycle, preservando a causa original em falhas futuras. Logs de tentativas conservados em build/t04.

A imagem regenerada `sample/composeApp/build/reports/local-smoke.png` foi inspecionada: campo editado, texto atualizado e status confirmado. Isso valida renderização Compose em JVM; não comprova inspeção de janela nativa ou Android/Apple/JS/Wasm. Sem publicação nem backend novo. Status e retomada somente em current-state.md.

## T05 — schemas e compatibilidade do catálogo — 2026-10-23

Implementados NodeDefinition, PropertyDefinition, ParsedNodeProperties e NodeCatalog em commonMain, sem imports Compose/Lifecycle nesses tipos. SDLibrary preserva suas APIs e acrescenta registro de definições. O ViewModel valida papéis/propriedades antes de resolver handlers. LayoutDefinitions, ActionDefinitions e NavigationDefinitions fornecem 15 definições versão 1, com cobertura e exceções inventariadas no contrato. Os adapters preservam os valores wire originais e seus defaults/coerções no renderer.

Comando final: `.\gradlew.bat :humming-core:jvmTest :humming-actions:jvmTest :sample:composeApp:jvmTest --console=plain`, exit 0, log `build/t05/final.log`. XMLs: **90 core + 4 ações + 16 sample = 110 testes**, zero falhas, erros ou ignorados. Os três grupos executaram na chamada final. Antes dela houve uma falha de inferência de tipo na asserção nova de mapa nullable; corrigida no teste, sem alterar dependências. As tarefas JVM já tinham sido descobertas nos recortes anteriores.

Novos casos: 12 NodeDefinitionTest, 1 NestedActionsTest para método não registrado, 9 CatalogCompatibilityTest e 1 CatalogRenderingTest. Sample total: 9 testes de catálogo sem composição e 7 testes Compose. Cobrem caminhos/documento/ID, campos obrigatórios/tipos/faixas, defaults sem mutação do wire, bindings, enums e fallbacks reais, propriedades desconhecidas/opacas, travessia limitada e ciclos programáticos, papéis em eventos/filhos, rotas duplicadas/ausentes, deep links legados versus autoria, métodos registrados e preservação das extensões sem schema.

CatalogRenderingTest executa Column/Row/Box/Text/TextField/Button reais, slot label, binding textual, edição do campo e sequência ifState → method → update, observando o resultado Ana: salvo / confirmado. As regressões anteriores de cancelamento, navegação/retorno e recuperação de falha também passaram. Não houve nova inspeção visual nativa; assertions de composição não substituem essa qualificação.

Metadados não comprovam implementação do Inspector ou negociação de capacidades. A ausência de imports UI nos tipos novos também não prova classpath puro: isso permanece para T06. Não executados Android, Apple, JS ou Wasm neste recorte; sem backend, publicação ou mudanças de versões. Status e retomada somente em current-state.md.

## T06 — contrato puro e consumidor headless — 2026-10-24

Extraídos para humming-contract: ServerDrivenNode, NodeDecoder, NodeDiagnostic, NodeDefinition, NodeJson e as três listas de definições. Pacotes públicos preservados; humming-core exporta o contrato via api. Color e execução permanecem no core. UUID não depende mais de Platform. O build do contrato só usa os plugins Kotlin Multiplatform e serialização, toolchain 25 e bytecode JVM 17.

Comandos executados, exit 0:

- `.\gradlew.bat :humming-contract:tasks --all --console=plain`: tarefas JVM e demais targets declarados, log build/t06/tasks.log.
- `.\gradlew.bat :humming-contract:jvmTest :humming-core:jvmTest :humming-actions:jvmTest :sample:composeApp:jvmTest --console=plain`: **30 contrato + 64 core + 4 ações + 16 sample = 114 testes**, zero falhas, erros ou ignorados. Log build/t06/tests.log. As 26 regressões de parsing/limites foram movidas do core, mantendo seu conteúdo; os 4 casos adicionais são headless.
- `.\gradlew.bat :humming-contract:dependencies --configuration jvmTestRuntimeClasspath :humming-core:compileDebugKotlinAndroid --console=plain`: relatório sem UI no contrato e compilação Kotlin do core Android aprovada, log build/t06/classpath-android.log.

HeadlessContractTest valida catálogo/diagnóstico e serializa modelo editado com ServerDrivenNode.serializer(), sem inicializar renderer ou ViewModel. HeadlessClasspathTest verifica que classes Compose, Lifecycle, Navigation, Material e Skia não estão disponíveis no classloader de teste. T01/T05 e os fluxos Compose anteriores continuam aprovados nos respectivos módulos. O contrato puro depende de stdlib e serialização; testes acrescentam kotlin-test/JUnit.

Migração de facades JVM exige recompilar consumidores antigos, descrita no contrato. Não houve publicação nem execução Android em dispositivo; compilar o core não comprova aplicativo Android. Os demais módulos Android e Apple/JS/Wasm ainda precisam de qualificação. Estado e próxima tarefa somente em current-state.md.

## T13 — modelo autoral e codecs locais — 2026-10-24

Implementados em humming-studio-model: AuthoringDocument/AuthorNode/AuthorEdge, placements, índices derivados, validação estrutural, AuthoringDocumentCodec e LegacyAuthoringCodec. Dependência única de produção: humming-contract. A ponte preserva IDs/ordem/propriedades e campos extras; o codec autoral detecta duplicatas em listas e produz JSON canônico. IDs gerados são persistidos. Ações e eventos usam arestas, não cópias ocultas no mapa de propriedades.

Descoberta de tarefas: `.\gradlew.bat :humming-studio-model:tasks --all --console=plain`, exit 0, build/t13/tasks.log. A primeira compilação de testes encontrou dois parênteses excedentes em asserções novas, corrigidos. Houve mensagem de fallback do compilador incremental após mover ServerDrivenNode entre módulos; a execução posterior completou sem trocar versões ou limpar arquivos de usuário.

`.\gradlew.bat :humming-studio-model:compileCommonMainKotlinMetadata :humming-studio-model:compileKotlinJs :humming-studio-model:compileKotlinWasmJs --console=plain` passou, incluindo as compilações correspondentes do contrato, em build/t13/multiplatform.log. Isso comprova compilação dos módulos puros, sem execução de UI em browser ou Apple.

Os testes incluem round trip de fixture com JSON primitivo/desconhecido, slots objeto/array/string, eventos/condições, IDs gerados, edição seguida de exportação, mapas embaralhados, duplicatas no wire, pais/portas/referências inválidas, chaves/limites, contenção e fluxo cíclicos, navegação cíclica válida e raízes não selecionadas. Grafos compartilhados e navegação explícita não produzem árvore legada com perda silenciosa; são preservados no codec autoral e recebem diagnóstico para compilação.

Verificação final de T13: `.\gradlew.bat :humming-studio-model:jvmTest :humming-contract:jvmTest :humming-core:jvmTest :humming-actions:jvmTest :sample:composeApp:jvmTest :humming-studio-model:compileKotlinJs :humming-studio-model:compileKotlinWasmJs --console=plain`, exit 0, build/t13/final.log. **21 autoria + 30 contrato + 64 core + 4 ações + 16 sample = 135 testes**, sem falhas/erros/ignorados nos XMLs. Na chamada final executaram os 21 testes de autoria e compilações alteradas; os demais foram UP-TO-DATE após a execução aprovada em tests.log. Não houve canvas, execução de UI browser ou backend.

## T14 — chaves, comandos atômicos e undo/redo — 2026-10-24

Implementados generateKeyBetween, operações tipadas, DocumentPatch e EditSession. Publicação por CAS inclui documento, histórico e recibos; entradas do host/comando são copiadas profundamente. Ordem usa chaves ordinais densas sem renumerar irmãos; esgotamento é erro explícito. As operações preservam relações, hints objeto/array/string e propriedades desconhecidas quando alteradas por campo.

Execução abrangente, exit 0 em build/t14/final.log: `.\gradlew.bat :humming-studio-model:jvmTest :humming-contract:jvmTest :humming-core:jvmTest :humming-actions:jvmTest :sample:composeApp:jvmTest :humming-studio-model:compileCommonMainKotlinMetadata :humming-studio-model:compileKotlinJs :humming-studio-model:compileKotlinWasmJs --console=plain`. Após acrescentar a regressão de identificador inválido, execução focada do modelo JVM/metadata/JS/Wasm passou em build/t14/verified.log. Nenhuma versão ou dependência nova foi necessária.

XMLs finais: **44 autoria + 30 contrato + 64 core + 4 ações + 16 sample = 158 testes**, zero falhas/erros/ignorados. Novos casos: 5 FractionalKeysTest, 16 EditSessionTest e 2 ConcurrentEditSessionTest. As regressões dos outros módulos permanecem aprovadas e foram UP-TO-DATE na execução abrangente; o modelo executou após as últimas mudanças.

Cobertura: vetores e 2.000 inserções pseudoaleatórias determinísticas sem alterar chaves existentes; 500 inserções no mesmo intervalo; prefixos, entradas inválidas e esgotamento. Movimentação no mesmo pai e entre containers, ancestrais/vizinhos/revisões inválidos, batch sem publicação parcial, remoção/undo de subárvore e arestas, troca de entry, posição, propriedades e JsonNull, hints de eventos, redo invalidado, limites de memória representada e recibos, propriedade de dados externos e namespace inválido.

Concorrência real JVM usa duas threads sincronizadas por barreira: requisições diferentes na mesma revisão têm um vencedor e um conflito; a mesma requisição simultânea tem um commit e um replay. Undo posterior confirma uma única entrada efetiva. Isso não comprova coordenação entre processos ou uso de undo remoto. Sem canvas/servidor/publicação; commits são locais conforme instrução do usuário.

## T15 — compilador e jornada local (2026-10-24)

Comando executado: `./gradlew.bat :humming-studio-model:jvmTest :humming-contract:jvmTest :humming-core:jvmTest :humming-actions:jvmTest :sample:composeApp:jvmTest :humming-studio-model:compileCommonMainKotlinMetadata :humming-studio-model:compileKotlinJs :humming-studio-model:compileKotlinWasmJs --console=plain`. BUILD SUCCESSFUL; log local ignorado `build/t15/final.log`. XMLs: 172 testes, zero falhas/erros (57 autoria, 30 contrato, 64 core, 4 ações, 17 sample); módulos sem mudanças puderam ficar UP-TO-DATE.

Treze testes do compilador verificam golden, determinismo com mapas embaralhados, capacidades e versões, métodos/providers, campos desconhecidos, desconectados, colisões de IDs na expansão, limites, conflito de destino, grafo externo e portas/ciclos. Compilação metadata/JS/Wasm do módulo puro passou; isso não comprova UI no navegador.

`CompiledJourneyTest` executa artefatos gerados com HummingSDUI, SDNavigation, SDActions e provider local real: primeira tela → segunda tela, ramo condicional, método registrado, atualização de estado e retorno. O teste observa `Segunda: sim / executado` e ausência de diagnóstico. Artefatos locais em `sample/composeApp/build/reports/compiled-journey/`. A captura second-screen.png foi inspecionada: o botão Voltar aparece, porém o texto escuro não fica legível no fundo transparente da captura; a evidência textual vem das asserções Compose, não da imagem. Não houve observação de janela nativa nem execução browser/Apple nesta etapa.

## T16 — preview Wasm observado (2026-10-24)

Tarefas descobertas por `./gradlew.bat :sample:composeApp:tasks --all`. `:sample:composeApp:compileKotlinWasmJs` passou antes e depois do host LocalPreview; `:sample:composeApp:jvmTest` passou com 18 testes do sample, total acumulado 173. LocalPreviewTest verifica edição, binding, update, MISSING_METHOD, limpeza do erro e descarte/recriação de estado. Logs locais ignorados em build/t16.

`./gradlew.bat :sample:composeApp:wasmJsBrowserDevelopmentRun --console=plain` gerou os executáveis e Webpack. A primeira tentativa falhou porque o cache Kotlin npm continha lock vazio e node_modules incompleto, sem webpack/bin/webpack.js nem integridade Yarn. Reexecutar kotlinWasmToolingSetup não reparou; o Yarn já instalado executou install --frozen-lockfile --non-interactive no cache indicado e reparou a instalação, sem troca de versões. Não é mudança necessária em dependências do projeto.

O HTML carregava sampleApp.js, enquanto o bundle real é composeApp.js; corrigido no target Wasm. Fragmento webpack restringe host a 127.0.0.1, impede abertura automática e serve somente kotlin/processedResources. Log server-local.log confirma loopback e ausência da raiz do repositório na lista estática.

Chrome em localhost:8080 renderizou a fixture. Digitação/substituição pelo input do Compose alterou o texto e seu binding; Confirmar exibiu Status: confirmado. Testar diagnóstico exibiu MISSING_METHOD, nó preview-missing-method e caminho $.children[5].properties["onClick"].properties["invoke"]. Limpar diagnóstico retirou a mensagem preservando estado. Capturas locais browser-diagnostic.png e browser-preview.png em build/t16. Não foi QA mobile nem qualificação integral do catálogo/targets.

Console: aviso de importação dinâmica do Webpack e mensagem de depreciação de acesso a wasmExports.memory por dependência existente; não impediram os fluxos observados. Não houve atualização de dependências por tentativa. Não há provider URL, navegação nem método de negócio no preview; o método inexistente é uma fixture intencional de diagnóstico.

## T17 — recorte de editor local (2026-10-24)

Descoberta: `./gradlew.bat :sample:composeApp:tasks --all --console=plain`, log build/t17/tasks.log. Validação final executada: `./gradlew.bat :humming-studio-model:jvmTest :humming-contract:jvmTest :humming-core:jvmTest :humming-actions:jvmTest :sample:composeApp:jvmTest :humming-studio-model:compileCommonMainKotlinMetadata :humming-studio-model:compileKotlinJs :sample:composeApp:compileKotlinWasmJs --console=plain`. BUILD SUCCESSFUL, log build/t17/final.log; 86 tarefas, 15 executadas e 71 UP-TO-DATE. XMLs somam 183 testes, zero falhas/erros/ignorados: 65 autoria, 30 contrato, 64 core, 4 ações e 20 sample. Não houve execução Android/Apple neste recorte.

LocalWorkspaceTest acrescentou oito casos de comportamento: criação/edição/undo/redo/save/open/compilação/reimportação; abertura inválida preservando histórico; schemas e ausência/vazio; dados desconhecidos somente leitura; reordenação/movimento e ciclo inválido; ordem de ações por aresta/porta inválida atômica; remoção sem descarte silencioso de ações; métodos sem capacidade bloqueados.

LocalStudioTest acrescentou dois testes Compose com renderer real. O fluxo cria Column/textField/text/button/action:update, edita e desfaz/refaz, seleciona nó recém-inserido na árvore, salva/reabre pelo adapter de teste, exporta contrato, executa o botão e verifica estado. A reabertura limpa histórico e estado do preview mesmo quando o documento é idêntico. Outro caso rejeita valor inválido e abertura inválida sem perder documento. O adapter RecordingFiles é fake de teste; não comprova o seletor nativo do navegador. Artefatos gerados em sample/composeApp/build/reports/local-studio/document.json e contract.json.

Falhas investigadas e corrigidas: tamanho forçado de teste excedia o viewport real de 1024×768; teste agora usa o tamanho efetivo. A árvore LazyColumn não observava a projeção da revisão; passou a consumir lista derivada com remember(document), com regressão de seleção de novo nó. Reabrir o mesmo documento mantinha ViewModel do preview por chave id/revisão idêntica; geração de abertura separada agora força descarte/recriação.

Browser: `./gradlew.bat :sample:composeApp:wasmJsBrowserDevelopmentRun --console=plain`, log build/t17/server-final.log; Chrome em localhost:8080. Observado: criação inicial de textField, importação de contrato pelo diálogo JSON, árvore com seis nós/uma ação, seleção pelo Inspector, execução de Confirmar exibindo Nome: Pessoa Studio / confirmado, edição de text para Editor: #{nome} / #{status} e preview atualizado. Captura build/t17/browser-studio.png inspecionada. Download real de humming-document.json confirmado em Downloads: seis nós, uma aresta, revisão 1 e texto editado; humming-contract.json também existe e contém o contrato atualizado. Cópias de evidência ignoradas em build/t17/browser-document.json e browser-contract.json. A espera automatizada pelo evento de download expirou; a existência e o conteúdo dos arquivos foram verificados diretamente, sem declarar que o evento passou.

**Limite da automação e aceite manual:** fileChooser.setFiles foi bloqueado pela extensão ChatGPT do Chrome sem permissão de URLs de arquivos locais. Nenhuma permissão foi alterada. O agente solicitou ao usuário abrir Downloads/humming-document.json pelo botão Abrir documento, confirmar Substituir e executar Confirmar. Em 2026-10-24, o usuário respondeu “apareceu confirmado”. Esse relato é a evidência manual de abertura e ação que faltava; não é execução automatizada do seletor nem confirmação manual de outros controles. A limpeza do histórico/estado ao reabrir permanece sustentada pelos testes Compose. O estado atual registra a conclusão de T17 e o próximo recorte. Nenhum teste/build novo foi executado nesta atualização documental; preservam-se os resultados anteriores de 183 testes JVM.

## T18 — geometria pura (2026-10-24)

Descobertas tarefas em build/t18/tasks.log. Executado `./gradlew.bat :humming-studio-model:jvmTest :humming-studio-model:compileCommonMainKotlinMetadata :humming-studio-model:compileKotlinJs :humming-studio-model:compileKotlinWasmJs --console=plain`: BUILD SUCCESSFUL, build/t18/geometry.log. Modelo soma 76 testes JVM sem falhas/erros/ignorados, incluindo 11 novos: centros/igualdade/gaps/padding, vazio/filtrados, gap completo sem colisão e undo, Row e guia transformada, zoom/densidade/afim singular, hit/pintura/clips, alvos aninhados e exclusão de descendentes, fluxo reverso/layout obsoleto, Box append, capture/cancel e revisões no release. Total acumulado 194 com os resultados anteriores dos demais módulos; eles não foram reexecutados neste recorte. Essa prova pura não valida medição ou gestos no renderer/browser, ainda necessários para T18.

## T18 — integração de bounds e gesto (2026-10-24)

Comando final: `./gradlew.bat :humming-studio-model:jvmTest :humming-contract:jvmTest :humming-core:jvmTest :humming-actions:jvmTest :sample:composeApp:jvmTest :humming-studio-model:compileCommonMainKotlinMetadata :humming-studio-model:compileKotlinJs :sample:composeApp:compileKotlinWasmJs --console=plain`. BUILD SUCCESSFUL em build/t18/final-verified.log; 86 tarefas, 15 executadas e 71 UP-TO-DATE. XMLs: **201 testes**, zero falhas/erros/ignorados (77 autoria/geometria, 30 contrato, 64 core, 4 ações, 26 sample). Metadata/JS do modelo e Wasm do sample compilados; não houve execução Android/Apple nem QA mobile.

Doze testes puros de DropGeometry incluem o caso adicional de overlay à frente: o drop não atravessa uma região de outra cadeia de ancestrais. Seis testes GeometryPreviewTest verificam limites OUTER/CONTENT com padding medido e clip real; descarte de bounds; drag entre containers e undo; Escape; drag com zoom 1,25 e pan; perda de foco; seleção em modo edição sem executar action:update e execução normal no outro modo. A guia é capturada durante o mouse pressionado, antes do release; verificação de pixels confirma a linha rosa. Imagem sample/composeApp/build/reports/drop-guide.png inspecionada.

A última falha de teste foi seletor ambíguo entre o texto Confirmar no Inspector e o botão do formulário. O seletor passou a exigir Role.Button; não houve defeito de execução nem supressão de asserção. O teste então passou e a suíte final foi executada.

Servidor local por `./gradlew.bat :sample:composeApp:wasmJsBrowserDevelopmentRun`, log build/t18/server-final.log. Chrome: carregou Exemplo de arraste, ativou Modo edição e moveu A do container roxo para o verde. Captura na conversa mostrou revisão 1, árvore com A abaixo de drop-right e preview com B à esquerda/A à direita. Uma captura concorrente ao drag mostrou o estado anterior; a ferramenta completa o drag antes de permitir observar a guia transitória. Não declarar essa linha como observada no Chrome. A versão final foi recarregada e capturada em build/t18/browser-current.png, com controles de pan legíveis; a imagem registra estado inicial, não aquele commit.

Pergunta manual enviada ao usuário para observar a linha rosa antes do release. Seu resultado ainda precisa ser registrado para fechar T18, conforme estado atual. Guia comprovada em Compose e commit comprovado no browser são evidências distintas. Não houve alteração de dependências/permissões nem push.

## T18 — distinguir tela vazia do exemplo de drag (2026-10-24)

O usuário respondeu à pergunta visual: “só apareceu uma caixinha quadrada bem pequena”. Inspeção do navegador integrado em 127.0.0.1:8080 mostrou documento com raiz Column vazia, revisão 0, em modo de edição, e contorno de 48×48; não havia A/B nem containers roxo/verde. Esse estado não exercita a inserção por drag. O relato não foi convertido em aceite nem em defeito comprovado da matemática.

LocalStudio passou a explicar a ausência de elementos e oferecer Carregar exemplo de arraste dentro do preview. GeometryPreviewTest acrescentou um caso que parte da tela vazia, ativa edição, carrega o exemplo, verifica os nós A/B/containers e a preservação do modo. Executado `./gradlew.bat :sample:composeApp:tasks --all --console=plain` e depois `./gradlew.bat :sample:composeApp:jvmTest :sample:composeApp:compileKotlinWasmJs --console=plain`: BUILD SUCCESSFUL, build/t18/empty-preview.log. Sample: 27 testes, zero falhas/erros/ignorados; total acumulado com os módulos sem mudanças: 202. Não foram reexecutados testes dos outros módulos. Servidor reconstruído em build/t18/empty-server.log. A guia no browser ainda depende de teste com o exemplo carregado; ver retomada no estado atual.

Após reconstruir e recarregar o host, o navegador integrado exibiu a mensagem Tela vazia e o botão contextual; captura build/t18/empty-browser.png inspecionada. Não houve alteração de permissões nem teste manual declarado como aprovado.

## T18 — confirmação manual após carregar o exemplo (2026-10-24)

Após a orientação corrigida de carregar o exemplo com A/B e os containers, o usuário respondeu “agora sim”. No contexto da verificação solicitada, esse relato confirma a guia visual e completa o aceite pendente. Não é captura automatizada adicional nem nova execução dos testes. Preservam-se os resultados anteriores de 202 testes JVM e as limitações de plataforma. O estado atual registra a conclusão de T18 e a retomada em T19. Nesta atualização somente documental foram revisados conteúdo, links e diff; não foi necessário novo build.

Para verificar o transporte opcional sem backend externo:

```powershell
.\gradlew.bat :humming-network:jvmTest
```

Os testes usam MockEngine e um HttpServer JVM em 127.0.0.1 com porta efêmera, encerrado em finally. Executam GET/POST reais, JSON tipado, falhas, limites, timeout, cancelamento, concorrência e ramos da ação. O host dos testes fecha cliente e engine/executor quando é seu dono. Isso não comprova execução HTTP em browser, Android ou Apple; ver a evidência específica no estado atual.

## Probe da bridge em Android

O sample fornece um runner de instrumentação próprio, sem substituir execução em dispositivo por teste JVM. Descobrir assembleDebug/assembleDebugAndroidTest no módulo sample:composeApp antes de gerar APKs locais. Instalar app e APK de teste somente no emulador/dispositivo selecionado explicitamente; não usar serial implícito quando houver várias instâncias.

Executar `adb -s SERIAL shell am instrument -w sample.app.androidApp.test/sample.app.NativeBridgeInstrumentation`. Aceite do probe: native.result=PASS, native.cases com real-uptime/argument-rejection/unavailable/version-rejection e INSTRUMENTATION_CODE=-1, além de native.hostRecreation=PASS. O runner compara o valor do adapter com chamadas reais de SystemClock.elapsedRealtime antes/depois; não usa JUnit e não deve ser contado como casos JVM. Falhas retornam native.result=FAIL e stacktrace.

A entrada opt-in de UI Android é a Activity sample.app.AppActivity do pacote sample.app.androidApp com extra booleano nativeExample=true; ela usa o adapter real e lê uptime pelo evento onCreate registrado, além do botão de leitura. A entrada padrão permanece o sample existente. Conferir resultado/tela e recriação do formulário; chamadas e evidência por plataforma devem ser registradas somente no estado atual. Browser em ?native recebe um registro vazio explícito, sem acesso ao adapter Android. Testes Compose JVM de handlers de fixture validam UI/lifecycle, sem comprovar APIs nativas.

## Host local de rascunhos

Descobrir tarefas em humming-server-application e humming-server-host. Ambos têm target JVM; não presumir tarefa de metadata independente. Executar jvmTest dos dois módulos e regressão de contrato/compilador/sample. O teste headless verifica ausência de Compose/Lifecycle/PostgreSQL no classpath da aplicação. Os testes HTTP devem usar porta loopback efêmera e HttpClient real, fechar clientes/host e verificar autorização, payload, compilador, conflito, timeout e cancelamento.

Para executar composition root local, fornecer HUMMING_LOCAL_TOKEN no ambiente do processo (por exemplo, gerar um GUID local sem imprimir ou salvar) e executar .\gradlew.bat :humming-server-host:runLocal. A URL/porta é impressa sem token; requisição sem Bearer deve receber 401. O principal inicial só tem grants para tenant local/document example. Dados são voláteis; encerrar o host após o smoke test. Não usar essa execução como prova de PostgreSQL ou permissão para expor o serviço à rede pública.

## Integração PostgreSQL real

Descobrir postgresIntegrationTest em humming-server-postgres e postgresHttpIntegrationTest em humming-server-host. Essas tarefas exigem HUMMING_TEST_DB_URL/USER/PASSWORD e falham se faltarem; não fazem fallback nem são casos ignorados da suíte comum. Guardas dos testes exigem banco local explicitamente chamado humming_test. jvmTest regular exclui essas classes; contar suas evidências separadamente.

Com Docker já instalado/disponível, executar .\scripts\verify-postgres.ps1. O script cria container/volume únicos rotulados T21, imagem oficial por digest, porta só em 127.0.0.1 e senha aleatória no ambiente. Credencial não vai para log ou arquivo. Aguarda banco/porta, roda ambas as tarefas, reinicia banco, confere registros, redescobre porta dinâmica e repete os testes. No finally restaura ambiente e para somente seu container; preserva container/volume para inspeção, sem remover dados. build/t21/environment.json contém metadados sem segredo; é um snapshot, não URL permanente. Não reutilizar a porta após reinício sem consultá-la novamente.

Verificar migrations idempotentes, dois clientes disputando revisão, reenvio do commandId, conflito de fingerprint, isolamento tenant/documento, lote inválido, falha SQL entre UPDATE e recibo, cancelamento durante row lock real e reabertura de host HTTP com o mesmo banco. O trigger de falha pertence apenas ao banco de integração e é removido no finally. Reiniciar banco deve preservar registros; host novo deve consultar fonte/revisão/recibo, sem depender de objeto anterior. Resultados e hashes/versões efetivamente executados pertencem somente ao estado atual.
