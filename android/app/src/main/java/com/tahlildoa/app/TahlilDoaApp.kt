package com.tahlildoa.app

import android.app.Application
import android.util.Log
import com.tahlildoa.app.data.SettingsRepository
import com.tahlildoa.app.data.TahlilContent
import com.tahlildoa.app.work.BackgroundScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Application class — holds the singleton [SettingsRepository] so all
 * screens share the same DataStore instance, parses the JSON content
 * asset, and schedules the daily tahlil reading reminder.
 *
 * No Hilt/Dagger — kept simple per the project's "keep simple if too complex"
 * constraint. The repository is created lazily and exposed via the
 * application context.
 *
 * Launch-time work is ordered defensively so that no optional subsystem
 * can force-close the app:
 *
 *  1. [TahlilCrashHandler] is installed FIRST — before anything else can
 *     throw — so every subsequent crash is logged to logcat (tag
 *     `TahlilCrash`) and persisted to `cache/last_crash.txt` for
 *     inspection on the next launch.
 *  2. [TahlilContent.init] parses `assets/tahlil-content.json` once and
 *     caches the result for the lifetime of the process. Wrapped in
 *     try/catch — a malformed asset is logged but never fatal (callers
 *     that read content will throw a clear error rather than force-close
 *     the whole app on launch).
 *  3. The daily reminder toggle is observed from a long-lived
 *     [applicationScope] coroutine. The reminder is scheduled when the
 *     user enables it and cancelled when they disable it; the scheduler
 *     itself is idempotent (`KEEP` policy) so re-scheduling is a no-op.
 *     A WorkManager init failure is logged but never fatal — the
 *     reminder is a nice-to-have, not critical functionality.
 */
class TahlilDoaApp : Application() {
    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(this)
    }

    /**
     * Long-lived scope for app-level coroutines that must outlive any
     * Activity (e.g. observing the daily-reminder toggle). Uses
     * [SupervisorJob] so one failed child doesn't cancel the others.
     */
    private val applicationScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Install the crash handler FIRST so any subsequent crash is logged.
        try {
            TahlilCrashHandler.install(cacheDir)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to install crash handler", e)
        }

        // Parse the JSON content asset once and cache it for the process.
        try {
            TahlilContent.init(this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse tahlil content asset", e)
        }

        // Observe the daily-reminder toggle for the lifetime of the process.
        // scheduleDailyReminder is idempotent (KEEP policy); the toggle just
        // gates whether the work is enqueued or cancelled.
        try {
            applicationScope.launch {
                settingsRepository.displayToggles
                    .map { it.dailyReminder }
                    .distinctUntilChanged()
                    .collect { enabled ->
                        if (enabled) {
                            BackgroundScheduler.scheduleDailyReminder(this@TahlilDoaApp)
                        } else {
                            BackgroundScheduler.cancelDailyReminder(this@TahlilDoaApp)
                        }
                    }
            }
        } catch (e: Exception) {
            // WorkManager init failure should NOT crash the app —
            // the reminder is a nice-to-have, not critical functionality.
            Log.e(TAG, "Failed to observe daily reminder toggle", e)
        }
    }

    companion object {
        private const val TAG = "TahlilDoaApp"

        @Volatile
        private var instance: TahlilDoaApp? = null

        fun getInstance(): TahlilDoaApp = instance ?: throw IllegalStateException("TahlilDoaApp not yet initialized")
    }
}
