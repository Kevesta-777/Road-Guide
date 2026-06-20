package com.example.roadguideapp.goldhunt.achievements.progress

import com.example.roadguideapp.goldhunt.achievements.AchievementCategory

/**
 * Gameplay events that drive automatic achievement progress updates.
 */
internal sealed class AchievementProgressEvent {
    abstract val timestampMs: Long

    data class LegacyIncrement(
        val achievementKey: String,
        val increment: Int = 1,
        override val timestampMs: Long = System.currentTimeMillis(),
    ) : AchievementProgressEvent()

    data class TreasureCollected(
        override val timestampMs: Long = System.currentTimeMillis(),
    ) : AchievementProgressEvent()

    data class CellExplored(
        override val timestampMs: Long = System.currentTimeMillis(),
    ) : AchievementProgressEvent()

    data class SecretPlaceDiscovered(
        override val timestampMs: Long = System.currentTimeMillis(),
    ) : AchievementProgressEvent()

    data class ProfileSynced(
        override val timestampMs: Long = System.currentTimeMillis(),
    ) : AchievementProgressEvent()

    data class CategoryReconcile(
        val categories: Set<AchievementCategory>,
        override val timestampMs: Long = System.currentTimeMillis(),
    ) : AchievementProgressEvent()

    data class FullReconcile(
        override val timestampMs: Long = System.currentTimeMillis(),
    ) : AchievementProgressEvent()
}
