package sample.app

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import org.junit.Rule
import org.junit.Test

class LocalPreviewTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun localPreviewEditsExecutesDiagnosesAndDisposesItsState() {
        val mounted = mutableStateOf(true)
        compose.setContent { if (mounted.value) LocalPreview() }
        awaitText("Nome informado: Visitante")
        compose.onNodeWithText("Visitante").performTextReplacement("Preview JVM")
        awaitText("Nome informado: Preview JVM")
        compose.onNodeWithText("Confirmar").performClick()
        awaitText("Status: confirmado")
        compose.onNodeWithText("Testar diagnóstico").performClick()
        awaitText("Diagnóstico: MISSING_METHOD")
        compose.onNodeWithText("Limpar diagnóstico").performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText("Diagnóstico: MISSING_METHOD").fetchSemanticsNodes().isEmpty()
        }
        compose.runOnIdle { mounted.value = false }
        compose.waitForIdle()
        compose.runOnIdle { mounted.value = true }
        awaitText("Nome informado: Visitante")
        compose.runOnIdle { mounted.value = false }
        compose.waitForIdle()
    }

    private fun awaitText(text: String) {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().size == 1
        }
    }
}
