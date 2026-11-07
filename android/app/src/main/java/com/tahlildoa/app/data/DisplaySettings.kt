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
 * - breakOnComma:     When true (default), each Arabic comma (،) and Latin
 *                     period (.) in Arabic text starts a new line — the
 *                     classic per-clause layout. When false, Arabic text
 *                     flows as a single block (only ayah marks still force
 *                     a line break, since they are real verse boundaries).
 * - colorizeArabic:   When true, each Arabic word is rendered in one of two
 *                     alternating shades so consecutive words are visually
 *                     distinguishable.
 * - keepScreenOn:     When true, the device screen stays awake while the app
 *                     is in the foreground (FLAG_KEEP_SCREEN_ON). Default
 *                     OFF so users don't drain their battery accidentally.
 * - dailyReminder:    When true, schedules a once-a-day WorkManager
 *                     notification reminding the user to read tahlil
 *                     (see `BackgroundScheduler.scheduleDailyReminder`).
 *                     Default OFF — notifications are opt-in.
 *
 * All toggles default to true except [punctuateArabic], [colorizeArabic],
 * [keepScreenOn], and [dailyReminder], matching the web app's defaults.
 * [breakOnComma] also defaults to true to preserve the historical per-clause
 * line layout.
 */
@Serializable
data class DisplayToggles(
    val arabic: Boolean = true,
    val transliteration: Boolean = true,
    val translation: Boolean = true,
    val punctuateArabic: Boolean = false,
    val breakOnComma: Boolean = true,
    val colorizeArabic: Boolean = false,
    val keepScreenOn: Boolean = false,
    val dailyReminder: Boolean = false,
) {
    companion object {
        val Default = DisplayToggles()
    }
}
