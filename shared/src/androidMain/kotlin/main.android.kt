import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import me.next.serverdriven.compose.SDCLibrary.Companion.launchHandling
import me.next.serverdriven.core.library.navigation.components.LocalNavigator
import java.util.UUID

object AndroidApp {
    lateinit var INSTANCE: Application
    fun init(application: Application) {
        INSTANCE = application
    }
}

actual fun getPlatformName(): String = "Android"
internal actual fun openUrl(url: String?) {
    val uri = url?.let { Uri.parse(it) } ?: return
    val intent = Intent().apply {
        action = Intent.ACTION_VIEW
        data = uri
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
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
