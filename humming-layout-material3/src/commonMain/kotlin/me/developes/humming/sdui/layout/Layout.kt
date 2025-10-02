package me.developes.humming.sdui.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.developes.humming.sdui.common.ComposableContent
import me.developes.humming.sdui.getPlatform

interface Layout : ComposableContent {
    val key: LayoutKey
        get() = uniqueLayoutKey

    @Composable
    override fun Content()
}

typealias LayoutKey = String

val Layout.uniqueLayoutKey: LayoutKey
    get() = "Layout#${getPlatform().generateUUID()}"

val String.dp: Dp
    get() = this.toFloat().dp