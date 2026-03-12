# ProGuard rules for release build
# Keep mDNS and NSD related classes
-keep class android.net.nsd.** { *; }

# Keep Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep the app classes
-keep class com.example.mdiscovery.** { *; }
