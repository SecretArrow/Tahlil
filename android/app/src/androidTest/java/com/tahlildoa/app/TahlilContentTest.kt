package com.tahlildoa.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tahlildoa.app.data.ContentVariant
import com.tahlildoa.app.data.TahlilContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Sanity checks for the embedded content table.
 *
 * These tests guard against silent regressions where the lazy
 * `TahlilContent` initialization fails (e.g. due to a ProGuard-stripped
 * `@Serializable` companion or a malformed JSON asset) — which would
 * result in an empty list at runtime and a blank home screen.
 *
 * `TahlilContent.init(context)` is invoked from `TahlilDoaApp.onCreate`,
 * so by the time instrumentation runs the parsed content is already
 * cached for the process.
 */
@RunWith(AndroidJUnit4::class)
class TahlilContentTest {
    @Test
    fun singkat_has_28_sections() {
        assertEquals(
            "Singkat variant should have 28 sections",
            28,
            TahlilContent.sectionsFor(ContentVariant.Singkat).size,
        )
    }

    @Test
    fun lengkap_has_39_sections() {
        assertEquals(
            "Lengkap variant should have 39 sections",
            39,
            TahlilContent.sectionsFor(ContentVariant.Lengkap).size,
        )
    }

    @Test
    fun firstSingkatSectionIsPengantarAlFatihah() {
        val first = TahlilContent.sectionsFor(ContentVariant.Singkat).first()
        assertEquals("pengantar-al-fatihah-untuk-rasulullah-saw", first.id)
        assertEquals(1, first.number)
        assertEquals("Pengantar Al-Fatihah untuk Rasulullah SAW", first.title)
    }

    @Test
    fun allSectionsHaveBlocks() {
        ContentVariant.values().forEach { variant ->
            TahlilContent.sectionsFor(variant).forEach { section ->
                assertTrue(
                    "Section ${section.id} (${variant.label}) should have blocks",
                    section.blocks.isNotEmpty(),
                )
            }
        }
    }

    @Test
    fun allBlocksHaveArabicText() {
        TahlilContent.sectionsFor(ContentVariant.Singkat).forEach { section ->
            section.blocks.forEach { block ->
                assertNotNull("Block in ${section.id} should have arabic", block.arabic)
                assertTrue(
                    "Block in ${section.id} should have non-empty arabic",
                    block.arabic!!.isNotEmpty(),
                )
            }
        }
    }

    @Test
    fun byIdReturnsCorrectSection() {
        val section =
            TahlilContent.byId(
                "pengantar-al-fatihah-untuk-rasulullah-saw",
                ContentVariant.Singkat,
            )
        assertNotNull(section)
        assertEquals(1, section!!.number)
    }

    @Test
    fun byIdReturnsNullForUnknownId() {
        val section = TahlilContent.byId("nonexistent", ContentVariant.Singkat)
        assertEquals(null, section)
    }

    @Test
    fun lengkap_includes_yasin_sections() {
        assertTrue(
            "Lengkap variant should include Surah Yasin sections",
            TahlilContent
                .sectionsFor(ContentVariant.Lengkap)
                .any { it.title.contains("Yasin") },
        )
    }
}
