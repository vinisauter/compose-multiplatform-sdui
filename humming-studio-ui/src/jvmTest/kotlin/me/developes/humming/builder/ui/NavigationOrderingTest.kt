package me.developes.humming.builder.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import me.developes.humming.sdui.navigation.JourneyDefinitionCache
import me.developes.humming.sdui.navigation.JourneyDefinitions
import me.developes.humming.sdui.navigation.DefinitionCachePolicy
import me.developes.humming.sdui.navigation.DefinitionSnapshot
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
import kotlin.test.assertEquals

class NavigationOrderingTest {
    @get:Rule val compose = createComposeRule()
    private val owner = object : ViewModelStoreOwner, LifecycleOwner {
        override val viewModelStore = ViewModelStore()
        override val lifecycle = LifecycleRegistry(this)
    }
    private val mounted = mutableStateOf(true)
    private val loading = CompletableDeferred<Unit>()
    private val release = CompletableDeferred<Unit>()

    @After fun cleanup() {
        release.complete(Unit)
        compose.runOnIdle { mounted.value = false }
        compose.waitForIdle()
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
                if (mounted.value) HummingSDUI().Content(config, savedStateHandleFactory = { SavedStateHandle() })
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
        try { awaitText("Dado carregado: pronto") } catch (failure: Throwable) {
            compose.runOnIdle { println("Navigation state: ${vm.formState}; error: ${vm.errorState.value}") }
            println(compose.onRoot().printToString())
            throw failure
        }
        compose.onNodeWithText("Voltar").performClick()
        awaitText("Abrir detalhe")
    }

    private fun awaitText(text: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().size == 1 }
    }

    @Test
    fun versionedNavigationConsultsCacheAgainAfterTtlWithoutInstallingRouteProviders() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        var now = 0L
        var loads = 0
        val cache = JourneyDefinitionCache(scope, policy = DefinitionCachePolicy(ttlMillis = 10), nowMillis = { now })
        val graph = """{"component":"navigation:graph","id":"graph","properties":{"startDestination":"start"},"children":[{"component":"navigation:node","properties":{"name":"start","type":"definitions","destiny":"start.json"}},{"component":"navigation:node","properties":{"name":"detail","type":"definitions","destiny":"detail.json"}}]}"""
        val start = """{"component":"layout:button","id":"open","properties":{"onClick":[{"component":"navigation:goTo","id":"go","properties":{"destiny":"detail"}}]},"children":[{"component":"layout:text","id":"open-label","properties":{"text":"Cache: abrir"}}]}"""
        val detail = """{"component":"layout:button","id":"back","properties":{"onClick":[{"component":"navigation:goBack","id":"return"}]},"children":[{"component":"layout:text","id":"back-label","properties":{"text":"Cache: voltar"}}]}"""
        val definitions = JourneyDefinitions("example", "v1", "session", cache) { key ->
            loads++
            DefinitionSnapshot(key, if (key.resource == "start.json") start else detail)
        }
        try {
            compose.runOnIdle { owner.lifecycle.currentState = Lifecycle.State.RESUMED }
            val config = HummingSDUI.Config(type = "local", destiny = "graph", theme = Material3Theme()) {
                addLibrary(SDLayoutMaterial3())
                addLibrary(SDNavigation(definitions))
                addNodeProvider("local") { parseNode(graph) }
            }
            compose.setContent {
                CompositionLocalProvider(LocalViewModelStoreOwner provides owner, LocalLifecycleOwner provides owner) {
                    if (mounted.value) HummingSDUI().Content(config, savedStateHandleFactory = { SavedStateHandle() })
                }
            }
            awaitText("Cache: abrir")
            compose.onNodeWithText("Cache: abrir").performClick()
            awaitText("Cache: voltar")
            compose.onNodeWithText("Cache: voltar").performClick()
            awaitText("Cache: abrir")
            compose.onNodeWithText("Cache: abrir").performClick()
            awaitText("Cache: voltar")
            compose.runOnIdle { assertEquals(2, loads) }
            compose.onNodeWithText("Cache: voltar").performClick()
            awaitText("Cache: abrir")
            compose.runOnIdle { now = 11 }
            compose.onNodeWithText("Cache: abrir").performClick()
            awaitText("Cache: voltar")
            compose.runOnIdle { assertEquals(3, loads) }
            assertEquals(1, runBlocking { cache.metrics().hits })
        } finally {
            runBlocking { cache.close() }
            scope.cancel()
        }
    }
}
