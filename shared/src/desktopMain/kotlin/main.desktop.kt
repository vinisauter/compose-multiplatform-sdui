import androidx.compose.runtime.Composable
import java.awt.Desktop
import java.net.URI
import java.util.UUID

actual fun getPlatformName(): String = "Desktop"
internal actual fun openUrl(url: String?) {
    val uri = url?.let { URI.create(it) } ?: return
    Desktop.getDesktop().browse(uri)
}

actual fun generateUUID() = UUID.randomUUID().toString()

@Composable
fun MainView() = App()