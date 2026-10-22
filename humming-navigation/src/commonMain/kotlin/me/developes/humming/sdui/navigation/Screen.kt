package me.developes.humming.sdui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import me.developes.humming.sdui.common.ServerDrivenNode

val LocalScreenTracker = compositionLocalOf<Screen> {
    error("CompositionLocal LocalScreenTracker not present")
}
// TODO add lifecycle methods (onAppear, onDisappear)
abstract class Screen(
    val name: String,
    val node: ServerDrivenNode,
) {

    @Composable
    fun Start() {
        CompositionLocalProvider(LocalScreenTracker provides this) {
            Content()
        }
    }

    @Composable
    abstract fun Content()
}