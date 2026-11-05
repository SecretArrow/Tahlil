package com.tahlildoa.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.tahlildoa.app.data.ThemeMode

/**
 * App theme — Material 3 (Material You) with custom emerald/gold palette
 * matching the web app.
 *
 * - When [dynamicColor] is true and the device supports it (Android 12+),
 *   the system palette is used so the app matches the user's wallpaper.
 * - Otherwise the custom emerald/gold palette is used.
 * - Dark mode follows [themeMode] with a System fallback.
 */
private val LightColors = lightColorScheme(
    primary = Emerald40,
    onPrimary = Color.White,
    primaryContainer = Emerald90,
    onPrimaryContainer = Emerald10,
    secondary = Amber50,
    onSecondary = Color.White,
    secondaryContainer = Amber90,
    onSecondaryContainer = Amber10,
    tertiary = Teal40,
    onTertiary = Color.White,
    tertiaryContainer = Teal90,
    onTertiaryContainer = Teal10,
    error = Rose40,
    onError = Color.White,
    errorContainer = Rose90,
    onErrorContainer = Rose10,
    background = CreamBackground,
    onBackground = Color(0xFF1A1C19),
    surface = CreamSurface,
    onSurface = Color(0xFF1A1C19),
    surfaceVariant = CreamSurfaceVariant,
    onSurfaceVariant = Color(0xFF4A4639),
    outline = CreamOutline,
    outlineVariant = Color(0xFFD9CFB9),
    scrim = Color.Black,
)

private val DarkColors = darkColorScheme(
    primary = Emerald80,
    onPrimary = OnEmeraldDark,
    primaryContainer = Emerald30,
    onPrimaryContainer = Emerald90,
    secondary = Amber80,
    onSecondary = OnAmberDark,
    secondaryContainer = Amber30,
    onSecondaryContainer = Amber90,
    tertiary = Teal80,
    onTertiary = OnTealDark,
    tertiaryContainer = Teal30,
    onTertiaryContainer = Teal90,
    error = Rose80,
    onError = Rose20,
    errorContainer = Rose30,
    onErrorContainer = Rose90,
    background = DarkBackground,
    onBackground = Color(0xFFE8E3D6),
    surface = DarkSurface,
    onSurface = Color(0xFFE8E3D6),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFB3CABE),
    outline = DarkOutline,
    outlineVariant = Color(0xFF2D4439),
    scrim = Color.Black,
)

@Composable
fun TahlilDoaTheme(
    themeMode: ThemeMode = ThemeMode.System,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.System -> systemDark
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
