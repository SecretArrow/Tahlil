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
    fun has_two_variants() {
        assertEquals(2, ContentVariant.values().size)
    }
}
