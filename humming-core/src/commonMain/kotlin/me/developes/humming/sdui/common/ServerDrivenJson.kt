package me.developes.humming.sdui.common

import androidx.compose.ui.graphics.Color

fun String?.toColor(): Color {
    if (this == null) return Color.Unspecified
    try {
        val color = substring(if (this[0] == '#') 1 else 0)
        val colorHex = when (color.length) {
            3 -> {
                // #RGB -> #RRGGBB
                val r = color[0].toString().repeat(2)
                val g = color[1].toString().repeat(2)
                val b = color[2].toString().repeat(2)
                "ff$r$g$b".toLong(16)
            }
            4 -> {
                // #ARGB -> #AARRGGBB
                val a = color[0].toString().repeat(2)
                val r = color[1].toString().repeat(2)
                val g = color[2].toString().repeat(2)
                val b = color[3].toString().repeat(2)
                "$a$r$g$b".toLong(16)
            }
            6 -> "ff$color".toLong(16)
            8 -> color.toLong(16)
            else -> throw IllegalArgumentException("Unknown color: $this")
        }
        return Color(colorHex)
    } catch (t: Throwable) {
        throw IllegalArgumentException("Unknown color: $this", t)
    }
}
