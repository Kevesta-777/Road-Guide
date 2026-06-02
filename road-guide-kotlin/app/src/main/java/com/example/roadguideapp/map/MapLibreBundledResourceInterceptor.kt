package com.example.roadguideapp.map

import android.content.Context
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response

/**
 * When the tileserver is unreachable, serves sprite/glyph HTTP from (in order):
 * 1. OkHttp disk cache (resources fetched during the last online session)
 * 2. Tileserver prefetch under `filesDir/map_offline_resources/`
 * 3. APK bundled sprites/glyphs
 *
 * When online, requests pass through so z11+ detail layers use the live tileserver pack.
 */
internal class MapLibreBundledResourceInterceptor(
    private val appContext: Context,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (shouldUseLiveTileserverSprites()) {
            return chain.proceed(request)
        }
        if (TileserverBundledResources.isTileserverSpriteOrGlyphUrl(request.url)) {
            tryServeFromOkHttpCache(chain, request)?.let { return it }
            val body = TileserverBundledResources.createOfflineHttpResponseBody(appContext, request.url)
            if (body != null) {
                return Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(body)
                    .build()
            }
        }
        return chain.proceed(request)
    }

    private fun shouldUseLiveTileserverSprites(): Boolean {
        // Before the session probe, prefer the network so online startup is not pinned to APK assets.
        if (!TileserverReachability.wasProbed()) return true
        return TileserverReachability.isReachable()
    }

    private fun tryServeFromOkHttpCache(
        chain: Interceptor.Chain,
        request: okhttp3.Request,
    ): Response? {
        val cacheRequest = request.newBuilder()
            .cacheControl(CacheControl.FORCE_CACHE)
            .build()
        return try {
            val response = chain.proceed(cacheRequest)
            if (response.isSuccessful) {
                response
            } else {
                response.close()
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
