# Criador visual de apps

O criador reúne projeto, telas, componentes, edição, ações, teste e exportação sobre o documento SDUI do Studio. O [estado atual](current-state.md) registra a qualificação por plataforma. O [guia do app](../sample/README.md) explica como executar cada parte.

## Jornada

1. Abra **Criador de apps** no catálogo, `?builder` no Wasm ou `--builder` no desktop.
2. Em **Novo**, escolha nome e modelo: Em branco, Formulário ou Jornada. Substituir trabalho alterado pede confirmação.
3. Em **Estrutura**, acrescente telas, escolha a entrada e renomeie páginas. A árvore seleciona os nós reais.
4. Em **Design**, busque elementos e escolha o slot em **Adicionar em**. Edite propriedades conforme o schema. Arraste elementos, reordene, remova e desfaça/refaça. Escape cancela o gesto; os limites geométricos são os do [Studio](studio.md).
5. No Inspector **Ações**, escolha um evento declarado. Acrescente atualização de estado, navegação para outra tela ou retorno. Selecione a ação conectada para editar suas propriedades. A etapa **Ações** mostra o grafo e seus conectores.
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
