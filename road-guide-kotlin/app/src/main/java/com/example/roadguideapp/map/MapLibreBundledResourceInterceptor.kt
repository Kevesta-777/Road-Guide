package com.example.roadguideapp.map

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response

/**
 * Serves tileserver sprite/glyph HTTP URLs from APK assets or the on-device resource pack
 * when the tileserver is unreachable (or the live request fails).
 *
 * When online, MapLibre must fetch sprites/glyphs from the tileserver so z11+ detail layers
 * (POI icons, labels, etc.) match the live vector tiles. Serving the smaller APK bundle
 * instead caused missing or wrong icons after the dual-tier switch to tileserver tiles.
 */
internal class MapLibreBundledResourceInterceptor(
    private val appContext: Context,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (shouldUseLiveTileserverSprites()) {
            return chain.proceed(request)
        }
        return serveBundledOrProceed(chain, request)
    }

    private fun shouldUseLiveTileserverSprites(): Boolean {
        // Before the session probe, prefer the network so online startup is not pinned to APK assets.
        if (!TileserverReachability.wasProbed()) return true
        return TileserverReachability.isReachable()
    }

    private fun serveBundledOrProceed(chain: Interceptor.Chain, request: okhttp3.Request): Response {
        val body = TileserverBundledResources.createLocalHttpResponseBody(appContext, request.url)
        if (body != null) {
            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(body)
                .build()
        }
        return chain.proceed(request)
    }
}
