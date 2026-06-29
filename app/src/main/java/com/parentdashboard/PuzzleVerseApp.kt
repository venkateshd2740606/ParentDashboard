package com.parentdashboard

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.parentdashboard.ads.AdManager
import com.parentdashboard.util.LocaleHelper
import com.parentdashboard.worker.ChallengeWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ParentDashboardApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var adManager: AdManager

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        LocaleHelper.syncFromPreferences(this)
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

        val remoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig.setConfigSettingsAsync(remoteConfigSettings { minimumFetchIntervalInSeconds = 3600 })
        remoteConfig.setDefaultsAsync(
            mapOf(
                "hint_cost_coins" to 15L,
                "daily_challenge_enabled" to true,
                "interstitial_ad_interval" to 3L,
                "theme_unlock_cost" to 150L
            )
        )
        remoteConfig.fetchAndActivate()

        adManager.initialize()

        ChallengeWorker.schedule(this)
    }
}
