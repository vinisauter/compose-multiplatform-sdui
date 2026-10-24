package me.developes.humming.sdui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/** A navigation adapter reuses the enclosing host's loading/error UI for its entry owner. */
val LocalScreenFeedbackHandler = staticCompositionLocalOf<@Composable (SDUIFormViewModel) -> Unit> { {} }
