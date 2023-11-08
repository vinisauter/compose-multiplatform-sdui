package me.next.serverdriven.compose.components

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

fun Modifier.fromNode(properties: MutableMap<String, Any?>?): Modifier {
    properties?.get("width")?.let {
        width(it.toString().toInt().dp)
    }
    properties?.get("height")?.let {
        height(it.toString().toInt().dp)
    }
    val minWidth = properties?.get("minWidth")
    val minHeight = properties?.get("minHeight")
    if (minWidth != null || minHeight != null) {
        val width = minWidth?.toString()?.toInt()?.dp ?: 0.dp
        val height = minHeight?.toString()?.toInt()?.dp ?: 0.dp
        defaultMinSize(minWidth = width, minHeight = height)
    }
    properties?.get("fillMaxSize")?.let {
        fillMaxSize(it.toString().toFloat())
    }
    properties?.get("fillMaxSize")?.let {
        fillMaxSize(it.toString().toFloat())
    }
    properties?.get("fillMaxWidth")?.let {
        fillMaxWidth(it.toString().toFloat())
    }
    properties?.get("fillMaxHeight")?.let {
        fillMaxHeight(it.toString().toFloat())
    }
    properties?.get("paddingAll")?.let {
        padding(all = it.toString().toInt().dp)
    }
    val paddingHorizontal = properties?.get("paddingHorizontal")
    val paddingVertical = properties?.get("paddingVertical")
    if (paddingHorizontal != null || paddingVertical != null) {
        val horizontal = paddingHorizontal?.toString()?.toInt()?.dp ?: 0.dp
        val vertical = paddingVertical?.toString()?.toInt()?.dp ?: 0.dp
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
        val start = paddingStart?.toString()?.toInt()?.dp ?: 0.dp
        val top = paddingTop?.toString()?.toInt()?.dp ?: 0.dp
        val end = paddingEnd?.toString()?.toInt()?.dp ?: 0.dp
        val bottom = paddingBottom?.toString()?.toInt()?.dp ?: 0.dp
        padding(
            start = start,
            top = top,
            end = end,
            bottom = bottom
        )
    }
    properties?.get("backgroundColor")?.let {
        background(color = Color(it.toString().toLong()))
    }
// TODO: other Modifier
//    clip(RoundedCornerShape(8.dp))
//    etc.
    return this
}
