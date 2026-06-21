package com.example.roadguideapp.map

import org.junit.Assert.assertEquals
import org.junit.Test

class MapAppearanceTest {

    @Test
    fun resolveMapTimeOfDay_usesClockWhenNotOverridden() {
        assertEquals(
            MapTimeOfDay.Dusk,
            resolveMapTimeOfDay(
                clock = MapTimeOfDay.Dusk,
                appearanceManuallyOverridden = false,
                isDarkAppearance = false,
            ),
        )
        assertEquals(
            MapTimeOfDay.Dawn,
            resolveMapTimeOfDay(
                clock = MapTimeOfDay.Dawn,
                appearanceManuallyOverridden = false,
                isDarkAppearance = true,
            ),
        )
    }

    @Test
    fun resolveMapTimeOfDay_manualOverrideMapsToDayOrNight() {
        assertEquals(
            MapTimeOfDay.Night,
            resolveMapTimeOfDay(
                clock = MapTimeOfDay.Day,
                appearanceManuallyOverridden = true,
                isDarkAppearance = true,
            ),
        )
        assertEquals(
            MapTimeOfDay.Day,
            resolveMapTimeOfDay(
                clock = MapTimeOfDay.Night,
                appearanceManuallyOverridden = true,
                isDarkAppearance = false,
            ),
        )
    }
}
