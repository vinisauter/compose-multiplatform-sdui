package me.developes.humming.sdui.layout.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.painter.Painter

/** Client-owned image capabilities. Payloads select registered keys, never platform paths. */
val LocalImageSources = staticCompositionLocalOf<Map<String, @Composable () -> Painter>> { emptyMap() }
