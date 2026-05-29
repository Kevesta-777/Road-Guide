package com.example.roadguideapp.map

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * When the tileserver is unreachable, serve MapLibre tile/sprite/glyph requests from the OkHttp
 * disk cache (if available) instead of failing the request.
 *
 * This only affects HTTP(S) requests; PMTiles requests are handled via pmtiles://file://.
 */
internal class TileserverOfflineCacheInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = try {
            chain.proceed(request)
        } catch (_: IOException) {
            return proceedFromCache(chain, request)
        }
        if (response.isSuccessful) return response
        response.close()
        return proceedFromCache(chain, request)
    }

    private fun proceedFromCache(chain: Interceptor.Chain, request: okhttp3.Request): Response {
        val cacheRequest = request.newBuilder()
            .cacheControl(CacheControl.FORCE_CACHE)
            .build()
        return chain.proceed(cacheRequest)
    }
}

