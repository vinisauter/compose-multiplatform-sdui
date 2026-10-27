package me.developes.humming.sdui.common

import kotlin.test.*

class DesignSystemTest {
    @Test fun generatedSchemesAreDeterministicCompleteAndHaveContrastingTextPairs() {
        for (seed in listOf("#6750A4", "#000000", "#FFFFFF", "#FF0000", "#00FF00", "#0000FF")) {
            val design = DesignSystem(seed = seed, light = generateThemeColors(seed, false), dark = generateThemeColors(seed, true))
            assertEquals(emptyList(), design.diagnostics())
            for (dark in listOf(false, true)) {
                val colors = design.colors(dark)
                assertEquals(36, colors.size)
                assertEquals(colors, generateThemeColors(seed, dark))
                designContrastPairs.forEach { (fg, bg) -> assertTrue(designContrast(colors.getValue(fg), colors.getValue(bg)) >= 4.5, "$seed $dark $fg $bg") }
            }
            assertNotEquals(design.light, design.dark)
        }
    }

    @Test fun standaloneCodecRoundTripsAndRejectsUnknownVersionsFieldsAndInvalidTokens() {
        val design = DesignSystem(mode = ThemeMode.DARK, fontFamily = "Serif")
        val json = DesignSystemCodec.encode(design)
        assertEquals(design, DesignSystemCodec.decode(json))
        assertEquals(json, DesignSystemCodec.encode(DesignSystemCodec.decode(json)))
        assertFailsWith<PayloadException> { DesignSystemCodec.decode(json.replace("\"formatVersion\":1", "\"formatVersion\":9")) }
        assertFailsWith<PayloadException> { DesignSystemCodec.decode(json.dropLast(1) + ",\"code\":\"run()\"}") }
        assertFailsWith<PayloadException> { DesignSystemCodec.decode("[]") }
        for (invalid in listOf(design.copy(seed = "red"), design.copy(light = emptyMap()), design.copy(fontFamily = "network-font"),
            design.copy(shapes = design.shapes + ("medium" to -1f)), design.copy(spacing = design.spacing + ("md" to Float.NaN)),
            design.copy(typography = design.typography + ("bodyLarge" to DesignTextStyle(20f, 10f))))) {
            assertTrue(invalid.diagnostics().all { it.code == "INVALID_DESIGN_SYSTEM" && it.path.startsWith("$.designSystem.") })
            assertFailsWith<PayloadException> { DesignSystemCodec.encode(invalid) }
        }
    }

    @Test fun contrastWarningsRemainObservableForUserOverrides() {
        assertEquals(21.0, designContrast("#000000", "#FFFFFF"))
        val design = DesignSystem().let { it.copy(light = it.light + ("onPrimary" to it.light.getValue("primary"))) }
        assertTrue(design.diagnostics().isEmpty())
        assertEquals(1.0, designContrast(design.light.getValue("onPrimary"), design.light.getValue("primary")))
    }
}
