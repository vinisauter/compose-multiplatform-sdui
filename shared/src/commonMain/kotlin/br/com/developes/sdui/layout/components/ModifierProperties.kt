package br.com.developes.sdui.layout.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.developes.sdui.ServerDrivenNode

fun Modifier.fromNode(
    node: ServerDrivenNode,
    state: MutableMap<String, String> = mutableMapOf()
): Modifier = ModifierProperties(node, this, state).modifier

class ModifierProperties(
    node: ServerDrivenNode,
    modifier: Modifier = Modifier,
    state: MutableMap<String, String> = mutableMapOf()
) {
    private val width = node.propertyState("width", state)?.dp
    private val height = node.propertyState("height", state)?.dp
    private val minWidth = node.property("minWidth")?.dp
    private val minHeight = node.property("minHeight")?.dp
    private val fillMaxSize = node.property("fillMaxSize")?.tryFloat(1f)
    private val fillMaxWidth = node.property("fillMaxWidth")?.tryFloat(1f)
    private val fillMaxHeight = node.property("fillMaxHeight")?.tryFloat(1f)
    private val paddingAll = node.property("paddingAll")?.dp
    private val paddingHorizontal = node.property("paddingHorizontal")?.dp
    private val paddingVertical = node.property("paddingVertical")?.dp
    private val paddingStart = node.property("paddingStart")?.dp
    private val paddingTop = node.property("paddingTop")?.dp
    private val paddingEnd = node.property("paddingEnd")?.dp
    private val paddingBottom = node.property("paddingBottom")?.dp
    private val backgroundColor = node.property("backgroundColor")?.let { Color(it.toLong()) }
    private val verticalScroll = node.property("verticalScroll")?.let { true }
    private val horizontalScroll = node.property("horizontalScroll")?.let { true }
    val modifier = modifier
        .ifNotNullThen(width) {
            width(it)
        }.ifNotNullThen(height) {
            height(it)
        }.conditional(minWidth != null || minHeight != null) {
            val width = minWidth ?: Dp.Unspecified
            val height = minHeight ?: Dp.Unspecified
            defaultMinSize(minWidth = width, minHeight = height)
        }.ifNotNullThen(fillMaxSize) {
            fillMaxSize(it)
        }.ifNotNullThen(fillMaxWidth) {
            fillMaxWidth(it)
        }.ifNotNullThen(fillMaxHeight) {
            fillMaxHeight(it)
        }.conditional(
            paddingAll != null ||
                    paddingHorizontal != null ||
                    paddingVertical != null ||
                    paddingStart != null ||
                    paddingTop != null ||
                    paddingEnd != null ||
                    paddingBottom != null
        ) {
            val all = paddingAll ?: 0.dp
            val horizontal = paddingHorizontal ?: all
            val vertical = paddingVertical ?: all
            val start = paddingStart ?: horizontal
            val top = paddingTop ?: vertical
            val end = paddingEnd ?: horizontal
            val bottom = paddingBottom ?: vertical
            padding(
                start = start,
                top = top,
                end = end,
                bottom = bottom
            )
        }.ifNotNullThen(backgroundColor) {
            background(color = it)
        }.conditional(verticalScroll) {
            composed {
                val vScroll = rememberScrollState()
                verticalScroll(vScroll)
            }
        }.conditional(horizontalScroll) {
            composed {
                val hScroll = rememberScrollState()
                horizontalScroll(hScroll)
            }
        }

    // ------------ Modifier Utils ------------
    private fun String.tryFloat(default: Float): Float = try {
        this.toFloat()
    } catch (ignored: Throwable) {
        default
    }

    private val String.dp: Dp
        get() = this.toFloat().dp
}

fun Modifier.conditional(condition: Boolean?, modifier: Modifier.() -> Modifier): Modifier {
    return if (condition == true) {
        then(modifier(Modifier))
    } else {
        this
    }
}

fun <T> Modifier.ifNotNullThen(value: T?, modifier: Modifier.(T) -> Modifier): Modifier {
    return if (value != null) {
        then(modifier.invoke(Modifier, value))
    } else {
        this
    }
}