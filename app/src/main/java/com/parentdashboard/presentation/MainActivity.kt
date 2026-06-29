package com.parentdashboard.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.parentdashboard.ads.AdManager
import com.parentdashboard.analytics.AnalyticsManager
import com.parentdashboard.domain.model.UserPreferences
import com.parentdashboard.domain.repository.PreferencesRepository
import com.parentdashboard.presentation.navigation.ParentDashboardNavHost
import com.parentdashboard.presentation.navigation.Screen
import com.parentdashboard.presentation.ui.theme.ParentDashboardTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var adManager: AdManager
    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        adManager.initialize()

        setContent {
            val prefs by preferencesRepository.getUserPreferences()
                .collectAsStateWithLifecycle(initialValue = null)

            if (prefs == null) {
                LoadingShell()
                return@setContent
            }

            ParentDashboardRoot(
                prefs = prefs!!,
                adManager = adManager,
                analyticsManager = analyticsManager
            )
        }
    }
}

@Composable
private fun LoadingShell() {
    ParentDashboardTheme {
        Box(Modifier.fillMaxSize())
    }
}

@Composable
private fun ParentDashboardRoot(
    prefs: UserPreferences,
    adManager: AdManager,
    analyticsManager: AnalyticsManager
) {
    LaunchedEffect(prefs.analyticsEnabled) {
        analyticsManager.setCollectionEnabled(prefs.analyticsEnabled)
    }

    LaunchedEffect(prefs.adsEnabled, prefs.personalizedAds) {
        adManager.updateAdPolicy(prefs.adsEnabled, prefs.personalizedAds)
    }

    val startDestination = when {
        !prefs.consentGiven -> Screen.Consent.route
        !prefs.onboardingCompleted -> Screen.Onboarding.route
        else -> Screen.Home.route
    }

    ParentDashboardTheme(
        appTheme = prefs.appTheme,
        highContrast = prefs.highContrastMode,
        colorBlindMode = prefs.colorBlindMode,
        fontScale = prefs.fontScale
    ) {
        val navController = rememberNavController()
        ParentDashboardNavHost(
            navController = navController,
            adManager = adManager,
            analyticsManager = analyticsManager,
            preferences = prefs,
            startDestination = startDestination
        )
    }
}
