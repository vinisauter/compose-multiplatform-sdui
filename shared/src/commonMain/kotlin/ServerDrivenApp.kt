import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import me.next.serverdriven.compose.SDCLibrary
import me.next.serverdriven.compose.provider.SDCJsonFileStateUiProvider
import me.next.serverdriven.core.library.SDLibrary

@Composable
fun ServerDrivenApp() {
    var layout by remember { mutableStateOf("layout/static.json") }
    // TODO: ACTIONS
    SDCLibrary() {
        SDCJsonFileStateUiProvider(res = layout)
    }
}

@Composable
fun SCNavigation() {
    var destination by remember { mutableStateOf("layout/static.json") }

    val navigationLib = SDLibrary("navigation")
        .addComponent("graph") { node ->

        }
        .addComponent("node") { node ->

        }
        .addComponent("goTo") { node ->
            val type = node.properties?.get("type")!!.toString()
            val destiny = node.properties?.get("destiny")!!
            when (type) {
                "file" -> destination = destiny.toString()
                "link" -> openUrl(destiny.toString())
                "node" -> {}
            }
        }

    SDCLibrary(navigationLib) {
        SDCJsonFileStateUiProvider(res = destination)
    }
}