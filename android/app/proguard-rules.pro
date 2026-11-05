# Add project specific ProGuard rules here.
# ===================================================

# --- Kotlinx Serialization -----------------------------------------------
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

# --- Keep data classes -----------------------------------------------------
-keep class com.tahlildoa.app.data.** { *; }
-keep class com.tahlildoa.app.util.** { *; }

# --- Compose (handled by BOM) ----------------------------------------------
# Compose ships consumer proguard rules, but be explicit with @Stable classes.
-keep @androidx.compose.runtime.Stable class * { *; }
-keep @androidx.compose.runtime.Immutable class * { *; }

# --- Keep enum members for Compose -----------------------------------------
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --- General Android -------------------------------------------------------
-dontwarn android.support.**
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keep public class * extends androidx.activity.ComponentActivity
