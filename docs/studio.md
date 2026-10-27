# Studio: modelo autoral e mecânicas

O modelo/codec de T13 e os comandos/histórico de T14 estão implementados no módulo puro humming-studio-model; o editor local está integrado ao sample, com aceite rastreado no estado atual; a geometria de seleção/drop está implementada e seu aceite está no estado atual; conectores, canvas de interações e jornadas locais seguem as APIs descritas abaixo, com aceite e limites no estado atual. O primeiro recorte é autoria local de telas compatíveis com o [contrato legado](contract.md). Não incluir renderização gráfica de baixo nível, roteamento complexo de obstáculos ou colaboração na primeira entrega.

## Documento e índices

Modelo implementado em [AuthoringDocument.kt](../humming-studio-model/src/commonMain/kotlin/me/developes/humming/sdui/studio/AuthoringDocument.kt), sem dependência do runtime visual:

```text
AuthoringDocument {
  documentId: String
  formatVersion: Int
  revision: Long
  entryNodeId: String
  nodes: Map<NodeId, AuthorNode>
  edges: Map<EdgeId, Edge>
}
AuthorNode {
  id: NodeId
  kind: VISUAL | SCREEN | ACTION | NAVIGATION | UNKNOWN
  definition: String                 // identificador namespace:nome
  properties: JsonObject
  placement: Root(orderKey) | Child(parentId, slot, orderKey)
  canvasPosition: Point?             // somente organização do canvas macro
}
Edge { id, role, sourceNodeId, sourcePort, targetNodeId, targetPort, orderKey?, metadata }
role = EVENT | ACTION_FLOW | NAVIGATION
```

`placement` é a autoridade de contenção/ordem estrutural. `childrenByParentAndSlot`, adjacency de arestas, bounds, seleção e projeção de árvore são índices/estado derivados. Atualizá-los na mesma transação ou reconstruí-los da revisão; não persistir uma segunda árvore mutável concorrente. O schema define slots estruturais, incluindo conteúdo e metadados permitidos. Eventos e fluxo de ações usam arestas como autoridade, não placement em um slot de evento. Ações permanecem raízes autorais e se ligam por portas; o compilador materializa sua estrutura aninhada. Arestas não duplicam a contenção estrutural.

Invariantes: IDs únicos; todos os pais/portas existentes e compatíveis; nenhuma referência cruzada inválida entre documentos; contenção sem ciclos; ordem total por slot; raízes declaradas; arestas com cardinalidade válida. Ciclos de navegação são permitidos. Ciclos de execução são rejeitados no recorte inicial. Elementos visuais e metadados não são intercambiáveis.

Importar árvore legada atribui/preserva IDs, cria relações normalizadas e guarda propriedades ainda não editáveis sem perda. Campos ou nós desconhecidos ficam preservados e marcados como não editáveis quando não houver schema. Exportar usa codec explícito, sem depender do `toString()` do wrapper atual. Compilar exige suporte semântico para os elementos alcançados; não descartar silenciosamente nós desconhecidos.

### Codec local e limites implementados

humming-studio-model depende somente de humming-contract. AuthoringDocument usa mapas por ID em memória; o codec próprio versão 1 escreve arrays nodes/edges ordenados por ID e ordena as chaves dos objetos JSON, preservando a ordem dos arrays de dados. Arrays no wire permitem diagnosticar IDs duplicados antes da construção dos mapas. Use AuthoringDocumentCodec para persistência, sem serializar o mapa ou o wrapper legado diretamente. O discriminador de placement é placementType, com root/child.

O documento possui entryNodeId. Cada AuthorNode também armazena nodeListShapes (OBJECT, ARRAY, STRING_OBJECT, STRING_ARRAY), hasProperties/hasChildren e extensions. Esses campos preservam a representação original de slots/eventos, arrays vazios, ausência versus objeto vazio e campos extras do nó. Relações conhecidas saem de properties: slots estruturais viram Placement.Child; eventos/fluxos de ação viram arestas e ações ficam em Placement.Root. Propriedades primitivas/objetos desconhecidos conservam seus tipos JSON. IDs explícitos são preservados; IDs omitidos são gerados uma vez e persistidos.

AuthoringIndices deriva raízes, filhos por pai/slot, arestas por origem/porta e entradas por destino. Não existe índice persistido paralelo. validateAuthoringDocument valida identidade, entry, pais, chaves, papéis/slots, portas, cardinalidade de representação, referências, coordenadas finitas e ciclos. A validação estrutural permite preservar propriedades/rascunhos ainda não compiláveis; a importação/exportação legada também executa a validação do catálogo. Ciclos ACTION_FLOW são rejeitados. NAVIGATION usa portas navigate → entry com destino de tipo SCREEN/NAVIGATION e pode formar ciclos, sem criar contenção. Esse protocolo de autoria não instala ações no runtime.

Limites padrão de autoria: 10.000 nós, 20.000 arestas, profundidade 64 e chaves de até 256 caracteres. Somam-se os limites JSON do contrato, inclusive orçamento de texto/valores/profundidade; aumentar um limite não suspende os demais. Importação gera ranks lexicográficos fixos terminados em V; geração de chaves intermediárias usa generateKeyBetween de T14.

LegacyAuthoringCodec importa/exporta uma árvore local e usa dados atuais, independentemente de toString(). Desconhecidos são preservados com diagnóstico, sem serem considerados editáveis. Exportar uma raiz específica avisa sobre nós não alcançados, que continuam salvos no documento autoral. Arestas explícitas NAVIGATION e compartilhamento de um mesmo nó por caminhos distintos exigem política de compilação/identidade e retornam erro de exportação; não são descartados nem expandidos com IDs duplicados. O codec autoral salva esses grafos quando estruturalmente válidos. O compilador de T15 continua responsável por capacidades, expansão e semântica de destinos.

## Ordenação por strings densas

Usar chaves lexicográficas no documento autoral, não no JSON legado. O compilador ordena por chave e emite arrays. Alfabeto e comparação são independentes de locale:

```text
D = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
BASE = 62
chave válida = string não vazia de D, sem '0' no final
comparação = lexicográfica ordinal, com prefixo menor que extensão
null em prev = menos infinito; null em next = mais infinito
```

A restrição do último caractere evita intervalos sem chave válida. O algoritmo de referência abaixo é determinístico. `drop(s,n)` remove no máximo `s.length` caracteres; `take` usa índices exclusivos no fim.

```text
generateKeyBetween(prev, next):
  require prev == null or valid(prev)
  require next == null or valid(next)
  require prev == null or next == null or prev < next
  return midpoint(prev ?: "", next)

midpoint(a, b):
  if b != null:
    n = 0
    while n < length(b) and (n < length(a) ? a[n] : '0') == b[n]:
      n = n + 1
    if n > 0:
      return take(b, n) + midpoint(drop(a, n), drop(b, n))
  lo = length(a) == 0 ? 0 : indexOf(D, a[0])
  hi = b == null ? BASE : indexOf(D, b[0])
  if hi - lo > 1:
    return D[floor((lo + hi) / 2)].toString()
  if b != null and length(b) > 1:
    return b[0].toString()
  return D[lo] + midpoint(drop(a, 1), null)
```

Vetores obrigatórios de T14:

| prev | next | resultado |
| --- | --- | --- |
| null | null | `V` |
| `V` | null | `k` |
| null | `V` | `F` |
| `V` | `W` | `VV` |
| `V` | `V0V` | `V0F` |

Testar também geração repetida no mesmo intervalo, prefixos, limites, rejeição de `V0`, caracteres inválidos e propriedade `prev < resultado < next`. A referência recursiva com cópias de strings pode custar O(L²); a implementação pode usar cursores/buffer para custo O(L), onde L é o comprimento percorrido. Uma movimentação escreve a relação do nó e uma chave, mas consulta, geração, índices e projeção têm seus próprios custos; não afirmar custo total O(1).

Definir limite de comprimento e erro explícito. Se necessário, rebalancear chaves em transação versionada que preserve a ordem; não truncar. No recorte inicial, revisões serializadas impedem colisões de comandos simultâneos. Duas inserções independentes com mesmos vizinhos geram a mesma chave; chaves densas não resolvem concorrência por si mesmas. T25 define essa política.

## Comandos, revisão e undo

EditCommand contém commandId, expectedRevision e uma lista ordenada de EditOperation. Aplicação local é serializada, valida invariantes e produz novo documento, revisão, mudanças e inversa. Falha não altera parcialmente o documento. Comandos mínimos: inserir, remover subárvore, mover, atualizar propriedades, adicionar/remover aresta e alterar posição macro.

`Move` recebe nó, novo pai/slot e IDs dos vizinhos após remover o nó da origem. Valida ancestralidade, capacidade do destino e revisão; resolve vizinhos atuais; gera chave; atualiza placement; incrementa revisão. IDs de vizinhos são preferíveis a um índice calculado em uma revisão antiga. Se revisão ou vizinhos não forem compatíveis, rejeitar e recalcular preview.

A inversa de remoção preserva a subárvore completa, relações, chaves e arestas incidentes. Desfazer/refazer restaura o documento semanticamente, incluindo ordem, sem regenerar IDs. Eventos de seleção/hover não entram no histórico. Undo concorrente futuro não reutiliza cegamente uma inversa local sobre edição alheia.

### API local de edição implementada

[EditSession](../humming-studio-model/src/commonMain/kotlin/me/developes/humming/sdui/studio/EditSession.kt) possui execute, undo, redo e snapshot. [EditCommand](../humming-studio-model/src/commonMain/kotlin/me/developes/humming/sdui/studio/EditCommand.kt) oferece InsertNode, RemoveNode, MoveNode, SetProperties, PutProperty, RemoveProperty, SetCanvasPosition, InsertEdge, RemoveEdge e SetEntry. SetProperties substitui o mapa inteiro; PutProperty/RemoveProperty preservam os demais campos e distinguem JsonNull de remoção. Relações continuam em placements/arestas; propriedades com nós conhecidos não podem duplicá-las.

InsertNode usa o container do placement recebido e gera a chave na revisão atual; a chave do template não é aplicada. MoveNode remove o próprio nó da lista de vizinhos antes do cálculo. previousId/nextId precisam descrever uma lacuna adjacente no destino completo. null identifica uma extremidade; ambos null só são válidos num destino vazio. Raízes de ação também integram a ordem das raízes. Não usar apenas os vizinhos da lista visual filtrada do drag preview.

[generateKeyBetween](../humming-studio-model/src/commonMain/kotlin/me/developes/humming/sdui/studio/FractionalKeys.kt) implementa os vetores acima e valida alfabeto, ordem e comprimento. Ao esgotar 256 caracteres por padrão, retorna KEY_SPACE_EXHAUSTED; não trunca nem reindexa irmãos. Rebalanceamento automático não foi implementado. Consultas/validação/ordenação de índices continuam tendo seus próprios custos; não há promessa de transação O(1).

A sessão copia profundamente documento inicial e comandos, trabalha sobre candidatos e valida antes de publicar. Um compare-and-set de AtomicReference comum publica documento, histórico e recibos juntos. Comandos concorrentes com a mesma revisão têm um vencedor; o outro recebe conflito. Cada comando aceito avança a revisão, inclusive undo/redo; a inversa restaura conteúdo/IDs/chaves, sem retroceder a revisão. DocumentPatch expõe alterações e inversa no resultado, mas a aplicação de patches internos pertence à sessão. Inserções dependentes devem ordenar pai antes de filho na lista de operações.

commandId repetido com a mesma requisição na janela de recibos retorna replay com o documento atual e appliedRevision original; não devolve um snapshot antigo nem reaplica. Reutilizar o ID com outra requisição falha. Limites padrão: 1.000 operações/comando; histórico de 100 entradas e 8.000.000 caracteres serializados de patches; 256 recibos e 2.000.000 caracteres de recibos. Esses são orçamentos de representação, não medições exatas de RAM. O histórico mais antigo é descartado para caber; comando individual que não cabe é rejeitado inteiro. Nova edição após undo elimina redo. Não há deduplicação persistida entre processos/reinícios.

Remoção apaga a subárvore de contenção e arestas incidentes; ações que são raízes independentes não são silenciosamente apagadas por perder seu gatilho. Undo restaura subárvore/arestas/hints completos. Hints OBJECT/STRING_OBJECT são retirados ao esvaziar o slot e promovidos a array ao receber mais de um alvo, mantendo representação legada consistente. Seleção/hover e preview transitório não fazem parte do documento/histórico. Isso entrega comandos locais; não é coordenação de editores remotos.

## Coordenadas, seleção e bounds

Usar `Double` nas funções geométricas puras e uma unidade de canvas consistente. Converter densidade de dp para pixels uma vez na fronteira do host. Não misturar bounds globais de Compose com coordenadas locais já transformadas.

Viewport com zoom positivo z e translação p:

```text
screen = z * world + p
world = (screen - p) / z
zoom ancorado em s: w = (s - p) / z; pNovo = s - zNovo * w
```

Para transformações afins gerais, `world = inverse(V) * screen` e `local = inverse(Tcontainer) * world`. Matriz singular invalida o alvo. Registrar bounds com ID, revisão de layout, transform e ordem de pintura. Um registro obsoleto precisa de recomposição/revalidação antes de commit.

Um retângulo local `(x,y,w,h)`, com dimensões não negativas, contém q quando `x <= q.x <= x+w` e `y <= q.y <= y+h`; retângulos degenerados não são alvos de drop. Aplicar clipping de ancestrais. Para sobreposição, selecionar primeiro a região pintada à frente, respeitando descendência; empates usam ordem estável da projeção e ID. Área menor isoladamente não decide o container.

O modo de edição seleciona/manipula; o modo preview executa as capacidades controladas do renderer. Capture do ponteiro impede outro gesto concorrente de realizar o mesmo commit. Cancelamento, Escape e perda de captura descartam apenas o preview transitório.

## Drop em Row e Column

Escopo inicial: fluxo linear, sem wrap e sem inversão visual. Column usa eixo y; Row usa eixo x. Suporte a RTL, reverse ou wrap deve ter tarefa/semântica próprios antes de ser habilitado no Inspector. Medir filhos pelo renderer, sem recalcular manualmente tipografia, pesos ou intrinsic sizes.

### Escolha do container

1. Converter o ponteiro screen → world.
2. Determinar a região visível pintada à frente, em ordem de hit definida acima, e percorrer sua cadeia de ancestrais. Um overlay não elegível impede selecionar um container de outra cadeia que esteja atrás dele.
3. Excluir o nó arrastado, seus descendentes e destinos proibidos; aplicar clipping e teste local do retângulo.
4. Escolher o candidato válido à frente e mais específico na cadeia de ancestrais. Em área sem descendente elegível, usar o ancestral elegível sob o ponteiro.
5. Para cada candidato, usar apenas bounds da revisão de layout corrente. Sem candidato, não mostrar guia de inserção.

No fluxo do container, excluir o arrastado e itens invisíveis, absolutos ou não visuais. Ordenar os restantes pela ordem autoral efetivamente renderizada. Se os centros não forem monotônicos no eixo escolhido, rejeitar o algoritmo linear e diagnosticar layout não suportado no recorte inicial. Essa lista filtrada serve ao cálculo visual; o comando precisa de vizinhos no slot completo, como definido abaixo.

### Índice e linha guia

Para os N filhos elegíveis, sejam `a[i]` e `b[i]` o início e fim do bound local no eixo principal, com `c[i] = (a[i] + b[i]) / 2`. Seja u a coordenada local do ponteiro nesse eixo:

```text
targetIndex = primeiro i em [0,N) tal que u < c[i]; se não existir, N
```

A igualdade com o centro passa para depois daquele filho. Com N=0, `targetIndex=0`. `targetIndex` é relativo à lista filtrada **sem o arrastado**, evitando ajustes duplicados ao mover dentro do mesmo pai.

```text
guideAxis = contentStart                         se N == 0
guideAxis = a[0]                                 se targetIndex == 0 e N > 0
guideAxis = b[N-1]                               se targetIndex == N e N > 0
guideAxis = (b[targetIndex-1] + a[targetIndex])/2  nos demais casos
```

Se o conteúdo local é `(cx,cy,cw,ch)`, Column desenha de `(cx,guideAxis)` a `(cx+cw,guideAxis)`; Row desenha de `(guideAxis,cy)` a `(guideAxis,cy+ch)`. Transformar ambos os pontos pelo transform do container e viewport para o overlay. Padding define o content rect; gaps já estão representados pelos bounds dos filhos.

```text
onPointerMove(screenPoint):
  target = findEligibleContainer(screenPoint, draggedId, layoutRevision)
  if target == null: clearPreview(); return
  q = inverse(target.transform) * inverse(viewport) * screenPoint
  children = eligibleFlowChildren(target, excluding = draggedId)
  require monotonicCenters(children, target.axis)
  i = firstIndex(children, q.axis < center(child)) ?: children.size
  gap = resolveFullSlotGap(target, excluding = draggedId, nextVisibleId = children[i]?.id)
  preview = {target.id, target.slot, i, gap.beforeId, gap.afterId,
             document.revision, layoutRevision, viewportRevision}
  showGuide(target, children, i)

onPointerUp():
  if preview == null: return
  if preview.documentRevision != document.revision or
     preview.layoutRevision != currentLayoutRevision or
     preview.viewportRevision != currentViewportRevision:
    clearPreview(); return             // descarta drop obsoleto; novo gesto recalcula
  dispatch Move(draggedId, preview.target, preview.slot,
                preview.beforeId, preview.afterId, preview.documentRevision)
  clearPreview()
```

`resolveFullSlotGap` obtém todos os filhos do slot, ordenados e sem o arrastado. Se houver próximo filho visível, insere imediatamente antes dele, usando seu predecessor no **slot completo** como `beforeId` e ele como `afterId`. Sem próximo visível, insere no fim do slot completo; se vazio, ambos são null. Assim filhos filtrados mantêm sua ordem, e uma chave não colide com a de um filho invisível entre vizinhos visuais. Testar, por exemplo, vizinhos visíveis `F`/`k` com invisível `U`: não gerar chave diretamente entre `F` e `k`.

Movimento de ponteiro não altera o documento. Bounds, scroll, densidade e resize invalidam a revisão de layout; pan/zoom invalidam a de viewport. O comando ainda valida revisão/vizinhos na transação. Box usa regra própria de empilhamento/slot: iniciar com inserir no fim e reordenar pelo Inspector; não aplicar a fórmula de Row/Column implicitamente.

## Grafo de interações e conectores

`Interaction { sourceNodeId, trigger, actionRootIds, transition? }` é uma **projeção derivada**, não uma coleção persistida adicional. Arestas EVENT saem da porta de evento do componente para raízes de ação, ordenadas por `orderKey`; a porta identifica o trigger e `metadata` guarda parâmetros/transição declarados pelo schema. Arestas ACTION_FLOW representam sequências/ramos e NAVIGATION identifica a tela de destino. Não manter uma cópia mutável de `onClick` em properties ou placement: o importador converte a relação e o compilador a materializa. A ligação de navegação também deve ter uma só autoridade; o compilador gera `destiny` a partir do destino autoral.

`Trigger`, ações e transições são uniões fechadas por schema/capacidade. No primeiro recorte, suportar click → ação registrada/navegação suportada pelo cliente. Hover, timers e gestos não devem aparecer como executáveis antes de terem handler, lifecycle e testes.

Portas tipadas determinam quais conexões são válidas e a ordem das ações. Arestas de navegação apontam para a identidade de uma tela; ações ordenadas têm `orderKey`. O compilador percorre com `visited` e pilha de recursão, valida nós alcançáveis e limita expansão. Múltiplas saídas ordenadas compilam arrays; condições exigem portas nomeadas `then`/`else`. Reuso de uma subárvore não pode causar expansão infinita nem ordenação dependente do hash do mapa.

Conector visual pode sair do bound de um componente interno e chegar à borda do frame de destino. Usar retângulos no espaço world; o endpoint é uma função dos bounds atuais, não coordenada persistida de pixels.

### Ancoragem e curva inicial

Regra inicial determinística: calcular `d=centerDestino-centerOrigem`. Se `abs(d.x)>=abs(d.y)`, usar lado horizontal; caso contrário, vertical. Para centros coincidentes, escolher saída à direita e entrada à esquerda. `sign(0)=+1` para esta escolha. Âncoras ficam no centro dos lados escolhidos; a entrada usa o lado oposto. Normal n aponta para fora do retângulo:

```text
direita: P=(x+w,y+h/2), n=(1,0)
esquerda: P=(x,y+h/2), n=(-1,0)
baixo: P=(x+w/2,y+h), n=(0,1)
cima: P=(x+w/2,y), n=(0,-1)
```

Com P0 origem, P3 destino, n0/n3 suas normais e `distance=length(P3-P0)`, adotar inicialmente `h=max(24,min(160,0.5*distance))` em unidades world. Os valores são parâmetros de desenho deste projeto e devem ser avaliados visualmente em T19.

```text
P1 = P0 + h*n0
P2 = P3 + h*n3
B(t) = (1-t)^3*P0 + 3*(1-t)^2*t*P1 + 3*(1-t)*t^2*P2 + t^3*P3
B'(t) = 3*(1-t)^2*(P1-P0) + 6*(1-t)*t*(P2-P1) + 3*t^2*(P3-P2)
0 <= t <= 1
```

A tangente final é `3*(P3-P2)=-3*h*n3`, voltada para dentro do destino. Para seta, `v=normalize(B'(1))`, perpendicular `r=(-v.y,v.x)`, ponta P3 e base `P3-arrowLength*v ± arrowWidth*r/2`. Converter tamanhos desejados em pixels para world dividindo pelo zoom. Um badge pode usar `B(0.5)`; isso não é metade do comprimento de arco.

Autoconexão visual usa inicialmente loop externo em segmentos cúbicos com portas distintas, ou é rejeitada no Inspector até haver fixture; não reutilizar curva degenerada como se fosse roteamento válido. O primeiro escopo não evita obstáculos automaticamente. Uma tarefa futura, justificada por casos reais, pode introduzir roteador ortogonal/obstáculos sem alterar a semântica das interações.

### Hit do conector

Flatten adaptativo em screen space via subdivisão de De Casteljau em t=0.5: subdividir até distância de ambos os controles ao **segmento finito** da corda ser <=1 pixel, com limite de profundidade/erro reportado. Distância à reta infinita não basta: controles colineares podem ultrapassar endpoints e produzir retorno da curva. Para corda degenerada, comparar os controles ao endpoint. Em cada segmento A→C, com ponteiro Q:

```text
v = C-A
u = dot(v,v)==0 ? 0 : clamp(dot(Q-A,v)/dot(v,v),0,1)
nearest = A + u*v
distanceSquared = dot(Q-nearest,Q-nearest)
hit = min(distanceSquared sobre segmentos) <= 8^2
```

Tolerância de 8 pixels é parâmetro inicial do host. Testar zoom, densidade, endpoint, curvas curtas, bounds sobrepostos e cancelamento do gesto. Incluir cúbica colinear com controles `(0,0),(24,0),(-23,0),(1,0)`, em zoom 2: ela não pode ser reduzida à corda inicial, pois `B(0.2)=(7.016,0)`. Não misturar tolerância de screen com distância de world sem conversão.

## Fronteira de entrega

T13–T15 entregam modelo, comandos e compilação puros. T13/T14 são verificadas com fixtures semânticas e testes puros; a integração de exportar e executar o contrato no cliente começa em T15. T16–T17 entregam preview e edição local utilizável. T18 acrescenta o drop geométrico e T19 o grafo visual, também validados pela execução do contrato resultante. Persistência e múltiplos usuários só entram nas tarefas remotas. Uma imagem do canvas isolada não valida a semântica.

## Compilação local e capacidades

`ContractCompiler.compileTree` gera uma árvore; `compileJourney` gera `graph.json` e `screens/<id-em-hex-UTF8>.json`. `TargetCapabilities` declara versões exatas de definições, métodos e providers disponíveis. O host deve registrar implementações reais: a declaração não instala handlers. O compilador não executa efeitos.

Arestas NAVIGATION de `navigation:goTo` usam `navigate → entry` para uma SCREEN raiz e substituem a necessidade de `destiny` literal; manter ambos é erro. Ciclos de navegação são permitidos. Ciclos de ações são rejeitados. A ponte LegacyAuthoringCodec continua conservadora; a compilação resolve navegação explícita e expande ações compartilhadas.

Na expansão, a primeira ocorrência preserva o ID; repetições recebem `__expanded:<sourceID>:<ocorrência>`, evitando colisões com IDs autorais e emitidos. `sourceNodeIds` relaciona IDs gerados às origens. Mapas embaralhados produzem os mesmos artefatos. Essa identidade não promete estabilidade após qualquer edição do grafo.

Nós/arestas desconectados são rejeitados por padrão; `UnreachablePolicy.REPORT` permite omissão com aviso explícito, sem alterar o documento. Extensões e metadados sem semântica compilável são rejeitados. Limites padrão: 20.000 nós expandidos, 256 artefatos, profundidade 64 e 4.000.000 caracteres somados; os limites de payload também se aplicam. A escrita JSON canônica verifica tamanho antes de formar a saída completa. Falhas não retornam compilação parcial.

## Executar o preview local

Na raiz, executar `./gradlew.bat :sample:composeApp:wasmJsBrowserDevelopmentRun` e abrir http://127.0.0.1:8080/?preview. LocalPreview usa renderer e catálogo reais, provider limitado à fixture local, SavedStateHandle em memória e ViewModelStore descartado com o host. Edição e update são reais; Testar diagnóstico invoca propositalmente um método ausente. A fixture não oferece rede, navegação ou métodos do aplicativo. O host de produção App permanece separado.

## Editor local por árvore e Inspector

`LocalWorkspace` orquestra o modelo puro e `EditSession`: criar documento Column, importar contrato, abrir/salvar documento autoral, editar campos, inserir componentes e action:update por evento, remover, reordenar e mover entre slots. `tree()` projeta contenção e relações de ações; não mantém outra árvore mutável. A ordem de ações segue suas arestas, incluindo Subir/Descer. Remover um botão preserva ações que ficam desconectadas; elas aparecem como raízes e a compilação acusa o problema em vez de descartá-las.

`LocalStudio`, no commonMain do sample, apresenta árvore, Inspector e preview. Insere Column, Row, Box, text, textField e button; este recebe um label text. Campos são descritos pelo catálogo: tipo, enum, obrigatoriedade, default e bindings. Aplicar valida o campo antes do comando; remover valor distingue ausência de string vazia. Propriedades desconhecidas/não editáveis e nós sem schema permanecem somente leitura e salvos. Relações usam comandos, sem edição textual de slots/eventos. Campos inválidos preservam o documento.

Abrir documento lê o formato autoral; Importar JSON aceita documento ou contrato, por escolha explícita. Importação inválida preserva documento e histórico. Novo/abrir/importar pedem confirmação quando há alterações locais. Undo/redo são locais à sessão e não acompanham o arquivo. Reabrir cria outra sessão e reinicia o estado do preview, mesmo quando documento/revisão coincidem. Cada edição válida também recria o preview; estado de formulário não faz parte do documento.

O sample agora depende de humming-studio-model em commonMain. `CompiledPreview` consome o JSON do compilador com renderer real e ViewModelStore próprio, descartado ao substituir a composição. Compilação usa capacidades layout/action do catálogo, nenhum método de aplicativo/provider externo, e rejeita desconectados por padrão. Incompatibilidade mostra diagnósticos e retira o preview, evitando exibir uma revisão anterior como atual. Navegação de telas e edição gráfica de interações continuam nos recortes próprios.

`StudioFiles` é uma porta do host. `BrowserStudioFiles` usa seletor de um arquivo JSON e download via Blob, sem rede. Limites de leitura: 8 MB antes de alocar o texto, 2 milhões de caracteres antes de entregar ao codec, mais os limites do modelo. Cancelar o seletor preserva o documento; erro de leitura aparece na UI. URLs temporárias de download são revogadas. Solicitar download não comprova que o usuário preservou o arquivo; por isso o indicador de alterações locais não é apagado. Outros hosts podem omitir a porta, ficando com botões de arquivo indisponíveis; não há integração nativa de arquivos entregue.

## API pura de geometria

DropGeometry implementa seleção e planejamento de drop a partir de LayoutSnapshot revisionado. MeasuredNode contém retângulo local, transformação afim, clips, ordem de pintura, visibilidade/participação no fluxo e ContainerBounds opcional. Esses dados são derivados do host, nunca persistidos no documento. Rect2/Point2 usam Double; inversa com determinante absoluto abaixo de 1e-12 é tratada como não utilizável. CanvasViewport aplica densidade uma vez: screen=world*zoom*density+panPixels, com zoom ancorado.

A ordem de hit é paintOrder decrescente, profundidade decrescente e ID ordinal. Column/Row usam centros medidos; igualdade passa para depois. Box somente acrescenta ao fim, com guia no fundo do content rect. O gap do comando vem do slot completo, sem o arrastado. Fluxo não monotônico é diagnosticado. DropGesture captura um ponteiro, mantém apenas preview e publica MoveNode no release com revisão de documento/layout/viewport conferida. Cancelamento ou perda de captura chama cancel e não edita o documento.

## Integração geométrica com o renderer

`LocalNodeBoundsObserver`, na biblioteca de layout, é opcional. Os seis componentes do catálogo observam limites OUTER e CONTENT em seus modificadores, sem Box ou outro wrapper de layout. Sem observador o modifier é preservado. A API usa ServerDrivenNode e LayoutCoordinates; não depende de autoria ou regras do sample. Descarte informa removed(nodeId).

`PreviewGeometry` copia medidas do renderer para um snapshot: bounds completos, conteúdo, clip efetivo e ordem da projeção autoral. Não retém LayoutCoordinates após o callback nem recalcula padding/tipografia. Converte pixels de root para mundo do viewport, aplicando densidade uma vez. Cada alteração de medidas/origem/descarte incrementa a revisão de layout. O host inicial força LTR; wrap/reverse e slots de drop além de children não estão habilitados.

No Studio, Exemplo de arraste cria dois containers e os textos A/B. Modo edição mostra seleção e captura o gesto sobre o preview; Modo execução deixa o formulário funcionar. Arrastar além de touchSlop calcula guia sem editar; release aplica MoveNode e atualiza árvore/contrato. Escape, perda de foco do overlay/janela, múltiplos ponteiros ou cancelamento da coroutine descartam a captura. Release rejeita revisões obsoletas. Guia obsoleta não é desenhada.

Os controles Zoom +/− e Esquerda/Direita/Acima/Abaixo alteram só o viewport; Centralizar restaura-o. Modos e viewport sobrevivem ao commit, enquanto o estado do formulário é reiniciado pela nova revisão conforme T17. A seleção respeita clipping e o elemento à frente; o alvo de drop é o ancestral elegível mais próximo dessa região, excluindo o arrastado e seus descendentes. Box oferece somente append; reordenação fina permanece no Inspector.

Uma tela nova começa sem componentes. Seu contorno de seleção pode ser pequeno e não é a linha de inserção. O preview mostra Tela vazia e oferece Carregar exemplo de arraste. Antes de testar drag, conferir A/B e os dois containers; ativar Modo edição, pressionar um texto e movê-lo sobre o outro container. A guia só corresponde a um alvo elegível durante esse gesto.

### API de geometria dos conectores

`ConnectorGeometry.between(sourceId, sourceBounds, targetId, targetBounds)` deriva `CubicConnector` em world; retorna null para IDs iguais. `point(t)` e `tangent(t)` aceitam t entre 0 e 1. Centros coincidentes de nós diferentes usam saída direita/entrada esquerda. Não há desvio de obstáculos.

`arrow` retorna triângulo em pixels físicos, com dimensões padrão 12×8, após aplicar `CanvasViewport` (zoom, densidade e pan). Tangente final nula retorna null. `flatten` aplica a mesma viewport e subdivide por De Casteljau usando distância ao segmento finito; tolerância padrão 1 pixel, profundidade padrão 12 (máximo permitido 16). `FlattenedConnector.maxErrorPixels` é o maior limite conservador das folhas; `toleranceMet=false` indica orçamento insuficiente. O host deve tratar esse resultado explicitamente. `hit` consulta a aproximação com raio padrão de 8 pixels; não declara interseção exata com a cúbica. Pontos/setas são derivados, não serializados no documento.

### Edição local das relações

`LocalWorkspace.connectAction` deriva EVENT/ACTION_FLOW pelo papel da origem e acrescenta a relação ao final da porta, validada pelo catálogo. Rejeita duplicata e ciclo de ações sem alterar a revisão. `disconnect` remove somente a aresta; a ação desconectada é preservada e pode bloquear compilação até ser reconectada/removida. O Inspector expõe esses comandos em eventos e ramos conhecidos, mantendo a reordenação existente.

`addScreen` cria uma raiz SCREEN com canvasPosition autoral. `setNavigation` exige navigation:goTo → SCREEN raiz, substitui as saídas NAVIGATION e remove destiny legado em uma transação; undo restaura ambos. Enquanto há relação NAVIGATION, putProperty rejeita destiny explícito. Autonavegação de tela e ciclos continuam permitidos semanticamente; a rejeição geométrica de IDs iguais não altera o compilador.

`compile()` preserva o preview de árvore existente. `compileJourney(providerType)` é explícito: o consumidor deve registrar esse provider real e conservar todos os artefatos, incluindo graph.json e screens/*.json. A existência dessa API não instala navegação no host nem exporta automaticamente os arquivos da jornada. Nenhum método customizado é declarado por esse helper.

### Jornadas no host local

O Studio usa compilePreview: árvore sem navegação conserva o caminho anterior; múltiplas telas ou nós de navegação selecionam compileJourney com provider compiled-preview. O host registra SDNavigation somente na jornada e resolve destinos exclusivamente no mapa de artefatos em memória. Preview de jornada é de execução; manipulação entre telas pertence ao canvas macro. Cada revisão/abertura descarta o estado de formulário do preview anterior.

O Inspector cria update, ifState, then, goTo e goBack, em eventos/ramos tipados; goTo e sua relação são inseridos atomicamente. Ações customizadas e transições não suportadas não são oferecidas. Telas desconectadas continuam rascunhos diagnosticados pelo compilador, sem exclusão automática.

A exportação de árvore mantém humming-contract.json. Jornada usa humming-journey.json, envelope local formatVersion=1 com CompiledContracts completo (entryDestination, providerType, artifacts e sourceNodeIds). CompiledBundleCodec ordena mapas, valida manifesto/caminhos gerados e payloads, limita 256 artefatos, 4 milhões de caracteres de contratos e 8 milhões no envelope. Esse envelope é transporte local; o runtime continua recebendo cada artefato no wire existente através do provider registrado pelo host. O arquivo autoral continua separado em humming-document.json; não há importação automática de pacote compilado como documento editável.

### Canvas de interações do sample

InteractionProjection deriva NavigationLink por evento e aresta NAVIGATION alcançável por ACTION_FLOW. Ramos representam possibilidades, não uma previsão do estado de execução. Subárvores compartilhadas geram uma ligação por evento/destino autoral; a projeção ordena IDs, usa visited e limita 4096 ligações/100000 visitas. Excesso retorna INTERACTION_LIMIT sem resultado parcial silencioso.

InteractionCanvas desenha cartões de 280×240 dp com o renderer existente, recortando conteúdo a esse tamanho inicial. PreviewGeometry mede o componente interno; os cartões de destino são medidos em LayoutCoordinates e copiados para retângulos. As duas medidas são convertidas pelo mesmo CanvasViewport. Âncoras/curvas/setas e flatten são derivados dessas medidas, sem persistência de pixels. Posição dos cartões usa canvasPosition (editável no Inspector, com undo) ou ordenação determinística para o fallback.

O canvas separa seleção de execução: seu overlay consome o ponteiro, seleciona a relação NAVIGATION pela curva ou o componente pelos bounds e não aciona o onClick do formulário. Zoom/pan são locais; movimento acima de touchSlop, Escape, múltiplos ponteiros ou perda de foco cancelam a seleção pendente. A aproximação sem tolerância suficiente exibe diagnóstico e desativa hit daquela curva. A seleção de uma curva abre a ação/destino no Inspector; conexões são criadas/editadas por portas no Inspector, sem gesto de arraste para criar arestas neste recorte.

GoBack depende do histórico em execução e não ganha destino estático fictício. Autoconexão em que origem visual e tela destino têm o mesmo ID é rejeitada pelos comandos do editor; importações existentes continuam compiláveis e exibem SELF_CONNECTION_UNSUPPORTED no canvas. Botão interno → tela que o contém e ciclos entre telas são permitidos. Não há roteamento de obstáculos. Artefatos de execução e rascunho autoral continuam separados.

## Autoria das propriedades reativas

A paleta de inserção inclui layout:reactiveText, reactiveButton e validatedTextField. A inserção produz envelopes válidos, fallback explícito e chave canônica distinta por campo; botões recebem um filho de texto. Inspector edita EXPRESSION_TEXT/EXPRESSION_BOOLEAN como objetos JSON limitados e tokens como nomes literais. Alterações passam pelo schema e histórico existentes; importação/exportação preserva os envelopes, sem converter bindings legados em expressões.

LocalWorkspace.presentationTokens contém o registro instalado para o preview. compile/compileJourney declaram seus nomes em TargetCapabilities; tokens usados e não declarados impedem compilação. O host de CompiledPreview fornece esse mesmo registro por LocalPresentationTokens. O bundle compilado transporta os nomes utilizados, sem instalar código ou o registro de apresentação no destinatário; outros hosts devem declarar capacidades e instalar seus próprios registros compatíveis. Envelopes e políticas de apresentação estão no [contrato](contract.md).

## Workspace com outbox remota

LocalWorkspace.attachRemote conecta um RemoteEditor já carregado/recuperado pelo host. Comandos do Inspector/drag/relações entram na mesma outbox e atualizam o preview otimista; refreshRemote reconcilia o snapshot visível após transição do protocolo. Abrir/importar/criar outro documento fica bloqueado enquanto esse workspace está conectado. Undo/redo remoto exige política concorrente própria e não usa o histórico local como se fosse autoridade do servidor.

LocalStudio mostra revisão confirmada, quantidade pendente, pausa e recuperação ainda não confirmada pelo storage. Exportar recuperação solicita humming-remote-checkpoint.json, mantendo a distinção entre iniciar download e comprovar gravação. A existência dessa UI/attach não instala transporte, autenticação ou persistência automaticamente: o host deve carregar a definição, salvar checkpoint, enviar/confirmar sequencialmente e notificar a UI. Status e parte restante da integração ficam somente no estado atual.

### Host remoto explícito do Studio

O modo desktop é opt-in: .\gradlew.bat :sample:composeApp:run --args="--remote-studio". A tarefa run/--args deve ser conferida no build atual. Configure no ambiente do processo:

| Variável | Conteúdo |
| --- | --- |
| HUMMING_STUDIO_ORIGIN | Origin HTTP loopback do host, sem path/query/credencial, com porta explícita |
| HUMMING_STUDIO_CONNECTION | Alias estável e exclusivo do servidor lógico, para vincular recuperação; não reutilizar para outro banco/servidor |
| HUMMING_STUDIO_TENANT | Tenant autorizado |
| HUMMING_STUDIO_DOCUMENT | ID de rascunho já criado no servidor |
| HUMMING_STUDIO_TOKEN | Bearer em memória; não colocar em arquivo/URL/checkpoint |
| HUMMING_STUDIO_CHECKPOINT | Caminho explícito do arquivo local de recuperação, fora de arquivos versionados |

O host servidor local usa sua configuração PostgreSQL/HUMMING_LOCAL_TOKEN descrita no contrato. O principal runLocal inicial só autoriza local/example; criar o rascunho com documentId=example e revisão 0 pela API antes de abrir esse recorte. Consultar a porta realmente impressa. O alias da conexão deve continuar apontando ao mesmo serviço lógico mesmo que a porta local mude; não é autenticação de servidor por si.

LocalStudio recebe RemoteStudioHost e seu workspace explicitamente. Após cada revisão local, tenta salvar recuperação e mostra a confirmação do storage. Sincronizar pendências executa flush ordenado/retry, exibindo revisão confirmada, pendências e pausa. Ao reabrir com outbox existente, ela é restaurada antes de qualquer substituição por leitura remota, para recuperar recibo perdido. Em conflito/rejeição, Arquivar e usar remoto exige confirmação do usuário, grava cópia das pendências e só então adota a definição remota. Falha ao arquivar conserva o preview local. O download de Exportar recuperação continua distinto da gravação efetiva do storage.

O adapter desktop fecha HTTP ao sair. Workspace/preview não instalam métodos de negócio nem rede no renderer; a API de edição pertence ao host. Browser padrão permanece local até receber adapters e qualificação próprios. Colaboração remota usa a política abaixo.

### Acompanhamento e undo remoto

Acompanhar editores inicia polling a cada 2s enquanto habilitado na composição. Mostra presença por actor e indisponibilidade; não sincroniza automaticamente comandos locais. Ao desabilitar tenta remover presença; saída abrupta expira em 30s. Feed limpo aplica patches contíguos; lacuna carrega snapshot. Pendências locais permanecem protegidas.

Desfazer último envio cria compensação do último comando próprio reconhecido nesta conexão. Só fica habilitado sem pendências; grava intenção antes de enviar. Alteração de outro editor no mesmo nó/aresta bloqueia undo integralmente, mesmo que seja outro campo. Alteração em outro nó é preservada. Falha mantém intenção para retry ou arquivo/resolução explícita. Não há redo remoto ou histórico multínivel remoto na UI. Dois editores devem usar checkpoints distintos e actors atribuídos pelo host; runLocal de demonstração não é provisionamento multiusuário.

## Jornada de projeto

O [criador de apps](app-builder.md) acrescenta metadados versionados e um shell de autoria sobre o mesmo documento e comandos. O envelope de projeto não substitui o formato autoral nem o pacote compilado.

## Drop em containers declarados pelo schema

DropGeometry.visualSlots deriva os destinos de childrenRole=COMPONENT e propriedades NODES/COMPONENT editáveis. PreviewGeometry fornece bounds e candidatos por slot; o planejamento usa o container compatível mais profundo. Row/Column e containers com fluxo observado usam seus eixos e centros; destinos sem eixo definido inserem no fim sem inventar uma direção de layout. Por exemplo, Text pode ser movido ou inserido dentro de Button. Nenhum filho é adicionado a uma propriedade de ação. Preferências de slot do Inspector são mantidas por nó, separadas da seleção durante o gesto.

## Fluxo observado e guia

NodeBoundsObserver pode receber NodeContentFlow do renderer. PreviewGeometry converte essa observação em eixo do container; o documento não persiste essa informação. Fluxo horizontal usa guia vertical; vertical usa horizontal. DropGuideKind.CONTAINER representa inclusão por contorno para destinos sem eixo linear conhecido. O mesmo DropPlan determina guia, gap e comando final.

## Drag-and-drop estrutural na árvore

TreeDrop calcula BEFORE/INSIDE/AFTER com base em AuthoringIndices e slots visuais de schema. O plano guarda documento/revisão e um MoveNode com âncoras de irmãos, excluindo a origem; commit aplica uma transação guardada e usa o histórico existente. Ciclos, ações como filhos de views e reparenting de raízes são rejeitados. EditorTree compartilha seleção, marcas, captura no viewport da lista, autoscroll e cancelamento entre AppBuilder e LocalStudio. A captura pertence à lista para sobreviver à virtualização da linha de origem.

## Inspector de propriedades

O Inspector compartilhado lista todo o schema com busca e grupos Componente, Modifier e Slots e eventos. Campos ausentes são descobríveis. Escalares usam validação/histórico; brushes e filtros são JSON tipado. Relações continuam pela árvore/painel de ações. Mapping e limites em [Propriedades e Modifier](modifiers.md).
