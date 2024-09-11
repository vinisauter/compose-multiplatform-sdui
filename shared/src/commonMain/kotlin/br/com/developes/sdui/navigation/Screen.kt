package br.com.developes.sdui.navigation

import androidx.compose.runtime.Composable
import br.com.developes.sdui.generateUUID

interface Screen {
    val key: ScreenKey
        get() = uniqueScreenKey

    @Composable
    fun Content()
}

typealias ScreenKey = String

val Screen.uniqueScreenKey: ScreenKey
    get() = "Screen#${generateUUID()}"