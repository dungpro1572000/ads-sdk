package com.yourcompany.adssdk.ui.compose

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.yourcompany.adssdk.AdsManager
import com.yourcompany.adssdk.R
import com.yourcompany.adssdk.core.*

/**
 * Native Ad Composable - Custom Layout
 *
 * Vì Native Ad cần binding với NativeAdView của AdMob,
 * nên sử dụng AndroidView để wrap XML layout
 */
@Composable
fun NativeAd(
    modifier: Modifier = Modifier,
    adUnitId: String? = null,
    onAdLoaded: () -> Unit = {},
    onAdFailedToLoad: (AdError) -> Unit = {},
    onAdClicked: () -> Unit = {},
    loadingContent: @Composable () -> Unit = { DefaultNativeAdLoading() },
    errorContent: @Composable (AdError) -> Unit = { DefaultNativeAdError(it) }
) {
    val context = LocalContext.current
    var nativeAdData by remember { mutableStateOf<NativeAdData?>(null) }
    var adError by remember { mutableStateOf<AdError?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val callback = remember {
        object : NativeAdCallback {
            override fun onAdLoaded() {
                isLoading = false
                onAdLoaded()
            }

            override fun onAdFailedToLoad(error: AdError) {
                isLoading = false
                adError = error
                onAdFailedToLoad(error)
            }

            override fun onAdClicked() = onAdClicked()

            override fun onAdImpression() {}

            override fun onNativeAdLoaded(nativeAd: NativeAdData) {
                nativeAdData = nativeAd
            }
        }
    }

    LaunchedEffect(Unit) {
        AdsManager.getInstance().loadNativeAd(
            context = context,
            adUnitId = adUnitId,
            callback = callback
        )
    }

    Box(modifier = modifier) {
        when {
            isLoading -> loadingContent()
            adError != null -> errorContent(adError!!)
            nativeAdData != null -> {
                NativeAdContent(
                    nativeAdData = nativeAdData!!,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Render Native Ad content sử dụng XML layout
 * Bắt buộc phải dùng NativeAdView từ AdMob để tracking hoạt động đúng
 */
@Composable
private fun NativeAdContent(
    nativeAdData: NativeAdData,
    modifier: Modifier = Modifier
) {
    val nativeAd = nativeAdData.originalAd as? NativeAd

    AndroidView(
        factory = { context ->
            // Inflate XML layout
            val adView = LayoutInflater.from(context)
                .inflate(R.layout.native_ad_layout, null) as NativeAdView

            // Bind views
            adView.headlineView = adView.findViewById(R.id.ad_headline)
            adView.bodyView = adView.findViewById(R.id.ad_body)
            adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
            adView.iconView = adView.findViewById(R.id.ad_icon)
            adView.mediaView = adView.findViewById(R.id.ad_media)
            adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
            adView.priceView = adView.findViewById(R.id.ad_price)
            adView.storeView = adView.findViewById(R.id.ad_store)
            adView.starRatingView = adView.findViewById(R.id.ad_stars)

            adView
        },
        update = { adView ->
            nativeAd?.let { ad ->
                // Populate views
                (adView.headlineView as? TextView)?.text = ad.headline
                (adView.bodyView as? TextView)?.text = ad.body
                (adView.callToActionView as? Button)?.text = ad.callToAction

                ad.icon?.let { icon ->
                    (adView.iconView as? ImageView)?.setImageDrawable(icon.drawable)
                    adView.iconView?.visibility = View.VISIBLE
                } ?: run {
                    adView.iconView?.visibility = View.GONE
                }

                ad.mediaContent?.let { media ->
                    (adView.mediaView as? MediaView)?.mediaContent = media
                }

                ad.advertiser?.let { advertiser ->
                    (adView.advertiserView as? TextView)?.text = advertiser
                    adView.advertiserView?.visibility = View.VISIBLE
                } ?: run {
                    adView.advertiserView?.visibility = View.GONE
                }

                ad.price?.let { price ->
                    (adView.priceView as? TextView)?.text = price
                    adView.priceView?.visibility = View.VISIBLE
                } ?: run {
                    adView.priceView?.visibility = View.GONE
                }

                ad.store?.let { store ->
                    (adView.storeView as? TextView)?.text = store
                    adView.storeView?.visibility = View.VISIBLE
                } ?: run {
                    adView.storeView?.visibility = View.GONE
                }

                ad.starRating?.let { rating ->
                    (adView.starRatingView as? RatingBar)?.rating = rating.toFloat()
                    adView.starRatingView?.visibility = View.VISIBLE
                } ?: run {
                    adView.starRatingView?.visibility = View.GONE
                }

                // Register the native ad
                adView.setNativeAd(ad)
            }
        },
        modifier = modifier
    )
}

@Composable
private fun DefaultNativeAdLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun DefaultNativeAdError(error: AdError) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Ad failed to load",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}
