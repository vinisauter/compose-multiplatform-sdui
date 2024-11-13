package br.com.developes.sdui.utils

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun String.toColor() = when {
    this.startsWith("theme.") -> this.toMaterialThemeColor()
    this.startsWith("#") -> this.toHexColor()
    else -> null
}

@Composable
private fun String.toMaterialThemeColor() = when (this.removePrefix("theme.")) {
    "primary" -> MaterialTheme.colors.primary
    "primaryVariant" -> MaterialTheme.colors.primaryVariant
    "secondary" -> MaterialTheme.colors.secondary
    "secondaryVariant" -> MaterialTheme.colors.secondaryVariant
    "background" -> MaterialTheme.colors.background
    "surface" -> MaterialTheme.colors.surface
    "error" -> MaterialTheme.colors.error
    "onPrimary" -> MaterialTheme.colors.onPrimary
    "onSecondary" -> MaterialTheme.colors.onSecondary
    "onBackground" -> MaterialTheme.colors.onBackground
    "onSurface" -> MaterialTheme.colors.onSurface
    "onError" -> MaterialTheme.colors.onError
    else -> null
}

private fun String.toHexColor() = when {
    this.length == 7 -> this.hexToRgbColor()
    this.length == 9 -> this.hexToColor()
    else -> null
}

/**
 * Supports colors in hex format AARRGGBB
 * */
private fun String.hexToColor(): Color {
    val alpha = this.substring(1, 3).toInt(16)
    val red = this.substring(3, 5).toInt(16)
    val green = this.substring(5, 7).toInt(16)
    val blue = this.substring(7, 9).toInt(16)
    return Color(alpha = alpha, red = red, green = green, blue = blue)
}

/**
 * Supports colors in hex format RRGGBB
 * */
private fun String.hexToRgbColor(): Color {
    val red = this.substring(1, 3).toInt(16)
    val green = this.substring(3, 5).toInt(16)
    val blue = this.substring(5, 7).toInt(16)

    return Color(red = red, green = green, blue = blue)
}
