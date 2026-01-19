package com.yourcompany.adssdk.factory

import com.yourcompany.adssdk.config.AdsProviderType
import com.yourcompany.adssdk.core.IAdsProvider
import com.yourcompany.adssdk.provider.admob.AdMobProvider

/**
 * Factory để tạo các Provider instance
 * Khi thêm provider mới, chỉ cần thêm case trong hàm create()
 */
object AdsProviderFactory {

    fun create(type: AdsProviderType): IAdsProvider {
        return when (type) {
            AdsProviderType.ADMOB -> AdMobProvider()
            AdsProviderType.FACEBOOK -> throw NotImplementedError("Facebook Ads not implemented yet")
            AdsProviderType.UNITY -> throw NotImplementedError("Unity Ads not implemented yet")
            AdsProviderType.APPLOVIN -> throw NotImplementedError("AppLovin not implemented yet")
            AdsProviderType.IRONSOURCE -> throw NotImplementedError("IronSource not implemented yet")
        }
    }

    /**
     * Tạo multiple providers cho mediation
     */
    fun createMultiple(types: List<AdsProviderType>): List<IAdsProvider> {
        return types.mapNotNull { type ->
            try {
                create(type)
            } catch (e: NotImplementedError) {
                null
            }
        }
    }
}
