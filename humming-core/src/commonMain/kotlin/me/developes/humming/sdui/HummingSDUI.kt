package me.developes.humming.sdui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.developes.humming.sdui.common.ComposableContent
import me.developes.humming.sdui.common.ErrorData
import me.developes.humming.sdui.common.SDUIFormViewModel

interface ThemeProvider {
    @Composable
    fun Content(content: @Composable () -> Unit)
}

class HummingSDUI {
    data class Config(
        val type: String,
        val destiny: String,
        val placeholder: @Composable () -> Unit = {},
        val loadingHandler: @Composable () -> Unit = {},
        val errorHandler: @Composable (error: ErrorData, viewModel: SDUIFormViewModel) -> Unit = { _, _ -> },
        val theme: ThemeProvider = NoTheme,
        val block: SDUIFormViewModel.() -> Unit
    ) {
        private object NoTheme : ThemeProvider {
            @Composable
            override fun Content(content: @Composable () -> Unit) {
                content()
            }
        }
    }

    /**
     * Hosts without a SavedStateRegistryOwner can provide an explicit handle factory.
     * The factory is used only when creating the ViewModel; persistence remains host-owned.
     */
    @Composable
    fun Content(
        config: Config,
        savedStateHandleFactory: (() -> SavedStateHandle)? = null,
    ) = config.theme.Content {
        val viewModel = viewModel {
            SDUIFormViewModel(
                handle = savedStateHandleFactory?.invoke() ?: createSavedStateHandle()
            ).apply {
                config.block(this)
            }
        }

        val isLoading by viewModel.isLoading.collectAsState()
        val errorDialogData by viewModel.errorState.collectAsState()
        val contentState = remember(config.type, config.destiny) {
            mutableStateOf<ComposableContent?>(null)
        }
        LaunchedEffect(config.type, config.destiny) {
            contentState.value = viewModel.loadComponent(config.type, config.destiny)
        }

        when {
            isLoading -> config.loadingHandler()
            errorDialogData != null -> config.errorHandler(errorDialogData!!, viewModel)
        }
        contentState.value?.Content() ?: config.placeholder()
    }
}
