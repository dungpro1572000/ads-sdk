# Consumer ProGuard rules for ads-sdk
# These rules will be applied to apps that use this SDK

# Keep SDK public APIs
-keep class com.yourcompany.adssdk.** { *; }
-keep interface com.yourcompany.adssdk.** { *; }

# Keep callback interfaces
-keepclassmembers class * implements com.yourcompany.adssdk.core.BaseAdCallback {
    <methods>;
}
