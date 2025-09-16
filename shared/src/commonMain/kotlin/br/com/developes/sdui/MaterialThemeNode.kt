package br.com.developes.sdui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.developes.sdui.utils.fromString
import br.com.developes.sdui.utils.toColor
import kotlinx.serialization.Serializable

@Serializable
open class MaterialThemeNode(
    private val colors: Map<String, String?>? = null,
    private val typography: Map<String, TypographyNode?>? = null,
    private val shapes: Map<String, Int?>? = null
) {

    @Composable
    fun color(key: String) = colors?.get(key)?.toColor()

    fun typography(key: String) = typography?.get(key)?.let {
        TextStyle.Default.copy(
            fontWeight = FontWeight.fromString(it.fontWeight),
            fontSize = it.fontSize.sp,
            lineHeight = it.lineHeight.sp,
            letterSpacing = (it.letterSpacing).sp
        )
    }

    fun shape(key: String) = shapes?.get(key)?.let {
        RoundedCornerShape(size = it.dp)
    }
}

@Serializable
open class TypographyNode(
    val fontWeight: String,
    val fontSize: Int,
    val lineHeight: Int,
    val letterSpacing: Float
)

@Composable
fun MaterialThemeNode.toMaterialTheme(content: @Composable () -> Unit) = MaterialTheme(
    colors = MaterialTheme.colors.copy(
        primary = color("primary") ?: MaterialTheme.colors.primary,
        primaryVariant = color("primaryVariant") ?: MaterialTheme.colors.primaryVariant,
        secondary = color("secondary") ?: MaterialTheme.colors.secondary,
        secondaryVariant = color("secondaryVariant") ?: MaterialTheme.colors.secondaryVariant,
        background = color("background") ?: MaterialTheme.colors.background,
        surface = color("surface") ?: MaterialTheme.colors.surface,
        error = color("error") ?: MaterialTheme.colors.error,
        onPrimary = color("onPrimary") ?: MaterialTheme.colors.onPrimary,
        onSecondary = color("onSecondary") ?: MaterialTheme.colors.onSecondary,
        onBackground = color("onBackground") ?: MaterialTheme.colors.onBackground,
        onSurface = color("onSurface") ?: MaterialTheme.colors.onSurface,
        onError = color("onError") ?: MaterialTheme.colors.onError,
        isLight = MaterialTheme.colors.isLight
    ),
    typography = MaterialTheme.typography.copy(
        h1 = typography("h1") ?: MaterialTheme.typography.h1,
        h2 = typography("h2") ?: MaterialTheme.typography.h2,
        h3 = typography("h3") ?: MaterialTheme.typography.h3,
        h4 = typography("h4") ?: MaterialTheme.typography.h4,
        h5 = typography("h5") ?: MaterialTheme.typography.h5,
        h6 = typography("h6") ?: MaterialTheme.typography.h6,
        subtitle1 = typography("subtitle1") ?: MaterialTheme.typography.subtitle1,
        subtitle2 = typography("subtitle2") ?: MaterialTheme.typography.subtitle2,
        body1 = typography("body1") ?: MaterialTheme.typography.body1,
        body2 = typography("body2") ?: MaterialTheme.typography.body2,
        button = typography("button") ?: MaterialTheme.typography.button,
        caption = typography("caption") ?: MaterialTheme.typography.caption,
        overline = typography("overline") ?: MaterialTheme.typography.overline
    ),
    shapes = MaterialTheme.shapes.copy(
        small = shape("small") ?: MaterialTheme.shapes.small,
        medium = shape("medium") ?: MaterialTheme.shapes.medium,
        large = shape("large") ?: MaterialTheme.shapes.large
    ),
    content = content
)
