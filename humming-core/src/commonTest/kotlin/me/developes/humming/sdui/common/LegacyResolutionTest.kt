package me.developes.humming.sdui.common

import androidx.compose.runtime.Composable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull
import kotlin.test.assertSame

@OptIn(ExperimentalCoroutinesApi::class)
class LegacyResolutionTest : ViewModelTestSupport() {
    private object EmptyContent : ComposableContent {
        @Composable
        override fun Content() = Unit
    }

    @Test
    fun componentResolutionUsesNamespaceAndExactName() = runTest {
        val resolved = mutableListOf<String>()
        val first = SDLibrary("first").addComponent("textField") { node, _ ->
            resolved += "first:${node.id}"
            EmptyContent
        }
        val second = SDLibrary("second").addComponent("textField") { node, _ ->
            resolved += "second:${node.id}"
            EmptyContent
        }
        val vm = viewModel(first, second)
        assertSame(EmptyContent, vm.loadComponent(ServerDrivenNode("second:textField", "field")))
        assertEquals(listOf("second:field"), resolved)
        assertNull(vm.loadComponent(ServerDrivenNode("second:textfield")))
        assertEquals("MISSING_COMPONENT", vm.errorState.value?.code)
    }

    @Test
    fun unknownNamespaceAndMissingHandlerProduceErrorState() = runTest {
        val vm = viewModel(SDLibrary("known"))
        for (component in listOf("unknown:text", "known:missing")) {
            assertNull(vm.loadComponent(ServerDrivenNode(component)))
            assertEquals("MISSING_COMPONENT", vm.errorState.value?.code)
            vm.dismissError()
            assertNull(vm.errorState.value)
        }
    }

    @Test
    fun componentHandlerFailureIsReported() = runTest {
        val library = SDLibrary("test").addComponent("broken") { _, _ ->
            error("fixture handler failure")
        }
        val vm = viewModel(library)
        assertNull(vm.loadComponent(ServerDrivenNode("test:broken")))
        assertEquals("COMPONENT_ERROR", vm.errorState.value?.code)
    }

    @Test
    fun providerReceivesDestinationAndResolvesReturnedNode() = runTest {
        val requested = mutableListOf<String>()
        val library = SDLibrary("test").addComponent("text") { node, _ ->
            assertEquals("provided", node.id)
            EmptyContent
        }
        val vm = viewModel(library)
        vm.addNodeProvider("local") { destiny ->
            requested += destiny
            delay(25)
            ServerDrivenNode("test:text", "provided")
        }
        assertSame(EmptyContent, vm.loadComponent("local", "fixture.json"))
        assertEquals(listOf("fixture.json"), requested)
        assertNull(vm.errorState.value)
    }

    @Test
    fun missingProviderIsReported() = runTest {
        val vm = viewModel()
        assertNull(vm.loadComponent("unknown", "fixture.json"))
        assertEquals("MISSING_PROVIDER", vm.errorState.value?.code)
    }

    @Test
    fun providerFailureIsReported() = runTest {
        val vm = viewModel()
        vm.addNodeProvider("broken") { error("fixture provider failure") }
        assertNull(vm.loadComponent("broken", "fixture.json"))
        assertEquals("PROVIDER_ERROR", vm.errorState.value?.code)
    }

    @Test
    fun directActionHandlersAreAwaitedInOrder() = runTest {
        val events = mutableListOf<String>()
        val library = SDLibrary("test")
            .addAction("first") { _, _ ->
                events += "first:start"
                delay(25)
                events += "first:end"
            }
            .addAction("second") { _, _ -> events += "second" }
        val vm = viewModel(library)
        vm.invokeActions(
            arrayListOf(ServerDrivenNode("test:first"), ServerDrivenNode("test:second")),
            beforeInvoke = { events += "before" },
            afterInvoke = { events += "after" },
        )
        runCurrent()
        assertEquals(listOf("before", "first:start"), events)
        advanceUntilIdle()
        assertEquals(listOf("before", "first:start", "first:end", "second", "after"), events)
    }

    @Test
    fun unknownActionNamespaceIsReported() = runTest {
        val vm = viewModel()
        vm.invokeActions(arrayListOf(ServerDrivenNode("unknown:action")))
        advanceUntilIdle()
        assertEquals("MISSING_ACTION", vm.errorState.value?.code)
    }

    @Test
    fun missingNamespaceIsReportedWithNodeAndPath() = runTest {
        val vm = viewModel()
        assertNull(vm.loadComponent(ServerDrivenNode("withoutSeparator", "invalid")))
        assertEquals("INVALID_IDENTIFIER", vm.errorState.value?.code)
        assertEquals("invalid", vm.errorState.value?.diagnostic?.nodeId)
        assertEquals("$.component", vm.errorState.value?.diagnostic?.path)
    }

    @Test
    fun extraIdentifierSegmentIsRejected() = runTest {
        val vm = viewModel(SDLibrary("test").addComponent("text") { _, _ -> EmptyContent })
        assertNull(vm.loadComponent(ServerDrivenNode("test:text:extra")))
        assertEquals("INVALID_IDENTIFIER", vm.errorState.value?.code)
    }
}
