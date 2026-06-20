package com.example.roadguideapp.goldhunt.events

import java.time.LocalDate

/**
 * Resolves which seasonal events are active on a calendar date.
 */
internal object SeasonalEventSchedule {
    fun activeOn(month: Int, day: Int): List<SeasonalEventType> =
        SeasonalEventCatalog.displayOrder.filter { type ->
            SeasonalEventWindow.from(type).contains(month, day)
        }

    fun activeTemplatesOn(month: Int, day: Int): List<SeasonalEventTemplate> =
        activeOn(month, day).map { SeasonalEventCatalog.templateFor(it) }

    fun activeOn(date: LocalDate): List<SeasonalEventType> =
        activeOn(date.monthValue, date.dayOfMonth)

    fun activeTemplatesOn(date: LocalDate): List<SeasonalEventTemplate> =
        activeTemplatesOn(date.monthValue, date.dayOfMonth)

    fun isActive(type: SeasonalEventType, month: Int, day: Int): Boolean =
        SeasonalEventWindow.from(type).contains(month, day)

    fun isActive(type: SeasonalEventType, date: LocalDate): Boolean =
        isActive(type, date.monthValue, date.dayOfMonth)
}
