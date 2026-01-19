# Android Ads SDK

A scalable Android advertising SDK that supports multiple ad providers (AdMob, Facebook Audience Network, Unity Ads, AppLovin, etc.). Built with Clean Architecture and SOLID principles.

## Features

- **Multiple Ad Types**: Banner, Interstitial, Rewarded, Native Ads
- **Provider Abstraction**: Easy to add new ad providers
- **Jetpack Compose Support**: Native Compose components for ads
- **Kotlin Coroutines**: Async initialization and state management
- **Builder Pattern**: Flexible configuration
- **Singleton Pattern**: Easy access via `AdsManager.getInstance()`

## Requirements

- **Min SDK**: 30
- **Target SDK**: 35
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose + XML support

## Installation

### Option 1: JitPack (Recommended)

Add JitPack repository to your root `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add dependency to your app `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.dungpro1572000:ads-sdk:1.0.0")
}
```

### Option 2: Local Module

Clone the repository and add as a local module:

```bash
git clone https://github.com/dungpro1572000/ads-sdk.git
```

Add to `settings.gradle.kts`:

```kotlin
include(":ads-sdk")
project(":ads-sdk").projectDir = File("path/to/ads-sdk")
```

Add dependency:

```kotlin
dependencies {
    implementation(project(":ads-sdk"))
}
```

## Setup

### 1. Configure AdMob App ID

Add your AdMob App ID to `AndroidManifest.xml`:

```xml
<manifest>
    <application>
        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY"/>
    </application>
</manifest>
```

### 2. Initialize SDK

Initialize the SDK in your `Application` class or `MainActivity`:

```kotlin
import com.yourcompany.adssdk.AdsManager
import com.yourcompany.adssdk.config.AdsConfig
import com.yourcompany.adssdk.config.AdsProviderType

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        lifecycleScope.launch {
            val config = AdsConfig.Builder()
                .setProvider(AdsProviderType.ADMOB)
                .setTestMode(BuildConfig.DEBUG)
                .setTestDeviceIds(listOf("YOUR_TEST_DEVICE_ID"))
                .setBannerAdUnitId("ca-app-pub-3940256099942544/6300978111")
                .setInterstitialAdUnitId("ca-app-pub-3940256099942544/1033173712")
                .setRewardedAdUnitId("ca-app-pub-3940256099942544/5224354917")
                .setNativeAdUnitId("ca-app-pub-3940256099942544/2247696110")
                .setEnableLogging(true)
                .setInterstitialCooldown(30_000L) // 30 seconds
                .build()

            val result = AdsManager.getInstance().initialize(this@MyApplication, config)

            if (result.isSuccess) {
                Log.d("AdsSDK", "SDK initialized successfully")
            }
        }
    }
}
```

## Usage

### Banner Ads (Jetpack Compose)

```kotlin
import com.yourcompany.adssdk.ui.compose.BannerAd
import com.yourcompany.adssdk.config.AdSize

@Composable
fun MyScreen() {
    Column {
        // Your content
        Text("My App Content")

        Spacer(modifier = Modifier.weight(1f))

        // Banner at bottom
        BannerAd(
            modifier = Modifier.fillMaxWidth(),
            adSize = AdSize.BANNER,
            onAdLoaded = { Log.d("Ads", "Banner loaded") },
            onAdFailedToLoad = { error -> Log.e("Ads", "Failed: ${error.message}") },
            onAdClicked = { Log.d("Ads", "Banner clicked") }
        )
    }
}
```

#### Banner with State Handling

```kotlin
BannerAdWithState(
    modifier = Modifier.fillMaxWidth(),
    adSize = AdSize.ADAPTIVE,
    loadingContent = { CircularProgressIndicator() },
    errorContent = { error -> Text("Ad failed: ${error.message}") }
)
```

#### Available Banner Sizes

| AdSize | Dimensions |
|--------|------------|
| `BANNER` | 320x50 |
| `LARGE_BANNER` | 320x100 |
| `MEDIUM_RECTANGLE` | 300x250 |
| `FULL_BANNER` | 468x60 |
| `LEADERBOARD` | 728x90 |
| `ADAPTIVE` | Adaptive width |

### Interstitial Ads

```kotlin
import com.yourcompany.adssdk.AdsManager
import com.yourcompany.adssdk.core.InterstitialAdCallback
import com.yourcompany.adssdk.core.AdError

class GameActivity : ComponentActivity() {

    private val adsManager = AdsManager.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Preload interstitial
        loadInterstitial()
    }

    private fun loadInterstitial() {
        adsManager.loadInterstitial(
            activity = this,
            callback = object : InterstitialAdCallback {
                override fun onAdLoaded() {
                    Log.d("Ads", "Interstitial loaded")
                }

                override fun onAdFailedToLoad(error: AdError) {
                    Log.e("Ads", "Load failed: ${error.message}")
                }

                override fun onAdShowed() {
                    Log.d("Ads", "Interstitial showed")
                }

                override fun onAdDismissed() {
                    Log.d("Ads", "Interstitial dismissed")
                    // Load next ad
                    loadInterstitial()
                }

                override fun onAdFailedToShow(error: AdError) {
                    Log.e("Ads", "Show failed: ${error.message}")
                }

                override fun onAdClicked() {}
                override fun onAdImpression() {}
            }
        )
    }

    private fun showInterstitialAd() {
        if (adsManager.isInterstitialReady()) {
            adsManager.showInterstitial(this, object : InterstitialAdCallback {
                override fun onAdLoaded() {}
                override fun onAdFailedToLoad(error: AdError) {}
                override fun onAdShowed() {}
                override fun onAdDismissed() {
                    navigateToNextLevel()
                }
                override fun onAdFailedToShow(error: AdError) {
                    navigateToNextLevel()
                }
                override fun onAdClicked() {}
                override fun onAdImpression() {}
            })
        } else {
            navigateToNextLevel()
        }
    }
}
```

### Rewarded Ads

```kotlin
import com.yourcompany.adssdk.core.RewardedAdCallback
import com.yourcompany.adssdk.core.AdReward

private fun loadRewardedAd() {
    adsManager.loadRewarded(
        activity = this,
        callback = object : RewardedAdCallback {
            override fun onAdLoaded() {
                Log.d("Ads", "Rewarded ad loaded")
            }

            override fun onAdFailedToLoad(error: AdError) {
                Log.e("Ads", "Load failed: ${error.message}")
            }

            override fun onUserEarnedReward(reward: AdReward) {
                // Give reward to user
                Log.d("Ads", "User earned: ${reward.amount} ${reward.type}")
                addCoins(reward.amount)
            }

            override fun onAdShowed() {}
            override fun onAdDismissed() {
                // Load next rewarded ad
                loadRewardedAd()
            }
            override fun onAdFailedToShow(error: AdError) {}
            override fun onAdClicked() {}
            override fun onAdImpression() {}
        }
    )
}

private fun showRewardedAd() {
    if (adsManager.isRewardedReady()) {
        adsManager.showRewarded(this, object : RewardedAdCallback {
            override fun onUserEarnedReward(reward: AdReward) {
                addCoins(reward.amount)
            }
            // ... other callbacks
        })
    } else {
        Toast.makeText(this, "Ad not ready", Toast.LENGTH_SHORT).show()
    }
}
```

### Native Ads (Jetpack Compose)

```kotlin
import com.yourcompany.adssdk.ui.compose.NativeAd

@Composable
fun ContentWithNativeAd() {
    LazyColumn {
        items(contentList) { item ->
            ContentItem(item)
        }

        item {
            NativeAd(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                onAdLoaded = { Log.d("Ads", "Native ad loaded") },
                onAdFailedToLoad = { error -> Log.e("Ads", "Failed: ${error.message}") },
                loadingContent = {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(250.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                },
                errorContent = { error ->
                    Text("Ad failed to load", color = Color.Red)
                }
            )
        }
    }
}
```

## Configuration Options

```kotlin
AdsConfig.Builder()
    .setProvider(AdsProviderType.ADMOB)      // Ad provider
    .setTestMode(true)                        // Enable test mode
    .setTestDeviceIds(listOf("DEVICE_ID"))   // Test device IDs
    .setBannerAdUnitId("ca-app-pub-xxx")     // Banner ad unit
    .setInterstitialAdUnitId("ca-app-pub-xxx") // Interstitial ad unit
    .setRewardedAdUnitId("ca-app-pub-xxx")   // Rewarded ad unit
    .setNativeAdUnitId("ca-app-pub-xxx")     // Native ad unit
    .setEnableLogging(true)                   // Enable logging
    .setAutoLoadInterstitial(true)            // Auto load interstitial
    .setAutoLoadRewarded(true)                // Auto load rewarded
    .setInterstitialCooldown(30_000L)         // Cooldown between interstitials
    .build()
```

## Test Ad Unit IDs (AdMob)

Use these IDs for testing:

| Ad Type | Test Ad Unit ID |
|---------|-----------------|
| Banner | `ca-app-pub-3940256099942544/6300978111` |
| Interstitial | `ca-app-pub-3940256099942544/1033173712` |
| Rewarded | `ca-app-pub-3940256099942544/5224354917` |
| Native | `ca-app-pub-3940256099942544/2247696110` |

## Error Handling

```kotlin
sealed class AdError(
    open val code: Int,
    open val message: String
) {
    data class LoadError(...)      // Ad failed to load
    data class ShowError(...)      // Ad failed to show
    data class NetworkError(...)   // No internet connection
    data class NotInitializedError(...) // SDK not initialized
    data class AdNotReadyError(...)     // Ad not loaded yet
}
```

## Cleanup

Don't forget to release resources when done:

```kotlin
override fun onDestroy() {
    super.onDestroy()
    AdsManager.getInstance().destroy()
}
```

## Adding New Providers

To add a new ad provider (e.g., Facebook):

1. Create provider class implementing `IAdsProvider`:

```kotlin
class FacebookProvider : BaseAdsProvider() {
    override val providerName = "Facebook"

    override suspend fun initialize(context: Context, config: AdsConfig): Result<Boolean> {
        // Initialize Facebook SDK
    }

    // Implement other methods...
}
```

2. Update `AdsProviderFactory`:

```kotlin
fun create(type: AdsProviderType): IAdsProvider {
    return when (type) {
        AdsProviderType.ADMOB -> AdMobProvider()
        AdsProviderType.FACEBOOK -> FacebookProvider()
        // ...
    }
}
```

## Architecture

```
┌─────────────────────────────────────────┐
│              Application                │
└─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│              AdsManager                 │
│           (Facade Pattern)              │
└─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│         IAdsProvider Interface          │
└─────────────────────────────────────────┘
                    │
        ┌───────────┼───────────┐
        ▼           ▼           ▼
   ┌─────────┐ ┌─────────┐ ┌─────────┐
   │ AdMob   │ │Facebook │ │  Unity  │
   │Provider │ │Provider │ │Provider │
   └─────────┘ └─────────┘ └─────────┘
```

## License

MIT License

## Author

Created by dungpro1572000
