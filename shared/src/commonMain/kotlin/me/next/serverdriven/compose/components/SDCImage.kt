package me.next.serverdriven.compose.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.next.serverdriven.core.tree.ServerDrivenNode
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SDCImage(node: ServerDrivenNode): Unit {
    val modifier = Modifier.fromNode(node.properties)
    val painter = node.properties?.get("painterResource")?.toString()?.let { painterResource(it) }
    val contentDescription: String? = node.properties?.get("contentDescription")?.toString()

    Image(
        modifier = modifier,
        painter = painter!!,
        contentDescription = contentDescription
    )
}