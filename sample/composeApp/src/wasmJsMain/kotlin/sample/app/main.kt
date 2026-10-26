import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import sample.app.LocalPreview
import sample.app.LocalStudio
import sample.app.BrowserStudioFiles
import sample.app.VersionedJourneyExample
import sample.app.NativeCapabilityExample
import sample.app.OfficialCatalogApp
import me.developes.humming.sdui.common.NativeBridge
import androidx.compose.runtime.remember

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val body = document.body ?: return
    ComposeViewport(body) {
        if (window.location.search == "?native") NativeCapabilityExample(remember { NativeBridge(emptyMap()) })
        else if (window.location.search == "?journey") VersionedJourneyExample()
        else if (window.location.search == "?preview") LocalPreview()
        else if (window.location.search == "?studio") LocalStudio(files = remember { BrowserStudioFiles() })
        else OfficialCatalogApp(files = remember { BrowserStudioFiles() })
    }
}
