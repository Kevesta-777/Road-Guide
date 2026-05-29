package com.example.roadguideapp.map

import android.util.Log
import com.example.roadguideapp.BuildConfig

/**
 * Debug logging for which map data tier is active at the current zoom (PMTiles overview vs
 * tileserver detail). Filter logcat with tag [TAG].
 */
internal object MapDataTierLogger {

    const val TAG = "MapDataTier"

    private var lastLoggedTier: Tier? = null
    private var lastLoggedZoomBucket: Int? = null

    enum class Tier {
        /** Bundled offline style — PMTiles vector at all zooms. */
        OfflinePmtilesOnly,

        /** Dual-tier: overview layers visible (PMTiles, possibly overzoomed). */
        OnlineOverviewPmtiles,

        /** Dual-tier: detail vector layers visible (tileserver or HTTP cache). */
        OnlineDetailTileserver,

        /** Dual-tier: both overview and detail layers visible (overlap). */
        OnlineOverlap,

        /** Dual-tier: no overview or detail vector layers (zoom gap). */
        OnlineGap,
    }

    fun resetThrottle() {
        lastLoggedTier = null
        lastLoggedZoomBucket = null
    }

    fun logStyleMode(mode: ResolvedMapStyle.Mode) {
        resetThrottle()
        when (mode) {
            ResolvedMapStyle.Mode.Online -> {
                if (!BuildConfig.OVERVIEW_PMTILES_ENABLED) {
                    Log.i(TAG, "Style mode: ONLINE — tileserver only (overview PMTiles disabled)")
                } else {
                    val overviewMax = BuildConfig.OVERVIEW_PMTILES_MAX_ZOOM
                    val detailMin = BuildConfig.DETAIL_TILES_MIN_ZOOM
                    Log.i(
                        TAG,
                        "Style mode: ONLINE dual-tier — PMTiles z0-$overviewMax, tileserver z$detailMin+",
                    )
                }
            }
            ResolvedMapStyle.Mode.CachedHybrid -> Log.i(
                TAG,
                "Style mode: CACHED HYBRID — dual-tier style from disk; z11+ detail from HTTP cache " +
                    "when available, else PMTiles overzoom.",
            )
            ResolvedMapStyle.Mode.BundledOffline -> Log.i(
                TAG,
                "Style mode: BUNDLED OFFLINE — PMTiles + assets/map/basic.json with local sprites/glyphs " +
                    "when prefetched (${BuildConfig.OVERVIEW_PMTILES_ASSET_PATH})",
            )
        }
    }

    fun logZoomTierIfChanged(zoom: Double, mode: ResolvedMapStyle.Mode) {
        val tier = resolveTier(zoom, mode)
        val bucket = (zoom * 10).toInt()
        if (tier == lastLoggedTier && bucket == lastLoggedZoomBucket) return
        lastLoggedTier = tier
        lastLoggedZoomBucket = bucket

        val overviewMax = BuildConfig.OVERVIEW_PMTILES_MAX_ZOOM
        val detailMin = BuildConfig.DETAIL_TILES_MIN_ZOOM
        val overviewActive = isOverviewLayerVisible(zoom, mode)
        val detailActive = isDetailLayerVisible(zoom, mode)

        Log.i(
            TAG,
            "zoom=${"%.2f".format(zoom)} tier=${tier.name} overviewLayers=$overviewActive " +
                "detailLayers=$detailActive (config: overviewMaxZoom=$overviewMax " +
                "detailMinZoom=$detailMin mode=$mode)",
        )
        when (tier) {
            Tier.OnlineGap -> Log.w(
                TAG,
                "No vector layers at this zoom — map may look empty. " +
                    "Raise overview layer maxzoom or lower detail minzoom so ranges overlap.",
            )
            Tier.OnlineOverviewPmtiles -> Log.d(TAG, "Vector tiles: bundled PMTiles (overview source)")
            Tier.OnlineDetailTileserver -> Log.d(
                TAG,
                if (mode == ResolvedMapStyle.Mode.CachedHybrid) {
                    "Vector tiles: cached tileserver (OkHttp disk cache)"
                } else {
                    "Vector tiles: live tileserver (openmaptiles)"
                },
            )
            Tier.OnlineOverlap -> Log.d(TAG, "Vector tiles: PMTiles overview + tileserver detail")
            Tier.OfflinePmtilesOnly -> Log.d(TAG, "Vector tiles: bundled PMTiles (all zooms)")
        }
    }

    fun resolveTier(zoom: Double, mode: ResolvedMapStyle.Mode): Tier {
        if (mode == ResolvedMapStyle.Mode.BundledOffline) {
            return Tier.OfflinePmtilesOnly
        }
        if (!BuildConfig.OVERVIEW_PMTILES_ENABLED) {
            return Tier.OnlineDetailTileserver
        }
        val overview = isOverviewLayerVisible(zoom, mode)
        val detail = isDetailLayerVisible(zoom, mode)
        return when {
            overview && detail -> Tier.OnlineOverlap
            overview -> Tier.OnlineOverviewPmtiles
            detail -> Tier.OnlineDetailTileserver
            else -> Tier.OnlineGap
        }
    }

    fun isOverviewLayerVisible(zoom: Double, mode: ResolvedMapStyle.Mode): Boolean {
        if (mode == ResolvedMapStyle.Mode.BundledOffline) return true
        return zoom < BuildConfig.OVERVIEW_PMTILES_MAX_ZOOM
    }

    fun isDetailLayerVisible(zoom: Double, mode: ResolvedMapStyle.Mode): Boolean {
        if (mode == ResolvedMapStyle.Mode.BundledOffline) return false
        if (!BuildConfig.OVERVIEW_PMTILES_ENABLED) return true
        return zoom >= BuildConfig.DETAIL_TILES_MIN_ZOOM
    }
}
