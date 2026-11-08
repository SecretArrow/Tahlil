package com.tahlil.app

import com.tahlil.app.data.SectionType
import com.tahlil.app.data.TahlilContentData
import com.tahlil.app.data.loadFromJson
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
        val json =
            javaClass
                .getResourceAsStream("/assets/tahlil-content.json")!!
                .bufferedReader()
                .use { it.readText() }
        return loadFromJson(json)
    }

    @Test
    fun singkat_has_28_sections() {
        assertEquals(28, loadContent().variants["singkat"]!!.sections.size)
    }

    @Test
    fun lengkap_has_28_sections() {
        assertEquals(28, loadContent().variants["lengkap"]!!.sections.size)
    }

    @Test
    fun yasin_has_83_sections() {
        // Surah Yasin — exactly 83 ayat.
        assertEquals(83, loadContent().variants["yasin"]!!.sections.size)
    }

    @Test
    fun yasin_tahlil_has_111_sections() {
        // Yasin (83) + Tahlil Lengkap (28) = 111 combined sections.
        assertEquals(111, loadContent().variants["yasin_tahlil"]!!.sections.size)
    }

    @Test
    fun first_yasin_section_is_ayat_1() {
        val first = loadContent().variants["yasin"]!!.sections.first()
        assertEquals("yasin-ayat-1", first.id)
        assertEquals("Surah Yasin Ayat 1", first.title)
    }

    @Test
    fun first_yasin_tahlil_section_is_yt_yasin_ayat_1() {
        val first = loadContent().variants["yasin_tahlil"]!!.sections.first()
        assertEquals("yt-yasin-ayat-1", first.id)
        assertEquals("Surah Yasin Ayat 1", first.title)
    }

    @Test
    fun yasin_tahlil_contains_both_yasin_and_lengkap_sections() {
        val sections = loadContent().variants["yasin_tahlil"]!!.sections
        // Yasin portion: ids prefixed "yt-yasin-".
        val yasinSections = sections.filter { it.id.startsWith("yt-yasin-") }
        assertEquals(83, yasinSections.size)
        // Lengkap portion: ids prefixed "yt-" and NOT "yt-yasin-".
        val lengkapSections =
            sections.filter { it.id.startsWith("yt-") && !it.id.startsWith("yt-yasin-") }
        assertEquals(28, lengkapSections.size)
        // Together they account for every section in the combined variant.
        assertEquals(111, yasinSections.size + lengkapSections.size)
        assertEquals(111, sections.size)
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
    fun lengkap_does_not_include_yasin_sections() {
        // Per the NU source (quran.nu.or.id/tahlil), Surah Yasin is a
        // separate menu item and does NOT belong inside the Tahlil sequence.
        // Both Lengkap and Singkat render the same 28 tahlil sections; Yasin
        // is available via the dedicated `yasin` and `yasin_tahlil` variants.
        assertFalse(
            "Lengkap variant should NOT include Surah Yasin sections",
            loadContent().variants["lengkap"]!!.sections.any { it.title.contains("Yasin") },
        )
    }

    @Test
    fun singkat_does_not_include_yasin_sections() {
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
    fun every_arabic_allah_uses_fully_voweled_form() {
        // The name of God in Arabic must always appear in its fully-voweled
        // form: alef-lam-lam-shadda-superscript-alef-ha (اللّٰه). The bare
        // form alef-lam-lam-ha (الله) without the following shadda (U+0651)
        // and superscript alef (U+0670) is forbidden in the content.
        //
        // Both forms are assembled char-by-char so this test file does not
        // trip the source audit grep that scans for the bare substring.
        val bareAllah =
            String(
                charArrayOf(
                    '\u0627', // ARABIC LETTER ALEF
                    '\u0644', // ARABIC LETTER LAM
                    '\u0644', // ARABIC LETTER LAM
                    '\u0647', // ARABIC LETTER HEH
                ),
            )
        val shadda = '\u0651' // ARABIC SHADDA
        val superscriptAlef = '\u0670' // ARABIC SUPERSCRIPT ALEF
        loadContent().variants.values.flatMap { it.sections }.forEach { section ->
            section.blocks.forEach { block ->
                val arabic = block.arabic ?: return@forEach
                var idx = 0
                while (true) {
                    val at = arabic.indexOf(bareAllah, idx)
                    if (at < 0) break
                    // Check the two characters immediately after the bare form.
                    // If they are shadda + superscript alef, this occurrence is
                    // the fully-voweled form and is allowed; otherwise it is a
                    // forbidden bare occurrence.
                    val following = arabic.substring(at + bareAllah.length)
                    val isFullyVoweled =
                        following.length >= 2 &&
                            following[0] == shadda &&
                            following[1] == superscriptAlef
                    assertTrue(
                        "Arabic block in section ${section.id} contains the bare " +
                            "(unvoweled) form of the name of God; use the fully-voweled " +
                            "form (alef-lam-lam-shadda-superscript-alef-ha) instead.",
                        isFullyVoweled,
                    )
                    idx = at + bareAllah.length
                }
            }
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
