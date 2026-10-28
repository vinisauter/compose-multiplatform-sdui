package me.developes.humming.builder.ui

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import kotlinx.coroutines.*
import me.developes.humming.sdui.HummingSDUI
import me.developes.humming.sdui.action.SDActions
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.layout.SDLayoutMaterial3
import me.developes.humming.sdui.layout.components.Material3Theme
import me.developes.humming.sdui.navigation.*
import me.developes.humming.sdui.navigation.components.Graph
import org.junit.Rule
import org.junit.Test
import kotlin.test.*

class JourneySessionTest {
    @get:Rule val compose = createComposeRule()
    private class Host : ViewModelStoreOwner, LifecycleOwner {
        override val viewModelStore = ViewModelStore()
        override val lifecycle = LifecycleRegistry(this)
    }

    @Test fun hostSessionReplacementDiscardsOldEntriesTasksInputsAndDefinitions() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        val cache = JourneyDefinitionCache(scope)
        val hosts = listOf(Host(), Host())
        var host by mutableStateOf(hosts.first())
        var mounted by mutableStateOf(true)
        val entries = mutableMapOf<String, SDUIFormViewModel>()
        val graphs = mutableMapOf<String, Graph>()
        val requests = mutableListOf<DefinitionKey>()
        val started = CompletableDeferred<Unit>()
        val cancelled = CompletableDeferred<Unit>()
        val graphSource = """{"component":"navigation:graph","id":"graph","properties":{"startDestination":"form"},"children":[{"component":"navigation:node","id":"form-route","properties":{"name":"form","type":"host","destiny":"form.json","deepLinks":["https://journey.example/form"]}}]}"""
        val form = """{"component":"layout:column","id":"form","children":[{"component":"action:event","id":"create","properties":{"onCreate":{"component":"action:method","id":"capture","properties":{"invoke":"capture"}}}},{"component":"layout:textField","id":"input","properties":{"text":"initial","onChangeUpdateState":"field"}},{"component":"layout:text","id":"value","properties":{"text":"Value: #{field}"}},{"component":"layout:button","id":"hold","properties":{"onClick":{"component":"action:method","id":"wait","properties":{"invoke":"hold"}}},"children":[{"component":"layout:text","id":"hold-label","properties":{"text":"Hold"}}]}]}"""
        fun config(session: String): HummingSDUI.Config {
            val definitions = JourneyDefinitions("journey", "v1", session, cache) {
                requests += it
                DefinitionSnapshot(it, form)
            }
            return HummingSDUI.Config("local", "graph", theme = Material3Theme(),
                screenStateMode = ScreenStateMode.ISOLATED_ENTRIES,
                entryInputs = { _, _ -> ScreenInputs(server = SduiValue.ObjectValue(mapOf("session" to SduiValue.Text(session)))) },
            ) {
                addLibrary(SDActions().registerMethod("capture") { _, owner -> entries[session] = owner }
                    .registerMethod("hold") { _, owner -> owner.runTask(block = {
                        started.complete(Unit)
                        try { awaitCancellation() } finally { cancelled.complete(Unit) }
                    }) })
                addLibrary(SDLayoutMaterial3())
                val navigation = SDNavigation(definitions)
                val handler = checkNotNull(navigation.getComponent("graph"))
                navigation.addComponent("graph") { node, model -> handler(node, model).also { graphs[session] = it as Graph } }
                addLibrary(navigation)
                addNodeProvider("local") { parseNode(graphSource) }
            }
        }
        var config by mutableStateOf(config("a"))
        compose.runOnIdle { host.lifecycle.currentState = Lifecycle.State.RESUMED }
        compose.setContent {
            if (mounted) CompositionLocalProvider(LocalViewModelStoreOwner provides host, LocalLifecycleOwner provides host) {
                HummingSDUI().Content(config, savedStateHandleFactory = { SavedStateHandle() })
            }
        }
        fun awaitText(text: String) {
            compose.waitUntil(10_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().size == 1 }
        }
        try {
            awaitText("Value: initial")
            compose.waitUntil(10_000) { entries.containsKey("a") }
            compose.onNode(hasSetTextAction()).performTextReplacement("private-a")
            awaitText("Value: private-a")
            compose.onNodeWithText("Hold").performClick()
            compose.waitUntil(10_000) { started.isCompleted }
            // This is the host's logout boundary: unmount, clear old store, invalidate, then mount a new identity.
            compose.runOnIdle { mounted = false }
            compose.waitForIdle()
            compose.runOnIdle {
                hosts.first().lifecycle.currentState = Lifecycle.State.DESTROYED
                hosts.first().viewModelStore.clear()
            }
            runBlocking { cache.invalidateSession("a") }
            compose.waitUntil(10_000) { cancelled.isCompleted }
            compose.runOnIdle {
                host = hosts.last()
                host.lifecycle.currentState = Lifecycle.State.RESUMED
                config = config("b")
                mounted = true
            }
            awaitText("Value: initial")
            compose.waitUntil(10_000) { entries.containsKey("b") }
            compose.runOnIdle {
                assertNotSame(entries["a"], entries["b"])
                assertTrue(entries.getValue("a").stateValues.isEmpty())
                assertTrue(entries.getValue("a").screenInputs.server.values.isEmpty())
                assertEquals(SduiValue.Text("b"), entries.getValue("b").screenInputs.server.values["session"])
                assertFalse(graphs.getValue("a").openDeepLink("https://journey.example/form"))
                assertEquals(listOf("a", "b"), requests.map { it.session })
            }
            assertEquals(1, runBlocking { cache.metrics().entries })
        } finally {
            compose.runOnIdle { mounted = false }
            compose.waitForIdle()
            compose.runOnIdle { hosts.forEach { it.lifecycle.currentState = Lifecycle.State.DESTROYED; it.viewModelStore.clear() } }
            runBlocking { cache.close() }
            scope.cancel()
        }
    }
}
