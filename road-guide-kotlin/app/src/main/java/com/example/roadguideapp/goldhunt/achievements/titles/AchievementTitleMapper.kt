package com.example.roadguideapp.goldhunt.achievements.titles

internal object AchievementTitleMapper {
    fun toDomain(
        definition: AchievementTitleDefinition,
        entity: AchievementTitleEntryEntity?,
        activeTitleKey: String?,
    ): AchievementTitleEntry = AchievementTitleEntry(
        titleKey = definition.titleKey,
        achievementId = definition.achievementId,
        displayTitle = entity?.displayTitle?.takeIf { it.isNotBlank() } ?: definition.displayTitle,
        rarity = entity?.rarity?.let { AchievementTitleRarity.fromId(it) } ?: definition.rarity,
        unlockDateMs = entity?.unlockDateMs?.takeIf { it > 0L },
        isActive = definition.titleKey == activeTitleKey,
        schemaVersion = entity?.schemaVersion ?: AchievementTitleSchema.VERSION,
        extensionJson = entity?.extensionJson ?: AchievementTitleSchema.EMPTY_EXTENSIONS_JSON,
    )

    fun toEntity(
        definition: AchievementTitleDefinition,
        unlockDateMs: Long?,
        timestampMs: Long,
    ): AchievementTitleEntryEntity = AchievementTitleEntryEntity(
        titleKey = definition.titleKey,
        achievementId = definition.achievementId,
        displayTitle = definition.displayTitle,
        rarity = definition.rarity.id,
        unlockDateMs = unlockDateMs?.takeIf { it > 0L },
        updatedAtMs = timestampMs,
    )

    fun buildProgress(entries: List<AchievementTitleEntry>): AchievementTitleProgress {
        val unlocked = entries.filter { it.isUnlocked }
        return AchievementTitleProgress(
            totalCount = entries.size,
            unlockedCount = unlocked.size,
            lockedCount = entries.count { it.isLocked },
            activeCount = entries.count { it.isActive },
        )
    }
}
