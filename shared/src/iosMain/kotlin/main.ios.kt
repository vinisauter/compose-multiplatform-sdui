import androidx.compose.ui.window.ComposeUIViewController

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun getPlatformName(): String = "iOS"
internal actual fun openUrl(url: String?) {
    val nsUrl = url?.let { NSURL.URLWithString(it) } ?: return
    UIApplication.sharedApplication.openURL(nsUrl)
}

fun MainViewController() = ComposeUIViewController { App() }