package me.developes.humming.sdui.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ActionExecutionTest : ViewModelTestSupport() {
    private fun action(name: String) = ServerDrivenNode("test:$name", name)

    @Test
    fun suspendedSequenceWaitsForNestedHandlers() = runTest {
        val events = mutableListOf<String>()
        val library = SDLibrary("test")
            .addAction("a") { _, _ -> events += "A" }
            .addAction("nested") { _, vm -> vm.executeActions(listOf(action("b"), action("c"))) }
            .addAction("b") { _, _ -> delay(25); events += "B" }
            .addAction("c") { _, _ -> events += "C" }
            .addAction("d") { _, _ -> events += "D" }
        val result = viewModel(library).executeActions(listOf(action("a"), action("nested"), action("d")))
        assertEquals(listOf("A", "B", "C", "D"), events)
        assertEquals(5, result.attempted)
        assertTrue(result.isSuccess)
    }

    @Test
    fun continueIsLegacyDefaultAndCollectsFailures() = runTest {
        val events = mutableListOf<String>()
        val vm = viewModel(SDLibrary("test")
            .addAction("broken") { _, _ -> error("fixture") }
            .addAction("next") { _, _ -> events += "next" })
        val result = vm.executeActions(listOf(action("broken"), action("next")))
        assertEquals(listOf("next"), events)
        assertEquals(2, result.attempted)
        assertEquals("HANDLER_FAILURE", result.errors.single().diagnostic?.code)
        assertFalse(result.isSuccess)
    }

    @Test
    fun stopPolicyStopsOuterAndNestedSequenceWithoutDuplicateErrors() = runTest {
        val events = mutableListOf<String>()
        val vm = viewModel(SDLibrary("test")
            .addAction("nested") { _, vm -> vm.executeActions(listOf(action("broken"), action("next"))) }
            .addAction("broken") { _, _ -> error("fixture") }
            .addAction("next") { _, _ -> events += "unexpected" })
        val result = vm.executeActions(listOf(action("nested"), action("next")), ActionFailurePolicy.STOP_ON_ERROR)
        assertTrue(events.isEmpty())
        assertEquals(2, result.attempted)
        assertEquals(1, result.errors.size)
    }

    @Test
    fun missingAndMalformedActionsProduceStructuredResults() = runTest {
        val vm = viewModel()
        val result = vm.executeActions(listOf(ServerDrivenNode("invalid", "bad"), action("missing")))
        assertEquals(listOf("INVALID_IDENTIFIER", "MISSING_ACTION"), result.errors.map { it.code })
        assertEquals("bad", result.errors[0].diagnostic?.nodeId)
    }

    @Test
    fun actionBudgetStopsEvenContinuePolicy() = runTest {
        var calls = 0
        val vm = viewModel(SDLibrary("test").addAction("run") { _, _ -> calls++ })
        val result = vm.executeActions(List(3) { action("run") }, limits = ActionExecutionLimits(maxActions = 2))
        assertEquals(2, calls)
        assertEquals(2, result.attempted)
        assertEquals("EXECUTION_ACTION_LIMIT", result.errors.single().code)
    }

    @Test
    fun recursionBudgetStopsNestedCalls() = runTest {
        val vm = viewModel(SDLibrary("test").addAction("recursive") { node, vm -> vm.executeActions(listOf(node)) })
        val result = vm.executeActions(listOf(action("recursive")), limits = ActionExecutionLimits(maxDepth = 3))
        assertEquals(3, result.attempted)
        assertEquals("EXECUTION_DEPTH_LIMIT", result.errors.single().code)
    }

    @Test
    fun concurrentExecutionsKeepIndependentPoliciesAndResults() = runTest {
        val events = mutableListOf<String>()
        val vm = viewModel(SDLibrary("test")
            .addAction("broken") { _, _ -> delay(25); error("fixture") }
            .addAction("continue") { _, _ -> events += "continue" }
            .addAction("stop") { _, _ -> events += "stop" })
        val first = async { vm.executeActions(listOf(action("broken"), action("continue"))) }
        val second = async { vm.executeActions(listOf(action("broken"), action("stop")), ActionFailurePolicy.STOP_ON_ERROR) }
        assertEquals(2, first.await().attempted)
        assertEquals(1, second.await().attempted)
        assertEquals(listOf("continue"), events)
    }

    @Test
    fun cancellationPropagatesAndReleasesLoading() = runTest {
        val vm = viewModel(SDLibrary("test").addAction("wait") { _, _ -> delay(10_000) })
        val job = launch { vm.executeActions(listOf(action("wait"))) }
        runCurrent()
        advanceTimeBy(101)
        runCurrent()
        assertTrue(vm.isLoading.value)
        job.cancelAndJoin()
        advanceUntilIdle()
        assertTrue(job.isCancelled)
        assertFalse(vm.isLoading.value)
        assertNull(vm.errorState.value)
    }

    @Test
    fun uiCancellationRunsSuspendingCleanupExactlyOnce() = runTest {
        val events = mutableListOf<String>()
        val vm = viewModel(SDLibrary("test").addAction("wait") { _, _ -> delay(10_000) })
        val job = vm.invokeActions(arrayListOf(action("wait")), afterInvoke = { delay(25); events += "after" })
        runCurrent()
        job.cancelAndJoin()
        advanceUntilIdle()
        assertEquals(listOf("after"), events)
        assertFalse(vm.isLoading.value)
        assertNull(vm.errorState.value)
    }

    @Test
    fun beforeFailureStillRunsCleanupAndFinalizesLoading() = runTest {
        val events = mutableListOf<String>()
        val failure = IllegalStateException("before")
        val vm = viewModel(SDLibrary("test").addAction("run") { _, _ -> events += "unexpected" })
        vm.invokeActions(arrayListOf(action("run")), beforeInvoke = { throw failure }, afterInvoke = { events += "after" }).join()
        advanceUntilIdle()
        assertEquals(listOf("after"), events)
        assertSame(failure, vm.errorState.value?.cause)
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun cleanupFailurePreservesPrimaryCallbackFailure() = runTest {
        val primary = IllegalStateException("before")
        val cleanup = IllegalArgumentException("after")
        val vm = viewModel()
        vm.invokeActions(arrayListOf(), beforeInvoke = { throw primary }, afterInvoke = { throw cleanup }).join()
        advanceUntilIdle()
        assertSame(primary, vm.errorState.value?.cause)
        assertTrue(primary.suppressedExceptions.any { it === cleanup || it.cause === cleanup })
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun cleanupFailureWithoutPrimaryIsReported() = runTest {
        val vm = viewModel()
        vm.invokeActions(arrayListOf(), afterInvoke = { error("cleanup") }).join()
        advanceUntilIdle()
        assertEquals("afterInvoke", vm.errorState.value?.diagnostic?.operation)
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun errorMapperFailureDoesNotLeakLoadingOrOriginalCause() = runTest {
        val primary = IllegalStateException("task")
        val mapping = IllegalArgumentException("mapper")
        val vm = viewModel()
        vm.runTask(block = { throw primary }, onError = { throw mapping }).join()
        advanceUntilIdle()
        assertSame(primary, vm.errorState.value?.cause)
        assertTrue(primary.suppressedExceptions.contains(mapping))
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun taskCancellationIsNotReportedAsFailure() = runTest {
        val vm = viewModel()
        val job = vm.runTask(block = { delay(10_000) })
        runCurrent()
        job.cancelAndJoin()
        advanceUntilIdle()
        assertNull(vm.errorState.value)
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun cleanupCancellationIsNotSwallowedAfterReportedActionFailure() = runTest {
        val vm = viewModel(SDLibrary("test").addAction("broken") { _, _ -> error("fixture") })
        val job = vm.invokeActions(arrayListOf(action("broken")), afterInvoke = { throw CancellationException("cleanup") })
        job.join()
        advanceUntilIdle()
        assertTrue(job.isCancelled)
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun customErrorMappingRetainsOriginalCause() = runTest {
        val primary = IllegalStateException("task")
        val vm = viewModel()
        vm.runTask(block = { throw primary }, onError = { ErrorData("CUSTOM", "fixture") }).join()
        advanceUntilIdle()
        assertEquals("CUSTOM", vm.errorState.value?.code)
        assertSame(primary, vm.errorState.value?.cause)
    }

    @Test
    fun errorMapperCanCancelTaskWithoutLeakingLoading() = runTest {
        val vm = viewModel()
        val job = vm.runTask(block = { error("fixture") }, onError = { throw CancellationException("mapper") })
        job.join()
        advanceUntilIdle()
        assertTrue(job.isCancelled)
        assertNull(vm.errorState.value)
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun programmingErrorsEscapeTheSuspendedExecutor() = runTest {
        val vm = viewModel(SDLibrary("test").addAction("broken") { _, _ -> throw AssertionError("fixture") })
        assertFailsWith<AssertionError> { vm.executeActions(listOf(action("broken"))) }
        advanceUntilIdle()
        assertNull(vm.errorState.value)
        assertFalse(vm.isLoading.value)
    }
}
