package br.com.developes.sdui.layout.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import br.com.developes.sdui.SDCLibrary
import br.com.developes.sdui.ServerDrivenNode
import br.com.developes.sdui.layout.Layout

class SDCScaffold(
    private val node: ServerDrivenNode, private val dataState: MutableMap<String, String>
) : Layout {

    private val modifier = Modifier.fromNode(node)
    private val verticalArrangement: Arrangement.Vertical =
        when (node.property("verticalArrangement")) {
            null -> Arrangement.Top
            "Top" -> Arrangement.Top
            "Center" -> Arrangement.Center
            "Bottom" -> Arrangement.Bottom
            "SpaceAround" -> Arrangement.SpaceAround
            "SpaceBetween" -> Arrangement.SpaceBetween
            "SpaceEvenly" -> Arrangement.SpaceEvenly
            else -> error("Unknown value for verticalArrangement ${node.property("verticalArrangement")}")
        }
    private val horizontalAlignment: Alignment.Horizontal =
        when (node.property("horizontalAlignment")) {
            null -> Alignment.Start
            "Start" -> Alignment.Start
            "Center" -> Alignment.CenterHorizontally
            "End" -> Alignment.End
            else -> error("Unknown value for horizontalAlignment ${node.property("horizontalAlignment")}")
        }
    private val loadChildren: @Composable () -> Unit? = {
        node.children?.let {
            for (serverDrivenNode in it) {
                SDCLibrary.loadComponent(node = serverDrivenNode, dataState = dataState)
            }
        }
    }
    private val loadTopBar: @Composable () -> Unit? = {
        node.propertyNode("topBar")?.let { serverDrivenNode ->
            SDCLibrary.loadComponent(node = serverDrivenNode, dataState = dataState)
        }
    }
    private val loadBottomBar: @Composable () -> Unit? = {
        node.propertyNode("bottomBar")?.let { serverDrivenNode ->
            SDCLibrary.loadComponent(node = serverDrivenNode, dataState = dataState)
        }
    }

    @Composable
    override fun Content() {
        Scaffold(
            topBar = { loadTopBar.invoke() },
            bottomBar = { loadBottomBar.invoke() }
        ) { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment
            ) {
                loadChildren.invoke()
            }
        }
    }
}