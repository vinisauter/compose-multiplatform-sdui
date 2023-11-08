import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import java.awt.Desktop
import java.net.URI

actual fun getPlatformName(): String = "Desktop"
internal actual fun openUrl(url: String?) {
    val uri = url?.let { URI.create(it) } ?: return
    Desktop.getDesktop().browse(uri)
}

@Composable
fun MainView() = App()

@Preview
@Composable
fun AppPreview() {
    App()
}