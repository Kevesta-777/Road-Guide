package com.example.roadguideapp.goldhunt.achievements.chain

import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardHookEntity
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardSchema

internal object AchievementChainResolver {
    fun buildContext(
        achievementsByKey: Map<String, com.example.roadguideapp.goldhunt.achievements.Achievement>,
        chainHooks: List<AchievementRewardHookEntity> = emptyList(),
    ): AchievementChainContext = AchievementChainContext(
        achievementsByKey = achievementsByKey,
        chainUnlockedAchievementKeys =
            chainUnlockedAchievementKeys(chainHooks) +
                derivedChainUnlockedAchievementKeys(achievementsByKey),
    )

    fun derivedChainUnlockedAchievementKeys(
        achievementsByKey: Map<String, com.example.roadguideapp.goldhunt.achievements.Achievement>,
    ): Set<String> = AchievementChainCatalog.rewards
        .asSequence()
        .filter { reward -> achievementsByKey[reward.sourceAchievementKey]?.completed == true }
        .flatMap { reward ->
            reward.unlocks.filterIsInstance<AchievementChainUnlock.Achievement>()
                .map { it.achievementKey }
        }
        .toSet()

    fun isProgressAllowed(
        achievementKey: String,
        context: AchievementChainContext,
    ): Boolean {
        if (
            AchievementChainCatalog.requiresChainUnlock(achievementKey) &&
            achievementKey !in context.chainUnlockedAchievementKeys
        ) {
            return false
        }
        return areDependenciesMet(
            gate = AchievementChainCatalog.gateFor(achievementKey),
            context = context,
        )
    }

    fun areDependenciesMet(
        gate: AchievementChainGate?,
        context: AchievementChainContext,
    ): Boolean {
        if (gate == null) return true
        return gate.dependencies.all { dependency ->
            when (dependency) {
                is AchievementChainDependency.AchievementCompleted ->
                    context.isAchievementAvailable(dependency.achievementKey)
            }
        }
    }

    fun missingDependencyKeys(
        achievementKey: String,
        context: AchievementChainContext,
    ): List<String> {
        val gate = AchievementChainCatalog.gateFor(achievementKey) ?: return emptyList()
        return gate.dependencies.mapNotNull { dependency ->
            when (dependency) {
                is AchievementChainDependency.AchievementCompleted ->
                    dependency.achievementKey.takeUnless {
                        context.isAchievementAvailable(it)
                    }
            }
        }
    }

    fun chainUnlockedAchievementKeys(
        chainHooks: List<AchievementRewardHookEntity>,
    ): Set<String> = chainHooks
        .asSequence()
        .filter { hook ->
            hook.hookType == AchievementRewardSchema.HookTypes.CHAIN_UNLOCK &&
                hook.hookKey.startsWith("${AchievementChainSchema.HookSubtypes.ACHIEVEMENT}:")
        }
        .map { hook -> hook.hookKey.substringAfter(':') }
        .filter { it.isNotBlank() }
        .toSet()
}
