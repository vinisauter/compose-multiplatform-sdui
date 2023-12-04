package me.next.serverdriven.interfaces

import androidx.compose.runtime.Composable
import generateUUID

interface Layout {
    val key: LayoutKey
        get() = uniqueLayoutKey

    @Composable
    fun Content()
}

typealias LayoutKey = String

public val Layout.uniqueLayoutKey: LayoutKey
    get() = "Layout#${generateUUID()}"