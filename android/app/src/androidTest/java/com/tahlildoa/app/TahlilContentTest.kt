package com.tahlildoa.app

import androidx.test.ext.junit.runners.AndroidJUnit4
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
 * `TahlilContent.sections` initialization fails (e.g. due to a
 * ProGuard-stripped `@Serializable` companion) — which would
 * result in an empty list at runtime and a blank home screen.
 */
@RunWith(AndroidJUnit4::class)
class TahlilContentTest {

    @Test
    fun allElevenSectionsAreLoaded() {
        assertEquals("Should have 11 sections", 11, TahlilContent.sections.size)
    }

    @Test
    fun firstSectionIsPembukaan() {
        val first = TahlilContent.sections.first()
        assertEquals("pembukaan", first.id)
        assertEquals(1, first.number)
        assertEquals("Pembukaan", first.title)
    }

    @Test
    fun allSectionsHaveBlocks() {
        TahlilContent.sections.forEach { section ->
            assertTrue(
                "Section ${section.id} should have blocks",
                section.blocks.isNotEmpty(),
            )
        }
    }

    @Test
    fun allBlocksHaveArabicText() {
        TahlilContent.sections.forEach { section ->
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
        val section = TahlilContent.byId("tahlil-ahli-kubur")
        assertNotNull(section)
        assertEquals(3, section!!.number)
    }

    @Test
    fun byIdReturnsNullForUnknownId() {
        val section = TahlilContent.byId("nonexistent")
        assertEquals(null, section)
    }
}
