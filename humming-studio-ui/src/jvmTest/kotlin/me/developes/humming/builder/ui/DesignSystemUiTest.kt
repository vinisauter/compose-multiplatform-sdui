package me.developes.humming.builder.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.layout.components.*
import me.developes.humming.sdui.studio.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import kotlin.test.*

class DesignSystemUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun compiledBundleThemeChangesRealMaterialColorsAndPreservesFormState() {
        val workspace = LocalWorkspace().apply { importContract("""{"component":"layout:column","id":"root","children":[{"component":"layout:filledTonalButton","id":"tonal","children":[{"component":"layout:text","id":"label","properties":{"text":"Ação"}}]},{"component":"layout:outlinedTextField","id":"field","properties":{"stateKey":"name","text":""}}]}""").requireValue() }
        val base = DesignSystem()
        var design by mutableStateOf(base.copy(mode = ThemeMode.LIGHT,
            light = base.light + ("secondaryContainer" to "#FF0000"), dark = base.dark + ("secondaryContainer" to "#0000FF")))
        val contracts = workspace.compile().requireValue()
        var mounted by mutableStateOf(true)
        compose.setContent { if (mounted) Box(Modifier.size(500.dp)) {
            val encoded = CompiledBundleCodec().encode(contracts.copy(designSystem = design)).requireValue()
            CompiledPreview(CompiledBundleCodec().decode(encoded).requireValue())
        } }
        fun background() = compose.onNodeWithTag("tonal").captureToImage().toPixelMap().let { it[it.width / 2, 5] }
        assertTrue(background().red > .95f && background().blue < .05f)
        compose.onNodeWithTag("field").performTextInput("Persistiu")
        compose.runOnIdle { design = design.copy(mode = ThemeMode.DARK) }
        assertTrue(background().blue > .95f && background().red < .05f)
        compose.onNodeWithTag("field").assertTextContains("Persistiu")
        compose.runOnIdle { mounted = false }
    }

    @Test fun typographyShapesAndPaddingTokenReachTheRuntime() {
        val initial = DesignSystem()
        val design = initial.copy(mode = ThemeMode.LIGHT, spacing = initial.spacing + ("md" to 30f),
            typography = initial.typography + ("bodyLarge" to DesignTextStyle(32f, 40f)), shapes = initial.shapes + ("medium" to 27f))
        var observed: Shapes? = null
        compose.setContent { CompositionLocalProvider(LocalPresentationTokens provides PresentationTokens(
            mapOf("brand" to "#123456"), mapOf("private" to AsciiDigitMask("##-##")))) { ProjectTheme(design).Content {
            val shapes = MaterialTheme.shapes
            val tokens = LocalPresentationTokens.current
            SideEffect {
                observed = shapes
                assertEquals("#123456", tokens.color("brand"))
                assertNotNull(tokens.mask("private"))
            }
            Box(Modifier.fillMaxSize()) {
                Box(Modifier.testTag("padded").fromNode(ServerDrivenNode("layout:box", "space", mutableMapOf("paddingToken" to "md")))) { Box(Modifier.size(20.dp)) }
                CompiledPreview(CompiledContracts("text", 0, "screen.json", null,
                    mapOf("screen.json" to """{"component":"layout:text","id":"large","properties":{"text":"Tipografia do projeto"}}"""), emptyMap(), design))
            }
        } }
        }
        compose.onNodeWithTag("padded").assertWidthIsEqualTo(80.dp).assertHeightIsEqualTo(80.dp)
        compose.onNodeWithText("Tipografia do projeto").assertHeightIsAtLeast(40.dp)
        compose.runOnIdle { assertEquals(RoundedCornerShape(27.dp), observed!!.medium) }
    }

    @Test fun builderConfiguresCancelsSavesReopensAndExportsTheDesignSystem() {
        val owner = object : LifecycleOwner { override val lifecycle = LifecycleRegistry(this) }
        val downloads = mutableMapOf<String, String>()
        val files = object : StudioFiles {
            override fun open(result: (StudioFileResult) -> Unit) { result(StudioFileResult(content = downloads.getValue("humming-app-project.json"))) }
            override fun download(name: String, content: String) { downloads[name] = content }
        }
        var mounted by mutableStateOf(true)
        compose.runOnIdle { owner.lifecycle.currentState = Lifecycle.State.RESUMED }
        compose.setContent { CompositionLocalProvider(LocalLifecycleOwner provides owner) {
            if (mounted) Box(Modifier.size(1440.dp, 1000.dp)) { AppBuilder(files, LocalWorkspace.blankDocument()) }
        } }
        compose.onNodeWithTag("builder-design-system").performClick()
        compose.onNodeWithTag("theme-seed").performTextReplacement("#008577")
        compose.onNodeWithTag("theme-generate").performScrollTo().performClick()
        compose.onNodeWithTag("theme-role-color").performScrollTo().performTextReplacement("#FF0000")
        compose.onNodeWithTag("theme-apply-color").performScrollTo().performClick()
        compose.onNodeWithText("Modo do aplicativo: SYSTEM").performScrollTo().performClick()
        compose.onNodeWithText("DARK", substring = false).performClick()
        val dir = File("build/reports/design-system").also { it.mkdirs() }
        org.jetbrains.skia.Image.makeFromBitmap(compose.onNodeWithTag("design-system-editor").captureToImage().asSkiaBitmap()).use { image ->
            image.encodeToData()?.use { File(dir, "design-system.png").writeBytes(it.bytes) }
        }
        compose.onNodeWithTag("theme-save").performClick()
        compose.onNodeWithText("Salvar projeto", substring = false).performClick()
        val project = AppProjectCodec().decode(downloads.getValue("humming-app-project.json")).requireValue()
        assertEquals("#008577", project.designSystem!!.seed)
        assertEquals("#FF0000", project.designSystem!!.light.getValue("primary"))
        assertEquals(ThemeMode.DARK, project.designSystem!!.mode)
        val saved = downloads.getValue("humming-app-project.json")
        compose.onNodeWithTag("builder-design-system").performClick()
        compose.onNodeWithTag("theme-seed").performTextReplacement("#000000")
        compose.onNodeWithTag("theme-generate").performScrollTo().performClick()
        compose.onNodeWithText("Cancelar", substring = false).performClick()
        compose.onNodeWithText("Salvar projeto", substring = false).performClick()
        assertEquals(saved, downloads.getValue("humming-app-project.json"))
        compose.onNodeWithText("Abrir projeto", substring = false).performClick()
        compose.onNodeWithText("Continuar", substring = false).performClick()
        compose.onNodeWithTag("builder-step-4").performClick()
        compose.onNodeWithText("Exportar jornada SDUI", substring = false).performScrollTo().performClick()
        assertEquals(project.designSystem, CompiledBundleCodec().decode(downloads.getValue("humming-app-contracts.json")).requireValue().designSystem)
        compose.runOnIdle { mounted = false; owner.lifecycle.currentState = Lifecycle.State.DESTROYED }
    }

    @Test fun componentShowcaseIsValidAndMountsInBothModes() {
        assertTrue(initialNodeCatalog().validate(parseNode(themeFixture), mode = ValidationMode.AUTHORING).isEmpty())
        var mode by mutableStateOf(ThemeMode.LIGHT)
        var mounted by mutableStateOf(true)
        compose.setContent { if (mounted) Box(Modifier.size(500.dp, 700.dp)) {
            CompiledPreview(CompiledContracts("showcase", 0, "screen.json", null, mapOf("screen.json" to themeFixture), emptyMap(), DesignSystem(mode = mode)))
        } }
        compose.onNodeWithTag("theme-button").assertExists()
        compose.runOnIdle { mode = ThemeMode.DARK }
        compose.onNodeWithTag("theme-input").assertExists()
        compose.runOnIdle { mounted = false }
    }
}
