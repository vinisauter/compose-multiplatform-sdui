# Arquitetura e evolução

O [estado atual](current-state.md) descreve o código existente. Os desenhos abaixo orientam a evolução planejada; não representam módulos ou serviços já entregues. O objetivo é estabilizar o cliente, permitir autoria local compatível e só depois acrescentar serviços remotos.

## Limites do produto

O cliente interpreta contratos declarativos usando capacidades instaladas. O Studio edita documentos autorais e compila contratos. Um servidor futuro armazena versões, publica snapshots e resolve dados autorizados. Essas responsabilidades devem evoluir separadamente, com testes de fronteira.

O núcleo compartilhável não conhece Material 3, banco de dados, endpoints de negócio ou credenciais. Ações não executam código arbitrário do payload. Regras do sample permanecem no host. Não é necessário construir servidor ou colaboração para demonstrar renderização, edição, importação e exportação locais.

## Cliente: mudanças incrementais

Preservar a fachada `HummingSDUI`, os nomes `humming-*` e as extensões `SDLibrary`, `ComponentHandler`, `ActionHandler` e `NodeProvider`. Introduzir implementações internas por trás desses pontos antes de trocar APIs públicas.

| Responsabilidade | Local inicial | Evolução planejada |
| --- | --- | --- |
| Modelo/codec/schemas | humming-contract, sem tipos Compose/Lifecycle | Consumidores de autoria/compilação usam essa fronteira; adapters visuais ficam nas bibliotecas |
| Registro | `SDLibrary` com handlers e definições versionadas; subconjunto inicial acrescido de reactiveText opt-in | Ampliar definições sem acoplar seus metadados a tipos UI |
| Renderização | Catálogo Material 3 | Continuar resolvendo no catálogo; um `when` local ao catálogo não acopla o core ao Material 3 |
| Execução | Executor suspenso no core, contexto/política por execução e fachada de lançamento | Novos efeitos usam a mesma execução estruturada |
| Estado | ViewModel textual; bindings frios no catálogo e descarte imperativo explícito | Stores tipados e escopos de tela/jornada |
| Navegação | Biblioteca atual | Adaptador sobre Compose Navigation, estado por entrada e snapshot fixado na jornada |
| Transporte | Esboço/host | Cliente injetado, engines de plataforma e políticas explícitas; resultado por ação |
| Capacidades nativas | Host | Bridge suspensa registrada pelo host, com suporte e cancelamento por plataforma |

A necessidade de um consumidor JVM sem UI justificou humming-contract. Ele contém modelo, JSON, diagnósticos e schemas, e depende somente de Kotlin/serialização. humming-core o exporta via api; ViewModel, efeitos, Color e os handlers visuais continuam no runtime. Bibliotecas e hosts apontam para o contrato, sem dependência inversa. Pacotes públicos foram preservados; a migração de facades JVM top-level requer recompilar consumidores, conforme contrato. O módulo puro usa toolchain 25 e bytecode JVM 17, consumível pelo core Android sem converter o contrato em módulo de UI.

### Definições e schemas

NodeDefinition/PropertyDefinition e NodeCatalog implementam a descrição compartilhada do subconjunto inicial, inventariado no contrato. O schema descreve tipo, obrigatoriedade, defaults, enums, slots, eventos e papéis; o parser preserva strings legadas para os adapters. Metadados, validação e as definições iniciais residem em humming-contract. Seu consumidor de teste JVM valida e serializa sem classes UI disponíveis; o core e os adapters continuam compilando sobre a mesma API. O Inspector local consome essas definições e deve continuar a usá-las e usar o modo AUTHORING; comportamentos visuais específicos continuam no handler.

O compilador precisa distinguir componente visual, metadado de navegação e ação. A classe wire compartilhada não elimina essa validação. Schemas precisam declarar capacidade/versão; apenas um número global de versão não prova que o cliente consegue executar todos os nós.

### Estado, valores e expressões

Separar `server` (dados resolvidos), `nav` (argumentos de navegação) e `state` (estado editável) por entrada de back stack. `response` e `error` pertencem ao contexto temporário de uma execução de ação. Duas requisições simultâneas não podem escrever implicitamente na mesma variável global de resposta.

SduiValue distingue ausente, null, string, boolean, número, objeto e array. O adaptador legado preserva o contrato textual. IDs permanecem strings; valores decimais exatos exigem representação e arredondamento explícitos quando o domínio os solicitar.

ExpressionSpec/ExpressionProgram formam uma linguagem pura, opcional e versionada no contrato. Tokenizer, parser com precedência, AST privado e evaluator limitado são independentes do runtime; a integração a propriedades pertence à T09. Definir curto-circuito, tipos, acesso ausente, null, coerção, erros e orçamento de operações. Nenhuma reflexão, script ou função arbitrária do payload. Bindings legados `#{chave}` continuam disponíveis pelo caminho legado.

### Execução, recursos e efeitos

As ações do catálogo usam um scope cancelado junto com o componente. `executeActions` troca para `Dispatchers.Main.immediate`, preservando o Job chamador e o contexto de execução; isso mantém a afinidade exigida por Lifecycle/Navigation. Sequências e ramos são suspensos e aguardados. CONTINUE é o padrão legado e STOP_ON_ERROR é seleção explícita do host. Cancelamento propaga e contadores/cleanup finalizam mesmo em erro. Bindings frios são coletados pela composição; APIs imperativas antigas possuem descarte explícito. Eventos de estado carregam origem causal pelo contexto da coroutine; métodos customizados usam updateStateFromAction para participar da prevenção de realimentação.

O host é dono do `HttpClient` e o fecha ao encerrar seu ciclo de vida. Engines são específicas de plataforma. Métodos HTTP, headers permitidos, timeout, corpo tipado, resposta e falha são explícitos. O sample usa um endpoint local neutro para validação. Preview do Studio fornece executores controlados para impedir efeitos externos involuntários; modo simulado deve aparecer na UI e não comprova integração real.

### Navegação e cache

Evoluir a navegação existente. Separar definições de tela cacheáveis de dados personalizados. Fixar snapshot da jornada, particionar dados por sessão e definir invalidação, expiração, retorno e cancelamento. Políticas EAGER, LAZY e PREFETCH só entram após cenários e medição; não prometer latência nula. Capacidade nativa indisponível retorna resultado de não suporte, sem sucesso fictício no browser.

## Studio e compilação

O [modelo autoral](studio.md) está implementado em humming-studio-model: documento normalizado, codec determinístico e ponte legada. Esse módulo depende de humming-contract; o runtime não depende do modelo autoral. Comandos revisionados e undo/redo locais estão implementados em EditSession; ContractCompiler implementa a compilação semântica local, enquanto seleção/drop e o grafo visual são integrados pelo host. O documento possui nós por ID e relações explícitas. Índices e projeção de árvore são derivados; não há duas autoridades mutáveis. Comandos transacionais aplicam edição, geram inversa e incrementam revisão. A árvore de contenção é acíclica; ciclos de navegação são permitidos; ciclos de ações precisam de rejeição ou semântica limitada explícita.

O compilador é puro: documento + schema/catálogo + capacidades de destino → contrato determinístico ou diagnósticos. Ele monta `children` e eventos ordenados, valida referências/portas e limita expansão. Na primeira entrega, produz apenas o subconjunto legado suportado; rede, expressões e bridge não são pré-requisitos para editar uma tela local.

O Studio reutiliza o catálogo e o renderer do cliente. Seleção, Inspector, bounds e overlays ficam na camada de autoria. Coordenadas livres do canvas macro não viram obrigatoriamente coordenadas absolutas na UI final: Row/Column/Box compilam sua própria semântica. O primeiro host do Studio será Wasm conforme [decisões](decisions.md), sujeito à prova de build em T16; testes puros continuam executáveis em JVM.

## Serviços remotos posteriores

Criar módulos somente nas etapas correspondentes. Nomes abaixo descrevem responsabilidades, não imports existentes:

| Camada futura | Depende de | Não deve depender de |
| --- | --- | --- |
| Contrato/compilador puro | Kotlin e serialização adequados ao recorte | Compose, drivers de banco, framework HTTP |
| Aplicação de servidor | Contrato, interfaces de repositório e políticas | Implementação concreta de banco |
| Adaptador PostgreSQL | Interfaces da aplicação e driver | Rotas HTTP e Studio |
| Host servidor | Aplicação, adaptadores e configuração | Estado mutável compartilhado com o cliente |
| Host Studio | Modelo autoral, compilador e renderer | Acesso direto ao banco |

O composition root do host conecta as interfaces às implementações, evitando ciclo servidor ↔ persistência. Validar PostgreSQL real nas integrações; banco substituto não comprova semântica de revisão/transação em produção.

Rascunhos são mutáveis com `expectedRevision`; publicação cria snapshot imutável validado contra capacidades do cliente. Dados personalizados ficam fora do snapshot. O cliente nunca muda de versão silenciosamente no meio de uma jornada.

O BFF futuro resolve fontes previamente registradas, templates e mapeamentos limitados. Não é um proxy de URL/headers arbitrários do contrato. Exige autorização por tenant, isolamento de cache, limites de paralelismo, timeout, cancelamento e política de falhas parciais. Transporte inicial pode ser HTTP simples; GraphQL e federação não são pré-requisitos do editor ou do cliente.

Colaboração vem depois de edição local e persistência com revisão. Um `Mutex` no processo não coordena réplicas. `commandId`, revisão esperada e sequência devem ser persistidos transacionalmente, com deduplicação, replay e recuperação após desconexão. Undo local precede undo concorrente; este exige política de conflitos própria. Não presumir CRDT, entrega exatamente uma vez ou aceitação de chaves concorrentes iguais.

## Marcas de entrega

O [plano](development-plan.md) define entregas independentes: runtime verificável; autoria local; cliente com capacidades adicionais; publicação remota; colaboração. Cada uma requer evidência adequada em [validação](validation.md). Nenhuma etapa remota deve ser usada para adiar a demonstração local de uma capacidade já implementável.

## Fronteira do compilador autoral

`humming-studio-model` depende apenas do contrato puro e contém documento, codecs, comandos/histórico e `ContractCompiler`. O compilador recebe capacidades do destino e produz JSON mais mapa de origem, sem Compose, IO ou registro de handlers. O host integra esses artefatos ao NodeProvider existente. Em T15 o sample consumia o módulo autoral somente em jvmTest. O host de edição LocalStudio agora o consome em commonMain; o runtime não depende do modelo autoral. O aceite de cada recorte do editor está no estado atual.

## Bounds e interação do host

A biblioteca de layout expõe LocalNodeBoundsObserver sem dependência do modelo autoral. O sample observa medidas reais por ID, cria LayoutSnapshot puro e consulta DropGeometry/DropGesture. Overlay, foco, viewport e entrada de ponteiro ficam no host; edição usa o mesmo EditSession do Inspector. A medição não acrescenta nós visuais nem transfere componentes Material 3 para o core.

## Ownership dos dados do cliente

humming-contract contém SduiValue e os inputs tipados sem dependência UI. humming-core mantém o mapa de estado e sua visão textual no mesmo owner, além de ActionValues no contexto da execução. O contexto distingue owners para que uma chamada a outro ViewModel não herde os resultados/política da tela anterior.

No modo ISOLATED_ENTRIES, Graph pede um owner ao ViewModelStore de cada NavBackStackEntry. Bibliotecas/providers registrados são capacidades compartilhadas; o formulário não é compartilhado. Entradas fora de foco retidas na pilha preservam estado; o descarte pelo Navigation cancela o owner e limpa dados. LocalScreenFeedbackHandler encaminha UI de erro/loading do host ao owner correto, sem dependência de Material no core. A seleção LEGACY_SHARED preserva o caminho anterior; cache de nós continua sendo definição, não store de dados personalizados ou snapshot persistido.

## Transporte e tipos públicos do runtime

humming-network é um adaptador opcional do core para Ktor, sem engine, UI ou backend próprio. HttpClient e registro fixo de endpoints pertencem ao host; SDNetwork fornece ação estruturada com ramos aguardados e resultados em ActionValues. Definições/schema ficam no contrato puro sem instalar o handler no catálogo inicial. Studio permanece local e não recebe capacidades de rede implicitamente.

O core exporta explicitamente os artefatos AndroidX Lifecycle que definem ViewModel e SavedStateHandle na sua API pública. Eles já eram resolvidos por Navigation 2.9.2 como Lifecycle 2.9.4; a declaração API corrige sua ausência no metadata dos consumidores. O wrapper JetBrains 2.9.6 resolvido pelo projeto contém EmptyFile.kt no commonMain e, isoladamente, não exporta esses tipos. Não se elevou a versão resolvida para contornar o erro. Warnings de KLIB duplicado existentes devem ser tratados em uma revisão própria de dependências.

O módulo navigation também fornece JourneyDefinitionCache para definições imutáveis qualificadas por sessão/versão. Seu lifecycle é um SupervisorJob filho do escopo do host; estado e inputs da entrada continuam fora do cache. SDNavigation recebe opcionalmente JourneyDefinitions, com identidade imutável e loader explícito do host. Graph consulta esse cache em cada navegação e mantém separadamente as definições exibidas; rotas sem preload carregam no lifecycle da composição/owner. A API não instala transporte nem persiste estado. O host fecha/invalida o cache e descarta owners ao trocar de sessão; aceite e ponto de retomada estão no estado atual.
