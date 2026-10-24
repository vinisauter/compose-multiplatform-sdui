package me.developes.humming.sdui.studio

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.developes.humming.sdui.common.initialNodeCatalog
import java.util.concurrent.Callable
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConcurrentEditSessionTest {
    private val catalog = initialNodeCatalog()
    private fun session(): EditSession {
        val root = AuthorNode("root", AuthorNodeKind.SCREEN, "layout:column", JsonObject(emptyMap()), Placement.Root("V"))
        return EditSession(AuthoringDocument("concurrent", entryNodeId = "root", nodes = mapOf(root.id to root)), catalog)
    }
    private fun insert(commandId: String, nodeId: String) = EditCommand(commandId, 0, listOf(EditOperation.InsertNode(
        AuthorNode(nodeId, AuthorNodeKind.VISUAL, "layout:text", JsonObject(mapOf("text" to JsonPrimitive(nodeId))),
            Placement.Child("root", "children", "V")))))

    private fun race(first: () -> EditOutcome, second: () -> EditOutcome): List<EditOutcome> {
        val executor = Executors.newFixedThreadPool(2)
        val barrier = CyclicBarrier(2)
        return try {
            val tasks = listOf(first, second).map { block -> executor.submit(Callable {
                barrier.await(5, TimeUnit.SECONDS)
                block()
            }) }
            tasks.map { it.get(10, TimeUnit.SECONDS) }
        } finally { executor.shutdownNow() }
    }

    @Test fun concurrentDifferentCommandsPublishExactlyOneRevisionAndOneUndoEntry() {
        val session = session()
        val results = race({ session.execute(insert("first", "a")) }, { session.execute(insert("second", "b")) })
        assertEquals(1, results.count { it.isSuccess })
        assertEquals("REVISION_CONFLICT", results.single { !it.isSuccess }.diagnostics.single().code)
        assertEquals(1L, session.snapshot().revision)
        assertEquals(2, session.snapshot().nodes.size)
        assertTrue(session.undo("undo", 1).isSuccess)
        assertEquals(setOf("root"), session.snapshot().nodes.keys)
    }

    @Test fun simultaneousRetryOfSameCommandHasOneCommitAndOneReplay() {
        val session = session()
        val command = insert("same", "child")
        val results = race({ session.execute(command) }, { session.execute(command) })
        assertTrue(results.all { it.isSuccess })
        assertEquals(1, results.count { it.replayed })
        assertEquals(1L, session.snapshot().revision)
        assertEquals(setOf("root", "child"), session.snapshot().nodes.keys)
        assertTrue(session.undo("undo", 1).isSuccess)
        assertEquals(setOf("root"), session.snapshot().nodes.keys)
    }
}
