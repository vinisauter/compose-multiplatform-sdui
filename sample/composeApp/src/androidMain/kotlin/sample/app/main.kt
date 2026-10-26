package sample.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.getBooleanExtra("nativeExample", false)) {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            )
        }
        setContent {
            if (intent.getBooleanExtra("nativeExample", false)) {
                NativeCapabilityExample(remember { androidNativeBridge() }, readOnStart = true)
            } else if (intent.getBooleanExtra("catalog", false)) OfficialCatalogApp(bridge = remember { androidNativeBridge() })
            else App()
        }
    }
}
