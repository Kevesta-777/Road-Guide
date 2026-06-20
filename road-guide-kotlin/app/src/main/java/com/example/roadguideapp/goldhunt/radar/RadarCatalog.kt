package com.example.roadguideapp.goldhunt.radar

/**
 * Read-only access to [RadarType] definitions.
 * Pulse logic, UI, and persistence consume this catalog; no generation here.
 */
internal object RadarCatalog {
    val displayOrder: List<RadarType> = RadarType.ALL_ORDERED

    fun fromId(id: String): RadarType? = RadarType.fromId(id)

    fun templateFor(type: RadarType): RadarTemplate = templatesById.getValue(type.id)

    fun templatesUnlockedAt(explorerLevel: Int): List<RadarTemplate> =
        displayOrder
            .filter { explorerLevel >= it.unlockLevel }
            .map { templateFor(it) }

    fun highestUnlockedAt(explorerLevel: Int): RadarTemplate? =
        templatesUnlockedAt(explorerLevel).lastOrNull()

    private val templatesById: Map<String, RadarTemplate> =
        displayOrder.associate { type ->
            type.id to RadarTemplate.from(type)
        }
}
