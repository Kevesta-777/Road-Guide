package com.example.roadguideapp.goldhunt.events

/**
 * Read-only access to [SeasonalEventType] definitions.
 * Reward scaling, achievements, and story/relic hooks consume this catalog.
 */
internal object SeasonalEventCatalog {
    val displayOrder: List<SeasonalEventType> = SeasonalEventType.ALL_ORDERED

    fun fromId(id: String): SeasonalEventType? = SeasonalEventType.fromId(id)

    fun templateFor(type: SeasonalEventType): SeasonalEventTemplate = templatesById.getValue(type.id)

    fun allTemplates(): List<SeasonalEventTemplate> =
        displayOrder.map { templateFor(it) }

    private val templatesById: Map<String, SeasonalEventTemplate> =
        displayOrder.associate { type ->
            type.id to SeasonalEventTemplate.from(type)
        }
}
