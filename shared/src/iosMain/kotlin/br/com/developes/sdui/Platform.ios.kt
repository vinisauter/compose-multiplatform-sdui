package br.com.developes.sdui

import androidx.compose.ui.window.ComposeUIViewController
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

    fun MainViewController() = ComposeUIViewController { App() }

    fun initialize() {
        Firebase.initialize()
    }
}

actual fun getPlatform(): Platform = IOSPlatform()
actual fun generateUUID(): String = NSUUID().UUIDString()
actual fun openUrl(url: String?) {
    val nsUrl = url?.let { NSURL.URLWithString(it) } ?: return
    UIApplication.sharedApplication.openURL(nsUrl)
}
