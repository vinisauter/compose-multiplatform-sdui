# Contrato declarativo e compatibilidade

Este documento separa o formato existente das extensões planejadas. O [estado atual](current-state.md) aponta para os arquivos que fundamentam a leitura. Parsing limitado e execução estruturada possuem regressões em JVM; cobertura e limitações estão em [validação](validation.md). Isso não comprova suporte em outros targets.

## Formato atual

A raiz é um nó, sem envelope ou negociação de versão implementados. A estrutura pública é:

```kotlin
@Serializable
open class ServerDrivenNode(
    val component: String,
    val id: String = "$component:${getPlatform().generateUUID()}",
    val properties: MutableMap<String, String?>? = null,
    val children: MutableList<ServerDrivenNode>? = null
)
```

`component` identifica `namespace:nome`, com diferenciação entre maiúsculas e minúsculas. ID omitido é gerado durante parsing; não é identidade estável entre carregamentos. A ordem dos arrays de `children` e de ações tem significado. O formato do runtime atual é uma árvore aninhada; o modelo normalizado do Studio é uma representação autoral separada, compilada para essa árvore.

### Parser e serializer

O parser de `NodeDecoder.kt` e `NodeJson.kt` aceita propriedades JSON primitivas, objetos e arrays, embora o modelo público as armazene como strings:

| Entrada em `properties` | Representação observada após parsing |
| --- | --- |
| `"abc"` | String `abc` |
| `12`, `true` | Strings `12`, `true` |
| Objeto ou array | Texto JSON correspondente |
| `null` | String `"null"`; não equivale à ausência nem ao null Kotlin |

`parseNode(source, limits, documentId)` valida raiz, identidade, propriedades, filhos e orçamento antes de materializar nós. Identificadores exigem exatamente dois segmentos não vazios, sem espaços. `propertyNodes` aceita objeto único/array e rejeita elementos não objeto com diagnóstico do caminho; `propertyNode` rejeita mais de um nó. Entradas inválidas antes toleradas precisam ser corrigidas no produtor, sem descarte silencioso.

Não presumir equivalência entre parser manual, serializer gerado e `toString()`. T01 confirmou que `JsonServerDrivenNode.toString()` devolve o JSON original sem refletir uma mutação posterior em propriedades. As fixtures cobrem round trip do JSON não editado e do modelo textual serializável, sem afirmar equivalência entre esses dois codecs. LegacyAuthoringCodec e ContractCompiler oferecem exportação explícita após edição.

## Registro e papéis

Pontos de extensão a preservar:

```kotlin
typealias ComponentHandler = (ServerDrivenNode, SDUIFormViewModel) -> ComposableContent
typealias ActionHandler = suspend (ServerDrivenNode, SDUIFormViewModel) -> Unit
typealias NodeProvider = suspend (String) -> ServerDrivenNode
```

`SDLibrary(namespace)` resolve componentes e ações. Métodos invocados por ações devem estar previamente registrados no cliente. Providers resolvem uma origem pelo tipo registrado pelo host, sem pressupor disponibilidade universal de `file`, `url` ou qualquer integração externa.

`HummingSDUI.Content(config, savedStateHandleFactory = ...)` aceita uma factory opcional para hosts sem `SavedStateRegistryOwner`. Ela é usada na criação do ViewModel. Sem factory, permanece `createSavedStateHandle()` do Lifecycle e seus requisitos de owner. O sample JVM fornece `SavedStateHandle()` em memória; isso não implementa persistência do formulário entre processos. A estrutura e a trailing lambda de `Config` permanecem iguais.

Um nó wire pode representar componente visual, ação ou metadado, mas esses papéis não são intercambiáveis. Schemas futuros devem validar o papel, slots, eventos e filhos permitidos antes de compilar ou executar. O core continua extensível por bibliotecas, sem lista fixa de componentes Material 3.

## Nomes e propriedades a preservar

| Área | Contrato existente relevante |
| --- | --- |
| Bindings | `#{chave}` para substituição textual |
| Campo de texto | `layout:textField`, `onChangeUpdateState`, `visualMask`, `keyboardType` |
| Conteúdo de botão | Conteúdo visual em `children`; ações em `properties.onClick` |
| Slots/eventos | Propriedades podem conter nó único ou array, conforme helper/handler; não mover ações para filhos visuais |
| Layout | Propriedades como `paddingAll`, `paddingHorizontal` e enums como `Center`/`Start`; não substituir por tokens de outro vocabulário silenciosamente |
| Grafo | `navigation:graph` com `startDestination`; filhos `navigation:node` com `name`, `type`, `destiny`, `deepLinks?` |
| Ir para rota | `navigation:goTo` com `destiny`, `popStack`, `clearStack` |
| Voltar | `navigation:goBack` |
| Atualizar estado | `action:update` com `state`, `value`, `delay?` |
| Método | `action:method` lê `invoke`, que identifica método registrado; a propriedade antiga `method` em alguns exemplos precisa de correção |
| Condição | `action:ifState` com `state`, `equals`, `then`, `else` |
| Ramo condicional | `action:then` lê `condition` como nome de estado booleano e executa nós de `invoke` quando verdadeiro; ausência resulta em false |
| Eventos | `action:event` com `onCreate`, `onStateChange` |

Exemplo mínimo do formato legado a caracterizar e usar no fluxo local:

```json
{
  "component": "layout:button",
  "id": "submit-button",
  "properties": {
    "onClick": [
      {
        "component": "action:update",
        "id": "set-status",
        "properties": {"state": "status", "value": "enviado"}
      }
    ]
  },
  "children": [
    {"component": "layout:text", "id": "submit-label", "properties": {"text": "Enviar"}}
  ]
}
```

Este exemplo ilustra a estrutura. T00 executou um payload local equivalente em forma de campo/binding/ação pelo catálogo, e T01 caracterizou eventos objeto/array e separação de conteúdo/ação. Isso não significa que cada exemplo deste documento foi executado literalmente. O subconjunto com schema está inventariado abaixo; os demais componentes mantêm o caminho legado e não são declarados editáveis. Exemplos com integrações ausentes não são testes de aceitação.

## Schemas do catálogo inicial

[NodeDefinition.kt](../humming-contract/src/commonMain/kotlin/me/developes/humming/sdui/common/NodeDefinition.kt) define metadados sem tipos Compose/Lifecycle. `NodeDefinition` identifica nome, papel, versão de schema, propriedades, papel dos filhos e relações entre campos. `PropertyDefinition` descreve tipo, obrigatoriedade, default ou descrição do default calculado, enum, bindings, fallback legado e possibilidade de edição. Eventos e slots usam `PropertyKind.NODES` com papel esperado; arrays e objetos de evento válidos mantêm sua ordem.

`SDLibrary.addDefinition` registra uma definição do mesmo namespace, sem substituir as APIs de handlers. `SDUIFormViewModel.catalog()` oferece um snapshot `NodeCatalog`; `capabilities` retorna nome → versão de schema, com ordem determinística. É descrição local do subconjunto, não negociação de versão wire nem garantia de que providers, métodos e destinos estejam disponíveis. Em particular, os handlers goTo/goBack continuam instalados quando o grafo é criado.

| Definições | Responsável |
| --- | --- |
| `layout:text`, `textField`, `button`, `row`, `column`, `box`, `reactiveText`, `reactiveButton`, `validatedTextField` | [LayoutDefinitions](../humming-contract/src/commonMain/kotlin/me/developes/humming/sdui/layout/LayoutDefinitions.kt) |
| `action:update`, `method`, `ifState`, `then`; componente `action:event` | [ActionDefinitions](../humming-contract/src/commonMain/kotlin/me/developes/humming/sdui/action/ActionDefinitions.kt) |
| `navigation:graph`, `node`, `goTo`, `goBack` | [NavigationDefinitions](../humming-contract/src/commonMain/kotlin/me/developes/humming/sdui/navigation/NavigationDefinitions.kt) |

São 18 definições: as 15 originais e reactiveButton na versão 1; reactiveText e validatedTextField na versão 2, com tokens declarados. `parse(node)` produz `ParsedNodeProperties`: strings originais, diagnóstico por campo e `requireValid()` para rejeitar erros. Os adapters modelados usam esse parser; conversões finais para tipos Compose permanecem no catálogo visual. Defaults informam o Inspector, sem serem injetados no payload: ausência, string vazia e valor explícito continuam distintos. FontWeight.Normal, estado TextField_<id>, limites derivados da máscara e scroll por presença possuem descrição explícita em vez de um default wire inventado. O parser é compartilhável por código; não há exportação JSON Schema entregue.

### Validação e compatibilidade

`NodeCatalog.validate(node, expectedRole, mode)` valida árvore, slots e eventos. O runtime usa LEGACY: fallback já tolerado pelo renderer vira warning, desconhecidos continuam no caminho legado, erros de propriedade/papel impedem o despacho. `ValidationMode.AUTHORING` rejeita propriedades ou nós sem schema, campos não editáveis e valores inválidos que o legado tolerava. Warnings são retornados pela API de validação; não abrem errorState automaticamente.

Exemplos preservados: Row usa fallback para enum desconhecido; Column rejeita esse enum; Boolean legado mantém sua coerção; enabled dinâmico aceita `#{chave}` sem avaliá-lo durante o schema. Cores aceitam hex RGB/ARGB/RRGGBB/AARRGGBB, com # opcional. minLines/maxLines têm limites e relação de ordem quando singleLine não é true. Navegação exige nomes de rota únicos e startDestination existente. Deep links mantêm coerção de primitivas/remoção de null no LEGACY e exigem strings na autoria. JSON textual dessas listas é limitado antes do parser. A travessia usa orçamento padrão de 10.000 nós e profundidade 64; também interrompe ciclos de objetos construídos pelo host.

Migração: ações ou metadados colocados em posições visuais, componentes em eventos, valores antes falhando só no renderer e grafos inconsistentes passam a gerar diagnóstico localizado antes do handler. A validação percorre inclusive ramos condicionais não selecionados; produtores devem corrigir esses ramos. Método explicitamente desconhecido produz MISSING_METHOD no campo invoke; invoke ausente conserva o no-op legado. Argumentos extras de métodos customizados permanecem aceitos no LEGACY, mas precisam de schema específico antes de serem apresentados como campos suportados de autoria.

### Inventário fora da edição inicial

Continuam registrados e executáveis pelo caminho legado, sem schema editável: layout:animatedVisibility, image, outlinedButton, textButton, iconButton, icon, scaffold, topAppBar, divider e style; action:remove, not, and, or, xor, equals, greaterThan, lessThan, greaterThanOrEquals, lessThanOrEquals, isEmpty e isNotEmpty. Bibliotecas customizadas sem definição seguem a mesma regra.

Os modificadores escalares usados pelos seis componentes possuem metadados. backgroundGradient é opaco e não editável; seu parser legado continua responsável pelos detalhes internos. textFieldColor, trailingIconColor e underLineColor são lidos pelo código antigo sem aplicação visual e também não são editáveis. Isso não implica suporte à edição de todos os estilos, gradientes ou campos de componentes legados.

## Estado e execução atuais

O estado é textual no ViewModel. `getState` pode inicializar chave ausente, `updateState(key, null)` remove e o binding de chave ausente pode criar valor vazio. Não existe linguagem tipada nem garantia de avaliação recursiva. `SavedStateHandle` recebido não comprova persistência do mapa entre recriações.

`executeActions` é suspenso, executa em `Dispatchers.Main.immediate` preservando o Job chamador e aguarda handlers e ramos aninhados. Retorna `ActionExecutionResult(attempted, errors)`. `ActionFailurePolicy.CONTINUE` preserva a continuação legada; `STOP_ON_ERROR` é uma seleção explícita do host. Chamadas aninhadas herdam política e limites: 10.000 tentativas e profundidade 64 por padrão. Excesso de orçamento interrompe a sequência nas duas políticas. Cancelamento propaga; falhas de handlers conservam diagnóstico e causa, enquanto `Error` propaga.

`invokeActions` e `runTask` são fachadas que retornam `Job` no scope do ViewModel. Handlers que precisam de ordem devem aguardar `executeActions`, sem lançar outra execução. Callbacks de encerramento são finalizados em contexto não cancelável e preservam a falha original. Navegação aguarda o provider pelo caminho `navigateToAwaited`.

### Ownership de bindings e eventos

`binding(value, calculation)` e `stateBinding(key, initialValue)` produzem `NodeBinding` frio. `updates` só assina enquanto coletado; `collectAsState()` pertence à composição. Constantes não assinam mudanças do formulário. Os antigos `getStateFlow` e `resolveStatePlaceholdersFlow` permanecem disponíveis; consumidores imperativos devem chamar `releaseBinding(flow)` ao descartá-los. O ViewModel cancela os restantes ao ser encerrado.

As ações iniciadas pelos componentes do catálogo são canceladas quando sua composição é removida. Busy pertence ao componente e não altera o valor de enabled vindo do binding. `invokeActions` continua com ownership do ViewModel para consumidores imperativos que precisam dessa duração.

Loading aparece depois de 100 ms de atividade contínua, permanece ativo enquanto qualquer operação contada estiver executando e desaparece sem atraso quando a última termina. Uma operação curta não deixa uma emissão atrasada de loading.

`updateState` não emite quando o valor não muda ou quando remove uma chave ausente. `action:event` observa uma emissão inicial por entrada na composição; `onCreate` e seu observador de mudanças são efeitos canceláveis. Ações do catálogo preservam a cadeia causal com `updateStateFromAction`: um mesmo observador não reage novamente à sua própria cadeia de mudanças, inclusive através de outro observador. Cadeias têm limite de 64 observadores. Ações customizadas suspensas devem usar essa API para participar da prevenção de ciclos; `updateState` representa uma nova origem externa. O fluxo de revisões é conflado e não promete entrega individual de todas as mudanças concorrentes. LEGACY_SHARED mantém o estado no ViewModel do grafo; ISOLATED_ENTRIES usa um ViewModel por entrada de navegação, conforme a seção abaixo. Snapshot/cache versionado da jornada permanece fora desse isolamento local.

## Fronteira pura de contrato

[humming-contract](../humming-contract/build.gradle.kts) contém modelo, codec, limites, diagnósticos, schemas e definições iniciais. Depende somente de Kotlin e kotlinx.serialization. `initialNodeCatalog()` oferece o inventário de schemas sem instalar handlers, providers ou efeitos. `humming-core` o exporta com api(project), preservando os pacotes/imports das APIs; catálogo Material 3, ViewModel, execução e Color continuam fora do módulo puro.

O consumidor JVM headless valida payloads e serializa `ServerDrivenNode.serializer()` sem Compose, Lifecycle, Navigation, Material ou Skia no classpath. A serialização explícita do modelo reflete edições; `JsonServerDrivenNode.toString()` continua devolvendo o JSON original. O codec autoral e o compilador permanecem etapas separadas.

Migração de build: adicionar dependência direta de humming-contract em consumidores sem UI; consumidores do runtime continuam usando humming-core. Não há tipo duplicado nem árvore de modelo paralela. IDs omitidos continuam no formato component:UUID, agora gerados por kotlin.uuid no módulo puro, sem depender de Platform; IDs explícitos são preservados.

Os pacotes/classes públicos permanecem iguais, mas funções top-level de JSON passam da facade JVM ServerDrivenJsonKt para NodeJsonKt. Consumidores JVM previamente compilados devem ser recompilados junto com os módulos; não se afirma compatibilidade binária com um artefato externo antigo. toColor permanece no core com sua facade anterior. Nenhum artefato foi publicado nesta migração. A configuração de publicação deverá incluir o novo módulo antes de qualquer release futuro.

## Extensões planejadas e migração

Ainda não estão implementados: envelope/versionamento wire, schemas para todo o catálogo, negociação de capacidades, valores tipados, expressões, escopos `server/nav/state`, resultados `response/error` por execução e novos contratos de rede/jornada. Os nomes descritivos do planejamento não são sintaxe JSON já aceita.

Para introduzir qualquer extensão:

1. Definir schema, semântica, capacidade/versão e exemplo mínimo na tarefa responsável.
2. Criar fixtures do legado e do novo formato, incluindo falha e limites.
3. Implementar adaptador ou migrador explícito; não inferir formato por uma propriedade ambígua.
4. Manter o caminho legado e avaliar depreciação com os consumidores existentes.
5. Fazer o Studio emitir somente capacidades suportadas pelo destino, com diagnósticos para incompatibilidades.
6. Atualizar este contrato, exemplos e evidência de execução.

Novos formatos de tokens reativos, temas e máscaras exigem versionamento ou extensão declarada; as propriedades legadas, incluindo `visualMask`, continuam preservadas. Valores numéricos não devem converter IDs, strings vazias, null ou booleanos por heurística. Se houver sintaxe de sufixos para tipos em corpos HTTP, decidir colisões de nomes, arrays, objetos e null antes de adotá-la; ela não integra o contrato atual.

## Limites de entrada

`PayloadLimits` limita texto a 2.000.000 caracteres UTF-16, profundidade JSON a 64, objetos com `component` a 10.000, valores JSON a 100.000 e propriedades/strings a 65.536 caracteres por padrão. `JsonObject.toNode()` valida a árvore já construída; use `parseNode` para limitar também o texto antes do parser. `NodeDiagnostic` inclui código, severidade, operação, caminho, documento opcional e nó quando conhecido. Fallback de resolução: retornar null e expor erro ao host, preservando causa; não substituir erro de componente por provider ausente. O subconjunto descrito neste documento já valida papel e propriedades; ampliar a cobertura exige definição e fixture próprias.

## Compilação de documentos autorais

O módulo puro `humming-studio-model` oferece `ContractCompiler` e `TargetCapabilities`. A saída reutiliza o contrato existente de `navigation:graph`, `navigation:node`, `navigation:goTo` e ações; não introduz execução arbitrária nem novo formato no renderer. Versões dos schemas devem coincidir exatamente com as capacidades declaradas. Providers e métodos precisam ser registrados pelo host. A declaração de provider externo não comprova disponibilidade de seus recursos; jornadas locais compiladas fornecem o mapa de artefatos para o host resolver.

## Edição local de propriedades

LocalWorkspace usa PropertyDefinition para autorizar campos escalares e valida com AUTHORING antes de publicar o comando. O Inspector grava valores textuais compatíveis com o wire legado; STRING_ARRAY passa pelo parser JSON limitado. Não injeta defaults nem apaga propriedades desconhecidas. Remover o valor é comando distinto de definir string vazia. Slots/eventos são relações do documento autoral e não podem ser sobrescritos como escalares pelo Inspector. Nenhum nome, binding ou sintaxe wire foi alterado por essa integração.

## Valores internos e estado por entrada

SduiValue distingue Missing, Null, Text, BooleanValue, NumberValue, ObjectValue e ArrayValue no módulo puro humming-contract. parseSduiValue respeita PayloadLimits, preserva strings como IDs e valida números pela gramática JSON e magnitude finita em Double. NumberValue mantém o lexema exato, inclusive zeros decimais e inteiros acima da precisão de Double; não implementa aritmética ou arredondamento. Número fora da magnitude suportada gera VALUE_NUMBER com caminho. Missing é ausência: não é membro JSON nem item de array e não pode ser serializado como null.

O mapa tipado stateValues é a autoridade do ViewModel. stateValue devolve Missing para uma chave ausente; updateValue com Missing remove a chave e com Null mantém null explícito. formState continua uma visão textual viva: Text conserva o conteúdo, números conservam o lexema, booleanos/objetos/arrays/null usam texto JSON. updateState(key,null) continua removendo; updateState com string continua Text, sem inferência de tipo. Bindings #{chave} e seus defaults preservam a semântica anterior; não ganharam expressão ou acesso a escopo por heurística.

HummingSDUI.Config.screenStateMode é seleção do host: LEGACY_SHARED é o padrão compatível; ISOLATED_ENTRIES cria um SDUIFormViewModel no ViewModelStore de cada NavBackStackEntry. Duas entradas da mesma rota recebem IDs/estado distintos. entryInputs(entryId,route) fornece ScreenInputs com server/nav tipados e imutáveis por entrada; não copia implicitamente o formulário do grafo. state é editável; server/nav são inputs somente leitura. Métodos registrados recebem o owner da entrada como parâmetro e devem usá-lo, evitando capturar o ViewModel global para ler/escrever dados da tela.

Voltar reutiliza o owner da entrada anterior enquanto ela permanece na pilha. Pop/clear da entrada cancela seus jobs/bindings e limpa state/server/nav. A sessão é em memória: recarregar o processo/página ou reconstruir o host não restaura esses valores automaticamente; snapshot/persistência precisam de contrato próprio. O SavedStateHandle recebido pelo host não representa persistência automática dos novos stores. O preview compilado do Studio ativa ISOLATED_ENTRIES; os demais hosts continuam no padrão até optarem explicitamente. Erros/loading da entrada usam os handlers do host através de LocalScreenFeedbackHandler.

Dentro de executeActions, actionValues() oferece response/error isolados por execução raiz e por ViewModel. Sequências/ramos aninhados no mesmo owner herdam os slots; execuções simultâneas têm instâncias distintas. Falhas tratadas pelo executor preenchem error com code/message; métodos podem preencher response/error explicitamente. O errorState visual do ViewModel continua separado desses resultados temporários. Ao terminar, falhar ou cancelar, os slots são limpos em finally; actionValues fora do contexto do owner falha explicitamente. Não há transporte HTTP ou resposta global implícita nessa API.

Nenhum nome/propriedade do wire legado foi alterado. Consumidores binários devem recompilar após as novas assinaturas Config/ViewModel. Chamadas que passavam block como argumento posicional devem usar block nomeado ou lambda final ao configurar as novas opções; chamadas existentes com nomes/lambda final continuam compatíveis.

No modo isolado, navigation:goTo resolve o provider no owner da ação, mantendo falhas na entrada atual. Rota desconhecida gera UNKNOWN_ROUTE; provider ausente conserva MISSING_PROVIDER com origem da rota, sem NullPointerException que sobrescreva o diagnóstico. O grafo plano foi o recorte de integração; deep links e recuperação após processo não são qualificados por esses testes.

Pacotes compilados não serializam screenStateMode nem o estado do formulário. Para reproduzir o isolamento do preview ao consumir uma jornada exportada, o host receptor deve configurar ISOLATED_ENTRIES; um host LEGACY_SHARED conserva a semântica compartilhada anterior. A política pertence à aplicação, não é ativada por texto do payload.

## Expressões opt-in: especificação da linguagem 1

A API pura recebe ExpressionSpec(language="humming.expression", version=1, source=...). O marcador e a versão são obrigatórios; não há detecção por prefixo em strings legadas. Esta especificação não registra uma nova propriedade executável no catálogo. Integração reativa/negociação por componente pertence à T09.

Gramática (da menor para a maior precedência): coalesce `??`, OR `||`, AND `&&`, igualdade `== !=`, comparação `< <= > >=`, adição `+ -`, multiplicação `* / %`, prefixos `! -`, primários e acesso. Operadores binários são associativos à esquerda. Parênteses mudam a precedência. Literais: null, missing, true, false, número JSON não negativo (sinal via prefixo) e string JSON entre aspas duplas. Não há arrays/objetos literais, interpolação, chamada de função, atribuição ou reflexão.

Raízes permitidas: state, server, nav, response e error, fornecidas pelo chamador em ExpressionContext. Acesso usa `.identificadorASCII` ou `[expressão]`; objetos exigem chave Text e arrays índice Number inteiro não negativo seguro. Propriedade/índice ausente resulta Missing; atravessar Missing/Null continua Missing sem avaliar a expressão do índice; atravessar outro escalar gera erro de tipo. Nomes que não são raízes/literais são rejeitados no parser.

`&&`, `||` e `!` exigem booleanos, sem truthiness/coerção. AND/OR avaliam o lado direito somente quando necessário. `??` avalia o lado direito somente para Missing ou Null. `+` soma dois números ou concatena dois textos; demais operações aritméticas/comparações exigem números. Igualdade compara escalares do mesmo tipo; tipos diferentes são desiguais, Missing difere de Null; comparar dois containers não tem igualdade estrutural implícita. Igualdade numérica é por valor no domínio abaixo, não por lexema.

Aritmética usa IEEE-754 Double, com resultado finito; divisão/resto por zero falham. Para não arredondar inteiros silenciosamente, números inteiros fora de ±9007199254740991 são rejeitados em operações/comparações numéricas. O armazenamento SduiValue ainda preserva lexemas maiores; consultá-los não os converte. Não há aritmética decimal financeira nem coerção de IDs Text para Number.

Limites padrão: fonte 4096 caracteres, 1024 tokens, profundidade 64, 10000 visitas no evaluator e 65536 caracteres por texto resultante/lido. Profundidade e tokens são limitados durante parsing; o evaluator também limita profundidade/visitas e aceita um callback checkCancellation chamado em cada visita, propagando a exceção original. O resultado não executa IO nem código do payload. Falhas têm NodeDiagnostic com operação parseExpression/evaluateExpression, código, caminho $.expression.source[offset] e offset da operação; T09 deverá anexar nó/propriedade do consumidor.

ExpressionProgram é imutável e cada evaluate inicia seus próprios contadores; o mesmo programa pode ser reutilizado com contextos distintos sem cache de resultado. O contexto não é um store mutável. Resultados numéricos de operações usam a representação textual de Double do target; sua semântica e testes são por valor, não pela presença de .0 ou notação exponencial. T09 deve definir a apresentação numérica de propriedades, sem converter implicitamente IDs textuais. maxDepth configurável fica entre 1 e 128 para manter a recursão limitada.

Forma equivalente da gramática (espaços ASCII são ignorados fora de strings; não há comentários):

```ebnf
expr       = coalesce ;
coalesce   = orExpr, { "??", orExpr } ;
orExpr     = andExpr, { "||", andExpr } ;
andExpr    = equality, { "&&", equality } ;
equality   = comparison, { ("==" | "!="), comparison } ;
comparison = addition, { ("<" | "<=" | ">" | ">="), addition } ;
addition   = product, { ("+" | "-"), product } ;
product    = prefix, { ("*" | "/" | "%"), prefix } ;
prefix     = ("!" | "-"), prefix | postfix ;
postfix    = primary, { ".", IDENT | "[", expr, "]" } ;
primary    = JSON_STRING | UNSIGNED_JSON_NUMBER | "true" | "false"
           | "null" | "missing" | SCOPE | "(", expr, ")" ;
SCOPE      = "state" | "server" | "nav" | "response" | "error" ;
IDENT      = (ASCII_LETTER | "_"), { ASCII_LETTER | DIGIT | "_" } ;
```

## Dependências e coleta de expressões

`ExpressionProgram.dependencies` descreve leituras conservadoras por escopo. `state.profile.name` observa a chave raiz `profile`; `state.items[state.index]` observa `items` e `index`; `state[nav.key]` e a leitura de `state` inteiro observam todo o estado. Todos os ramos entram na análise, inclusive os que sofreriam curto-circuito. Não há promessa de dependências mínimas por caminho executado. `readsScope`, `readsAll` e `keys` distinguem ausência, escopo inteiro e chaves conhecidas.

No core, `SDUIFormViewModel.expressionValues(spec, node, propertyName, fallback)` é uma API opt-in de código cliente, sem novo wire de propriedades. O stream é frio: cada consumidor possui sua coleta, cancelada pelo próprio consumidor. Projeta o snapshot das chaves relevantes e compara por conteúdo antes de avaliar; mudanças alheias ainda passam pela projeção, mas não executam o evaluator. Revisões são confladas, como os bindings existentes; não é um log de cada mutação intermediária.

`server` e `nav` vêm dos inputs da entrada. `response` e `error` são rejeitados com `EXPRESSION_PROPERTY_SCOPE`, inclusive em ramos não executados, pois não pertencem ao lifecycle da propriedade. Erros de payload produzem `ExpressionValue(fallback, diagnostic)`, com nó, propriedade e offset quando disponível; alterações relevantes posteriores podem recuperar o resultado. Cancelamento e defeitos de programação propagam. O fallback é obrigatório e tipado; nenhum valor é gravado no estado durante avaliação. Strings legadas permanecem inalteradas.

## Propriedades reativas opt-in

O identificador novo `layout:reactiveText` (schema 2) declara `value` obrigatório como EXPRESSION_TEXT e `visible` opcional como EXPRESSION_BOOLEAN, além dos modificadores comuns, fontSize e color. Sem visible, o texto é visível. Nenhum schema legado muda de versão. Consumidores sem essa definição devem rejeitar a capacidade; não há downgrade automático para layout:text.

Cada propriedade contém exatamente os três campos abaixo. O fallback tem de ser Text para value ou Boolean para visible; Missing/Null/números não sofrem coerção para esses tipos. Versões, campos extras, descritor inválido, sintaxe e limites são validados no contrato puro. Erros de avaliação/tipo em runtime entregam fallback e diagnóstico visível com código, nó e caminho. Um fallback false de visible esconde o conteúdo, mas o diagnóstico de visibilidade continua visível. Quando oculto, o componente mantém somente a assinatura de visibilidade; ao reaparecer, lê o texto atual.

```json
{"version":1,"expression":{"language":"humming.expression","version":1,"source":"state.nome ?? \"Visitante\""},"fallback":"Visitante"}
```

O Inspector edita esse envelope como JSON limitado. Importação, histórico e compilação usam os schemas existentes; o payload serializado permanece um objeto JSON no wire legado. A avaliação não escreve no estado e a apresentação não converte strings numéricas em números. A API tipada não fornece formatação monetária/decimal implícita.

`layout:reactiveButton` schema 1 exige enabled como EXPRESSION_BOOLEAN; onClick e children seguem os papéis/ordem existentes. O botão combina o resultado com o estado ocupado do invoker; ações dependentes são aguardadas e canceladas quando ele sai da composição. Falhas exibem diagnóstico e usam o fallback declarado. Use fallback false quando a operação deve permanecer indisponível em falha.

`layout:validatedTextField` schema 2 exige stateKey literal, valid como EXPRESSION_BOOLEAN e errorText literal; label e enabled reativo são opcionais. Ausência do valor canônico apresenta texto vazio sem gravar um default. Edição grava SduiValue.Text; Null ou outro tipo mostra FIELD_VALUE_TYPE sem coerção/escrita corretiva automática. valid false mostra errorText; falha da regra mostra também seu diagnóstico e fallback. A validação é imediata, inclusive antes da primeira edição. Não há bloqueio global de submissão: o autor deve declarar a regra de enabled na ação/botão, e o domínio ainda precisa validar a operação.

A fixture local usa concatenação com texto vazio para exigir Text antes de comparar comprimento implícito não vazio: `(state.nome ?? "") + "" != ""`. Isso rejeita tipos incompatíveis via EXPRESSION_TYPE e fallback false; não remove espaços nem implementa validação de domínio, regex ou rede. Campos e propriedades legados continuam com suas regras anteriores.

## Tokens de apresentação e máscaras

`PresentationTokens` é um registro imutável instalado pelo cliente. O padrão instala as cores content (#202124) e positive (#146C43), e a máscara code (###-###). `LocalPresentationTokens` permite ao host substituir esse registro; o core/contrato não dependem de Material 3. `TargetCapabilities.colorTokens/maskTokens` declara os nomes aceitos pelo destino. O compilador rejeita UNSUPPORTED_PRESENTATION_TOKEN se algum nome utilizado estiver ausente. Declarar nomes não instala o registro: o host deve fornecer os mesmos nomes em runtime. O preview utiliza o registro do seu LocalWorkspace.

reactiveText schema 2 acrescenta colorToken; a cor registrada tem precedência sobre color literal. Token desconhecido em payload executado diretamente usa a cor literal/Unspecified e mostra UNKNOWN_COLOR_TOKEN. validatedTextField schema 2 acrescenta maskToken; token desconhecido preserva a apresentação sem máscara e mostra UNKNOWN_MASK_TOKEN. Os 15 schemas legados permanecem versão 1, reactiveButton também permanece 1. Para compilar os dois componentes ampliados, o cliente deve declarar versão 2; um destino versão 1 é rejeitado, sem downgrade implícito. Envelopes de expressão continuam na versão 1 e payloads anteriores sem tokens continuam renderizáveis pelo novo cliente.

AsciiDigitMask é uma capacidade deliberadamente restrita a dígitos ASCII; # representa um dígito e os demais caracteres são literais. Pattern: 1–256 unidades UTF-16 e pelo menos um #. Input: até 4096 dígitos. Nenhuma conversão numérica, arredondamento, validação monetária ou remoção de zeros ocorre. Digitar além dos slots mantém os dígitos restantes no final; não há truncamento. Literais só são inseridos antes de um próximo dígito, sem sufixo decorativo antecipado. Input inválido mostra MASK_INPUT, mantém o valor canônico e apresenta o texto original editável; correção recupera a máscara.

Para cada dígito de índice i, o algoritmo emite os literais pendentes (cada offset transformado aponta para i), emite o dígito (offset seguinte aponta para i+1) e registra forward[i+1] como o tamanho de saída. forward[0]=reverse[0]=0. As tabelas de cursor são monótonas; reverse[forward[i]]=i. Offsets fora do intervalo são limitados aos extremos. Custo O(n+p), memória O(n+p), com n<=4096 e p<=256; nenhuma assinatura ou escrita de estado é criada pela formatação. A máscara é apresentação, não uma regra de validade: o autor deve declarar valid conforme o domínio.

## Transporte HTTP opt-in

humming-network instala somente a ação network:request (schema 1) por SDNetwork(transport). Não é dependência do contrato nem do modelo autoral e não é instalado pelo Studio local. Depende do core e de Ktor Core; engine é fornecida pelo host. O módulo não cria/fecha HttpClient por chamada, não publica artefatos e não fornece backend.

SduiHttpTransport recebe HttpClient, Map<String,HttpEndpoint> e HttpPolicy. Cada endpoint fixa URL, métodos GET/POST permitidos e headers do host. O payload escolhe somente o nome; não define URL, headers, credenciais, redirects, timeout ou limite. URLs aceitam HTTP/HTTPS, sem userinfo/fragmento. Headers registrados não aceitam CR/LF. O transporte exige cliente sem HttpRedirect (followRedirects=false) e define expectSuccess=false por requisição para classificar status. Configuração e engines pertencem ao host; ver APIs oficiais de [configuração Ktor](https://api.ktor.io/ktor-client-core/io.ktor.client/-http-client-config/index.html) e [validação de respostas](https://ktor.io/docs/client-response-validation.html).

```kotlin
// Source set JVM/Android do host, com engine OkHttp instalada pelo host.
val client = HttpClient(OkHttp) { followRedirects = false }
val transport = SduiHttpTransport(client, mapOf(
    "saveProfile" to HttpEndpoint("$trustedBaseUrl/profile", setOf("POST")),
))
val library = SDNetwork(transport) // registrar por addLibrary no host
// Ao terminar o lifecycle do host: client.close().
// Se uma engine foi fornecida como instância compartilhada, seu dono também a fecha.
```

Propriedades: endpoint obrigatório; method GET por ausência, ou POST; bodyState referencia uma chave canônica do owner; bodyJson contém texto JSON serializado literal. bodyState e bodyJson são mutuamente exclusivos. Nenhum deles interpola #{chave} ou executa expressões. bodyState Missing produz NETWORK_BODY_MISSING; Null é corpo JSON null. GET rejeita qualquer corpo. POST envia bytes JSON reais com Content-Type application/json. Para texto escalar literal, bodyJson deve conter a representação JSON completa, incluindo aspas; bodyState evita ambiguidade com strings legadas.

onSuccess/onFailure são sequências de ações registradas e aguardadas no mesmo owner/frame. Sucesso preenche response={status:Number,body:SduiValue} e limpa error. Falha preenche error={code:Text,nodeId:Text,path:Text,operation:"request",status?:Number} e limpa response. Há limpeza antes de cada requisição e no finally da execução raiz, inclusive cancelamento. Nada é copiado automaticamente para estado de tela/global. Um método registrado no ramo pode ler actionValues() e aplicar explicitamente o dado via updateValueFromAction.

Se onFailure foi fornecido (mesmo vazio), a falha esperada é dado tratado pelo ramo; não é erro do executor por si. Sem onFailure, a ação lança PayloadException localizado, participa de ActionExecutionResult/errors e respeita a política CONTINUE/STOP_ON_ERROR existente. Falhas de programação não são convertidas em NETWORK_IO; cancelamento do chamador propaga. Timeout pertencente à requisição produz NETWORK_TIMEOUT; IO produz código genérico, sem URL, corpo, headers ou mensagem bruta da exceção.

HttpPolicy padrão: 10s para IO/leitura, request 65536 bytes, response 1048576 bytes. Valores aceitos: timeout 1..120000ms, cada orçamento 1..8388608 bytes. Validação prévia limita corpo canônico a profundidade 64, 10000 nós e orçamento mínimo de caracteres; serialização verifica também bytes UTF-8/escapes reais. A leitura streaming pede no máximo maxResponseBytes+1 e encerra o statement em todos os caminhos. Status não 2xx produz NETWORK_HTTP_STATUS sem expor body; 204 retorna Null. Outros sucessos exigem application/json ou application/*+json, UTF-8 válido e JSON tipado limitado. Não há retries instalados pelo módulo. Plugins adicionais, autenticação e logging do cliente são responsabilidade explícita do host; não registrar corpos/credenciais.

O esboço anterior estava fora do build e não constituía API funcional: SDNetwork() sem transporte, network:get/post e componente request vazio são substituídos pela ação explícita. App do sample recebe urlProvider opcional; não instala mais provider remoto nem cria cliente por chamada por padrão. A integração de navegação/cache/versão pertence à T11. A compilação autoral só pode incluir rede quando o host fornecer o schema, os handlers e as capacidades de destino; o catálogo local inicial não habilita rede.

## Cache de definições por sessão e versão

JourneyDefinitionCache é uma API sob demanda do módulo navigation. O host ativa seu uso no Graph com SDNavigation(JourneyDefinitions(...)); SDNavigation() e a forma com lambda de preload preservam o caminho legado. JourneyDefinitions fixa journey/version/session e recebe cache e loader suspenso do host; cada rota fornece name/type/destiny como route/provider/resource. O loader versionado substitui somente o provider da definição de rota, sem instalar rede ou alterar o provider do próprio grafo. DefinitionKey contém journey/version/session/route/provider/resource (segmentos não vazios, até 4096 caracteres). DefinitionSnapshot é um DTO de código cliente com key, source e formatVersion=1; não é ainda um codec de restauração persistida. O loader deve devolver exatamente a chave pedida e o formato conhecido. Fonte é JSON declarativo de definição, sem inputs de tela/estado personalizado; o host não deve misturar dados de usuário nessa definição.

No modo versionado, toda navegação explícita consulta o cache, incluindo TTL; o mapa de definições exibidas não substitui essa consulta. Voltar mantém a entrada/definição já exibida. Uma rota aberta sem preload carrega no owner da entrada e exibe o conteúdo de preload durante a espera. Sair da composição cancela a espera; o cache continua responsável pela carga compartilhada no scope do host. Falhas são encaminhadas ao owner com diagnóstico e causa; cancelamento não vira erro. Graph.reloadRoute invalida a chave e aguarda substituição validada, mantendo a definição visível anterior em caso de falha; não apaga dados da entrada e não navega. O host deve tratar falhas dessa API suspensa. Instalar nova identidade de sessão requer novo grafo e descarte dos owners anteriores; invalidar cache isoladamente não equivale a logout.

Cache exige schema de autoria conhecido no NodeCatalog fornecido, IDs explícitos e únicos em children e slots/eventos declarados. Snapshots sem identidade, incompatíveis, inválidos ou excessivos são rejeitados e não entram no cache. O source imutável é armazenado; cada leitura devolve um novo parseNode, preservando extensões JSON e impedindo que propriedades mutadas por um consumidor contaminem outro. Dados de formulário permanecem no owner da T07.

Política padrão: TTL 300000ms (configurável 1..86400000), 64 entradas LRU, 1000000 caracteres por fonte, 4000000 no total e 16 cargas simultâneas. Hits promovem LRU; entrada expirada é removida e não é usada como fallback silencioso. Relógio monotônico é padrão e pode ser injetado. Uma falha não fica cacheada e a próxima tentativa pode recuperar.

Loads simultâneos da mesma chave compartilham uma Deferred do cache; cancelar um consumidor não cancela o outro. O cache cria SupervisorJob filho do Job do host, de forma que falha de um provider não destrua o escopo do host. invalidate(key), invalidateSession(session) e invalidateAll removem definições e cancelam flights associados; identidade da flight é conferida antes da gravação, evitando ressurreição por resposta antiga. close invalida/cancela somente os recursos do cache; não cancela o escopo do host. Cleanup e invalidação terminam em NonCancellable mesmo quando chamados em finally de uma coroutine cancelada. Providers devem propagar cancelamento e o dono deve fechar o cache.

metrics() fornece hits, misses, coalesced, providerLoads realmente iniciados, evictions, loadMillis acumulado (loader/validação/commit), entradas e flights atuais; não registra source, recursos, credenciais ou dados da sessão. Não há EAGER/PREFETCH, cache persistente ou restauração automática habilitados por essa API.
