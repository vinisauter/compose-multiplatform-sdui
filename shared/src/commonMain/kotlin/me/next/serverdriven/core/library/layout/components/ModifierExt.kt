package me.next.serverdriven.core.library.layout.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.next.serverdriven.core.tree.ServerDrivenNode

fun Modifier.fromNode(node: ServerDrivenNode): Modifier {
    node.run {
        properties?.get("width")?.let {
            width(it.toInt().dp)
        }
        properties?.get("height")?.let {
            height(it.toInt().dp)
        }
        val minWidth = properties?.get("minWidth")
        val minHeight = properties?.get("minHeight")
        if (minWidth != null || minHeight != null) {
            val width = minWidth?.toInt()?.dp ?: 0.dp
            val height = minHeight?.toInt()?.dp ?: 0.dp
            defaultMinSize(minWidth = width, minHeight = height)
        }
        properties?.get("fillMaxSize")?.let {
            fillMaxSize(it.toFloat())
        }
        properties?.get("fillMaxSize")?.let {
            fillMaxSize(it.toFloat())
        }
        properties?.get("fillMaxWidth")?.let {
            fillMaxWidth(it.toFloat())
        }
        properties?.get("fillMaxHeight")?.let {
            fillMaxHeight(it.toFloat())
        }
        properties?.get("paddingAll")?.let {
            padding(all = it.toInt().dp)
        }
        val paddingHorizontal = properties?.get("paddingHorizontal")
        val paddingVertical = properties?.get("paddingVertical")
        if (paddingHorizontal != null || paddingVertical != null) {
            val horizontal = paddingHorizontal?.toInt()?.dp ?: 0.dp
            val vertical = paddingVertical?.toInt()?.dp ?: 0.dp
            padding(
                start = horizontal,
                top = vertical,
                end = horizontal,
                bottom = vertical
            )
        }
        val paddingStart = properties?.get("paddingStart")
        val paddingTop = properties?.get("paddingTop")
        val paddingEnd = properties?.get("paddingEnd")
        val paddingBottom = properties?.get("paddingBottom")
        if (paddingStart != null ||
            paddingTop != null ||
            paddingEnd != null ||
            paddingBottom != null
        ) {
            val start = paddingStart?.toInt()?.dp ?: 0.dp
            val top = paddingTop?.toInt()?.dp ?: 0.dp
            val end = paddingEnd?.toInt()?.dp ?: 0.dp
            val bottom = paddingBottom?.toInt()?.dp ?: 0.dp
            padding(
                start = start,
                top = top,
                end = end,
                bottom = bottom
            )
        }
        properties?.get("backgroundColor")?.let {
            background(color = Color(it.toLong()))
        }
// TODO: other Modifier
//    clip(RoundedCornerShape(8.dp))
//    etc.
    }
    return this
}
