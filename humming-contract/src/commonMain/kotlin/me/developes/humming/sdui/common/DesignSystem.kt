package me.developes.humming.sdui.common

import kotlinx.serialization.Serializable
import kotlin.math.*

@Serializable enum class ThemeMode { LIGHT, DARK, SYSTEM }
@Serializable data class DesignTextStyle(val size: Float, val lineHeight: Float, val weight: Int = 400, val tracking: Float = 0f)

/** Declarative project configuration. Interpretation belongs to the registered visual library. */
@Serializable data class DesignSystem(
    val formatVersion: Int = 1,
    val name: String = "Tema do aplicativo",
    val seed: String = "#6750A4",
    val mode: ThemeMode = ThemeMode.SYSTEM,
    val light: Map<String, String> = generateThemeColors("#6750A4", false),
    val dark: Map<String, String> = generateThemeColors("#6750A4", true),
    val fontFamily: String = "SansSerif",
    val typography: Map<String, DesignTextStyle> = defaultDesignTypography(),
    val shapes: Map<String, Float> = mapOf("extraSmall" to 4f, "small" to 8f, "medium" to 12f, "large" to 16f, "extraLarge" to 28f),
    val spacing: Map<String, Float> = mapOf("xs" to 4f, "sm" to 8f, "md" to 16f, "lg" to 24f, "xl" to 32f),
) {
    fun diagnostics(): List<NodeDiagnostic> {
        val result = mutableListOf<NodeDiagnostic>()
        fun reject(path: String) { result += NodeDiagnostic("INVALID_DESIGN_SYSTEM", "Invalid design token", "designSystem", "$.designSystem.$path") }
        if (formatVersion != 1) reject("formatVersion")
        if (name.isBlank() || name.length > 80) reject("name")
        if (!isDesignColor(seed)) reject("seed")
        for ((label, colors) in listOf("light" to light, "dark" to dark)) {
            if (colors.keys != designColorRoles.toSet()) reject(label)
            colors.forEach { (key, value) -> if (!isDesignColor(value)) reject("$label.$key") }
        }
        if (fontFamily !in setOf("Default", "SansSerif", "Serif", "Monospace")) reject("fontFamily")
        if (typography.keys != defaultDesignTypography().keys) reject("typography")
        typography.forEach { (key, style) ->
            if (!style.size.isFinite() || style.size !in 1f..200f || !style.lineHeight.isFinite() || style.lineHeight !in style.size..300f ||
                style.weight !in 100..900 || !style.tracking.isFinite() || style.tracking !in -10f..30f) reject("typography.$key")
        }
        if (shapes.keys != setOf("extraSmall", "small", "medium", "large", "extraLarge")) reject("shapes")
        if (spacing.keys != setOf("xs", "sm", "md", "lg", "xl")) reject("spacing")
        shapes.forEach { (key, v) -> if (!v.isFinite() || v !in 0f..200f) reject("shapes.$key") }
        spacing.forEach { (key, v) -> if (!v.isFinite() || v !in 0f..256f) reject("spacing.$key") }
        return result
    }
    fun requireValid(): DesignSystem {
        diagnostics().firstOrNull()?.let { throw PayloadException(it) }
        return this
    }
    fun colors(isDark: Boolean) = if (isDark) dark else light
}

fun isDesignColor(value: String) = value.matches(Regex("#[0-9a-fA-F]{6}"))

val designColorRoles = listOf("primary", "onPrimary", "primaryContainer", "onPrimaryContainer", "inversePrimary",
    "secondary", "onSecondary", "secondaryContainer", "onSecondaryContainer", "tertiary", "onTertiary", "tertiaryContainer", "onTertiaryContainer",
    "background", "onBackground", "surface", "onSurface", "surfaceVariant", "onSurfaceVariant", "surfaceTint", "inverseSurface", "inverseOnSurface",
    "error", "onError", "errorContainer", "onErrorContainer", "outline", "outlineVariant", "scrim", "surfaceBright", "surfaceContainer",
    "surfaceContainerHigh", "surfaceContainerHighest", "surfaceContainerLow", "surfaceContainerLowest", "surfaceDim")

fun defaultDesignTypography() = linkedMapOf(
    "displayLarge" to DesignTextStyle(57f, 64f, tracking = -.25f), "displayMedium" to DesignTextStyle(45f, 52f), "displaySmall" to DesignTextStyle(36f, 44f),
    "headlineLarge" to DesignTextStyle(32f, 40f), "headlineMedium" to DesignTextStyle(28f, 36f), "headlineSmall" to DesignTextStyle(24f, 32f),
    "titleLarge" to DesignTextStyle(22f, 28f), "titleMedium" to DesignTextStyle(16f, 24f, 500, .15f), "titleSmall" to DesignTextStyle(14f, 20f, 500, .1f),
    "bodyLarge" to DesignTextStyle(16f, 24f, tracking = .5f), "bodyMedium" to DesignTextStyle(14f, 20f, tracking = .25f), "bodySmall" to DesignTextStyle(12f, 16f, tracking = .4f),
    "labelLarge" to DesignTextStyle(14f, 20f, 500, .1f), "labelMedium" to DesignTextStyle(12f, 16f, 500, .5f), "labelSmall" to DesignTextStyle(11f, 16f, 500, .5f),
)

private fun channels(hex: String) = hex.removePrefix("#").chunked(2).map { it.toInt(16) / 255.0 }
private fun hex(channels: List<Double>) = "#" + channels.joinToString("") { (it.coerceIn(0.0, 1.0) * 255).roundToInt().toString(16).padStart(2, '0').uppercase() }
private fun luminance(color: String): Double {
    val rgb = channels(color).map { if (it <= .04045) it / 12.92 else ((it + .055) / 1.055).pow(2.4) }
    return rgb[0] * .2126 + rgb[1] * .7152 + rgb[2] * .0722
}
fun designContrast(first: String, second: String): Double {
    require(isDesignColor(first) && isDesignColor(second))
    val a = luminance(first); val b = luminance(second)
    return (max(a, b) + .05) / (min(a, b) + .05)
}
val designContrastPairs = listOf("onPrimary" to "primary", "onPrimaryContainer" to "primaryContainer", "onSecondary" to "secondary",
    "onSecondaryContainer" to "secondaryContainer", "onTertiary" to "tertiary", "onTertiaryContainer" to "tertiaryContainer",
    "onError" to "error", "onErrorContainer" to "errorContainer", "onSurface" to "surface", "onSurfaceVariant" to "surfaceVariant",
    "onBackground" to "background", "inverseOnSurface" to "inverseSurface")

/** Humming v1: sRGB interpolation toward black/white. Deliberately not Material HCT. */
fun generateThemeColors(seed: String, dark: Boolean): Map<String, String> {
    require(isDesignColor(seed))
    fun mix(a: String, b: String, fraction: Double) = hex(channels(a).zip(channels(b)) { x, y -> x * (1 - fraction) + y * fraction })
    fun tone(base: String, amount: Double) = if (amount >= .5) mix(base, "#FFFFFF", (amount - .5) * 2) else mix("#000000", base, amount * 2)
    fun ink(bg: String) = if (designContrast(bg, "#000000") >= designContrast(bg, "#FFFFFF")) "#000000" else "#FFFFFF"
    val primary = tone(seed, if (dark) .78 else .40)
    val secondary = tone(mix(seed, "#808080", .65), if (dark) .78 else .40)
    val tertiary = tone(mix(seed, "#008577", .65), if (dark) .78 else .40)
    val neutral = mix(seed, "#808080", .94)
    val map = linkedMapOf<String, String>()
    for ((name, color) in listOf("primary" to primary, "secondary" to secondary, "tertiary" to tertiary, "error" to tone("#BA1A1A", if (dark) .78 else .45))) {
        map[name] = color; map["on" + name.replaceFirstChar { it.uppercaseChar() }] = ink(color)
        val container = tone(color, if (dark) .25 else .92)
        map[name + "Container"] = container; map["on" + name.replaceFirstChar { it.uppercaseChar() } + "Container"] = ink(container)
    }
    map["surface"] = tone(neutral, if (dark) .07 else .99)
    map["background"] = map.getValue("surface"); map["onSurface"] = ink(map.getValue("surface")); map["onBackground"] = map.getValue("onSurface")
    map["surfaceVariant"] = tone(neutral, if (dark) .25 else .9); map["onSurfaceVariant"] = ink(map.getValue("surfaceVariant"))
    map["inverseSurface"] = tone(neutral, if (dark) .9 else .15); map["inverseOnSurface"] = ink(map.getValue("inverseSurface"))
    map["inversePrimary"] = tone(seed, if (dark) .4 else .8); map["surfaceTint"] = primary
    map["outline"] = tone(neutral, .5); map["outlineVariant"] = tone(neutral, if (dark) .25 else .8); map["scrim"] = "#000000"
    for ((name, lightTone, darkTone) in listOf(Triple("surfaceDim", .87, .07), Triple("surfaceBright", .99, .24), Triple("surfaceContainerLowest", 1.0, .04),
        Triple("surfaceContainerLow", .97, .10), Triple("surfaceContainer", .95, .12), Triple("surfaceContainerHigh", .93, .15), Triple("surfaceContainerHighest", .91, .18))) map[name] = tone(neutral, if (dark) darkTone else lightTone)
    return designColorRoles.associateWith { map.getValue(it) }
}
