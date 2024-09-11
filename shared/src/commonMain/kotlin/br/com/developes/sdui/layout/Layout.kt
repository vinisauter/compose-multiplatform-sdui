package br.com.developes.sdui.layout

import androidx.compose.runtime.Composable
import br.com.developes.sdui.generateUUID

interface Layout {
    val key: LayoutKey
        get() = uniqueLayoutKey

    @Composable
    fun Content()
}

typealias LayoutKey = String

val Layout.uniqueLayoutKey: LayoutKey
    get() = "Layout#${generateUUID()}"