package com.tahlildoa.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tahlildoa.app.data.ThemeMode
import com.tahlildoa.app.ui.TahlilViewModel
import com.tahlildoa.app.ui.navigation.TahlilNavigation
import com.tahlildoa.app.ui.theme.TahlilDoaTheme

/**
 * MainActivity — hosts the Compose UI.
 *
 * Sets up the splash screen, wires the [TahlilViewModel] (which provides
 * the reactive UI state), and applies the [TahlilDoaTheme] based on the
 * user's chosen theme mode.
 *
 * The resolved isDark flag is published back to the ViewModel so Arabic
 * colorize logic can pick the correct color pair.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val app = remember { (application as TahlilDoaApp) }
            val viewModel: TahlilViewModel = viewModel(
                factory = TahlilViewModel.factory(app.settingsRepository)
            )
            val uiState by viewModel.uiState.collectAsState()

            // Compute the actual dark flag from the user's preference + system.
            val systemDark = isSystemInDarkTheme()
            val isDark = when (uiState.themeMode) {
                ThemeMode.System -> systemDark
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }

            // Push the resolved flag into the ViewModel so ArabicText can read it.
            LaunchedEffect(isDark) {
                viewModel.setDark(isDark)
            }

            TahlilDoaTheme(themeMode = uiState.themeMode, dynamicColor = true) {
                TahlilNavigation(
                    toggles = uiState.toggles,
                    style = uiState.style,
                    themeMode = uiState.themeMode,
                    isDark = isDark,
                    onTogglesChange = { newToggles -> viewModel.updateToggles { newToggles } },
                    onStyleChange = { newStyle -> viewModel.updateStyle { newStyle } },
                    onThemeModeChange = { mode -> viewModel.setThemeMode(mode) },
                    onToggleThemeClick = { viewModel.toggleTheme() },
                )
            }
        }
    }
}
