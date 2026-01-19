package com.yourcompany.adssdk.core

import android.app.Activity
import android.content.Context
import com.yourcompany.adssdk.config.AdsConfig
import com.yourcompany.adssdk.config.AdSize

/**
 * Interface chính cho tất cả Ads Provider
 * Mỗi provider mới (AdMob, Facebook, Unity...) phải implement interface này
 */
interface IAdsProvider {

    /** Tên của provider */
    val providerName: String

    /** Khởi tạo SDK của provider */
    suspend fun initialize(context: Context, config: AdsConfig): Result<Boolean>

    /** Kiểm tra provider đã được khởi tạo chưa */
    fun isInitialized(): Boolean

    // ============ BANNER ADS ============
    fun loadBannerAd(
        adUnitId: String,
        adSize: AdSize,
        callback: BannerAdCallback
    )

    // ============ INTERSTITIAL ADS ============
    fun loadInterstitialAd(
        activity: Activity,
        adUnitId: String,
        callback: InterstitialAdCallback
    )

    fun showInterstitialAd(
        activity: Activity,
        callback: InterstitialAdCallback
    )

    fun isInterstitialAdReady(): Boolean

    // ============ REWARDED ADS ============
    fun loadRewardedAd(
        activity: Activity,
        adUnitId: String,
        callback: RewardedAdCallback
    )

    fun showRewardedAd(
        activity: Activity,
        callback: RewardedAdCallback
    )

    fun isRewardedAdReady(): Boolean

    // ============ NATIVE ADS ============
    fun loadNativeAd(
        context: Context,
        adUnitId: String,
        callback: NativeAdCallback
    )

    /** Giải phóng tài nguyên */
    fun destroy()
}
