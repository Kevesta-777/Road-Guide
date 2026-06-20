package com.example.roadguideapp.goldhunt.events

import java.time.LocalDate

/**
 * Resolves annual cycle years and concrete [LocalDate] windows for seasonal events.
 */
internal object SeasonalEventCycle {
    fun wrapsYearBoundary(type: SeasonalEventType): Boolean =
        wrapsYearBoundary(windowFor(type))

    fun wrapsYearBoundary(window: SeasonalEventWindow): Boolean =
        window.endMonth < window.startMonth ||
            (window.endMonth == window.startMonth && window.endDay < window.startDay)

    fun resolveCycleYear(type: SeasonalEventType, date: LocalDate): Int =
        resolveCycleYear(windowFor(type), date)

    fun resolveCycleYear(window: SeasonalEventWindow, date: LocalDate): Int {
        if (!wrapsYearBoundary(window)) return date.year
        return if (date.monthValue <= window.endMonth) {
            date.year - 1
        } else {
            date.year
        }
    }

    fun windowDates(type: SeasonalEventType, cycleYear: Int): Pair<LocalDate, LocalDate> =
        windowDates(windowFor(type), cycleYear)

    fun windowDates(window: SeasonalEventWindow, cycleYear: Int): Pair<LocalDate, LocalDate> {
        val startDate = LocalDate.of(cycleYear, window.startMonth, window.startDay)
        val endYear = if (wrapsYearBoundary(window)) cycleYear + 1 else cycleYear
        val endDate = LocalDate.of(endYear, window.endMonth, window.endDay)
        return startDate to endDate
    }

    private fun windowFor(type: SeasonalEventType): SeasonalEventWindow =
        SeasonalEventWindow.from(type)
}
