package br.com.developes.sdui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import br.com.developes.sdui.defaultError
import br.com.developes.sdui.defaultLoading

@Composable
fun <T> LoaderLayout(
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