package me.developes.humming.sdui.common

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BindingLifecycleTest : ViewModelTestSupport() {
    @Test fun coldBindingOnlySubscribesWhileCollected() = runTest {
        val vm = viewModel()
        val binding = vm.binding("Hello #{name}")
        assertEquals("Hello ", binding.value)
        assertEquals(0, vm.onStateUpdated.subscriptionCount.value)
        val values = mutableListOf<String>()
        val job = launch { binding.updates.collect { values += it } }
        runCurrent()
        assertEquals(1, vm.onStateUpdated.subscriptionCount.value)
        vm.updateState("name", "Ana")
        runCurrent()
        vm.updateState("name", null)
        runCurrent()
        assertEquals(listOf("Hello ", "Hello Ana", "Hello "), values)
        job.cancel()
        runCurrent()
        assertEquals(0, vm.onStateUpdated.subscriptionCount.value)
    }

    @Test fun constantBindingNeedsNoStateSubscription() = runTest {
        val vm = viewModel()
        val values = mutableListOf<String>()
        vm.binding("constant").updates.collect { values += it }
        assertEquals(listOf("constant"), values)
        assertEquals(0, vm.onStateUpdated.subscriptionCount.value)
    }

    @Test fun legacyBindingCanBeReleasedAndStopsUpdating() = runTest {
        val vm = viewModel()
        val binding = vm.getStateFlow("name", "initial")
        runCurrent()
        assertEquals(1, vm.onStateUpdated.subscriptionCount.value)
        vm.releaseBinding(binding)
        vm.releaseBinding(binding)
        runCurrent()
        vm.updateState("name", "later")
        runCurrent()
        assertEquals("initial", binding.value)
        assertEquals(0, vm.onStateUpdated.subscriptionCount.value)
    }

    @Test fun shortTaskNeverFlashesLoading() = runTest {
        val vm = viewModel()
        val gate = CompletableDeferred<Unit>()
        vm.runTask(block = { gate.await() })
        runCurrent()
        advanceTimeBy(50)
        gate.complete(Unit)
        runCurrent()
        advanceTimeBy(200)
        runCurrent()
        assertFalse(vm.isLoading.value)
    }

    @Test fun loadingWaitsForLastConcurrentTaskAndHidesImmediately() = runTest {
        val vm = viewModel()
        val first = CompletableDeferred<Unit>()
        val second = CompletableDeferred<Unit>()
        vm.runTask(block = { first.await() })
        val other = vm.runTask(block = { second.await() })
        runCurrent()
        advanceTimeBy(100)
        runCurrent()
        assertTrue(vm.isLoading.value)
        first.complete(Unit)
        runCurrent()
        assertTrue(vm.isLoading.value)
        other.cancel()
        runCurrent()
        assertFalse(vm.isLoading.value)
        assertEquals(null, vm.errorState.value)
    }

    @Test fun stateBindingReflectsRemovalWithoutRestoringInitialValue() = runTest {
        val vm = viewModel()
        val binding = vm.stateBinding("name", "initial")
        assertEquals("initial", binding.value)
        vm.updateState("name", null)
        assertEquals("", binding.value)
    }

    @Test fun clearingOwnerCancelsTasksAndAllLegacyBindingVariants() = runTest {
        val vm = SDUIFormViewModel(SavedStateHandle())
        val store = ViewModelStore().also { it.put("test", vm) }
        try {
            vm.getStateFlow("name", "initial")
            vm.getStateFlow("name") { it?.length ?: 0 }
            vm.resolveStatePlaceholdersFlow("#{name}")
            vm.resolveStatePlaceholdersFlow("#{name}") { it?.length ?: 0 }
            val gate = CompletableDeferred<Unit>()
            val task = vm.runTask(block = { gate.await() })
            runCurrent()
            advanceTimeBy(100)
            runCurrent()
            assertEquals(4, vm.onStateUpdated.subscriptionCount.value)
            assertTrue(vm.isLoading.value)
            store.clear()
            runCurrent()
            assertTrue(task.isCancelled)
            assertFalse(vm.isLoading.value)
            assertEquals(0, vm.onStateUpdated.subscriptionCount.value)
        } finally { store.clear() }
    }
}
