package com.parentdashboard.consent

import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.parentdashboard.analytics.AnalyticsManager
import com.parentdashboard.domain.repository.PreferencesRepository
import com.parentdashboard.security.SecurityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsentManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository,
    private val securityManager: SecurityManager,
    private val analyticsManager: AnalyticsManager
) {
    suspend fun hasConsent(): Boolean =
        preferencesRepository.getUserPreferences().first().consentGiven

    suspend fun applyConsent(
        analyticsEnabled: Boolean,
        personalizedAds: Boolean
    ) {
        preferencesRepository.updatePreferences { prefs ->
            prefs.copy(
                consentGiven = true,
                analyticsEnabled = analyticsEnabled,
                personalizedAds = personalizedAds,
                adsEnabled = true
            )
        }
        securityManager.storeSecureValue(KEY_CONSENT_TIMESTAMP, System.currentTimeMillis().toString())
        securityManager.storeSecureValue(KEY_ANALYTICS, analyticsEnabled.toString())
        securityManager.storeSecureValue(KEY_PERSONALIZED_ADS, personalizedAds.toString())

        val requestConfiguration = com.google.android.gms.ads.RequestConfiguration.Builder()
            .setTagForChildDirectedTreatment(
                com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED
            )
            .build()
        MobileAds.setRequestConfiguration(requestConfiguration)
        analyticsManager.setCollectionEnabled(analyticsEnabled)
    }

    suspend fun denyAll() {
        preferencesRepository.updatePreferences { prefs ->
            prefs.copy(
                consentGiven = true,
                analyticsEnabled = false,
                personalizedAds = false,
                adsEnabled = false
            )
        }
        securityManager.storeSecureValue(KEY_CONSENT_TIMESTAMP, System.currentTimeMillis().toString())
        analyticsManager.setCollectionEnabled(false)
    }

    companion object {
        private const val KEY_CONSENT_TIMESTAMP = "consent_timestamp"
        private const val KEY_ANALYTICS = "consent_analytics"
        private const val KEY_PERSONALIZED_ADS = "consent_personalized_ads"
    }
}
