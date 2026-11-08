package com.tahlil.app.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tahlil.app.BuildConfig
import com.tahlil.app.data.ContentVariant
import com.tahlil.app.data.HeroInfo
import com.tahlil.app.data.Section
import com.tahlil.app.ui.components.SectionCard
import com.tahlil.app.ui.components.contentMaxWidth
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * One entry in the variant menu shown on the home screen.
 *
 * @param variant The [ContentVariant] this card selects.
 * @param icon Material icon shown at the top of the card.
 * @param description Short human-readable summary of the variant (e.g.
 *        "28 bagian, tanpa Surah Yasin") shown under the label.
 */
private data class VariantMenuEntry(
    val variant: ContentVariant,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val description: String,
)

// The four variant menu cards, in display order. Each entry pairs the enum
// with the Material icon and a one-line description used on the card. The
// card's label comes from [ContentVariant.label] so this list stays in
// sync with the enum.
private val VariantMenu =
    listOf(
        VariantMenuEntry(
            variant = ContentVariant.Lengkap,
            icon = Icons.Filled.MenuBook,
            description = "28 bagian, tanpa Surah Yasin",
        ),
        VariantMenuEntry(
            variant = ContentVariant.Singkat,
            icon = Icons.Filled.MenuBook,
            description = "28 bagian",
        ),
        VariantMenuEntry(
            variant = ContentVariant.Yasin,
            icon = Icons.Filled.AutoStories,
            description = "Surah Yasin 83 ayat",
        ),
        VariantMenuEntry(
            variant = ContentVariant.YasinTahlil,
            icon = Icons.Filled.LibraryBooks,
            description = "Yasin + Tahlil Lengkap",
        ),
    )

/**
 * HomeScreen — hero + variant menu + scroll list of all sections.
 *
 * Each section is wrapped in a `key`-anchored item so that active-section
 * tracking via [LazyListState] works (we read the first visible item index).
 *
 * The hero is the first item in the LazyColumn (so it scrolls with the
 * list, matching the web app's behavior).
 *
 * Features:
 *  - **Variant menu**: a 2×2 grid of large cards (one per [ContentVariant])
 *    lets the user pick which compilation to read. The selected card is
 *    highlighted with the primary color; the section list below updates
 *    to reflect the new variant.
 *  - **Mulai Membaca button**: below the menu grid, a button that jumps
 *    to the first section of the active variant (via [onSectionClick]).
 *  - **BackHandler**: pressing the back button on the home screen shows a
 *    confirmation dialog ("Keluar dari aplikasi?") with Batal / Keluar
 *    buttons. If the app is currently in fullscreen mode, the first back
 *    press exits fullscreen instead of showing the dialog.
 *  - **Reading progress bar**: a thin emerald `LinearProgressIndicator`
 *    rendered on top of the screen tracks the scroll position of the
 *    LazyColumn.
 *  - **Back-to-top FAB**: appears once the user scrolls past ~2 viewports
 *    and animates the LazyColumn back to the top on tap.
 *  - **Continue reading card**: when [lastReadSectionId] is set, a small
 *    card is inserted above the hero offering to jump straight back to
 *    the last section the user opened. A "Mulai dari awal" affordance
 *    clears the pointer.
 *
 * @param variant The currently-selected content variant (drives the menu
 *        highlight and the "Mulai Membaca" target).
 * @param onVariantChange Called when the user taps one of the variant
 *        cards. The section list below re-derives from the new variant.
 * @param onSectionClick Called when the "Mulai Membaca" button or a "more"
 *        link is tapped.
 * @param onActiveSectionChange Called when the first visible section changes
 *        (used by the drawer to highlight the active entry).
 * @param isFullscreen The current immersive-mode flag — when true, the back
 *        button exits fullscreen first instead of showing the exit dialog.
 * @param onExitFullscreen Called to leave fullscreen mode.
 * @param lastReadSectionId The id of the section the user last opened (or null).
 * @param onContinueReading Called when the user taps the continue-reading card.
 * @param onClearLastRead Called when the user taps "Mulai dari awal".
 */
@Composable
fun HomeScreen(
    toggles: com.tahlil.app.data.DisplayToggles,
    style: com.tahlil.app.data.ArabicStyleSettings,
    isDark: Boolean,
    variant: ContentVariant,
    onVariantChange: (ContentVariant) -> Unit,
    sections: List<Section>,
    onSectionClick: (String) -> Unit,
    onActiveSectionChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    isFullscreen: Boolean = false,
    onExitFullscreen: () -> Unit = {},
    lastReadSectionId: String? = null,
    onContinueReading: (String) -> Unit = {},
    onClearLastRead: () -> Unit = {},
) {
    val state = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Track the first visible section's id (for drawer highlighting).
    LaunchedEffect(state) {
        snapshotFlow {
            val firstVisible = state.firstVisibleItemIndex
            // The hero takes index 0; sections start at index 1.
            if (firstVisible <= 0) {
                sections.firstOrNull()?.id
            } else {
                sections.getOrNull(firstVisible - 1)?.id
            }
        }.distinctUntilChanged().collect { id ->
            id?.let { onActiveSectionChange(it) }
        }
    }

    // --- Exit dialog state ------------------------------------------------
    var showExitDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    BackHandler(enabled = true) {
        if (isFullscreen) {
            // First back press exits fullscreen; subsequent press shows dialog.
            onExitFullscreen()
        } else {
            showExitDialog = true
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Keluar dari aplikasi?") },
            text = { Text("Apakah Anda yakin ingin keluar dari Tahlil?") },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    // Finish the host activity — closes the app to the home
                    // screen (matches the system "back" gesture).
                    (context as? Activity)?.finish()
                }) { Text("Keluar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("Batal") }
            },
        )
    }

    // --- Reading progress (scroll-based) ----------------------------------
    val progress by remember {
        derivedStateOf {
            val total = state.layoutInfo.totalItemsCount
            if (total == 0) {
                0f
            } else {
                val first = state.firstVisibleItemIndex.toFloat()
                // Cap at 1f — once the user reaches the last item, the bar
                // is full even if the offset scrolls past it.
                (first / (total - 1).coerceAtLeast(1)).coerceIn(0f, 1f)
            }
        }
    }

    // --- Show back-to-top FAB once the user scrolls past ~2 viewports ------
    val showBackToTop by remember {
        derivedStateOf {
            // Show when firstVisibleItemIndex >= 2 OR when scrolled far
            // enough on the first item.
            state.firstVisibleItemIndex >= 2 ||
                (
                    state.firstVisibleItemIndex == 1 &&
                        state.firstVisibleItemScrollOffset > 400
                )
        }
    }

    // --- Bottom system-bar inset ------------------------------------------
    // The app is edge-to-edge, so the bottom of the list (the footer) and
    // the back-to-top FAB would otherwise sit UNDER the Android navigation
    // bar (gesture pill or 3-button bar). The navigationBars inset is read
    // in BOTH modes: in fullscreen the bars are hidden and it reads 0, so
    // the content stays truly edge-to-edge — but if the bars are ever
    // visible again (transient swipe-reveal, an OEM ROM that ignores the
    // hide call, or the exit transition) the padding re-applies and neither
    // the list nor the FAB can ever collide with the system menu.
    val density = LocalDensity.current
    val navBarBottomDp = with(density) { WindowInsets.navigationBars.getBottom(this).toDp() }

    Box(modifier = modifier.fillMaxSize()) {
        // Card list capped at CONTENT_MAX_WIDTH and centered — on phones it
        // uses the full width; on tablets/desktop the list stays a comfortable
        // reading width instead of stretching edge to edge.
        LazyColumn(
            state = state,
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .contentMaxWidth()
                    .fillMaxSize(),
            contentPadding =
                PaddingValues(
                    bottom = contentPadding.calculateBottomPadding() + navBarBottomDp + 8.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Continue reading card (only if a last-read section exists).
            val lastSection =
                lastReadSectionId?.let { id ->
                    sections.firstOrNull { it.id == id }
                }
            if (lastSection != null) {
                item(key = "continue_reading") {
                    ContinueReadingCard(
                        sectionTitle = lastSection.title,
                        sectionNumber = lastSection.number,
                        onContinue = { onContinueReading(lastSection.id) },
                        onClear = onClearLastRead,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }

            item(key = "hero") {
                Hero(
                    variant = variant,
                    onVariantChange = onVariantChange,
                    onSectionClick = onSectionClick,
                    firstSectionId = sections.firstOrNull()?.id,
                )
            }
            items(items = sections, key = { it.id }) { section ->
                SectionCard(
                    section = section,
                    toggles = toggles,
                    style = style,
                    isDark = isDark,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item(key = "footer") {
                Footer()
            }
        }

        // Thin reading progress bar pinned to the top of the screen.
        LinearProgressIndicator(
            progress = { progress },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.TopCenter),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        )

        // Back-to-top FAB
        AnimatedVisibility(
            visible = showBackToTop,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    // Keep the FAB clear of the Android navigation bar in EVERY
                    // mode — the inset reads 0 while the bars are hidden in
                    // fullscreen, so it only takes space when the bar is visible.
                    .navigationBarsPadding()
                    .padding(end = 16.dp, bottom = 16.dp),
        ) {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        state.scrollToItem(0)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = "Kembali ke atas",
                )
            }
        }
    }
}

/**
 * Continue-reading card — appears above the hero when the user has a
 * last-read section saved. Tapping it navigates straight to that section;
 * the "Mulai dari awal" affordance clears the pointer.
 */
@Composable
private fun ContinueReadingCard(
    sectionTitle: String,
    sectionNumber: Int,
    onContinue: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            ),
        onClick = onContinue,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Lanjutkan membaca",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "$sectionNumber. $sectionTitle",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            TextButton(onClick = onClear) {
                Text(
                    text = "Mulai dari awal",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

/**
 * Hero — title, subtitle, description, Bismillah, the 2×2 variant menu
 * grid, and a "Mulai Membaca" button that jumps to the first section of
 * the active variant.
 *
 * The variant grid highlights the currently-selected card (primary
 * background + onPrimary content); non-selected cards use the surface
 * color. Tapping a card calls [onVariantChange] — it does NOT navigate
 * to a section directly, so the section list below stays in sync with
 * the new selection.
 *
 * The "Mulai Membaca" button calls [onSectionClick] with the active
 * variant's first section id (passed in as [firstSectionId]); it is
 * disabled when the variant has no sections (defensive — should not
 * happen in practice).
 */
@Composable
private fun Hero(
    variant: ContentVariant,
    onVariantChange: (ContentVariant) -> Unit,
    onSectionClick: (String) -> Unit,
    firstSectionId: String?,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary

    Box(modifier = modifier.fillMaxWidth()) {
        // Decorative background orbs
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .size(180.dp)
                    .blur(80.dp)
                    .background(primary.copy(alpha = 0.25f), CircleShape),
        )
        Box(
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(top = 60.dp)
                    .size(160.dp)
                    .blur(80.dp)
                    .background(secondary.copy(alpha = 0.2f), CircleShape),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Decorative top icon
            Box(
                modifier =
                    Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(primary, tertiary, secondary)),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "ت",
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = HeroInfo.title,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Text(
                text = HeroInfo.subtitle,
                style = MaterialTheme.typography.titleMedium,
                color = primary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            // Bismillah
            Text(
                text = HeroInfo.arabicBismillah,
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            )

            Text(
                text = HeroInfo.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(Modifier.height(20.dp))

            // Variant menu — 2×2 grid of large cards. The selected variant
            // uses primary background + onPrimary content; the rest use the
            // surface color. Tapping a card calls onVariantChange (NO direct
            // section navigation — the list below re-derives from the new
            // variant).
            //
            // Layout: two Rows of two cards each, each card taking equal
            // weight(1f). Matches the previous 2×2 CTA chip layout so the
            // hero width stays the same on every screen size.
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                VariantMenu.take(2).forEach { entry ->
                    VariantCard(
                        entry = entry,
                        selected = entry.variant == variant,
                        onClick = { onVariantChange(entry.variant) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                VariantMenu.drop(2).forEach { entry ->
                    VariantCard(
                        entry = entry,
                        selected = entry.variant == variant,
                        onClick = { onVariantChange(entry.variant) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // "Mulai Membaca" — jumps to the first section of the active
            // variant. Disabled (no-op) when the variant has no sections.
            Button(
                onClick = { firstSectionId?.let(onSectionClick) },
                enabled = firstSectionId != null,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(50),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            ) {
                Text("Mulai Membaca", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Sumber: ${HeroInfo.source}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            )
        }
    }
}

/**
 * One card in the 2×2 variant menu grid. Shows the variant's icon, label,
 * and short description. The selected card uses primary + onPrimary; the
 * non-selected card uses surface + onSurface.
 */
@Composable
private fun VariantCard(
    entry: VariantMenuEntry,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor =
        if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surface
        }
    val contentColor =
        if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    val subtleColor =
        if (selected) {
            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

    Card(
        modifier = modifier.height(132.dp),
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
        onClick = onClick,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = entry.icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(28.dp),
            )
            Text(
                text = entry.variant.label,
                style =
                    MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                color = contentColor,
            )
            Text(
                text = entry.description,
                style = MaterialTheme.typography.bodySmall,
                color = subtleColor,
            )
        }
    }
}

/**
 * Footer shown at the bottom of the home list and the section reading view.
 * Internal (not private) so [SectionDetailScreen] can reuse it as an
 * end-cap. The version line reads the real versionName from BuildConfig so
 * it always matches the release instead of drifting like the old hardcoded
 * "v1.0.0".
 */
@Composable
internal fun Footer(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .width(80.dp)
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.primary,
                                Color.Transparent,
                            ),
                        ),
                    ),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Tahlil",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )
        Text(
            text = "v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
        )
    }
}
