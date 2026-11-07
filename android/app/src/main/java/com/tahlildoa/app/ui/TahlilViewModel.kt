package com.tahlildoa.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tahlildoa.app.data.ArabicStyleSettings
import com.tahlildoa.app.data.ContentVariant
import com.tahlildoa.app.data.DisplayToggles
import com.tahlildoa.app.data.Section
import com.tahlildoa.app.data.SettingsRepository
import com.tahlildoa.app.data.TahlilContent
import com.tahlildoa.app.data.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Top-level UI state — bundles all reactive settings into one StateFlow so
 * the screens can observe a single source of truth.
 *
 * - [lastReadSectionId]: persisted pointer to the last section the user opened
 *   (used by HomeScreen to show a "Continue reading" card + auto-navigate on
 *   app launch).
 * - [isFullscreen]: session-only fullscreen toggle (NOT persisted — resets to
 *   false each launch). When true, MainActivity hides the system bars via
 *   WindowInsetsControllerCompat.
 * - [variant]: persisted content variant ([ContentVariant.Singkat] /
 *   [ContentVariant.Lengkap]). Drives which set of sections the home and
 *   detail screens render.
 */
data class TahlilUiState(
    val toggles: DisplayToggles = DisplayToggles(),
    val style: ArabicStyleSettings = ArabicStyleSettings.Default,
    val themeMode: ThemeMode = ThemeMode.System,
    val isDark: Boolean = false,
    val isFullscreen: Boolean = false,
    val lastReadSectionId: String? = null,
    val variant: ContentVariant = ContentVariant.Singkat,
)

/** Internal helper so we can combine >5 flows via a nested combine(). */
private data class PersistedSettings(
    val toggles: DisplayToggles,
    val style: ArabicStyleSettings,
    val themeMode: ThemeMode,
    val lastRead: String?,
    val isDark: Boolean,
)

/**
 * ViewModel for the whole app — owns the [SettingsRepository] and exposes
 * a single [uiState] for screens to observe.
 *
 * Updates are fire-and-forget — the StateFlow is the source of truth and
 * always reflects what's persisted.
 */
class TahlilViewModel(
    private val repo: SettingsRepository,
) : ViewModel() {
    // The isDark flag is set by the screen from the system theme resolver,
    // since only Composables can call isSystemInDarkTheme().
    private val _isDark = MutableStateFlow(false)
    val isDark: StateFlow<Boolean> = _isDark.asStateFlow()

    // Session-only fullscreen flag (not persisted) — MainActivity observes
    // this and toggles the system-bars visibility via WindowInsetsControllerCompat.
    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

    private val persisted: Flow<PersistedSettings> =
        combine(
            repo.displayToggles,
            repo.arabicStyle,
            repo.themeMode,
            repo.lastReadSectionId,
            _isDark,
        ) { toggles, style, themeMode, lastRead, isDark ->
            PersistedSettings(toggles, style, themeMode, lastRead, isDark)
        }

    // Three-way combine (the 3-arg `combine` overload) threads the persisted
    // 5-flow result, the session-only fullscreen flag, and the content
    // variant into one [TahlilUiState]. Variant lives outside `persisted`
    // so the 5-arg combine stays within kotlinx-coroutines' typed arity.
    val uiState: StateFlow<TahlilUiState> =
        combine(
            persisted,
            _isFullscreen,
            repo.variant,
        ) { p, isFullscreen, variant ->
            TahlilUiState(
                toggles = p.toggles,
                style = p.style,
                themeMode = p.themeMode,
                isDark = p.isDark,
                isFullscreen = isFullscreen,
                lastReadSectionId = p.lastRead,
                variant = variant,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = TahlilUiState(),
        )

    /**
     * Variant-aware sections list. Recomputes whenever the persisted
     * variant changes, seeded with the [ContentVariant.Singkat] list so the
     * first frame is never empty before DataStore emits.
     */
    val sections: StateFlow<List<Section>> =
        repo.variant
            .map { variant -> TahlilContent.sectionsFor(variant) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = TahlilContent.sectionsFor(ContentVariant.Singkat),
            )

    fun setDark(value: Boolean) {
        _isDark.value = value
    }

    /** Toggle (or set) the immersive fullscreen mode for the current session. */
    fun setFullscreen(value: Boolean) {
        _isFullscreen.value = value
    }

    /** Convenience: flip the fullscreen flag. */
    fun toggleFullscreen() {
        _isFullscreen.value = !_isFullscreen.value
    }

    /** Persist the selected content variant (Singkat / Lengkap). */
    fun setVariant(variant: ContentVariant) {
        viewModelScope.launch { repo.updateVariant(variant) }
    }

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

    /**
     * Convenience: cycle theme modes for the quick toggle button in the
     * header. System → Light → Dark → System → … so all three modes are
     * reachable from the header without opening Settings.
     */
    fun toggleTheme() {
        val next =
            when (uiState.value.themeMode) {
                ThemeMode.System -> ThemeMode.Light
                ThemeMode.Light -> ThemeMode.Dark
                ThemeMode.Dark -> ThemeMode.System
            }
        setThemeMode(next)
    }

    /**
     * Persist the last-read section id (called whenever a section is opened).
     * Pass null to clear the pointer ("Mulai dari awal").
     */
    fun setLastReadSectionId(id: String?) {
        viewModelScope.launch { repo.setLastReadSectionId(id) }
    }

    companion object {
        fun factory(repo: SettingsRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = TahlilViewModel(repo) as T
            }
    }
}
