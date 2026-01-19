package com.yourcompany.adssdk.config

/**
 * Configuration cho SDK
 */
data class AdsConfig(
    val providerType: AdsProviderType = AdsProviderType.ADMOB,
    val testMode: Boolean = false,
    val testDeviceIds: List<String> = emptyList(),

    // Ad Unit IDs
    val bannerAdUnitId: String = "",
    val interstitialAdUnitId: String = "",
    val rewardedAdUnitId: String = "",
    val nativeAdUnitId: String = "",

    // Optional settings
    val enableLogging: Boolean = true,
    val autoLoadInterstitial: Boolean = true,
    val autoLoadRewarded: Boolean = true,
    val interstitialCooldownMs: Long = 30_000L, // 30 seconds

    // Mediation (future)
    val enableMediation: Boolean = false,
    val mediationProviders: List<AdsProviderType> = emptyList()
) {
    class Builder {
        private var providerType: AdsProviderType = AdsProviderType.ADMOB
        private var testMode: Boolean = false
        private var testDeviceIds: List<String> = emptyList()
        private var bannerAdUnitId: String = ""
        private var interstitialAdUnitId: String = ""
        private var rewardedAdUnitId: String = ""
        private var nativeAdUnitId: String = ""
        private var enableLogging: Boolean = true
        private var autoLoadInterstitial: Boolean = true
        private var autoLoadRewarded: Boolean = true
        private var interstitialCooldownMs: Long = 30_000L

        fun setProvider(type: AdsProviderType) = apply { providerType = type }
        fun setTestMode(enabled: Boolean) = apply { testMode = enabled }
        fun setTestDeviceIds(ids: List<String>) = apply { testDeviceIds = ids }
        fun setBannerAdUnitId(id: String) = apply { bannerAdUnitId = id }
        fun setInterstitialAdUnitId(id: String) = apply { interstitialAdUnitId = id }
        fun setRewardedAdUnitId(id: String) = apply { rewardedAdUnitId = id }
        fun setNativeAdUnitId(id: String) = apply { nativeAdUnitId = id }
        fun setEnableLogging(enabled: Boolean) = apply { enableLogging = enabled }
        fun setAutoLoadInterstitial(enabled: Boolean) = apply { autoLoadInterstitial = enabled }
        fun setAutoLoadRewarded(enabled: Boolean) = apply { autoLoadRewarded = enabled }
        fun setInterstitialCooldown(ms: Long) = apply { interstitialCooldownMs = ms }

        fun build() = AdsConfig(
            providerType = providerType,
            testMode = testMode,
            testDeviceIds = testDeviceIds,
            bannerAdUnitId = bannerAdUnitId,
            interstitialAdUnitId = interstitialAdUnitId,
            rewardedAdUnitId = rewardedAdUnitId,
            nativeAdUnitId = nativeAdUnitId,
            enableLogging = enableLogging,
            autoLoadInterstitial = autoLoadInterstitial,
            autoLoadRewarded = autoLoadRewarded,
            interstitialCooldownMs = interstitialCooldownMs
        )
    }
}
