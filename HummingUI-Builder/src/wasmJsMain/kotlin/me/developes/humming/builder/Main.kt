package me.developes.humming.builder

import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import me.developes.humming.builder.ui.HummingBuilder

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    document.body?.let { body -> ComposeViewport(body) { HummingBuilder(remember { BuilderBrowserFiles() }) } }
}
