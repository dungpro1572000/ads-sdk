package com.yourcompany.adssdk.provider.admob

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.yourcompany.adssdk.config.AdsConfig
import com.yourcompany.adssdk.config.AdSize
import com.yourcompany.adssdk.core.*
import com.yourcompany.adssdk.provider.base.BaseAdsProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AdMobProvider : BaseAdsProvider() {

    override val providerName: String = "AdMob"

    // Ad instances
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var nativeAd: NativeAd? = null
    private var bannerAdView: AdView? = null

    // ============ INITIALIZATION ============

    override suspend fun initialize(context: Context, config: AdsConfig): Result<Boolean> {
        return suspendCancellableCoroutine { continuation ->
            try {
                this.config = config

                // Configure test devices
                if (config.testMode && config.testDeviceIds.isNotEmpty()) {
                    val testConfig = RequestConfiguration.Builder()
                        .setTestDeviceIds(config.testDeviceIds)
                        .build()
                    MobileAds.setRequestConfiguration(testConfig)
                }

                // Initialize AdMob SDK
                MobileAds.initialize(context) { initStatus ->
                    val statusMap = initStatus.adapterStatusMap
                    log("AdMob initialized. Adapters: ${statusMap.keys}")

                    isInitialized = true
                    continuation.resume(Result.success(true))
                }
            } catch (e: Exception) {
                logError("Failed to initialize AdMob", e)
                continuation.resume(Result.failure(e))
            }
        }
    }

    // ============ BANNER ADS ============

    override fun loadBannerAd(
        adUnitId: String,
        adSize: AdSize,
        callback: BannerAdCallback
    ) {
        checkPreconditions()?.let {
            callback.onAdFailedToLoad(it)
            return
        }

        _bannerState.value = AdState.Loading

        // Banner loading được xử lý trong AdMobBannerAd.kt
        // Vì Banner cần View, nên việc load sẽ được thực hiện khi add vào Composable/XML
        callback.onAdLoaded()
        _bannerState.value = AdState.Loaded
    }

    /**
     * Tạo AdView cho Banner
     */
    fun createBannerAdView(
        context: Context,
        adUnitId: String,
        adSize: AdSize,
        callback: BannerAdCallback
    ): AdView {
        val adView = AdView(context).apply {
            this.adUnitId = adUnitId
            setAdSize(adSize.toAdMobSize(context))

            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    log("Banner ad loaded")
                    _bannerState.value = AdState.Loaded
                    callback.onAdLoaded()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    logError("Banner ad failed to load: ${error.message}")
                    val adError = error.toAdError()
                    _bannerState.value = AdState.Error(adError)
                    callback.onAdFailedToLoad(adError)
                }

                override fun onAdClicked() {
                    callback.onAdClicked()
                }

                override fun onAdImpression() {
                    callback.onAdImpression()
                }

                override fun onAdOpened() {
                    callback.onAdOpened()
                }

                override fun onAdClosed() {
                    callback.onAdClosed()
                }
            }
        }

        bannerAdView = adView

        // Load ad
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        return adView
    }

    // ============ INTERSTITIAL ADS ============

    override fun loadInterstitialAd(
        activity: Activity,
        adUnitId: String,
        callback: InterstitialAdCallback
    ) {
        checkPreconditions()?.let {
            callback.onAdFailedToLoad(it)
            return
        }

        _interstitialState.value = AdState.Loading
        log("Loading interstitial ad...")

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            activity,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    log("Interstitial ad loaded")
                    interstitialAd = ad
                    _interstitialState.value = AdState.Loaded
                    callback.onAdLoaded()

                    // Set fullscreen callback
                    ad.fullScreenContentCallback = createInterstitialCallback(callback)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    logError("Interstitial ad failed to load: ${error.message}")
                    interstitialAd = null
                    val adError = error.toAdError()
                    _interstitialState.value = AdState.Error(adError)
                    callback.onAdFailedToLoad(adError)
                }
            }
        )
    }

    override fun showInterstitialAd(activity: Activity, callback: InterstitialAdCallback) {
        when {
            !isInitialized -> {
                callback.onAdFailedToShow(AdError.NotInitializedError())
            }
            interstitialAd == null -> {
                callback.onAdFailedToShow(AdError.AdNotReadyError())
            }
            !canShowInterstitial() -> {
                callback.onAdFailedToShow(
                    AdError.ShowError(
                        code = -4,
                        message = "Cooldown not finished"
                    )
                )
            }
            else -> {
                _interstitialState.value = AdState.Showing
                interstitialAd?.fullScreenContentCallback = createInterstitialCallback(callback)
                interstitialAd?.show(activity)
            }
        }
    }

    override fun isInterstitialAdReady(): Boolean = interstitialAd != null

    private fun createInterstitialCallback(callback: InterstitialAdCallback): FullScreenContentCallback {
        return object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                log("Interstitial ad showed")
                callback.onAdShowed()
            }

            override fun onAdDismissedFullScreenContent() {
                log("Interstitial ad dismissed")
                lastInterstitialShowTime = System.currentTimeMillis()
                interstitialAd = null
                _interstitialState.value = AdState.Idle
                callback.onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                logError("Interstitial ad failed to show: ${error.message}")
                interstitialAd = null
                val adError = AdError.ShowError(
                    code = error.code,
                    message = error.message
                )
                _interstitialState.value = AdState.Error(adError)
                callback.onAdFailedToShow(adError)
            }

            override fun onAdImpression() {
                callback.onAdImpression()
            }

            override fun onAdClicked() {
                callback.onAdClicked()
            }
        }
    }

    // ============ REWARDED ADS ============

    override fun loadRewardedAd(
        activity: Activity,
        adUnitId: String,
        callback: RewardedAdCallback
    ) {
        checkPreconditions()?.let {
            callback.onAdFailedToLoad(it)
            return
        }

        _rewardedState.value = AdState.Loading
        log("Loading rewarded ad...")

        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            activity,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    log("Rewarded ad loaded")
                    rewardedAd = ad
                    _rewardedState.value = AdState.Loaded
                    callback.onAdLoaded()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    logError("Rewarded ad failed to load: ${error.message}")
                    rewardedAd = null
                    val adError = error.toAdError()
                    _rewardedState.value = AdState.Error(adError)
                    callback.onAdFailedToLoad(adError)
                }
            }
        )
    }

    override fun showRewardedAd(activity: Activity, callback: RewardedAdCallback) {
        when {
            !isInitialized -> {
                callback.onAdFailedToShow(AdError.NotInitializedError())
            }
            rewardedAd == null -> {
                callback.onAdFailedToShow(AdError.AdNotReadyError())
            }
            else -> {
                _rewardedState.value = AdState.Showing

                rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdShowedFullScreenContent() {
                        log("Rewarded ad showed")
                        callback.onAdShowed()
                    }

                    override fun onAdDismissedFullScreenContent() {
                        log("Rewarded ad dismissed")
                        rewardedAd = null
                        _rewardedState.value = AdState.Idle
                        callback.onAdDismissed()
                    }

                    override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                        logError("Rewarded ad failed to show: ${error.message}")
                        rewardedAd = null
                        val adError = AdError.ShowError(
                            code = error.code,
                            message = error.message
                        )
                        _rewardedState.value = AdState.Error(adError)
                        callback.onAdFailedToShow(adError)
                    }

                    override fun onAdImpression() {
                        callback.onAdImpression()
                    }

                    override fun onAdClicked() {
                        callback.onAdClicked()
                    }
                }

                rewardedAd?.show(activity) { rewardItem ->
                    log("User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                    callback.onUserEarnedReward(
                        AdReward(
                            type = rewardItem.type,
                            amount = rewardItem.amount
                        )
                    )
                }
            }
        }
    }

    override fun isRewardedAdReady(): Boolean = rewardedAd != null

    // ============ NATIVE ADS ============

    override fun loadNativeAd(
        context: Context,
        adUnitId: String,
        callback: NativeAdCallback
    ) {
        checkPreconditions()?.let {
            callback.onAdFailedToLoad(it)
            return
        }

        _nativeState.value = AdState.Loading
        log("Loading native ad...")

        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad ->
                // Destroy previous ad
                nativeAd?.destroy()
                nativeAd = ad

                log("Native ad loaded")
                _nativeState.value = AdState.Loaded

                val nativeAdData = NativeAdData(
                    headline = ad.headline,
                    body = ad.body,
                    callToAction = ad.callToAction,
                    icon = ad.icon?.drawable,
                    mediaContent = ad.mediaContent,
                    advertiser = ad.advertiser,
                    price = ad.price,
                    store = ad.store,
                    starRating = ad.starRating,
                    originalAd = ad
                )

                callback.onNativeAdLoaded(nativeAdData)
            }
            .withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    callback.onAdLoaded()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    logError("Native ad failed to load: ${error.message}")
                    val adError = error.toAdError()
                    _nativeState.value = AdState.Error(adError)
                    callback.onAdFailedToLoad(adError)
                }

                override fun onAdClicked() {
                    callback.onAdClicked()
                }

                override fun onAdImpression() {
                    callback.onAdImpression()
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .build()
            )
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    // ============ CLEANUP ============

    override fun destroy() {
        log("Destroying AdMob provider...")

        bannerAdView?.destroy()
        bannerAdView = null

        interstitialAd = null
        rewardedAd = null

        nativeAd?.destroy()
        nativeAd = null

        isInitialized = false
        _bannerState.value = AdState.Idle
        _interstitialState.value = AdState.Idle
        _rewardedState.value = AdState.Idle
        _nativeState.value = AdState.Idle
    }
}

// ============ EXTENSION FUNCTIONS ============

/**
 * Convert SDK AdSize to AdMob AdSize
 */
private fun AdSize.toAdMobSize(context: Context): com.google.android.gms.ads.AdSize {
    return when (this) {
        AdSize.BANNER -> com.google.android.gms.ads.AdSize.BANNER
        AdSize.LARGE_BANNER -> com.google.android.gms.ads.AdSize.LARGE_BANNER
        AdSize.MEDIUM_RECTANGLE -> com.google.android.gms.ads.AdSize.MEDIUM_RECTANGLE
        AdSize.FULL_BANNER -> com.google.android.gms.ads.AdSize.FULL_BANNER
        AdSize.LEADERBOARD -> com.google.android.gms.ads.AdSize.LEADERBOARD
        AdSize.ADAPTIVE -> {
            val displayMetrics = context.resources.displayMetrics
            val adWidthPixels = displayMetrics.widthPixels
            val density = displayMetrics.density
            val adWidth = (adWidthPixels / density).toInt()
            com.google.android.gms.ads.AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
        }
    }
}

/**
 * Convert AdMob LoadAdError to SDK AdError
 */
private fun LoadAdError.toAdError(): AdError {
    return AdError.LoadError(
        code = this.code,
        message = this.message,
        providerErrorCode = this.code
    )
}
