package com.example.roadguideapp.goldhunt.panorama.rewards

internal data class PanoramaHuntAchievementProgress(
    val achievementKey: String,
    val completionCount: Int,
    val updatedAtMs: Long,
)

internal data class PanoramaHuntAchievementProgressResult(
    val progress: PanoramaHuntAchievementProgress?,
    val isNewIncrement: Boolean,
) {
    companion object {
        val skipped = PanoramaHuntAchievementProgressResult(progress = null, isNewIncrement = false)
    }
}
