# Plano de desenvolvimento

Define tarefas, dependências e critérios de aceite. O [estado atual](current-state.md) é a única fonte de status, evidências por etapa e ponto de retomada. Revisão inicial: 2026-10-23. A reorganização documental não executou nenhuma tarefa de implementação. O escopo de cada implementação segue a solicitação de trabalho; concluir um plano não significa entregar o programa.

Entradas: [estado atual](current-state.md), [arquitetura](architecture.md), [contrato](contract.md), [Studio](studio.md), [decisões](decisions.md) e [validação](validation.md).

## Como executar

1. Selecionar uma tarefa pronta, conferir dependências e ler os arquivos afetados. Dependências abaixo são gates de conclusão; protótipos isolados podem antecipar investigação sem declarar integração pronta.
2. Registrar responsável, recorte e status **em execução** na linha da etapa em `current-state.md`, com próxima ação. Fechar as decisões técnicas necessárias no registro de decisões.
3. Implementar a menor entrega funcional, com fixtures e testes proporcionais. Preservar o legado ou implementar migração explícita.
4. Executar os checks do recorte e registrar resultados/limites conforme validação. Em bloqueio, anotar causa, comando, responsável e próximo passo; continuar trabalho independente possível.
5. Revisar contrato/arquitetura/estado atual afetados. Só então marcar a etapa **concluída** no estado atual, vinculando commit/PR e evidência, e atualizar o ponto de retomada. Uma compilação isolada não substitui os demais critérios.

O estado atual define os status e deve ser atualizado após cada entrega. Não manter status neste plano nem outro checklist paralelo com resultado divergente.

## Sequência e entregas utilizáveis

| Entrega | Resultado demonstrável | Tarefas |
| --- | --- | --- |
| A — base verificável | Sample local com contrato caracterizado, diagnósticos e execução/lifecycle testados | T00–T04 |
| B — definições compartilhadas | Subconjunto de catálogo com schema e fronteira adequada para uso sem UI | T05–T06 |
| C — autoria local | Importar, editar, desfazer, exportar e executar uma tela legada sem servidor | T13–T17 |
| D — manipulação visual | Reordenar por drop e editar interações com resultado compilável | T18–T19 |
| E — capacidades adicionais do cliente | Tipos/escopos, expressões, transporte, jornada/cache e bridge, em recortes independentes | T07–T12 |
| F — documentos remotos | Persistir rascunhos e consumir publicação imutável compatível | T20–T22, T24 |
| G — dados e colaboração | Resolver dados autorizados e sincronizar edições com recuperação | T23, T25 |
| Qualificação | Evidência por plataforma e recorte escolhido | T26, continuamente |

A prioridade inicial é A → B → C → D. E pode avançar em paralelo após seus pré-requisitos e integrar o Studio por capacidade. F/G vêm depois do compilador e da autoria local. Não exigir a conclusão de E inteira para construir C. Nenhum serviço remoto é pré-requisito do primeiro editor local.

## Backlog e dependências

Todos os IDs referidos na coluna de dependências são obrigatórios para concluir a tarefa. Dependências condicionais de integração estão descritas no detalhe.

| ID | Tarefa | Depende de |
| --- | --- | --- |
| T00 | Base executável JVM/desktop | — |
| T01 | Fixtures e testes de caracterização | T00 |
| T02 | Diagnósticos e resolução de payload | T01 |
| T03 | Executor suspenso e política de falha | T01, T02 |
| T04 | Lifecycle, loading e bindings legados | T03 |
| T05 | Definições/schema do catálogo inicial | T02, T04 |
| T06 | Fronteira pura e consumo sem UI | T05 |
| T07 | Valores tipados e escopos de tela/ação | T04, T05 |
| T08 | Linguagem de expressões limitada | T07 |
| T09 | Propriedades reativas, tokens e validação | T05, T08 |
| T10 | Transporte HTTP com ownership e resultado | T04, T07 |
| T11 | Jornada, navegação e cache versionado | T07, T10 |
| T12 | Bridge de capacidades nativas | T11 |
| T13 | Documento autoral e importação/exportação | T05 |
| T14 | Comandos, chaves de ordem e undo local | T13 |
| T15 | Compilador puro do subconjunto legado | T05, T06, T14 |
| T16 | Preview do catálogo no host Wasm | T05 |
| T17 | Editor local e Inspector | T14, T15, T16 |
| T18 | Seleção, bounds e drop geométrico | T17 |
| T19 | Grafo de interações e conectores | T15, T17 |
| T20 | Aplicação de servidor e interfaces | T06, T15 |
| T21 | Rascunhos/revisões em PostgreSQL | T20 |
| T22 | Publicação imutável e capacidades | T15, T21 |
| T23 | Resolução BFF e isolamento de dados | T07, T20, T22 |
| T24 | Edição remota com revisão e recuperação | T17, T21, T22 |
| T25 | Múltiplos editores e conflitos | T24 |
| T26 | Qualificação por plataforma e recorte | T00 |

## A — tornar o cliente confiável

### T00 — Base executável JVM/desktop

- **Entradas/arquivos:** settings, builds, catálogo, wrapper, workflows e App/recursos do sample.
- **Trabalho:** descobrir projetos/tarefas; registrar JDK/SDK e dependências resolvidas; compilar core e sample JVM; escolher um fluxo exclusivamente local com componentes/providers existentes. Corrigir somente bloqueios reproduzidos. Confrontar workflows com os módulos reais, corrigindo referências inválidas quando incluídas no recorte, sem acionar distribuição.
- **Saída:** procedimento reproduzível em `validation.md`, plataforma identificada e fixture local selecionada. Registrar separadamente problemas de configuração e limitações de exemplos.
- **Aceite:** log dos comandos disponíveis, compilação JVM e fluxo visual observado, com estado/binding e ação simples; erro ou bloqueio não vira sucesso. Não fazer upgrade amplo nem criar servidor/editor.

### T01 — Fixtures e testes de caracterização

- **Entradas/arquivos:** builds de teste, ServerDrivenNode/Json, SDLibrary, ViewModel, ações e fixtures do sample.
- **Trabalho:** habilitar `commonTest` nos módulos necessários e dependências de teste no source set correto; caracterizar parsing, IDs, propriedades, eventos, namespace e bindings. Retirar dependência exclusivamente de teste da produção se não houver uso legítimo.
- **Saída:** fixtures mínimas válidas/inválidas, teste que executa lógica real e casos conhecidos de defeito identificados como tais. Distinguir resultado observado de comportamento desejado.
- **Aceite:** testes JVM executados para objeto/array/null, nome exato, namespace ausente/desconhecido, ID ausente, múltiplos placeholders e round trip. Teste verde não deve afirmar como desejado um defeito conhecido; documentar teste de regressão a ativar na correção correspondente.

### T02 — Diagnósticos e resolução de payload

- **Entradas/arquivos:** parser, registro, carregamento no ViewModel e fixtures T01.
- **Trabalho:** definir erro com nó/caminho/operação/código, limites de entrada e política de fallback; validar raiz, namespace, provider e handler sem `!!` no caminho de erro de payload. Separar falha de entrada de exceção de programação.
- **Saída:** parsing/resolução com diagnósticos consumíveis por host e futuro Studio, mantendo adaptador/API legada.
- **Aceite:** testes de profundidade/tamanho, raiz inválida, tipo incompatível, namespace ausente/malformado/desconhecido, provider falho e handler que lança erro. Fallback e logging não mascaram defeitos.

### T03 — Executor suspenso e política de falha

- **Entradas/arquivos:** SDUIFormViewModel, SDAction, ações de navegação e testes de coroutine.
- **Trabalho:** separar lançamento de UI de `execute` suspenso; aguardar sequências/ramos e efeitos de navegação que pertençam à sequência. Propagar cancelamento. Definir política legada de continuação e opção explícita de interrupção para o novo contrato; não mudar silenciosamente o tratamento de erro.
- **Saída:** ordem determinística para `A, if(B,C), D`, resultados de execução e callbacks finalizados com `finally`.
- **Aceite:** teste com delays prova ordem e conclusão; falha intermediária em cada política, cancelamento, exceção em callback e erro no cleanup não prendem contador nem ocultam causa. Nenhum lançamento aninhado escapa à operação aguardada.

### T04 — Lifecycle, loading e bindings legados

- **Entradas/arquivos:** ViewModel, helpers de propriedades/bindings, action:event e componentes do sample.
- **Trabalho:** dar ownership a jobs/collectors; observar bindings sem registrar novo collector a cada composição; garantir loading consistente em cargas concorrentes/falhas/cancelamento. Definir emissão inicial e prevenção de realimentação de `onStateChange`.
- **Saída:** runtime legado estável, com contrato de lifecycle e um fluxo local navegável selecionado em T00.
- **Aceite:** testes de recomposição/assinaturas, remoção de estado, fechamento, jobs concorrentes, provider falho e cancelamento; UI observada em JVM incluindo navegar/voltar e falha recuperável. Isolamento tipado por entrada fica em T07/T11.

## B — preparar reutilização sem reescrever a POC

### T05 — Definições/schema do catálogo inicial

- **Entradas/arquivos:** SDLibrary, catálogo Material 3, parsers de propriedades, ações e metadados de navegação.
- **Trabalho:** adicionar metadados puros e adapters compatíveis para definições. Começar por text, textField, button, Row/Column/Box, update/method/ifState e navegação existente. Inventariar o restante sem fingir cobertura. Preservar slots, eventos, nomes/enums e defaults reais.
- **Saída:** schema e parser compartilháveis por Inspector/validador; catálogo de capacidades suportadas com versão. Nós sem schema continuam no caminho legado, mas não são editáveis como se estivessem suportados.
- **Aceite:** mesmo payload válido produz propriedades equivalentes; inválidos têm diagnóstico por campo; componente, ação e metadado em papel errado são rejeitados. Compatibilidade `layout:textField`, `onClick`, `#{...}` e `invoke` testada.

### T06 — Fronteira pura e consumo sem UI

- **Entradas/arquivos:** modelo, codecs/schemas, helpers de JSON/Color e builds dos consumidores.
- **Trabalho:** separar modelo/codec/diagnósticos de Compose, Lifecycle e execução. Criar módulo próprio somente se necessário ao consumo headless; registrar decisão de manter pacote interno ou extrair módulo. Atualizar dependências sem ciclo e com adaptadores para API pública.
- **Saída:** teste/consumidor JVM sem inicializar UI consegue validar e serializar o contrato; plano de migração aplicado no recorte.
- **Aceite:** compilação do consumidor puro e dos consumidores existentes; testes T01/T05 preservados; nenhuma dependência de Material 3 ou runtime de UI na fronteira pura. Não renomear módulos por uniformidade estética.

## E — capacidades adicionais do cliente

Esta trilha pode avançar em paralelo à autoria local. Cada novo formato exige decisão/schema/fixture antes de uso em produção e integração posterior ao compilador somente quando suportado.

### T07 — Valores tipados e escopos de tela/ação

- **Entradas/arquivos:** modelo interno de valores, stores, ViewModel e adapter de navegação.
- **Trabalho:** distinguir ausente/null/tipos; criar `server/nav/state` por entrada e `response/error` por execução, preservando caminho textual legado. Definir duração e restauração de cada store.
- **Saída:** contexto de avaliação/execução isolado, sem resposta global compartilhada entre requisições.
- **Aceite:** duas entradas da mesma rota não compartilham formulário; concorrência de ações não troca resultados; voltar/descartar limpa recursos; IDs continuam strings; null/ausente e números inválidos têm semântica testada.

### T08 — Linguagem de expressões limitada

- **Entradas/arquivos:** contrato da nova linguagem, tokenizer, AST, parser e evaluator puros.
- **Trabalho:** especificar gramática, precedência/associatividade, acesso a escopos, tipos, curto-circuito, coerção, erros e limites antes de integrar propriedades. Implementar parser determinístico e evaluator com orçamento. Não interpretar `#{...}` como nova linguagem por heurística.
- **Saída:** extensão opt-in versionada e suíte de expressões válidas/inválidas.
- **Aceite:** precedência, curto-circuito, null/ausente, caminhos inválidos, operadores por tipo, divisão inválida, limites de tamanho/profundidade/operações e cancelamento quando aplicável. Payload não alcança código/reflexão arbitrários.

### T09 — Propriedades reativas, tokens e validação

- **Entradas/arquivos:** definição/schema de componentes, evaluator, stores e componentes visuais afetados.
- **Trabalho:** ligar dependências de expressões às propriedades sem collectors duplicados; definir tokens/tema e masks/validação como capacidades declaradas, mantendo o formato legado. Separar dado canônico de apresentação.
- **Saída:** formulário neutro com visibilidade/habilitação/texto reativos e erros de validação por campo.
- **Aceite:** mudança no estado atualiza somente observadores necessários; saída da tela descarta assinaturas; entrada/edição/remoção e erro de expressão têm UI definida. Decimais exatos só entram com regra de domínio e arredondamento testada.

### T10 — Transporte HTTP com ownership e resultado

- **Entradas/arquivos:** humming-network experimental, provider URL do host, executor e contexto T07; builds/engines.
- **Trabalho:** definir API e formato de requisição/resposta, injetar `HttpClient` host-owned, implementar métodos reais e body tipado, limites, headers permitidos, timeout e cancelamento. Incluir o módulo no build apenas com responsabilidade/dependências claras. Primeiro fluxo não depende da linguagem T08.
- **Saída:** ação de transporte funcional com sucesso/falha no contexto da execução e endpoint local de teste; documentação da configuração por host.
- **Aceite:** testes com engine controlada e integração HTTP local real para GET/POST, status não 2xx, JSON inválido, timeout, cancelamento e requisições simultâneas. Cliente reutilizado e fechado pelo dono. Segredos fora do payload/logs; sem dependência de serviço pago.

### T11 — Jornada, navegação e cache versionado

- **Entradas/arquivos:** SDNavigation/Graph, providers, stores, cache e transporte.
- **Trabalho:** evoluir Compose Navigation, separar definição/dado personalizado, fixar versão da jornada, criar política de carga/expiração e particionar cache por sessão. Definir entrada, deep link, voltar, reload, logout, restauração e falha.
- **Saída:** jornada de duas telas com argumentos/estado isolados, definição cacheada e recuperação de provider/rede.
- **Aceite:** navegação/cancelamento/retorno, mesma rota repetida, deep link sem cache prévio, troca de sessão, snapshot incompatível e falha de carga; medir cache/latência. EAGER/LAZY/PREFETCH só habilitadas com testes correspondentes.

### T12 — Bridge de capacidades nativas

- **Entradas/arquivos:** interfaces comuns de capacidades e implementações do host/sample por source set.
- **Trabalho:** registrar bridge suspensa com resultado tipado e lifecycle. Escolher uma capacidade local neutra; declarar disponibilidade e restrições de plataforma. Preview usa executor explícito isolado.
- **Saída:** chamada real em Android, quando houver ambiente, com fallback de não suporte no browser e cancelamento ao sair da tela.
- **Aceite:** sucesso, rejeição, indisponibilidade, cancelamento e recriação do host. Testes comuns validam protocolo; observação no dispositivo/emulador valida implementação. Se Android estiver bloqueado, não concluir a parte nativa com stub.

## C/D — autoria local e manipulação visual

### T13 — Documento autoral e importação/exportação

- **Entradas/arquivos:** modelo/codec novos no recorte de autoria, contrato e schemas T05.
- **Trabalho:** implementar mapa por ID, placement, arestas e índices derivados descritos em Studio. Importar árvore legada com IDs persistidos, preservar propriedades desconhecidas e validar referências/contensão.
- **Saída:** documento local versionado com codec próprio e diagnóstico de elementos não suportados. Ainda sem canvas ou servidor.
- **Aceite:** importar/exportar fixture preserva propriedades/ordem/IDs; rejeitar ciclos de contenção, pais ausentes, duplicatas e referências inválidas. Serialização determinística e independente do `toString()` legado. Ciclo de navegação não é confundido com ciclo de árvore.

### T14 — Comandos, chaves de ordem e undo local

- **Entradas/arquivos:** documento, índices, funções de chave e histórico de comandos.
- **Trabalho:** implementar inserir/remover/mover/propriedades/arestas/posição, validação de revisão, aplicação atômica, inversa e undo/redo. Usar algoritmo/vetores de Studio e ordenar sem locale.
- **Saída:** API única de mutação; preview transitório separado do documento. Limite de chave e estratégia explícita de erro/rebalanceamento.
- **Aceite:** testes de propriedade das chaves; movimentação no mesmo pai e entre pais; rejeição de ancestralidade/revisão; remoção/undo restaura subárvore, chaves e arestas; falha não deixa índices parcialmente atualizados. Não afirmar desempenho sem medição.

### T15 — Compilador puro do subconjunto legado

- **Entradas/arquivos:** fronteira pura, documento, schemas/capacidades e fixtures de saída.
- **Trabalho:** transformar o documento em árvore/runtime legado, preservando arrays, slots/eventos e ordem de ações. Validar portas, papéis e referências com percurso determinístico, detecção de ciclos e limites de expansão. Definir comportamento de nós desconectados e dados desconhecidos.
- **Saída:** `document + targetCapabilities -> contract | diagnostics`, sem Compose, rede ou banco. Começar por uma tela com atualização de estado e navegação suportada; novas ações/expressões entram só após a tarefa cliente correspondente.
- **Aceite:** golden fixtures determinísticas, mapas embaralhados produzem a mesma saída, ciclos de ação/expansão e portas inválidas têm erro, ciclos de navegação válidos compilam, payload final executa no cliente. Não gerar sintaxe ainda sem handler.

### T16 — Preview do catálogo no host Wasm

- **Entradas/arquivos:** builds/browser do sample, host de preview e renderer/catálogo existentes.
- **Trabalho:** provar compilação/execução Wasm antes de criar estrutura extensa de Studio; montar preview da fixture local com catálogo reutilizado e executor de efeitos controlado. Definir canal de diagnóstico e suporte de capacidades.
- **Saída:** preview visível no browser com o subconjunto T05. Se Wasm bloquear por causa reproduzida, investigar/corrigir e registrar alternativa técnica em decisões; não declarar o target validado por execução JVM.
- **Aceite:** abrir browser, observar renderização, edição de estado e ação local; falha aparece de forma identificável. Preview simulado é rotulado, não envia efeitos externos. Não exigir HTTP, linguagem nova ou backend.

### T17 — Editor local e Inspector

- **Entradas/arquivos:** documento/comandos/compilador e host T16; schema/catálogo.
- **Trabalho:** criar/importar documento, selecionar por árvore, editar propriedades por schema, inserir/remover/reordenar pela árvore, undo/redo, salvar/carregar arquivo, compilar/exportar e atualizar preview. Manter propriedades desconhecidas preservadas e bloqueadas quando necessário.
- **Saída:** primeiro editor utilizável inteiramente local. Implementar seleção por árvore antes de depender do hit geométrico; nenhuma exigência de servidor ou arrastar para concluir esta etapa.
- **Aceite:** criar tela com Column, textField, button e ação; editar, desfazer, exportar, reimportar e executar no sample. Documentar fluxo observado e erros de schema/capacidade. Uma tela demonstrável vale mais que scaffolding de módulos vazios.

### T18 — Seleção, bounds e drop geométrico

- **Entradas/arquivos:** registro de bounds, viewport, overlays, gestos e comandos T14.
- **Trabalho:** implementar conversões, seleção por pintura/clipping, drag com capture/cancel, alvo elegível, índice e guia conforme Studio. Iniciar por Column/Row sem wrap/reverse, com Box explicitamente limitado.
- **Saída:** mover no mesmo container e entre containers via comandos, mantendo preview transitório e revisão do layout/documento.
- **Aceite:** testes puros com container vazio, igualdade no centro, gaps/padding, exclusão do arrastado, filhos filtrados sem colisão de chave, zoom/pan/densidade, sobreposição e destino proibido. Observar guia e commit no browser; revisão de layout/viewport obsoleta, Escape/cancel/perda de capture não alteram o documento. Exportação preserva ordem resultante.

### T19 — Grafo de interações e conectores

- **Entradas/arquivos:** modelo de interação/portas, compilador, canvas macro e geometria pura.
- **Trabalho:** editar click/ações/navegação suportadas, validar cardinalidade e ramos, desenhar âncoras/cúbicas/setas e hit por curva conforme Studio. Gravar relações sem persistir pixels derivados. Não adicionar roteador complexo de obstáculos neste recorte.
- **Saída:** componente interno liga a tela de destino; Inspector mostra trigger/ação/transição apenas quando suportados pelo cliente. Política inicial de autoconexão é decidida/testada, sem curva degenerada aceita silenciosamente.
- **Aceite:** testes das âncoras, tangentes, zoom e seleção; sequência/condição compilada mantém ordem e portas; remover/undo restaura arestas; executar navegação exportada no sample. Navegação cíclica válida não trava o compilador.

## F/G — serviços posteriores

### T20 — Aplicação de servidor e interfaces

- **Entradas/arquivos:** contrato/compilador puros, interfaces de repositório e novo host servidor quando necessário.
- **Trabalho:** definir casos de uso para rascunho/validação/leitura, DTOs, autorização por documento/tenant e erros; composition root injeta adapters. Separar interfaces da aplicação de drivers e rotas para evitar dependência circular.
- **Saída:** aplicação/rotas compiláveis com testes dos casos de uso, sem fingir persistência durável. HTTP inicial suficiente; decidir framework pela prova de build, sem impor GraphQL/federação.
- **Aceite:** testes de autorização, entrada inválida e diagnósticos do compilador; interfaces sem Compose/driver; host sobe localmente. Double de repositório testa lógica, não conclui integração de banco.

### T21 — Rascunhos/revisões em PostgreSQL

- **Entradas/arquivos:** adapter PostgreSQL, migrations e repositório das interfaces T20.
- **Trabalho:** persistir documento/revisão e aplicar comandos com `expectedRevision` em transação. Definir unicidade, tenant, `commandId`, leitura e conflito. Registrar execução local do banco sem credenciais versionadas.
- **Saída:** criar/ler/editar/reabrir rascunho com revisão monotônica e conflito explícito.
- **Aceite:** migrations e integração em PostgreSQL real; rollback, disputa de revisão, duplicata de comando e isolamento de tenant. Reiniciar host não perde documento; não usar H2 como prova de PostgreSQL.

### T22 — Publicação imutável e capacidades

- **Entradas/arquivos:** persistência, compilador, manifesto de capacidades e provider do cliente no formato publicado.
- **Trabalho:** validar draft, criar snapshot imutável identificado e negociar compatibilidade; separar dados de sessão e definir pin da jornada. Definir seleção/rollback de versão sem mutar histórico.
- **Saída:** rascunho editável produz versão que cliente compatível carrega; cliente incompatível recebe diagnóstico/fallback explícito. A integração com nova jornada de T11 depende de T11 concluída; primeira prova pode usar provider legado adaptado.
- **Aceite:** publicar não altera snapshot anterior; rollback seleciona versão existente; duas capacidades distintas não são confundidas por um schemaVersion global; carregamento real do cliente e dados personalizados fora do snapshot. Publicação local de teste não autoriza deploy público.

### T23 — Resolução BFF e isolamento de dados

- **Entradas/arquivos:** aplicação do servidor, registry de fontes e mapeamentos, transporte e escopos.
- **Trabalho:** resolver somente fontes registradas, com identidade/autorização, timeout, concorrência limitada, cancelamento e falha parcial. Definir cache por sessão/tenant e dados anexados ao snapshot fixado sem modificá-lo.
- **Saída:** tela com dado de serviço local real, incluindo falha e recuperação. Sem proxy arbitrário de URLs/headers, segredos em contrato ou acoplamento a um backend comercial.
- **Aceite:** integração HTTP + PostgreSQL quando usado; tenants/sessões isolados, fonte ausente, timeout, resposta inválida, cache errado e cancelamento testados. Testes de política não substituem chamada real local.

### T24 — Edição remota com revisão e recuperação

- **Entradas/arquivos:** Studio local, API, rascunhos, log de comandos e snapshots.
- **Trabalho:** carregar/salvar documento remoto, enviar comando com ID/revisão, tratar conflito sem perder edição local e recuperar após desconexão. Definir outbox/estado pendente, reenvio idempotente e sequência de confirmação antes de adicionar broadcast multiusuário.
- **Saída:** um editor remoto reabre rascunho e retoma após interrupção com diferença entre salvo e pendente visível.
- **Aceite:** reinício/desconexão/reenvio/duplicata, erro de autorização e revisão divergente; ordem de confirmações não sobrescreve estado novo com resposta antiga. Publicação continua imutável.

### T25 — Múltiplos editores e conflitos

- **Entradas/arquivos:** protocolo remoto/log transacional, canal de eventos e política de conflito/undo.
- **Trabalho:** definir autoridade de revisão/sequência no banco, broadcast, replay, deduplicação, presença e ressincronização. Resolver disputa de ordenação e undo sobre alterações concorrentes. Nenhum `Mutex` local substitui a transação entre réplicas.
- **Saída:** dois editores convergem ou recebem conflito explícito recuperável; estado de conexão/presença não altera contrato publicado.
- **Aceite:** dois clientes e duas instâncias de servidor, mensagem perdida/duplicada/fora de ordem, reconexão, comandos concorrentes e undo sem apagar edição alheia. Demonstrar limites de garantia; não declarar exatamente uma vez ou CRDT sem protocolo correspondente.

## Qualificação contínua

### T26 — Qualificação por plataforma e recorte

- **Entradas/arquivos:** cada entrega funcional selecionada, builds/CI reais e matriz de validação.
- **Trabalho:** após T00, anexar evidência por entrega/target em `validation.md`. JVM/desktop primeiro; Wasm nas tarefas de Studio; Android como primeiro dispositivo; Apple em ambiente compatível. Corrigir CI no recorte sem publicar artefatos automaticamente.
- **Saída:** matriz com comando, ambiente, resultado e limites. Cada release candidato declara exatamente o conjunto de funcionalidades/plataformas qualificado; não depende de terminar todo o backlog futuro.
- **Aceite:** testes e fluxos visuais do recorte em cada target anunciado, cancelamento/lifecycle/erro, compatibilidade de fixtures e capacidades. Itens bloqueados ficam explícitos; código compilável, tarefas declaradas ou metadados Maven não comprovam suporte funcional. Encerrar T26 somente para o recorte identificado no registro de evidência; novos recortes reabrem a qualificação.

## Início concreto

A sequência começa por T00, sem alteração estrutural preventiva: inventariar o ambiente, descobrir tarefas, reproduzir bloqueios e executar um fluxo local mínimo. T01 configura testes úteis, T02 melhora diagnóstico, T03 corrige a execução e T04 fecha lifecycle. Só depois o catálogo recebe schemas e alimenta a autoria. O próximo trabalho efetivo é sempre o registrado no ponto de retomada do estado atual. Essa ordem reduz a chance de o editor reproduzir defeitos do runtime e mantém entregas demonstráveis ao longo do desenvolvimento.

## T27 — catálogo Material 3

- Dependências: T05, T09, T17; reabre T26 para componentes novos.
- Entradas: API Material 3 efetivamente resolvida, handlers layout, schemas puros e Studio.
- Trabalho: inventariar famílias visuais públicas da versão resolvida, acrescentar handlers e schemas sem alterar os wrappers legados; conectar estado/eventos, slots e escopos de containers; documentar APIs auxiliares e limites.
- Aceite: inventário API → wire → propriedades/slots; registro coerente com schema, validação de payloads, testes de UI com mudanças de estado/eventos e containers; compilação JVM/Android/Wasm. Sem upgrade de dependências ou promessa de paridade com versões futuras.

## T28 — catálogo oficial executável

- Dependências: T13–T27; exemplos reutilizam runtime/autoria existentes.
- Trabalho: cobrir cada componente registrado com fixture determinística; autoria → compilação → provider → renderer; busca/categorias, código/schema, reset, exportação e abertura no Studio. Reunir cenários de ações/expressões/navegação/cache/nativo e conexão remota explícita. README do app com comandos e prints reais.
- Aceite: cobertura de registro sem lacunas, todos os exemplos autorais compilados e montados; ações/overlays/escopos funcionais; cenários ponta a ponta com integrações reais quando exigirem serviço; comandos de entrada descobertos; prints inspecionados. Capacidades ausentes aparecem como indisponíveis, sem mock de sucesso.

## T29 — criador visual de apps

- Dependências: T13–T28.
- Trabalho: projeto local versionado com nome/viewport/telas sobre o documento autoral existente; fluxo criar → estruturar → editar → conectar → executar → exportar. Shell com páginas/árvore, canvas e Inspector, inspirado nas referências visuais fornecidas. Reutilizar comandos, schemas, bounds/drop e navegação; sem uma segunda autoridade de nós.
- Aceite: criar projeto/tela, inserir e selecionar componentes, editar propriedades, conectar navegação, testar ida/volta, salvar/reabrir projeto e exportar jornada real. Capturas inspecionadas e testes de fluxo; viewport não é emulador nativo. Publicação/serviços reutilizam composição existente; sem deploy automático ou geração de APK prometida.

## T30 — autoria gestual e usabilidade do criador

- Dependências: T18, T19, T29.
- Trabalho: nova tela sempre acessível, canvas de todas as telas inclusive desconectadas, puxada de conectores por evento até tela, paleta com miniaturas SDUI e arraste para Auto Layout. Reaproveitar geometria/comandos, sem mutação durante gesto.
- Aceite: criar três telas; inserir por clique/arraste; guia e ordem Auto Layout; puxar/setar/cancelar conector; undo/redo e execução real da navegação. Testes de ponteiro e inspeção Wasm; arquivos e contratos preservados.

## T31 — viewport do canvas de Ações

- Dependências: T29, T30.
- Trabalho: frames de Ações usam o viewport selecionado; enquadramento e espaçamento derivados acompanham dimensões, sem reescrever posições do documento.
- Aceite: proporções PHONE/TABLET/DESKTOP medidas em UI, troca reativa, conectores válidos após mudança e build Wasm.

## T32 — drop guiado pelo schema

- Dependências: T18, T30.
- Trabalho: aceitar destinos com children/component e slots visuais editáveis; usar bounds e slot escolhido, sem whitelist de containers. Row/Column ordenam; outros inserem no fim.
- Aceite: mover Text para Button com renderer real, inserir da paleta em Button, undo/cancelamento/ciclos; testes de todas as definições com slots e preservação de Row/Column.

## T33 — orientação da guia de inserção

- Dependências: T18, T32.
- Trabalho: renderer informa fluxo de filhos; guia vertical para fluxo horizontal e horizontal para vertical. Destinos sem eixo definido indicam inclusão por contorno.
- Aceite: marca vertical em Button antes do release, ordem lado a lado após drop, regressão Row/Column e destinos de schema, builds de plataforma.

## T34 — indicação da tela inicial

- Dependências: T19, T29.
- Trabalho: seta e rótulo acessível no frame correspondente a entryNodeId, sem relação de navegação artificial.
- Aceite: só a entrada tem seta; mudar SetEntry e desfazer movem o indicador; teste UI e build Wasm.

## T35 — drag-and-drop na árvore

- Dependências: T14, T17, T32.
- Trabalho: reordenar views nas bordas de linhas e reparentear no centro de containers; marca e slot explícitos, autoscroll, cancelamento e um comando por release. Compartilhar UI entre criador e Studio.
- Aceite: arraste real na árvore para Button e entre irmãos, undo/redo, Escape/fora/ciclo/stale sem mutação; slots derivados do schema, seleção e ações preservadas.

## T36 — propriedades e modificadores no Inspector

- Dependências: T09, T27, T35.
- Trabalho: tornar todo o schema descobrível, separar propriedades do componente/Modifier/slots, permitir busca e documentar campos preservados sem suporte autoral. Completar modificadores declarativos de tamanho, posicionamento e aparência com validação no contrato puro e aplicação real no renderer.
- Aceite: testes de schema, Inspector, edição/undo/exportação e medidas do renderer; compilar os targets disponíveis. APIs Compose que exigem callbacks, receivers ou objetos de plataforma precisam de adapters próprios, sem campos fictícios.
