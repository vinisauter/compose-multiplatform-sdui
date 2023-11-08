import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable

object AndroidApp {
    lateinit var INSTANCE: Application
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

@Composable
fun MainView() = App()
