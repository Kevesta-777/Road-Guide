package com.example.roadguideapp.goldhunt.events

import java.time.LocalDate

/**
 * Offline device calendar for seasonal activation (no network time sync).
 */
internal object SeasonalEventDeviceClock {
    fun today(): LocalDate = LocalDate.now()
}
