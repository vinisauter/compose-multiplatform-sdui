package sample.app

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import java.io.File
import kotlin.test.assertEquals

class LocalSmokeTest {
    @get:Rule
    val compose = createComposeRule()

    private val owner = object : ViewModelStoreOwner {
        override val viewModelStore = ViewModelStore()
    }

    @After
    fun clearViewModels() {
        compose.runOnIdle { owner.viewModelStore.clear() }
    }

    @Test
    fun localPayloadRendersAndUpdatesBindingsThroughRegisteredAction() {
        var handleCreations = 0
        compose.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                App(
                    initialResource = "files/layout/local-smoke.json",
                    savedStateHandleFactory = {
                        handleCreations++
                        SavedStateHandle()
                    },
                )
            }
        }

        awaitText("Nome informado: Visitante")
        compose.onNodeWithText("Visitante").performTextReplacement("Pessoa de teste")
        awaitText("Nome informado: Pessoa de teste")

        compose.onNodeWithText("Confirmar").performClick()
        awaitText("Status: confirmado")
        compose.runOnIdle { assertEquals(1, handleCreations) }

        val screenshot = compose.onRoot().captureToImage()
        val output = File("build/reports/local-smoke.png")
        output.parentFile.mkdirs()
        Image.makeFromBitmap(screenshot.asSkiaBitmap()).use { image ->
            requireNotNull(image.encodeToData(EncodedImageFormat.PNG)).use { png ->
                output.writeBytes(png.bytes)
            }
        }
    }

    private fun awaitText(text: String) {
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().size == 1
        }
    }
}
