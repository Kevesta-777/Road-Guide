package com.example.roadguideapp.goldhunt.achievements.rewards

import android.content.Context
import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.AchievementDao
import com.example.roadguideapp.goldhunt.achievements.AchievementMapper
import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeRepository
import com.example.roadguideapp.goldhunt.achievements.chain.AchievementChainUnlockManager
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleRepository
import com.example.roadguideapp.goldhunt.achievements.notifications.AchievementNotificationCenter
import com.example.roadguideapp.goldhunt.achievements.statistics.AchievementStatisticsManager
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import com.example.roadguideapp.goldhunt.profile.xp.GameplayXpAwarder
import com.example.roadguideapp.goldhunt.repository.GoldHuntRepository

/**
 * Idempotent orchestrator for unified achievement completion rewards.
 *
 * Credits and XP use existing ledger idempotency keys; story fragments, relic
 * progress, titles, and badges persist via [AchievementRewardHookRepository].
 * Grant rows in [AchievementRewardGrantRepository] prevent duplicate payouts.
 */
internal class AchievementRewardGrantManager private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val achievementDao: AchievementDao = GoldHuntDatabase.get(appContext).achievementDao()
    private val goldHuntRepository by lazy { GoldHuntRepository.get(appContext) }
    private val gameplayXp = GameplayXpAwarder.get(appContext)
    private val grantRepository = AchievementRewardGrantRepository.get(appContext)
    private val hookRepository = AchievementRewardHookRepository.get(appContext)
    private val statisticsManager = AchievementStatisticsManager.get(appContext)
    private val badgeRepository = AchievementBadgeRepository.get(appContext)
    private val titleRepository = AchievementTitleRepository.get(appContext)
    private val chainUnlockManager = AchievementChainUnlockManager.get(appContext)

    suspend fun tryGrantRewards(
        achievement: Achievement,
        timestampMs: Long = System.currentTimeMillis(),
    ): AchievementRewardGrantResult {
        if (!achievement.completed) return AchievementRewardGrantResult.skipped
        if (grantRepository.isGranted(achievement.achievementId)) {
            return AchievementRewardGrantResult.skipped
        }

        val outcome = AchievementRewardDispatcher.dispatch(achievement)
        val record = grantRepository.recordGrant(
            AchievementRewardGrantEntity(
                achievementId = achievement.achievementId,
                creditsGranted = outcome.creditGrant.amount,
                xpGranted = outcome.xp,
                storyFragmentKey = outcome.storyFragmentKey,
                storyFragmentRecorded = false,
                legendaryRelicKey = outcome.legendaryRelicKey,
                legendaryRelicRecorded = false,
                titleKey = outcome.titleKey,
                titleRecorded = false,
                badgeKey = outcome.badgeKey,
                badgeRecorded = false,
                grantedAtMs = timestampMs,
            ),
        )
        if (!record.isNew) {
            return AchievementRewardGrantResult.skipped
        }

        goldHuntRepository.ensureInitialized()
        val creditsGranted = goldHuntRepository.applyCreditGrant(outcome.creditGrant, timestampMs)
        val xpOutcome = gameplayXp.tryAwardAchievementCompletion(
            achievementId = achievement.achievementId,
            category = achievement.category,
            xpAwarded = outcome.xp,
            title = achievement.title,
            timestampMs = timestampMs,
        )
        val hookResult = hookRepository.recordCompletionHooks(
            outcome = outcome,
            timestampMs = timestampMs,
        )
        if (hookResult.badgeRecorded && !outcome.badgeKey.isNullOrBlank()) {
            badgeRepository.recordUnlock(
                badgeKey = outcome.badgeKey,
                achievementId = achievement.achievementId,
                unlockedAtMs = timestampMs,
            )
        }
        if (hookResult.titleRecorded && !outcome.titleKey.isNullOrBlank()) {
            titleRepository.recordUnlock(
                titleKey = outcome.titleKey,
                achievementId = achievement.achievementId,
                unlockedAtMs = timestampMs,
            )
        }
        val grantResult = AchievementRewardGrantResult(
            achievementId = achievement.achievementId,
            creditsGranted = creditsGranted,
            xpGranted = xpOutcome.xpAwarded,
            storyFragmentRecorded = hookResult.storyFragmentRecorded,
            legendaryRelicRecorded = hookResult.legendaryRelicRecorded,
            titleRecorded = hookResult.titleRecorded,
            badgeRecorded = hookResult.badgeRecorded,
            isNewGrant = true,
        )
        statisticsManager.recordGrant(achievement, grantResult, timestampMs)
        chainUnlockManager.dispatchChainUnlocks(achievement, timestampMs)
        AchievementNotificationCenter.get().enqueueFromGrant(
            achievement = achievement,
            outcome = outcome,
            grantResult = grantResult,
            timestampMs = timestampMs,
        )
        return grantResult
    }

    suspend fun reconcilePendingGrants(
        timestampMs: Long = System.currentTimeMillis(),
    ): List<AchievementRewardGrantResult> {
        val completed = achievementDao.getAll()
            .filter { it.completed }
            .mapNotNull { AchievementMapper.toDomain(it) }
        return completed.mapNotNull { achievement ->
            val result = tryGrantRewards(achievement, timestampMs)
            result.takeIf { it.isNewGrant }
        }
    }

    companion object {
        @Volatile
        private var instance: AchievementRewardGrantManager? = null

        fun get(context: Context): AchievementRewardGrantManager =
            instance ?: synchronized(this) {
                instance ?: AchievementRewardGrantManager(context.applicationContext)
                    .also { instance = it }
            }
    }
}
