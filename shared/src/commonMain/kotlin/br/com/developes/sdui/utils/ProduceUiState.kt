package br.com.developes.sdui.utils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import br.com.developes.sdui.logger


/**
 * provides a structured way to handle asynchronous operations in Jetpack Compose. It uses coroutines to execute the provided lambda and updates the UI state accordingly.
 * The [UIResult] sealed interface helps represent the different states of the operation (loading, success, or error),
 * allowing you to display appropriate content or handle errors gracefully in your UI
 *
 * Example Usage
 *
 * ```
 * val uiState = produceUiState {
 *     // Perform a network request or some other long-running operation
 *     myRepository.fetchData()
 * }
 *
 * when (uiState.value) {
 *     is UIResult.Loading -> ShowLoadingIndicator()
 *     is UIResult.Success -> DisplayData(uiState.value.result)
 *     is UIResult.Error -> DisplayError(uiState.value.throwable)
 * }
 * ```
 * */
@Composable
inline fun <T> produceUiState(crossinline block: suspend () -> T): State<UIResult<T>> =
    produceState<UIResult<T>>(initialValue = UIResult.Loading) {
        value = try {
            UIResult.Success(block.invoke())
        } catch (error: Throwable) {
            logger.e("ProduceUiState", error)
            UIResult.Error(error)
        }
    }

sealed interface UIResult<out T> {
    data object Loading : UIResult<Nothing>
    data class Success<T>(val result: T) : UIResult<T>
    data class Error(val throwable: Throwable) : UIResult<Nothing>
}