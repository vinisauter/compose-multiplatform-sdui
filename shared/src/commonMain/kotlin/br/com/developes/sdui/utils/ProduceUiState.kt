package br.com.developes.sdui.utils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import br.com.developes.sdui.logger

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