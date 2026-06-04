package com.example.roadguideapp.map

import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Map color presets similar to Mapbox day / dawn / dusk / night appearances.
 * Values are MapLibre-compatible color strings (#RRGGBB or #RRGGBBAA).
 */
internal enum class MapTimeOfDay(val label: String, val palette: MapPalette) {
    Dawn(
        label = "Dawn",
        palette = MapPalette(
            background = "#F4EFE8",
            water = "#A8C8E8",
            waterOutline = "#7EB3E2",
            landcover = "#D7E5C2",
            landuse = "#E9E3DC",
            park = "#CBE7C7",
            roadCasing = "#D8D0C8",
            road = "#FFFDF8",
            roadRail = "#B8B8B8",
            roadPath = "#F5F5F0",
            roadFerry = "#7EB3E2",
            buildingFill = "#DDD7D0",
            buildingOutline = "#C5BEB7",
            buildingExtrusion = "#D0C8C0",
            boundary = "#B2A9A2",
        ),
    ),
    Day(
        label = "Day",
        palette = MapPalette(
            background = "#F7F4EF",
            water = "#9FD0F8",
            waterOutline = "#72B9F0",
            landcover = "#D8E7C5",
            landuse = "#EBE8E4",
            park = "#C8EAC8",
            roadCasing = "#D5D0C9",
            road = "#FFFFFF",
            roadRail = "#BBBBBB",
            roadPath = "#FFFFFF",
            roadFerry = "#72B9F0",
            buildingFill = "#DED9D2",
            buildingOutline = "#C4BFB7",
            buildingExtrusion = "#CEC8C0",
            boundary = "#AAA7A1",
        ),
    ),
    Dusk(
        label = "Dusk",
        palette = MapPalette(
            background = "#2E313A",
            water = "#304D6D",
            waterOutline = "#243D58",
            landcover = "#374036",
            landuse = "#343743",
            park = "#304738",
            roadCasing = "#4A4E59",
            road = "#707682",
            roadRail = "#5C6068",
            roadPath = "#8A909C",
            roadFerry = "#4A6A8A",
            buildingFill = "#474B56",
            buildingOutline = "#393D47",
            buildingExtrusion = "#555A66",
            boundary = "#707787",
        ),
    ),
    Night(
        label = "Night",
        palette = MapPalette(
            background = "#2F313A",
            water = "#28384C",
            waterOutline = "#1F2D3E",
            landcover = "#343A34",
            landuse = "#333541",
            park = "#304237",
            roadCasing = "#565A65",
            road = "#7A808C",
            roadRail = "#666A74",
            roadPath = "#949AA6",
            roadFerry = "#3D5A78",
            buildingFill = "#444854",
            buildingOutline = "#373B45",
            buildingExtrusion = "#555A67",
            boundary = "#747B8A",
        ),
    ),
    ;

    companion object {
        /**
         * Picks appearance from the device's default time zone and local wall clock
         * (same zone as [java.time.ZoneId.systemDefault]).
         */
        fun fromSystemLocalClock(): MapTimeOfDay {
            val hour = ZonedDateTime.now(ZoneId.systemDefault()).hour
            return when (hour) {
                in 5..7 -> Dawn
                in 8..17 -> Day
                in 18..20 -> Dusk
                else -> Night // 21:00–04:59
            }
        }
    }
}

internal data class MapPalette(
    val background: String,
    val water: String,
    val waterOutline: String,
    val landcover: String,
    val landuse: String,
    val park: String,
    val roadCasing: String,
    val road: String,
    val roadRail: String,
    val roadPath: String,
    val roadFerry: String,
    val buildingFill: String,
    val buildingOutline: String,
    val buildingExtrusion: String,
    val boundary: String,
)
