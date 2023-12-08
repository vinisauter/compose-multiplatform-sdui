import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import me.next.serverdriven.compose.SDCLibrary
import me.next.serverdriven.compose.SDCLibrary.Companion.loadNodeTypeProvider
import me.next.serverdriven.compose.SDCLibrary.Companion.registerMethod

@Composable
fun ServerDrivenApp() {
    SDCLibrary(debug = true) {
        registerMethod("getPlatformName") { _, _ -> getPlatformName() }
        registerMethod("openUrl") { node, map ->
            val url = node.propertyState("url", map)
            openUrl(url)
            ""
        }
    }

    val graph = "navigation/app-navigation.json"
    val stateMap: MutableMap<String, String> = remember { mutableStateMapOf() }
    val node = loadNodeTypeProvider("file").invoke(graph)
    SDCLibrary.loadComponent(node, stateMap)
}