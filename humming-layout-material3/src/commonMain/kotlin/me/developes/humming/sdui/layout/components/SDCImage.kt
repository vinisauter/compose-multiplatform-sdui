package me.developes.humming.sdui.layout.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultFilterQuality
import androidx.compose.ui.layout.ContentScale
import coil3.compose.rememberAsyncImagePainter
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.Url
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.common.toColor
import me.developes.humming.sdui.layout.Layout

class SDCImage(
    val node: ServerDrivenNode,
    val viewModel: SDUIFormViewModel,
) : Layout {
    private val modifier = Modifier.fromNode(node)
    private val actions = node.propertyNodes("onClick")
    private val imageUrl: String? = node.property("imageUrl")
    private val lottieUrl: String? = node.property("lottieUrl")
    private val contentDescription: String? = node.property("contentDescription")
    private val alignment: Alignment = when (node.property("alignment")) {
        "TopStart" -> Alignment.TopStart
        "TopCenter" -> Alignment.TopCenter
        "TopEnd" -> Alignment.TopEnd
        "CenterStart" -> Alignment.CenterStart
        "Center" -> Alignment.Center
        "CenterEnd" -> Alignment.CenterEnd
        "BottomStart" -> Alignment.BottomStart
        "BottomCenter" -> Alignment.BottomCenter
        "BottomEnd" -> Alignment.BottomEnd
        else -> Alignment.Center
    }
    private val contentScale: ContentScale = when (node.property("contentScale")) {
        "Fit" -> ContentScale.Fit
        "Crop" -> ContentScale.Crop
        "Inside" -> ContentScale.Inside
        "FillBounds" -> ContentScale.FillBounds
        "FillHeight" -> ContentScale.FillHeight
        "FillWidth" -> ContentScale.FillWidth
        "None" -> ContentScale.None
        else -> ContentScale.Fit
    }
    private val alpha: Float = node.property("alpha")?.toFloatOrNull() ?: DefaultAlpha
    private val colorFilter: ColorFilter? = node.propertyJsonObject("colorFilter")?.let {
        it["type"]?.toString()?.let { type ->
            when (type) {
                "tint" -> {
                    val color = it["color"].toString().toColor()
                    val blendModeStr = it["blendMode"]?.toString()
                    val blendMode = when (blendModeStr) {
                        "SrcOver" -> BlendMode.SrcOver
                        "DstOver" -> BlendMode.DstOver
                        "SrcIn" -> BlendMode.SrcIn
                        "DstIn" -> BlendMode.DstIn
                        "SrcOut" -> BlendMode.SrcOut
                        "DstOut" -> BlendMode.DstOut
                        "SrcAtop" -> BlendMode.SrcAtop
                        "DstAtop" -> BlendMode.DstAtop
                        "Xor" -> BlendMode.Xor
                        "Plus" -> BlendMode.Plus
                        "Modulate" -> BlendMode.Modulate
                        "Screen" -> BlendMode.Screen
                        "Overlay" -> BlendMode.Overlay
                        "Darken" -> BlendMode.Darken
                        "Lighten" -> BlendMode.Lighten
                        "ColorDodge" -> BlendMode.ColorDodge
                        "ColorBurn" -> BlendMode.ColorBurn
                        "HardLight" -> BlendMode.Hardlight
                        "SoftLight" -> BlendMode.Softlight
                        "Difference" -> BlendMode.Difference
                        "Exclusion" -> BlendMode.Exclusion
                        "Multiply" -> BlendMode.Multiply
                        "Hue" -> BlendMode.Hue
                        "Saturation" -> BlendMode.Saturation
                        "Color" -> BlendMode.Color
                        "Luminosity" -> BlendMode.Luminosity
                        else -> BlendMode.SrcIn
                    }
                    ColorFilter.tint(color, blendMode)
                }

                "colorMatrix" -> {
                    val matrix = it["matrix"]?.let { matrixJson ->
                        val nums =
                            matrixJson.toString().removePrefix("[").removeSuffix("]").split(",")
                                .mapNotNull { numStr -> numStr.trim().toFloatOrNull() }
                        if (nums.size == 20) nums.toFloatArray() else null
                    }
                    if (matrix != null) {
                        ColorFilter.colorMatrix(ColorMatrix(matrix))
                    } else {
                        null
                    }
                }

                "lighting" -> {
                    val mul = it["mul"].toString().toColor()
                    val add = it["add"].toString().toColor()
                    ColorFilter.lighting(mul, add)
                }

                else -> null
            }
        }

    }
    private val filterQuality: FilterQuality = node.property("filterQuality").let {
        when (it) {
            "None" -> FilterQuality.None
            "Low" -> FilterQuality.Low
            "Medium" -> FilterQuality.Medium
            "High" -> FilterQuality.High
            else -> null
        }
    } ?: DefaultFilterQuality
    private val clipToBounds: Boolean = node.property("alpha")?.toBooleanStrictOrNull() ?: true

    @Composable
    override fun Content() {
        lottieUrl?.let { url ->
            Lottie(url)
            return
        }
        imageUrl?.let { url ->
Async(url)
            return
        }
    }

    @Composable
    fun Lottie(url: String) {
        val composition by rememberLottieComposition { LottieCompositionSpec.Url(url) }
        Image(
            painter = rememberLottiePainter(composition = composition),
            modifier = modifier.clickable {
                viewModel.invokeActions(actions)
            },
            contentDescription = contentDescription,
            alignment = alignment,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter,
//            placeholder = painterResource(id = R.drawable.placeholder_image),
//            error = painterResource(id = R.drawable.error_image)
        )
    }
    @Composable
    fun Async(url: String) {
        //val client = HttpClient()
        Image(
            painter  = rememberAsyncImagePainter(url),
            modifier = modifier.clickable {
                viewModel.invokeActions(actions)
            },
            contentDescription = contentDescription,
            alignment = alignment,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter
//            placeholder = painterResource(id = R.drawable.placeholder_image),
//            error = painterResource(id = R.drawable.error_image)
        )
    }
}
