package com.example.roadguideapp.goldhunt.achievements.chain

import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.AchievementSchema
import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeDefinition
import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeRarity
import com.example.roadguideapp.goldhunt.achievements.registry.AchievementMilestoneScale
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleDefinition
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleRarity

/**
 * Static catalog of achievement chain gates and completion unlock rewards.
 */
internal object AchievementChainCatalog {
    private val milestoneCategories = listOf(
        AchievementCategory.EXPLORATION,
        AchievementCategory.TREASURE,
        AchievementCategory.STORY,
        AchievementCategory.LEGENDARY_RELIC,
    )

    val gates: List<AchievementChainGate> = buildList {
        addAll(milestoneGates())
        addAll(masterExplorerGates())
        addAll(streakGates())
        add(storyCollectorGate())
    }

    val rewards: List<AchievementChainReward> = buildList {
        addAll(milestoneChainRewards())
        addAll(masterExplorerChainRewards())
        addAll(streakChainRewards())
        add(crossCategoryPanoramaUnlock())
    }

    fun gateFor(achievementKey: String): AchievementChainGate? =
        gates.firstOrNull { it.achievementKey == achievementKey }

    fun rewardsFor(sourceAchievementKey: String): List<AchievementChainUnlock> =
        rewards.firstOrNull { it.sourceAchievementKey == sourceAchievementKey }?.unlocks.orEmpty()

    val chainUnlockedAchievementTargets: Set<String> by lazy {
        rewards.flatMap { reward ->
            reward.unlocks.filterIsInstance<AchievementChainUnlock.Achievement>()
                .map { it.achievementKey }
        }.toSet()
    }

    fun requiresChainUnlock(achievementKey: String): Boolean =
        achievementKey in chainUnlockedAchievementTargets

    fun chainBadgeDefinitions(): List<AchievementBadgeDefinition> =
        rewards.flatMap { reward ->
            reward.unlocks.filterIsInstance<AchievementChainUnlock.Badge>().map { unlock ->
                AchievementBadgeDefinition(
                    badgeKey = unlock.badgeKey,
                    achievementId = reward.sourceAchievementKey,
                    title = badgeTitleFor(unlock.badgeKey),
                    iconKey = badgeIconFor(unlock.badgeKey),
                    rarity = badgeRarityFor(unlock.badgeKey),
                )
            }
        }.distinctBy { it.badgeKey }

    fun chainTitleDefinitions(): List<AchievementTitleDefinition> =
        rewards.flatMap { reward ->
            reward.unlocks.filterIsInstance<AchievementChainUnlock.Title>().map { unlock ->
                AchievementTitleDefinition(
                    titleKey = unlock.titleKey,
                    achievementId = reward.sourceAchievementKey,
                    displayTitle = titleLabelFor(unlock.titleKey),
                    rarity = titleRarityFor(unlock.titleKey),
                )
            }
        }.distinctBy { it.titleKey }

    private fun milestoneGates(): List<AchievementChainGate> =
        milestoneCategories.flatMap { category ->
            val milestones = AchievementMilestoneScale.STANDARD
            milestones.zipWithNext { parent, child ->
                gate(
                    achievementKey = AchievementSchema.MilestoneKeys.forCategory(category, child),
                    requires = AchievementSchema.MilestoneKeys.forCategory(category, parent),
                )
            }
        }

    private fun masterExplorerGates(): List<AchievementChainGate> {
        val category = AchievementCategory.MASTER_EXPLORER
        val level20 = AchievementSchema.AchievementKeys.forCategory(category, "level_20")
        val level30 = AchievementSchema.AchievementKeys.forCategory(category, "level_30")
        val mastery = AchievementSchema.AchievementKeys.mastery(category)
        return listOf(
            gate(achievementKey = level30, requires = level20),
            gate(achievementKey = mastery, requires = level30),
        )
    }

    private fun streakGates(): List<AchievementChainGate> {
        val category = AchievementCategory.STREAK
        val days7 = AchievementSchema.AchievementKeys.forCategory(category, "days_7")
        val days30 = AchievementSchema.AchievementKeys.forCategory(category, "days_30")
        val days100 = AchievementSchema.AchievementKeys.forCategory(category, "days_100")
        return listOf(
            gate(achievementKey = days30, requires = days7),
            gate(achievementKey = days100, requires = days30),
        )
    }

    private fun storyCollectorGate(): AchievementChainGate = gate(
        achievementKey = AchievementSchema.AchievementKeys.collector(AchievementCategory.STORY),
        requires = AchievementSchema.MilestoneKeys.forCategory(
            AchievementCategory.STORY,
            AchievementMilestoneScale.STANDARD.last(),
        ),
    )

    private fun milestoneChainRewards(): List<AchievementChainReward> = listOf(
        reward(
            AchievementSchema.MilestoneKeys.forCategory(AchievementCategory.TREASURE, 100),
            AchievementChainUnlock.StoryFragment(
                "${AchievementChainSchema.KeyPrefixes.STORY_FRAGMENT}_treasure_pathfinder",
            ),
        ),
        reward(
            AchievementSchema.MilestoneKeys.forCategory(AchievementCategory.TREASURE, 500),
            AchievementChainUnlock.Badge("${AchievementChainSchema.KeyPrefixes.BADGE}_treasure_veteran"),
        ),
        reward(
            AchievementSchema.MilestoneKeys.forCategory(AchievementCategory.TREASURE, 1_000),
            AchievementChainUnlock.Title("${AchievementChainSchema.KeyPrefixes.TITLE}_treasure_legend"),
            AchievementChainUnlock.LegendaryRelic(
                "${AchievementChainSchema.KeyPrefixes.LEGENDARY_RELIC}_treasure_mastery",
            ),
        ),
        reward(
            AchievementSchema.MilestoneKeys.forCategory(AchievementCategory.EXPLORATION, 100),
            AchievementChainUnlock.StoryFragment(
                "${AchievementChainSchema.KeyPrefixes.STORY_FRAGMENT}_exploration_pathfinder",
            ),
        ),
        reward(
            AchievementSchema.MilestoneKeys.forCategory(AchievementCategory.EXPLORATION, 500),
            AchievementChainUnlock.Badge("${AchievementChainSchema.KeyPrefixes.BADGE}_exploration_veteran"),
        ),
        reward(
            AchievementSchema.MilestoneKeys.forCategory(AchievementCategory.STORY, 500),
            AchievementChainUnlock.StoryFragment(
                "${AchievementChainSchema.KeyPrefixes.STORY_FRAGMENT}_story_chronicler",
            ),
        ),
        reward(
            AchievementSchema.MilestoneKeys.forCategory(AchievementCategory.LEGENDARY_RELIC, 500),
            AchievementChainUnlock.LegendaryRelic(
                "${AchievementChainSchema.KeyPrefixes.LEGENDARY_RELIC}_relic_seeker",
            ),
        ),
    )

    private fun masterExplorerChainRewards(): List<AchievementChainReward> {
        val category = AchievementCategory.MASTER_EXPLORER
        return listOf(
            reward(
                AchievementSchema.AchievementKeys.forCategory(category, "level_20"),
                AchievementChainUnlock.Title("${AchievementChainSchema.KeyPrefixes.TITLE}_explorer_veteran"),
                AchievementChainUnlock.StoryFragment(
                    "${AchievementChainSchema.KeyPrefixes.STORY_FRAGMENT}_master_explorer_veteran",
                ),
            ),
            reward(
                AchievementSchema.AchievementKeys.mastery(category),
                AchievementChainUnlock.Badge("${AchievementChainSchema.KeyPrefixes.BADGE}_master_explorer_capstone"),
                AchievementChainUnlock.Title("${AchievementChainSchema.KeyPrefixes.TITLE}_master_explorer_capstone"),
                AchievementChainUnlock.LegendaryRelic(
                    "${AchievementChainSchema.KeyPrefixes.LEGENDARY_RELIC}_master_explorer_capstone",
                ),
            ),
        )
    }

    private fun streakChainRewards(): List<AchievementChainReward> = listOf(
        reward(
            AchievementSchema.AchievementKeys.forCategory(AchievementCategory.STREAK, "days_30"),
            AchievementChainUnlock.Badge("${AchievementChainSchema.KeyPrefixes.BADGE}_streak_dedicated"),
        ),
        reward(
            AchievementSchema.AchievementKeys.forCategory(AchievementCategory.STREAK, "days_100"),
            AchievementChainUnlock.Title("${AchievementChainSchema.KeyPrefixes.TITLE}_streak_legend"),
            AchievementChainUnlock.StoryFragment(
                "${AchievementChainSchema.KeyPrefixes.STORY_FRAGMENT}_streak_legend",
            ),
        ),
    )

    private fun crossCategoryPanoramaUnlock(): AchievementChainReward = reward(
        AchievementSchema.AchievementKeys.firstUnlock(AchievementCategory.SECRET_PLACE),
        AchievementChainUnlock.Achievement(
            AchievementSchema.AchievementKeys.firstUnlock(AchievementCategory.PANORAMA),
        ),
        AchievementChainUnlock.StoryFragment(
            "${AchievementChainSchema.KeyPrefixes.STORY_FRAGMENT}_secret_to_panorama",
        ),
    )

    private fun gate(achievementKey: String, requires: String): AchievementChainGate =
        AchievementChainGate(
            achievementKey = achievementKey,
            dependencies = listOf(
                AchievementChainDependency.AchievementCompleted(requires),
            ),
        )

    private fun reward(
        sourceAchievementKey: String,
        vararg unlocks: AchievementChainUnlock,
    ): AchievementChainReward = AchievementChainReward(
        sourceAchievementKey = sourceAchievementKey,
        unlocks = unlocks.toList(),
    )

    private fun badgeTitleFor(badgeKey: String): String = when {
        badgeKey.contains("treasure_veteran") -> "Treasure Veteran"
        badgeKey.contains("exploration_veteran") -> "Exploration Veteran"
        badgeKey.contains("streak_dedicated") -> "Dedicated Streak"
        badgeKey.contains("master_explorer_capstone") -> "Master Explorer Capstone"
        else -> badgeKey.removePrefix("${AchievementChainSchema.KeyPrefixes.BADGE}_")
            .replace('_', ' ')
            .replaceFirstChar { it.uppercase() }
    }

    private fun badgeIconFor(badgeKey: String): String =
        badgeKey.removePrefix("${AchievementChainSchema.KeyPrefixes.BADGE}_")

    private fun badgeRarityFor(badgeKey: String): AchievementBadgeRarity = when {
        badgeKey.contains("capstone") || badgeKey.contains("legend") -> AchievementBadgeRarity.LEGENDARY
        badgeKey.contains("veteran") || badgeKey.contains("dedicated") -> AchievementBadgeRarity.RARE
        else -> AchievementBadgeRarity.COMMON
    }

    private fun titleLabelFor(titleKey: String): String = when {
        titleKey.contains("treasure_legend") -> "Treasure Legend"
        titleKey.contains("explorer_veteran") -> "Veteran Explorer"
        titleKey.contains("master_explorer_capstone") -> "Capstone Explorer"
        titleKey.contains("streak_legend") -> "Streak Legend"
        else -> titleKey.removePrefix("${AchievementChainSchema.KeyPrefixes.TITLE}_")
            .replace('_', ' ')
            .replaceFirstChar { it.uppercase() }
    }

    private fun titleRarityFor(titleKey: String): AchievementTitleRarity = when {
        titleKey.contains("capstone") || titleKey.contains("legend") -> AchievementTitleRarity.LEGENDARY
        titleKey.contains("veteran") -> AchievementTitleRarity.RARE
        else -> AchievementTitleRarity.COMMON
    }
}
