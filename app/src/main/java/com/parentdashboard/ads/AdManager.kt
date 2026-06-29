
package com.parentdashboard.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.parentdashboard.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Families-compliant ads for education apps: home banner + interstitial between lessons only.
 * No app-open, rewarded, or in-lesson placements.
 */
@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context
) : Application.ActivityLifecycleCallbacks {
    private var interstitialAd: InterstitialAd? = null
    private var gameCompletionCount = 0
    private var currentActivity: Activity? = null

    @Volatile
    private var adsEnabled = true

    @Volatile
    private var personalizedAds = true

    fun updateAdPolicy(adsEnabled: Boolean, personalizedAds: Boolean) {
        this.adsEnabled = adsEnabled
        this.personalizedAds = personalizedAds
    }

    private fun adsAllowed(): Boolean = BuildConfig.ENABLE_ADS && adsEnabled

    private fun buildAdRequest(): AdRequest {
        val builder = AdRequest.Builder()
        if (!personalizedAds) {
            val extras = Bundle().apply { putString("npa", "1") }
            builder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
        }
        return builder.build()
    }

    fun initialize(onComplete: () -> Unit = {}) {
        if (!BuildConfig.ENABLE_ADS) { onComplete(); return }
        (context as Application).registerActivityLifecycleCallbacks(this)
        MobileAds.initialize(context) {
            if (adsAllowed()) loadInterstitialAd()
            onComplete()
        }
    }

    override fun onActivityCreated(a: Activity, b: Bundle?) {}
    override fun onActivityStarted(a: Activity) { currentActivity = a }
    override fun onActivityResumed(a: Activity) { currentActivity = a }
    override fun onActivityPaused(a: Activity) {}
    override fun onActivityStopped(a: Activity) {}
    override fun onActivitySaveInstanceState(a: Activity, o: Bundle) {}
    override fun onActivityDestroyed(a: Activity) { if (currentActivity == a) currentActivity = null }

    fun loadInterstitialAd(ctx: Context? = null) {
        if (!adsAllowed()) return
        InterstitialAd.load(ctx ?: context, BuildConfig.ADMOB_INTERSTITIAL_ID, buildAdRequest(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) { interstitialAd = ad }
                override fun onAdFailedToLoad(e: LoadAdError) { interstitialAd = null }
            })
    }

    fun maybeShowInterstitialAd(activity: Activity, interval: Int = 3, onDismissed: () -> Unit = {}) {
        if (!adsAllowed()) { onDismissed(); return }
        gameCompletionCount++
        if (gameCompletionCount >= interval && interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null; gameCompletionCount = 0; loadInterstitialAd(activity); onDismissed()
                }
                override fun onAdFailedToShowFullScreenContent(e: AdError) { onDismissed() }
            }
            interstitialAd?.show(activity)
        } else onDismissed()
    }

    fun getBannerAdRequest(): AdRequest = buildAdRequest()
}
