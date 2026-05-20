# Add project specific ProGuard rules here.

# Room
-keep class com.yanpai.clipboardcleaner.data.** { *; }

# Kotlin
-keepattributes *Annotation*
-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }

# Compose
-dontwarn androidx.compose.**
