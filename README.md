# HummingUI

POC de um cliente de Server-Driven UI em Kotlin Multiplatform e Compose Multiplatform. O cliente interpreta uma árvore JSON e resolve componentes e ações registrados por namespace.

O repositório contém contrato puro, modelo autoral/compilador, runtime, bibliotecas, transporte HTTP opt-in e sample. O Studio local integra árvore/Inspector, arraste, canvas de interações e formulário reativo. Consulte o estado atual para evidências e limites por plataforma. Há também aplicação/host HTTP local opt-in com autorização por documento e adapter PostgreSQL. Publicações imutáveis e compatibilidade por capacidades são separadas dos dados de sessão. O Studio desktop tem conexão remota explícita, recuperação em arquivo, acompanhamento por journal, presença e undo conservador. O navegador permanece editor local; consulte a matriz de qualificação antes de anunciar suporte.

## Comece aqui

- [Índice da documentação](docs/README.md): ordem de leitura e responsabilidade de cada documento.
- [Estado atual e retomada](docs/current-state.md): o que existe, status de cada etapa, bloqueios e próxima ação para continuar o desenvolvimento.
- [Plano de desenvolvimento](docs/development-plan.md): tarefas, dependências e critérios de conclusão.
- [Orientações para agentes](AGENTS.md): regras de trabalho também aplicáveis a contribuições assistidas por IA.

## Preparar o ambiente

Leia [settings.gradle.kts](settings.gradle.kts), os builds dos módulos e o [catálogo de versões](gradle/libs.versions.toml). Use o wrapper no PowerShell para descobrir os projetos e as tarefas disponíveis:

```powershell
.\gradlew.bat projects
.\gradlew.bat :humming-core:tasks --all
.\gradlew.bat :sample:composeApp:tasks --all
```

A tarefa T00 do plano registra o JDK/SDK encontrado, a primeira compilação e a execução local do sample. Os builds declaram toolchain JVM 25 e compatibilidade Java Android 17. Tarefas e targets declarados não comprovam execução em cada plataforma; consulte o [procedimento de validação](docs/validation.md).

O recorte local JVM de T00 já foi verificado. Para reproduzi-lo:

```powershell
.\gradlew.bat :sample:composeApp:run --args=--local-smoke
.\gradlew.bat :sample:composeApp:jvmTest
```

O modo local carrega uma tela de formulário sem rede. O teste verifica edição, binding e ação registrada, gerando uma imagem em `sample/composeApp/build/reports/local-smoke.png`. O modo padrão continua disponível sem o argumento. Consulte o estado atual para a próxima tarefa e os limites da qualificação.

## Editor local no navegador

```powershell
.\gradlew.bat :sample:composeApp:wasmJsBrowserDevelopmentRun
```

Abrir http://127.0.0.1:8080 para o Studio: selecionar pela árvore, editar propriedades por schema, inserir/remover/ordenar, desfazer/refazer, importar JSON, salvar documento e exportar contrato. O preview executa somente capacidades locais. Exemplo de arraste + Modo edição habilitam seleção e movimentação entre containers; há zoom/pan, guia de inserção e cancelamento por Escape. O aceite visual desse recorte está no estado atual. Abrir documento usa o seletor do navegador; downloads exigem preservação pelo usuário. O modo anterior de fixture está em http://127.0.0.1:8080/?preview. Veja o [guia do Studio](docs/studio.md) e a [validação](docs/validation.md) para limites.

## Consumir o contrato sem UI

Use humming-contract para parsing, schemas e serialização sem Compose/Lifecycle. O runtime humming-core o exporta para os consumidores existentes. Para verificar a fronteira headless:

```powershell
.\gradlew.bat :humming-contract:jvmTest
```

Os pacotes públicos são preservados; a migração de build e a necessidade de recompilar consumidores JVM antigos estão no [contrato](docs/contract.md).

## Contribuir

Selecione uma tarefa com pré-requisitos atendidos no plano. Preserve os contratos `namespace:component`, bindings `#{chave}` e os pontos de extensão `SDLibrary`, `ComponentHandler`, `ActionHandler` e `NodeProvider`. Mudanças incompatíveis precisam de migração documentada e exemplos atualizados.

Compile e teste o recorte alterado, registre a plataforma observada e atualize os documentos responsáveis pelo comportamento. Alterações somente documentais exigem revisão de conteúdo, links e diff.
