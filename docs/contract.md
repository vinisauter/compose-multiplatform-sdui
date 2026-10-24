# Contrato declarativo e compatibilidade

Este documento separa o formato existente das extensões planejadas. O [estado atual](current-state.md) aponta para os arquivos que fundamentam a leitura. T01 produziu fixtures e 30 testes de caracterização executados em JVM; cobertura e limitações estão em [validação](validation.md). Isso não comprova suporte em outros targets.

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

`component` identifica `namespace:nome`, com diferenciação entre maiúsculas e minúsculas. ID omitido é gerado durante parsing; não é identidade estável entre carregamentos. A ordem dos arrays de `children` e de ações tem significado. O formato do runtime atual é uma árvore aninhada; o modelo normalizado do Studio é uma representação autoral futura, compilada para essa árvore.

### Parser e serializer

O parser manual de `ServerDrivenJson.kt` aceita propriedades JSON primitivas, objetos e arrays, embora o modelo público as armazene como strings:

| Entrada em `properties` | Representação observada após parsing |
| --- | --- |
| `"abc"` | String `abc` |
| `12`, `true` | Strings `12`, `true` |
| Objeto ou array | Texto JSON correspondente |
| `null` | String `"null"`; não equivale à ausência nem ao null Kotlin |

O acesso não nulo a `component` pode lançar erro sem diagnóstico de caminho. Helpers como `propertyNodes` leem slots/eventos como objeto único ou array de objetos; elementos não objeto em arrays podem ser ignorados. Isso é uma limitação a diagnosticar, não uma política de validação desejada.

Não presumir equivalência entre parser manual, serializer gerado e `toString()`. T01 confirmou que `JsonServerDrivenNode.toString()` devolve o JSON original sem refletir uma mutação posterior em propriedades. As fixtures cobrem round trip do JSON não editado e do modelo textual serializável, sem afirmar equivalência entre esses dois codecs. T13/T15 ainda precisam implementar exportação explícita após edição.

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

Este exemplo ilustra a estrutura. T00 executou um payload local equivalente em forma de campo/binding/ação pelo catálogo, e T01 caracterizou eventos objeto/array e separação de conteúdo/ação. Isso não significa que cada exemplo deste documento foi executado literalmente. Os demais componentes do catálogo mantêm seus nomes e propriedades atuais até terem schema e fixture em T05. Exemplos com integrações ausentes não são testes de aceitação.

## Estado e execução atuais

O estado é textual no ViewModel. `getState` pode inicializar chave ausente, `updateState(key, null)` remove e o binding de chave ausente pode criar valor vazio. Não existe linguagem tipada nem garantia de avaliação recursiva. `SavedStateHandle` recebido não comprova persistência do mapa entre recriações.

O dispatcher atual itera handlers diretos, mas `invokeActions` lança coroutine. Ações aninhadas e efeitos agendados por navegação não têm conclusão aguardada pela sequência externa. Há captura ampla de falhas e riscos de cancelamento consumido. Esses são problemas a corrigir, sem prometer a ordem desejada antes dos testes de T03.

A futura semântica será execução suspensa ordenada, com cancelamento propagado, contexto por execução e limpeza garantida. Antes de alterar tratamento de falhas, caracterizar o legado. Uma mudança entre continuar após erro e interromper sequência deve ser selecionada explicitamente por política/versão, com migração e testes; cancelamento não é erro comum recuperável.

## Extensões planejadas e migração

Ainda não estão implementados: envelope/versionamento, schemas, capacidades negociadas, valores tipados, expressões, escopos `server/nav/state`, resultados `response/error` por execução e novos contratos de rede/jornada. Os nomes descritivos do planejamento não são sintaxe JSON já aceita.

Para introduzir qualquer extensão:

1. Definir schema, semântica, capacidade/versão e exemplo mínimo na tarefa responsável.
2. Criar fixtures do legado e do novo formato, incluindo falha e limites.
3. Implementar adaptador ou migrador explícito; não inferir formato por uma propriedade ambígua.
4. Manter o caminho legado e avaliar depreciação com os consumidores existentes.
5. Fazer o Studio emitir somente capacidades suportadas pelo destino, com diagnósticos para incompatibilidades.
6. Atualizar este contrato, exemplos e evidência de execução.

Novos formatos de tokens reativos, temas e máscaras exigem versionamento ou extensão declarada; as propriedades legadas, incluindo `visualMask`, continuam preservadas. Valores numéricos não devem converter IDs, strings vazias, null ou booleanos por heurística. Se houver sintaxe de sufixos para tipos em corpos HTTP, decidir colisões de nomes, arrays, objetos e null antes de adotá-la; ela não integra o contrato atual.

## Limites de entrada

T02/T05 devem definir limites configuráveis de profundidade, quantidade de nós, tamanho de propriedades e orçamento de processamento. Diagnósticos identificam documento, nó, caminho, código e severidade. Namespace ausente, handler desconhecido, papel inválido e falha de provider precisam de políticas explícitas. O fallback não pode transformar erro de programação em sucesso aparente.
