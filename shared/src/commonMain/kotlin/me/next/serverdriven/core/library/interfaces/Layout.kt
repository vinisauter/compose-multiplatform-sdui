package me.next.serverdriven.core.library.interfaces

import androidx.compose.runtime.Composable
import generateUUID

interface Layout {
    val key: LayoutKey
        get() = uniqueLayoutKey

    @Composable
    fun Content()
}

typealias LayoutKey = String

val Layout.uniqueLayoutKey: LayoutKey
    get() = "Layout#${generateUUID()}"