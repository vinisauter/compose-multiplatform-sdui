import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import sample.app.App
import sample.app.OfficialCatalogApp

fun MainViewController(): UIViewController = ComposeUIViewController { App() }

fun CatalogViewController(): UIViewController = ComposeUIViewController { OfficialCatalogApp() }
