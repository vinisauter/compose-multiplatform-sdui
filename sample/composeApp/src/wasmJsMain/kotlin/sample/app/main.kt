import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import sample.app.LocalPreview
import me.developes.humming.builder.ui.LocalStudio
import sample.app.BrowserStudioFiles
import me.developes.humming.builder.ui.VersionedJourneyExample
import me.developes.humming.builder.ui.NativeCapabilityExample
import me.developes.humming.builder.ui.OfficialCatalogApp
import me.developes.humming.builder.ui.AppBuilder
import me.developes.humming.sdui.common.NativeBridge
import androidx.compose.runtime.remember

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val body = document.body ?: return
    ComposeViewport(body) {
        if (window.location.search == "?native") NativeCapabilityExample(remember { NativeBridge(emptyMap()) })
        else if (window.location.search == "?journey") VersionedJourneyExample()
        else if (window.location.search == "?preview") LocalPreview()
        else if (window.location.search == "?builder") AppBuilder(files = remember { BrowserStudioFiles() })
        else if (window.location.search == "?studio") LocalStudio(files = remember { BrowserStudioFiles() })
        else OfficialCatalogApp(files = remember { BrowserStudioFiles() })
    }
}
