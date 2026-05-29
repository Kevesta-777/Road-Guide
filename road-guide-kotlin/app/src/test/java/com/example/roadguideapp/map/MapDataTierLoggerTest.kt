package com.example.roadguideapp.map

import com.example.roadguideapp.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MapDataTierLoggerTest {

    @Test
    fun overviewLayerVisible_belowOverviewMaxZoomForDualTier() {
        val max = BuildConfig.OVERVIEW_PMTILES_MAX_ZOOM
        assertTrue(
            MapDataTierLogger.isOverviewLayerVisible(
                (max - 0.5).toDouble(),
                ResolvedMapStyle.Mode.Online,
            ),
        )
        assertFalse(
            MapDataTierLogger.isOverviewLayerVisible(
                max.toDouble(),
                ResolvedMapStyle.Mode.Online,
            ),
        )
    }

    @Test
    fun bundledOffline_usesPmtilesAtAllZooms() {
        assertTrue(
            MapDataTierLogger.isOverviewLayerVisible(12.0, ResolvedMapStyle.Mode.BundledOffline),
        )
        assertFalse(
            MapDataTierLogger.isDetailLayerVisible(12.0, ResolvedMapStyle.Mode.BundledOffline),
        )
        assertEquals(
            MapDataTierLogger.Tier.OfflinePmtilesOnly,
            MapDataTierLogger.resolveTier(12.0, ResolvedMapStyle.Mode.BundledOffline),
        )
    }

    @Test
    fun cachedHybrid_usesDetailTierAtHighZoom() {
        val min = BuildConfig.DETAIL_TILES_MIN_ZOOM
        assertTrue(
            MapDataTierLogger.isDetailLayerVisible(
                min.toDouble(),
                ResolvedMapStyle.Mode.CachedHybrid,
            ),
        )
    }
}
