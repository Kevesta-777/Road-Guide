# MapLibre / Mapbox SDK
-keep class org.maplibre.** { *; }
-dontwarn org.maplibre.**

# Gson (vector feature properties)
-keepattributes Signature
-keep class com.google.gson.** { *; }

# BuildConfig / map package models used via reflection in tests only
-keep class com.example.roadguideapp.BuildConfig { *; }
-keep class com.example.roadguideapp.map.MapPlaceDetail { *; }
-keep class com.example.roadguideapp.map.PeliasSearchResult { *; }

# Coroutines
-dontwarn kotlinx.coroutines.**

# OkHttp (MapLibre tile cache)
-dontwarn okhttp3.**
-dontwarn okio.**

# Friends / QR (release builds)
-keep class com.google.mlkit.** { *; }
-keep class com.google.zxing.** { *; }
-keep class com.example.roadguideapp.auth.** { *; }
