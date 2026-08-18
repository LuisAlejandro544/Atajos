# ProGuard / R8 Rules for Flurix
# Optimized for release builds, code shrinking and obfuscation

# Keep Room SQLite entities and DAOs
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Entity class * { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    static <fields>;
}

# Keep Moshi models and serialization adapters
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-dontwarn com.squareup.moshi.**
-dontwarn javax.annotation.**
-keep class com.squareup.moshi.** { *; }
-keep class com.example.data.model.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}
-keepclassmembers class * {
    @com.squareup.moshi.JsonQualifier *;
}
-keep @com.squareup.moshi.JsonQualifier @interface * { *; }
-keep class *JsonAdapter { *; }
-keep class * extends com.squareup.moshi.JsonAdapter { *; }

# Keep OkHttp & Retrofit
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Preserve line numbers for stacktraces
-keepattributes SourceFile,LineNumberTable
