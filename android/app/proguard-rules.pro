# ============================================================
# ProGuard / R8 rules for Tahlil & Doa
# ------------------------------------------------------------
# The default config (proguard-android-optimize.txt) enables
# aggressive optimization that has historically stripped
# classes used reflectively by Jetpack Compose, kotlinx
# serialization, DataStore, SplashScreen, Navigation Compose,
# WorkManager, and lifecycle. This file pins them down
# explicitly.
#
# Build profile:
#   - Kotlin 1.9.22
#   - Compose BOM 2024.02.00 (Compose Compiler 1.5.8)
#   - Material3 + Navigation 2.7.7 + DataStore 1.0.0
#   - WorkManager 2.9.0
#   - minSdk 24 / targetSdk 34 / compileSdk 34
# ============================================================

# === Kotlin Coroutines ===
-keepclassmembernames class kotlinx.** { *; }
-dontwarn kotlinx.coroutines.**

# === Kotlinx Serialization ===
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.tahlildoa.app.**$$serializer { *; }
-keepclassmembers class com.tahlildoa.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.tahlildoa.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep @kotlinx.serialization.Serializable class com.tahlildoa.app.** { *; }

# === Compose (additive to consumer rules) ===
# Many Compose lambdas and internal classes are referenced by
# reflection at runtime. The consumer rules shipped with the
# BOM cover the common cases but R8 with isMinifyEnabled=true
# has been observed stripping some runtime hooks — pin them.
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.animation.** { *; }
-keep class androidx.compose.material.** { *; }
-keep @androidx.compose.runtime.Stable class * { *; }
-keep @androidx.compose.runtime.Immutable class * { *; }
# Keep Composable lambda methods (referenced via the annotation).
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# === DataStore ===
-keep class androidx.datastore.** { *; }
-keep class com.google.protobuf.** { *; }
-dontwarn androidx.datastore.**

# === AndroidX core / Activity / Lifecycle ===
-keep class androidx.core.** { *; }
-keep class androidx.activity.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class androidx.savedstate.** { *; }
-dontwarn androidx.activity.**

# === Navigation Compose ===
-keep class androidx.navigation.** { *; }

# === SplashScreen API ===
-keep class androidx.core.splashscreen.** { *; }

# === WorkManager ===
# CoroutineWorker subclasses are instantiated reflectively by
# WorkManager's factory. Keep the entire androidx.work package
# plus our worker subclass so the factory can find the ctor.
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keepclassmembers class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# === App's own classes — comprehensive ===
-keep class com.tahlildoa.app.** { *; }
-keepclassmembers class com.tahlildoa.app.** { *; }

# === Keep enum members (Compose state restoration) ===
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# === General Android ===
-dontwarn android.support.**
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod
-keepattributes SourceFile,LineNumberTable
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-keep public class * extends androidx.activity.ComponentActivity
-keep public class * extends android.app.Application

# === Disable aggressive optimization that breaks Compose ===
# Compose relies on certain classes & lambdas being present
# even when R8 cannot statically prove they're referenced.
# -dontoptimize keeps R8 from rewriting method bodies in ways
# that can break runtime hooks (e.g. snapshot invalidation).
-dontoptimize
-dontpreverify
