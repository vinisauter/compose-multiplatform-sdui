package me.next.serverdriven.core.library.layout.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.runBlocking
import me.next.serverdriven.core.library.interfaces.Layout
import me.next.serverdriven.core.tree.ServerDrivenNode
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.resource

@OptIn(ExperimentalResourceApi::class)
class SDCImage(
    node: ServerDrivenNode, state: MutableMap<String, String>,
    private val resource: String = node.propertyState("painterResource", state)!!.also {
        runBlocking {
            // FIXME: ExperimentalResourceApi
            //  LANÇAR EXCEPTION ANTES DO COMPOSABLE (painterResource)
            resource(it).readBytes()
        }
    }
) : Layout {
    private val modifier = Modifier.fromNode(node)
    private val contentDescription: String? = node.propertyState("contentDescription", state)

    @Composable
    override fun Content() {
        Image(
            modifier = modifier,
            painter = painterResource(resource),
            contentDescription = contentDescription
        )
    }
}
