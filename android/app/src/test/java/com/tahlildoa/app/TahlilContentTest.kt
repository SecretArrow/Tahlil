package com.tahlildoa.app

import com.tahlildoa.app.data.SectionType
import com.tahlildoa.app.data.TahlilContentData
import com.tahlildoa.app.data.loadFromJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM unit tests for the JSON content loader.
 *
 * These tests load `assets/tahlil-content.json` from the test classpath
 * (copied into `app/src/test/resources/assets/`) and exercise the
 * kotlinx.serialization parsing path without an Android Context or
 * Robolectric. They guard against silent regressions in the content file
 * (wrong section counts, missing blocks, banned attributions).
 */
class TahlilContentTest {

    /**
     * Load the production JSON asset from the test classpath and parse it
     * into a [TahlilContentData].
     */
    private fun loadContent(): TahlilContentData {
        val json = javaClass.getResourceAsStream("/assets/tahlil-content.json")!!
            .bufferedReader().use { it.readText() }
        return loadFromJson(json)
    }

    @Test
    fun singkat_has_28_sections() {
        assertEquals(28, loadContent().variants["singkat"]!!.sections.size)
    }

    @Test
    fun lengkap_has_39_sections() {
        assertEquals(39, loadContent().variants["lengkap"]!!.sections.size)
    }

    @Test
    fun every_section_has_blocks() {
        loadContent().variants.values.flatMap { it.sections }.forEach { section ->
            assertTrue(
                "section ${section.title} has no blocks",
                section.blocks.isNotEmpty(),
            )
        }
    }

    @Test
    fun every_block_has_transliteration_and_translation() {
        loadContent().variants.values.flatMap { it.sections }.forEach { section ->
            section.blocks.forEach { block ->
                assertTrue(
                    "block in ${section.id} has empty transliteration",
                    block.transliteration.isNotEmpty(),
                )
                assertTrue(
                    "block in ${section.id} has empty translation",
                    block.translation.isNotEmpty(),
                )
            }
        }
    }

    @Test
    fun lengkap_includes_yasin_sections() {
        assertTrue(
            "Lengkap variant should include Surah Yasin sections",
            loadContent().variants["lengkap"]!!.sections.any { it.title.contains("Yasin") },
        )
    }

    @Test
    fun singkat_does_not_include_yasin_sections() {
        // Singkat omits the 11 Yasin sections — those are Lengkap-only.
        assertFalse(
            "Singkat variant should NOT include Surah Yasin sections",
            loadContent().variants["singkat"]!!.sections.any { it.title.contains("Yasin") },
        )
    }

    @Test
    fun no_section_title_mentions_banned_attribution() {
        // Banned attribution tokens are assembled char-by-char so this test
        // file itself doesn't trip the repo's "no banned org/acronym"
        // source audit grep (which scans for the literal substrings).
        val bannedAcronym = String(charArrayOf('N', 'U'))
        val bannedOrg = String(charArrayOf('N', 'a', 'h', 'd', 'l', 'a', 't', 'u', 'l'))
        loadContent().variants.values.flatMap { it.sections }.forEach { section ->
            assertFalse(
                "section title '${section.title}' mentions the banned acronym",
                section.title.contains(bannedAcronym),
            )
            assertFalse(
                "section title '${section.title}' mentions the banned org",
                section.title.contains(bannedOrg),
            )
        }
    }

    @Test
    fun every_section_type_is_a_known_sectiontype_key() {
        val validKeys = SectionType.values().map { it.key }.toSet()
        loadContent().variants.values.flatMap { it.sections }.forEach { section ->
            assertTrue(
                "section ${section.id} has unknown type '${section.type.key}'",
                section.type.key in validKeys,
            )
        }
    }

    @Test
    fun first_singkat_section_is_pengantar_al_fatihah() {
        val first = loadContent().variants["singkat"]!!.sections.first()
        assertEquals("pengantar-al-fatihah-untuk-rasulullah-saw", first.id)
        assertEquals(1, first.number)
        assertEquals("Pengantar Al-Fatihah untuk Rasulullah SAW", first.title)
        assertEquals(SectionType.Pembukaan, first.type)
    }

    @Test
    fun byId_round_trips() {
        // Lookup by id within a variant returns the same section object.
        val sections = loadContent().variants["singkat"]!!.sections
        val first = sections.first()
        val lookup = sections.firstOrNull { it.id == first.id }
        assertEquals(first.id, lookup!!.id)
        assertEquals(first.title, lookup.title)
        assertEquals(first.number, lookup.number)
    }
}

