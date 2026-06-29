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
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.parentdashboard.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context
) : Application.ActivityLifecycleCallbacks {
    companion object {
        const val HINTS_PER_REWARDED_AD = 3
    }
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var appOpenAd: AppOpenAd? = null
    private var loadTime = 0L
    private var gameCompletionCount = 0
    private var currentActivity: Activity? = null

    @Volatile
    private var adsEnabled = true

    @Volatile
    private var personalizedAds = true

    private val _rewardedAdReady = MutableStateFlow(false)
    val rewardedAdReady: StateFlow<Boolean> = _rewardedAdReady.asStateFlow()

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
            if (adsAllowed()) {
                loadInterstitialAd()
                loadRewardedAd()
                loadRewardedInterstitialAd()
                loadAppOpenAd()
            }
            onComplete()
        }
    }

    override fun onActivityCreated(a: Activity, b: android.os.Bundle?) {}
    override fun onActivityStarted(a: Activity) { currentActivity = a }
    override fun onActivityResumed(a: Activity) { currentActivity = a }
    override fun onActivityPaused(a: Activity) {}
    override fun onActivityStopped(a: Activity) {}
    override fun onActivitySaveInstanceState(a: Activity, o: android.os.Bundle) {}
    override fun onActivityDestroyed(a: Activity) { if (currentActivity == a) currentActivity = null }

    fun loadAppOpenAd(ctx: Context? = null) {
        if (!adsAllowed()) return
        AppOpenAd.load(ctx ?: context, BuildConfig.ADMOB_APP_OPEN_ID, buildAdRequest(),
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) { appOpenAd = ad; loadTime = Date().time }
                override fun onAdFailedToLoad(e: LoadAdError) { appOpenAd = null }
            })
    }

    fun showAppOpenAdIfAvailable(onDismissed: () -> Unit = {}) {
        val activity = currentActivity ?: return
        if (!adsAllowed() || appOpenAd == null) { onDismissed(); if (adsAllowed()) loadAppOpenAd(activity); return }
        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() { appOpenAd = null; loadAppOpenAd(activity); onDismissed() }
            override fun onAdFailedToShowFullScreenContent(e: AdError) { onDismissed() }
        }
        appOpenAd?.show(activity)
    }

    fun loadInterstitialAd(ctx: Context? = null) {
        if (!adsAllowed()) return
        InterstitialAd.load(ctx ?: context, BuildConfig.ADMOB_INTERSTITIAL_ID, buildAdRequest(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) { interstitialAd = ad }
                override fun onAdFailedToLoad(e: LoadAdError) { interstitialAd = null }
            })
    }

    fun loadRewardedAd(ctx: Context? = null) {
        if (!adsAllowed()) return
        RewardedAd.load(ctx ?: context, BuildConfig.ADMOB_REWARDED_ID, buildAdRequest(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) { rewardedAd = ad; _rewardedAdReady.value = true }
                override fun onAdFailedToLoad(e: LoadAdError) { rewardedAd = null; _rewardedAdReady.value = false }
            })
    }

    fun loadRewardedInterstitialAd(ctx: Context? = null) {
        if (!adsAllowed()) return
        RewardedInterstitialAd.load(ctx ?: context, BuildConfig.ADMOB_REWARDED_INTERSTITIAL_ID,
            buildAdRequest(), object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) { rewardedInterstitialAd = ad }
                override fun onAdFailedToLoad(e: LoadAdError) { rewardedInterstitialAd = null }
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

    fun showRewardedAd(
        activity: Activity,
        onRewarded: (Int) -> Unit,
        onFailed: () -> Unit = {},
        rewardAmount: Int = HINTS_PER_REWARDED_AD
    ) {
        if (!adsAllowed() || rewardedAd == null) { onFailed(); return }
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() { rewardedAd = null; _rewardedAdReady.value = false; loadRewardedAd(activity) }
        }
        rewardedAd?.show(activity) { onRewarded(rewardAmount) }
    }

    fun showRewardedInterstitialAd(
        activity: Activity,
        onRewarded: (Int) -> Unit,
        onFailed: () -> Unit = {},
        rewardAmount: Int = HINTS_PER_REWARDED_AD
    ) {
        if (!adsAllowed() || rewardedInterstitialAd == null) { onFailed(); return }
        rewardedInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() { rewardedInterstitialAd = null; loadRewardedInterstitialAd(activity) }
        }
        rewardedInterstitialAd?.show(activity) { onRewarded(rewardAmount) }
    }

    fun getBannerAdRequest(): AdRequest = buildAdRequest()
}
