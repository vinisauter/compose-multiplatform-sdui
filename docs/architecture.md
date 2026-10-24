# Arquitetura e evolução

O [estado atual](current-state.md) descreve o código existente. Os desenhos abaixo orientam a evolução planejada; não representam módulos ou serviços já entregues. O objetivo é estabilizar o cliente, permitir autoria local compatível e só depois acrescentar serviços remotos.

## Limites do produto

O cliente interpreta contratos declarativos usando capacidades instaladas. O Studio edita documentos autorais e compila contratos. Um servidor futuro armazena versões, publica snapshots e resolve dados autorizados. Essas responsabilidades devem evoluir separadamente, com testes de fronteira.

O núcleo compartilhável não conhece Material 3, banco de dados, endpoints de negócio ou credenciais. Ações não executam código arbitrário do payload. Regras do sample permanecem no host. Não é necessário construir servidor ou colaboração para demonstrar renderização, edição, importação e exportação locais.

## Cliente: mudanças incrementais

Preservar a fachada `HummingSDUI`, os nomes `humming-*` e as extensões `SDLibrary`, `ComponentHandler`, `ActionHandler` e `NodeProvider`. Introduzir implementações internas por trás desses pontos antes de trocar APIs públicas.

| Responsabilidade | Local inicial | Evolução planejada |
| --- | --- | --- |
| Modelo/codec | Core atual | Fronteira pura, sem tipos Compose/Lifecycle; adaptador do formato legado |
| Registro | `SDLibrary` | Definições com identificador, parser, schema e handler; schema sem Compose |
| Renderização | Catálogo Material 3 | Continuar resolvendo no catálogo; um `when` local ao catálogo não acopla o core ao Material 3 |
| Execução | ViewModel + biblioteca de ações | Executor suspenso estruturado, contexto por execução e fachada de lançamento na UI |
| Estado | ViewModel atual | Stores com ownership explícito e escopos de tela/jornada; bindings observáveis com descarte |
| Navegação | Biblioteca atual | Adaptador sobre Compose Navigation, estado por entrada e snapshot fixado na jornada |
| Transporte | Esboço/host | Cliente injetado, engines de plataforma e políticas explícitas; resultado por ação |
| Capacidades nativas | Host | Bridge suspensa registrada pelo host, com suporte e cancelamento por plataforma |

Não mover o core inteiro para um módulo “puro”: ele contém helpers e execução visuais. A fronteira deve ser separada no código primeiro. Criar um módulo de contrato apenas quando testes/headless Studio/servidor precisarem consumi-lo, com dependências apontando do runtime e dos hosts para esse contrato. A decisão e a migração pertencem a T06, sem renomeação em massa.

### Definições e schemas

Cada componente/ação deve possuir definição identificável por namespace, parser de propriedades, schema e implementação. O schema descreve tipo, obrigatoriedade, valor padrão, enum, slots, eventos e papel semântico. O parser e o Inspector consomem a mesma descrição quando possível; comportamentos específicos continuam no handler.

O compilador precisa distinguir componente visual, metadado de navegação e ação. A classe wire compartilhada não elimina essa validação. Schemas precisam declarar capacidade/versão; apenas um número global de versão não prova que o cliente consegue executar todos os nós.

### Estado, valores e expressões

Separar `server` (dados resolvidos), `nav` (argumentos de navegação) e `state` (estado editável) por entrada de back stack. `response` e `error` pertencem ao contexto temporário de uma execução de ação. Duas requisições simultâneas não podem escrever implicitamente na mesma variável global de resposta.

Introduzir valores internos tipados com distinção entre ausente, null, string, boolean, número, objeto e array. O adaptador legado preserva o contrato textual. IDs permanecem strings; valores decimais exatos exigem representação e arredondamento explícitos quando o domínio os solicitar.

Expressões são uma linguagem futura, opcional e versionada. Tokenizer, parser com precedência, AST puro e evaluator limitado devem ser testados antes de uso em propriedades. Definir curto-circuito, tipos, acesso ausente, null, coerção, erros e orçamento de operações. Nenhuma reflexão, script ou função arbitrária do payload. Bindings legados `#{chave}` continuam disponíveis pelo caminho legado.

### Execução, recursos e efeitos

Um ponto de entrada de UI lança uma coroutine; dentro dela, sequências e ramos são funções suspensas aguardadas. Cancelamento sempre se propaga. Loading e cleanup precisam de `finally`, inclusive quando callbacks falham. Caracterizar a continuação após falhas no legado antes de mudar: a política pode continuar para contratos antigos e falhar no primeiro erro em um formato novo explicitamente selecionado.

O host é dono do `HttpClient` e o fecha ao encerrar seu ciclo de vida. Engines são específicas de plataforma. Métodos HTTP, headers permitidos, timeout, corpo tipado, resposta e falha são explícitos. O sample usa um endpoint local neutro para validação. Preview do Studio fornece executores controlados para impedir efeitos externos involuntários; modo simulado deve aparecer na UI e não comprova integração real.

### Navegação e cache

Evoluir a navegação existente. Separar definições de tela cacheáveis de dados personalizados. Fixar snapshot da jornada, particionar dados por sessão e definir invalidação, expiração, retorno e cancelamento. Políticas EAGER, LAZY e PREFETCH só entram após cenários e medição; não prometer latência nula. Capacidade nativa indisponível retorna resultado de não suporte, sem sucesso fictício no browser.

## Studio e compilação

O [modelo autoral](studio.md) é um documento normalizado com nós por ID e relações explícitas. Índices e projeção de árvore são derivados; não há duas autoridades mutáveis. Comandos transacionais aplicam edição, geram inversa e incrementam revisão. A árvore de contenção é acíclica; ciclos de navegação são permitidos; ciclos de ações precisam de rejeição ou semântica limitada explícita.

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
