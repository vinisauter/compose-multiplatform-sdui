package br.com.developes.sdui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
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
import br.com.developes.sdui.utils.LoaderLayout
import br.com.developes.sdui.utils.produceUiState
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
        MaterialTheme(
            colors = MaterialTheme.colors,
            typography = MaterialTheme.typography,
            shapes = MaterialTheme.shapes,
        ) {
            ServerDrivenApp()
        }
    }
}

/**
 * Initializes a server-driven UI SDK, loads a UI structure from a JSON file, and uses the SDK to render the UI based on the loaded structure.
 * The use of [produceUiState] makes possible that the UI can be updated asynchronously, possibly reflecting changes from the server.
 * */
@Composable
fun ServerDrivenApp() {
    SDCLibrary(debug = true) {
        //Local
//        val stateMap: MutableMap<String, String> = remember { mutableStateMapOf() }
//        val graph = "files/navigation/app-navigation.json"
//        val uiState by produceUiState {
//            loadNodeTypeProvider("file").invoke(graph)
//        }

        val stateMap: MutableMap<String, String> = remember { mutableStateMapOf() }
        val graph = "app_nav"
        val uiState by produceUiState {
            loadNodeTypeProvider("firestore").invoke(graph)
        }
        logger.i("UiState: $uiState")
        LoaderLayout(modifier = Modifier, state = uiState) { node ->
            logger.i("Node: $node")
            SDCLibrary.loadComponent(node, stateMap)
        }
    }
}