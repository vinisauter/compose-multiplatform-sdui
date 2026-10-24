package sample.app

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import me.developes.humming.sdui.HummingSDUI
import me.developes.humming.sdui.action.SDActions
import me.developes.humming.sdui.common.parseNode
import me.developes.humming.sdui.layout.SDLayoutMaterial3
import org.junit.After
import org.junit.Rule
import org.junit.Test

class ActionRecoveryTest {
    @get:Rule val compose = createComposeRule()
    private val mounted = mutableStateOf(true)
    private val owner = object : ViewModelStoreOwner {
        override val viewModelStore = ViewModelStore()
    }

    @After fun cleanup() {
        compose.runOnIdle { mounted.value = false }
        compose.waitForIdle()
        compose.runOnIdle { owner.viewModelStore.clear() }
    }

    @Test fun failedActionCanBeDismissedAndRetriedThroughTheHost() {
        var attempts = 0
        val config = HummingSDUI.Config(
            type = "local", destiny = "recovery",
            errorHandler = { _, vm ->
                TextButton(onClick = { vm.dismissError() }) { Text("Fechar falha") }
            },
        ) {
            addLibrary(SDLayoutMaterial3())
            addLibrary(SDActions().registerMethod("unstable") { _, vm ->
                if (++attempts == 1) error("Recoverable fixture")
                vm.updateStateFromAction("status", "recuperado")
            })
            addNodeProvider("local") {
                parseNode("""{"component":"layout:column","children":[{"component":"layout:textButton","properties":{"text":"Tentar","onClick":{"component":"action:method","properties":{"invoke":"unstable"}}}},{"component":"layout:text","properties":{"text":"Status: #{status}"}}]}""")
            }
        }
        compose.setContent {
            if (mounted.value) CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                HummingSDUI().Content(config, savedStateHandleFactory = { SavedStateHandle() })
            }
        }
        awaitText("Tentar")
        compose.onNodeWithText("Tentar").performClick()
        awaitText("Fechar falha")
        compose.onNodeWithText("Fechar falha").performClick()
        compose.onNodeWithText("Tentar").performClick()
        awaitText("Status: recuperado")
    }

    private fun awaitText(text: String) {
        compose.waitUntil(5_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().size == 1 }
    }
}
