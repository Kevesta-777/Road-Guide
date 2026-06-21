package com.example.roadguideapp.goldhunt.relics.chain

import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeDefinition
import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeRarity
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleDefinition
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleRarity
import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicSchema

/**
 * Static catalog of relic hunt chain gates and completion unlock rewards.
 */
internal object RelicHuntChainCatalog {
    private val foundationCategories = RelicCategory.ALL_ORDERED.filter {
        it != RelicCategory.SEASONAL_EVENT
    }

    val gates: List<RelicHuntChainGate> = buildList {
        addAll(categoryProgressionGates())
        add(hiddenCapstoneGate())
    }

    val rewards: List<RelicHuntChainReward> = buildList {
        addAll(categoryCompletionRewards())
        add(hiddenCapstoneReward())
    }

    fun gateFor(relicId: String): RelicHuntChainGate? =
        gates.firstOrNull { it.relicId == relicId }

    fun rewardsFor(sourceRelicId: String): List<RelicHuntChainUnlock> =
        rewards.firstOrNull { it.sourceRelicId == sourceRelicId }?.unlocks.orEmpty()

    val chainUnlockedRelicTargets: Set<String> by lazy {
        rewards.flatMap { reward ->
            reward.unlocks.filterIsInstance<RelicHuntChainUnlock.LegendaryRelic>()
                .map { it.relicKey }
        }.toSet()
    }

    fun requiresChainUnlock(relicId: String): Boolean =
        relicId in chainUnlockedRelicTargets

    fun chainBadgeDefinitions(): List<AchievementBadgeDefinition> =
        rewards.flatMap { reward ->
            reward.unlocks.filterIsInstance<RelicHuntChainUnlock.Badge>().map { unlock ->
                AchievementBadgeDefinition(
                    badgeKey = unlock.badgeKey,
                    achievementId = reward.sourceRelicId,
                    title = badgeTitleFor(unlock.badgeKey),
                    iconKey = badgeIconFor(unlock.badgeKey),
                    rarity = badgeRarityFor(unlock.badgeKey),
                )
            }
        }.distinctBy { it.badgeKey }

    fun chainTitleDefinitions(): List<AchievementTitleDefinition> =
        rewards.flatMap { reward ->
            reward.unlocks.filterIsInstance<RelicHuntChainUnlock.Title>().map { unlock ->
                AchievementTitleDefinition(
                    titleKey = unlock.titleKey,
                    achievementId = reward.sourceRelicId,
                    displayTitle = titleLabelFor(unlock.titleKey),
                    rarity = titleRarityFor(unlock.titleKey),
                )
            }
        }.distinctBy { it.titleKey }

    private fun categoryProgressionGates(): List<RelicHuntChainGate> =
        foundationCategories.zipWithNext { parent, child ->
            gate(
                relicId = RelicSchema.RelicKeys.forCategory(child),
                requiresRelic = RelicSchema.RelicKeys.forCategory(parent),
            )
        }

    private fun hiddenCapstoneGate(): RelicHuntChainGate = gate(
        relicId = RelicSchema.RelicKeys.forCategory(RelicCategory.HIDDEN),
        requiresRelic = RelicSchema.RelicKeys.forCategory(RelicCategory.ROYAL),
    )

    private fun categoryCompletionRewards(): List<RelicHuntChainReward> = listOf(
        reward(
            RelicSchema.RelicKeys.forCategory(RelicCategory.EXPLORER),
            RelicHuntChainUnlock.StoryFragment(
                "${RelicHuntChainSchema.KeyPrefixes.STORY_FRAGMENT}_explorer_pathfinder",
            ),
        ),
        reward(
            RelicSchema.RelicKeys.forCategory(RelicCategory.STORY),
            RelicHuntChainUnlock.Badge("${RelicHuntChainSchema.KeyPrefixes.BADGE}_story_chronicler"),
            RelicHuntChainUnlock.StoryFragment(
                "${RelicHuntChainSchema.KeyPrefixes.STORY_FRAGMENT}_story_chronicler",
            ),
        ),
        reward(
            RelicSchema.RelicKeys.forCategory(RelicCategory.WARRIOR),
            RelicHuntChainUnlock.Title("${RelicHuntChainSchema.KeyPrefixes.TITLE}_warrior_veteran"),
            RelicHuntChainUnlock.Achievement(
                RelicSchema.AchievementKeys.collector(RelicCategory.WARRIOR),
            ),
        ),
        reward(
            RelicSchema.RelicKeys.forCategory(RelicCategory.MYTHICAL),
            RelicHuntChainUnlock.LegendaryRelic(
                RelicSchema.RelicKeys.forCategory(RelicCategory.ANCIENT_CIVILIZATION),
            ),
            RelicHuntChainUnlock.StoryFragment(
                "${RelicHuntChainSchema.KeyPrefixes.STORY_FRAGMENT}_mythical_echo",
            ),
        ),
        reward(
            RelicSchema.RelicKeys.forCategory(RelicCategory.ANCIENT_CIVILIZATION),
            RelicHuntChainUnlock.Badge("${RelicHuntChainSchema.KeyPrefixes.BADGE}_ancient_scholar"),
        ),
        reward(
            RelicSchema.RelicKeys.forCategory(RelicCategory.ROYAL),
            RelicHuntChainUnlock.LegendaryRelic(
                RelicSchema.RelicKeys.forCategory(RelicCategory.HIDDEN),
            ),
            RelicHuntChainUnlock.StoryFragment(
                "${RelicHuntChainSchema.KeyPrefixes.STORY_FRAGMENT}_royal_legacy",
            ),
            RelicHuntChainUnlock.Achievement(
                RelicSchema.AchievementKeys.mastery(RelicCategory.ROYAL),
            ),
        ),
    )

    private fun hiddenCapstoneReward(): RelicHuntChainReward = reward(
        RelicSchema.RelicKeys.forCategory(RelicCategory.HIDDEN),
        RelicHuntChainUnlock.Badge("${RelicHuntChainSchema.KeyPrefixes.BADGE}_hidden_capstone"),
        RelicHuntChainUnlock.Title("${RelicHuntChainSchema.KeyPrefixes.TITLE}_hidden_capstone"),
        RelicHuntChainUnlock.StoryFragment(
            "${RelicHuntChainSchema.KeyPrefixes.STORY_FRAGMENT}_hidden_capstone",
        ),
        RelicHuntChainUnlock.Achievement(
            RelicSchema.AchievementKeys.completeAllCategories(),
        ),
    )

    private fun gate(relicId: String, requiresRelic: String): RelicHuntChainGate =
        RelicHuntChainGate(
            relicId = relicId,
            dependencies = listOf(RelicHuntChainDependency.RelicCompleted(requiresRelic)),
        )

    private fun reward(
        sourceRelicId: String,
        vararg unlocks: RelicHuntChainUnlock,
    ): RelicHuntChainReward = RelicHuntChainReward(
        sourceRelicId = sourceRelicId,
        unlocks = unlocks.toList(),
    )

    private fun badgeTitleFor(badgeKey: String): String = when {
        badgeKey.contains("story_chronicler") -> "Story Chronicler"
        badgeKey.contains("ancient_scholar") -> "Ancient Scholar"
        badgeKey.contains("hidden_capstone") -> "Hidden Capstone"
        else -> badgeKey.removePrefix("${RelicHuntChainSchema.KeyPrefixes.BADGE}_")
            .replace('_', ' ')
            .replaceFirstChar { it.uppercase() }
    }

    private fun badgeIconFor(badgeKey: String): String =
        badgeKey.removePrefix("${RelicHuntChainSchema.KeyPrefixes.BADGE}_")

    private fun badgeRarityFor(badgeKey: String): AchievementBadgeRarity = when {
        badgeKey.contains("capstone") -> AchievementBadgeRarity.LEGENDARY
        badgeKey.contains("scholar") || badgeKey.contains("chronicler") -> AchievementBadgeRarity.RARE
        else -> AchievementBadgeRarity.COMMON
    }

    private fun titleLabelFor(titleKey: String): String = when {
        titleKey.contains("warrior_veteran") -> "Warrior Veteran"
        titleKey.contains("hidden_capstone") -> "Hidden Capstone"
        else -> titleKey.removePrefix("${RelicHuntChainSchema.KeyPrefixes.TITLE}_")
            .replace('_', ' ')
            .replaceFirstChar { it.uppercase() }
    }

    private fun titleRarityFor(titleKey: String): AchievementTitleRarity = when {
        titleKey.contains("capstone") -> AchievementTitleRarity.LEGENDARY
        titleKey.contains("veteran") -> AchievementTitleRarity.RARE
        else -> AchievementTitleRarity.COMMON
    }
}
