package sample.app

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.CompletableDeferred
import me.developes.humming.sdui.HummingSDUI
import me.developes.humming.sdui.action.SDActions
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.parseNode
import me.developes.humming.sdui.layout.SDLayoutMaterial3
import me.developes.humming.sdui.layout.components.Material3Theme
import me.developes.humming.sdui.navigation.SDNavigation
import org.junit.After
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertNull

class NavigationOrderingTest {
    @get:Rule val compose = createComposeRule()
    private val owner = object : ViewModelStoreOwner, LifecycleOwner {
        override val viewModelStore = ViewModelStore()
        override val lifecycle = LifecycleRegistry(this)
    }
    private val loading = CompletableDeferred<Unit>()
    private val release = CompletableDeferred<Unit>()

    @After fun cleanup() {
        release.complete(Unit)
        compose.runOnIdle {
            owner.lifecycle.currentState = Lifecycle.State.DESTROYED
            owner.viewModelStore.clear()
        }
    }

    @Test
    fun navigationWaitsForProviderBeforeNextActionAndCanReturn() {
        compose.runOnIdle { owner.lifecycle.currentState = Lifecycle.State.RESUMED }
        lateinit var vm: SDUIFormViewModel
        val graph = """{"component":"navigation:graph","id":"graph","properties":{"startDestination":"start"},"children":[{"component":"navigation:node","properties":{"name":"start","type":"local","destiny":"start"}},{"component":"navigation:node","properties":{"name":"detail","type":"local","destiny":"detail"}}]}"""
        val start = """{"component":"layout:button","id":"open","properties":{"onClick":[{"component":"navigation:goTo","properties":{"destiny":"detail"}},{"component":"action:update","properties":{"state":"after","value":"pronto"}}]},"children":[{"component":"layout:text","properties":{"text":"Abrir detalhe"}}]}"""
        val detail = """{"component":"layout:column","id":"detail","children":[{"component":"layout:text","properties":{"text":"Dado carregado: #{after}"}},{"component":"layout:button","properties":{"onClick":{"component":"navigation:goBack"}},"children":[{"component":"layout:text","properties":{"text":"Voltar"}}]}]}"""
        val config = HummingSDUI.Config(type = "local", destiny = "graph", theme = Material3Theme()) {
            vm = this
            addLibrary(SDActions())
            addLibrary(SDLayoutMaterial3())
            addLibrary(SDNavigation())
            addNodeProvider("local") { destiny ->
                val source = when (destiny) {
                    "graph" -> graph
                    "start" -> start
                    "detail" -> { loading.complete(Unit); release.await(); detail }
                    else -> error("Unknown fixture")
                }
                parseNode(source, documentId = destiny)
            }
        }
        compose.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides owner, LocalLifecycleOwner provides owner) {
                HummingSDUI().Content(config, savedStateHandleFactory = { SavedStateHandle() })
            }
        }
        awaitText("Abrir detalhe")
        compose.onNodeWithText("Abrir detalhe").performClick()
        compose.waitUntil(10_000) { loading.isCompleted }
        compose.runOnIdle {
            assertNull(vm.formState["after"])
            assertNull(vm.errorState.value)
        }
        release.complete(Unit)
        awaitText("Dado carregado: pronto")
        compose.onNodeWithText("Voltar").performClick()
        awaitText("Abrir detalhe")
    }

    private fun awaitText(text: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().size == 1 }
    }
}
