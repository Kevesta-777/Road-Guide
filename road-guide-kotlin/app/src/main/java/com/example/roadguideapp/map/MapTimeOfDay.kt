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
            background = "#f0dfe8",
            water = "#9bb8d9",
            waterOutline = "#7a9fc9",
            landcover = "#c9dcb8",
            landuse = "#e0d4dc",
            park = "#b8d9c4",
            roadCasing = "#d4c8ce",
            road = "#fff8fc",
            buildingFill = "#e2d2da",
            buildingOutline = "#c4b4bc",
            buildingExtrusion = "#d8c8d0",
            boundary = "#b8a0b0",
        ),
    ),
    Day(
        label = "Day",
        palette = MapPalette(
            background = "#f8f4f0",
            water = "#a0c8f0",
            waterOutline = "#74b5e8",
            landcover = "#d4e4bc",
            landuse = "#e8e4e4",
            park = "#c8facc",
            roadCasing = "#cfcdca",
            road = "#ffffff",
            buildingFill = "#dcd9d5",
            buildingOutline = "#bfbab5",
            buildingExtrusion = "#c8c2bc",
            boundary = "#9e9cab",
        ),
    ),
    Dusk(
        label = "Dusk",
        palette = MapPalette(
            background = "#2a2438",
            water = "#2a4a6a",
            waterOutline = "#1f3a58",
            landcover = "#3a3d32",
            landuse = "#353040",
            park = "#2f4538",
            roadCasing = "#4a424f",
            road = "#6a6270",
            buildingFill = "#4a4455",
            buildingOutline = "#3a3545",
            buildingExtrusion = "#5a5465",
            boundary = "#6a5a78",
        ),
    ),
    Night(
        label = "Night",
        palette = MapPalette(
            background = "#1a1d24",
            water = "#162030",
            waterOutline = "#0f1828",
            landcover = "#222820",
            landuse = "#242228",
            park = "#1a2820",
            roadCasing = "#2a2e38",
            road = "#3d4450",
            buildingFill = "#2e323c",
            buildingOutline = "#22262e",
            buildingExtrusion = "#383c48",
            boundary = "#4a5060",
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
    val buildingFill: String,
    val buildingOutline: String,
    val buildingExtrusion: String,
    val boundary: String,
)
