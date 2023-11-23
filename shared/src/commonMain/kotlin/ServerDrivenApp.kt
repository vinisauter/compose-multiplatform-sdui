import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import me.next.serverdriven.compose.SDCLibrary
import me.next.serverdriven.compose.SDCLibrary.Companion.registerMethod
import me.next.serverdriven.compose.provider.SDCJsonFileStateUiProvider
import me.next.serverdriven.core.library.SDLibrary

@Composable
fun ServerDrivenApp() {
    var layout by remember { mutableStateOf("layout/template.json") }

    SDCLibrary {
        registerMethod("getPlatformName") { _, _ ->
            getPlatformName()
        }
        registerMethod("openUrl") { node, map ->
            val url = node.propertyState("url", map)
            openUrl(url)
            return@registerMethod ""
        }
        SDCJsonFileStateUiProvider(res = layout)
    }
}

@Composable
fun SCNavigation() {
    var destination by remember { mutableStateOf("layout/static.json") }

    val navigationLib = SDLibrary("navigation")
        .addAction("graph") { node, map ->

        }
        .addAction("node") { node, _ ->

        }
        .addAction("goTo") { node, _ ->
            val type = node.property("type")!!
            val destiny = node.property("destiny")!!
            when (type) {
                "file" -> destination = destiny
                "link" -> openUrl(destiny)
                "node" -> {}
            }
        }

    SDCLibrary(navigationLib) {
        SDCJsonFileStateUiProvider(res = destination)
    }
}