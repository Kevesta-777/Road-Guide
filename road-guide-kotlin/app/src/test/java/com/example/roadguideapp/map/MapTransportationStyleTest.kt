package com.example.roadguideapp.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class MapTransportationStyleTest {

    @Test
    fun lineColorForTransportationLayer_classifiesHeadwayLayerIds() {
        val palette = MapTimeOfDay.Day.palette
        assertEquals(palette.roadCasing, MapTransportationStyle.lineColorForTransportationLayer("road_motorway_casing", palette))
        assertEquals(palette.roadCasing, MapTransportationStyle.lineColorForTransportationLayer("overview_tunnel_street_casing", palette))
        assertEquals(palette.road, MapTransportationStyle.lineColorForTransportationLayer("bridge_secondary_tertiary", palette))
        assertEquals(palette.roadRail, MapTransportationStyle.lineColorForTransportationLayer("road_major_rail", palette))
        assertEquals(palette.roadPath, MapTransportationStyle.lineColorForTransportationLayer("tunnel_path_pedestrian", palette))
        assertEquals(palette.roadFerry, MapTransportationStyle.lineColorForTransportationLayer("route_ferry", palette))
    }

    @Test
    fun palettes_differAcrossTimeOfDay_forSameLayerId() {
        val dawnRoad = MapTransportationStyle.lineColorForTransportationLayer("road_minor", MapTimeOfDay.Dawn.palette)
        val nightRoad = MapTransportationStyle.lineColorForTransportationLayer("road_minor", MapTimeOfDay.Night.palette)
        assertEquals(MapTimeOfDay.Dawn.palette.road, dawnRoad)
        assertEquals(MapTimeOfDay.Night.palette.road, nightRoad)
        assertNotEquals(dawnRoad, nightRoad)
    }
}
