package com.calendariomagico.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OnAccentLight = Color(0xFFFFFDF9)

private val LightColors = lightColorScheme(
    primary = LavenderDeep,
    onPrimary = OnAccentLight,
    primaryContainer = Lavender,
    onPrimaryContainer = LavenderDeep,
    secondary = MintDeep,
    onSecondary = OnAccentLight,
    secondaryContainer = Mint,
    onSecondaryContainer = MintDeep,
    tertiary = PeachDeep,
    tertiaryContainer = Peach,
    onTertiaryContainer = PeachDeep,
    background = CreamBackground,
    onBackground = InkText,
    surface = CreamSurface,
    onSurface = InkText,
    surfaceVariant = Color(0xFFF3EAFB),
    onSurfaceVariant = InkTextSoft,
    error = Color(0xFFE0637A),
    outline = Color(0xFFD8CDEA)
)

private val DarkColors = darkColorScheme(
    primary = Lavender,
    onPrimary = DarkBackground,
    primaryContainer = LavenderDeep,
    onPrimaryContainer = Lavender,
    secondary = Mint,
    onSecondary = DarkBackground,
    secondaryContainer = MintDeep,
    onSecondaryContainer = Mint,
    tertiary = Peach,
    tertiaryContainer = PeachDeep,
    onTertiaryContainer = Peach,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = Color(0xFF3C3449),
    onSurfaceVariant = Color(0xFFD3C8E3),
    error = Color(0xFFFF8FA3),
    outline = Color(0xFF554C67)
)

@Composable
fun CalendarioMagicoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = CalendarioTypography,
        shapes = CalendarioShapes,
        content = content
    )
}
