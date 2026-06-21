package com.example.roadguideapp.goldhunt.events

/**
 * Annual calendar window for a seasonal event (month/day only, no year).
 */
internal data class SeasonalEventWindow(
    val startMonth: Int,
    val startDay: Int,
    val endMonth: Int,
    val endDay: Int,
) {
    init {
        require(startMonth in 1..12) { "startMonth must be 1..12" }
        require(endMonth in 1..12) { "endMonth must be 1..12" }
        require(startDay in 1..SeasonalEventCalendar.daysInMonth(startMonth)) {
            "startDay invalid for startMonth"
        }
        require(endDay in 1..SeasonalEventCalendar.daysInMonth(endMonth)) {
            "endDay invalid for endMonth"
        }
    }

    fun contains(month: Int, day: Int): Boolean {
        require(month in 1..12) { "month must be 1..12" }
        require(day in 1..SeasonalEventCalendar.daysInMonth(month)) { "day invalid for month" }
        val today = SeasonalEventCalendar.dayOfYear(month, day)
        val start = SeasonalEventCalendar.dayOfYear(startMonth, startDay)
        val end = SeasonalEventCalendar.dayOfYear(endMonth, endDay)
        return if (start <= end) {
            today in start..end
        } else {
            today >= start || today <= end
        }
    }

    companion object {
        fun from(type: SeasonalEventType): SeasonalEventWindow = SeasonalEventWindow(
            startMonth = type.startMonth,
            startDay = type.startDay,
            endMonth = type.endMonth,
            endDay = type.endDay,
        )

        fun from(template: SeasonalEventTemplate): SeasonalEventWindow = SeasonalEventWindow(
            startMonth = template.startMonth,
            startDay = template.startDay,
            endMonth = template.endMonth,
            endDay = template.endDay,
        )
    }
}

internal object SeasonalEventCalendar {
    fun daysInMonth(month: Int): Int = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> 28
        else -> 30
    }

    /** Day index 1..365 using a non-leap February for deterministic annual windows. */
    fun dayOfYear(month: Int, day: Int): Int {
        var total = day
        for (m in 1 until month) {
            total += daysInMonth(m)
        }
        return total
    }
}
