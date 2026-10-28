import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.SavedStateHandle
import sample.app.App
import sample.app.DesktopRemoteStudio
import sample.app.DesktopCatalog
import me.developes.humming.builder.ui.AppBuilder
import sample.app.DesktopStudioFiles
import java.awt.Dimension

fun main(args: Array<String>) = application {
    Window(
        title = "sample",
        state = rememberWindowState(width = if (args.any { it in setOf("--remote-studio", "--catalog", "--builder") }) 1400.dp else 800.dp,
            height = if (args.any { it in setOf("--remote-studio", "--catalog", "--builder") }) 900.dp else 600.dp),
        onCloseRequest = ::exitApplication,
    ) {
        window.minimumSize = Dimension(350, 600)
        if ("--builder" in args) AppBuilder(files = androidx.compose.runtime.remember { DesktopStudioFiles() })
        else if ("--catalog" in args) DesktopCatalog()
        else if ("--remote-studio" in args) DesktopRemoteStudio() else App(
            initialResource = if ("--local-smoke" in args) {
                "files/layout/local-smoke.json"
            } else {
                "files/navigation/app-navigation.json"
            },
            savedStateHandleFactory = { SavedStateHandle() },
        )
    }
}
