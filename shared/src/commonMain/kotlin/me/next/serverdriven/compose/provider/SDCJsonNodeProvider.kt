package me.next.serverdriven.compose.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import me.next.serverdriven.compose.produceUiState
import me.next.serverdriven.core.library.defaultError
import me.next.serverdriven.core.library.defaultLoading
import me.next.serverdriven.core.tree.ServerDrivenNode
import me.next.serverdriven.utils.toNode

@Composable
fun SDCJsonNodeProvider(
    json: String,
    loading: @Composable (Modifier) -> Unit = defaultLoading,
    error: @Composable (modifier: Modifier, throwable: Throwable) -> Unit = defaultError,
    block: @Composable (ServerDrivenNode) -> Unit
) {
    val state by produceUiState {
        val node = Json.decodeFromString<JsonObject>(json)
        node.toNode()
    }
    SDCLoaderLayout(state = state, loading = loading, error = error) {
        block.invoke(it)
    }
}