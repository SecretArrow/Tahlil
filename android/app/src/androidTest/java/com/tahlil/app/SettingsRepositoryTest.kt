package com.tahlil.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tahlil.app.data.ContentVariant
import com.tahlil.app.data.SettingsRepository
import com.tahlil.app.data.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for [SettingsRepository].
 *
 * These run on a real device or emulator so they exercise the
 * actual DataStore Preferences implementation — including its
 * on-disk file. This is the layer most likely to fail silently
 * when ProGuard strips DataStore internals.
 *
 * Note: these tests share the application's DataStore file. We
 * accept the side effects because the test runner creates a fresh
 * test instance of the app on each run.
 */
@RunWith(AndroidJUnit4::class)
class SettingsRepositoryTest {
    private lateinit var repo: SettingsRepository

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        repo = SettingsRepository(context)
    }

    @After
    fun tearDown() {
        // Best-effort clean-up: reset the toggles we mutated so the
        // next test starts from defaults.
        runBlocking {
            try {
                repo.updateDisplayToggles(repo.displayToggles.first().copy(keepScreenOn = false, dailyReminder = false))
                repo.setLastReadSectionId(null)
                repo.updateVariant(ContentVariant.Singkat)
            } catch (_: Throwable) {
                // Swallow — clean-up is best-effort.
            }
        }
    }

    @Test
    fun defaultDisplayTogglesAreCorrect() =
        runBlocking {
            val toggles = repo.displayToggles.first()
            assertEquals(true, toggles.arabic)
            assertEquals(true, toggles.transliteration)
            assertEquals(true, toggles.translation)
            assertEquals(false, toggles.punctuateArabic)
            assertEquals(false, toggles.colorizeArabic)
            assertEquals(false, toggles.keepScreenOn)
            assertEquals(false, toggles.dailyReminder)
        }

    @Test
    fun defaultThemeIsSystem() =
        runBlocking {
            val mode = repo.themeMode.first()
            assertEquals(ThemeMode.System, mode)
        }

    @Test
    fun defaultVariantIsSingkat() =
        runBlocking {
            val variant = repo.variant.first()
            assertEquals(ContentVariant.Singkat, variant)
        }

    @Test
    fun updatingVariantToLengkapPersists() =
        runBlocking {
            repo.updateVariant(ContentVariant.Lengkap)
            val reloaded = repo.variant.first()
            assertEquals(ContentVariant.Lengkap, reloaded)
        }

    @Test
    fun updatingVariantToYasinPersists() =
        runBlocking {
            repo.updateVariant(ContentVariant.Yasin)
            val reloaded = repo.variant.first()
            assertEquals(ContentVariant.Yasin, reloaded)
        }

    @Test
    fun updatingVariantToYasinTahlilPersists() =
        runBlocking {
            repo.updateVariant(ContentVariant.YasinTahlil)
            val reloaded = repo.variant.first()
            assertEquals(ContentVariant.YasinTahlil, reloaded)
        }

    @Test
    fun updatingDisplayTogglesPersists() =
        runBlocking {
            val updated = repo.displayToggles.first().copy(keepScreenOn = true)
            repo.updateDisplayToggles(updated)
            val reloaded = repo.displayToggles.first()
            assertEquals(true, reloaded.keepScreenOn)
        }

    @Test
    fun updatingDailyReminderPersists() =
        runBlocking {
            val updated = repo.displayToggles.first().copy(dailyReminder = true)
            repo.updateDisplayToggles(updated)
            val reloaded = repo.displayToggles.first()
            assertEquals(true, reloaded.dailyReminder)
        }

    @Test
    fun lastReadSectionIdPersistsAndClears() =
        runBlocking {
            repo.setLastReadSectionId("pengantar-al-fatihah-untuk-rasulullah-saw")
            assertEquals(
                "pengantar-al-fatihah-untuk-rasulullah-saw",
                repo.lastReadSectionId.first(),
            )

            repo.setLastReadSectionId(null)
            assertEquals(null, repo.lastReadSectionId.first())
        }
}
