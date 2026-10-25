import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import sample.app.LocalPreview
import sample.app.LocalStudio
import sample.app.BrowserStudioFiles
import sample.app.VersionedJourneyExample
import androidx.compose.runtime.remember

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val body = document.body ?: return
    ComposeViewport(body) {
        if (window.location.search == "?journey") VersionedJourneyExample()
        else if (window.location.search == "?preview") LocalPreview()
        else LocalStudio(files = remember { BrowserStudioFiles() })
    }
}
