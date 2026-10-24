package me.developes.humming.sdui.common

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class StateEventTest : ViewModelTestSupport() {
    @Test fun unchangedValuesAndAbsentRemovalDoNotEmit() = runTest {
        val vm = viewModel()
        vm.updateState("missing", null)
        assertEquals(0, vm.onStateUpdated.value)
        vm.updateState("key", "value")
        vm.updateState("key", "value")
        assertEquals(1, vm.onStateUpdated.value)
    }

    @Test fun initialEmissionRunsOnceAndOwnMutationCannotReenter() = runTest {
        var calls = 0
        val vm = viewModel(SDLibrary("test").addAction("change") { _, state ->
            calls++
            state.updateStateFromAction("key", calls.toString())
        })
        val token = StateEventToken()
        val actions = listOf(ServerDrivenNode("test:change", "change"))
        vm.executeStateChangeActions(token, 0, actions)
        vm.executeStateChangeActions(token, vm.onStateUpdated.value, actions)
        assertEquals(1, calls)
        vm.updateState("external", "new event")
        vm.executeStateChangeActions(token, vm.onStateUpdated.value, actions)
        assertEquals(2, calls)
    }

    @Test fun causalChainPreventsTwoListenersFromCycling() = runTest {
        var calls = 0
        val vm = viewModel(SDLibrary("test").addAction("change") { _, state ->
            state.updateStateFromAction("key", (++calls).toString())
        })
        val first = StateEventToken()
        val second = StateEventToken()
        val actions = listOf(ServerDrivenNode("test:change", "change"))
        vm.executeStateChangeActions(first, 0, actions)
        vm.executeStateChangeActions(second, vm.onStateUpdated.value, actions)
        vm.executeStateChangeActions(first, vm.onStateUpdated.value, actions)
        vm.executeStateChangeActions(second, vm.onStateUpdated.value, actions)
        assertEquals(2, calls)
    }

    @Test fun staleEmissionDoesNotExecuteWithNewerState() = runTest {
        var calls = 0
        val vm = viewModel(SDLibrary("test").addAction("change") { _, _ -> calls++ })
        vm.updateState("key", "new")
        vm.executeStateChangeActions(StateEventToken(), 0,
            listOf(ServerDrivenNode("test:change", "change")))
        assertEquals(0, calls)
    }
}
