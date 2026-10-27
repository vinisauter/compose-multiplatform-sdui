package me.developes.humming.sdui.layout.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.*
import androidx.compose.ui.zIndex
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.float
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.common.toColor

fun Modifier.fromNode(
    node: ServerDrivenNode,
): Modifier = ModifierProperties(node, this).modifier

class ModifierProperties(
    private val node: ServerDrivenNode,
    modifier: Modifier = Modifier
) {
    init {
        me.developes.humming.sdui.common.NodeDefinition(node.component,
            me.developes.humming.sdui.common.NodeRole.COMPONENT,
            me.developes.humming.sdui.layout.LayoutDefinitions.modifiers).parse(node).requireValid()
    }
    private val width = node.property("width")?.dp
    private val height = node.property("height")?.dp
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
    private val backgroundColor = node.property("backgroundColor")?.toColor()
    private val backgroundShape = node.property("backgroundShape")
    private val backgroundShapeSize = node.property("backgroundShapeSize")?.toIntOrNull()
    private val backgroundShapeTopStart = node.property("backgroundShapeTopStart")?.toIntOrNull()
    private val backgroundShapeTopEnd = node.property("backgroundShapeTopEnd")?.toIntOrNull()
    private val backgroundShapeBottomStart = node.property("backgroundShapeBottomStart")?.toIntOrNull()
    private val backgroundShapeBottomEnd = node.property("backgroundShapeBottomEnd")?.toIntOrNull()
    private val backgroundGradient =
        node.propertyJsonObject("backgroundGradient")?.let { gradient ->
            val type = gradient["type"]?.jsonPrimitive?.content
            val colors = gradient["colors"]?.let { colors ->
                colors.jsonArray.map { it.jsonPrimitive.content.toColor() }
            } ?: emptyList()
            val tileMode: TileMode = gradient["tileMode"]?.let {
                when (it.jsonPrimitive.content) {
                    "Clamp" -> TileMode.Clamp
                    "Repeat" -> TileMode.Repeated
                    "Mirror" -> TileMode.Mirror
                    "Decal" -> TileMode.Decal
                    else -> null
                }
            } ?: TileMode.Clamp

            if (type == "linear") {
                val start: Offset = gradient["startOffset"]?.let { offset ->
                    Offset(
                        offset.jsonObject["x"]!!.jsonPrimitive.float,
                        offset.jsonObject["y"]!!.jsonPrimitive.float
                    )
                } ?: Offset.Zero
                val end: Offset = gradient["endOffset"]?.let { offset ->
                    Offset(
                        offset.jsonObject["x"]!!.jsonPrimitive.float,
                        offset.jsonObject["y"]!!.jsonPrimitive.float
                    )
                } ?: Offset.Infinite
                Brush.linearGradient(colors = colors, tileMode = tileMode, start = start, end = end)
            } else if (type == "radial") {
                val center: Offset = gradient["centerOffset"]?.let { offset ->
                    Offset(
                        offset.jsonObject["x"]!!.jsonPrimitive.float,
                        offset.jsonObject["y"]!!.jsonPrimitive.float
                    )
                } ?: Offset.Unspecified
                val radius = gradient["radius"]?.jsonPrimitive?.float ?: Float.POSITIVE_INFINITY
                Brush.radialGradient(
                    colors = colors,
                    tileMode = tileMode,
                    center = center,
                    radius = radius
                )
            } else if (type == "sweep") {
                val center: Offset = gradient["centerOffset"]?.let { offset ->
                    Offset(
                        offset.jsonObject["x"]!!.jsonPrimitive.float,
                        offset.jsonObject["y"]!!.jsonPrimitive.float
                    )
                } ?: Offset.Unspecified
                Brush.sweepGradient(colors = colors, center = center)
            } else if (type == "vertical") {
                val startY = gradient["startY"]?.jsonPrimitive?.float ?: 0.0f
                val endY = gradient["endY"]?.jsonPrimitive?.float ?: Float.POSITIVE_INFINITY
                Brush.verticalGradient(
                    colors = colors,
                    tileMode = tileMode,
                    startY = startY,
                    endY = endY
                )
            } else if (type == "horizontal") {
                val startX = gradient["startX"]?.jsonPrimitive?.float ?: 0.0f
                val endX = gradient["endX"]?.jsonPrimitive?.float ?: Float.POSITIVE_INFINITY
                Brush.horizontalGradient(
                    colors = colors,
                    tileMode = tileMode,
                    startX = startX,
                    endX = endX
                )
            } else {
                null
            }
        }
    private val verticalScroll = node.property("verticalScroll")?.let { true }
    private val horizontalScroll = node.property("horizontalScroll")?.let { true }
    private val shape = backgroundShape.toShape(backgroundShapeSize, backgroundShapeTopStart,
        backgroundShapeTopEnd, backgroundShapeBottomStart, backgroundShapeBottomEnd)
    private fun number(name: String) = node.property(name)?.toFloatOrNull()?.takeIf { it.isFinite() }
    private fun enabled(name: String) = node.property(name) == "true"

    private fun Modifier.extendedLayout(): Modifier {
        var result = this
        number("size")?.let { result = result.size(it.dp) }
        number("requiredSize")?.let { result = result.requiredSize(it.dp) }
        number("requiredWidth")?.let { result = result.requiredWidth(it.dp) }
        number("requiredHeight")?.let { result = result.requiredHeight(it.dp) }
        if (number("widthInMin") != null || number("widthInMax") != null)
            result = result.widthIn(number("widthInMin")?.dp ?: Dp.Unspecified, number("widthInMax")?.dp ?: Dp.Unspecified)
        if (number("heightInMin") != null || number("heightInMax") != null)
            result = result.heightIn(number("heightInMin")?.dp ?: Dp.Unspecified, number("heightInMax")?.dp ?: Dp.Unspecified)
        if (listOf("requiredMinWidth", "requiredMinHeight", "requiredMaxWidth", "requiredMaxHeight").any { number(it) != null })
            result = result.requiredSizeIn(number("requiredMinWidth")?.dp ?: Dp.Unspecified,
                number("requiredMinHeight")?.dp ?: Dp.Unspecified, number("requiredMaxWidth")?.dp ?: Dp.Unspecified,
                number("requiredMaxHeight")?.dp ?: Dp.Unspecified)
        node.property("wrapContentSize")?.let { value -> result = result.wrapContentSize(when(value) {
            "TopStart" -> Alignment.TopStart; "TopCenter" -> Alignment.TopCenter; "TopEnd" -> Alignment.TopEnd
            "CenterStart" -> Alignment.CenterStart; "CenterEnd" -> Alignment.CenterEnd
            "BottomStart" -> Alignment.BottomStart; "BottomCenter" -> Alignment.BottomCenter; "BottomEnd" -> Alignment.BottomEnd
            else -> Alignment.Center
        }, enabled("wrapContentUnbounded")) }
        number("maxWidth")?.let { result = result.widthIn(max = it.dp) }
        number("maxHeight")?.let { result = result.heightIn(max = it.dp) }
        number("aspectRatio")?.let { result = result.aspectRatio(it, enabled("aspectRatioMatchHeightConstraintsFirst")) }
        node.property("wrapContentWidth")?.let { value -> result = result.wrapContentWidth(
            when(value) { "Start" -> Alignment.Start; "End" -> Alignment.End; else -> Alignment.CenterHorizontally }, enabled("wrapContentUnbounded")) }
        node.property("wrapContentHeight")?.let { value -> result = result.wrapContentHeight(
            when(value) { "Top" -> Alignment.Top; "Bottom" -> Alignment.Bottom; else -> Alignment.CenterVertically }, enabled("wrapContentUnbounded")) }
        if (number("offsetX") != null || number("offsetY") != null)
            result = result.offset((number("offsetX") ?: 0f).dp, (number("offsetY") ?: 0f).dp)
        if (number("absoluteOffsetX") != null || number("absoluteOffsetY") != null)
            result = result.absoluteOffset((number("absoluteOffsetX") ?: 0f).dp, (number("absoluteOffsetY") ?: 0f).dp)
        number("zIndex")?.let { result = result.zIndex(it) }
        number("modifierAlpha")?.let { result = result.alpha(it) }
        number("rotation")?.let { result = result.rotate(it) }
        if (number("scaleX") != null || number("scaleY") != null)
            result = result.scale(number("scaleX") ?: 1f, number("scaleY") ?: 1f)
        number("shadowElevation")?.let { result = result.shadow(it.dp, shape, enabled("shadowClip")) }
        if (enabled("clipToBounds")) result = result.clipToBounds()
        if (enabled("clip")) result = result.clip(shape)
        return result
    }

    val modifier = modifier.extendedLayout()
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
        }.conditional(node.property("paddingToken") != null) {
            composed {
                val design = LocalProjectDesignSystem.current
                val spacing = design?.spacing ?: me.developes.humming.sdui.common.DesignSystem().spacing
                padding(spacing.getValue(node.property("paddingToken")!!).dp)
            }
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
        }.ifNotNullThen(backgroundGradient) {
            background(brush = it)
        }.composed {
            ifNotNullThen(backgroundColor) {
                background(
                    color = it,
                    shape = backgroundShape.toShape(
                        size = backgroundShapeSize,
                        topStart = backgroundShapeTopStart,
                        topEnd = backgroundShapeTopEnd,
                        bottomStart = backgroundShapeBottomStart,
                        bottomEnd = backgroundShapeBottomEnd
                    )
                )
            }
        }.conditional(number("borderWidth") != null) {
            border(number("borderWidth")!!.dp, node.property("borderColor")?.toColor() ?: androidx.compose.ui.graphics.Color.Black, shape)
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
    private fun String.tryFloat(default: Float): Float = toFloatOrNull() ?: default

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

private fun String?.toShape(
    size: Int?,
    topStart: Int?,
    topEnd: Int?,
    bottomStart: Int?,
    bottomEnd: Int?
): Shape {
    return when (this) {
        "CircleShape" -> CircleShape
        "RectangleShape" -> RectangleShape
        "RoundedCornerShape" -> generateRoundedCornerShape(
            size = size,
            topStart = topStart,
            topEnd = topEnd,
            bottomStart = bottomStart,
            bottomEnd = bottomEnd
        )

        "CutCornerShape" -> generateCutCornerShape(
            size = size,
            topStart = topStart,
            topEnd = topEnd,
            bottomStart = bottomStart,
            bottomEnd = bottomEnd
        )

        else -> RectangleShape
    }
}

private fun generateRoundedCornerShape(
    size: Int?,
    topStart: Int?,
    topEnd: Int?,
    bottomStart: Int?,
    bottomEnd: Int?
): RoundedCornerShape {
    val default = 0.dp

    return if (size != null) {
        RoundedCornerShape(size = size.dp)
    } else {
        RoundedCornerShape(
            topStart = topStart?.dp ?: default,
            topEnd = topEnd?.dp ?: default,
            bottomStart = bottomStart?.dp ?: default,
            bottomEnd = bottomEnd?.dp ?: default
        )
    }
}

private fun generateCutCornerShape(
    size: Int?,
    topStart: Int?,
    topEnd: Int?,
    bottomStart: Int?,
    bottomEnd: Int?
): CutCornerShape {
    val default = 0.dp

    return if (size != null) {
        CutCornerShape(size = size.dp)
    } else {
        CutCornerShape(
            topStart = topStart?.dp ?: default,
            topEnd = topEnd?.dp ?: default,
            bottomStart = bottomStart?.dp ?: default,
            bottomEnd = bottomEnd?.dp ?: default
        )
    }
}