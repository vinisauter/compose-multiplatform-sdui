# Blup: pesquisa e implicações para HummingUI

Pesquisa solicitada em 2026-10-26 sobre o site e a instalação local. Este documento reúne fontes, limites e recomendações; status e retomada ficam em [estado atual](../current-state.md). Não altera contrato, roadmap aprovado ou implementação.

## Conclusão

O maior ganho para HummingUI é tornar suas capacidades existentes mais fáceis de descobrir, editar e depurar. Já existem documento autoral, comandos/undo, schemas, preview, ações, navegação, transporte e serviços opcionais. Recomeçar esses subsistemas para reproduzir o Blup desperdiçaria essa base. As oportunidades são Inspector visual, depuração ligada ao grafo, recuperação do projeto e autoria de dados/estilos reutilizáveis.

Blup é apresentado como um ambiente de criação de apps Flutter, com design, lógica, backend e publicação. Sua página promete geração de código Flutter. Não foi encontrada nesta pesquisa evidência suficiente para classificá-lo como um runtime SDUI que recebe contratos remotos. A equivalência útil é a experiência de autoria, não uma suposta arquitetura interna idêntica. [Design](https://www.blup.in/design), [visão geral oficial](https://docs.blup.in/).

## Evidência e limites

| ID local | Observação e método | O que permite concluir |
| --- | --- | --- |
| L01 | REA `open_binary` reconheceu `Blup.exe` como PE x86_64; SHA-256 `d920c98cf56029d1757cfa25b890721da6fb624cd646e01a1f32ab0b0b0eedd9` | Identidade do alvo; não houve código decompilado |
| L02 | VersionInfo: FileVersion/ProductVersion `1.54.0`; ProductName `ide` | Metadado declarado pelo executável, sem autenticação da versão comercial |
| L03 | `flutter_windows.dll`, `data/flutter_assets/AssetManifest.json` e `data/app.so` de 42.058.656 bytes; SHA-256 do último `d49387ab1a18c30d7be234226e8dd4b65c84cf238d2509d07618b9c690fad90f` | Estrutura compatível com aplicativo Flutter compilado; não é uma árvore Electron/ASAR |
| L04 | Busca ASCII literal no `app.so`, sem imprimir strings vizinhas | Conceitos presentes no artefato, sem prova de execução; offsets abaixo |
| L05 | ZIP `extensions.zip` contém entradas `extensions/bat/package.json`, `extensions/clojure/package.json` e gramáticas/snippets | Recursos de linguagem de editor empacotados; não prova marketplace funcional de plugins de negócio |
| L06 | DLLs para desktop drop, seleção de arquivos, múltiplas janelas, áudio/vídeo e WebRTC; bibliotecas Firebase/Firestore | Dependências distribuídas; não prova integração habilitada, colaboração funcional ou backend Firebase |
| L07 | Computer Use observou janela Blup em onboarding com diálogo de avaliação; Escape e tentativa de fechar somente o diálogo não liberaram o editor | Editor, criação, salvar/reabrir e execução de um projeto não foram testados |

REA `binary_overview` retornou `provider_unavailable`: Ghidra sem `GHIDRA_INSTALL_DIR`, IDA sem configuração, Hopper incompatível com Windows e Kuna sem configuração/suporte PE nessa rota. Não se instalaram ferramentas nem se alterou configuração. Sessão encerrada com `close_binary`. `open_binary` retornou identidade, sem Evidence ID; L01–L07 são identificadores deste relatório, não IDs REA inventados.

Não houve decompilação de funções, recuperação do código Dart original, tráfego capturado, acesso autenticado a serviços ou importação de projeto. A engenharia reversa realizada é estática e parcial, acompanhada de observação limitada da interface. Conteúdo de configurações e possíveis credenciais não foi copiado nem utilizado.

| Literal L04 | Primeiro offset de byte, decimal |
| --- | ---: |
| AutoLayout | 8854998 |
| Import from Figma | 10907680 |
| Debug Console | 8055697 |
| Minimap | 7388481 |
| NodePoint | 7362900 |
| Choose from Assets | 8202736 |
| onClick | 7693556 |
| pubspec.yaml | 8156873 |

Reproduzir somente a inspeção estática, sem abrir o app:

```powershell
.\tools\research\Inspect-Blup.ps1
```

Inventário completo desta execução: [blup-static.json](blup-static.json). As buscas são case-insensitive, limitadas a literais escolhidos. Strings e dependências não equivalem a comportamento. Uma versão diferente pode mudar os offsets.

## Como o produto funciona segundo as fontes oficiais

**Design.** Páginas/camadas, canvas, widgets e propriedades formam o ambiente de autoria. O Inspector muda conforme o widget. Há controles de cor, tipografia e decoração. O Toolkit documenta seleção, desenho de retângulos/textos e arraste de mídia. Isso sugere uma edição direta com feedback imediato, além de edição por campos. [Design UI](https://docs.blup.in/wiki/design-ui), [Properties Panel](https://docs.blup.in/wiki/design-ui/properties-panel), [Toolkit](https://docs.blup.in/wiki/design-ui/tools-section).

**Composição.** O menu contextual documenta duplicação de componentes/páginas, transferência de estilos, agrupamento, ordem de camadas e conversões para containers roláveis/listas. A oportunidade é reduzir o número de passos para transformar uma seleção em uma composição válida. Não se deve transportar a semântica de sobreposição para a ordem estrutural de qualquer layout. [Menu contextual](https://docs.blup.in/wiki/design-ui/right-click-menu-in-design).

**Logics.** Os nós possuem entradas e saídas; conexões de dados respeitam tipos, enquanto portas de função representam disparo e sequenciamento. A documentação descreve busca com foco no nó, zoom, minimapa e console com erros/prints. Isso separa visualmente fluxo de dados e fluxo de execução. Os detalhes de scheduling, concorrência e cancelamento internos continuam desconhecidos. [Logics](https://docs.blup.in/wiki/logics).

**Dados e importação.** Backend expõe uma experiência de Sheets/SQL; a visão geral o descreve sobre AWS. Importar do Figma usa File ID e token, permite selecionar frames e gera elementos editáveis. Nenhuma dessas integrações foi executada aqui. [Backend](https://docs.blup.in/wiki/backend), [Figma](https://docs.blup.in/import-from-figma).

## Comparação com o código atual

A documentação antiga de outubro já não descreve o checkout atual. `settings.gradle.kts` inclui `humming-contract`, `humming-studio-model`, `humming-network` e os três módulos de servidor. O build do sample usa toolchain 25 e hosts específicos; a pesquisa não executou esses targets.

| Área | Base HummingUI inspecionada | Diferença útil |
| --- | --- | --- |
| Projeto | `AppProject.kt`: envelope v1 fechado, nome/viewport/rótulos/documento | Assets, tema e recuperação persistente exigem desenho/migração próprios |
| Inspector | `LocalStudio.kt`, `PropertyInspector`: seleção de propriedade e `OutlinedTextField` genérico | Editores especializados orientados por schema, sem exigir JSON para tarefas comuns |
| Canvas | `InteractionCanvas.kt`; T30 local em andamento | Busca/foco, minimapa e melhor navegação em documentos grandes, após consolidar os gestos atuais |
| Grafo | `InteractionProjection.kt` projeta relações de navegação; documento distingue EVENT/ACTION_FLOW/NAVIGATION | Autoria de valores tipados e inspeção do fluxo não são resolvidas somente por desenhar mais linhas |
| Execução | `ActionExecution.kt`: executor suspenso, limites, política de falha | Observação correlacionada com nós/arestas e Inspector de estado |
| Persistência | Arquivos locais e recuperação remota opt-in documentados separadamente | Autosave do envelope local completo, sem confundir download solicitado com gravação confirmada |

Pontos de extensão: [modelo](../../humming-studio-model/src/commonMain/kotlin/me/developes/humming/sdui/studio/AppProject.kt), [Inspector](../../sample/composeApp/src/commonMain/kotlin/sample/app/LocalStudio.kt), [canvas](../../sample/composeApp/src/commonMain/kotlin/sample/app/InteractionCanvas.kt), [projeção](../../humming-studio-model/src/commonMain/kotlin/me/developes/humming/sdui/studio/InteractionProjection.kt), [executor](../../humming-core/src/commonMain/kotlin/me/developes/humming/sdui/common/ActionExecution.kt).

## Funcionalidades recomendadas e aceite sugerido

As prioridades abaixo são recomendações da pesquisa, não tarefas aprovadas nem capacidades entregues. Esforço relativo considera a base atual, sem estimativa de prazo.

| Prioridade | Recorte e benefício | Implementação sugerida e dependências | Critério de aceite |
| --- | --- | --- | --- |
| 1 — médio | Inspector visual: editar sem decorar o wire | `PropertyInspector` por tipo/enum/unidade; boolean, cor, espaçamento e seletor de binding. Reutilizar schema e comandos | Editar propriedades → undo/redo → salvar/reabrir → mesmo wire e preview; inválidos com caminho; preservar bindings e valores desconhecidos |
| 1 — médio | Recuperação local: evitar perda ao fechar aba | Storage do host por projeto/revisão; salvar envelope completo; debouncing e confirmação de gravação; adapter browser/JVM separado | Reabrir após encerramento recupera última revisão confirmada; quota/IO/cancelamento não exibem falso sucesso; arquivo corrompido preservado para recuperação |
| 1 — médio | Console ligado ao grafo: entender por que uma ação falhou | Observer opt-in do executor, IDs de execução/nó/entrada e compilador com mapa autoral → wire; buffer limitado e redação de dados | Evento → ação → resultado/erro/cancelamento; clicar log foca nó correto, inclusive expansões; instrumentação não muda sequência/falha nem retém payload pessoal |
| 2 — pequeno/médio | Busca global, foco e minimapa: navegar em projetos maiores | Índices derivados de documento/bounds, reutilizar transformação de viewport; não persistir outra árvore | Encontrar por nome/ID/definição e focar em zoom/densidade distintos; documento grande com orçamento de desenho definido |
| 2 — médio | Duplicar, copiar estilo e envolver em container | Estender comandos/transações sobre `LocalWorkspace`; remapear IDs/arestas/chaves; validar slots | Um undo por operação; bindings internos remapeados; externos preservados; nenhuma relação órfã ou alteração da ordem de siblings |
| 2 — médio/alto | Assets e tema do projeto: consistência e reutilização | Registry de assets no host e tokens existentes; definir envelope v2/migração antes de acrescentar campos ao codec fechado | Mudar token atualiza usos; importar/exportar preserva identidade; recurso ausente tem diagnóstico/fallback; testar limites de mídia |
| 2 — alto | Painel de dados/bindings: conectar conteúdo real sem JSON manual | Seletor `server/nav/state`, amostras explicitamente identificadas, expressão limitada e planos/fontes registrados pelo host | Ausente/null/tipo inválido, loading, vazio, erro/retry e cancelamento demonstrados; sem URL/SQL/credencial arbitrários no contrato |
| 3 — alto | Grafo de dados tipado: visualização e composição de expressões | Primeiro projetar `ExpressionSpec` existente. Novas portas de dados exigem semântica/versionamento do modelo, compilador e capacidades | Conexão incompatível rejeitada antes de exportar; equivalência com expressão textual; limites/ciclos/cancelamento verificados |
| 3 — alto | Importação Figma limitada: aproveitar designs existentes | Primeiro importar representação intermediária local; subset de frames/texto/imagens/layouts para schemas conhecidos; relatório de perdas | Fixture reproduzível; ordem/IDs/slots corretos; elementos não suportados explicitados; resultado compila e reabre sem perda silenciosa |

Ordem sugerida após o recorte ativo: Inspector → recuperação → console; em seguida busca/composição, assets/tema e dados. O novo ponto de entrada de cada implementação deve ser criado no plano com dependências e aceite, sem renumerar T30 durante esta pesquisa.

## Adaptação ao SDUI

Não incorporar execução arbitrária de Dart/SDKs ou SQL dentro dos payloads. O equivalente seguro e extensível continua sendo `SDLibrary`, `ComponentHandler`, `ActionHandler`, `NodeProvider` e capacidades registradas pelo host. Serviços e segredos permanecem fora do documento publicado.

Preservar a separação entre posição de organização no canvas e layout executável. O gesto pode parecer livre, mas o resultado precisa respeitar `placement`, slots e ordem de `children`. Agrupar/envolver exige comando estrutural e não apenas somar retângulos.

Geração de Flutter, IDE completa, build em nuvem e envio às lojas ampliariam substancialmente o produto. Para HummingUI, exportação SDUI e validação de capacidades continuam sendo a entrega apropriada; distribuição de apps não é consequência automática de um editor visual.

## Perguntas ainda abertas

| Pergunta | Cobertura desta pesquisa | Próxima evidência necessária |
| --- | --- | --- |
| Quais conceitos de produto podem ser aproveitados? | Respondida por fontes oficiais e comparação do código | Prototipar um recorte e medir o fluxo |
| O pacote instalado contém referências aos conceitos centrais? | Respondida no limite da inspeção estática | Repetir script se a versão mudar |
| Como Blup serializa projetos, implementa undo, agenda nós e salva? | Não resolvida | Projeto de teste exportado e editor acessível; depois análise nativa se necessária |
| Figma/backend/publicação funcionam nesta instalação/conta? | Não validada | Fluxo real em workspace de teste com acesso do usuário e escopo próprio |
| Blup usa protocolo SDUI remoto? | Não estabelecida | Contrato/tráfego ou fonte técnica primária específica; não inferir pela aparência |

Não há base para alegar clone fiel, formato interoperável ou paridade funcional. As sugestões são decisões de design próprias para HummingUI, fundamentadas nas capacidades descritas e no código local.
