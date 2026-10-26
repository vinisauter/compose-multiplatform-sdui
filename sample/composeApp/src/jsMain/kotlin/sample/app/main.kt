import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import sample.app.App
import sample.app.OfficialCatalogApp
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val body = document.body ?: return
    ComposeViewport(body) {
        if (window.location.search == "?catalog") OfficialCatalogApp() else App()
    }
}
