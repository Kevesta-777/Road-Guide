package com.example.roadguideapp.map

/**
 * Process-wide map style mode for HTTP interceptors that cannot see [MapScreenController].
 */
internal object MapStyleSession {

    @Volatile
    var mode: ResolvedMapStyle.Mode = ResolvedMapStyle.Mode.Online

    /** Bundled APK sprites only match bundled-offline vector tiles, not cached Headway detail. */
    val allowBundledSpriteFallback: Boolean
        get() = mode == ResolvedMapStyle.Mode.BundledOffline
}
