import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import sample.app.App
import me.developes.humming.builder.ui.OfficialCatalogApp

fun MainViewController(): UIViewController = ComposeUIViewController { App() }

fun CatalogViewController(): UIViewController = ComposeUIViewController { OfficialCatalogApp() }
