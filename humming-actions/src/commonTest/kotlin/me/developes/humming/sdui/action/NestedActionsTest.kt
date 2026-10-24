package me.developes.humming.sdui.action

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.developes.humming.sdui.common.ActionFailurePolicy
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.common.parseNode
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class NestedActionsTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()

    @BeforeTest fun setup() { Dispatchers.setMain(dispatcher) }
    @AfterTest fun cleanup() {
        try { store.clear(); dispatcher.scheduler.runCurrent() } finally { Dispatchers.resetMain() }
    }

    private fun method(value: String) = ServerDrivenNode("action:method", properties = mutableMapOf("invoke" to "record", "value" to value))

    private fun vm(events: MutableList<String>) = SDUIFormViewModel(SavedStateHandle(), listOf(
        SDActions().registerMethod("record") { node, _ ->
            val value = node.property("value")!!
            delay(25)
            if (value == "broken") error("fixture")
            events += value
        },
    )).also { store.put("test", it) }

    @Test fun unregisteredMethodReportsInvokeFieldAndDoesNotExecuteCode() = runTest {
        val events = mutableListOf<String>()
        val vm = vm(events)
        val node = parseNode("""{"component":"action:method","id":"method","properties":{"invoke":"unregistered"}}""", documentId = "methods")
        val result = vm.executeActions(listOf(node))
        assertEquals(emptyList(), events)
        assertEquals("MISSING_METHOD", result.errors.single().diagnostic?.code)
        assertEquals("$.properties[\"invoke\"]", result.errors.single().diagnostic?.path)
        assertEquals("methods", result.errors.single().diagnostic?.documentId)
    }

    @Test
    fun ifStateWaitsForThenActionsBeforeContinuingOuterSequence() = runTest {
        val events = mutableListOf<String>()
        val vm = vm(events)
        vm.updateState("ready", "true")
        val condition = parseNode("""{"component":"action:ifState","properties":{"state":"ready","equals":"true","then":[{"component":"action:method","properties":{"invoke":"record","value":"B"}},{"component":"action:method","properties":{"invoke":"record","value":"C"}}]}}""")
        val result = vm.executeActions(listOf(method("A"), condition, method("D")))
        assertEquals(listOf("A", "B", "C", "D"), events)
        assertEquals(5, result.attempted)
    }

    @Test
    fun thenAwaitsInvokeArray() = runTest {
        val events = mutableListOf<String>()
        val vm = vm(events)
        vm.updateState("ready", "true")
        val condition = parseNode("""{"component":"action:then","properties":{"condition":"ready","invoke":{"component":"action:method","properties":{"invoke":"record","value":"B"}}}}""")
        vm.executeActions(listOf(condition, method("C")))
        assertEquals(listOf("B", "C"), events)
    }

    @Test
    fun nestedStopPolicySkipsBothRemainingBranchAndOuterActions() = runTest {
        val events = mutableListOf<String>()
        val vm = vm(events)
        vm.updateState("ready", "false")
        val condition = parseNode("""{"component":"action:ifState","properties":{"state":"ready","equals":"true","else":[{"component":"action:method","properties":{"invoke":"record","value":"broken"}},{"component":"action:method","properties":{"invoke":"record","value":"unexpected"}}]}}""")
        val result = vm.executeActions(listOf(condition, method("unexpected")), ActionFailurePolicy.STOP_ON_ERROR)
        assertEquals(emptyList(), events)
        assertEquals(1, result.errors.size)
    }
}
