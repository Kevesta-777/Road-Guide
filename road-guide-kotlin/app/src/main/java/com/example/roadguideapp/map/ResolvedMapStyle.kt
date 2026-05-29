package com.example.roadguideapp.map

/**
 * Result of [TileserverStyleLoader.loadResolvedStyleJson].
 *
 * @param mode How map data is sourced for this session.
 */
internal data class ResolvedMapStyle(
    val json: String,
    val mode: Mode,
) {
    enum class Mode {
        /** Tileserver reachable; dual-tier PMTiles z0–11 + live tileserver z11+. */
        Online,

        /**
         * Tileserver down but a prior [TileserverLocationDiskCache] dual-tier style exists.
         * z11+ detail uses OkHttp disk cache when tiles were prefetched; otherwise overview
         * PMTiles layers overzoom at high zoom.
         */
        CachedHybrid,

        /**
         * Tileserver down and no cached dual-tier style; all vector layers use bundled PMTiles
         * (overzoom at high zoom) + [assets/map/basic.json] + bundled sprites/glyphs.
         */
        BundledOffline,
    }

    /** @deprecated Use [mode] == [Mode.BundledOffline]. */
    val isOfflineOnly: Boolean
        get() = mode == Mode.BundledOffline

    val usesCachedDetailTiles: Boolean
        get() = mode == Mode.CachedHybrid
}
