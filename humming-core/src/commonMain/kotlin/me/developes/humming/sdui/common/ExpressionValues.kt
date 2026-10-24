package me.developes.humming.sdui.common

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonPrimitive

/** A payload failure retains an explicit fallback and its location. Programming defects still propagate. */
data class ExpressionValue(val value: SduiValue, val diagnostic: NodeDiagnostic? = null)

/**
 * Cold, consumer-owned stream. No ViewModel job or subscription is allocated until collection.
 * This API does not reinterpret legacy strings or install a property in a component schema.
 */
fun SDUIFormViewModel.expressionValues(
    spec: ExpressionSpec,
    node: ServerDrivenNode,
    propertyName: String,
    fallback: SduiValue,
): Flow<ExpressionValue> {
    fun failure(diagnostic: NodeDiagnostic): ExpressionValue = ExpressionValue(fallback,
        node.diagnostic(diagnostic.code, diagnostic.message, diagnostic.operation,
            "${node.sourcePath}.properties[${JsonPrimitive(propertyName)}]${diagnostic.path.removePrefix("$")}"))

    val program = try { compileExpression(spec) } catch (failure: PayloadException) {
        return flowOf(failure(failure.diagnostic))
    }
    val dependencies = program.dependencies
    if (dependencies.readsScope("response") || dependencies.readsScope("error")) {
        return flowOf(failure(NodeDiagnostic("EXPRESSION_PROPERTY_SCOPE",
            "Property expressions cannot retain temporary action response/error scopes", "bindExpression")))
    }
    fun stateSnapshot(): SduiValue.ObjectValue = SduiValue.ObjectValue(
        if (dependencies.readsAll("state")) stateValues.toMap()
        else dependencies.keys("state").mapNotNull { key ->
            stateValue(key).takeUnless { it === SduiValue.Missing }?.let { key to it }
        }.toMap())

    return onStateUpdated.map { stateSnapshot() }.distinctUntilChanged().map { state ->
        val context = currentCoroutineContext()
        context.ensureActive()
        try {
            ExpressionValue(program.evaluate(ExpressionContext(state, screenInputs.server, screenInputs.nav)) {
                context.ensureActive()
            })
        } catch (error: PayloadException) { failure(error.diagnostic) }
    }
}
