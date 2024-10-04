package br.com.developes.sdui.layout.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import br.com.developes.sdui.SDCLibrary
import br.com.developes.sdui.SDCLibrary.Companion.launchHandling
import br.com.developes.sdui.ServerDrivenNode
import br.com.developes.sdui.layout.Layout

class SDCTopAppBar(val node: ServerDrivenNode, val state: MutableMap<String, String>) : Layout {
    private var modifier = Modifier.fromNode(node)
    private val title by node.propertyState("title", state) { it ?: "" }
    private val actions = node.propertyNodes("onClick")
    private val enabled = node.propertyState("enabled", state)?.toBoolean()

    @Composable
    override fun Content() {
        var isEnabled by remember { mutableStateOf(enabled ?: true) }
        val action = SDCLibrary.loadActions(actions)
        // Creates a CoroutineScope bound to the Content's lifecycle
        val scope = rememberCoroutineScope()

        TopAppBar(
            navigationIcon = {
                IconButton(onClick = {
                    scope.launchHandling(after = { isEnabled = true }) {
                        action.invoke(node, state)
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = ""
                    )
                }
            },
            title = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier =  modifier,
                        text = title
                    )
                }
            },
        )
    }
}
