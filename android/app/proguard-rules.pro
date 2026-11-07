# ============================================================
# ProGuard / R8 rules for Tahlil & Doa — MINIMAL, Singkat-style
# ------------------------------------------------------------
# History: this file once carried aggressive keeps for
# @Stable/@Immutable/@Composable classes, ViewModels, enums and
# utils. Those rules PIN Compose classes so R8 cannot merge them
# freely — and the half-pinned class graph produced:
#   IncompatibleClassChangeError: Conflicting default method
#   implementations ModifierLocalProvider.all(...)
# on the first composed frame of every minified release build
# (confirmed across 6 CI runs). The working sister app
# Tahlil-Singkat ships with only TWO rules and its minified APK
# runs fine. This file now matches that philosophy: keep only
# what genuinely needs keeping (serialization + workers), let R8
# do everything else.
# ============================================================

# === Attributes (stack traces + runtime annotations) ===
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepattributes SourceFile, LineNumberTable

# === kotlinx.serialization (official recommended rules) ===
# Covers every @Serializable data class under com.tahlildoa.app, including:
#   - data/Section.kt: SectionType, ContentBlock, Section
#   - data/TahlilContent.kt: VariantData, TahlilContentData (+ top-level loadFromJson)
#   - data/ContentVariant.kt: ContentVariant enum
#   - data/DisplaySettings.kt: DisplayToggles
#   - data/ArabicStyleSettings.kt: ArabicStyleSettings, ArabicStyleMode, ArabicAlign
# The broad `com.tahlildoa.app.**` rule below is intentionally wider than the
# data-layer-only keep suggested for the JSON migration: keeping the whole
# data package (and its serializers) lets kotlinx.serialization round-trip
# every field without R8 stripping the generated `$$serializer` companions.
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.tahlildoa.app.**$$serializer { *; }
-keepclassmembers class com.tahlildoa.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.tahlildoa.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep @kotlinx.serialization.Serializable class com.tahlildoa.app.** { *; }
# Explicit data-layer keep — guards the JSON content models even if a future
# refactor drops the @Serializable annotation on a leaf class by mistake.
-keep class com.tahlildoa.app.data.** { *; }
-keepclassmembers class com.tahlildoa.app.data.** { *; }

# === WorkManager ===
# Workers are instantiated via reflection by WorkManager's factory.
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# === General Android ===
-dontwarn android.support.**
