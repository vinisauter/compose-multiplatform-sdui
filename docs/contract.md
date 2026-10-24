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
| `layout:text`, `textField`, `button`, `row`, `column`, `box` | [LayoutDefinitions](../humming-contract/src/commonMain/kotlin/me/developes/humming/sdui/layout/LayoutDefinitions.kt) |
| `action:update`, `method`, `ifState`, `then`; componente `action:event` | [ActionDefinitions](../humming-contract/src/commonMain/kotlin/me/developes/humming/sdui/action/ActionDefinitions.kt) |
| `navigation:graph`, `node`, `goTo`, `goBack` | [NavigationDefinitions](../humming-contract/src/commonMain/kotlin/me/developes/humming/sdui/navigation/NavigationDefinitions.kt) |

São 15 definições, versão 1. `parse(node)` produz `ParsedNodeProperties`: strings originais, diagnóstico por campo e `requireValid()` para rejeitar erros. Os adapters dos seis componentes, das ações update/method/ifState e da navegação usam esse parser; conversões finais para tipos Compose permanecem no catálogo visual. Defaults informam o Inspector, sem serem injetados no payload: ausência, string vazia e valor explícito continuam distintos. FontWeight.Normal, estado TextField_<id>, limites derivados da máscara e scroll por presença possuem descrição explícita em vez de um default wire inventado. O parser é compartilhável por código; não há exportação JSON Schema entregue.

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

`updateState` não emite quando o valor não muda ou quando remove uma chave ausente. `action:event` observa uma emissão inicial por entrada na composição; `onCreate` e seu observador de mudanças são efeitos canceláveis. Ações do catálogo preservam a cadeia causal com `updateStateFromAction`: um mesmo observador não reage novamente à sua própria cadeia de mudanças, inclusive através de outro observador. Cadeias têm limite de 64 observadores. Ações customizadas suspensas devem usar essa API para participar da prevenção de ciclos; `updateState` representa uma nova origem externa. O fluxo de revisões é conflado e não promete entrega individual de todas as mudanças concorrentes. Estado textual continua compartilhado pelo ViewModel; isolamento entre entradas ainda depende de T07/T11.

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
