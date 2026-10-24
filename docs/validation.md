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
| [LegacyParsingTest](../humming-core/src/commonTest/kotlin/me/developes/humming/sdui/common/LegacyParsingTest.kt) | 12 | Identidade explícita/gerada, nomes case-sensitive, ordem, propriedades textuais/nested, slots, eventos objeto/array, JSON inválido, round trips e limitações do codec |
| [LegacyResolutionTest](../humming-core/src/commonTest/kotlin/me/developes/humming/sdui/common/LegacyResolutionTest.kt) | 10 | Namespaces, handlers, providers suspensos, destino repassado, falhas, sequência direta e identificadores malformados |
| [LegacyStateTest](../humming-core/src/commonTest/kotlin/me/developes/humming/sdui/common/LegacyStateTest.kt) | 8 | Inicialização, edição/remoção, vazio versus ausência, múltiplos placeholders, atualização de flows e isolamento entre instâncias de ViewModel |

[LegacyFixtures](../humming-core/src/commonTest/kotlin/me/developes/humming/sdui/common/LegacyFixtures.kt) concentra um payload com IDs estáveis, eventos e conteúdo visual separados. [ViewModelTestSupport](../humming-core/src/commonTest/kotlin/me/developes/humming/sdui/common/ViewModelTestSupport.kt) instala dispatcher Main de teste, mantém ownership dos ViewModels e limpa stores/jobs antes de restaurar o dispatcher. Os handlers/providers de teste são doubles das extensões do core; não comprovam backend, catálogo Material 3 ou plataforma nativa.

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
