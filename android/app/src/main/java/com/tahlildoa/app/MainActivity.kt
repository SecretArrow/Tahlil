package com.tahlildoa.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
 *
 * Three cross-cutting behaviors are driven from here:
 *  - **System bar appearance**: the status bar / navigation bar icon
 *    appearance (light vs. dark icons) is set programmatically based on
 *    the resolved theme, with API-level guards for
 *    `isAppearanceLightStatusBars` (API 23+) and
 *    `isAppearanceLightNavigationBars` (API 26+). This replaces the
 *    XML `windowLightStatusBar` / `windowLightNavigationBar` attributes
 *    that were previously set in themes.xml — moving the logic into
 *    Kotlin lets us guard by API level at runtime and avoids any
 *    `Resources$NotFoundException` from missing theme attributes.
 *  - **Keep screen on**: when `toggles.keepScreenOn` is true, the
 *    `FLAG_KEEP_SCREEN_ON` window flag is added so the screen never
 *    sleeps while the app is in the foreground. The flag is cleared
 *    on dispose.
 *  - **Fullscreen (immersive)**: when [TahlilViewModel.isFullscreen] is
 *    true, the system bars (status + navigation) are hidden with
 *    `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE` so a swipe from the edge
 *    transiently reveals them.
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

            val window = this.window
            val view = LocalView.current

            // --- System bar appearance (light/dark icons) ----------------
            // Driven programmatically so we can guard API levels at runtime
            // and react to the user's theme preference flipping.
            //
            // - isAppearanceLightStatusBars(true) → dark icons on a light
            //   status bar (used in light theme).
            // - isAppearanceLightStatusBars(false) → light icons on a dark
            //   status bar (used in dark theme).
            //
            // WindowInsetsControllerCompat handles the API-level branching
            // for us — calling it on API < 23 is a safe no-op for status
            // bars, and on API < 26 is a safe no-op for navigation bars.
            DisposableEffect(isDark) {
                val controller = WindowInsetsControllerCompat(window, view)
                controller.isAppearanceLightStatusBars = !isDark
                controller.isAppearanceLightNavigationBars = !isDark
                onDispose { }
            }

            // --- Keep-screen-on window flag --------------------------------
            // Apply FLAG_KEEP_SCREEN_ON based on the user toggle. Using the
            // window flag (rather than View.keepScreenOn) survives config
            // changes reliably.
            DisposableEffect(uiState.toggles.keepScreenOn) {
                if (uiState.toggles.keepScreenOn) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                onDispose {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            // --- Fullscreen (immersive) system bars ---------------------------
            DisposableEffect(uiState.isFullscreen) {
                val controller = WindowInsetsControllerCompat(window, view)
                if (uiState.isFullscreen) {
                    controller.hide(WindowInsetsCompat.Type.systemBars())
                    controller.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    controller.show(WindowInsetsCompat.Type.systemBars())
                }
                onDispose {
                    // On dispose, always restore the system bars so we never
                    // strand the user in immersive mode after a config change.
                    controller.show(WindowInsetsCompat.Type.systemBars())
                }
            }

            TahlilDoaTheme(themeMode = uiState.themeMode, dynamicColor = true) {
                TahlilNavigation(
                    toggles = uiState.toggles,
                    style = uiState.style,
                    themeMode = uiState.themeMode,
                    isDark = isDark,
                    isFullscreen = uiState.isFullscreen,
                    lastReadSectionId = uiState.lastReadSectionId,
                    onTogglesChange = { newToggles -> viewModel.updateToggles { newToggles } },
                    onStyleChange = { newStyle -> viewModel.updateStyle { newStyle } },
                    onThemeModeChange = { mode -> viewModel.setThemeMode(mode) },
                    onToggleThemeClick = { viewModel.toggleTheme() },
                    onToggleFullscreen = { viewModel.toggleFullscreen() },
                    onSetLastReadSection = { id -> viewModel.setLastReadSectionId(id) },
                    onClearLastReadSection = { viewModel.setLastReadSectionId(null) },
                )
            }
        }
    }
}
