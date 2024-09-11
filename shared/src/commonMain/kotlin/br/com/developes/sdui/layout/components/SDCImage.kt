package br.com.developes.sdui.layout.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Density
import br.com.developes.sdui.ServerDrivenNode
import br.com.developes.sdui.layout.Layout
import br.com.developes.sdui.resources.Res
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageVector

class SDCImage @OptIn(ExperimentalResourceApi::class) constructor(
    node: ServerDrivenNode, state: MutableMap<String, String>,
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
    private val contentDescription: String? = node.propertyState("contentDescription", state)

    @Composable
    override fun Content() {
        Image(
            modifier = modifier,
            imageVector = resource,
            contentDescription = contentDescription
        )
    }
}
