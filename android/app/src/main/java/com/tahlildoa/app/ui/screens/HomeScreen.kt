package com.tahlildoa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tahlildoa.app.data.HeroInfo
import com.tahlildoa.app.data.TahlilContent
import com.tahlildoa.app.ui.components.SectionCard
import kotlinx.coroutines.flow.distinctUntilChanged

/** CTA button definition for the hero. */
private data class CtaButton(
    val sectionId: String,
    val label: String,
    val primary: Boolean,
)

private val CtaButtons = listOf(
    CtaButton("pembukaan", "Mulai", primary = true),
    CtaButton("tahlil-ahli-kubur", "Tahlil", primary = false),
    CtaButton("dzikir", "Dzikir", primary = false),
    CtaButton("doa-pembuka", "Doa", primary = false),
)

/**
 * HomeScreen — hero + scroll list of all 11 sections.
 *
 * Each section is wrapped in a `key`-anchored item so that active-section
 * tracking via [LazyListState] works (we read the first visible item index).
 *
 * The hero is the first item in the LazyColumn (so it scrolls with the
 * list, matching the web app's behavior).
 *
 * @param onSectionClick Called when a CTA button or "more" link is tapped.
 * @param onActiveSectionChange Called when the first visible section changes
 *        (used by the drawer to highlight the active entry).
 */
@Composable
fun HomeScreen(
    toggles: com.tahlildoa.app.data.DisplayToggles,
    style: com.tahlildoa.app.data.ArabicStyleSettings,
    isDark: Boolean,
    onSectionClick: (String) -> Unit,
    onActiveSectionChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val state = rememberLazyListState()
    val sections = remember { TahlilContent.sections }

    // Track the first visible section's id.
    LaunchedEffect(state) {
        snapshotFlow {
            val firstVisible = state.firstVisibleItemIndex
            // The hero takes index 0; sections start at index 1.
            if (firstVisible <= 0) sections.firstOrNull()?.id
            else sections.getOrNull(firstVisible - 1)?.id
        }.distinctUntilChanged().collect { id ->
            id?.let { onActiveSectionChange(it) }
        }
    }

    LazyColumn(
        state = state,
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "hero") {
            Hero(
                onCtaClick = onSectionClick,
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
}

/**
 * Hero — title, subtitle, description, Bismillah, and 4 CTA buttons.
 */
@Composable
private fun Hero(onCtaClick: (String) -> Unit, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary

    Box(modifier = modifier.fillMaxWidth()) {
        // Decorative background orbs
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(180.dp)
                .blur(80.dp)
                .background(primary.copy(alpha = 0.25f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(top = 60.dp)
                .size(160.dp)
                .blur(80.dp)
                .background(secondary.copy(alpha = 0.2f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Decorative top icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(primary, tertiary, secondary))
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
                style = MaterialTheme.typography.headlineSmall.copy(
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

            // CTA buttons (2x2 grid on small screens)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CtaButtons.take(2).forEach { btn ->
                    CtaChip(btn, onCtaClick, Modifier.weight(1f))
                }
            }
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CtaButtons.drop(2).forEach { btn ->
                    CtaChip(btn, onCtaClick, Modifier.weight(1f))
                }
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

@Composable
private fun CtaChip(
    btn: CtaButton,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (btn.primary) {
        Button(
            onClick = { onClick(btn.sectionId) },
            modifier = modifier.height(48.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(btn.label, fontWeight = FontWeight.SemiBold)
        }
    } else {
        OutlinedButton(
            onClick = { onClick(btn.sectionId) },
            modifier = modifier.height(48.dp),
            shape = RoundedCornerShape(50),
        ) {
            Text(btn.label, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Footer shown at the bottom of the home list. */
@Composable
private fun Footer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.primary,
                            Color.Transparent,
                        )
                    )
                )
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Tahlil & Doa",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )
        Text(
            text = "v1.0.0",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
        )
    }
}
