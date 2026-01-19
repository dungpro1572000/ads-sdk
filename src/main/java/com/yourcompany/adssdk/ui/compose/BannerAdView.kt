package com.yourcompany.adssdk.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdView
import com.yourcompany.adssdk.AdsManager
import com.yourcompany.adssdk.config.AdSize
import com.yourcompany.adssdk.core.*

/**
 * Composable Banner Ad
 *
 * Usage:
 * ```
 * BannerAd(
 *     modifier = Modifier.fillMaxWidth(),
 *     adSize = AdSize.BANNER,
 *     onAdLoaded = { /* handle */ },
 *     onAdFailedToLoad = { error -> /* handle */ }
 * )
 * ```
 */
@Composable
fun BannerAd(
    modifier: Modifier = Modifier,
    adUnitId: String? = null,
    adSize: AdSize = AdSize.BANNER,
    onAdLoaded: () -> Unit = {},
    onAdFailedToLoad: (AdError) -> Unit = {},
    onAdClicked: () -> Unit = {},
    onAdImpression: () -> Unit = {},
    onAdOpened: () -> Unit = {},
    onAdClosed: () -> Unit = {}
) {
    val context = LocalContext.current
    var adView by remember { mutableStateOf<AdView?>(null) }

    val callback = remember {
        object : BannerAdCallback {
            override fun onAdLoaded() = onAdLoaded()
            override fun onAdFailedToLoad(error: AdError) = onAdFailedToLoad(error)
            override fun onAdClicked() = onAdClicked()
            override fun onAdImpression() = onAdImpression()
            override fun onAdOpened() = onAdOpened()
            override fun onAdClosed() = onAdClosed()
        }
    }

    DisposableEffect(Unit) {
        val view = AdsManager.getInstance().createBannerView(
            context = context,
            adUnitId = adUnitId,
            adSize = adSize,
            callback = callback
        ) as? AdView

        adView = view

        onDispose {
            adView?.destroy()
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        adView?.let { view ->
            AndroidView(
                factory = { view },
                modifier = Modifier.wrapContentSize()
            )
        }
    }
}

/**
 * Banner Ad với state handling
 */
@Composable
fun BannerAdWithState(
    modifier: Modifier = Modifier,
    adUnitId: String? = null,
    adSize: AdSize = AdSize.BANNER,
    loadingContent: @Composable () -> Unit = {},
    errorContent: @Composable (AdError) -> Unit = {}
) {
    var adState by remember { mutableStateOf<BannerState>(BannerState.Loading) }

    when (val state = adState) {
        is BannerState.Loading -> {
            loadingContent()
            BannerAd(
                modifier = modifier,
                adUnitId = adUnitId,
                adSize = adSize,
                onAdLoaded = { adState = BannerState.Loaded },
                onAdFailedToLoad = { error -> adState = BannerState.Error(error) }
            )
        }
        is BannerState.Error -> errorContent(state.error)
        is BannerState.Loaded -> {
            BannerAd(
                modifier = modifier,
                adUnitId = adUnitId,
                adSize = adSize,
                onAdLoaded = { adState = BannerState.Loaded },
                onAdFailedToLoad = { error -> adState = BannerState.Error(error) }
            )
        }
    }
}

private sealed class BannerState {
    object Loading : BannerState()
    object Loaded : BannerState()
    data class Error(val error: AdError) : BannerState()
}
