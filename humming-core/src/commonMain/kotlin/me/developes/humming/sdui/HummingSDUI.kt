package me.developes.humming.sdui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.developes.humming.sdui.common.ComposableContent
import me.developes.humming.sdui.common.ErrorData
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.ScreenStateMode
import me.developes.humming.sdui.common.ScreenInputs
import me.developes.humming.sdui.common.LocalScreenFeedbackHandler

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
        val screenStateMode: ScreenStateMode = ScreenStateMode.LEGACY_SHARED,
        val entryInputs: (entryId: String, route: String) -> ScreenInputs = { _, _ -> ScreenInputs() },
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
                handle = savedStateHandleFactory?.invoke() ?: createSavedStateHandle(),
                screenStateMode = config.screenStateMode, entryInputs = config.entryInputs,
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
        CompositionLocalProvider(LocalScreenFeedbackHandler provides { owner ->
            val loading by owner.isLoading.collectAsState()
            val error by owner.errorState.collectAsState()
            when {
                loading -> config.loadingHandler()
                error != null -> config.errorHandler(checkNotNull(error),owner)
            }
        }) { contentState.value?.Content() ?: config.placeholder() }
    }
}
