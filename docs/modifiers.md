# Propriedades e Modifier

O Inspector do criador e do Studio lê o schema de cada nó. **Todas**, **Componente**, **Modifier** e **Slots e eventos** permitem filtrar por grupo; a busca usa o nome técnico, inclusive em campos ausentes. Aplicar/Remover usa validação, transação e histórico. Selecionar não grava defaults. Remover restaura a ausência.

Slots visuais são editados pela árvore, paleta e **Adicionar em**; eventos pelo painel de ações e conectores. `children` aparece quando o schema permite conteúdo estrutural. Relações não são campos de texto JSON. Propriedades desconhecidas importadas continuam preservadas como somente leitura.

## Mapping declarativo

`LayoutDefinitions.modifiers` define os **59 campos** compartilhados pelos 111 schemas visuais; `ModifierProperties` os aplica. As APIs foram conferidas nas fontes resolvidas Compose 1.8.2 (Size.kt, AspectRatio.kt, Offset.kt, Border.kt) e por compilação. Não são introduzidos tipos Compose no contrato puro.

| Campos | API / semântica |
| --- | --- |
| width, height | width/height em dp |
| minWidth, minHeight | defaultMinSize legado |
| size | size(dp) quadrado |
| maxWidth, maxHeight | widthIn/heightIn com máximo |
| widthInMin, widthInMax, heightInMin, heightInMax | Ranges preferidos por eixo; equivalentes à composição de sizeIn |
| requiredSize, requiredWidth, requiredHeight | Tamanhos obrigatórios, conforme constraints Compose |
| requiredMinWidth, requiredMaxWidth, requiredMinHeight, requiredMaxHeight | requiredSizeIn; equivalentes também a requiredWidthIn/requiredHeightIn |
| fillMaxSize, fillMaxWidth, fillMaxHeight | Frações legadas; texto inválido conserva fallback 1 |
| wrapContentSize | TopStart/TopCenter/TopEnd/CenterStart/Center/CenterEnd/BottomStart/BottomCenter/BottomEnd |
| wrapContentWidth, wrapContentHeight | Start/Center/End e Top/Center/Bottom |
| wrapContentUnbounded | Booleano dos três wrappers de conteúdo |
| aspectRatio, aspectRatioMatchHeightConstraintsFirst | Razão positiva finita e prioridade de constraints de altura |
| offsetX, offsetY | offset em dp, respeita direção de layout |
| absoluteOffsetX, absoluteOffsetY | absoluteOffset em dp |
| paddingAll, paddingHorizontal, paddingVertical, paddingStart, paddingTop, paddingEnd, paddingBottom | dp; lado explícito precede eixo, que precede All |
| modifierAlpha | alpha de 0 a 1; nome separado preserva Image.alpha |
| rotation | rotate em graus |
| scaleX, scaleY | scale; eixo ausente usa 1 |
| zIndex | Ordenação de desenho entre irmãos |
| backgroundColor, backgroundGradient | Background sólido e brush tipado |
| backgroundShape | CircleShape/RectangleShape/RoundedCornerShape/CutCornerShape |
| backgroundShapeSize, backgroundShapeTopStart, backgroundShapeTopEnd, backgroundShapeBottomStart, backgroundShapeBottomEnd | Cantos em dp; Size precede cantos individuais |
| clip, clipToBounds | Clip pela forma do background ou retângulo |
| borderWidth, borderColor | Borda em dp, mesma forma; preto quando cor ausente; sem largura não há borda |
| shadowElevation, shadowClip | Sombra em dp, mesma forma; clip explicitamente opt-in |
| verticalScroll, horizontalScroll | Legado por presença: qualquer valor, inclusive false, ativa; **Remover valor** desativa |

Campos novos são opcionais. Dimensões novas são finitas e não negativas; aspectRatio é positivo; ranges rejeitam máximo menor que mínimo, inclusive valores fracionários. Rotação, escala, offsets e zIndex aceitam negativos finitos. Diagnósticos identificam nó/campo/operação antes da renderização.

## Ordem da cadeia

A ordem fixa é: size → requiredSize/Width/Height → ranges preferidos → ranges obrigatórios → wrapContentSize → máximos → aspectRatio → wrapContentWidth/Height → offset → absoluteOffset → zIndex → alpha → rotate → scale → shadow → clips → cadeia legada (width/height/defaultMinSize/fill/padding/gradient/background) → border → scroll. Ausência não acrescenta operações. A precedência das constraints segue essa ordem; evite combinar size/width ou outros tamanhos conflitantes. O Inspector não modifica outro campo silenciosamente.

Transforms afetam desenho/bounds; esta extensão não qualifica hit-testing exato de contornos rotacionados ou não retangulares.

## Brushes, filtros e cores

backgroundGradient aceita JSON limitado: type linear/radial/sweep/vertical/horizontal e colors com no mínimo duas cores. Opcionais: tileMode Clamp/Repeat/Mirror/Decal; startOffset/endOffset/centerOffset com x/y finitos; radius positivo; startX/endX/startY/endY finitos. Coordenadas do brush são pixels de desenho, não dp. Ausência conserva os defaults Compose (início zero, fim infinito, centro/raio automáticos). Campos desconhecidos são rejeitados.

```json
{"type":"linear","colors":["#6750A4","#D0BCFF"],"startOffset":{"x":0,"y":0},"endOffset":{"x":300,"y":160},"tileMode":"Clamp"}
```

Image.colorFilter aceita tint com color/blendMode opcional (padrão SrcIn), lighting com mul/add ou colorMatrix com exatamente 20 números finitos. O schema valida os blend modes implementados.

```json
{"type":"tint","color":"#FF0000","blendMode":"SrcIn"}
```

No TextField legado, textFieldColor agora controla o container; trailingIconColor o ícone final; underLineColor o indicador inferior. As cores se aplicam em focused/unfocused/disabled/error; ausência mantém defaults Material. Uma cor explícita sobrescreve a cor correspondente de erro, mantendo os demais indicadores de erro.

## Compatibilidade e extensão

Documentos/nomes antigos permanecem; campos novos exigem runtime atualizado. Versões base dos schemas/envelope não mudam por estes campos opcionais; capabilities não negocia cada propriedade. Não enviar campos novos a clientes antigos esperando efeito equivalente. As cores anteriormente inativas do TextField passam a aparecer e os filtros válidos de imagem passam a ser aplicados. Brushes/filtros malformados recebem diagnóstico.

Modifier é uma interface extensível, sem lista fechada de propriedades. weight, align/alignBy de receiver, matchParentSize, menuAnchor, callbacks de pointerInput/layout/foco, animações, nestedScroll e objetos de plataforma precisam de adapters com escopo/lifecycle próprios. Não são campos JSON executáveis nesta superfície. Alguns componentes Material já possuem wrappers específicos; isso não equivale a modificadores universais. Cada extensão deve incluir schema, execução e teste, sem controles sem efeito.
