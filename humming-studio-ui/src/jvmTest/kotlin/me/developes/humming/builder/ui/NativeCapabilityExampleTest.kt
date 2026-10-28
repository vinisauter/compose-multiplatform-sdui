package me.developes.humming.builder.ui

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.*
import me.developes.humming.sdui.common.*
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

class NativeCapabilityExampleTest {
    @get:Rule val compose = createComposeRule()

    private fun runExample(bridge: NativeBridge, readOnStart: Boolean = false, verify: () -> Unit) {
        val host = object : LifecycleOwner { override val lifecycle = LifecycleRegistry(this) }
        var mounted by mutableStateOf(true)
        compose.runOnIdle { host.lifecycle.currentState = Lifecycle.State.RESUMED }
        compose.setContent { if (mounted) CompositionLocalProvider(LocalLifecycleOwner provides host) { NativeCapabilityExample(bridge, readOnStart) } }
        try { verify() } finally {
            compose.runOnIdle { mounted = false }
            compose.waitForIdle()
            compose.runOnIdle { host.lifecycle.currentState = Lifecycle.State.DESTROYED }
        }
    }

    private fun awaitText(text: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().size == 1 }
    }

    @Test fun previewRegistryIsUnavailableAndRecreationStartsFresh() = runExample(NativeBridge(emptyMap())) {
        awaitText("Resultado: Aguardando chamada")
        compose.onNodeWithText("Ler uptime").performClick()
        awaitText("Resultado: NATIVE_UNAVAILABLE")
        compose.onNodeWithText("Recriar formulário").performClick()
        awaitText("Resultado: Aguardando chamada")
    }

    @Test fun hostResultIsTypedAndRecreationCancelsPendingCapability() {
        var calls = 0
        val started = CompletableDeferred<Unit>()
        val cancelled = CompletableDeferred<Unit>()
        val bridge = NativeBridge(mapOf("device.uptime" to {
            if (++calls == 1) NativeResult.Success(SduiValue.NumberValue("123")) else {
                started.complete(Unit)
                try { awaitCancellation() } finally { cancelled.complete(Unit) }
            }
        }))
        runExample(bridge) {
            awaitText("Resultado: Aguardando chamada")
            compose.onNodeWithText("Ler uptime").performClick()
            awaitText("Resultado: NATIVE_SUCCESS")
            awaitText("Uptime ms: 123")
            compose.onNodeWithText("Ler uptime").performClick()
            compose.waitUntil(10_000) { started.isCompleted }
            compose.onNodeWithText("Recriar formulário").performClick()
            awaitText("Resultado: Aguardando chamada")
            compose.waitUntil(10_000) { cancelled.isCompleted }
            assertTrue(cancelled.isCompleted)
        }
    }

    @Test fun explicitHostStartupUsesTheRegisteredEventAgainAfterRecreation() {
        var calls = 0
        val bridge = NativeBridge(mapOf("device.uptime" to { NativeResult.Success(SduiValue.NumberValue((++calls).toString())) }))
        runExample(bridge, readOnStart = true) {
            awaitText("Resultado: NATIVE_SUCCESS")
            awaitText("Uptime ms: 1")
            compose.onNodeWithText("Recriar formulário").performClick()
            awaitText("Uptime ms: 2")
        }
    }
}
