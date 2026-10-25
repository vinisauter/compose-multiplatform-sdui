# Orientações para agentes — HummingUI

## Entrada obrigatória

1. Leia o [índice](docs/README.md), o [estado atual](docs/current-state.md) e a [arquitetura](docs/architecture.md).
2. Para continuar o desenvolvimento, siga o ponto de retomada do estado atual: tarefa ativa, bloqueios e próxima ação. Consulte seu detalhe no [plano de desenvolvimento](docs/development-plan.md), conferindo dependências, arquivos afetados e critérios de conclusão. Não reinicie etapas comprovadamente concluídas.
3. Consulte o [contrato](docs/contract.md) e as [decisões](docs/decisions.md). Para o editor, leia também [Studio](docs/studio.md).
4. Examine `git status`, `settings.gradle.kts`, os builds afetados e `gradle/libs.versions.toml`. Preserve alterações locais preexistentes. O código/build é a evidência do estado implementado; divergências com o plano devem ser registradas.

## Escopo e execução

- Comunique-se em português, preservando nomes de APIs, pacotes e o estilo Kotlin existente.
- HummingUI é uma POC de cliente SDUI. Funcionalidades futuras descritas no plano não estão entregues. Esta organização documental não comprova implementação nem validação.
- Trabalhe no recorte solicitado, com mudanças pequenas e verificáveis. Resolva escolhas rotineiras dentro do escopo; registre decisões técnicas e suas razões sem atribuir ao usuário aprovações inexistentes.
- Preserve a extensibilidade por `SDLibrary`, `ComponentHandler`, `ActionHandler` e `NodeProvider`. O core não deve depender de Material 3, backend específico ou regras de negócio do sample.
- Código compartilhável pertence a `commonMain`; APIs de plataforma ficam nos source sets adequados. Extraia módulos apenas quando houver necessidade concreta e direção de dependências definida.
- Nomes `namespace:component`, propriedades, bindings `#{chave}`, slots/eventos e ordem de `children` são contratos. Mudanças incompatíveis exigem estratégia de migração, fixtures e exemplos atualizados.
- Payloads só podem resolver componentes, ações e métodos conhecidos pelo cliente. Não os transforme em execução arbitrária de código.
- Ao alterar assincronismo, propague cancelamento, defina a política de falha de sequências, aguarde operações dependentes e finalize recursos/contadores mesmo em erro. Não esconda defeitos de programação como erros de payload.
- Diagnósticos devem identificar nó, caminho e operação. Defina fallback explicitamente. Fakes são úteis nos testes; não substituem integrações reais nos critérios de aceite.
- Não versione segredos, credenciais ou caminhos locais de SDK. Não publique artefatos nem acione distribuição para validar mudanças locais.

## Verificação e conclusão

- Use `.\gradlew.bat` no PowerShell. Descubra as tarefas antes de usá-las. Não atualize dependências por tentativa para contornar um erro sem investigar a causa.
- Toolchain JVM 25 e compatibilidade Java Android 17 são configurações diferentes. Consulte os builds atuais.
- Mudanças de comportamento exigem testes úteis no source set apropriado, verificando sua configuração. Para UI, observe também o fluxo no sample e informe a plataforma.
- Uma compilação JVM não comprova Android, Apple ou browser. Em bloqueios, registre comando, causa e alcance da validação conforme [validação](docs/validation.md).
- Mudanças somente documentais exigem revisão de conteúdo, links locais e diff; não exigem build.
- Atualize o documento responsável; registre status, evidência, trabalho parcial e próxima ação somente em `docs/current-state.md`. Atualize o ponto de retomada antes de encerrar cada recorte. O plano guarda tarefas/dependências/aceite, sem outro status concorrente.
- Ao concluir cada entregável validado, criar um commit local com escopo e evidência coerentes. Não executar push neste trabalho. Preserve alterações alheias e não inclua arquivos sem relação com a entrega.
- Ao encerrar, informe o que mudou, o que foi executado e o que permanece pendente. Não declare testes aprovados sem execução.

## Pontos de atenção existentes

- `humming-network` está incluído: transporte opt-in com HttpClient e endpoints registrados pelo host. O host fecha cliente/engine; não instalar rede implicitamente no Studio. Consultar contrato e evidência por plataforma no estado atual.
- Há exemplos com nomes/providers não registrados. A T00 deve selecionar um fluxo local compatível antes de afirmar que o sample funciona.
- Workflows ainda precisam ser confrontados com os módulos e tarefas reais. Metadados de publicação e targets declarados não comprovam releases ou suporte funcional.
