package me.developes.humming.builder.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.junit.After
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonPrimitive
import me.developes.humming.sdui.studio.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import kotlin.test.*

class HummingBuilderTest {
    @get:Rule val compose = createComposeRule()
    private val owner = object : LifecycleOwner { override val lifecycle = LifecycleRegistry(this) }
    @After fun closeOwner() { compose.runOnIdle { if (owner.lifecycle.currentState == Lifecycle.State.INITIALIZED) owner.lifecycle.currentState = Lifecycle.State.CREATED; owner.lifecycle.currentState = Lifecycle.State.DESTROYED } }

    @Test fun starterUsesRegisteredNodesAndKeepsProjectRoundTrip() {
        val workspace = LocalWorkspace(builderStarter())
        val compilation = workspace.compileJourney("compiled-preview")
        assertTrue(compilation.isSuccess, compilation.diagnostics.toString())
        val fields = builderStateFields(workspace.document)
        assertEquals(setOf("pix_key", "amount", "instant"), fields.map { it.key }.toSet())
        assertTrue(workspace.putProperty("pix-amount", "text", JsonPrimitive("500,00")).isSuccess)
        assertEquals("500,00", builderStateFields(workspace.document).single { it.key == "amount" }.defaultValue)
        assertTrue(workspace.undo().isSuccess)
        assertEquals("250,00", builderStateFields(workspace.document).single { it.key == "amount" }.defaultValue)
        val project = AppProject("FinTech App", ProjectViewport.PHONE, emptyMap(), workspace.document)
        val codec = AppProjectCodec()
        assertEquals(project, codec.decode(codec.encode(project).requireValue()).requireValue())
    }

    @Test fun officialShellRunsFlowsAndExposesRealState() {
        compose.runOnIdle { owner.lifecycle.currentState = Lifecycle.State.RESUMED }
        compose.setContent { CompositionLocalProvider(LocalLifecycleOwner provides owner) { Box(Modifier.size(1520.dp, 960.dp)) { HummingBuilder() } } }
        compose.waitForIdle()
        compose.onNodeWithText("Send Pix Instant").assertExists()
        val initialHeadingX = compose.onNodeWithText("Send Pix Instant").fetchSemanticsNode().boundsInRoot.center.x
        capture("workspace")
        compose.onNodeWithTag("official-nav-2").performClick()
        compose.waitForIdle()
        capture("flows")
        compose.onNodeWithTag("official-nav-4").performClick()
        compose.waitForIdle()
        val scopeHeading = compose.onNodeWithText("STATE COLLECTIONS").fetchSemanticsNode().boundsInRoot
        val scopeSource = compose.onNodeWithText("Screen inputs", substring=false).fetchSemanticsNode().boundsInRoot
        assertTrue(scopeSource.top > scopeHeading.bottom, "State scope labels must be vertically arranged")
        compose.onNodeWithTag("official-state-amount").assertExists().assertIsEnabled()
        compose.onNodeWithTag("official-state-amount").performTextReplacement("700,00")
        compose.onNodeWithTag("official-state-json").assertTextContains("700,00", substring = true)
        capture("state")
        compose.onNodeWithTag("official-reset-state").performClick()
        compose.waitForIdle()
        compose.onNodeWithTag("official-state-json").assertTextContains("250,00", substring = true)
        compose.onNodeWithTag("official-theme").performClick()
        capture("dark")
        compose.onNodeWithTag("official-nav-0").performClick()
        compose.onNodeWithTag("official-run").performClick()
        compose.onNodeWithText("Continuar no exemplo").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Fluxo concluído no sandbox local").assertExists()
        compose.onNodeWithTag("official-nav-0").performClick()
        compose.waitForIdle()
        assertEquals(initialHeadingX, compose.onNodeWithText("Send Pix Instant").fetchSemanticsNode().boundsInRoot.center.x, 1f)
    }

    @Test fun compactInspectorAndProjectMenuPreserveCommandsUndoAndFiles() {
        var saved = ""
        val files = object : StudioFiles {
            override fun open(result: (StudioFileResult) -> Unit) { result(StudioFileResult(saved)) }
            override fun download(name: String, content: String) { saved = content }
        }
        compose.runOnIdle { owner.lifecycle.currentState = Lifecycle.State.RESUMED }
        compose.setContent { CompositionLocalProvider(LocalLifecycleOwner provides owner) { HummingBuilder(files) } }
        compose.onNodeWithTag("stitch-padding-16").performClick()
        menu("Salvar projeto")
        val codec = AppProjectCodec()
        assertEquals(JsonPrimitive("16"),codec.decode(saved).requireValue().document.nodes.getValue("pix-key").properties["paddingAll"])
        compose.onNodeWithText("Undo",substring=false).performClick()
        menu("Salvar projeto")
        assertFalse("paddingAll" in codec.decode(saved).requireValue().document.nodes.getValue("pix-key").properties)
        menu("Rename project")
        compose.onNodeWithTag("stitch-project-name").performTextReplacement("Meu Builder")
        compose.onNodeWithText("Apply",substring=false).performClick()
        menu("Salvar projeto")
        assertEquals("Meu Builder",codec.decode(saved).requireValue().name)
        menu("Abrir projeto")
        compose.onNodeWithText("Continuar",substring=false).performClick()
        menu("Salvar projeto")
        assertEquals("Meu Builder",codec.decode(saved).requireValue().name)
        assertTrue(LocalWorkspace(codec.decode(saved).requireValue().document).compileJourney("compiled-preview").isSuccess)
    }

    private fun menu(label: String) {
        compose.onNodeWithTag("stitch-project-menu").performClick()
        compose.onNodeWithText(label,substring=false).performClick()
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val target = File("build/reports/builder/$name.png").also { it.parentFile.mkdirs() }
        val data = org.jetbrains.skia.Image.makeFromBitmap(compose.onRoot().captureToImage().asSkiaBitmap()).encodeToData()!!
        target.writeBytes(data.bytes)
    }
}
