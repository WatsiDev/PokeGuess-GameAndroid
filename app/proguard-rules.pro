# R8 / ProGuard rules for PokeGuess Redux

# ----------------------------------------------------------------
# General & Reflection Attributes
# ----------------------------------------------------------------
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable

# ----------------------------------------------------------------
# Keep Project Data Models & Serialized Fields
# ----------------------------------------------------------------
# Protect all Data Models and Entities to ensure API responses match Moshi/Room
-keep class com.watsidev.pokeguessredux.data.model.** { *; }
-keepclassmembers class com.watsidev.pokeguessredux.data.model.** { *; }

-keep class com.watsidev.pokeguessredux.data.remote.** { *; }
-keepclassmembers class com.watsidev.pokeguessredux.data.remote.** { *; }

-keep class com.watsidev.pokeguessredux.data.local.** { *; }
-keepclassmembers class com.watsidev.pokeguessredux.data.local.** { *; }

-keep class com.watsidev.pokeguessredux.domain.model.** { *; }
-keepclassmembers class com.watsidev.pokeguessredux.domain.model.** { *; }

# Keep classes annotated with @Keep
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# ----------------------------------------------------------------
# Moshi Json Adapter Rules
# ----------------------------------------------------------------
-keep class * extends com.squareup.moshi.JsonAdapter
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}

# ----------------------------------------------------------------
# Kotlinx Serialization Rules
# ----------------------------------------------------------------
-keepattributes *Annotation*, ElementValuePairs, Signature
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# ----------------------------------------------------------------
# Room Database Rules
# ----------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Entity class * { *; }

# ----------------------------------------------------------------
# Retrofit & OkHttp
# ----------------------------------------------------------------
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ----------------------------------------------------------------
# Google Play In-App Updates API & Play Services
# ----------------------------------------------------------------
-keep class com.google.android.play.core.** { *; }
-keep class com.google.android.gms.ads.** { *; }

# ----------------------------------------------------------------
# Lottie Compose
# ----------------------------------------------------------------
-keep class com.airbnb.lottie.** { *; }
