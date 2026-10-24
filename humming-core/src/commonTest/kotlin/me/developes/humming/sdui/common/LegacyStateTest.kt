package me.developes.humming.sdui.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LegacyStateTest : ViewModelTestSupport() {
    @Test
    fun initialValueIsUsedOnlyWhenKeyIsAbsent() = runTest {
        val vm = viewModel()
        assertEquals("initial", vm.getState("name", "initial"))
        assertEquals("initial", vm.getState("name", "replacement"))
        vm.updateState("name", "edited")
        assertEquals("edited", vm.getState("name"))
    }

    @Test
    fun nullUpdateRemovesKeyWhileEmptyStringRemainsPresent() = runTest {
        val vm = viewModel()
        vm.updateState("name", "")
        assertTrue(vm.formState.containsKey("name"))
        vm.updateState("name", null)
        assertFalse(vm.formState.containsKey("name"))
    }

    @Test
    fun multipleAndRepeatedPlaceholdersResolveFromCurrentState() = runTest {
        val vm = viewModel()
        vm.updateState("first", "Ana")
        vm.updateState("last", "Silva")
        assertEquals("Ana Silva / Ana", vm.resolveStatePlaceholders("#{first} #{last} / #{first}"))
        assertEquals("plain", vm.resolveStatePlaceholders("plain"))
        assertEquals("", vm.resolveStatePlaceholders(null))
    }

    @Test
    fun placeholderFlowReflectsUpdatesAndRemoval() = runTest {
        val vm = viewModel()
        vm.updateState("name", "initial")
        val resolved = vm.resolveStatePlaceholdersFlow("Nome: #{name}")
        runCurrent()
        assertEquals("Nome: initial", resolved.value)
        vm.updateState("name", "edited")
        runCurrent()
        assertEquals("Nome: edited", resolved.value)
        vm.updateState("name", null)
        runCurrent()
        assertEquals("Nome: ", resolved.value)
    }

    @Test
    fun stateFlowReflectsEdits() = runTest {
        val vm = viewModel()
        val state = vm.getStateFlow("name", "initial")
        runCurrent()
        vm.updateState("name", "edited")
        runCurrent()
        assertEquals("edited", state.value)
    }

    @Test
    fun separateViewModelsDoNotShareState() = runTest {
        val first = viewModel()
        val second = viewModel()
        first.updateState("name", "first")
        second.updateState("name", "second")
        assertEquals("first", first.getState("name"))
        assertEquals("second", second.getState("name"))
    }

    @Test
    fun observedLegacyMissingBindingCreatesEmptyStateEntry() = runTest {
        val vm = viewModel()
        assertEquals("[]", vm.resolveStatePlaceholders("[#{missing}]"))
        assertEquals("", vm.formState["missing"])
    }

    @Test
    fun legacyBindingsDoNotRecursivelyEvaluateStoredPlaceholders() = runTest {
        val vm = viewModel()
        vm.updateState("inner", "value")
        vm.updateState("outer", "#{inner}")
        assertEquals("#{inner}", vm.resolveStatePlaceholders("#{outer}"))
    }
}
