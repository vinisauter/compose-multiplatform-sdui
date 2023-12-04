import androidx.compose.ui.window.ComposeUIViewController
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.UIKit.UIApplication

actual fun getPlatformName(): String = "iOS"
internal actual fun openUrl(url: String?) {
    val nsUrl = url?.let { NSURL.URLWithString(it) } ?: return
    UIApplication.sharedApplication.openURL(nsUrl)
}

actual fun generateUUID(): String = NSUUID().UUIDString()
fun MainViewController() = ComposeUIViewController { App() }