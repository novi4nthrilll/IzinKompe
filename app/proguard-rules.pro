# ==============================================================================
# ProGuard / R8 Rules for IzinKompe
# ==============================================================================

# Keep attributes necessary for reflection, Room, and Coroutines
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# ------------------------------------------------------------------------------
# Logcat Stripping (Security: Prevents leaking debug info or OTP secrets in release)
# ------------------------------------------------------------------------------
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# ------------------------------------------------------------------------------
# App Models and Database (Room & Entities)
# ------------------------------------------------------------------------------
-keep class com.izinkompe.data.model.** { *; }
-keep class com.izinkompe.data.db.** { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# ------------------------------------------------------------------------------
# Services & Receivers
# ------------------------------------------------------------------------------
-keep class com.izinkompe.receiver.AdminReceiver { *; }
-keep class com.izinkompe.receiver.BootReceiver { *; }
-keep class com.izinkompe.service.AppMonitorService { *; }

# ------------------------------------------------------------------------------
# DataStore Preferences
# ------------------------------------------------------------------------------
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# ------------------------------------------------------------------------------
# Apache Commons Codec (Base32 for TOTP)
# ------------------------------------------------------------------------------
-keep class org.apache.commons.codec.binary.Base32 { *; }
-keep class org.apache.commons.codec.binary.BaseNCodec { *; }
-keep class org.apache.commons.codec.** { *; }
-dontwarn org.apache.commons.codec.**

# ------------------------------------------------------------------------------
# ZXing (QR Code generation and scanning)
# ------------------------------------------------------------------------------
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**
-keep class com.journeyapps.barcodescanner.** { *; }
-dontwarn com.journeyapps.barcodescanner.**

# ------------------------------------------------------------------------------
# Kotlin Coroutines
# ------------------------------------------------------------------------------
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
