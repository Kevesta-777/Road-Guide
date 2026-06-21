package com.example.roadguideapp.goldhunt.relics.chain

import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardHookEntity
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardSchema

internal object RelicHuntChainResolver {
    fun buildContext(
        relicsById: Map<String, com.example.roadguideapp.goldhunt.relics.LegendaryRelic>,
        chainHooks: List<LegendaryRelicRewardHookEntity> = emptyList(),
        completedAchievementKeys: Set<String> = emptySet(),
    ): RelicHuntChainContext = RelicHuntChainContext(
        relicsById = relicsById,
        chainUnlockedRelicIds =
            chainUnlockedRelicIds(chainHooks) +
                derivedChainUnlockedRelicIds(relicsById),
        completedAchievementKeys = completedAchievementKeys,
    )

    fun derivedChainUnlockedRelicIds(
        relicsById: Map<String, com.example.roadguideapp.goldhunt.relics.LegendaryRelic>,
    ): Set<String> = RelicHuntChainCatalog.rewards
        .asSequence()
        .filter { reward -> relicsById[reward.sourceRelicId]?.completed == true }
        .flatMap { reward ->
            reward.unlocks.filterIsInstance<RelicHuntChainUnlock.LegendaryRelic>()
                .map { it.relicKey }
        }
        .toSet()

    fun isProgressAllowed(
        relicId: String,
        context: RelicHuntChainContext,
    ): Boolean {
        if (
            RelicHuntChainCatalog.requiresChainUnlock(relicId) &&
            relicId !in context.chainUnlockedRelicIds &&
            !context.isRelicCompleted(relicId)
        ) {
            return false
        }
        return areDependenciesMet(
            gate = RelicHuntChainCatalog.gateFor(relicId),
            context = context,
        )
    }

    fun areDependenciesMet(
        gate: RelicHuntChainGate?,
        context: RelicHuntChainContext,
    ): Boolean {
        if (gate == null) return true
        return gate.dependencies.all { dependency ->
            when (dependency) {
                is RelicHuntChainDependency.RelicCompleted ->
                    context.isRelicAvailable(dependency.relicId)
                is RelicHuntChainDependency.AchievementCompleted ->
                    dependency.achievementKey in context.completedAchievementKeys
            }
        }
    }

    fun missingDependencyRelicIds(
        relicId: String,
        context: RelicHuntChainContext,
    ): List<String> {
        val gate = RelicHuntChainCatalog.gateFor(relicId) ?: return emptyList()
        return gate.dependencies.mapNotNull { dependency ->
            when (dependency) {
                is RelicHuntChainDependency.RelicCompleted ->
                    dependency.relicId.takeUnless { context.isRelicAvailable(it) }
                is RelicHuntChainDependency.AchievementCompleted -> null
            }
        }
    }

    fun chainUnlockedRelicIds(
        chainHooks: List<LegendaryRelicRewardHookEntity>,
    ): Set<String> = chainHooks
        .asSequence()
        .filter { hook -> hook.hookType == LegendaryRelicRewardSchema.HookTypes.CHAIN_UNLOCK }
        .map { it.hookKey }
        .filter { it.isNotBlank() && RelicHuntChainCatalog.chainUnlockedRelicTargets.contains(it) }
        .toSet()

    fun chainUnlockedAchievementKeys(
        chainHooks: List<LegendaryRelicRewardHookEntity>,
    ): Set<String> = chainHooks
        .asSequence()
        .filter { hook ->
            hook.hookType == LegendaryRelicRewardSchema.HookTypes.CHAIN_UNLOCK &&
                hook.hookKey.startsWith("${RelicHuntChainSchema.HookSubtypes.ACHIEVEMENT}:")
        }
        .map { hook -> hook.hookKey.substringAfter(':') }
        .filter { it.isNotBlank() }
        .toSet()
}
