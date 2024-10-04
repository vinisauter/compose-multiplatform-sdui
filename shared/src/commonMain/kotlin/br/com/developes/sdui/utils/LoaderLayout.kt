package br.com.developes.sdui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import br.com.developes.sdui.defaultError
import br.com.developes.sdui.defaultLoading

/**
 * This function defines a composable function called [LoaderLayout] that is designed to present a UI
 * based on different states of a network operation or other asynchronous task.
 * It takes a generic type T which represents the type of data that is expected to be loaded.
 *
 * @param modifier: Modifier = Modifier: A Modifier is used to modify the composable's appearance or behavior.
 * @param state: [UIResult]<T>: This parameter represents the current state of the data loading operation. [UIResult] is a sealed class (or a similar custom class)
 * that has three possible states: Loading, Success, and Error.
 * @param loading: @Composable (Modifier) -> Unit = defaultLoading: This is a lambda function that defines the UI to be displayed when the state is Loading.
 * It takes a Modifier as a parameter and defaults to a function named defaultLoading.
 * @param error: @Composable (modifier: Modifier, throwable: Throwable) -> Unit = defaultError: This is a lambda function that defines the UI to be displayed when the state is Error.
 * It takes a [Modifier] and a [Throwable] (representing the error) as parameters and defaults to a function named defaultError.
 * @param success: @Composable (T) -> Unit: This is a lambda function that defines the UI to be displayed when the state is Success. It takes the successfully loaded data of type T as a parameter.
 *
 * Inside the function:
 * If the state is UIResult.Error, the error composable function is called with the modifier and the error (Throwable).
 * If the state is UIResult.Loading, the loading composable function is called with the modifier.
 * If the state is UIResult.Success, the success composable function is called with the loaded data.
 *
 * This function provides a structured way to handle different UI states within a Jetpack Compose application, making it easier to manage loading, error, and success states in a clean and reusable manner.
 * */
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