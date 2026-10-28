package me.developes.humming.builder

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import me.developes.humming.builder.ui.HummingBuilder
import java.awt.Dimension

fun main() = application {
    Window(title = "HummingUI Builder", onCloseRequest = ::exitApplication,
        state = rememberWindowState(width = 1520.dp, height = 960.dp)) {
        window.minimumSize = Dimension(1000, 700)
        HummingBuilder(files = remember { BuilderDesktopFiles() })
    }
}
