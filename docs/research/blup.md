# Blup: pesquisa e implicações para HummingUI

Pesquisa solicitada em 2026-10-26 sobre o site e a instalação local, com complemento técnico em 2026-10-27. Este documento reúne fontes, limites e recomendações; status e retomada ficam em [estado atual](../current-state.md). Não altera contrato, roadmap aprovado ou implementação. A comparação inicial abaixo retrata T29/T30; o complemento ao final considera T36.

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

## Complemento: estrutura Dart AOT e revisão da comparação

### Dois artefatos, duas rotas

O executável `Blup.exe` é PE x86_64, mas `data/app.so` é **ELF64 little endian, máquina 62 (x86-64)**. A extensão não foi usada para decidir o formato: foram lidos o cabeçalho ELF, a tabela de seções e a tabela dinâmica de símbolos. O SHA-256 continua igual ao de L03. Logo, o suporte PE do Ghidra/REA no Windows não estabelece suporte à decompilação desse ELF. Os comandos `identify_flutter_build` e `inspect_dart_aot` anunciados pelo servidor recebem APK; não se passou um arquivo Windows/ELF como se fosse APK.

O novo [script Python](../../tools/research/inspect_blup_aot.py) usa somente a biblioteca padrão, não executa o alvo e não precisa de pacotes instalados. Resolve os símbolos para regiões file-backed, valida seus limites e pesquisa URIs somente dentro de `_kDartIsolateSnapshotData`. Não é um desserializador do heap Dart nem um decompilador de funções. Resultados completos de metadados: [blup-aot.json](blup-aot.json).

```powershell
python tools/research/inspect_blup_aot.py "C:\Program Files (x86)\Blup\data\app.so" --output docs/research/blup-aot.json
```

| Símbolo observado em `.dynsym` | Offset no arquivo | Bytes | Seção |
| --- | ---: | ---: | --- |
| `_kDartVmSnapshotData` | 832 | 36.560 | .rodata |
| `_kDartIsolateSnapshotData` | 37.440 | 16.420.496 | .rodata |
| `_kDartVmSnapshotInstructions` | 16.465.920 | 27.136 | .text |
| `_kDartIsolateSnapshotInstructions` | 16.493.056 | 25.550.784 | .text |
| `_kDartSnapshotBuildId` | 456 | 32 | .note.gnu.build-id |

Esses tamanhos são `st_size` dos símbolos, não quantidade de classes/funções recuperadas. O inventário inclui hashes por região, 3.931 URIs de pacote distintas e uma seleção de 105 referências relevantes, todas com primeiro offset. A presença literal de URI não prova que o arquivo foi executado ou que seu código está integralmente recuperável. `visual_builder` aparece em 722 URIs; `ide`, em 99. Não são versões nem árvores completas de dependências.

### O que as referências acrescentam

| Referência dentro de `package:visual_builder/` | Primeiro offset | Interpretação limitada e implicação para HummingUI |
| --- | ---: | --- |
| `widgets/blup_designer/components/components_utils/group/smart_layout_values.dart` | 12.713.936 | Referência a Smart Layout; priorizar regras explícitas de sizing/alinhamento e feedback de drop coerente |
| `widgets/blup_designer/guiding_lines/points_calculation_logic.dart` | 9.956.288 | Referência a cálculo de guias; candidato a snapping e medidas de espaçamento derivadas de bounds, sem mutar durante gesto |
| `widgets/blup_designer/undo_redo/undo_redo.dart` | 8.696.736 | Referência a undo/redo; HummingUI já possui comandos/histórico, portanto reutilizá-los |
| `widgets/blup_designer/library_widget/wid_lib/save_dialog.dart` | 8.681.456 | Referência a salvar item de biblioteca; sugere investigar autoria de composições reutilizáveis além do catálogo oficial |
| `widgets/blup_designer/components/components_utils/list/list_comp_save_and_load_data.dart` | 11.285.152 | Referência a persistência de dados da lista; não revela formato, atomicidade ou política de recuperação |
| `widgets/blup_designer/components/components_utils/list/list_comp_codegen.dart` | 11.497.168 | Referência a geração de código da lista; reforça o cuidado em distinguir compilação de apps de distribuição SDUI |
| `widgets/blup_lightning/utils/node_point/abstracts_n_mixins/mixins/np_error_analyser.dart` | 12.313.728 | Referência a analisador de erros de portas; oportunidade de diagnósticos de conexão antes do export |
| `widgets/blup_lightning/utils/node_point/node_point_utils/bl_code_gen.dart` | 11.419.024 | Referência a codegen associado às portas; não estabelece algoritmo nem scheduling |
| `widgets/blup_top_menu/import_figma/smart_layout_alignment.dart` | 9.309.968 | Referência a adaptação de layout na importação; um importador HummingUI precisaria converter semântica, não só coordenadas |

As inferências acima orientam decisões próprias de produto. Não afirmam que os algoritmos do Blup foram decompilados. A associação entre nome do arquivo e responsabilidade é uma hipótese sustentada pelo nome, sem grafo de chamadas Dart.

### Recomendações após T36

O código atual de `PropertyInspector` já inclui busca, grupos, enum/boolean e preservação de bindings. O registro T36 documenta ampliação de Modifier e validação de brushes/filtros. A prioridade 1 original de Inspector está **parcialmente atendida** e não deve virar uma nova implementação duplicada. Restam interações especializadas como edição visual de cores/gradientes, sizing/alinhamento e seleção assistida de bindings; a qualificação visual por plataforma continua separada.

Ordem sugerida para novos recortes:

1. **Recuperação do projeto local.** O código/documentação atuais ainda distinguem download solicitado de gravação confirmada e não têm autosave. Manter a proposta de storage por projeto/revisão, arquivo corrompido preservado e falha de quota/IO explícita.
2. **Console com foco no nó e estado da execução.** Reutilizar diagnósticos/executor e acrescentar correlação entre nó autoral, nó compilado, execução e entrada de navegação. Evitar logging de dados pessoais por padrão.
3. **Biblioteca de composições do usuário.** Reutilizar `insertFragment`/comandos, com IDs e chaves remapeados. Aceite: salvar uma composição com estado/ações, inseri-la duas vezes sem colisão, desfazer/refazer e reabrir arquivo. Definir primeiro se inserções são cópias independentes ou instâncias vinculadas; não prometer atualização global sem esse contrato.
4. **Bindings e listas com dados de exemplo identificados.** Editar tipos/escopos e simular vazio/loading/erro de forma explícita. Usar fontes registradas pelo host para execução real; dados de design não comprovam integração.
5. **Guias, snapping e navegação no canvas.** Complementar as entregas T30–T35 com limiar estável em pixels de tela, zoom/densidade e cancelamento testados. Posição macro, layout executável e ordem de siblings continuam distintos.

Nenhum desses recortes foi implementado pela pesquisa. A conta ausente impede demonstrar no Blup salvar/reabrir, comportamento do undo, biblioteca e importação; não se presume acesso nem se cria conta automaticamente.

### Prova nativa do provedor

Ghidra 12.1.4 foi instalado a partir do ZIP oficial com SHA-256 verificado, reutilizando o JDK existente. A configuração foi restrita ao servidor REA do Codex, com backup e sem substituição de skills. A sessão MCP posterior também reconheceu o provedor. Isso corrige a indisponibilidade original descrita em L01–L07, mas não torna todas as rotas de artefato suportadas.

O próprio `Blup.exe` teve o ponto de entrada `0x14066e960` analisado com sucesso. O corpo observado tem 18 bytes; o pseudocódigo inicializa `__security_init_cookie` e chama `FUN_14066e7ec`. É código de inicialização, sem prova de regras do editor. Evidence ID: `ev_6ae9159ca5dc694aafa74e1c662ded262f92b21e4356788fe83dd30e630f9528`. A primeira execução atingiu timeout de 330.000 ms; a execução concluída usou prazo de 900.000 ms e exclusão de `WindowsResourceReference`, somente no ambiente daquele processo. Registro integral: `build/blup/ghidra-entry-optimized.json`; snapshot de evidências consultadas: `build/blup/ghidra-optimized.snapshot.json`. O snapshot não é um banco completo de análise Ghidra nem contém o código Dart recuperado. O [resumo nativo](blup-native-proof.json) registra IDs, hashes e perfil para as duas provas.

A DLL `croppy.dll` da mesma instalação exporta `fit_polygon_in_quad` e `fit_polygon_in_quad_on_resize`, observadas na tabela de exports PE. O REA/Ghidra analisou a segunda em `0x18000a2a0`, corpo de 90 bytes, e produziu pseudocódigo e referência para `FUN_180008de0`. O pseudocódigo do wrapper encaminha os argumentos à função interna e copia quatro valores numéricos para o buffer de saída. A interpretação de tipos do decompilador não substitui o protótipo original. Não se analisou a função interna de ajuste nem se observou chamada durante uso do app.

Evidence ID: `ev_b8945f55890f0de4a071bc245f685cdcd7687bcbf75a7db35668d320989d6f93`. SHA-256 da DLL: `428c01b519a59a2bd6996a1b83316dad0154647f61b62ed418027d9efeea4675`. [Resumo selecionado da evidência](blup-native-proof.json), com hash do registro integral retido localmente em `build/blup/ghidra-croppy.json`. Esse JSON resumido não é o bundle REA completo. Perfil: `ghidra-default+disabled:WindowsResourceReference`; tal exclusão é limitação da cobertura, não configuração padrão integral.

A prova demonstra decompilação real de uma biblioteca PE distribuída com o produto. Não demonstra o algoritmo de recorte do Blup, sua interface de assets ou a decompilação das funções Dart em `app.so`. Crop/redimensionamento de mídia permanece uma oportunidade secundária de ferramentas de assets do host, sem colocar essa biblioteca nativa no core compartilhado.

Verificação do extrator ELF: reprodução determinística do inventário, conferência dos 105 offsets selecionados contra os bytes originais e rejeição de três entradas inválidas (texto, cabeçalho truncado e tabela de seções ausente). Uma tentativa inicial de criar fixtures no diretório temporário do sandbox falhou por permissão; a execução final usou `build/blup` e passou. Registro: `build/blup/aot-validation.json`. Não foram executados Gradle, testes do runtime SDUI ou fluxos autenticados do Blup neste recorte.
