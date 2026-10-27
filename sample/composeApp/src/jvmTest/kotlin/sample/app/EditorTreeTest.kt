package sample.app

import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import me.developes.humming.sdui.studio.*
import org.junit.Rule
import org.junit.Test
import kotlin.test.*

@OptIn(ExperimentalTestApi::class)
class EditorTreeTest {
    @get:Rule val compose = createComposeRule()

    @Test fun autoScrollKeepsTheCapturedSourceWhileItsRowLeavesTheVisibleList() {
        val items = (0 until 40).joinToString(",") { """{"component":"layout:text","id":"item$it","properties":{"text":"Item $it"}}""" }
        val workspace = LocalWorkspace().apply { importContract("""{"component":"layout:column","id":"root","children":[$items,{"component":"layout:button","id":"last"}]}""").requireValue() }
        val document = mutableStateOf(workspace.document)
        val before = workspace.document
        val mounted = mutableStateOf(true)
        compose.setContent { if (mounted.value) MaterialTheme { Box(Modifier.size(400.dp, 240.dp)) {
            EditorTree(workspace, document.value, workspace.tree(), "root", {}, { document.value = workspace.document })
        } } }
        val tree = compose.onNodeWithTag("studio-tree")
        val origin = tree.fetchSemanticsNode().boundsInRoot.topLeft
        val start = compose.onNodeWithTag("studio-node-item0").fetchSemanticsNode().boundsInRoot.center - origin
        val height = tree.fetchSemanticsNode().boundsInRoot.height
        tree.performMouseInput { moveTo(start); press(); moveTo(Offset(80f, height - 4), 300) }
        compose.mainClock.advanceTimeBy(6_000)
        compose.onNodeWithTag("studio-node-last").assertExists()
        assertEquals(before, workspace.document)
        val end = compose.onNodeWithTag("studio-node-last").fetchSemanticsNode().boundsInRoot.center - origin
        tree.performMouseInput { moveTo(end, 300); release() }
        compose.waitUntil(5_000) { (workspace.document.nodes.getValue("item0").placement as? Placement.Child)?.parentId == "last" }
        compose.runOnIdle { mounted.value = false }
    }

    @Test fun actualTreeReparentsReordersUndoesAndCancelsWithoutChangingTheDocument() {
        val workspace = LocalWorkspace().apply { importContract("""{"component":"layout:column","id":"root","properties":{"fillMaxSize":"1"},"children":[{"component":"layout:text","id":"moving","properties":{"text":"Texto movido"}},{"component":"layout:button","id":"button","children":[{"component":"layout:text","id":"label","properties":{"text":"Destino"}}]}]}""").requireValue() }
        val original = workspace.document
        val mounted = mutableStateOf(true)
        compose.setContent { if (mounted.value) LocalStudio(workspace = workspace) }
        compose.onNodeWithTag("studio-node-moving").assertExists()
        val tree = compose.onNodeWithTag("studio-tree")
        fun row(id: String) = compose.onNodeWithTag("studio-node-$id").fetchSemanticsNode().boundsInRoot
        fun relative(point: Offset) = point - tree.fetchSemanticsNode().boundsInRoot.topLeft
        var start = relative(row("moving").center)
        var end = relative(row("button").center)
        tree.performMouseInput { moveTo(start); press(); moveTo(end, 400) }
        compose.onNodeWithTag("studio-node-button").assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.StateDescription, "Dentro de button · children"))
        assertEquals(original, workspace.document)
        tree.performMouseInput { release() }
        compose.waitUntil(5_000) { (workspace.document.nodes.getValue("moving").placement as? Placement.Child)?.parentId == "button" }
        assertTrue(workspace.compile().isSuccess)
        compose.onNodeWithText("Desfazer", substring = false).performClick()
        assertEquals(original.nodes, workspace.document.nodes)
        compose.onNodeWithText("Refazer", substring = false).performClick()
        start = relative(row("moving").center)
        end = relative(Offset(row("label").center.x, row("label").top + 2))
        tree.performMouseInput { moveTo(start); press(); moveTo(end, 400); release() }
        compose.waitUntil(5_000) { AuthoringIndices(workspace.document).childrenByParentAndSlot[ParentSlot("button", "children")]?.firstOrNull()?.id == "moving" }
        val beforeCancel = workspace.document
        start = relative(row("label").center)
        end = relative(Offset(row("button").center.x, row("button").top + 2))
        tree.performMouseInput { moveTo(start); press(); moveTo(end, 400) }
        tree.performKeyInput { pressKey(Key.Escape) }
        tree.performMouseInput { release() }
        assertEquals(beforeCancel, workspace.document)
        start = relative(row("label").center)
        tree.performMouseInput { moveTo(start); press(); moveTo(Offset(-40f, -40f), 400); release() }
        assertEquals(beforeCancel, workspace.document)
        compose.runOnIdle { mounted.value = false }
    }
}
