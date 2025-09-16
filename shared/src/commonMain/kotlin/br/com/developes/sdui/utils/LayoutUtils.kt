package br.com.developes.sdui.utils

import androidx.compose.ui.text.font.FontWeight

fun FontWeight.Companion.fromString(value: String?) = when(value) {
    null, "Normal" -> Normal
    "Bold" -> Bold
    "Thin" -> Thin
    "ExtraBold" -> ExtraBold
    "Light" -> Light
    "Medium" -> Medium
    else -> {
        error("Unknown value for fontWeight $value")
        Normal
    }
}