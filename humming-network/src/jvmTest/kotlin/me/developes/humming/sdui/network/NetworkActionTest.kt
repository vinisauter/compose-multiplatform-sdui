package me.developes.humming.sdui.network

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.http.content.OutgoingContent
import kotlinx.coroutines.*
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import me.developes.humming.sdui.action.SDNetwork
import me.developes.humming.sdui.common.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkActionTest {
    @Test fun actionAwaitsItsBranchAndKeepsResultsScopedWithNoStaleSuccessOnFailure() = runBlocking<Unit> {
        Dispatchers.setMain(Dispatchers.Unconfined)
        val store = ViewModelStore()
        val client = HttpClient(MockEngine { request ->
            if (request.url.encodedPath == "/post") {
                assertEquals(HttpMethod.Post, request.method)
                assertEquals("{\"id\":\"0007\"}", (request.body as OutgoingContent.ByteArrayContent).bytes().decodeToString())
            }
            if (request.url.encodedPath == "/bad") respond("private", HttpStatusCode.BadRequest)
            else respond("{\"id\":\"0007\"}", headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }) { followRedirects = false }
        val snapshots = mutableListOf<Pair<SduiValue, SduiValue>>()
        val retained = mutableListOf<ActionValues>()
        var branchCompleted = false
        try {
            val transport = SduiHttpTransport(client, mapOf("ok" to HttpEndpoint("https://local.invalid/ok"), "bad" to HttpEndpoint("https://local.invalid/bad"),
                "post" to HttpEndpoint("https://local.invalid/post", setOf("POST"))))
            val observer = SDLibrary("test").addAction("observe") { _, owner ->
                val slots = owner.actionValues(); retained += slots
                delay(10)
                snapshots += slots.response to slots.error
                branchCompleted = true
            }
            val vm = SDUIFormViewModel(SavedStateHandle(), listOf(SDNetwork(transport), observer))
            store.put("test", vm)
            val request = parseNode("""{"component":"network:request","id":"request","properties":{"endpoint":"ok",
              "onSuccess":{"component":"test:observe"},"onFailure":{"component":"test:observe"}}}""")
            val failure = parseNode("""{"component":"network:request","id":"failed","properties":{"endpoint":"bad",
              "onFailure":{"component":"test:observe"}}}""")
            assertTrue(vm.executeActions(listOf(request, failure)).isSuccess)
            assertTrue(branchCompleted)
            assertIs<SduiValue.ObjectValue>(snapshots[0].first)
            assertEquals(SduiValue.Missing, snapshots[0].second)
            assertEquals(SduiValue.Missing, snapshots[1].first)
            assertEquals(SduiValue.Text("NETWORK_HTTP_STATUS"), (snapshots[1].second as SduiValue.ObjectValue).values["code"])
            assertTrue(retained.all { it.response === SduiValue.Missing && it.error === SduiValue.Missing })
            assertTrue(vm.stateValues.isEmpty())
            val unhandled = parseNode("""{"component":"network:request","id":"unhandled","properties":{"endpoint":"bad"}}""")
            val outcome = vm.executeActions(listOf(unhandled))
            assertFalse(outcome.isSuccess)
            assertEquals("NETWORK_HTTP_STATUS", outcome.errors.single().code)
            assertEquals("unhandled", outcome.errors.single().diagnostic?.nodeId)
            val body = parseSduiValue("""{"id":"0007"}""")
            vm.updateValue("form", body)
            val post = parseNode("""{"component":"network:request","id":"post","properties":{"endpoint":"post","method":"POST","bodyState":"form",
              "onSuccess":{"component":"test:observe"}}}""")
            assertTrue(vm.executeActions(listOf(post)).isSuccess)
            assertEquals(body, vm.stateValue("form"))
            assertEquals(SduiValue.Missing, snapshots.last().second)
            val literal = parseNode("""{"component":"network:request","id":"literal","properties":{"endpoint":"post","method":"POST",
              "bodyJson":"{\"id\":\"0007\"}","onSuccess":{"component":"test:observe"}}}""")
            assertTrue(vm.executeActions(listOf(literal)).isSuccess)
            val conflict = parseNode("""{"component":"network:request","id":"conflict","properties":{"endpoint":"post","method":"POST","bodyJson":"{}","bodyState":"form"}}""")
            assertEquals("NETWORK_BODY", vm.executeActions(listOf(conflict)).errors.single().code)
            val malformed = parseNode("""{"component":"network:request","id":"malformed","properties":{"endpoint":"post","method":"POST","bodyJson":"<bad>"}}""")
            val diagnostic = vm.executeActions(listOf(malformed)).errors.single().diagnostic!!
            assertEquals("malformed", diagnostic.nodeId)
            assertTrue(diagnostic.path.startsWith("$.properties[\"bodyJson\"]"))
            assertTrue(retained.all { it.response === SduiValue.Missing && it.error === SduiValue.Missing })
        } finally {
            store.clear(); client.close(); client.engine.close(); Dispatchers.resetMain()
        }
    }
}
