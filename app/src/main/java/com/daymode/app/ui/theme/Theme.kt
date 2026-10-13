package com.daymode.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.daymode.app.data.ThemeMode

private val Green = Color(0xFF1B4D3E)
private val GreenLight = Color(0xFF2F6F5E)
private val Cream = Color(0xFFF4F7F5)
private val Ink = Color(0xFF14221E)

private val LightColors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    secondary = GreenLight,
    onSecondary = Color.White,
    background = Cream,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
)

private val DarkColors = darkColorScheme(
    primary = GreenLight,
    onPrimary = Color.White,
    secondary = Green,
    onSecondary = Color.White,
    background = Color(0xFF0F1714),
    onBackground = Cream,
    surface = Color(0xFF1A2420),
    onSurface = Cream,
)

@Composable
fun DayModeTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (themeMode == ThemeMode.DARK) DarkColors else LightColors,
        content = content,
    )
}
