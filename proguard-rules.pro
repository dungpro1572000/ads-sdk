# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep SDK public APIs
-keep class com.yourcompany.adssdk.** { *; }
-keep interface com.yourcompany.adssdk.** { *; }

# AdMob
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }

# Keep callback interfaces
-keepclassmembers class * implements com.yourcompany.adssdk.core.BaseAdCallback {
    <methods>;
}

# Keep data classes
-keepclassmembers class com.yourcompany.adssdk.core.AdReward { *; }
-keepclassmembers class com.yourcompany.adssdk.core.NativeAdData { *; }
-keepclassmembers class com.yourcompany.adssdk.config.AdsConfig { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
