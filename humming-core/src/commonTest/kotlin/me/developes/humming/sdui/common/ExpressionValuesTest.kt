package me.developes.humming.sdui.common

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class ExpressionValuesTest : ViewModelTestSupport() {
    private val node = ServerDrivenNode("layout:text", "reactive-label")
    private fun spec(source: String) = ExpressionSpec("humming.expression", 1, source)
    private val fallback = SduiValue.Text("unavailable")

    @Test fun collectorObservesRelevantEditsAndRemovalAndStopsOnCancellation() = runTest {
        val vm = viewModel()
        val observed = mutableListOf<ExpressionValue>()
        val values = vm.expressionValues(spec("state.name ?? \"empty\""), node, "text", fallback)
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { values.toList(observed) }
        assertEquals(listOf(SduiValue.Text("empty")), observed.map { it.value })
        vm.updateState("unrelated", "ignored"); runCurrent()
        assertEquals(1, observed.size)
        vm.updateState("name", "Alice"); runCurrent()
        assertEquals(SduiValue.Text("Alice"), observed.last().value)
        vm.updateState("name", null); runCurrent()
        assertEquals(SduiValue.Text("empty"), observed.last().value)
        job.cancelAndJoin()
        vm.updateState("name", "after disposal"); runCurrent()
        assertEquals(3, observed.size)
        assertEquals(SduiValue.Text("after disposal"), values.first().value)
    }

    @Test fun dynamicRootKeysFollowSelectionChangesAndRecoverFromTypedFailures() = runTest {
        val vm = viewModel()
        vm.updateState("selected", "a"); vm.updateState("a", "first"); vm.updateState("b", "second")
        val observed = mutableListOf<ExpressionValue>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.expressionValues(spec("state[state.selected] + \"!\""), node, "text", fallback).toList(observed)
        }
        assertEquals(SduiValue.Text("first!"), observed.last().value)
        vm.updateState("selected", "b"); runCurrent()
        assertEquals(SduiValue.Text("second!"), observed.last().value)
        vm.updateValue("b", SduiValue.BooleanValue(true)); runCurrent()
        assertEquals(fallback, observed.last().value)
        assertEquals("EXPRESSION_TYPE", observed.last().diagnostic?.code)
        assertEquals("reactive-label", observed.last().diagnostic?.nodeId)
        assertTrue(observed.last().diagnostic!!.path.startsWith("$.properties[\"text\"].expression.source["))
        vm.updateState("b", "recovered"); runCurrent()
        assertEquals(ExpressionValue(SduiValue.Text("recovered!")), observed.last())
    }

    @Test fun parsingFailuresAndTemporaryActionScopesHaveExplicitLocatedFallbacks() = runTest {
        val vm = viewModel()
        val invalid = vm.expressionValues(spec("call()"), node, "text", fallback).first()
        assertEquals(fallback, invalid.value)
        assertEquals("reactive-label", invalid.diagnostic?.nodeId)
        val temporary = vm.expressionValues(spec("response.name"), node, "text", fallback).first()
        assertEquals("EXPRESSION_PROPERTY_SCOPE", temporary.diagnostic?.code)
        assertEquals(fallback, temporary.value)
    }
}
