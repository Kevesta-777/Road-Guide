package com.example.roadguideapp.map

/**
 * Resolves map + sheet appearance. Manual light/dark overrides the clock-based [MapTimeOfDay].
 */
internal fun resolveMapTimeOfDay(
    clock: MapTimeOfDay = MapTimeOfDay.fromSystemLocalClock(),
    isDarkAppearance: Boolean,
): MapTimeOfDay = if (isDarkAppearance) {
    MapTimeOfDay.Night
} else {
    MapTimeOfDay.Day
}

internal fun MapTimeOfDay.isDarkAppearance(): Boolean =
    this == MapTimeOfDay.Dusk || this == MapTimeOfDay.Night
