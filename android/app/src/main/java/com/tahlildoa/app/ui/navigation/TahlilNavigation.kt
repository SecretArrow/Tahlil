package com.tahlildoa.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tahlildoa.app.data.ArabicStyleSettings
import com.tahlildoa.app.data.DisplayToggles
import com.tahlildoa.app.data.TahlilContent
import com.tahlildoa.app.data.ThemeMode
import com.tahlildoa.app.ui.components.Header
import com.tahlildoa.app.ui.screens.HomeScreen
import com.tahlildoa.app.ui.screens.SectionDetailScreen
import com.tahlildoa.app.ui.screens.SettingsScreen
import kotlinx.coroutines.launch

/** All navigation routes — keep them in one place so they're easy to audit. */
object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val SECTION = "section/{sectionId}"

    fun section(id: String) = "section/$id"
}

/**
 * Top-level navigation host.
 *
 * Three destinations:
 *  - [Routes.HOME]       — the hero + scrollable list of all 11 sections
 *  - [Routes.SECTION]    — a focused single-section view (reached from a
 *                          CTA button or a drawer entry)
 *  - [Routes.SETTINGS]   — display & typography settings
 *
 * The drawer is mounted at this level so it's available from any screen.
 */
@Composable
fun TahlilNavigation(
    toggles: DisplayToggles,
    style: ArabicStyleSettings,
    themeMode: ThemeMode,
    isDark: Boolean,
    onTogglesChange: (DisplayToggles) -> Unit,
    onStyleChange: (ArabicStyleSettings) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onToggleThemeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    var activeSectionId by remember { mutableStateOf(TahlilContent.sections.firstOrNull()?.id ?: "") }
    val drawerState = androidx.compose.material3.rememberDrawerState(
        initialValue = androidx.compose.material3.DrawerValue.Closed
    )
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    val openDrawer: () -> Unit = {
        scope.launch { drawerState.open() }
    }
    val closeDrawer: () -> Unit = {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                activeSectionId = activeSectionId,
                onSectionClick = { id ->
                    closeDrawer()
                    navController.navigate(Routes.section(id)) {
                        launchSingleTop = true
                    }
                },
                onHomeClick = {
                    closeDrawer()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onSettingsClick = {
                    closeDrawer()
                    navController.navigate(Routes.SETTINGS) { launchSingleTop = true }
                },
            )
        },
        modifier = modifier,
    ) {
        val currentRoute = androidx.navigation.compose.currentBackStackEntryAsState()
            .value?.destination?.route
        val canNavigateBack = currentRoute != null && currentRoute != Routes.HOME

        androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxHeight()) {
            Header(
                title = "Tahlil & Doa",
                isDark = isDark,
                onMenuClick = openDrawer,
                onThemeToggleClick = onToggleThemeClick,
                onSettingsClick = { navController.navigate(Routes.SETTINGS) { launchSingleTop = true } },
                canNavigateBack = canNavigateBack,
                onBackClick = { navController.popBackStack() },
            )

            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier.weight(1f),
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        toggles = toggles,
                        style = style,
                        isDark = isDark,
                        onSectionClick = { id ->
                            navController.navigate(Routes.section(id)) { launchSingleTop = true }
                        },
                        onActiveSectionChange = { id -> activeSectionId = id },
                    )
                }
                composable(
                    route = Routes.SECTION,
                    arguments = listOf(navArgument("sectionId") { type = NavType.StringType }),
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("sectionId").orEmpty()
                    SectionDetailScreen(
                        sectionId = id,
                        toggles = toggles,
                        style = style,
                        isDark = isDark,
                    )
                    activeSectionId = id
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        toggles = toggles,
                        style = style,
                        themeMode = themeMode,
                        onTogglesChange = onTogglesChange,
                        onStyleChange = onStyleChange,
                        onThemeModeChange = onThemeModeChange,
                    )
                }
            }
        }
    }
}

/**
 * Drawer (Table of Contents) — shows the app logo at the top, a "Home" /
 * "Settings" pair of shortcuts, then a scrollable list of all 11 sections.
 *
 * Tapping a section navigates to [Routes.SECTION] for that id; the
 * active section is highlighted.
 */
@Composable
private fun DrawerContent(
    activeSectionId: String,
    onSectionClick: (String) -> Unit,
    onHomeClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    ModalDrawerSheet(
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
    ) {
        // Header — logo + app name
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.secondary,
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "ت",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Tahlil & Doa",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        text = "Majelis Tahlil & Doa",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        // Quick links
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            NavigationDrawerItem(
                label = { Text("Beranda") },
                selected = false,
                onClick = onHomeClick,
                icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                colors = NavigationDrawerItemDefaults.colors(),
            )
            NavigationDrawerItem(
                label = { Text("Pengaturan") },
                selected = false,
                onClick = onSettingsClick,
                icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                colors = NavigationDrawerItemDefaults.colors(),
            )
        }

        Spacer(Modifier.height(8.dp))

        // Section label
        Text(
            text = "Daftar Isi",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
        )

        // Section list — scrollable, max height to prevent overflow
        val sections = remember { TahlilContent.sections }
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            items(items = sections, key = { it.id }) { section ->
                val isActive = section.id == activeSectionId
                NavigationDrawerItem(
                    label = {
                        Column {
                            Text(
                                text = "${section.number}. ${section.title}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            if (!section.subtitle.isNullOrEmpty()) {
                                Text(
                                    text = section.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    },
                    selected = isActive,
                    onClick = { onSectionClick(section.id) },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = section.number.toString(),
                                color = if (isActive) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            )
                        }
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    ),
                )
            }
        }
    }
}
