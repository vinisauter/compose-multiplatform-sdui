package me.next.serverdriven.core.library.interfaces

import androidx.compose.runtime.Composable
import generateUUID


interface Screen {
    val key: ScreenKey
        get() = uniqueScreenKey

    @Composable
    fun Content()
}

typealias ScreenKey = String

val Screen.uniqueScreenKey: ScreenKey
    get() = "Screen#${generateUUID()}"