package com.example.roadguideapp.goldhunt

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiscoveryGridTest {

    @Test
    fun cellId_isStableForSameLocation() {
        val a = DiscoveryGrid.cellId(51.5, -0.12)
        val b = DiscoveryGrid.cellId(51.5, -0.12)
        assertEquals(a, b)
    }

    @Test
    fun disk_includesCenterAndNeighbors() {
        val cells = DiscoveryGrid.disk(51.5, -0.12, ring = 1)
        assertEquals(9, cells.size)
        assertTrue(cells.contains(DiscoveryGrid.cellId(51.5, -0.12)))
    }

    @Test
    fun unpack_reversesPack_forNegativeLongitude() {
        val id = DiscoveryGrid.cellId(51.5, -0.12)
        val (x, y) = DiscoveryGrid.unpack(id)
        assertTrue(x < 0)
        assertEquals(
            id,
            DiscoveryGrid.cellId(
                y * DiscoveryConfig.CELL_SIZE_DEG,
                x * DiscoveryConfig.CELL_SIZE_DEG,
            ),
        )
    }

    @Test
    fun unpack_preservesNegativeGridIndices() {
        val id = DiscoveryGrid.cellId(-33.8, -151.2)
        val (x, y) = DiscoveryGrid.unpack(id)
        assertTrue(x < 0)
        assertTrue(y < 0)
        val step = DiscoveryConfig.CELL_SIZE_DEG
        val south = y * step
        val north = south + step
        assertTrue(north > south)
    }

    @Test
    fun haversine_isZeroForSamePoint() {
        val d = DiscoveryGrid.haversineMeters(51.5, -0.1, 51.5, -0.1)
        assertEquals(0.0, d, 0.01)
    }
}
