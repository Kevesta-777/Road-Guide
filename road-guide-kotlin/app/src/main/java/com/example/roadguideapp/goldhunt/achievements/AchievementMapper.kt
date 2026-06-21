package com.example.roadguideapp.goldhunt.achievements

internal object AchievementMapper {
    fun toDomain(entity: AchievementEntity): Achievement? {
        val category = AchievementCategory.fromId(entity.category) ?: return null
        return Achievement(
            achievementId = entity.achievementId,
            title = entity.title,
            description = entity.description,
            category = category,
            targetValue = entity.targetValue.coerceAtLeast(1),
            currentValue = entity.currentValue.coerceAtLeast(0),
            completed = entity.completed,
            completionDate = entity.completionDateMs,
            rewardCredits = entity.rewardCredits.coerceAtLeast(0),
            rewardXp = entity.rewardXp.coerceAtLeast(0L),
            legendaryRelicKey = entity.legendaryRelicKey,
            titleKey = entity.titleKey,
            badgeKey = entity.badgeKey,
            schemaVersion = entity.schemaVersion,
            extensionJson = entity.extensionJson,
        )
    }

    fun toEntity(
        achievement: Achievement,
        updatedAtMs: Long = System.currentTimeMillis(),
    ): AchievementEntity = AchievementEntity(
        achievementId = achievement.achievementId,
        title = achievement.title,
        description = achievement.description,
        category = achievement.category.id,
        targetValue = achievement.targetValue,
        currentValue = achievement.currentValue,
        completed = achievement.completed,
        completionDateMs = achievement.completionDate,
        rewardCredits = achievement.rewardCredits,
        rewardXp = achievement.rewardXp,
        legendaryRelicKey = achievement.legendaryRelicKey,
        titleKey = achievement.titleKey,
        badgeKey = achievement.badgeKey,
        schemaVersion = achievement.schemaVersion,
        extensionJson = achievement.extensionJson,
        updatedAtMs = updatedAtMs,
    )

    fun fromDefinition(
        definition: AchievementDefinition,
        currentValue: Int = 0,
        completed: Boolean = false,
        completionDate: Long? = null,
    ): Achievement {
        val targetValue = definition.targetCount.coerceAtLeast(1)
        val resolvedCurrentValue = currentValue.coerceAtLeast(0)
        val resolvedCompleted = completed || resolvedCurrentValue >= targetValue
        val resolvedCompletionDate = when {
            resolvedCompleted -> completionDate ?: System.currentTimeMillis()
            else -> null
        }
        return Achievement(
            achievementId = definition.key,
            title = definition.displayName,
            description = AchievementDescriptions.forCategory(definition.category),
            category = definition.category,
            targetValue = targetValue,
            currentValue = if (resolvedCompleted) targetValue else resolvedCurrentValue,
            completed = resolvedCompleted,
            completionDate = resolvedCompletionDate,
            rewardCredits = AchievementCatalog.scaledCompletionCredits(definition.category),
            rewardXp = AchievementCatalog.scaledCompletionXp(definition.category),
            legendaryRelicKey = definition.legendaryRelicKey,
            titleKey = AchievementSchema.TitleKeys.forCategory(definition.category),
            badgeKey = AchievementSchema.BadgeKeys.forCategory(definition.category),
            schemaVersion = definition.schemaVersion,
            extensionJson = definition.extensionJson,
        )
    }
}

internal object AchievementDescriptions {
    fun forCategory(category: AchievementCategory): String = when (category) {
        AchievementCategory.EXPLORATION ->
            "Expand your explored map and push your discovery footprint further."
        AchievementCategory.TREASURE ->
            "Collect treasures scattered across the Gold Hunt world."
        AchievementCategory.RARITY ->
            "Discover treasures of increasingly rare tiers."
        AchievementCategory.SECRET_PLACE ->
            "Find and complete secret place categories hidden on the map."
        AchievementCategory.TREASURE_CLUSTER ->
            "Discover and complete treasure cluster families."
        AchievementCategory.STORY ->
            "Unlock story fragments tied to your exploration journey."
        AchievementCategory.RADAR ->
            "Master radar pulses and long-range target detection."
        AchievementCategory.PANORAMA ->
            "Complete panorama hunt families and hidden symbol challenges."
        AchievementCategory.SEASONAL_EVENT ->
            "Participate in and complete live seasonal event cycles."
        AchievementCategory.LEGENDARY_RELIC ->
            "Earn legendary relic hooks through high-tier achievements."
        AchievementCategory.STREAK ->
            "Maintain active exploration and reward streaks."
        AchievementCategory.MASTER_EXPLORER ->
            "Reach the capstone milestone of the Explorer progression path."
    }
}
