package me.developes.humming.sdui.layout.components

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import me.developes.humming.sdui.ThemeProvider
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.common.toColor

val LocalThemeComposition = staticCompositionLocalOf {
    lightColorScheme(
        primary = PrimaryLight,
        onPrimary = OnPrimaryLight,
        primaryContainer = PrimaryContainerLight,
        onPrimaryContainer = OnPrimaryContainerLight,
        secondary = SecondaryLight,
        onSecondary = OnSecondaryLight,
        secondaryContainer = SecondaryContainerLight,
        onSecondaryContainer = OnSecondaryContainerLight,
        tertiary = TertiaryLight,
        onTertiary = OnTertiaryLight,
        tertiaryContainer = TertiaryContainerLight,
        onTertiaryContainer = OnTertiaryContainerLight,
        error = ErrorLight,
        onError = OnErrorLight,
        errorContainer = ErrorContainerLight,
        onErrorContainer = OnErrorContainerLight,
        background = BackgroundLight,
        onBackground = OnBackgroundLight,
        surface = SurfaceLight,
        onSurface = OnSurfaceLight,
        surfaceVariant = SurfaceVariantLight,
        onSurfaceVariant = OnSurfaceVariantLight,
        outline = OutlineLight,
        outlineVariant = OutlineVariantLight,
        scrim = ScrimLight,
        inverseSurface = InverseSurfaceLight,
        inverseOnSurface = InverseOnSurfaceLight,
        inversePrimary = InversePrimaryLight,
        surfaceDim = SurfaceDimLight,
        surfaceBright = SurfaceBrightLight,
        surfaceContainerLowest = SurfaceContainerLowestLight,
        surfaceContainerLow = SurfaceContainerLowLight,
        surfaceContainer = SurfaceContainerLight,
        surfaceContainerHigh = SurfaceContainerHighLight,
        surfaceContainerHighest = SurfaceContainerHighestLight,
    )
}

class Material3Theme(val node: ServerDrivenNode = ServerDrivenNode("config:theme")) : ThemeProvider {
    private val primary = node.property("primary")?.toColor()
    private val onPrimary = node.property("onPrimary")?.toColor()
    private val primaryContainer = node.property("primaryContainer")?.toColor()
    private val onPrimaryContainer = node.property("onPrimaryContainer")?.toColor()
    private val inversePrimary = node.property("inversePrimary")?.toColor()
    private val secondary = node.property("secondary")?.toColor()
    private val onSecondary = node.property("onSecondary")?.toColor()
    private val secondaryContainer = node.property("secondaryContainer")?.toColor()
    private val onSecondaryContainer = node.property("onSecondaryContainer")?.toColor()
    private val tertiary = node.property("tertiary")?.toColor()
    private val onTertiary = node.property("onTertiary")?.toColor()
    private val tertiaryContainer = node.property("tertiaryContainer")?.toColor()
    private val onTertiaryContainer = node.property("onTertiaryContainer")?.toColor()
    private val background = node.property("background")?.toColor()
    private val onBackground = node.property("onBackground")?.toColor()
    private val surface = node.property("surface")?.toColor()
    private val onSurface = node.property("onSurface")?.toColor()
    private val surfaceVariant = node.property("surfaceVariant")?.toColor()
    private val onSurfaceVariant = node.property("onSurfaceVariant")?.toColor()
    private val surfaceTint = node.property("surfaceTint")?.toColor()
    private val inverseSurface = node.property("inverseSurface")?.toColor()
    private val inverseOnSurface = node.property("inverseOnSurface")?.toColor()
    private val error = node.property("error")?.toColor()
    private val onError = node.property("onError")?.toColor()
    private val errorContainer = node.property("errorContainer")?.toColor()
    private val onErrorContainer = node.property("onErrorContainer")?.toColor()
    private val outline = node.property("outline")?.toColor()
    private val outlineVariant = node.property("outlineVariant")?.toColor()
    private val scrim = node.property("scrim")?.toColor()
    private val surfaceBright = node.property("surfaceBright")?.toColor()
    private val surfaceContainer = node.property("surfaceContainer")?.toColor()
    private val surfaceContainerHigh = node.property("surfaceContainerHigh")?.toColor()
    private val surfaceContainerHighest = node.property("surfaceContainerHighest")?.toColor()
    private val surfaceContainerLow = node.property("surfaceContainerLow")?.toColor()
    private val surfaceContainerLowest = node.property("surfaceContainerLowest")?.toColor()
    private val surfaceDim = node.property("surfaceDim")?.toColor()

    @Composable
    override fun Content(content: @Composable () -> Unit) {
        val currentTheme: ColorScheme = LocalThemeComposition.current
        val colorScheme = ColorScheme(
            primary = primary ?: currentTheme.primary,
            onPrimary = onPrimary ?: currentTheme.onPrimary,
            primaryContainer = primaryContainer ?: currentTheme.primaryContainer,
            onPrimaryContainer = onPrimaryContainer ?: currentTheme.onPrimaryContainer,
            inversePrimary = inversePrimary ?: currentTheme.inversePrimary,
            secondary = secondary ?: currentTheme.secondary,
            onSecondary = onSecondary ?: currentTheme.onSecondary,
            secondaryContainer = secondaryContainer ?: currentTheme.secondaryContainer,
            onSecondaryContainer = onSecondaryContainer ?: currentTheme.onSecondaryContainer,
            tertiary = tertiary ?: currentTheme.tertiary,
            onTertiary = onTertiary ?: currentTheme.onTertiary,
            tertiaryContainer = tertiaryContainer ?: currentTheme.tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer ?: currentTheme.onTertiaryContainer,
            background = background ?: currentTheme.background,
            onBackground = onBackground ?: currentTheme.onBackground,
            surface = surface ?: currentTheme.surface,
            onSurface = onSurface ?: currentTheme.onSurface,
            surfaceVariant = surfaceVariant ?: currentTheme.surfaceVariant,
            onSurfaceVariant = onSurfaceVariant ?: currentTheme.onSurfaceVariant,
            surfaceTint = surfaceTint ?: currentTheme.surfaceTint,
            inverseSurface = inverseSurface ?: currentTheme.inverseSurface,
            inverseOnSurface = inverseOnSurface ?: currentTheme.inverseOnSurface,
            error = error ?: currentTheme.error,
            onError = onError ?: currentTheme.onError,
            errorContainer = errorContainer ?: currentTheme.errorContainer,
            onErrorContainer = onErrorContainer ?: currentTheme.onErrorContainer,
            outline = outline ?: currentTheme.outline,
            outlineVariant = outlineVariant ?: currentTheme.outlineVariant,
            scrim = scrim ?: currentTheme.scrim,
            surfaceBright = surfaceBright ?: currentTheme.surfaceBright,
            surfaceContainer = surfaceContainer ?: currentTheme.surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh ?: currentTheme.surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest
                ?: currentTheme.surfaceContainerHighest,
            surfaceContainerLow = surfaceContainerLow ?: currentTheme.surfaceContainerLow,
            surfaceContainerLowest = surfaceContainerLowest ?: currentTheme.surfaceContainerLowest,
            surfaceDim = surfaceDim ?: currentTheme.surfaceDim,
        )
        CompositionLocalProvider(LocalThemeComposition provides colorScheme) {
            content()
        }
    }
}