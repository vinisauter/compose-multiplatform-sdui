# Referência de produto — Stitch Canvas Studio

Os três HTMLs fornecidos pelo usuário são preservados em [design/stitch](../HummingUI-Builder/design/stitch/manifest.json), com hashes SHA-256. São referência visual e protótipo independente, não implementação do runtime HummingUI. Não são importados como código executável pelo renderer. A comparação orienta os critérios no [plano](development-plan.md); status e evidência ficam somente no [estado atual](current-state.md).

## Medidas e linguagem visual

- Workspace: cabeçalho 52 px; rail 60 px; biblioteca aproximadamente 260 px; Inspector 300 px; canvas #F8F9FA com grid de 18 px. Superfícies brancas, bordas #E5E7EB, texto #111827/#6B7280, seleção #2563EB. Controles de 24–32 px e raios 4–12 px.
- Tipografia: Inter 10–14 px nos painéis, JetBrains Mono em estado/JSON/metadados. Fontes incorporadas ao app com licenças OFL; o preview conserva seu Design System próprio.
- Biblioteca: Primitives/Components, busca, grade de duas colunas, árvore inferior com hierarquia e seleção. Preview central em dimensões reais do preset, overlay de seleção e barra contextual.
- Inspector: Design/Interactions/Rules, identidade, Auto Layout, alinhamento, padding, tipografia, cores e bindings. Não substituir essas seções por grandes controles Material padrão; o acesso ao schema completo continua disponível.
- Flows: canvas amplo sem biblioteca lateral, frames de telas, conectores e toolbar flutuante inferior; Inspector de conexão à direita.
- State Manager: escopos à esquerda, tabela de variáveis e regras no centro, telefone em coluna própria. Em larguras insuficientes, rolagem contida ou recolhimento explícito do Inspector deve evitar sobreposição.

## Funcionalidades encontradas e correspondência

| Elemento do HTML | Evidência no protótipo | Correspondência / tarefa de integração |
| --- | --- | --- |
| Primitives, busca, Layer Tree, seleção | Controles e markup, sem script de autoria | Catálogo, árvore e comandos existentes; T39 adequa apresentação |
| Move Up/Down, Duplicate, Bind Variable, remover | Barra contextual visual, sem handlers de edição no HTML | Reordenação/remoção/binding existentes; duplicação transacional e toolbar contextual em T40 |
| Auto Layout, alinhamento, padding, estilos/cores | Controles visuais | Schema, Modifier e Design System existentes; T39 usa esses comandos, T40 aprofunda edição contextual |
| Scopes: inputs, form, server, responses; New Variable/Scope; computed/getters | Valores e contagens fixos | `ScreenInputs`, estado tipado, expressões e ActionValues já existem; T41 integra autoria e inspeção sem outra autoridade |
| Valor/chave editáveis e preview reativo | `inputAmount` e `inputPixKey` disparam `updateState()` | Sandbox ligado ao owner real já existente; T39 aproxima tabela e preview |
| Limite de saldo e bloqueio do CTA | JavaScript compara amount com constante 14850.20 e muda erro/botão | T42: regras autorais pelo avaliador/contrato limitado, sem copiar JavaScript para payload |
| Reset e JSON | Reset coloca 500.00 e chave fixa; `JSON.stringify` mostra snapshot inteiro | Reset do Builder restaura defaults do documento; JSON do owner real. Diff/histórico em T45 |
| Instant toggle | Elemento visual capturado pelo script, mas sem listener; JSON força `is_instant: true` | Não tratar como toggle funcional do protótipo. Switch do Builder usa handler real |
| Regex de Pix e lookup remoto | Texto “Matches Regex” e endpoint, sem execução correspondente | T42/T43: validação limitada e capacidade HTTP registrada; não instalar endpoint Pix fictício |
| API Step, Branch, success/error, retry e toast | Cards e curvas SVG fixas, sem executor no HTML | Network/ações estruturadas já existem; T43 cria projeção/autoria dos nós e portas |
| Slide Right, Push View, Bottom Sheet | Seletor visual sem comportamento | T44 integra somente transições que o host realmente oferecer |
| Eager/On Demand, optimistic rollback e mapeamento | Chips e texto, sem política executável no HTML | Cache de jornada e dados tipados existentes; T44/T43 integram políticas e mapeamentos verificados |
| Live Sync, avatares, latência/200 OK e Deploy | Indicadores estáticos e botões sem transporte | T46/T47 integram os serviços opt-in existentes; não anunciar operação remota ativa |
| Share Proof/Done e ajustes +50/+100 | Botões do exemplo financeiro, sem handlers no HTML | Composições demonstradoras locais; sem transferência financeira real |

## Limites que a referência não resolve

O HTML não define serialização autoral, compatibilidade de clientes, ownership/cancelamento, segurança de endpoints, histórico transacional, persistência ou protocolo multiusuário. Esses contratos continuam pertencendo ao projeto. “Zero-Latency”, “Live Sync”, nomes de plataformas e números de latência são texto de apresentação, sem medição ou integração comprovada. O “State Diff Debugger” do script é um snapshot JSON, não um diff nem time travel. O limite diário exibido é 10000, mas o script compara somente com o saldo 14850.20.
