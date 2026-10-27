# Design System do projeto

No criador de apps, abra **Design System** na barra superior. A configuração é do aplicativo, enquanto a interface do editor conserva sua própria aparência. Projetos antigos sem tema mantêm a renderização anterior até aplicar um tema.

## Jornada

1. Nomeie o tema e escolha LIGHT, DARK ou SYSTEM. SYSTEM segue isSystemInDarkTheme da plataforma; os dois previews sempre mostram light e dark separadamente.
2. Informe a cor de origem em #RRGGBB e use **Gerar light e dark**. Isso substitui as duas paletas, preservando tipografia, formas e espaçamentos.
3. Em **Cores**, escolha a paleta e um dos 36 papéis do catálogo Material resolvido. Edite o hexadecimal e use **Aplicar cor**. Os modos são independentes.
4. Em **Tipografia**, configure família local Default/SansSerif/Serif/Monospace e os 15 estilos: tamanho, altura da linha, peso e espaçamento entre letras. Não há download de fontes.
5. Em **Formas**, configure os cinco raios Material. Em **Espaçamento**, configure xs/sm/md/lg/xl; nos componentes, selecione paddingToken no Inspector. Padding explícito soma-se ao token.
6. Compare os componentes SDUI reais ou use **Mostrar tela atual**. Alterações no rascunho não mudam o projeto. **Desfazer ajuste** reverte até 50 ajustes do rascunho; Cancelar descarta o rascunho.
7. **Aplicar ao projeto** atualiza o aplicativo, paleta de componentes, canvas, jornada e execução local. **Desfazer tema** restaura a configuração anterior sem alterar os nós. O histórico de nós e o de tema são separados.
8. Salve o projeto para preservar o tema. Não há autosave nem confirmação de persistência pelo adapter de download; reabrir o arquivo confirma a recuperação.

Em **Arquivo**, importar/exportar usa **JSON Humming DesignSystem v1**. Não é importação automática dos formatos JSON/CSS/Kotlin do Material Theme Builder. A importação carrega o rascunho; é necessário Aplicar ao projeto. Restaurar padrão também modifica apenas o rascunho.

## Integração

DesignSystem e DesignTextStyle são modelos serializáveis do humming-contract, sem dependência de Compose. DesignSystemCodec valida versões, chaves, cores, intervalos e orçamento JSON antes de aplicar. Cores são hexadecimais opacos #RRGGBB. Chaves desconhecidas e enums não registrados são rejeitados.

ProjectTheme é o adapter opt-in do humming-layout-material3: resolve o modo, instala MaterialTheme (ColorScheme, Typography e Shapes), LocalThemeComposition, LocalProjectDesignSystem e os tokens existentes content/onSurface e positive/tertiary. Cores e máscaras adicionais registradas pelo host são preservadas; somente content e positive são remapeadas. O core continua usando ThemeProvider e não depende de Material.

Os componentes Material reais herdam os papéis/estilos/Shapes que suas APIs padrão consultam. Text, reactiveText e TextField legados herdam tipografia no modo de projeto quando o payload não fornece tamanho/peso. Sem Design System, conservam os defaults legados. Cores, tamanhos, formas explícitas e layout:style continuam tendo precedência; botões legados com formas fixas não são reescritos. Temas não removem overrides do documento nem convertem valores literais em tokens automaticamente.

paddingToken é um modificador declarativo com valores xs/sm/md/lg/xl. Aplica padding antes do padding literal. Ausência não acrescenta operação; sem provider de projeto, os valores padrão são 4/8/16/24/32 dp. As 60 propriedades de Modifier estão listadas em [modifiers.md](modifiers.md).

## Arquivos e compatibilidade

| Arquivo | Contrato |
| --- | --- |
| Projeto sem tema | AppProject v1, wire anterior preservado |
| Projeto com tema | AppProject v2, campo designSystem; AuthoringDocument/nós não mudam |
| Tema avulso | DesignSystem v1, configuração completa validada |
| Pacote SDUI sem tema | CompiledBundle v1; campo novo não é emitido |
| Pacote SDUI com tema | CompiledBundle v2, contracts.designSystem, mesmos artefatos SDUI |

AppProjectCodec e CompiledBundleCodec leem v1 e v2. Clientes antigos não devem consumir os envelopes v2. O documento autoral avulso contém somente a estrutura e não inclui metadados do projeto. O pacote SDUI v2 é o caminho para executar a jornada exportada com seu tema.

O CompiledPreview do sample consome o tema incluído no pacote; para outro host, decodifique o pacote, valide capacidades e instale contracts.designSystem?.let(::ProjectTheme) ?: Material3Theme() como HummingSDUI.Config.theme. Ausência mantém o ThemeProvider anterior. Nenhum backend ou serviço é instalado implicitamente.

## Geração e contraste

A referência de organização é o [Material Theme Builder oficial](https://github.com/material-foundation/material-theme-builder/blob/main/README.md), com papéis de cores, escalas tipográficas, comparação e exportação. A implementação local **não usa HCT/CAM16, harmonização oficial, extração de imagem ou dynamic color de wallpaper**.

O gerador Humming v1 interpola canais sRGB com preto/branco. Para t ≥ 0,5, tone(c,t) = mix(c,branco,2t−1); para t < 0,5, tone(c,t) = mix(preto,c,2t). Secondary e tertiary misturam a origem com cinza e teal; neutrals usam 94% de cinza. Os níveis e papéis são determinísticos em generateThemeColors; valores gerados ficam salvos, sem regenerar ao abrir.

Para cada papel de texto gerado, escolhe-se preto ou branco pelo maior contraste. O verificador usa canais sRGB linearizados: c/12,92 quando c ≤ 0,04045, senão ((c+0,055)/1,055)^2,4; L=0,2126R+0,7152G+0,0722B; contraste=(Lmax+0,05)/(Lmin+0,05). Sobrescritas manuais podem baixar o contraste: são permitidas com aviso, sem correção silenciosa. O limite de aviso para os 12 pares de texto é 4,5:1, comparado sem arredondamento, conforme a referência [WCAG sobre contraste de texto](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html). Isso não certifica a acessibilidade da tela, imagens, estados ou combinações arbitrárias.

## Limites

O Design System é local e não sincroniza metadados com os serviços de colaboração/publicação de documentos. Semântica do tema SYSTEM depende da plataforma; compilar um target não prova a troca de preferência pelo sistema operacional. Formas fixas e overrides locais permanecem. Não há editor de fontes remotas, geração HCT ou exportação de código Kotlin/CSS nesta configuração; o artefato operacional é JSON consumido pelo runtime SDUI.
