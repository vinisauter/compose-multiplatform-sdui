package me.developes.humming.builder.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import me.developes.humming.sdui.studio.*
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.junit.Rule
import org.junit.Test
import java.io.File
import kotlin.test.*

@OptIn(ExperimentalTestApi::class)
class InteractionCanvasTest {
    @get:Rule val compose = createComposeRule()

    @Test fun realComponentBoundsDriveCurvePaintHitZoomAndEscapeCancellation() {
        val w = LocalWorkspace(journeyExample())
        val compiled = w.compilePreview().requireValue()
        var connections = emptyList<DrawnConnection>()
        var selected by mutableStateOf<String?>(null)
        var selections = 0
        var mounted by mutableStateOf(true)
        compose.setContent { if (mounted) MaterialTheme {
            Box(Modifier.size(900.dp,600.dp)) {
                InteractionCanvas(w.document,compiled,selected,
                    select = { _, edge -> selected=edge; selections++ }, observe = { connections=it })
            }
        } }
        try {
            compose.waitUntil(10_000) { connections.size == 1 && connections.single().flattened.toleranceMet }
            val initial = connections.single()
            assertTrue(initial.curve.p0.x in 1.0..279.0, "Origem deve usar o componente interno medido")
            assertEquals(400.0,initial.curve.p3.x,.1)
            val surface = compose.onNodeWithTag("studio-interaction-canvas")
            fun midpoint(): Offset {
                val points=connections.single().flattened.points
                // The cubic midpoint lies halfway along this symmetric horizontal traversal.
                val world=connections.single().curve.point(.5)
                val start=points.first(); val end=points.last()
                val c=connections.single().curve
                val scale=(end.x-start.x)/(c.p3.x-c.p0.x)
                return Offset((start.x+(world.x-c.p0.x)*scale).toFloat(),(start.y+(world.y-c.p0.y)*scale).toFloat())
            }
            surface.performMouseInput { click(midpoint()) }
            compose.waitForIdle()
            assertEquals(initial.link.navigationEdgeId,selected)
            val screenshot=surface.captureToImage()
            val pixels=screenshot.toPixelMap()
            assertTrue((0 until pixels.width).any { x -> (0 until pixels.height).any { y ->
                val c=pixels[x,y]; c.red>.6f && c.green<.2f && c.blue>.3f
            } }, "Curva selecionada deve estar pintada em rosa")
            val output=File("build/reports/interaction-canvas.png").apply { parentFile.mkdirs() }
            Image.makeFromBitmap(screenshot.asSkiaBitmap()).use { image ->
                requireNotNull(image.encodeToData(EncodedImageFormat.PNG)).use { output.writeBytes(it.bytes) }
            }
            val before=selections
            surface.performMouseInput { moveTo(midpoint()); press() }
            surface.performKeyInput { pressKey(Key.Escape) }
            surface.performMouseInput { release() }
            compose.runOnIdle { assertEquals(before,selections) }
            val beforeZoom=connections.single().flattened.points.last()
            compose.onNodeWithText("Canvas +").performClick()
            compose.waitUntil(5_000) { connections.single().flattened.points.last().x > beforeZoom.x*1.2 }
            surface.performMouseInput { click(midpoint()) }
            compose.runOnIdle { assertEquals(before+1,selections); assertEquals(initial.link.navigationEdgeId,selected) }
        } finally {
            compose.runOnIdle { mounted=false }
            compose.waitForIdle()
        }
    }
}
