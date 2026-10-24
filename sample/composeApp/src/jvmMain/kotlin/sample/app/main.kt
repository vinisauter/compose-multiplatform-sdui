import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.SavedStateHandle
import sample.app.App
import java.awt.Dimension

fun main(args: Array<String>) = application {
    Window(
        title = "sample",
        state = rememberWindowState(width = 800.dp, height = 600.dp),
        onCloseRequest = ::exitApplication,
    ) {
        window.minimumSize = Dimension(350, 600)
        App(
            initialResource = if ("--local-smoke" in args) {
                "files/layout/local-smoke.json"
            } else {
                "files/navigation/app-navigation.json"
            },
            savedStateHandleFactory = { SavedStateHandle() },
        )
    }
}
