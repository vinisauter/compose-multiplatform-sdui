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
