package br.com.developes.sdui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope

@Composable
fun CoroutineScopeLocalProvider(
    block: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    CompositionLocalProvider(LocalCoroutineScope provides coroutineScope) {
        block.invoke()
    }
}

val LocalCoroutineScope = staticCompositionLocalOf<CoroutineScope> {
    error("CompositionLocal LocalCoroutineScope not present")
}