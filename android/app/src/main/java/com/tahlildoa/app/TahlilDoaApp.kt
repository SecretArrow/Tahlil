package com.tahlildoa.app

import android.app.Application
import android.util.Log
import com.tahlildoa.app.data.SettingsRepository
import com.tahlildoa.app.work.BackgroundScheduler

/**
 * Application class — holds the singleton [SettingsRepository] so all
 * screens share the same DataStore instance, and schedules the daily
 * tahlil reading reminder.
 *
 * No Hilt/Dagger — kept simple per the project's "keep simple if too complex"
 * constraint. The repository is created lazily and exposed via the
 * application context.
 *
 * The daily reminder is scheduled from [onCreate] via WorkManager's
 * `enqueueUniquePeriodicWork` with `KEEP` policy — this is idempotent
 * so re-scheduling on every app launch is safe and cheap.
 */
class TahlilDoaApp : Application() {

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Schedule the daily reading reminder — KEEP policy means it
        // won't replace an existing schedule (idempotent across launches).
        // Wrapped in a try/catch so any WorkManager initialization issue
        // is logged but doesn't crash the app on launch.
        try {
            BackgroundScheduler.scheduleDailyReminder(this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule daily reminder", e)
        }
    }

    companion object {
        private const val TAG = "TahlilDoaApp"

        @Volatile
        private var instance: TahlilDoaApp? = null

        fun getInstance(): TahlilDoaApp =
            instance ?: throw IllegalStateException("TahlilDoaApp not yet initialized")
    }
}
