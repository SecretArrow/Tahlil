package com.tahlil.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tahlil.app.data.ContentVariant
import com.tahlil.app.data.TahlilContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
 * `TahlilContent.init(context)` is invoked from `TahlilApp.onCreate`,
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
    fun lengkap_has_28_sections() {
        assertEquals(
            "Lengkap variant should have 28 sections",
            28,
            TahlilContent.sectionsFor(ContentVariant.Lengkap).size,
        )
    }

    @Test
    fun yasin_has_83_sections() {
        assertEquals(
            "Yasin variant should have 83 sections (Surah Yasin)",
            83,
            TahlilContent.sectionsFor(ContentVariant.Yasin).size,
        )
    }

    @Test
    fun yasin_tahlil_has_111_sections() {
        assertEquals(
            "YasinTahlil variant should have 111 sections (Yasin + Lengkap)",
            111,
            TahlilContent.sectionsFor(ContentVariant.YasinTahlil).size,
        )
    }

    @Test
    fun firstYasinSectionIsAyat1() {
        val first = TahlilContent.sectionsFor(ContentVariant.Yasin).first()
        assertEquals("yasin-ayat-1", first.id)
        assertEquals("Surah Yasin Ayat 1", first.title)
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
    fun lengkap_does_not_include_yasin_sections() {
        // Per the NU source (quran.nu.or.id/tahlil), Surah Yasin is a
        // separate menu item and does NOT belong inside the Tahlil sequence.
        // Both Lengkap and Singkat render the same 28 tahlil sections; Yasin
        // is available via the dedicated Yasin / YasinTahlil variants.
        assertFalse(
            "Lengkap variant should NOT include Surah Yasin sections",
            TahlilContent
                .sectionsFor(ContentVariant.Lengkap)
                .any { it.title.contains("Yasin") },
        )
    }
}
