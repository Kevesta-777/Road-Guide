package com.example.roadguideapp.goldhunt.relics

/**
 * Canonical legendary relic definition for the unified catalog layer.
 *
 * Domain-specific relic sources (secret places, achievements, seasonal events)
 * may define their own shapes until migrated. New cross-domain relics should
 * use this contract so collection books, reward dispatchers, and Explorer
 * Profile share one shape.
 */
internal data class LegendaryRelicDefinition(
    val relicId: String,
    val name: String,
    val description: String,
    val category: RelicCategory,
    val rarity: RelicRarity,
    val pieceCount: Int = 1,
    val badgeKey: String? = null,
    val titleKey: String? = null,
    val powerKey: String? = null,
    val schemaVersion: Int = RelicSchema.VERSION,
    val extensionJson: String = RelicSchema.EMPTY_EXTENSIONS_JSON,
) {
    init {
        require(relicId.isNotBlank()) { "relicId must not be blank" }
        require(name.isNotBlank()) { "name must not be blank" }
        require(description.isNotBlank()) { "description must not be blank" }
        require(pieceCount >= 1) { "pieceCount must be at least 1" }
    }

    val iconKey: String get() = category.iconKey

    val resolvedBadgeKey: String get() = badgeKey ?: RelicSchema.BadgeKeys.forRelic(relicId)

    val resolvedTitleKey: String get() = titleKey ?: RelicSchema.TitleKeys.forRelic(relicId)

    val resolvedPowerKey: String get() = powerKey ?: RelicSchema.PowerKeys.forRelic(relicId)
}
