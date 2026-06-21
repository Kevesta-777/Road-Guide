package com.example.roadguideapp.goldhunt.achievements.badges

internal object AchievementBadgeMapper {
    fun toDomain(
        definition: AchievementBadgeDefinition,
        entity: AchievementBadgeEntryEntity?,
    ): AchievementBadgeEntry = AchievementBadgeEntry(
        badgeKey = definition.badgeKey,
        achievementId = definition.achievementId,
        title = entity?.title?.takeIf { it.isNotBlank() } ?: definition.title,
        iconKey = entity?.iconKey?.takeIf { it.isNotBlank() } ?: definition.iconKey,
        rarity = entity?.rarity?.let { AchievementBadgeRarity.fromId(it) } ?: definition.rarity,
        unlockDateMs = entity?.unlockDateMs?.takeIf { it > 0L },
        schemaVersion = entity?.schemaVersion ?: AchievementBadgeSchema.VERSION,
        extensionJson = entity?.extensionJson ?: AchievementBadgeSchema.EMPTY_EXTENSIONS_JSON,
    )

    fun toEntity(
        definition: AchievementBadgeDefinition,
        unlockDateMs: Long?,
        timestampMs: Long,
    ): AchievementBadgeEntryEntity = AchievementBadgeEntryEntity(
        badgeKey = definition.badgeKey,
        achievementId = definition.achievementId,
        title = definition.title,
        iconKey = definition.iconKey,
        rarity = definition.rarity.id,
        unlockDateMs = unlockDateMs?.takeIf { it > 0L },
        updatedAtMs = timestampMs,
    )

    fun buildProgress(entries: List<AchievementBadgeEntry>): AchievementBadgeProgress {
        val unlocked = entries.filter { it.isUnlocked }
        return AchievementBadgeProgress(
            totalCount = entries.size,
            unlockedCount = unlocked.size,
            lockedCount = entries.count { it.isLocked },
            commonUnlockedCount = unlocked.count { it.rarity == AchievementBadgeRarity.COMMON },
            rareUnlockedCount = unlocked.count { it.rarity == AchievementBadgeRarity.RARE },
            legendaryUnlockedCount = unlocked.count { it.rarity == AchievementBadgeRarity.LEGENDARY },
        )
    }
}
