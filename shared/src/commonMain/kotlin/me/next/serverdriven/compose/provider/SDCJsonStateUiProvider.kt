package me.next.serverdriven.compose.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import me.next.serverdriven.compose.loadComponent
import me.next.serverdriven.compose.produceUiState
import me.next.serverdriven.core.library.defaultError
import me.next.serverdriven.core.library.defaultLoading
import me.next.serverdriven.utils.toNode

@Composable
fun SDCJsonStateUiProvider(
    modifier: Modifier = Modifier,
    json: String,
    loading: @Composable (Modifier) -> Unit = defaultLoading,
    error: @Composable (modifier: Modifier, throwable: Throwable) -> Unit = defaultError
) {
    val state by produceUiState {
        val node = Json.decodeFromString<JsonObject>(json)
        node.toNode()
    }
    SDCStateUiProvider(state = state, loading = loading, error = error) {
        loadComponent(node = it)
    }
}