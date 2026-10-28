# Design System: HummingUI Canvas Studio

## 1. Visual Theme & Atmosphere

Referência: projeto Stitch 2399743288518543485, observado em 2026-10-27. Workspace de software denso e preciso: navegação estreita à esquerda, biblioteca/árvore, canvas pontilhado, Inspector à direita e barra de projeto. As três composições são Builder Workspace, Flows & Logic Canvas e Data & State Manager. Preservar a referência fornecida pelo usuário acima de preferências genéricas da skill.

## 2. Color Palette & Roles

- Brand Accent #2563EB: seleção, controles primários e foco.
- Canvas #F8FAFC: fundo claro de trabalho.
- Surface #FFFFFF: painéis e propriedades.
- Ink #0F172A: texto principal.
- Muted #64748B: metadados e rótulos.
- Border #E2E8F0: divisórias e grid.
- Estados de erro/sucesso usam cores semânticas; não são acentos decorativos.
- Modo escuro adapta superfícies e contraste pelo MaterialTheme; o tema do app editado é independente do tema do Builder.

## 3. Typography Rules

Inter e JetBrains Mono incorporadas em humming-studio-ui com licenças OFL, obtidas do repositório oficial Google Fonts. O shell funciona sem carregar fontes remotas. Títulos contidos, metadados pequenos e hierarquia por peso. Nenhuma fonte serifada no editor.

## 4. Component Stylings

Botões planos, seleção azul discreta, divisórias finas, campos rotulados. Cards somente para conteúdo agrupado ou preview. Nós e árvores refletem IDs reais. Indicadores de revisão e estado devem derivar da sessão. Não exibir colaboradores, latência ou sincronização fictícios.

## 5. Layout Principles

Cabeçalho de 52 dp, rail de 60 dp, biblioteca de 260 dp, centro flexível e Inspector de 300 dp. Canvas independente dos painéis. Painéis longos têm rolagem própria. A interface é uma ferramenta para janelas largas; não anunciar layout mobile qualificado. O preview usa dimensões lógicas de destino, sem prometer simulação nativa.

## 6. Motion & Interaction

Seleção, drag, zoom e conexões usam a geometria existente. Gesto cancelável e histórico transacional. Evitar movimento contínuo decorativo num editor. Estados de foco/seleção devem permanecer legíveis com redução de movimento.

## 7. Anti-Patterns

Não substituir renderer por mock de telefone. Não criar duas autoridades de documento. Não usar brilho neon, gradientes decorativos, sobreposição de campos ou controles que anunciam efeitos externos indisponíveis. Deploy só pode existir quando uma capacidade explícita do host o implementar.
