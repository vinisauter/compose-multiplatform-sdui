package me.next.serverdriven.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import me.next.serverdriven.core.ServerDrivenNodeProvider
import me.next.serverdriven.core.library.SDLibrary
import me.next.serverdriven.core.tree.ServerDrivenNode


abstract class SDComposeNodeProvider(
    override val library: SDLibrary,
    override val node: ServerDrivenNode
) : ServerDrivenNodeProvider {

    @Composable
    fun asd() {
        library.getComponent(node.component)
    }
}

@Composable
inline fun <T> produceUiState(crossinline block: suspend () -> T): State<UIResult<T>> =
    produceState<UIResult<T>>(initialValue = UIResult.Loading) {
        value = try {
            UIResult.Success(block.invoke())
        } catch (error: Throwable) {
            UIResult.Error(error)
        }
    }

sealed interface UIResult<out T> {
    data object Loading : UIResult<Nothing>
    data class Success<T>(val result: T) : UIResult<T>
    data class Error(val throwable: Throwable) : UIResult<Nothing>
}