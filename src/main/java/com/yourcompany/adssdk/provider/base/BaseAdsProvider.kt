package com.yourcompany.adssdk.provider.base

import android.app.Activity
import android.content.Context
import com.yourcompany.adssdk.config.AdsConfig
import com.yourcompany.adssdk.config.AdSize
import com.yourcompany.adssdk.core.*
import com.yourcompany.adssdk.utils.AdsLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Abstract base class cho tất cả providers
 * Chứa logic chung, các provider con chỉ cần override các hàm cần thiết
 */
abstract class BaseAdsProvider : IAdsProvider {

    protected var config: AdsConfig? = null
    protected var isInitialized = false

    // State flows cho từng loại ad
    protected val _interstitialState = MutableStateFlow<AdState>(AdState.Idle)
    val interstitialState: StateFlow<AdState> = _interstitialState.asStateFlow()

    protected val _rewardedState = MutableStateFlow<AdState>(AdState.Idle)
    val rewardedState: StateFlow<AdState> = _rewardedState.asStateFlow()

    protected val _bannerState = MutableStateFlow<AdState>(AdState.Idle)
    val bannerState: StateFlow<AdState> = _bannerState.asStateFlow()

    protected val _nativeState = MutableStateFlow<AdState>(AdState.Idle)
    val nativeState: StateFlow<AdState> = _nativeState.asStateFlow()

    // Timestamps để track cooldown
    protected var lastInterstitialShowTime: Long = 0

    override fun isInitialized(): Boolean = isInitialized

    /**
     * Kiểm tra điều kiện trước khi thực hiện action
     */
    protected fun checkPreconditions(): AdError? {
        if (!isInitialized) {
            return AdError.NotInitializedError()
        }
        return null
    }

    /**
     * Kiểm tra cooldown cho Interstitial
     */
    protected fun canShowInterstitial(): Boolean {
        val cooldown = config?.interstitialCooldownMs ?: 30_000L
        return System.currentTimeMillis() - lastInterstitialShowTime >= cooldown
    }

    /**
     * Log helper
     */
    protected fun log(message: String) {
        if (config?.enableLogging == true) {
            AdsLogger.d(providerName, message)
        }
    }

    protected fun logError(message: String, throwable: Throwable? = null) {
        if (config?.enableLogging == true) {
            AdsLogger.e(providerName, message, throwable)
        }
    }
}
