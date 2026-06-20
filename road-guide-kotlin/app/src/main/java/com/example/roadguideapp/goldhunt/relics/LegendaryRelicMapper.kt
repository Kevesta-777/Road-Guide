package com.example.roadguideapp.goldhunt.relics

internal object LegendaryRelicMapper {
    fun toDomain(entity: LegendaryRelicEntity): LegendaryRelic? {
        val category = RelicCategory.fromId(entity.category) ?: return null
        val rarity = RelicRarity.fromId(entity.rarity) ?: return null
        return LegendaryRelic(
            relicId = entity.relicId,
            name = entity.name,
            description = entity.description,
            category = category,
            rarity = rarity,
            pieceCount = entity.pieceCount.coerceAtLeast(1),
            piecesCollected = entity.piecesCollected.coerceIn(0, entity.pieceCount.coerceAtLeast(1)),
            completed = entity.completed,
            completionDate = entity.completionDateMs,
            rewardCredits = entity.rewardCredits.coerceAtLeast(0),
            rewardXp = entity.rewardXp.coerceAtLeast(0L),
            badgeKey = entity.badgeKey,
            titleKey = entity.titleKey,
            powerKey = entity.powerKey,
            schemaVersion = entity.schemaVersion,
            extensionJson = entity.extensionJson,
        )
    }

    fun toEntity(
        relic: LegendaryRelic,
        updatedAtMs: Long = System.currentTimeMillis(),
    ): LegendaryRelicEntity = LegendaryRelicEntity(
        relicId = relic.relicId,
        name = relic.name,
        description = relic.description,
        category = relic.category.id,
        rarity = relic.rarity.id,
        pieceCount = relic.pieceCount,
        piecesCollected = relic.piecesCollected,
        completed = relic.completed,
        completionDateMs = relic.completionDate,
        rewardCredits = relic.rewardCredits,
        rewardXp = relic.rewardXp,
        badgeKey = relic.badgeKey,
        titleKey = relic.titleKey,
        powerKey = relic.powerKey,
        schemaVersion = relic.schemaVersion,
        extensionJson = relic.extensionJson,
        updatedAtMs = updatedAtMs,
    )

    fun fromDefinition(
        definition: LegendaryRelicDefinition,
        piecesCollected: Int = 0,
        completed: Boolean = false,
        completionDate: Long? = null,
    ): LegendaryRelic {
        val pieceCount = definition.pieceCount.coerceAtLeast(1)
        val resolvedPiecesCollected = piecesCollected.coerceIn(0, pieceCount)
        val resolvedCompleted = completed || resolvedPiecesCollected >= pieceCount
        val resolvedCompletionDate = when {
            resolvedCompleted -> completionDate ?: System.currentTimeMillis()
            else -> null
        }
        return LegendaryRelic(
            relicId = definition.relicId,
            name = definition.name,
            description = definition.description,
            category = definition.category,
            rarity = definition.rarity,
            pieceCount = pieceCount,
            piecesCollected = if (resolvedCompleted) pieceCount else resolvedPiecesCollected,
            completed = resolvedCompleted,
            completionDate = resolvedCompletionDate,
            rewardCredits = LegendaryRelicCatalog.scaledCompletionCredits(
                category = definition.category,
                rarity = definition.rarity,
            ),
            rewardXp = LegendaryRelicCatalog.scaledCompletionXp(
                category = definition.category,
                rarity = definition.rarity,
            ),
            badgeKey = definition.badgeKey ?: definition.resolvedBadgeKey,
            titleKey = definition.titleKey ?: definition.resolvedTitleKey,
            powerKey = definition.powerKey ?: definition.resolvedPowerKey,
            schemaVersion = definition.schemaVersion,
            extensionJson = definition.extensionJson,
        )
    }
}
