package br.com.developes.sdui

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import br.com.developes.sdui.SDCLibrary.Companion.launchHandling
import br.com.developes.sdui.navigation.components.LocalNavigator
import java.util.UUID

object AndroidApp {
    lateinit var INSTANCE: Application
    fun init(application: Application) {
        INSTANCE = application
    }
}

class AndroidPlatform : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun openUrl(url: String?) {
    val uri = url?.let { Uri.parse(it) } ?: return
    val intent = Intent().apply {
        action = Intent.ACTION_VIEW
        data = uri
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
//    FIXME: Use: LocalContext.current.startActivity(intent)
    AndroidApp.INSTANCE.startActivity(intent)
}

actual fun generateUUID() = UUID.randomUUID().toString()

@Composable
fun MainView() {
    val scope = rememberCoroutineScope()
    BackPressHandler {
        scope.launchHandling {
            LocalNavigator?.navigateBack()
        }
    }
    App()
}