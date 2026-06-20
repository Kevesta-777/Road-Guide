package com.example.roadguideapp.goldhunt.events

import java.time.LocalDate

/**
 * Schedule shape for custom seasonal events (catalog types use [SeasonalEventType] windows).
 */
internal sealed class SeasonalCustomEventSchedule {
    abstract fun isActiveOn(date: LocalDate): Boolean
    abstract fun windowDates(date: LocalDate): Pair<LocalDate, LocalDate>

    data class Annual(
        val window: SeasonalEventWindow,
    ) : SeasonalCustomEventSchedule() {
        init {
            require(window.startMonth in 1..12) { "startMonth must be 1..12" }
            require(window.endMonth in 1..12) { "endMonth must be 1..12" }
        }

        override fun isActiveOn(date: LocalDate): Boolean =
            window.contains(date.monthValue, date.dayOfMonth)

        override fun windowDates(date: LocalDate): Pair<LocalDate, LocalDate> {
            val cycleYear = SeasonalEventCycle.resolveCycleYear(window, date)
            return SeasonalEventCycle.windowDates(window, cycleYear)
        }
    }

    data class FixedRange(
        val startDate: LocalDate,
        val endDate: LocalDate,
    ) : SeasonalCustomEventSchedule() {
        init {
            require(!endDate.isBefore(startDate)) { "endDate must be on or after startDate" }
        }

        override fun isActiveOn(date: LocalDate): Boolean =
            !date.isBefore(startDate) && !date.isAfter(endDate)

        override fun windowDates(date: LocalDate): Pair<LocalDate, LocalDate> =
            startDate to endDate
    }
}
