# Decisões técnicas

Atualizado em 2026-10-23, incluindo decisões de T00/T01. Este registro distingue padrões de planejamento de escolhas tomadas durante a implementação. Evidência de execução fica em validação; status e retomada pertencem ao [estado atual](current-state.md), e dependências/critérios ao [plano de desenvolvimento](development-plan.md).

## Como decidir e registrar

Ao executar uma tarefa solicitada, o agente ou desenvolvedor resolve as escolhas técnicas necessárias ao seu recorte, registra a justificativa e verifica o resultado. Uma escolha rotineira não exige nova aprovação humana a cada etapa. Respeite autorizações já dadas. Ampliações materiais de produto e ações externas, de publicação, infraestrutura ou migração de dados reais que ainda não tenham sido autorizadas exigem delimitar a ação e obter a autorização correspondente.

Um padrão deste plano orienta a implementação futura; não comprova código existente nem aprovação humana de toda a plataforma. Uma decisão técnica se torna registrada quando contém contexto, escolha e impacto. Uma funcionalidade só se torna comprovada quando há implementação e evidência correspondente. O [estado atual](current-state.md) e a [validação](validation.md) registram essas evidências em seus respectivos assuntos.

## Padrões adotados no planejamento

| ID | Padrão | Razão e limite |
| --- | --- | --- |
| D01 | Começar por testes JVM e fluxo local do sample desktop | Reduzir o primeiro recorte a um ambiente observável. T00 deve demonstrar sua viabilidade; isso não qualifica os outros targets. |
| D02 | Evoluir os módulos existentes preservando as extensões públicas | Manter `HummingSDUI`, `SDLibrary`, `ComponentHandler`, `ActionHandler` e `NodeProvider` durante a migração. Extrações dependem de fronteiras reais e testes, sem renomeação em massa. |
| D03 | Caracterizar o legado antes de alterar o contrato | Nomes, propriedades, slots, eventos, bindings e ordem de `children` seguem o [contrato](contract.md). Defeitos podem ser corrigidos com impacto e migração explícitos; não são garantias a perpetuar. |
| D04 | Usar execução suspensa estruturada e ownership explícito | Sequências dependentes aguardam seus efeitos; cancelamento se propaga; cleanup e contadores usam `finally`. A política de erro do legado precisa de caracterização antes de mudar. |
| D05 | Associar schemas às definições de componentes e ações | Parser, validação e Inspector devem compartilhar descrições quando possível. Schema não depende de Compose; capacidades e versões precisam de regras verificáveis. |
| D06 | Introduzir valores e escopos internos sem substituir implicitamente o formato textual | Separar estado editável, dados resolvidos, argumentos e resultado temporário de ações. Ausente, null e string precisam de distinção interna; o adaptador legado mantém compatibilidade explícita. |
| D07 | Manter autoria, compilação e runtime separados | O [Studio](studio.md) mantém modelo autoral normalizado; compilador puro produz contrato ou diagnósticos; o renderer continua responsável pela UI. Índices são derivados, sem duas autoridades mutáveis. |
| D08 | Entregar autoria local antes dos serviços remotos | Edição, undo, importação, exportação e preview podem ser verificados sem servidor. O primeiro host planejado é Wasm, condicionado à prova técnica de T16. |
| D09 | Restringir efeitos a capacidades registradas pelo host | Payload não executa código arbitrário. Transporte e bridge têm contratos limitados; preview usa executores controlados e informa quando há simulação. |
| D10 | Separar rascunho mutável, publicação imutável e dados de sessão | Revisões e snapshots pertencem às etapas remotas. Fixar a versão durante a jornada evita mudanças silenciosas no fluxo em andamento. |
| D11 | Acrescentar colaboração após comandos locais e persistência revisada | Deduplicação, replay, concorrência e undo remoto exigem semântica própria. Não presumir que sincronização em memória coordena processos ou resolve conflitos. |

Esses padrões devem ser lidos com as especificações de referência do contrato e do Studio. Regras de ordenação e geometria já propostas no Studio precisam dos testes das tarefas responsáveis; não são APIs implementadas. Novos formatos wire, gramática de expressões, protocolo de colaboração e APIs públicas exigem exemplos, testes e análise de migração antes de estabilização.

## Escolhas a fechar durante as tarefas

Esta tabela delimita perguntas técnicas; não cria outro backlog nem outra lista de aprovação. Consulte o [plano](development-plan.md) para saber quando uma tarefa pode começar e qual evidência a conclui.

| Tarefa | Escolhas necessárias ao recorte |
| --- | --- |
| T00 | Confirmar tarefas, toolchain e dependências reais; escolher fluxo local compatível, comandos e plataforma de observação. Se desktop não for viável, registrar causa e alternativa antes de mudar o alvo. |
| T01 | Selecionar fixtures representativas e incompatibilidades conhecidas; separar comportamento legado observado de comportamento desejado. |
| T02 | Definir estrutura de diagnóstico, nó/caminho/operação, severidade e fallback, diferenciando erro de payload de defeito de programação. |
| T03 | Definir resultado por execução, ordem de ramos aninhados, política de falha por contrato e efeitos de callbacks/cancelamento; testar antes de mudar a continuação legada. |
| T04 | Definir ownership de estado, collectors e eventos; descarte, emissão inicial de `onStateChange` e prevenção de realimentação. |
| T05–T06 | Definir API mínima de schemas, identificação de capacidades e fronteira pura; decidir se um módulo separado é necessário e provar a direção das dependências. |
| T07 | Definir valores, distinção ausente/null, escopos, inicialização, acesso e ciclo de vida por entrada de navegação, com adaptação textual explícita. |
| T08–T09 | Definir sintaxe e seleção explícita das expressões, precedência, tipos/coerções, erros, limites, ciclos e invalidação reativa. Manter `#{chave}` pelo caminho legado. |
| T10 | Definir ownership do cliente HTTP, métodos, headers permitidos, timeout, corpo/resposta e mapeamento de falha por execução. |
| T11 | Definir carregamento de deep links, estado por entrada, chave/partição/expiração do cache, invalidação e restauração; não tratar nó cacheado como dados atualizados. |
| T12 | Definir registro e resultado de capacidades nativas, suporte por plataforma, cancelamento e permissões necessárias ao host. |
| T13–T14 | Definir identidade autoral, relações, chaves de ordenação e comparação; política de esgotamento/rebalanceamento; comandos transacionais, revisão, inversas e limites de undo local. |
| T15 | Definir subconjunto legado exportável, codec explícito, ordenação determinística, limites de expansão e diagnósticos de referências/portas/ciclos. Validar importação e exportação antes de estabilizar o formato autoral. |
| T16–T18 | Demonstrar preview Wasm; definir recursos e efeitos controlados, persistência local, seleção/Inspector, transformação de coordenadas, hit-test e semântica de drag and drop em layouts. |
| T19 | Definir tipos de portas, conexões permitidas, geometria dos conectores e tratamento de ciclos por domínio, sem confundir contenção, navegação e ações. |
| T20–T21 | Definir interfaces da aplicação de servidor, autorização/isolamento e transações com revisão esperada; escolher adaptador e validar a persistência PostgreSQL real. |
| T22 | Definir validação por capacidade, identidade/hash do snapshot, atomicidade, promoção/retorno de versão e separação dos dados personalizados. |
| T23 | Definir fontes registradas, templates/mapeamentos limitados, isolamento de cache, paralelismo, timeout, cancelamento e falhas parciais do BFF. |
| T24 | Definir salvamento remoto com revisão esperada, conflitos, permissões e recuperação de edição sem perda silenciosa. |
| T25 | Definir sequência persistida, deduplicação por comando, replay, reconexão, conflitos e undo concorrente. Selecionar protocolo e transporte com testes entre processos. |
| T26 | Definir a matriz sustentada por execução real, critérios de qualificação, limites conhecidos e evidência de integração em outro host. |

## Registro de decisões tomadas durante a execução

Acrescente apenas escolhas relevantes que alterem esses padrões ou fechem questões da tarefa. Evite copiar o aceite do plano ou o rastreamento do estado atual. Cada entrada deve conter:

- Data, tarefa e problema concreto.
- Escolha, justificativa e alternativas descartadas quando ajudarem a entender o impacto.
- Efeito sobre contrato, exemplos, dependências e migração.
- Evidência executada ou indicação explícita de que a validação ainda está pendente.
- Origem da autorização humana somente quando ela existir; uma decisão do agente deve ser identificada como decisão técnica dentro do recorte solicitado.

### 2026-10-23 — T00: classpath público e inicialização desktop

A compilação do core passou, mas a do sample falhou por falta do supertipo público `androidx.lifecycle.ViewModel` no classpath e dos imports `getValue`/`setValue`. A dependência Lifecycle já existente no core passou de `implementation` para `api`, e os imports foram corrigidos, sem mudar versões.

A primeira execução desktop falhou em `createSavedStateHandle()` por ausência de `SAVED_STATE_REGISTRY_OWNER_KEY`. A escolha foi aceitar factory opcional em `HummingSDUI.Content`, mantendo o caminho padrão do Lifecycle e a assinatura de construção de `Config`. O host JVM fornece handle em memória explicitamente; não há catch/fallback silencioso nem promessa de persistência. Outros hosts mantêm o comportamento padrão e ainda precisam de qualificação.

O modo JVM `--local-smoke` seleciona payload local com textField, binding e ação registrada. O fluxo original continua sendo a entrada padrão. Um teste Compose JVM valida o mesmo payload pelo renderer; sua evidência deve ser registrada em validação. A alteração é decisão técnica no recorte T00 autorizado pelo usuário.

### 2026-10-23 — T01: caracterização e escopo de dependências

Usar `commonTest` para fixtures e lógica portável, executando primeiro em JVM. Coroutines-test foi movido para esse source set após confirmar ausência de uso em produção; dependencyInsight confirmou runtime sem a biblioteca e classpath de testes com ela. Não se alteraram versões.

Instalar Main de teste e manter ViewModels em stores com limpeza explícita evita que collectors do ViewModel sobrevivam às fixtures. Casos de comportamento frágil usam o prefixo `observedLegacy` e são mapeados para tarefas de correção/migração em validação, sem apresentá-los como requisitos desejados. T01 cria evidência do comportamento, sem antecipar correções de T02–T04.

### 2026-10-23 — T02/T03: entrada limitada e execução aguardada

Decisão técnica no recorte autorizado: rejeitar identificadores malformados e eventos parcialmente inválidos com diagnóstico de caminho, mantendo a coerção textual válida do legado. Resolver provider, componente e ação sem sobrescrever a causa original. `executeActions` é a API suspensa; CONTINUE preserva a continuação legada, STOP_ON_ERROR é seleção do host e limites interrompem ambas. Ramos herdam contexto, política e orçamento. Evidência e migração em contrato/validação.

### 2026-10-23 — T04: ownership e origem causal

Decisão técnica: bindings do catálogo são frios e coletados pela composição; consumidores das APIs imperativas antigas têm `releaseBinding`. Ações UI possuem Job da composição e executam no Main.immediate: o teste de navegação demonstrou que o dispatcher da composição, sozinho, não garante a thread exigida pelo Lifecycle. O ViewModel continua dono das fachadas imperativas.

Loading atrasa apenas a exibição (100 ms), cancelando emissão atrasada quando termina a última operação. Eventos preservam emissão inicial; updates idênticos deixam de emitir. Origem de mudanças via contexto suspenso evita variáveis globais de “ação atual” e impede a reentrada do mesmo observador na cadeia, limitada a 64 observadores. Métodos customizados devem usar updateStateFromAction; updates externos iniciam nova origem. O fluxo conflado não promete observar cada mutação concorrente. Não introduz escopos tipados por rota. Testes de descarte, recomposição, ciclos, loading, navegação e recuperação estão em validação.

### 2026-10-23 — T05: schemas com adaptação legada explícita

Decisão técnica no recorte autorizado: metadados e validação ficam inicialmente no core, sem novos módulos ou tipos Compose/Lifecycle nas definições. Cada biblioteca fornece suas definições; NodeCatalog descreve 15 capacidades de schema versão 1, sem introduzir envelope wire ou negociação remota. T06 deve demonstrar consumo puro e decidir a extração necessária, preservando direção de dependências.

O parser conserva strings e ausência de propriedades; defaults não são materializados no payload. Descrições representam defaults calculados e ausência com semântica diferente do valor explícito. LEGACY conserva coerções/fallbacks com warnings; AUTHORING rejeita cobertura desconhecida, campos não editáveis e valores inválidos. Nós conhecidos têm papéis validados em filhos, slots e eventos antes do despacho. Condicionais também validam ramos não selecionados. Métodos precisam continuar registrados e recebem diagnóstico no campo invoke quando desconhecidos.

Gradientes e campos de cor não aplicados pelo TextField não são anunciados editáveis. Componentes/ações fora do subconjunto mantêm a extensão legada. Essa limitação é preferível a um schema que finja cobertura do catálogo inteiro. O contrato inventaria os nomes, políticas e migração. Testes de equivalência textual, limites, papéis, navegação, métodos e um formulário Compose com todo o subconjunto verificam a decisão.

### 2026-10-24 — T06: artefato de contrato puro

O classpath do core impedia um consumidor realmente sem UI. Foi extraído humming-contract com modelo, JSON, diagnósticos, schemas e as definições iniciais, mantendo pacotes públicos. O core exporta o módulo via api e conserva ViewModel, execução, Color e adapters visuais. Dependências apontam do runtime para contrato, nunca o inverso. Não foram criados módulos de servidor ou wrappers duplicados.

Toolchain 25 com bytecode JVM 17 permite consumir o artefato puro no core Android; a compilação desse core comprovou a resolução do variant JVM. IDs usam kotlin.uuid, com a mesma precedência de IDs explícitos e formato prefixado. Funções JSON top-level agora usam NodeJsonKt; consumidores binários antigos precisam ser recompilados. toColor conserva sua facade no core. O módulo não foi publicado; publicação e qualificação completa permanecem etapas próprias.

As 26 regressões puras de parsing/limites foram movidas, sem duplicação, para commonTest do contrato. Novos testes fazem round trip após edição, validação do catálogo e verificam ausência das classes Compose/Lifecycle/Navigation/Material/Skia no runtime JVM. O relatório de dependências mostra apenas Kotlin/serialização/testes. As regressões do core, ações e sample passaram após a extração.

### 2026-10-24 — T13: autoria isolada e codec sem perda silenciosa

A autoria local foi isolada em humming-studio-model → humming-contract para impedir dependência do cliente em editor/undo. O modelo usa mapas por ID; o wire autoral usa arrays ordenados, permitindo rejeitar duplicatas antes de montar os mapas. O codec ordena objetos deterministicamente e preserva arrays. Relações conhecidas são placements/arestas; flags e nodeListShapes conservam ausência, arrays vazios e propriedades de nós codificadas como strings. Campos desconhecidos e extensões permanecem preservados com diagnóstico.

A ponte legada exporta uma raiz e reporta nós não alcançados, sem apagá-los. Navegação explícita e nós compartilhados permanecem no codec autoral, mas não são silenciosamente descartados/duplicados na árvore legada: exigem compilação e política de identidade na etapa responsável. Ciclos de navegação são válidos; ciclos de contenção/ação não são. Ranks iniciais são fixos e válidos; midpoint e mutações revisionadas ficam em T14. Isso não entrega canvas nem servidor.

### 2026-10-24 — T14: comandos por CAS, recibos e histórico limitado

EditSession publica documento/histórico/recibos num AtomicReference comum. Isso mantém o modelo sem dependência de coroutines/UI e impede dois commits na mesma revisão, comprovado com threads JVM. Documento e comandos recebem cópia profunda; falhas só atingem candidatos. A API atômica experimental da stdlib foi isolada internamente e compilou para JS/Wasm; não aparece nas assinaturas públicas.

Midpoint segue os vetores de Studio; limite resulta em erro explícito, sem truncamento/rebalanceamento automático. Vizinhos são IDs adjacentes no container completo após excluir o movido. Undo/redo aplicam patches inversos mantendo revisão monotônica, sem regenerar identidades. Alterações semânticas e ajustes dos hints de representação integram o mesmo commit local de documento.

Histórico e deduplicação têm limites de entradas e caracteres serializados. Requisição repetida retorna o estado atual, com revisão aplicada original; ID reutilizado para outro conteúdo falha. A janela não promete deduplicação persistida nem colaboração entre processos. SetProperties é substituição explícita; PutProperty/RemoveProperty são a API segura para um campo do Inspector, inclusive JsonNull. A declaração inválida de namespace é rejeitada também em inserções programáticas.

## Compilador: autoridade, expansão e limites

Navegação explícita usa uma única autoridade: a aresta de destino; `destiny` simultâneo é conflito. Ações compartilhadas são expandidas com IDs únicos determinísticos e mapa de origem, pois o contrato legado é uma árvore. Ciclos de navegação permanecem válidos; ciclos de ações não. Desconectados exigem rejeição ou aviso solicitado explicitamente. A geração é atômica, limitada por profundidade, quantidade e caracteres; o writer canônico limita a saída durante a escrita, inclusive nos codecs e comandos, em vez de só validar uma string já alocada.

## Host local do preview

Usar LocalPreview separado de App para não herdar providers URL e métodos do sample. O target Wasm abre esse host, com estado em memória e diagnóstico recuperável com nó/caminho. O servidor de desenvolvimento serve somente recursos gerados em loopback. A prova browser não exige atualizar versões; uma instalação incompleta no cache de tooling foi reparada preservando seu lockfile.

## T17 — autoria local e estado do preview

A orquestração pura fica em LocalWorkspace no módulo autoral existente; UI e arquivos ficam no sample, sem um módulo Studio vazio. A árvore é derivada da revisão do documento e observa explicitamente essa revisão na composição. Ações ligadas aparecem abaixo do evento; sua reordenação edita arestas. Desconectados são preservados e diagnosticados, sem coleta automática destrutiva.

O Inspector valida campos editáveis pelo schema compartilhado e preserva dados desconhecidos. Rascunhos importados podem continuar incompletos; o compilador bloqueia a execução incompatível. Preview recompõe com outro ViewModelStore após edição ou abertura, incluindo abertura do mesmo arquivo/revisão; estado de formulário nunca é serializado no documento.

Arquivos no navegador usam seletor e downloads padrão, sem adicionar dependência ou persistência remota. A UI informa solicitação de download e mantém alterações locais indicadas, pois não controla a confirmação de gravação do navegador. A permissão de arquivos locais da extensão de automação não foi ampliada: aceite manual de abertura é suficiente.

## T18 — observação e manipulação geométrica

Observar bounds nos modificadores existentes em vez de envolver componentes em containers adicionais; wrappers podem alterar medida, alinhamento e escopos de layout. O hook pertence à biblioteca visual e é opcional; o modelo geométrico continua puro. O host copia coordenadas e usa identidade de origem do compilador, sem persistir pixels.

Drop considera a região pintada à frente e seus ancestrais elegíveis, impedindo movimentação para um container encoberto por um overlay de outra cadeia. O slot completo resolve vizinhos; igualdade de centro insere depois. Separar modos edição/execução para que seleção/drag não acionem o formulário. Captura é coordenada pelo stream Compose e pointerId; cancelamento limpa estado transitório em finally. Cancelar também quando o foco é perdido.

Primeiro host usa LTR, Row/Column sem wrap/reverse, children e Box append. Zoom dos botões mantém a origem do conteúdo como âncora; a função pura também oferece zoom em ponto arbitrário. Viewport é estado do host e não reseta a cada edição. O aceite visual da guia no navegador é distinto da prova de pixels nos testes Compose; sua confirmação fica rastreada no estado atual.

## T19 — geometria e autoconexão visual

Rejeitar inicialmente a curva visual quando sourceId == targetId, sem alterar a permissão de ciclos de navegação no compilador. Um loop visual exige roteamento e fixture próprios. Geometria permanece pura em commonMain; seta e seleção usam pixels físicos, incluindo densidade exatamente uma vez. Subdivisão tem orçamento explícito e reporta tolerância não atingida, permitindo ao host diagnosticar a aproximação em vez de assumir precisão. Nenhuma coordenada derivada vira autoridade persistida.

## T19 — host de jornada e canvas macro

Exportar jornadas em um envelope local versionado com todos os artefatos evita perder telas e não depende de múltiplos downloads permitidos pelo navegador. O renderer recebe o wire legado via NodeProvider em memória; o envelope não vira um novo componente executável. Árvores continuam exportadas como contrato único.

Derivar conectores dos eventos que alcançam ações de navegação preserva ramos e compartilhamento sem armazenar uma segunda coleção Interaction. O canvas mostra possibilidades, não avalia condições para fingir um caminho executado. GoBack depende do histórico e não recebe destino fixo inventado. Cartões iniciais são recortados em 280×240 dp; origem e borda do destino vêm de medidas reais. Viewport permanece no host entre revisões, separado das posições autorais das telas. Roteamento complexo, conexões por gesto de arraste e expansão automática de cartões ficam fora deste recorte.

## T07 — valores, compatibilidade e lifetimes

Manter LEGACY_SHARED como padrão explícito do host preserva as fixtures que compartilham estado entre telas. ISOLATED_ENTRIES é opt-in e o Studio o ativa; nenhum payload muda essa política. O adapter usa o ViewModelStore real do NavBackStackEntry, preservando retorno e cancelando tarefas no pop em vez de manter um mapa paralelo de telas. Providers e bibliotecas são reutilizados como capacidades; métodos usam o parâmetro owner para acessar dados da entrada. Inputs server/nav vêm de callback confiável do host; nenhuma expressão ou chamada arbitrária é adicionada ao contrato.

Valores tipados ficam no módulo puro já existente. Estado tem uma autoridade tipada e uma projeção textual, sem mapas concorrentes. Null explícito projeta "null"; remoção legada permanece ausente. Números mantêm o lexema para não arredondar IDs/inteiros/decimais na ponte textual, mas sua magnitude inicial é limitada a Double finito; aritmética exata exige outro contrato. Strings nunca são inferidas como números ou booleanos.

response/error pertencem à execução raiz, no contexto suspenso do executor; filhos no mesmo owner herdam os slots. Outra execução/owner recebe novo contexto e finally limpa os slots inclusive em cancelamento. Erro visual continua propriedade da entrada, não um alias de response/error. Inputs e estado são mantidos apenas em memória, sem promessa de restauração após morte do processo. Retorno na pilha preserva o owner; descarte o libera. Persistência/cache versionado são matéria de T11/T26, sem fingir suporte por existir SavedStateHandle.

## T08 — linguagem explícita e evaluator puro

Usar ExpressionSpec com language=humming.expression e version=1 evita reinterpretar qualquer string/binding legado. Parser Pratt com tokenizer ASCII/strings JSON implementa precedência e AST privado; não existe chamada de função, reflexão ou atribuição. Somente state/server/nav/response/error são raízes; acesso a objetos/arrays é tipado e limitado. Missing/Null propagam ausência e coalesce é preguiçoso; operadores booleanos exigem Boolean e têm curto-circuito.

Aritmética é Double finito com rejeição de inteiros fora do intervalo seguro antes/depois de operar; isso impede arredondamento silencioso de inteiros grandes preservados em SduiValue. Igualdade de containers é rejeitada, sem travessia recursiva oculta. Resultado numérico é comparado por valor, sem exigir a mesma formatação de Double nos targets. IO, métodos e código externo não são capacidades do evaluator. Callback de cancelamento é do chamador confiável e suas exceções propagam sem conversão em erro de payload.

Parser limita fonte/tokens/profundidade e evaluator limita visitas/profundidade/texto. Acesso de índice em base Missing/Null também faz curto-circuito. O programa não guarda resultados/contexto entre chamadas. Não se conectou essa linguagem ao schema de propriedades nem ao runtime visual: T09 precisa de dependências reativas, opt-in por capacidade e política de apresentação/validação antes dessa integração.
