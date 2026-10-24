package me.developes.humming.sdui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** An action sequence owned by the composition that presents its trigger. */
class ActionInvoker internal constructor(
    private val viewModel: SDUIFormViewModel,
    private val scope: CoroutineScope,
) {
    var isRunning by mutableStateOf(false)
        private set

    fun invoke(actions: List<ServerDrivenNode>) {
        if (isRunning) return
        isRunning = true
        scope.launch {
            try { viewModel.executeActions(actions) } finally { isRunning = false }
        }.invokeOnCompletion { isRunning = false }
    }
}

@Composable
fun rememberActionInvoker(viewModel: SDUIFormViewModel): ActionInvoker {
    val scope = rememberCoroutineScope()
    return remember(viewModel, scope) { ActionInvoker(viewModel, scope) }
}
