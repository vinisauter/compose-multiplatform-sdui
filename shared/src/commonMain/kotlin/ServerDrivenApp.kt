import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import me.next.serverdriven.compose.SDCLibrary
import me.next.serverdriven.compose.SDCLibrary.Companion.loadNodeTypeProvider

@Composable
fun ServerDrivenApp() {
    SDCLibrary(debug = true)

    val graph = "navigation/app-navigation.json"
    val stateMap: MutableMap<String, String> = remember { mutableStateMapOf() }
    val node = loadNodeTypeProvider("file").invoke(graph)
    SDCLibrary.loadComponent(node, stateMap)
}