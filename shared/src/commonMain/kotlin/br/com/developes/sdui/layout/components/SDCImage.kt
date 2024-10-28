package br.com.developes.sdui.layout.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Density
import br.com.developes.sdui.SDCLibrary
import br.com.developes.sdui.SDCLibrary.Companion.launchHandling
import br.com.developes.sdui.ServerDrivenNode
import br.com.developes.sdui.layout.Layout
import br.com.developes.sdui.resources.Res
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageVector

class SDCImage @OptIn(ExperimentalResourceApi::class) constructor(
    val node: ServerDrivenNode, val state: MutableMap<String, String>,
    private val resource: ImageVector = node.propertyState("painterResource", state)!!.let {
        runBlocking {
            try {
                val bytes = Res.readBytes(it)
                bytes.decodeToImageVector(Density(1f))
            } catch (e: Exception) {
                throw Exception("Error loading image resource: $it")
            }
        }
    },
) : Layout {
    private val modifier = Modifier.fromNode(node)
    private val actions = node.propertyNodes("onClick")
    private val contentDescription: String? = node.propertyState("contentDescription", state)

    @Composable
    override fun Content() {
        val action = SDCLibrary.loadActions(actions)
        // Creates a CoroutineScope bound to the Content's lifecycle
        val scope = rememberCoroutineScope()

        Image(
            modifier = modifier.clickable {
                scope.launchHandling {
                    action.invoke(node, state)
                }
            },
            imageVector = resource,
            contentDescription = contentDescription,
        )
    }
}
