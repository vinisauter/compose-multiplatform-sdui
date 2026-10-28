package me.developes.humming.builder.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.awaitCancellation
import me.developes.humming.sdui.action.SDActions
import me.developes.humming.sdui.common.SDLibrary
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.collectAsState
import me.developes.humming.sdui.common.parseNode
import me.developes.humming.sdui.layout.SDLayoutMaterial3
import org.junit.After
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BindingCompositionTest {
    @get:Rule val compose = createComposeRule()
    private val store = ViewModelStore()
    private val mounted = mutableStateOf(true)

    private fun model(vararg libraries: SDLibrary): SDUIFormViewModel =
        SDUIFormViewModel(SavedStateHandle(), libraries.toList()).also { store.put("test", it) }

    @After fun cleanup() {
        compose.runOnIdle { mounted.value = false }
        compose.waitForIdle()
        compose.runOnIdle { store.clear() }
    }

    @Test fun recompositionDoesNotAccumulateSubscriptionsAndDisposalReleasesThem() {
        lateinit var vm: SDUIFormViewModel
        compose.runOnIdle { vm = model() }
        val tick = mutableStateOf(0)
        compose.setContent {
            if (mounted.value) {
                // Deliberately rebuild the binding on each recomposition.
                val value by vm.binding("#{name}").collectAsState()
                Text("$value / ${tick.value}")
            }
        }
        repeat(8) { index ->
            compose.runOnIdle { tick.value = index + 1; vm.updateState("name", "Ana") }
            compose.waitForIdle()
            compose.runOnIdle { assertEquals(1, vm.onStateUpdated.subscriptionCount.value) }
        }
        compose.runOnIdle { mounted.value = false }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(0, vm.onStateUpdated.subscriptionCount.value) }
    }

    @Test fun actionRemainsBusyDuringBindingChangesAndCancelsWhenButtonLeavesComposition() {
        var started = false
        var cancelled = false
        lateinit var vm: SDUIFormViewModel
        compose.runOnIdle {
            vm = model(SDLayoutMaterial3(), SDLibrary("test").addAction("wait") { _, _ ->
                started = true
                try { awaitCancellation() } finally { cancelled = true }
            })
            vm.updateState("enabled", "true")
        }
        val node = parseNode("""{"component":"layout:textButton","properties":{"text":"Executar","enabled":"#{enabled}","onClick":{"component":"test:wait"}}}""")
        compose.setContent { if (mounted.value) vm.loadComponent(node)?.Content() }
        compose.onNodeWithText("Executar").performClick()
        compose.waitUntil(5_000) { started }
        compose.runOnIdle { vm.updateState("enabled", "false") }
        compose.waitForIdle()
        compose.runOnIdle { vm.updateState("enabled", "true") }
        compose.onNodeWithText("Executar").assertIsNotEnabled()
        compose.runOnIdle { mounted.value = false }
        compose.waitUntil(5_000) { cancelled }
        compose.runOnIdle { assertNull(vm.errorState.value) }
    }

    @Test fun eventsRunInitiallyWithoutFeedbackAndStopOnDisposal() {
        var creates = 0
        var changes = 0
        lateinit var vm: SDUIFormViewModel
        compose.runOnIdle {
            vm = model(SDActions()
                .registerMethod("created") { _, _ -> creates++ }
                .registerMethod("changed") { _, state ->
                    changes++
                    state.updateStateFromAction("derived", changes.toString())
                })
        }
        val tick = mutableStateOf(0)
        val node = parseNode("""{"component":"action:event","properties":{"onCreate":{"component":"action:method","properties":{"invoke":"created"}},"onStateChange":{"component":"action:method","properties":{"invoke":"changed"}}}}""")
        compose.setContent {
            if (mounted.value) {
                Text("${tick.value}")
                vm.loadComponent(node)?.Content()
            }
        }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(1, creates); assertEquals(1, changes); tick.value++ }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(1, creates); assertEquals(1, changes); vm.updateState("external", "edit") }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(2, changes); mounted.value = false }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(0, vm.onStateUpdated.subscriptionCount.value)
            vm.updateState("external", "after disposal")
        }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(2, changes) }
    }
}
