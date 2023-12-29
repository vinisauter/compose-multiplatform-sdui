import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import me.next.serverdriven.compose.SDCLibrary
import me.next.serverdriven.compose.SDCLibrary.Companion.loadNodeTypeProvider
import me.next.serverdriven.compose.SDCLoaderLayout
import me.next.serverdriven.compose.produceUiState

@Composable
fun ServerDrivenApp() {
    SDCLibrary(debug = true) {
        val stateMap: MutableMap<String, String> = remember { mutableStateMapOf() }
        val graph = "navigation/app-navigation.json"
        val uiState by produceUiState {
            loadNodeTypeProvider("file").invoke(graph)
        }
        SDCLoaderLayout(modifier = Modifier, state = uiState) { node ->
            SDCLibrary.loadComponent(node, stateMap)
        }
    }
}