# Documentação do HummingUI

Este é o índice de entrada para desenvolvedores e agentes de IA. A documentação descreve o projeto atual e uma evolução incremental. O planejamento foi reorganizado em 2026-10-23; nenhuma etapa de implementação foi concluída por essa revisão documental.

## Ordem de leitura

1. [Orientações de trabalho](../AGENTS.md).
2. [Estado atual](current-state.md), para distinguir código existente de lacunas.
3. [Arquitetura](architecture.md) e [contrato](contract.md), para entender limites e compatibilidade.
4. [Plano de desenvolvimento](development-plan.md), para escolher a próxima tarefa e seus pré-requisitos.
5. [Decisões](decisions.md), mais [Studio](studio.md) quando o recorte envolver autoria visual.
6. [Validação](validation.md), antes de alterar comportamento ou declarar uma entrega concluída.

## Um responsável por assunto

| Documento | Responsabilidade | Quando atualizar |
| --- | --- | --- |
| [Estado atual](current-state.md) | Estado observado, status por etapa, evidências, bloqueios e ponto de retomada | Ao iniciar, avançar, bloquear ou concluir um recorte |
| [Arquitetura](architecture.md) | Limites, dependências e desenho de evolução | Quando responsabilidades ou módulos mudarem |
| [Contrato](contract.md) | Formato existente, compatibilidade e regras de extensão | Quando parsing, nomes, propriedades ou execução mudarem |
| [Studio](studio.md) | Modelo autoral, ordenação, comandos e geometria do editor | Quando a autoria ou a compilação mudar |
| [Plano](development-plan.md) | Definição das tarefas, dependências e critérios de aceite | Quando o escopo ou as dependências mudarem |
| [Decisões](decisions.md) | Escolhas técnicas e decisões necessárias por etapa | Quando houver decisão ou revisão fundamentada |
| [Validação](validation.md) | Evidência necessária, matrizes e registro de execução | Quando os procedimentos verificáveis mudarem |

O [README do projeto](../README.md) é a apresentação resumida. `AGENTS.md` é a entrada operacional. Evite criar outra versão de arquitetura, roadmap ou prompts mestres com instruções concorrentes.

## Catálogos especializados

- [Propriedades e Modifier](modifiers.md): campos do Inspector, mapping Compose, validação e limites de extensão.

- [Pesquisa Blup](research/blup.md): evidências da instalação, fontes oficiais e funcionalidades candidatas para o Studio; não altera o roadmap de implementação.

- [Criador de apps](app-builder.md): jornada, projeto versionado, arquivos e limites do editor.

- [App e catálogo oficial](../sample/README.md): como executar cada parte, fluxos completos e prints reais.

- [Compose Material 3](material3.md): inventário API → wire, estado, eventos, slots e compatibilidade dos componentes.

## Autoridade e status

Instruções do usuário delimitam o trabalho; `AGENTS.md` orienta sua execução. O código, os builds e resultados executados demonstram o estado implementado. O contrato registra compatibilidade, enquanto arquitetura e Studio descrevem também soluções planejadas, identificadas como tais. O documento de estado atual é a única fonte de status e retomada; o plano define o trabalho e seus gates. Uma divergência deve ser resolvida com inspeção do código e registro no documento responsável, sem converter defeitos em requisitos.

Estar listado neste índice torna o material descobrível por leitura e busca dos agentes. Isso não comprova ingestão em índice vetorial ou serviço externo. Não existe configuração de RAG entregue neste trabalho.

Status por etapa no estado atual: **pronta**, **pendente**, **em execução**, **bloqueada** ou **concluída**. Uma proposta técnica pode orientar a próxima tarefa sem ser apresentada como funcionalidade implementada. Uma tarefa só fica concluída quando seus critérios têm evidência; documentação, compilação, teste e observação visual são evidências distintas.

## Próxima ação

Consultar o **ponto de retomada e a próxima etapa** no [estado atual](current-state.md), inclusive quando o pedido for apenas “continue o desenvolvimento”. Na revisão inicial, o ponto de partida técnico é JVM e o sample desktop, seguido de testes compartilhados e correção do runtime. A autoria local pode evoluir sobre o contrato existente, sem depender de servidor ou colaboração. O desenvolvimento de cada recorte deve seguir a solicitação correspondente; este índice não autoriza publicação, infraestrutura ou migrações externas.
