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
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SDCJsonFileStateUiProvider(
    modifier: Modifier = Modifier,
    res: String,
    loading: @Composable (Modifier) -> Unit = defaultLoading,
    error: @Composable (modifier: Modifier, throwable: Throwable) -> Unit = defaultError
) {
    val state by produceUiState {
        val resource = resource(res)
        val bytes: ByteArray = resource.readBytes()
        val json = bytes.decodeToString()
        val node = Json.decodeFromString<JsonObject>(json)
        node.toNode()
    }
    SDCStateUiProvider(modifier = modifier,state = state, loading = loading, error = error) {
        loadComponent(node = it)
    }
}