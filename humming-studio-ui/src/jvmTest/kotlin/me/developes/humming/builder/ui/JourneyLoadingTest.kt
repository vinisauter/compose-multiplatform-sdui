package me.developes.humming.builder.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import kotlinx.coroutines.*
import me.developes.humming.sdui.HummingSDUI
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.layout.SDLayoutMaterial3
import me.developes.humming.sdui.layout.components.Material3Theme
import me.developes.humming.sdui.navigation.*
import me.developes.humming.sdui.navigation.components.Graph
import org.junit.After
import org.junit.Rule
import org.junit.Test
import kotlin.test.*

class JourneyLoadingTest {
    @get:Rule val compose = createComposeRule()
    private val owner = object : ViewModelStoreOwner, LifecycleOwner {
        override val viewModelStore = ViewModelStore()
        override val lifecycle = LifecycleRegistry(this)
    }
    private val mounted = mutableStateOf(true)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val cache = JourneyDefinitionCache(scope)
    private lateinit var graph: Graph
    private lateinit var vm: SDUIFormViewModel
    private val graphSource = """{"component":"navigation:graph","id":"graph","properties":{"startDestination":"home"},"children":[{"component":"navigation:node","id":"home-route","properties":{"name":"home","type":"host","destiny":"home.json"}},{"component":"navigation:node","id":"detail-route","properties":{"name":"detail","type":"host","destiny":"detail.json","deepLinks":["https://journey.example/detail"]}}]}"""

    @After fun cleanup() {
        compose.runOnIdle { mounted.value = false }
        compose.waitForIdle()
        compose.runOnIdle {
            owner.lifecycle.currentState = Lifecycle.State.DESTROYED
            owner.viewModelStore.clear()
        }
        runBlocking { cache.close() }
        scope.cancel()
    }

    private fun source(label: String) = """{"component":"layout:text","id":"label","properties":{"text":"$label"}}"""

    private fun mount(loader: suspend (DefinitionKey) -> DefinitionSnapshot) {
        val definitions = JourneyDefinitions("journey", "v1", "session-a", cache, loader)
        compose.runOnIdle { owner.lifecycle.currentState = Lifecycle.State.RESUMED }
        val config = HummingSDUI.Config(type = "local", destiny = "graph", theme = Material3Theme()) {
            vm = this
            addLibrary(SDLayoutMaterial3())
            // The normal extension point exposes the mounted Graph to this host, without reflection.
            addLibrary(SDNavigation(definitions).apply {
                addComponent("graph") { node, model -> Graph(node, model, definitions).also { graph = it } }
            })
            addNodeProvider("local") { parseNode(graphSource) }
        }
        compose.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides owner, LocalLifecycleOwner provides owner) {
                if (mounted.value) HummingSDUI().Content(config, savedStateHandleFactory = { SavedStateHandle() })
            }
        }
    }

    private fun awaitText(label: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(label).fetchSemanticsNodes().size == 1 }
    }

    @Test fun failedStartupIsLocatedAndCanRetryWithoutRecreatingHost() {
        val failure = IllegalStateException("provider bug")
        var fail = true
        mount { key -> if (fail) throw failure else DefinitionSnapshot(key, source("Recovered home")) }
        compose.waitUntil(10_000) { ::vm.isInitialized && vm.errorState.value != null }
        compose.runOnIdle {
            assertEquals("PROVIDER_ERROR", vm.errorState.value?.code)
            assertEquals("home-route", vm.errorState.value?.diagnostic?.nodeId)
            assertTrue(generateSequence(vm.errorState.value?.cause) { it.cause }.any { it === failure })
            fail = false
            vm.dismissError()
            vm.runTask(block = { graph.navigateToAwaited("home", clearStack = true) })
        }
        awaitText("Recovered home")
        compose.runOnIdle { assertNull(vm.errorState.value) }
    }

    @Test fun realDeepLinkLoadsColdAndReloadPreservesOldDefinitionOnFailure() {
        var detailLoads = 0
        var failReload = false
        val failure = IllegalStateException("reload bug")
        mount { key ->
            if (key.route == "detail") {
                detailLoads++
                if (failReload) throw failure
            }
            DefinitionSnapshot(key, source(if (key.route == "home") "Home" else "Detail $detailLoads"))
        }
        awaitText("Home")
        compose.runOnIdle {
            assertEquals(0, detailLoads)
            assertFalse(graph.openDeepLink("https://journey.example/unknown"))
            assertTrue(graph.openDeepLink("https://journey.example/detail"))
        }
        awaitText("Detail 1")
        var reloaded: Job? = null
        compose.runOnIdle {
            failReload = true
            reloaded = vm.runTask(block = { graph.reloadRoute("detail") })
        }
        compose.waitUntil(10_000) { reloaded?.isCompleted == true }
        awaitText("Detail 1")
        compose.runOnIdle {
            assertTrue(generateSequence(vm.errorState.value?.cause) { it.cause }.any { it === failure })
            failReload = false
            vm.dismissError()
            reloaded = vm.runTask(block = { graph.reloadRoute("detail") })
        }
        awaitText("Detail 3")
        compose.runOnIdle { graph.navigateBack() }
        awaitText("Home")
    }

    @Test fun leavingColdRouteCancelsWaiterButHostLoadCanFinishForNextVisit() {
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        var detailLoads = 0
        mount { key ->
            if (key.route == "detail") {
                detailLoads++
                started.complete(Unit)
                release.await()
            }
            DefinitionSnapshot(key, source(if (key.route == "home") "Home" else "Shared definition"))
        }
        try {
            awaitText("Home")
            compose.runOnIdle { assertTrue(graph.openDeepLink("https://journey.example/detail")) }
            compose.waitUntil(10_000) { started.isCompleted }
            compose.runOnIdle { graph.navigateBack() }
            awaitText("Home")
            compose.waitUntil(10_000) { !vm.isLoading.value }
            // The cache owns the flight; route disposal cancels only its waiter.
            assertEquals(1, runBlocking { cache.metrics().inFlight })
            release.complete(Unit)
            compose.waitUntil(10_000) { runBlocking { cache.metrics().inFlight == 0 } }
            compose.runOnIdle {
                assertNull(vm.errorState.value)
                assertEquals(1, detailLoads)
                assertTrue(graph.openDeepLink("https://journey.example/detail"))
            }
            awaitText("Shared definition")
            compose.runOnIdle { assertEquals(1, detailLoads) }
        } finally { release.complete(Unit) }
    }
}
