package me.developes.humming.sdui.common

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class NativeBridgeTest {
    @Test fun explicitRegistrationTypedResultsVersionAndBudget() = runTest {
        var calls = 0
        val handlers = mutableMapOf<String, NativeCapabilityHandler>("device.uptime" to {
            calls++
            if (it is SduiValue.ObjectValue && it.values.isEmpty()) NativeResult.Success(SduiValue.NumberValue("123"))
            else NativeResult.Rejected("NATIVE_ARGUMENT")
        })
        val bridge = NativeBridge(handlers)
        handlers.clear()
        assertEquals(setOf("device.uptime"), bridge.capabilities)
        assertEquals(NativeResult.Success(SduiValue.NumberValue("123")), bridge.invoke(NativeRequest("device.uptime")))
        assertEquals(NativeResult.Rejected("NATIVE_ARGUMENT"), bridge.invoke(NativeRequest("device.uptime", SduiValue.Null)))
        assertEquals(NativeResult.Unavailable, bridge.invoke(NativeRequest("device.unknown")))
        assertEquals(NativeResult.Rejected("NATIVE_VERSION"), bridge.invoke(NativeRequest("device.uptime", version = 2)))
        assertEquals(NativeResult.Rejected("NATIVE_INPUT_LIMIT"), bridge.invoke(NativeRequest("device.uptime", SduiValue.Text("a".repeat(65_537)))))
        assertEquals(2, calls)
        assertEquals(NativeResult.Unavailable, NativeBridge(emptyMap()).invoke(NativeRequest("device.uptime")))
    }

    @Test fun cancellationAndProgrammingDefectsPropagateWithoutBecomingRejection() = runTest {
        val entered = CompletableDeferred<Unit>()
        val cancelled = CompletableDeferred<Unit>()
        val bridge = NativeBridge(mapOf("device.wait" to {
            entered.complete(Unit)
            try { awaitCancellation() } finally { cancelled.complete(Unit) }
        }, "device.bug" to { error("handler defect") },
            "device.invalid" to { NativeResult.Success(SduiValue.Missing) }))
        val job = launch { bridge.invoke(NativeRequest("device.wait")) }
        entered.await()
        job.cancelAndJoin()
        assertTrue(cancelled.isCompleted)
        assertFailsWith<IllegalStateException> { bridge.invoke(NativeRequest("device.bug")) }
        assertFailsWith<IllegalStateException> { bridge.invoke(NativeRequest("device.invalid")) }
    }
}
