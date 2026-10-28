package me.developes.humming.builder.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.toPixelMap
import kotlinx.serialization.json.JsonPrimitive
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.layout.LayoutDefinitions
import me.developes.humming.sdui.layout.components.fromNode
import me.developes.humming.sdui.layout.components.LocalImageSources
import me.developes.humming.sdui.studio.*
import org.junit.Rule
import org.junit.Test
import kotlin.test.*

class PropertyInspectorTest {
    @get:Rule val compose = createComposeRule()

    @Test fun searchEditsModifierThroughWorkspaceAndPreservesUndoAndFile() {
        val workspace = LocalWorkspace()
        var document by mutableStateOf(workspace.document)
        val id = document.entryNodeId
        compose.setContent { MaterialTheme { Column(Modifier.size(600.dp).verticalScroll(rememberScrollState())) {
            PropertyInspector(document.nodes.getValue(id), workspace.catalog[document.nodes.getValue(id).definition]) { key, value ->
                assertTrue(workspace.putProperty(id, key, value).isSuccess); document = workspace.document
            }
        } } }
        compose.onNodeWithTag("studio-property-search").performTextInput("modifierAlpha")
        compose.onNodeWithTag("studio-property-value").performTextInput("0.4")
        compose.onNodeWithText("Aplicar propriedade").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(JsonPrimitive("0.4"), document.nodes.getValue(id).properties["modifierAlpha"])
            val copy = LocalWorkspace().apply { openAuthoring(workspace.save().requireValue()).requireValue() }
            assertTrue(copy.compile().requireValue().artifacts.values.any { "modifierAlpha" in it })
            assertTrue(workspace.undo().isSuccess)
            assertFalse("modifierAlpha" in workspace.document.nodes.getValue(id).properties)
        }
        compose.onNodeWithTag("studio-property-search").performTextClearance()
        compose.onNodeWithTag("studio-property-search").performTextInput("backgroundGradient")
        compose.onNodeWithTag("studio-property-value").assertExists()
    }

    @Test fun slotsAndFormerlyUnsupportedFieldsAreDiscoverableEvenWhenAbsent() {
        val workspace = LocalWorkspace()
        val node = workspace.document.nodes.getValue(workspace.document.entryNodeId).copy(definition = "layout:textField")
        compose.setContent { MaterialTheme { Column(Modifier.size(600.dp).verticalScroll(rememberScrollState())) {
            PropertyInspector(node, LayoutDefinitions.textField) { _, _ -> error("Read only") }
        } } }
        compose.onNodeWithTag("studio-property-search").performTextInput("onDone")
        compose.onNodeWithText("Evento:", substring = true).assertExists()
        compose.onNodeWithTag("studio-property-value").assertDoesNotExist()
        compose.onNodeWithTag("studio-property-search").performTextClearance()
        compose.onNodeWithTag("studio-property-search").performTextInput("textFieldColor")
        compose.onNodeWithTag("studio-property-value").assertExists()
    }

    @Test fun modifierRuntimeMeasuresSizeLimitsRatioAndOffset() {
        val node = ServerDrivenNode("layout:box", "sized", mutableMapOf("maxWidth" to "120", "aspectRatio" to "2",
            "offsetX" to "12", "offsetY" to "8", "borderWidth" to "2", "borderColor" to "#123456",
            "modifierAlpha" to "0.5", "clip" to "true", "shadowElevation" to "3"))
        compose.setContent { Box(Modifier.size(300.dp)) { Box(Modifier.fromNode(node).testTag("measured")) } }
        compose.onNodeWithTag("measured").assertWidthIsEqualTo(120.dp).assertHeightIsEqualTo(60.dp)
            .assertPositionInRootIsEqualTo(12.dp, 8.dp)
    }

    @Test fun imageTintIsAppliedByTheCompiledRuntime() {
        compose.setContent { MaterialTheme {
            CompositionLocalProvider(LocalImageSources provides mapOf("solid" to @Composable { ColorPainter(Color.White) })) {
                CompiledPreview("""{"component":"layout:image","id":"tinted","properties":{"asset":"solid","contentDescription":"Tint probe","width":"60","height":"60","colorFilter":{"type":"tint","color":"#ff0000","blendMode":"SrcIn"}}}""")
            }
        } }
        val pixels = compose.onNodeWithContentDescription("Tint probe").captureToImage().toPixelMap()
        val color = pixels[pixels.width / 2, pixels.height / 2]
        assertTrue(color.red > .95f && color.green < .05f && color.blue < .05f)
    }

    @Test fun legacyTextFieldContainerColorIsActuallyRendered() {
        compose.setContent { MaterialTheme {
            CompiledPreview("""{"component":"layout:textField","id":"colored","properties":{"text":"Probe","width":"200","height":"80","textFieldColor":"#00ff00","underLineColor":"#0000ff","trailingIconColor":"#ff0000"}}""")
        } }
        val pixels = compose.onNode(hasSetTextAction()).captureToImage().toPixelMap()
        val color = pixels[3, pixels.height / 2]
        assertTrue(color.green > .95f && color.red < .05f && color.blue < .05f)
    }

    @Test fun preferredAndRequiredRangesUseComposeConstraints() {
        var required by mutableStateOf(false)
        compose.setContent {
            val properties = if (required) mutableMapOf<String, String?>("requiredMinWidth" to "140", "requiredMaxWidth" to "150",
                "requiredMinHeight" to "60", "requiredMaxHeight" to "60")
            else mutableMapOf<String, String?>("widthInMin" to "80.5", "widthInMax" to "90", "heightInMin" to "40", "heightInMax" to "60")
            Box(Modifier.size(100.dp)) { Box(Modifier.fromNode(ServerDrivenNode("layout:box", "range", properties)).testTag("range")) }
        }
        compose.onNodeWithTag("range").assertWidthIsEqualTo(81.dp).assertHeightIsEqualTo(40.dp)
        compose.runOnIdle { required = true }
        compose.onNodeWithTag("range").assertWidthIsEqualTo(140.dp).assertHeightIsEqualTo(60.dp)
    }
}
