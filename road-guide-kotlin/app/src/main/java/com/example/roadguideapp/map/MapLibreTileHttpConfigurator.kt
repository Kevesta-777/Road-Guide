package com.example.roadguideapp.map

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.maplibre.android.module.http.HttpRequestUtil

/**
 * Applies a disk-backed [OkHttpClient] cache to MapLibre’s tile/font/sprite HTTP downloads so
 * previously fetched resources can be reused for the same tileserver URLs without hitting the wire.
 * Sprites/glyphs use the live tileserver when reachable; [MapLibreBundledResourceInterceptor]
 * only substitutes APK assets when the server is down.
 */
internal object MapLibreTileHttpConfigurator {

    private const val CACHE_SUBDIR = "maplibre_okhttp_disk_cache"
    private const val CACHE_MAX_BYTES = 256L * 1024L * 1024L

    fun install(applicationContext: Context) {
        val cacheDir = applicationContext.applicationContext.filesDir
            .resolve(CACHE_SUBDIR)
            .apply { mkdirs() }
        val cache = Cache(cacheDir, CACHE_MAX_BYTES)
        val client = OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(MapLibreBundledResourceInterceptor(applicationContext))
            .addInterceptor(TileserverOfflineCacheInterceptor())
            .build()
        HttpRequestUtil.setOkHttpClient(client)
    }
}
