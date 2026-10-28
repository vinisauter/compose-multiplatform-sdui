package me.developes.humming.builder.ui

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import me.developes.humming.sdui.studio.*
import org.junit.Rule
import org.junit.Test
import kotlin.test.*

class StudioJourneyTest {
    @get:Rule val compose = createComposeRule()

    @Test fun studioNavigatesExportsEveryArtifactAndReplaysTheExportedBundle() {
        val owner = object : LifecycleOwner { override val lifecycle = LifecycleRegistry(this) }
        val workspace = LocalWorkspace(journeyExample())
        assertEquals(1,workspace.tree().count { it.node.definition == "navigation:goBack" })
        val downloads = mutableMapOf<String,String>()
        val files = object : StudioFiles {
            override fun open(callback: (StudioFileResult) -> Unit) = Unit
            override fun download(name: String, content: String) { downloads[name] = content }
        }
        var replay by mutableStateOf<CompiledContracts?>(null)
        var mounted by mutableStateOf(true)
        compose.runOnIdle { owner.lifecycle.currentState = Lifecycle.State.RESUMED }
        compose.setContent {
            if (mounted) CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                val contracts = replay
                if (contracts == null) LocalStudio(files, workspace) else CompiledPreview(contracts)
            }
        }
        try {
            awaitText("Primeira tela")
            compose.onNodeWithText("Abrir segunda").performClick()
            awaitText("Segunda tela")
            compose.onNodeWithText("Voltar").performClick()
            awaitText("Primeira tela")
            compose.onNodeWithText("Exportar contrato").performClick()
            val exported = CompiledBundleCodec().decode(downloads.getValue("humming-journey.json")).requireValue()
            assertEquals(3,exported.artifacts.size)
            assertEquals(workspace.compilePreview().requireValue(),exported)
            compose.runOnIdle { replay = exported }
            awaitText("Primeira tela")
            compose.onNodeWithText("Abrir segunda").performClick()
            awaitText("Segunda tela")
            compose.onNodeWithText("Voltar").performClick()
            awaitText("Primeira tela")
        } finally {
            compose.runOnIdle { mounted = false }
            compose.waitForIdle()
            compose.runOnIdle { owner.lifecycle.currentState = Lifecycle.State.DESTROYED }
        }
    }

    private fun awaitText(text: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().size == 1 }
    }
}
