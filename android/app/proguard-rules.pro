# ============================================================
# ProGuard / R8 rules for Tahlil & Doa
# ------------------------------------------------------------
# Strategy: Enable aggressive R8 minification with surgical keep
# rules. Compose ships its own consumer proguard rules in
# META-INF/proguard/, so we DON'T need broad `-keep class
# androidx.compose.**` rules — that was the previous mistake that
# ballooned the APK from 1.8 MB to 7.7 MB.
#
# The rules below only ADD keeps for things R8 cannot figure out
# automatically:
#  - Reflection-based instantiation (serialization, workers)
#  - Enum valueOf() used by Compose state restoration
#  - JNI / native bridges (none here, but pattern is documented)
#
# Build profile:
#   - Kotlin 1.9.22 + Compose Compiler 1.5.8
#   - Compose BOM 2024.02.00 (ships consumer rules)
#   - Material3 + Navigation 2.7.7 + DataStore 1.0.0
#   - WorkManager 2.9.0
#   - minSdk 24 / targetSdk 34 / compileSdk 34
# ============================================================

# === Kotlinx Serialization ===
# R8 needs the generated $$serializer classes + the Companion that
# returns them. The kotlinx-serialization plugin adds @Serializable
# and synthesizes the serializer; we keep the synthetic class.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# Our @Serializable data classes — keep the synthetic serializer.
-keep,includedescriptorclasses class com.tahlildoa.app.**$$serializer { *; }
-keepclassmembers class com.tahlildoa.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.tahlildoa.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep @kotlinx.serialization.Serializable class com.tahlildoa.app.** { *; }

# === WorkManager ===
# WorkManager instantiates Worker subclasses via reflection
# (ListenableWorker.Factory). R8 would otherwise rename/remove
# the constructor. Keep just the Worker classes + their default
# ctor — let R8 strip everything else inside them.
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class com.tahlildoa.app.work.** { *; }

# === DataStore Preferences ===
# DataStore uses Kotlinx Serialization internally for some types,
# but our use of Preferences is reflection-free. Keep just the
# PreferenceDataStoreFactory delegate (R8 sometimes inlines it).
-dontwarn androidx.datastore.**

# === SplashScreen API ===
# SplashScreen reads theme attributes reflectively on API < 31.
-keep class androidx.core.splashscreen.** { *; }

# === App's own classes ===
# Keep our data classes (used in @Serializable + Compose state) and
# the util singletons (called reflectively by Compose for default
# args). Let R8 strip unused methods inside them.
-keep class com.tahlildoa.app.data.** { *; }
-keep class com.tahlildoa.app.util.** { *; }
# Application + Activity are referenced by name in the manifest.
-keep public class com.tahlildoa.app.TahlilDoaApp
-keep public class com.tahlildoa.app.MainActivity
# ViewModel factory pattern uses reflection via ViewModelProvider.
-keep class * extends androidx.lifecycle.ViewModel {
    public <init>();
}
# Compose: keep @Stable / @Immutable annotated classes (R8 doesn't
# strip the annotation itself, but Compose's runtime checks for these
# at compose-time and R8 must NOT inline them away).
-keep @androidx.compose.runtime.Stable class * { *; }
-keep @androidx.compose.runtime.Immutable class * { *; }
# Keep Composable function signatures (R8 may rename the function,
# which breaks Compose's runtime lookup of @Composable lambdas).
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# === Enums (used by Compose saveable state restoration) ===
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    ***[] $VALUES;
    public static ** fromStorageKey(java.lang.String);
    public static ** fromKey(java.lang.String);
}

# === General Android ===
-dontwarn android.support.**
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod
-keepattributes SourceFile, LineNumberTable
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleAnnotations, RuntimeInvisibleParameterAnnotations

# === Optimization settings ===
# Allow R8 full mode (default since AGP 8.0) for aggressive
# optimization. Compose ships consumer rules that handle its own
# needs, so we don't need to disable anything.
# DO NOT add `-dontoptimize` or `-dontpreverify` — they balloon APK
# size by preventing R8 from inlining and dead-code-eliminating.
# DO NOT add `-keep class androidx.**` broadly — Compose's consumer
# rules already keep what's needed; broad keeps prevent R8 from
# shrinking Compose.
