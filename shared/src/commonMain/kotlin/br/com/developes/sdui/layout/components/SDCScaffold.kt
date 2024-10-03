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
): Layout {

    private val modifier = Modifier.fromNode(node)
    private val horizontalArrangement: Arrangement.Horizontal =
        when (node.property("horizontalArrangement")) {
            "Start" -> Arrangement.Start
            "Center" -> Arrangement.Center
            "End" -> Arrangement.End
            "SpaceAround" -> Arrangement.SpaceAround
            "SpaceBetween" -> Arrangement.SpaceBetween
            "SpaceEvenly" -> Arrangement.SpaceEvenly
            else -> Arrangement.Start
        }
    private val verticalAlignment: Alignment.Vertical =
        when (node.property("verticalAlignment")) {
            "Top" -> Alignment.Top
            "Center" -> Alignment.CenterVertically
            "Bottom" -> Alignment.Bottom
            else -> Alignment.Top
        }
    private val loadChildren: @Composable () -> Unit? = {
        node.children?.let {
            for (serverDrivenNode in it) {
                SDCLibrary.loadComponent(node = serverDrivenNode, dataState = dataState)
            }
        }
    }

    @Composable
    override fun Content() {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = ""
                            )
                        }
                    },
                    title = { Text(text = "ENTRAR") },
                )
            },
        ) { innerPadding ->

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                loadChildren.invoke()
            }
        }
    }
}