package com.tahlildoa.app.data

import kotlinx.serialization.Serializable

/**
 * Display toggles for the prayer book.
 *
 * - arabic:           Show/hide the Arabic verse text.
 * - transliteration:  Show/hide the Latin transliteration.
 * - translation:      Show/hide the Indonesian translation.
 * - punctuateArabic:  When true, inserts Arabic commas (،) before " وَ"
 *                     connectors — mirroring the punctuation already present
 *                     in the Indonesian translation. Quran verses (with ۝ or
 *                     ۩ marks) are never modified.
 * - colorizeArabic:   When true, each Arabic word is rendered in one of two
 *                     alternating shades so consecutive words are visually
 *                     distinguishable.
 * - keepScreenOn:     When true, the device screen stays awake while the app
 *                     is in the foreground (FLAG_KEEP_SCREEN_ON). Default
 *                     OFF so users don't drain their battery accidentally.
 *
 * All toggles default to true except [punctuateArabic], [colorizeArabic], and
 * [keepScreenOn], matching the web app's defaults.
 */
@Serializable
data class DisplayToggles(
    val arabic: Boolean = true,
    val transliteration: Boolean = true,
    val translation: Boolean = true,
    val punctuateArabic: Boolean = false,
    val colorizeArabic: Boolean = false,
    val keepScreenOn: Boolean = false,
) {
    companion object {
        val Default = DisplayToggles()
    }
}
