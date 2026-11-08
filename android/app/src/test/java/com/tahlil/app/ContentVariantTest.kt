package com.tahlil.app

import com.tahlil.app.data.ContentVariant
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [ContentVariant] storage-key resolution.
 *
 * Pure JVM — no Android dependencies. Verifies the round-trip between
 * [ContentVariant.storageKey] and [ContentVariant.fromStorageKey], plus
 * the safe-default behaviour for unknown / null keys (first launch,
 * schema downgrade, corrupted DataStore entry).
 */
class ContentVariantTest {
    @Test
    fun fromStorageKey_known() {
        assertEquals(ContentVariant.Singkat, ContentVariant.fromStorageKey("singkat"))
        assertEquals(ContentVariant.Lengkap, ContentVariant.fromStorageKey("lengkap"))
        assertEquals(ContentVariant.Yasin, ContentVariant.fromStorageKey("yasin"))
        assertEquals(ContentVariant.YasinTahlil, ContentVariant.fromStorageKey("yasin_tahlil"))
    }

    @Test
    fun fromStorageKey_null_defaults_to_singkat() {
        assertEquals(ContentVariant.Singkat, ContentVariant.fromStorageKey(null))
    }

    @Test
    fun fromStorageKey_unknown_defaults_to_singkat() {
        assertEquals(ContentVariant.Singkat, ContentVariant.fromStorageKey("xyz"))
    }

    @Test
    fun storageKey_round_trips() {
        ContentVariant.values().forEach { variant ->
            assertEquals(variant, ContentVariant.fromStorageKey(variant.storageKey))
        }
    }

    @Test
    fun has_four_variants_in_order() {
        val variants = ContentVariant.values().toList()
        assertEquals(4, variants.size)
        // The home-screen menu relies on this exact order so the 2×2 grid
        // layout (Lengkap, Singkat / Yasin, YasinTahlil) stays stable.
        assertEquals(ContentVariant.Singkat, variants[0])
        assertEquals(ContentVariant.Lengkap, variants[1])
        assertEquals(ContentVariant.Yasin, variants[2])
        assertEquals(ContentVariant.YasinTahlil, variants[3])
    }

    @Test
    fun storageKey_values_are_correct() {
        assertEquals("singkat", ContentVariant.Singkat.storageKey)
        assertEquals("lengkap", ContentVariant.Lengkap.storageKey)
        assertEquals("yasin", ContentVariant.Yasin.storageKey)
        assertEquals("yasin_tahlil", ContentVariant.YasinTahlil.storageKey)
    }

    @Test
    fun label_values_are_correct() {
        assertEquals("Tahlil Singkat", ContentVariant.Singkat.label)
        assertEquals("Tahlil Lengkap", ContentVariant.Lengkap.label)
        assertEquals("Yasin", ContentVariant.Yasin.label)
        assertEquals("Yasin dan Tahlil", ContentVariant.YasinTahlil.label)
    }
}
