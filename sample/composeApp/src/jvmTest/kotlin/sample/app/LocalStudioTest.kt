package sample.app

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import me.developes.humming.sdui.studio.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import kotlin.test.*

class LocalStudioTest {
    @get:Rule val compose = createComposeRule()

    @Test fun disconnectsAndReconnectsExistingActionsThroughInspectorWithUndo() {
        val workspace = LocalWorkspace()
        assertTrue(workspace.importContract("""{"component":"layout:button","id":"button","properties":{"onClick":[{"component":"action:update","id":"update","properties":{"state":"status","value":"ok"}}]},"children":[{"component":"layout:text","id":"label","properties":{"text":"Executar"}}]}""").isSuccess)
        val edge = workspace.document.edges.values.single()
        compose.setContent { LocalStudio(RecordingFiles(),workspace) }
        compose.onNodeWithTag("studio-disconnect-${edge.id}").performScrollTo().performClick()
        compose.runOnIdle { assertTrue(workspace.document.edges.isEmpty()) }
        compose.onNodeWithText("Desfazer").performClick()
        compose.runOnIdle { assertEquals(edge,workspace.document.edges[edge.id]) }
        compose.onNodeWithTag("studio-disconnect-${edge.id}").performScrollTo().performClick()
        compose.onNodeWithTag("studio-action-target").performScrollTo().performTextReplacement("update")
        compose.onNodeWithText("Conectar ação").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals("update",workspace.document.edges.values.single().targetNodeId)
            assertTrue(workspace.compile().isSuccess)
        }
    }

    @Test fun createsEditsUndoesSavesReopensExportsAndRunsInTheRenderer() {
        val workspace = LocalWorkspace()
        val files = RecordingFiles()
        val mounted = mutableStateOf(true)
        val root = workspace.document.entryNodeId
        compose.setContent { if (mounted.value) LocalStudio(files, workspace) }
        compose.onNodeWithText("+ textField").performScrollTo().performClick()
        val field = workspace.document.nodes.values.single { it.definition == "layout:textField" }.id
        select(field)
        compose.onNodeWithTag("studio-property-value").performTextReplacement("Pessoa Studio")
        compose.onNodeWithText("Aplicar propriedade").performClick()
        awaitText("Pessoa Studio")
        compose.onNodeWithText("Desfazer").performClick()
        awaitText("Visitante")
        compose.onNodeWithText("Refazer").performClick()
        awaitText("Pessoa Studio")
        select(root)
        compose.onNodeWithText("+ text").performScrollTo().performClick()
        compose.onNodeWithTag("studio-property-value").performTextReplacement("Nome: #{nome} / #{status}")
        compose.onNodeWithText("Aplicar propriedade").performClick()
        awaitText("Nome: Pessoa Studio / ")
        select(root)
        compose.onNodeWithText("+ button").performScrollTo().performClick()
        val button = workspace.document.nodes.values.single { it.definition == "layout:button" }.id
        compose.onNodeWithText("+ action:update").performScrollTo().performClick()
        awaitText("Confirmar")
        compose.onNodeWithText("Confirmar", useUnmergedTree = false).performClick()
        awaitText("Nome: Pessoa Studio / confirmado")
        compose.onNodeWithText("Salvar documento").performClick()
        val saved = files.downloads.getValue("humming-document.json")
        compose.onNodeWithText("Exportar contrato").performClick()
        val exported = files.downloads.getValue("humming-contract.json")
        assertTrue(exported.contains("#{status}"))
        assertTrue(exported.contains(button)); assertTrue(exported.contains(field))
        File("build/reports/local-studio").apply { mkdirs() }.let {
            File(it, "document.json").writeText(saved)
            File(it, "contract.json").writeText(exported)
        }
        files.next = StudioFileResult(saved)
        compose.onNodeWithText("Abrir documento").performClick()
        compose.onNodeWithText("Substituir", substring = false).performClick()
        awaitText("Nome: Pessoa Studio / ")
        compose.onNodeWithText("Desfazer").assertIsNotEnabled()
        compose.onNodeWithText("Confirmar").performClick()
        awaitText("Nome: Pessoa Studio / confirmado")
        compose.runOnIdle { assertEquals(saved, workspace.save().requireValue()) }
        compose.runOnIdle { mounted.value = false }
        compose.waitForIdle()
    }

    @Test fun rejectsInvalidPropertyAndFailedOpenWithoutLosingTheDocument() {
        val workspace = LocalWorkspace()
        val files = RecordingFiles()
        val mounted = mutableStateOf(true)
        compose.setContent { if (mounted.value) LocalStudio(files, workspace) }
        val before = workspace.document
        compose.onNodeWithTag("studio-property-value").performTextReplacement("invalid")
        compose.onNodeWithText("Aplicar propriedade").performClick()
        compose.onNodeWithTag("studio-message").assertTextContains("INVALID_PROPERTY", substring = true)
        compose.runOnIdle { assertEquals(before, workspace.document) }
        files.next = StudioFileResult("{}")
        compose.onNodeWithText("Abrir documento").performClick()
        compose.runOnIdle { assertEquals(before, workspace.document) }
        compose.onNodeWithTag("studio-compile-error").assertDoesNotExist()
        compose.runOnIdle { mounted.value = false }
        compose.waitForIdle()
    }

    private fun select(id: String) { compose.onNodeWithTag("studio-node-$id").performClick() }
    private fun awaitText(text: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(text, substring = false).fetchSemanticsNodes().isNotEmpty() }
    }
    private class RecordingFiles : StudioFiles {
        var next = StudioFileResult()
        val downloads = mutableMapOf<String, String>()
        override fun open(result: (StudioFileResult) -> Unit) { result(next) }
        override fun download(name: String, content: String) { downloads[name] = content }
    }
}
