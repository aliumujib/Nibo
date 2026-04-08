# Consumer ProGuard rules for Nibo library

# Keep Retrofit interfaces
-keepattributes Signature
-keepattributes *Annotation*

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep Nibo public API
-keep class com.aliumujib.nibo.PlacePickerScreen* { *; }
-keep class com.aliumujib.nibo.PlacePickerActivity* { *; }
-keep class com.aliumujib.nibo.PlacePickerContract* { *; }
-keep class com.aliumujib.nibo.PlacePickerInput* { *; }
-keep class com.aliumujib.nibo.data.PlacePickerConfig { *; }
-keep class com.aliumujib.nibo.data.SelectedPlace { *; }
-keep class com.aliumujib.nibo.data.PlacePrediction { *; }
-keep class com.aliumujib.nibo.data.StructuredFormatting { *; }
-keep class com.aliumujib.nibo.data.PlaceTypes { *; }

# Keep internal API response models for Gson deserialization
-keep class com.aliumujib.nibo.api.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
}
