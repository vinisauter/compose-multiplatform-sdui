import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import me.next.serverdriven.compose.SDCLibrary
import me.next.serverdriven.compose.components.SDCColumn
import me.next.serverdriven.compose.components.SDCRow
import me.next.serverdriven.compose.components.SDCText
import me.next.serverdriven.compose.components.SDCTextButton
import me.next.serverdriven.compose.components.fromNode
import me.next.serverdriven.compose.provider.SDCJsonStateUiProvider
import me.next.serverdriven.core.library.SDLibrary
import me.next.serverdriven.core.tree.ServerDrivenNode
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ServerDrivenApp() {
    val layout = SDLibrary("layout")
        .addComponent("column") {
            val modifier = Modifier.fromNode(it.properties)
            SDCColumn(it)
        }
        .addComponent("row") { SDCRow(it) }
        .addComponent("text") { SDCText(it) }
        .addComponent("image") { node ->
            val modifier = Modifier.fromNode(node.properties)
            val painter =
                node.properties?.get("painterResource")?.toString()?.let { painterResource(it) }
            val contentDescription: String? = node.properties?.get("contentDescription")?.toString()

            Image(
                modifier = modifier,
                painter = painter!!,
                contentDescription = contentDescription
            )
        }
        .addComponent("textButton") {
            SDCTextButton(it) {

            }
        }

    var navigation by remember { mutableStateOf(TEMP_JSON) }

    val localActions = SDLibrary("local-action")
        .addComponent("go-to") {
            navigation = loadNextUi(it)
        }
        .addComponent("deep-link") {
            navigation = loadNextUi(it)
        }

    SDCLibrary(layout, localActions) {
        SDCJsonStateUiProvider(json = navigation)
    }
}

fun loadNextUi(it: ServerDrivenNode): String {
    TODO()
}

val TEMP_JSON = """{
  "id": "template",
  "component": "layout:column",
  "properties": {
    "fillMaxWidth": "1.0",
    "horizontalAlignment": "Center"
  },
  "children": [
    {
      "component": "layout:text",
      "properties": {
        "text": "TITLE"
      }
    },
    {
      "component": "layout:image",
      "properties": {
        "painterResource": "compose-multiplatform.xml",
        "contentDescription": ""
      }
    },
    {
      "component": "layout:text",
      "properties": {
        "text": "description"
      }
    }
  ]
}""".trimIndent()