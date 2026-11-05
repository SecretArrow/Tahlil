package com.tahlildoa.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tahlildoa.app.data.ArabicStyleSettings
import com.tahlildoa.app.data.DisplayToggles
import com.tahlildoa.app.data.TahlilContent
import com.tahlildoa.app.ui.components.SectionCard

/**
 * SectionDetailScreen — a focused view of a single [Section].
 *
 * Useful when the user taps a CTA or a TOC entry in the drawer — it shows
 * just that section's blocks (with full typography settings applied)
 * without the surrounding hero or other sections.
 *
 * The section is identified by [sectionId]. If the id doesn't match any
 * known section, the screen renders nothing (the navigation layer should
 * guard against this).
 */
@Composable
fun SectionDetailScreen(
    sectionId: String,
    toggles: DisplayToggles,
    style: ArabicStyleSettings,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val section = remember(sectionId) { TahlilContent.byId(sectionId) } ?: return
    val state = rememberLazyListState()

    LazyColumn(
        state = state,
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = section.id) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                SectionCard(
                    section = section,
                    toggles = toggles,
                    style = style,
                    isDark = isDark,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}
