package me.developes.humming.sdui.layout.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.developes.humming.sdui.ThemeProvider
import me.developes.humming.sdui.common.*

val LocalProjectDesignSystem = staticCompositionLocalOf<DesignSystem?> { null }

fun projectColorScheme(design: DesignSystem, dark: Boolean): ColorScheme {
    design.requireValid()
    val colors = design.colors(dark)
    return (if (dark) darkColorScheme() else lightColorScheme()).copy(
        primary = colors.getValue("primary").toColor(),
        onPrimary = colors.getValue("onPrimary").toColor(),
        primaryContainer = colors.getValue("primaryContainer").toColor(),
        onPrimaryContainer = colors.getValue("onPrimaryContainer").toColor(),
        inversePrimary = colors.getValue("inversePrimary").toColor(),
        secondary = colors.getValue("secondary").toColor(),
        onSecondary = colors.getValue("onSecondary").toColor(),
        secondaryContainer = colors.getValue("secondaryContainer").toColor(),
        onSecondaryContainer = colors.getValue("onSecondaryContainer").toColor(),
        tertiary = colors.getValue("tertiary").toColor(),
        onTertiary = colors.getValue("onTertiary").toColor(),
        tertiaryContainer = colors.getValue("tertiaryContainer").toColor(),
        onTertiaryContainer = colors.getValue("onTertiaryContainer").toColor(),
        background = colors.getValue("background").toColor(),
        onBackground = colors.getValue("onBackground").toColor(),
        surface = colors.getValue("surface").toColor(),
        onSurface = colors.getValue("onSurface").toColor(),
        surfaceVariant = colors.getValue("surfaceVariant").toColor(),
        onSurfaceVariant = colors.getValue("onSurfaceVariant").toColor(),
        surfaceTint = colors.getValue("surfaceTint").toColor(),
        inverseSurface = colors.getValue("inverseSurface").toColor(),
        inverseOnSurface = colors.getValue("inverseOnSurface").toColor(),
        error = colors.getValue("error").toColor(),
        onError = colors.getValue("onError").toColor(),
        errorContainer = colors.getValue("errorContainer").toColor(),
        onErrorContainer = colors.getValue("onErrorContainer").toColor(),
        outline = colors.getValue("outline").toColor(),
        outlineVariant = colors.getValue("outlineVariant").toColor(),
        scrim = colors.getValue("scrim").toColor(),
        surfaceBright = colors.getValue("surfaceBright").toColor(),
        surfaceContainer = colors.getValue("surfaceContainer").toColor(),
        surfaceContainerHigh = colors.getValue("surfaceContainerHigh").toColor(),
        surfaceContainerHighest = colors.getValue("surfaceContainerHighest").toColor(),
        surfaceContainerLow = colors.getValue("surfaceContainerLow").toColor(),
        surfaceContainerLowest = colors.getValue("surfaceContainerLowest").toColor(),
        surfaceDim = colors.getValue("surfaceDim").toColor(),
    )
}

/** Opt-in visual adapter; the SDUI core remains independent of Material. */
class ProjectTheme(private val design: DesignSystem) : ThemeProvider {
    init { design.requireValid() }
    @Composable override fun Content(content: @Composable () -> Unit) {
        val dark = when (design.mode) { ThemeMode.LIGHT -> false; ThemeMode.DARK -> true; ThemeMode.SYSTEM -> isSystemInDarkTheme() }
        val colors = remember(design, dark) { projectColorScheme(design, dark) }
        val family = when (design.fontFamily) { "Serif" -> FontFamily.Serif; "Monospace" -> FontFamily.Monospace; "Default" -> FontFamily.Default; else -> FontFamily.SansSerif }
        fun style(name: String): TextStyle {
            val token = design.typography.getValue(name)
            return TextStyle(fontSize = token.size.sp, lineHeight = token.lineHeight.sp, fontWeight = FontWeight(token.weight),
                letterSpacing = token.tracking.sp, fontFamily = family)
        }
        val typography = Typography(
            displayLarge = style("displayLarge"),
            displayMedium = style("displayMedium"),
            displaySmall = style("displaySmall"),
            headlineLarge = style("headlineLarge"),
            headlineMedium = style("headlineMedium"),
            headlineSmall = style("headlineSmall"),
            titleLarge = style("titleLarge"),
            titleMedium = style("titleMedium"),
            titleSmall = style("titleSmall"),
            bodyLarge = style("bodyLarge"),
            bodyMedium = style("bodyMedium"),
            bodySmall = style("bodySmall"),
            labelLarge = style("labelLarge"),
            labelMedium = style("labelMedium"),
            labelSmall = style("labelSmall"),
        )
        val shapes = Shapes(
            extraSmall = RoundedCornerShape(design.shapes.getValue("extraSmall").dp),
            small = RoundedCornerShape(design.shapes.getValue("small").dp),
            medium = RoundedCornerShape(design.shapes.getValue("medium").dp),
            large = RoundedCornerShape(design.shapes.getValue("large").dp),
            extraLarge = RoundedCornerShape(design.shapes.getValue("extraLarge").dp),
        )
        val inheritedTokens = LocalPresentationTokens.current
        val tokens = remember(design, dark, inheritedTokens) {
            PresentationTokens(inheritedTokens.colorNames.associateWith { inheritedTokens.color(it)!! } + mapOf(
                "content" to design.colors(dark).getValue("onSurface"), "positive" to design.colors(dark).getValue("tertiary")),
                inheritedTokens.maskNames.associateWith { inheritedTokens.mask(it)!! })
        }
        CompositionLocalProvider(LocalProjectDesignSystem provides design, LocalThemeComposition provides colors,
            LocalPresentationTokens provides tokens) {
            MaterialTheme(colorScheme = colors, typography = typography, shapes = shapes) {
                Surface(Modifier.fillMaxSize(), color = colors.background, contentColor = colors.onBackground, content = content)
            }
        }
    }
}
