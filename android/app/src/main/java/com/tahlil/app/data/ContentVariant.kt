package com.tahlil.app.data

import kotlinx.serialization.Serializable

/**
 * Content variant selection.
 *
 * The prayer book ships in two flavours:
 *  - [Singkat]: a concise 28-section compilation of the core tahlil reading.
 *  - [Lengkap]: the full 39-section compilation that adds 11 Surah Yasin
 *    sections (numbers 15–25) on top of the [Singkat] content.
 *
 * The variant is persisted via DataStore Preferences using [storageKey], so
 * the user's choice survives across launches. [fromStorageKey] tolerates
 * unknown / null keys (e.g. on first launch or after a downgrade) by
 * falling back to [Singkat] — the safest, smallest default.
 */
@Serializable
enum class ContentVariant(
    val storageKey: String,
    val label: String,
) {
    Singkat("singkat", "Singkat"),
    Lengkap("lengkap", "Lengkap"),
    ;

    companion object {
        /**
         * Resolve a persisted storage key back to a [ContentVariant].
         *
         * Unknown and `null` keys (first launch, schema downgrade, or
         * corrupted DataStore entry) resolve to [Singkat] so the app
         * never crashes on a missing preference.
         */
        fun fromStorageKey(key: String?): ContentVariant = values().firstOrNull { it.storageKey == key } ?: Singkat
    }
}
