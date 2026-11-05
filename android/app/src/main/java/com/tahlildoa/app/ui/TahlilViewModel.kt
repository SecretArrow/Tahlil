package com.tahlildoa.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tahlildoa.app.data.ArabicStyleSettings
import com.tahlildoa.app.data.DisplayToggles
import com.tahlildoa.app.data.SettingsRepository
import com.tahlildoa.app.data.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Top-level UI state — bundles all reactive settings into one StateFlow so
 * the screens can observe a single source of truth.
 */
data class TahlilUiState(
    val toggles: DisplayToggles = DisplayToggles(),
    val style: ArabicStyleSettings = ArabicStyleSettings.Default,
    val themeMode: ThemeMode = ThemeMode.System,
    val isDark: Boolean = false,
)

/**
 * ViewModel for the whole app — owns the [SettingsRepository] and exposes
 * a single [uiState] for screens to observe.
 *
 * Updates are fire-and-forget — the StateFlow is the source of truth and
 * always reflects what's persisted.
 */
class TahlilViewModel(private val repo: SettingsRepository) : ViewModel() {

    // The isDark flag is set by the screen from the system theme resolver,
    // since only Composables can call isSystemInDarkTheme().
    private val _isDark = MutableStateFlow(false)
    val isDark: StateFlow<Boolean> = _isDark.asStateFlow()

    val uiState: StateFlow<TahlilUiState> = combine(
        repo.displayToggles,
        repo.arabicStyle,
        repo.themeMode,
        _isDark,
    ) { toggles, style, themeMode, isDark ->
        TahlilUiState(toggles, style, themeMode, isDark)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = TahlilUiState(),
    )

    fun setDark(value: Boolean) { _isDark.value = value }

    fun updateToggles(transform: (DisplayToggles) -> DisplayToggles) {
        val next = transform(uiState.value.toggles)
        viewModelScope.launch { repo.updateDisplayToggles(next) }
    }

    fun updateStyle(transform: (ArabicStyleSettings) -> ArabicStyleSettings) {
        val next = transform(uiState.value.style)
        viewModelScope.launch { repo.updateArabicStyle(next) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repo.updateThemeMode(mode) }
    }

    /** Convenience: toggle between Light and Dark (skipping System). */
    fun toggleTheme() {
        val current = uiState.value.themeMode
        val next = when (current) {
            ThemeMode.System, ThemeMode.Light -> ThemeMode.Dark
            ThemeMode.Dark -> ThemeMode.Light
        }
        setThemeMode(next)
    }

    companion object {
        fun factory(repo: SettingsRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    TahlilViewModel(repo) as T
            }
    }
}
