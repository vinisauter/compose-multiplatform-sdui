package br.com.developes.sdui

import androidx.compose.ui.window.ComposeUIViewController
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

    fun MainViewController() = ComposeUIViewController { App() }
}

actual fun getPlatform(): Platform = IOSPlatform()
actual fun generateUUID(): String = NSUUID().UUIDString()
actual fun openUrl(url: String?) {
    val nsUrl = url?.let { NSURL.URLWithString(it) } ?: return
    UIApplication.sharedApplication.openURL(nsUrl)
}
