package br.com.developes.sdui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import br.com.developes.sdui.SDCLibrary.Companion.loadNodeTypeProvider
import br.com.developes.sdui.events.lifecycle.LifecycleTracker
import br.com.developes.sdui.events.lifecycle.LocalLifecycleTracker
import br.com.developes.sdui.provider.components.FirestoreMaterialThemeProvider
import br.com.developes.sdui.utils.LoaderLayout
import br.com.developes.sdui.utils.produceUiState
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
var defaultLoading: @Composable (modifier: Modifier) -> Unit = {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(modifier = it)
    }
}

@ThreadLocal
var defaultError: @Composable (modifier: Modifier, throwable: Throwable) -> Unit =
    { modifier, throwable ->
        Column(modifier = modifier.background(Color.Red)) {
            val msg = "Error: ${throwable.message ?: throwable::class.simpleName}"
            Text(msg)
            logger.e(msg, throwable)
        }
    }

@Composable
fun App(lifecycleTracker: LifecycleTracker) {
    CompositionLocalProvider(LocalLifecycleTracker provides lifecycleTracker) {
        ServerDrivenApp()
    }
}

/**
 * Initializes a server-driven UI SDK, loads a UI structure from a JSON file, and uses the SDK to render the UI based on the loaded structure.
 * The use of [produceUiState] makes possible that the UI can be updated asynchronously, possibly reflecting changes from the server.
 * */
@Composable
fun ServerDrivenApp() {
    SDCLibrary(debug = true) {
        val stateMap: MutableMap<String, String> = remember { mutableStateMapOf() }
        val firestorePath = "SD_APP_NAVIGATION/app_nav/4.0"
        val themePath = "SD_CONFIG/cfg_theme/1.0"
        val theme by produceUiState {
            FirestoreMaterialThemeProvider(Firebase.firestore, themePath).result
        }
        logger.i("theme: $theme")
        val uiState by produceUiState {
            loadNodeTypeProvider("firestore").invoke(firestorePath)
        }
        logger.i("UiState: $uiState")

        LoaderLayout(modifier = Modifier, state = theme) { materialTheme ->
            logger.i("materialTheme: $materialTheme")
            materialTheme.toMaterialTheme {
                LoaderLayout(modifier = Modifier, state = uiState) { node ->
                    logger.i("Node: $node")
                    SDCLibrary.loadComponent(node, stateMap)
                }
            }
        }
    }
}