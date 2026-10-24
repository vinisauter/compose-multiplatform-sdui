import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import sample.app.LocalPreview

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val body = document.body ?: return
    ComposeViewport(body) {
        LocalPreview()
    }
}
