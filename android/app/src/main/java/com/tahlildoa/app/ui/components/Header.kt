package com.tahlildoa.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tahlildoa.app.data.ThemeMode

/**
 * Header — top app bar with the app title and a few action buttons.
 *
 * - Back arrow (when [canNavigateBack] is true)
 * - Hamburger menu (when [canNavigateBack] is false)
 * - Fullscreen toggle (enter / exit immersive mode) — present on every screen
 *   so the user can switch to fullscreen from any view.
 * - Theme toggle (sun/moon) — cycles through System → Light → Dark.
 * - Settings gear
 *
 * A thin emerald accent bar runs under the header.
 *
 * When [isFullscreen] is true, the whole header is collapsed to a thin
 * transparent strip (the system bars are hidden by MainActivity). The
 * fullscreen toggle button stays in place so the user can exit fullscreen
 * from any screen via the Header, OR via the floating button that
 * [TahlilNavigation] overlays on top of the content.
 *
 * Insets: the app runs edge-to-edge (`enableEdgeToEdge()`), so WITHOUT any
 * compensation the header row would render UNDER the status bar — the
 * hamburger / fullscreen / theme / settings icons and the title would
 * collide with the clock and battery icons. When [isFullscreen] is false
 * we therefore pad the header by [WindowInsets.statusBars]; the surface
 * background still extends behind the status bar so there is no white
 * strip. In fullscreen the bars are hidden and the header stays truly
 * edge-to-edge.
 */
@Composable
fun Header(
    title: String,
    onMenuClick: () -> Unit,
    onThemeToggleClick: () -> Unit,
    onSettingsClick: () -> Unit,
    isDark: Boolean,
    themeMode: ThemeMode,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    modifier: Modifier = Modifier,
    canNavigateBack: Boolean = false,
    onBackClick: () -> Unit = {},
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            // Pad by the status-bar height (unless fullscreen) so the header
            // content never overlaps the notification bar. Background is
            // applied FIRST so the colored surface still extends behind the
            // status bar area instead of leaving a gap.
            .then(
                if (isFullscreen) Modifier
                else Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (canNavigateBack) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Kembali",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    } else {
                        IconButton(onClick = onMenuClick) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(primary, secondary))
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "ت",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                        )
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Fullscreen toggle — present on every screen.
                    IconButton(onClick = onToggleFullscreen) {
                        Icon(
                            imageVector = if (isFullscreen) Icons.Filled.FullscreenExit
                            else Icons.Filled.Fullscreen,
                            contentDescription = if (isFullscreen) "Keluar dari mode layar penuh"
                            else "Mode layar penuh",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(onClick = onThemeToggleClick) {
                        // Icon: show what the user would switch TO next.
                        // toggleTheme() cycles System → Light → Dark → System.
                        // In System mode we use the resolved `isDark` flag so
                        // the icon reflects what's actually on screen.
                        val nextIsDark = when (themeMode) {
                            ThemeMode.System -> !isDark   // flip the resolved system value
                            ThemeMode.Light -> true       // next is Dark
                            ThemeMode.Dark -> false       // next is System/Light
                        }
                        Icon(
                            imageVector = if (nextIsDark) Icons.Filled.DarkMode
                            else Icons.Filled.LightMode,
                            contentDescription = if (nextIsDark) "Mode gelap"
                            else "Mode terang",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Pengaturan",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            // Thin gradient accent bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(primary, MaterialTheme.colorScheme.tertiary, secondary)
                        )
                    )
            )
        }
    }
}
