package com.example.roadguideapp.goldhunt.relics.rewards

import android.content.Context
import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeDao
import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeMapper
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleDao
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleMapper
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import com.example.roadguideapp.goldhunt.profile.xp.GameplayXpAwarder
import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicCatalog
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicDao
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicMapper
import com.example.roadguideapp.goldhunt.relics.statistics.LegendaryRelicStatisticsManager
import com.example.roadguideapp.goldhunt.relics.chain.RelicHuntChainUnlockManager
import com.example.roadguideapp.goldhunt.relics.story.LegendaryRelicStoryHookRepository
import com.example.roadguideapp.goldhunt.repository.GoldHuntRepository

/**
 * Idempotent orchestrator for legendary relic completion rewards.
 *
 * Credits and XP use existing ledger idempotency keys; story fragments, titles,
 * badges, and future cosmetics persist via [LegendaryRelicRewardHookRepository].
 * Grant rows in [LegendaryRelicRewardGrantRepository] prevent duplicate payouts.
 */
internal class LegendaryRelicRewardGrantManager private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val relicDao: LegendaryRelicDao = GoldHuntDatabase.get(appContext).legendaryRelicDao()
    private val badgeDao: AchievementBadgeDao = GoldHuntDatabase.get(appContext).achievementBadgeDao()
    private val titleDao: AchievementTitleDao = GoldHuntDatabase.get(appContext).achievementTitleDao()
    private val goldHuntRepository by lazy { GoldHuntRepository.get(appContext) }
    private val gameplayXp = GameplayXpAwarder.get(appContext)
    private val grantRepository = LegendaryRelicRewardGrantRepository.get(appContext)
    private val hookRepository = LegendaryRelicRewardHookRepository.get(appContext)
    private val statisticsManager = LegendaryRelicStatisticsManager.get(appContext)
    private val storyHookRepository = LegendaryRelicStoryHookRepository.get(appContext)
    private val chainUnlockManager = RelicHuntChainUnlockManager.get(appContext)

    suspend fun tryGrantRewards(
        relic: LegendaryRelic,
        timestampMs: Long = System.currentTimeMillis(),
    ): LegendaryRelicRewardGrantResult {
        if (!relic.completed) return LegendaryRelicRewardGrantResult.skipped
        if (grantRepository.isGranted(relic.relicId)) {
            return LegendaryRelicRewardGrantResult.skipped
        }

        val outcome = LegendaryRelicRewardDispatcher.dispatch(relic)
        val record = grantRepository.recordGrant(
            LegendaryRelicRewardGrantEntity(
                relicId = relic.relicId,
                creditsGranted = outcome.creditGrant.amount,
                xpGranted = outcome.xp,
                storyFragmentKey = outcome.storyFragmentKey,
                storyFragmentRecorded = false,
                titleKey = outcome.titleKey,
                titleRecorded = false,
                badgeKey = outcome.badgeKey,
                badgeRecorded = false,
                cosmeticKey = outcome.cosmeticKey,
                cosmeticRecorded = false,
                grantedAtMs = timestampMs,
            ),
        )
        if (!record.isNew) {
            return LegendaryRelicRewardGrantResult.skipped
        }

        goldHuntRepository.ensureInitialized()
        val creditsGranted = goldHuntRepository.applyCreditGrant(outcome.creditGrant, timestampMs)
        val xpOutcome = gameplayXp.tryAwardLegendaryRelicCompletion(
            relicId = relic.relicId,
            category = relic.category,
            xpAwarded = outcome.xp,
            name = relic.name,
            timestampMs = timestampMs,
        )
        val hookResult = hookRepository.recordCompletionHooks(
            outcome = outcome,
            timestampMs = timestampMs,
        )
        if (hookResult.badgeRecorded) {
            recordBadgeUnlock(relic, timestampMs)
        }
        if (hookResult.titleRecorded) {
            recordTitleUnlock(relic, timestampMs)
        }
        if (hookResult.storyFragmentRecorded) {
            storyHookRepository.recordCompletionLoreHook(
                relic = relic,
                definition = LegendaryRelicCatalog.findById(relic.relicId),
                timestampMs = timestampMs,
            )
        }
        chainUnlockManager.dispatchChainUnlocks(relic, timestampMs)
        val grantResult = LegendaryRelicRewardGrantResult(
            relicId = relic.relicId,
            creditsGranted = creditsGranted,
            xpGranted = xpOutcome.xpAwarded,
            storyFragmentRecorded = hookResult.storyFragmentRecorded,
            titleRecorded = hookResult.titleRecorded,
            badgeRecorded = hookResult.badgeRecorded,
            cosmeticRecorded = hookResult.cosmeticRecorded,
            isNewGrant = true,
        )
        statisticsManager.recordGrant(relic, grantResult, timestampMs)
        statisticsManager.reconcile(timestampMs)
        return grantResult
    }

    suspend fun tryGrantRewardsForRelicId(
        relicId: String,
        timestampMs: Long = System.currentTimeMillis(),
    ): LegendaryRelicRewardGrantResult {
        val relic = relicDao.get(relicId)?.let { LegendaryRelicMapper.toDomain(it) }
            ?: return LegendaryRelicRewardGrantResult.skipped
        return tryGrantRewards(relic, timestampMs)
    }

    suspend fun reconcilePendingGrants(
        timestampMs: Long = System.currentTimeMillis(),
    ): List<LegendaryRelicRewardGrantResult> =
        relicDao.getAll()
            .mapNotNull { LegendaryRelicMapper.toDomain(it) }
            .filter { it.completed }
            .mapNotNull { relic ->
                val result = tryGrantRewards(relic, timestampMs)
                result.takeIf { it.isNewGrant }
            }

    private suspend fun recordBadgeUnlock(
        relic: LegendaryRelic,
        timestampMs: Long,
    ) {
        val definition = LegendaryRelicRewardBadgeCatalog.definitionForRelic(relic) ?: return
        val existing = badgeDao.get(definition.badgeKey)
        val unlockDate = when {
            existing?.unlockDateMs != null && existing.unlockDateMs > 0L ->
                minOf(existing.unlockDateMs, timestampMs)
            else -> timestampMs
        }
        badgeDao.upsert(
            AchievementBadgeMapper.toEntity(
                definition = definition,
                unlockDateMs = unlockDate,
                timestampMs = timestampMs,
            ),
        )
    }

    private suspend fun recordTitleUnlock(
        relic: LegendaryRelic,
        timestampMs: Long,
    ) {
        val definition = LegendaryRelicRewardTitleCatalog.definitionForRelic(relic) ?: return
        val existing = titleDao.get(definition.titleKey)
        val unlockDate = when {
            existing?.unlockDateMs != null && existing.unlockDateMs > 0L ->
                minOf(existing.unlockDateMs, timestampMs)
            else -> timestampMs
        }
        titleDao.upsert(
            AchievementTitleMapper.toEntity(
                definition = definition,
                unlockDateMs = unlockDate,
                timestampMs = timestampMs,
            ),
        )
    }

    companion object {
        @Volatile
        private var instance: LegendaryRelicRewardGrantManager? = null

        fun get(context: Context): LegendaryRelicRewardGrantManager =
            instance ?: synchronized(this) {
                instance ?: LegendaryRelicRewardGrantManager(context.applicationContext)
                    .also { instance = it }
            }
    }
}
