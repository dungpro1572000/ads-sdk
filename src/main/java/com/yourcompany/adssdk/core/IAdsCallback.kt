package com.yourcompany.adssdk.core

/**
 * Base callback interface
 */
interface BaseAdCallback {
    fun onAdLoaded()
    fun onAdFailedToLoad(error: AdError)
    fun onAdClicked()
}

/**
 * Callback cho Banner Ads
 */
interface BannerAdCallback : BaseAdCallback {
    fun onAdImpression()
    fun onAdOpened()
    fun onAdClosed()
}

/**
 * Callback cho Interstitial Ads
 */
interface InterstitialAdCallback : BaseAdCallback {
    fun onAdShowed()
    fun onAdDismissed()
    fun onAdFailedToShow(error: AdError)
    fun onAdImpression()
}

/**
 * Callback cho Rewarded Ads
 */
interface RewardedAdCallback : BaseAdCallback {
    fun onAdShowed()
    fun onAdDismissed()
    fun onAdFailedToShow(error: AdError)
    fun onUserEarnedReward(reward: AdReward)
    fun onAdImpression()
}

/**
 * Callback cho Native Ads
 */
interface NativeAdCallback : BaseAdCallback {
    fun onNativeAdLoaded(nativeAd: NativeAdData)
    fun onAdImpression()
}

/**
 * Data class cho reward
 */
data class AdReward(
    val type: String,
    val amount: Int
)

/**
 * Data class cho Native Ad
 */
data class NativeAdData(
    val headline: String?,
    val body: String?,
    val callToAction: String?,
    val icon: Any?,          // Drawable hoặc URL
    val mediaContent: Any?,  // MediaContent từ provider
    val advertiser: String?,
    val price: String?,
    val store: String?,
    val starRating: Double?,
    val extras: Map<String, Any> = emptyMap(),
    val originalAd: Any?     // Object gốc từ provider để render
)
