package com.tahlil.app.data

import kotlinx.serialization.Serializable

/**
 * Content variant selection.
 *
 * The prayer book ships in four flavours:
 *  - [Singkat]: a concise 28-section compilation of the core tahlil reading.
 *  - [Lengkap]: the full 28-section tahlil compilation. Identical in
 *    content to [Singkat] — Surah Yasin is NOT part of the tahlil sequence
 *    (per the NU source quran.nu.or.id/tahlil, where Yasin is a separate
 *    menu item), so it is offered via the dedicated [Yasin] and
 *    [YasinTahlil] variants.
 *  - [Yasin]: the 83 ayat of Surah Yasin as a standalone reading (sections
 *    numbered 1–83, ids `yasin-ayat-1` … `yasin-ayat-83`).
 *  - [YasinTahlil]: the [Yasin] reading (83 ayat) followed by the full
 *    [Lengkap] tahlil compilation, for a combined 111-section reading. The
 *    Yasin portion uses ids prefixed `yt-yasin-` and the tahlil portion uses
 *    ids prefixed `yt-` (and not `yt-yasin-`).
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
    Singkat("singkat", "Tahlil Singkat"),
    Lengkap("lengkap", "Tahlil Lengkap"),
    Yasin("yasin", "Yasin"),
    YasinTahlil("yasin_tahlil", "Yasin dan Tahlil"),
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
