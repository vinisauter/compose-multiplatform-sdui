package sample.app

import me.developes.humming.sdui.studio.*

/** Unconnected screens are authoring content too; strict export remains unchanged. */
internal fun compileBuilderCanvas(workspace: LocalWorkspace, target: TargetCapabilities): AuthoringResult<CompiledContracts> {
    val document = workspace.document
    val results = AuthoringIndices(document).roots.filter { it.kind == AuthorNodeKind.SCREEN }.map { screen ->
        ContractCompiler(workspace.catalog).compileJourney(document.copy(entryNodeId = screen.id), target,
            "compiled-preview", UnreachablePolicy.REPORT)
    }
    val diagnostics = results.flatMap { it.diagnostics }.distinct()
    val values = results.mapNotNull { it.value }
    if (values.size != results.size || values.isEmpty()) return AuthoringResult(null, diagnostics)
    val first = values.first()
    return AuthoringResult(first.copy(artifacts = values.flatMap { it.artifacts.entries }.associate { it.toPair() },
        sourceNodeIds = values.flatMap { it.sourceNodeIds.entries }.associate { it.toPair() }), diagnostics)
}
