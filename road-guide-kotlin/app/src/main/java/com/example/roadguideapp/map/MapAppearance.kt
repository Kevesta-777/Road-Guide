package com.example.roadguideapp.map

/**
 * Resolves map + sheet appearance.
 *
 * When [appearanceManuallyOverridden] is false, [clock] (local wall clock) selects Dawn/Day/Dusk/Night.
 * When the user toggles light/dark, [isDarkAppearance] maps to [MapTimeOfDay.Day] or [MapTimeOfDay.Night].
 */
internal fun resolveMapTimeOfDay(
    clock: MapTimeOfDay = MapTimeOfDay.fromSystemLocalClock(),
    appearanceManuallyOverridden: Boolean,
    isDarkAppearance: Boolean,
): MapTimeOfDay = if (appearanceManuallyOverridden) {
    if (isDarkAppearance) MapTimeOfDay.Night else MapTimeOfDay.Day
} else {
    clock
}

internal fun MapTimeOfDay.isDarkAppearance(): Boolean =
    this == MapTimeOfDay.Dusk || this == MapTimeOfDay.Night
