package me.developes.humming.sdui.common

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

enum class ActionFailurePolicy { CONTINUE, STOP_ON_ERROR }

data class ActionExecutionLimits(val maxActions: Int = 10_000, val maxDepth: Int = 64) {
    init { require(maxActions > 0 && maxDepth in 1..256) }
}

data class ActionExecutionResult(val attempted: Int, val errors: List<ErrorData>) {
    val isSuccess: Boolean get() = errors.isEmpty()
}

private class SequenceAborted : Exception()

private class ExecutionState(
    val policy: ActionFailurePolicy,
    val limits: ActionExecutionLimits,
) {
    val mutex = Mutex()
    var attempted = 0
    val errors = mutableListOf<ErrorData>()
}

private class ExecutionFrame(val state: ExecutionState, val depth: Int) :
    AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<ExecutionFrame>
}

internal class ActionExecutor(
    private val resolve: (ServerDrivenNode) -> ActionHandler?,
    private val owner: SDUIFormViewModel,
) {
    suspend fun execute(
        actions: List<ServerDrivenNode>,
        policy: ActionFailurePolicy?,
        limits: ActionExecutionLimits?,
    ): ActionExecutionResult {
        val parent = currentCoroutineContext()[ExecutionFrame]
        require(parent == null || policy == null && limits == null) { "Nested executions inherit policy and limits" }
        val state = parent?.state ?: ExecutionState(policy ?: ActionFailurePolicy.CONTINUE, limits ?: ActionExecutionLimits())
        val frame = ExecutionFrame(state, (parent?.depth ?: 0) + 1)
        try {
            withContext(frame) { sequence(actions, frame) }
        } catch (aborted: SequenceAborted) {
            if (parent != null) throw aborted
        }
        return state.mutex.withLock { ActionExecutionResult(state.attempted, state.errors.toList()) }
    }

    private suspend fun sequence(actions: List<ServerDrivenNode>, frame: ExecutionFrame) {
        for (node in actions) {
            currentCoroutineContext().ensureActive()
            val limitCode = frame.state.mutex.withLock {
                when {
                    frame.depth > frame.state.limits.maxDepth -> "EXECUTION_DEPTH_LIMIT"
                    frame.state.attempted >= frame.state.limits.maxActions -> "EXECUTION_ACTION_LIMIT"
                    else -> { frame.state.attempted++; null }
                }
            }
            if (limitCode != null) {
                fail(frame.state, ErrorData(limitCode, "Action execution budget exceeded",
                    node.diagnostic(limitCode, "Action execution budget exceeded", "execute")))
                throw SequenceAborted()
            }
            try {
                val handler = resolve(node)
                if (handler == null) {
                    fail(frame.state, ErrorData.missingAction(node.component).copy(
                        diagnostic = node.diagnostic("MISSING_ACTION", "Action is not registered", "resolve", "${node.sourcePath}.component"),
                    ))
                } else {
                    handler(node, owner)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (aborted: SequenceAborted) {
                throw aborted
            } catch (error: PayloadException) {
                fail(frame.state, ErrorData.payload(error))
            } catch (error: Exception) {
                fail(frame.state, ErrorData.actionError(node.component, "Handler failed").copy(
                    diagnostic = node.diagnostic("HANDLER_FAILURE", "Action handler failed", "actionHandler"),
                    cause = error,
                ))
            }
        }
    }

    private suspend fun fail(state: ExecutionState, error: ErrorData) {
        state.mutex.withLock { state.errors += error }
        owner.setError(error)
        if (state.policy == ActionFailurePolicy.STOP_ON_ERROR) throw SequenceAborted()
    }
}
