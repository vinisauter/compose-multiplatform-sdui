package me.developes.humming.sdui.common

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class PayloadResolutionTest : ViewModelTestSupport() {
    private object EmptyContent : ComposableContent {
        @Composable override fun Content() = Unit
    }

    @Test
    fun missingComponentDiagnosticIsNotOverwrittenByProviderFallback() = runTest {
        val vm = viewModel()
        vm.addNodeProvider("local") { parseNode("""{"component":"unknown:text","id":"text"}""", documentId = "screen") }
        assertNull(vm.loadComponent("local", "screen"))
        assertEquals("MISSING_COMPONENT", vm.errorState.value?.code)
        assertEquals("text", vm.errorState.value?.diagnostic?.nodeId)
        assertEquals("screen", vm.errorState.value?.diagnostic?.documentId)
    }

    @Test
    fun providerPayloadDiagnosticRetainsLocationAndCause() = runTest {
        val vm = viewModel()
        vm.addNodeProvider("local") { parseNode("{}", documentId = "screen") }
        assertNull(vm.loadComponent("local", "screen"))
        assertEquals("INVALID_COMPONENT", vm.errorState.value?.code)
        assertEquals("screen", vm.errorState.value?.diagnostic?.documentId)
        assertEquals("$.component", vm.errorState.value?.diagnostic?.path)
    }

    @Test
    fun handlerFailureIsDistinguishedFromPayloadFailure() = runTest {
        val failure = IllegalStateException("fixture bug")
        val vm = viewModel(SDLibrary("a").addComponent("broken") { _, _ -> throw failure })
        assertNull(vm.loadComponent(ServerDrivenNode("a:broken", "broken")))
        assertEquals("HANDLER_FAILURE", vm.errorState.value?.diagnostic?.code)
        assertEquals("componentHandler", vm.errorState.value?.diagnostic?.operation)
        assertSame(failure, vm.errorState.value?.cause)
    }

    @Test
    fun programmingErrorsAreNotConvertedToPayloadSuccess() = runTest {
        val vm = viewModel(SDLibrary("a").addComponent("broken") { _, _ -> throw AssertionError("fixture bug") })
        assertFailsWith<AssertionError> { vm.loadComponent(ServerDrivenNode("a:broken")) }
        assertNull(vm.errorState.value)
    }

    @Test
    fun providerCancellationPropagates() = runTest {
        val vm = viewModel()
        vm.addNodeProvider("cancel") { throw CancellationException("fixture") }
        assertFailsWith<CancellationException> { vm.loadComponent("cancel", "screen") }
        assertNull(vm.errorState.value)
    }
}
