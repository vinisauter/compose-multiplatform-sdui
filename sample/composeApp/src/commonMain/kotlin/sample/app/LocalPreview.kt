package sample.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import br.com.developes.sdui.resources.Res
import me.developes.humming.sdui.HummingSDUI
import me.developes.humming.sdui.action.SDActions
import me.developes.humming.sdui.common.parseNode
import me.developes.humming.sdui.layout.SDLayoutMaterial3
import me.developes.humming.sdui.layout.components.Material3Theme

/** Local fixture host. No URL provider, navigation host or application methods are installed. */
@Composable
fun LocalPreview() {
    val owner = remember {
        object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
    }
    DisposableEffect(owner) { onDispose { owner.viewModelStore.clear() } }
    CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
        Column(Modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState())) {
            Text("Preview local — estado em memória; sem efeitos externos")
            HummingSDUI().Content(
                HummingSDUI.Config(
                    type = "preview",
                    destiny = "files/layout/local-preview.json",
                    theme = Material3Theme(),
                    placeholder = { Text("Carregando fixture local…") },
                    errorHandler = { error, vm ->
                        Column {
                            Text("Diagnóstico: ${error.code}")
                            Text(error.message)
                            error.diagnostic?.let {
                                Text("Nó: ${it.nodeId}; caminho: ${it.path}")
                            }
                            Button(onClick = vm::dismissError) { Text("Limpar diagnóstico") }
                        }
                    },
                ) {
                    addNodeProvider("preview") { resource ->
                        require(resource == "files/layout/local-preview.json")
                        parseNode(Res.readBytes(resource).decodeToString(), documentId = resource)
                    }
                    addLibrary(SDActions())
                    addLibrary(SDLayoutMaterial3())
                },
                savedStateHandleFactory = { SavedStateHandle() },
            )
        }
    }
}
