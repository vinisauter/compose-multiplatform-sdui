package me.next.serverdriven.compose.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import me.next.serverdriven.compose.UIResult
import me.next.serverdriven.compose.loadComponent
import me.next.serverdriven.compose.produceUiState
import me.next.serverdriven.core.tree.ServerDrivenNode
import me.next.serverdriven.utils.toNode
import me.next.template.ErrorDefault
import me.next.template.LoadingDefault

@Composable
fun SDCJsonStateUiProvider(
    modifier: Modifier = Modifier,
    json: String,
    loading: @Composable () -> Unit = {
        LoadingDefault(modifier)
    },
    error: @Composable (throwable: Throwable) -> Unit = { throwable ->
        ErrorDefault(throwable, modifier)
    }
) {
    val state by produceUiState {
        val node = Json.decodeFromString<JsonObject>(json)
        node.toNode()
    }
    SDCJsonUiProvider(state = state, loading = loading, error = error) {
        loadComponent(node = it)
    }
}

@Composable
fun SDCJsonUiProvider(
    state: UIResult<ServerDrivenNode>,
    loading: @Composable () -> Unit,
    error: @Composable (throwable: Throwable) -> Unit,
    success: @Composable (ServerDrivenNode) -> Unit,
) {
    when (state) {
        is UIResult.Error -> error(state.throwable)
        UIResult.Loading -> loading()
        is UIResult.Success -> success(state.result)
    }
}