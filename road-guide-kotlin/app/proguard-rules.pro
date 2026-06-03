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

# GraphHopper offline routing
-keep class com.graphhopper.** { *; }
-dontwarn com.graphhopper.**

# JDK compiler API stub (javax.lang.model.SourceVersion) for GraphHopper on Android
-keep class javax.lang.model.SourceVersion { *; }
-keepclassmembers enum javax.lang.model.SourceVersion { *; }

# Friends / QR (release builds)
-keep class com.google.mlkit.** { *; }
-keep class com.google.zxing.** { *; }
-keep class com.example.roadguideapp.auth.** { *; }
