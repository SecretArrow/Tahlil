package com.tahlildoa.app

import android.app.Application
import com.tahlildoa.app.data.SettingsRepository

/**
 * Application class — holds the singleton [SettingsRepository] so all
 * screens share the same DataStore instance.
 *
 * No Hilt/Dagger — kept simple per the project's "keep simple if too complex"
 * constraint. The repository is created lazily and exposed via the
 * application context.
 */
class TahlilDoaApp : Application() {

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        @Volatile
        private var instance: TahlilDoaApp? = null

        fun getInstance(): TahlilDoaApp =
            instance ?: throw IllegalStateException("TahlilDoaApp not yet initialized")
    }
}
