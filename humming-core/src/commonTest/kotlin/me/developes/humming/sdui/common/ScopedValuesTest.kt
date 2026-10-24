package me.developes.humming.sdui.common

import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class ScopedValuesTest : ViewModelTestSupport() {
    private fun action(name: String, id: String = name) = ServerDrivenNode("test:$name",id)

    @Test fun typedStateIsTheAuthorityAndLegacyViewRemainsLive() = runTest {
        val vm=viewModel()
        val legacy=vm.formState
        assertEquals(SduiValue.Missing,vm.stateValue("id"))
        vm.updateState("id","0007")
        assertEquals(SduiValue.Text("0007"),vm.stateValue("id"))
        vm.updateValue("id",SduiValue.NumberValue("1.2300"))
        assertEquals("1.2300",legacy["id"])
        vm.updateValue("id",SduiValue.Null)
        assertEquals("null",vm.getState("id"))
        assertEquals(SduiValue.Null,vm.stateValue("id"))
        vm.updateState("id",null)
        assertEquals(SduiValue.Missing,vm.stateValue("id"))
        assertFalse(legacy.containsKey("id"))
        assertEquals("",vm.resolveStatePlaceholders("#{missing}"))
        assertEquals(SduiValue.Text(""),vm.stateValue("missing"))
    }

    @Test fun concurrentRootExecutionsDoNotExchangeResponsesAndNestedSequencesInheritThem() = runTest {
        val observed=mutableMapOf<String,SduiValue>()
        val retained=mutableListOf<ActionValues>()
        val vm=viewModel(SDLibrary("test")
            .addAction("request") { node,owner ->
                val values=owner.actionValues(); retained+=values
                values.response=SduiValue.Text(node.id)
                delay(if (node.id == "slow") 50 else 5)
                owner.executeActions(listOf(action("read",node.id)))
            }.addAction("read") { node,owner -> observed[node.id]=owner.actionValues().response })
        val slow=async { vm.executeActions(listOf(action("request","slow"))) }
        val fast=async { vm.executeActions(listOf(action("request","fast"))) }
        assertTrue(slow.await().isSuccess); assertTrue(fast.await().isSuccess)
        assertEquals(mapOf<String,SduiValue>("slow" to SduiValue.Text("slow"),"fast" to SduiValue.Text("fast")),observed)
        assertNotSame(retained[0],retained[1])
        assertTrue(retained.all { it.response === SduiValue.Missing && it.error === SduiValue.Missing })
        assertFailsWith<IllegalStateException> { vm.actionValues() }
    }

    @Test fun failuresAreLocalToExecutionAndCancellationClearsRetainedSlots() = runTest {
        var failed: SduiValue=SduiValue.Missing
        lateinit var retained: ActionValues
        val vm=viewModel(SDLibrary("test")
            .addAction("broken") { _,_ -> error("fixture") }
            .addAction("inspect") { _,owner -> failed=owner.actionValues().error }
            .addAction("wait") { _,owner -> retained=owner.actionValues(); retained.response=SduiValue.Text("private"); delay(10000) })
        vm.executeActions(listOf(action("broken"),action("inspect")))
        assertEquals(SduiValue.Text("ACTION_ERROR"),(failed as SduiValue.ObjectValue).values["code"])
        val job=launch { vm.executeActions(listOf(action("wait"))) }
        runCurrent()
        assertEquals(SduiValue.Missing,retained.error)
        job.cancelAndJoin()
        assertEquals(SduiValue.Missing,retained.response)
        assertEquals(SduiValue.Missing,retained.error)
    }

    @Test fun callingAnotherOwnerDoesNotInheritItsCallersSlotsOrPolicy() = runTest {
        val inner=viewModel(SDLibrary("test").addAction("inner") { _,owner ->
            assertEquals(SduiValue.Missing,owner.actionValues().response)
            owner.actionValues().response=SduiValue.Text("inner")
        })
        val outer=viewModel(SDLibrary("test").addAction("outer") { _,owner ->
            owner.actionValues().response=SduiValue.Text("outer")
            val result=inner.executeActions(listOf(action("inner")),ActionFailurePolicy.STOP_ON_ERROR)
            assertTrue(result.isSuccess)
            assertEquals(SduiValue.Text("outer"),owner.actionValues().response)
        })
        assertTrue(outer.executeActions(listOf(action("outer"))).isSuccess)
    }

    @Test fun newEntriesShareCapabilitiesButNotStateAndClearingTheirStoreReleasesValues() = runTest {
        val root=viewModel(SDLibrary("test").addAction("set") { _,owner -> owner.updateStateFromAction("field","updated") })
        root.updateState("field","root")
        root.addNodeProvider("local") { ServerDrivenNode("test:screen","node") }
        val first=root.createScreenEntry("first",ScreenInputs(nav=SduiValue.ObjectValue(mapOf("id" to SduiValue.Text("001")))))
        val second=root.createScreenEntry("second")
        val store=ViewModelStore().apply { put("first",first); put("second",second) }
        try {
            first.executeActions(listOf(action("set")))
            assertEquals("updated",first.formState["field"])
            assertNull(second.formState["field"])
            assertEquals("root",root.formState["field"])
            assertNotNull(second.loadNodeTypeProvider("local"))
            assertEquals(SduiValue.Text("001"),first.screenInputs.nav.values["id"])
        } finally { store.clear() }
        assertTrue(first.stateValues.isEmpty()); assertTrue(second.stateValues.isEmpty())
    }
}
