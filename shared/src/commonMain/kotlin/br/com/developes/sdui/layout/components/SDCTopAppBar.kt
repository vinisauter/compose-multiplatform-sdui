package br.com.developes.sdui.layout.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.developes.sdui.SDCLibrary
import br.com.developes.sdui.SDCLibrary.Companion.launchHandling
import br.com.developes.sdui.ServerDrivenNode
import br.com.developes.sdui.layout.Layout
import br.com.developes.sdui.resources.Res
import br.com.developes.sdui.resources.ic_back_button_toolbar
import br.com.developes.sdui.utils.hexToColor
import org.jetbrains.compose.resources.vectorResource

class SDCTopAppBar(val node: ServerDrivenNode, val state: MutableMap<String, String>) : Layout {
    private var modifier = Modifier.fromNode(node)
    private val title by node.propertyState("title", state) { it ?: "" }
    private val actions = node.propertyNodes("onClick")
    private val enabled = node.propertyState("enabled", state)?.toBoolean()
    private val backgroundColor = node.property("backgroundColor")?.hexToColor()
    private val color = node.property("color")?.hexToColor()

    @Composable
    override fun Content() {
        var isEnabled by remember { mutableStateOf(enabled ?: true) }
        val action = SDCLibrary.loadActions(actions)
        // Creates a CoroutineScope bound to the Content's lifecycle
        val scope = rememberCoroutineScope()

        TopAppBar(
            backgroundColor = backgroundColor ?: MaterialTheme.colors.primarySurface,
            navigationIcon = {
                IconButton(onClick = {
                    scope.launchHandling(after = { isEnabled = true }) {
                        action.invoke(node, state)
                    }
                }) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_back_button_toolbar),
                        contentDescription = "",
                        tint = color ?: LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
                    )
                }
            },
            title = {
                Box(
                    modifier = Modifier.fillMaxSize().padding(end = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = modifier,
                        text = title,
                        color = color ?: Color.Unspecified
                    )
                }
            },
        )
    }
}
