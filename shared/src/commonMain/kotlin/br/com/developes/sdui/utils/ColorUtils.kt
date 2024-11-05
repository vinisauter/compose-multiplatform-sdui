package br.com.developes.sdui.utils

import androidx.compose.ui.graphics.Color

/**
 * Supports colors in hex format AARRGGBB
 * */
fun String.hexToColor(): Color {
    val alpha = this.substring(0, 2).toInt(16)
    val red = this.substring(2, 4).toInt(16)
    val green = this.substring(4, 6).toInt(16)
    val blue = this.substring(6, 8).toInt(16)
    return Color(alpha = alpha, red = red, green = green, blue = blue)
}

/**
 * Supports colors in hex format RRGGBB
 * */
fun String.hexToRgbColor(): Color {
    val red = this.substring(0, 2).toInt(16)
    val green = this.substring(2, 4).toInt(16)
    val blue = this.substring(4, 6).toInt(16)

    return Color(red = red, green = green, blue = blue)
}