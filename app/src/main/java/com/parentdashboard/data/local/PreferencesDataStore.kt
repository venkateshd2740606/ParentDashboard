package com.parentdashboard.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.parentdashboard.domain.model.AppTheme
import com.parentdashboard.domain.model.ColorBlindMode
import com.parentdashboard.domain.model.Difficulty
import com.parentdashboard.domain.model.UserPreferences
import com.parentdashboard.util.LocaleHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "parentdashboard_prefs")

@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val APP_THEME = stringPreferencesKey("app_theme")
        val HAPTIC = booleanPreferencesKey("haptic")
        val SOUND = booleanPreferencesKey("sound")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val COLOR_BLIND = stringPreferencesKey("color_blind")
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val TIMER = booleanPreferencesKey("timer")
        val ADS = booleanPreferencesKey("ads")
        val ONBOARDING = booleanPreferencesKey("onboarding")
        val CONSENT = booleanPreferencesKey("consent")
        val ANALYTICS = booleanPreferencesKey("analytics")
        val PERSONALIZED_ADS = booleanPreferencesKey("personalized_ads")
        val LANGUAGE = stringPreferencesKey("language")
        val UNLOCKED_THEMES = stringSetPreferencesKey("unlocked_themes")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            appTheme = runCatching { AppTheme.valueOf(prefs[Keys.APP_THEME] ?: AppTheme.SYSTEM.name) }
                .getOrDefault(AppTheme.SYSTEM),
            hapticFeedback = prefs[Keys.HAPTIC] ?: true,
            soundEnabled = prefs[Keys.SOUND] ?: true,
            reducedMotion = prefs[Keys.REDUCED_MOTION] ?: false,
            highContrastMode = prefs[Keys.HIGH_CONTRAST] ?: false,
            colorBlindMode = runCatching {
                ColorBlindMode.valueOf(prefs[Keys.COLOR_BLIND] ?: ColorBlindMode.NONE.name)
            }.getOrDefault(ColorBlindMode.NONE),
            fontScale = prefs[Keys.FONT_SCALE] ?: 1.0f,
            timerVisible = prefs[Keys.TIMER] ?: true,
            adsEnabled = prefs[Keys.ADS] ?: true,
            onboardingCompleted = prefs[Keys.ONBOARDING] ?: false,
            consentGiven = prefs[Keys.CONSENT] ?: false,
            analyticsEnabled = prefs[Keys.ANALYTICS] ?: true,
            personalizedAds = prefs[Keys.PERSONALIZED_ADS] ?: false,
            language = prefs[Keys.LANGUAGE] ?: "system",
            unlockedThemes = prefs[Keys.UNLOCKED_THEMES] ?: setOf(
                AppTheme.SYSTEM.name, AppTheme.LIGHT.name, AppTheme.DARK.name
            )
        )
    }

    suspend fun update(transform: (UserPreferences) -> UserPreferences) {
        context.dataStore.edit { prefs ->
            val current = UserPreferences(
                appTheme = runCatching { AppTheme.valueOf(prefs[Keys.APP_THEME] ?: AppTheme.SYSTEM.name) }
                    .getOrDefault(AppTheme.SYSTEM),
                hapticFeedback = prefs[Keys.HAPTIC] ?: true,
                soundEnabled = prefs[Keys.SOUND] ?: true,
                reducedMotion = prefs[Keys.REDUCED_MOTION] ?: false,
                highContrastMode = prefs[Keys.HIGH_CONTRAST] ?: false,
                colorBlindMode = runCatching {
                    ColorBlindMode.valueOf(prefs[Keys.COLOR_BLIND] ?: ColorBlindMode.NONE.name)
                }.getOrDefault(ColorBlindMode.NONE),
                fontScale = prefs[Keys.FONT_SCALE] ?: 1.0f,
                timerVisible = prefs[Keys.TIMER] ?: true,
                adsEnabled = prefs[Keys.ADS] ?: true,
                onboardingCompleted = prefs[Keys.ONBOARDING] ?: false,
                consentGiven = prefs[Keys.CONSENT] ?: false,
                analyticsEnabled = prefs[Keys.ANALYTICS] ?: true,
                personalizedAds = prefs[Keys.PERSONALIZED_ADS] ?: false,
                language = prefs[Keys.LANGUAGE] ?: "system",
                unlockedThemes = prefs[Keys.UNLOCKED_THEMES] ?: setOf(
                    AppTheme.SYSTEM.name, AppTheme.LIGHT.name, AppTheme.DARK.name
                )
            )
            val updated = transform(current)
            prefs[Keys.APP_THEME] = updated.appTheme.name
            prefs[Keys.HAPTIC] = updated.hapticFeedback
            prefs[Keys.SOUND] = updated.soundEnabled
            prefs[Keys.REDUCED_MOTION] = updated.reducedMotion
            prefs[Keys.HIGH_CONTRAST] = updated.highContrastMode
            prefs[Keys.COLOR_BLIND] = updated.colorBlindMode.name
            prefs[Keys.FONT_SCALE] = updated.fontScale
            prefs[Keys.TIMER] = updated.timerVisible
            prefs[Keys.ADS] = updated.adsEnabled
            prefs[Keys.ONBOARDING] = updated.onboardingCompleted
            prefs[Keys.CONSENT] = updated.consentGiven
            prefs[Keys.ANALYTICS] = updated.analyticsEnabled
            prefs[Keys.PERSONALIZED_ADS] = updated.personalizedAds
            prefs[Keys.LANGUAGE] = updated.language
            prefs[Keys.UNLOCKED_THEMES] = updated.unlockedThemes
            LocaleHelper.persistLanguage(context, updated.language)
        }
    }

    suspend fun getCampaignLevel(difficulty: Difficulty): Int {
        val key = intPreferencesKey("campaign_${difficulty.name}")
        return context.dataStore.data.map { it[key] ?: 1 }.first()
    }

    suspend fun advanceCampaignLevel(difficulty: Difficulty): Int {
        val key = intPreferencesKey("campaign_${difficulty.name}")
        var next = 1
        context.dataStore.edit { prefs ->
            val current = prefs[key] ?: 1
            next = current + 1
            prefs[key] = next
        }
        return next
    }
}
