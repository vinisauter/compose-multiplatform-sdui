package me.next.serverdriven.compose.provider

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.next.serverdriven.compose.UIResult
import me.next.serverdriven.core.library.defaultError
import me.next.serverdriven.core.library.defaultLoading

@Composable
fun <T> SDCStateUiProvider(
    modifier: Modifier = Modifier,
    state: UIResult<T>,
    loading: @Composable (Modifier) -> Unit = defaultLoading,
    error: @Composable (modifier: Modifier, throwable: Throwable) -> Unit = defaultError,
    success: @Composable (T) -> Unit,
) {
    when (state) {
        is UIResult.Error -> error(modifier, state.throwable)
        UIResult.Loading -> loading(modifier)
        is UIResult.Success -> success(state.result)
    }
}