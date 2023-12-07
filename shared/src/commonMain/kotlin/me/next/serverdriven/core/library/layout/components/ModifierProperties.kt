package me.next.serverdriven.core.library.layout.components

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.next.serverdriven.core.tree.ServerDrivenNode

fun Modifier.conditional(condition: Boolean?, modifier: Modifier.() -> Modifier): Modifier {
    return if (condition == true) {
        then(modifier(Modifier))
    } else {
        this
    }
}

fun Modifier.fromNode(
    node: ServerDrivenNode,
    state: MutableMap<String, String> = mutableMapOf()
): Modifier = ModifierProperties(node, this, state).get()

fun Modifier.fromNodeProperties(
    node: ServerDrivenNode,
    state: MutableMap<String, String> = mutableMapOf()
): ModifierProperties = ModifierProperties(node, this, state)

class ModifierProperties(
    node: ServerDrivenNode,
    private var modifier: Modifier = Modifier,
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

    init {
        width?.let { modifier = modifier.width(it) }
        height?.let { modifier = modifier.height(it) }
        if (minWidth != null || minHeight != null) {
            val width = minWidth ?: Dp.Unspecified
            val height = minHeight ?: Dp.Unspecified
            modifier = modifier.defaultMinSize(minWidth = width, minHeight = height)
        }
        fillMaxSize?.let { modifier = modifier.fillMaxSize(it) }
        fillMaxWidth?.let { modifier = modifier.fillMaxWidth(it) }
        fillMaxHeight?.let { modifier = modifier.fillMaxHeight(it) }
        if (paddingAll != null ||
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
            modifier = modifier.padding(
                start = start,
                top = top,
                end = end,
                bottom = bottom
            )
        }
        backgroundColor?.let { modifier.background(color = it) }
    }

    fun get(): Modifier {
        return modifier.conditional(width != null) {
            width(width!!)
        }.conditional(height != null) {
            height(height!!)
        }
    }

    @Composable
    fun getComposable(): Modifier {
        val vScroll = rememberScrollState()
        val hScroll = rememberScrollState()
        return get()
            .conditional(verticalScroll) {
                verticalScroll(vScroll)
            }.conditional(horizontalScroll) {
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