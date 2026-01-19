package com.yourcompany.adssdk

import android.app.Activity
import android.content.Context
import com.yourcompany.adssdk.config.AdsConfig
import com.yourcompany.adssdk.config.AdSize
import com.yourcompany.adssdk.core.*
import com.yourcompany.adssdk.factory.AdsProviderFactory
import com.yourcompany.adssdk.provider.admob.AdMobProvider
import com.yourcompany.adssdk.utils.AdsLogger

/**
 * AdsManager - Facade pattern
 * Entry point chính để sử dụng SDK
 *
 * Usage:
 * ```
 * val adsManager = AdsManager.getInstance()
 * adsManager.initialize(context, config)
 * adsManager.showInterstitial(activity, callback)
 * ```
 */
class AdsManager private constructor() {

    private var provider: IAdsProvider? = null
    private var config: AdsConfig? = null
    private var isInitialized = false

    companion object {
        @Volatile
        private var instance: AdsManager? = null

        fun getInstance(): AdsManager {
            return instance ?: synchronized(this) {
                instance ?: AdsManager().also { instance = it }
            }
        }
    }

    // ============ INITIALIZATION ============

    /**
     * Khởi tạo SDK
     * @param context Application context
     * @param config Configuration
     * @return Result<Boolean>
     */
    suspend fun initialize(context: Context, config: AdsConfig): Result<Boolean> {
        if (isInitialized) {
            AdsLogger.w("AdsManager", "SDK already initialized")
            return Result.success(true)
        }

        this.config = config

        // Tạo provider từ factory
        provider = AdsProviderFactory.create(config.providerType)

        // Khởi tạo provider
        val result = provider?.initialize(context, config)

        return result?.also {
            if (it.isSuccess) {
                isInitialized = true
                AdsLogger.i("AdsManager", "SDK initialized successfully with ${config.providerType}")
            }
        } ?: Result.failure(Exception("Provider creation failed"))
    }

    fun isInitialized(): Boolean = isInitialized

    // ============ BANNER ADS ============

    fun loadBanner(
        adUnitId: String? = null,
        adSize: AdSize = AdSize.BANNER,
        callback: BannerAdCallback
    ) {
        ensureInitialized { error ->
            callback.onAdFailedToLoad(error)
            return
        }

        val unitId = adUnitId ?: config?.bannerAdUnitId ?: ""
        provider?.loadBannerAd(unitId, adSize, callback)
    }

    /**
     * Lấy AdView cho Compose/XML
     * Chỉ dùng với AdMob provider
     */
    fun createBannerView(
        context: Context,
        adUnitId: String? = null,
        adSize: AdSize = AdSize.BANNER,
        callback: BannerAdCallback
    ): Any? {
        val admobProvider = provider as? AdMobProvider
        val unitId = adUnitId ?: config?.bannerAdUnitId ?: ""
        return admobProvider?.createBannerAdView(context, unitId, adSize, callback)
    }

    // ============ INTERSTITIAL ADS ============

    fun loadInterstitial(
        activity: Activity,
        adUnitId: String? = null,
        callback: InterstitialAdCallback
    ) {
        ensureInitialized { error ->
            callback.onAdFailedToLoad(error)
            return
        }

        val unitId = adUnitId ?: config?.interstitialAdUnitId ?: ""
        provider?.loadInterstitialAd(activity, unitId, callback)
    }

    fun showInterstitial(activity: Activity, callback: InterstitialAdCallback) {
        ensureInitialized { error ->
            callback.onAdFailedToShow(error)
            return
        }

        provider?.showInterstitialAd(activity, callback)
    }

    fun isInterstitialReady(): Boolean = provider?.isInterstitialAdReady() ?: false

    // ============ REWARDED ADS ============

    fun loadRewarded(
        activity: Activity,
        adUnitId: String? = null,
        callback: RewardedAdCallback
    ) {
        ensureInitialized { error ->
            callback.onAdFailedToLoad(error)
            return
        }

        val unitId = adUnitId ?: config?.rewardedAdUnitId ?: ""
        provider?.loadRewardedAd(activity, unitId, callback)
    }

    fun showRewarded(activity: Activity, callback: RewardedAdCallback) {
        ensureInitialized { error ->
            callback.onAdFailedToShow(error)
            return
        }

        provider?.showRewardedAd(activity, callback)
    }

    fun isRewardedReady(): Boolean = provider?.isRewardedAdReady() ?: false

    // ============ NATIVE ADS ============

    fun loadNativeAd(
        context: Context,
        adUnitId: String? = null,
        callback: NativeAdCallback
    ) {
        ensureInitialized { error ->
            callback.onAdFailedToLoad(error)
            return
        }

        val unitId = adUnitId ?: config?.nativeAdUnitId ?: ""
        provider?.loadNativeAd(context, unitId, callback)
    }

    // ============ UTILITY ============

    /**
     * Lấy provider hiện tại (nếu cần custom)
     */
    fun getProvider(): IAdsProvider? = provider

    /**
     * Thay đổi provider
     */
    suspend fun switchProvider(
        context: Context,
        newConfig: AdsConfig
    ): Result<Boolean> {
        // Destroy provider cũ
        provider?.destroy()
        isInitialized = false

        // Khởi tạo provider mới
        return initialize(context, newConfig)
    }

    /**
     * Giải phóng tài nguyên
     */
    fun destroy() {
        provider?.destroy()
        provider = null
        isInitialized = false
        config = null
    }

    // ============ PRIVATE HELPERS ============

    private inline fun ensureInitialized(onError: (AdError) -> Unit) {
        if (!isInitialized || provider == null) {
            onError(AdError.NotInitializedError())
        }
    }
}
