package me.developes.humming.sdui.layout.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.common.toColor
import me.developes.humming.sdui.layout.Layout

class SDCTopAppBar(val node: ServerDrivenNode, val viewModel: SDUIFormViewModel) : Layout {
    private var modifier = Modifier.fromNode(node)
    private val title = viewModel.resolveStatePlaceholdersFlow(node.property("title"))
    private val actions = node.propertyNodes("onClick")
    private val icon = node.property("icon")?.let { Icons.getIcon(it) } ?: FeatherIcons.ArrowLeft
    private val iconDescription = node.property("iconDescription") ?: node.property("icon")
    private val enabled = viewModel.resolveStatePlaceholdersFlow(node.property("enabled")) {
        it?.toBooleanStrictOrNull() ?: true
    }
    private val backgroundColor = node.property("backgroundColor")?.toColor()
    private val textColor = node.property("color")?.toColor()

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val isEnabled by enabled.collectAsState()

        TopAppBar(
            title = {
                Box(
                    modifier = Modifier.fillMaxSize().padding(end = 48.dp).also {
                        if (backgroundColor != null) {
                            it.background(backgroundColor)
                        }
                    },
                    contentAlignment = Alignment.Center
                ) {
                    val title by title.collectAsState()
                    Text(
                        modifier = modifier,
                        text = title,
                        color = textColor ?: MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            modifier = modifier,
            navigationIcon = {
                IconButton(
                    enabled = isEnabled,
                    onClick = {
                        viewModel.invokeActions(actions, beforeInvoke = {
                            enabled.value = false
                        }, afterInvoke = {
                            enabled.value = true
                        })
                    }) {
                    Icon(
                        imageVector = icon,
                        contentDescription = iconDescription,
                        tint = textColor ?: LocalContentColor.current
                    )
                }
            },
//            actions = TODO(),
//            expandedHeight = TODO(),
//            windowInsets = TODO(),
//            colors = TODO(),
//            scrollBehavior = TODO()
        )
    }
}
