# Criador visual de apps

O criador reúne projeto, telas, componentes, edição, ações, teste e exportação sobre o documento SDUI do Studio. O [estado atual](current-state.md) registra a qualificação por plataforma. O [guia do app](../sample/README.md) explica como executar cada parte.

## Jornada

1. Abra **Criador de apps** no catálogo, `?builder` no Wasm ou `--builder` no desktop.
2. Em **Novo**, escolha nome e modelo: Em branco, Formulário ou Jornada. Substituir trabalho alterado pede confirmação.
3. **+ Nova tela** permanece visível nas abas Telas e Elementos, inclusive na etapa Ações. Em **Estrutura**, escolha a entrada e renomeie páginas. A árvore seleciona os nós reais.
4. Em **Design**, a paleta mostra miniaturas reais SDUI ao lado dos nomes. Clique para inserir no slot escolhido em **Adicionar em**, ou arraste a miniatura para um Auto Layout medido. A linha rosa indica a posição antes de soltar; soltar fora não insere. Edite propriedades conforme o schema. Arraste elementos, reordene, remova e desfaça/refaça. Escape cancela o gesto; os limites geométricos são os do [Studio](studio.md).
5. No Inspector **Ações**, escolha um evento declarado. Acrescente atualização de estado, navegação para outra tela ou retorno. Selecione a ação conectada para editar suas propriedades. A etapa **Ações** mostra todas as telas, mesmo desconectadas. Puxe uma bolinha azul da borda direita de um componente/tela até outra tela e solte para criar uma navegação. O gesto usa `onClick` quando declarado; caso contrário, o primeiro evento declarado no schema. Para outro evento, use o Inspector. Uma curva rosa acompanha o ponteiro; Escape, perda de foco ou soltar fora cancela. O comando só ocorre ao soltar e pode ser desfeito. Conexões repetidas para a mesma origem/destino são recusadas pelo criador.
6. **Executar app** começa pela entrada do documento, com estado, handlers e navegação reais. Design começa pela tela selecionada. A sessão de execução é independente; editar ou mudar de etapa pode reiniciá-la.
7. Em **Exportar**, corrija os diagnósticos, salve o projeto completo e exporte a jornada SDUI. Reabra o arquivo para continuar a autoria.

**Studio avançado** usa a mesma instância de `LocalWorkspace`. Catálogo e serviços permanecem acessíveis pela saída do criador. Serviços remotos são fluxos opt-in separados; o criador local não sincroniza automaticamente seu projeto.

## Referências

As imagens fornecidas orientaram páginas/árvore à esquerda, superfície central com viewport, Inspector à direita e barra de projeto/histórico/execução. A [página pública Blup Design](https://www.blup.in/design) foi analisada como referência para widgets, propriedades e canvas. Isso não constitui inspeção da implementação interna do produto. O fluxo usa o contrato HummingUI existente.

O pacote exportado é SDUI para um host compatível. IA, importação proprietária, geração de APK/IPA, assinatura, hospedagem e distribuição não compõem essa exportação.

## Projeto e integridade

`AppProject`/`AppProjectCodec` ficam no módulo puro `humming-studio-model`. O envelope fechado versão 1 contém exatamente `formatVersion`, `name`, `viewport`, `screenNames` e `document`. O último campo é o documento completo validado pelo codec autoral existente. Nós, relações, ordenação e entrada têm uma única autoridade. Histórico de undo e estado da execução não são serializados.

Nome e rótulos explícitos têm de 1 a 80 caracteres não vazios; rótulos são únicos e referem somente telas raiz existentes. Presets: PHONE 390×844, TABLET 820×1180, DESKTOP 1280×800, em unidades lógicas Compose. Não simulam plataforma, teclado, safe areas ou capacidades nativas. O codec rejeita versões/campos desconhecidos, referências inválidas e excesso de limites (8 milhões de caracteres e 200 mil valores JSON), com serialização canônica e diagnósticos estruturados.

A paleta lista os 111 componentes do catálogo. Componentes simples usam `insertVisual`. Composições com pais/slots/ações usam fixtures oficiais e `insertFragment`, com novos IDs de nós/arestas/chaves de estado. Campos novos também recebem chave exclusiva; payloads existentes são preservados. Uma composição ocupa um passo de undo. Fragmentos inválidos, colisões, múltiplas telas ou navegação interna são rejeitados. O compilador continua decidindo a validade final.

## Arquivos e recuperação

`StudioFiles` pertence ao host. O projeto completo usa `humming-app-project.json`; a jornada, `humming-app-contracts.json`; o documento isolado, `humming-document.json`. JSON legado e documento isolado são importados pelo Studio avançado, não pelo comando Abrir projeto.

O adapter não confirma persistência do download: salvar não elimina o aviso de alterações. Preserve o arquivo e reabra para confirmar recuperação. Não há autosave nem recuperação após fechar a aba sem exportar. O projeto carregado estabelece a base de comparação. Substituição e saída pelo catálogo são protegidas; fechar janela/aba não tem proteção universal implementada.

## Verificação e próximos recortes

`AppProjectTest` cobre codec, validação, transação e undo/redo. `AppBuilderTest` compila as 111 inserções, testa isolamento e percorre criação de duas telas, propriedades, navegação/retorno, arquivo real, reabertura e pacote compilado. Prints Compose/JVM no guia não substituem qualificação nativa.

O shell é destinado a janelas largas. Abaixo de 850 unidades lógicas o Inspector lateral é ocultado; use Studio avançado nesse tamanho. Novas funções de dados, assets próprios, tema de projeto, histórico persistente ou sincronização do envelope precisam de aceite específico no plano. Mantenha uma autoridade de documento e reutilize comandos, schemas e undo.

## Canvas e inserção gestual

O canvas compila cada tela com política REPORT e reúne seus artefatos somente para autoria. Isso evita esconder a tela nova antes de conectá-la. A exportação continua estrita: telas inalcançáveis precisam ser conectadas ou removidas. Enquadrar ajusta todas as telas; botões movem/alteram o zoom do canvas. Bolinhas usam os bounds reais do renderer; a seta permanente usa a geometria existente e pode ser selecionada. Não há roteamento de obstáculos arbitrários.

A inserção da paleta reutiliza `DropGeometry` de movimento, incluindo clips, transformações, eixo, centros dos irmãos, ordenação e guia. Um nó apenas projetado serve para calcular o gap; nenhum nó é persistido durante o arraste. Inserção simples/composta aplica o gap e verifica revisão/documento/destino em uma única transação. Os destinos gestuais vêm do schema: children com papel COMPONENT e propriedades NODES/COMPONENT editáveis. Row/Column e containers com fluxo informado pelo renderer preservam ordenação por centros; os demais acrescentam ao fim do slot. É possível arrastar Text para dentro de Button, Card, Surface e demais componentes com conteúdo filho declarado. Componentes sem filhos visuais não recebem conteúdo; a busca sobe até um ancestral compatível.

Novas telas usam `fillMaxSize = "1"`, a fração definida pelo contrato, para aceitar drops na região vazia. Projetos anteriores não são reescritos: para expandir uma raiz antiga, selecione a tela e aplique a propriedade fillMaxSize com valor 1 no Inspector. Miniaturas são decorativas, renderizadas pelas fixtures existentes com owners isolados, sem disparo de eventos por clique; o cartão inteiro captura o gesto. A paleta é lazy e descarta owners fora da composição.

Em Ações, os frames respeitam a proporção e as dimensões lógicas do viewport escolhido. Trocar o preset reenquadra o conjunto e ajusta o espaçamento de apresentação, preservando as posições do documento.

## Destinos e slots do drop

A geometria observa os containers visuais e consulta o catálogo do workspace, sem uma lista fixa de nomes permitidos. Slots já ocupados usam a região medida de seus filhos; children usa a área de conteúdo do container. Para direcionar a um slot vazio ou ambíguo, selecione o container e escolha **Adicionar em** no Inspector antes de arrastar. Essa preferência fica por nó e não muda quando o ponteiro seleciona a origem; selecionar durante a captura não pode reiniciar a geometria. O destino compatível mais profundo continua tendo prioridade.

Scaffold, Style e AnimatedVisibility também informam bounds. Conteúdo invisível/fechado não oferece região de drop até ser mostrado. Os slots topBar/bottomBar de Scaffold aceitam a lista de nós declarada pelo schema, preservando ordem e filhos existentes; para compor um layout específico dentro de um slot, use um Row/Column. Ações e propriedades somente escalares não são destinos de views. Proteção contra inserir em si/descendentes, clips, revisão e undo continuam valendo.

## Marca de inserção e fluxo real

A marca representa a disposição dos filhos, não uma direção genérica de drop. Button e as variantes Material de conteúdo Row informam fluxo horizontal: a guia é vertical, indicando antes/depois na mesma linha. Cards informam fluxo vertical: a guia é horizontal. A orientação é observação derivada do renderer, sem propriedade nova no payload e sem mudar os critérios de quem aceita filhos. Row/Column mantêm suas regras.

Quando o eixo não é definido, como em superfícies de sobreposição ou slots genéricos, o editor desenha um contorno rosa no destino para indicar inclusão; não exibe uma linha embaixo que sugira empilhamento vertical. O gesto e a paleta usam a mesma representação.
