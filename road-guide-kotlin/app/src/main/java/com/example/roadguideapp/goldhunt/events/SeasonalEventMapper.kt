package com.example.roadguideapp.goldhunt.events

import java.time.LocalDate

internal object SeasonalEventMapper {
    fun toDomain(
        template: SeasonalEventTemplate,
        evaluatedAt: LocalDate = LocalDate.now(),
    ): SeasonalEvent = SeasonalEventFactory.fromTemplate(template, evaluatedAt)

    fun toDomains(
        templates: List<SeasonalEventTemplate>,
        evaluatedAt: LocalDate = LocalDate.now(),
    ): List<SeasonalEvent> = templates.map { toDomain(it, evaluatedAt) }
}
