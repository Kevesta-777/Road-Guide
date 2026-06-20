package com.example.roadguideapp.goldhunt.secretplaces.statistics

import android.content.Context
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileRepository
import com.example.roadguideapp.goldhunt.secretplaces.SecretPlace
import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory
import com.example.roadguideapp.goldhunt.secretplaces.categories.rewards.SecretPlaceCategoryRewards
import com.example.roadguideapp.goldhunt.secretplaces.collectionbook.SecretPlaceCollectionBookRepository

/**
 * Persists lifetime secret-place category statistics on the explorer profile singleton.
 */
internal class SecretPlaceCategoryStatisticsManager private constructor(context: Context) {
    private val repository = ExplorerProfileRepository.get(context.applicationContext)
    private val collectionBookRepository = SecretPlaceCollectionBookRepository.get(context.applicationContext)

    suspend fun load(): SecretPlaceCategoryStatistics =
        SecretPlaceCategoryStatisticsMapper.fromProfile(repository.loadProfile())

    suspend fun recordDiscovery(
        category: SecretPlaceCategory,
        timestampMs: Long = System.currentTimeMillis(),
    ): SecretPlaceCategoryStatistics {
        val updated = repository.updateProfile(timestampMs) { profile ->
            val stats = SecretPlaceCategoryStatisticsMapper
                .fromProfile(profile)
                .withDiscovery(category)
            SecretPlaceCategoryStatisticsMapper.applyToProfile(profile, stats)
        }
        collectionBookRepository.recordDiscovery(category, timestampMs)
        return SecretPlaceCategoryStatisticsMapper.fromProfile(updated)
    }

    suspend fun recordCompletion(
        place: SecretPlace,
        timestampMs: Long = System.currentTimeMillis(),
    ): SecretPlaceCategoryStatistics {
        val bundle = SecretPlaceCategoryRewards.completionBundle(place)
        return recordCompletion(
            category = place.category,
            creditsEarned = bundle.credits,
            xpEarned = bundle.xp,
            timestampMs = timestampMs,
        )
    }

    suspend fun recordCompletion(
        category: SecretPlaceCategory,
        creditsEarned: Int,
        xpEarned: Long,
        timestampMs: Long = System.currentTimeMillis(),
    ): SecretPlaceCategoryStatistics {
        val updated = repository.updateProfile(timestampMs) { profile ->
            val stats = SecretPlaceCategoryStatisticsMapper
                .fromProfile(profile)
                .withCompletion(category, creditsEarned, xpEarned)
            SecretPlaceCategoryStatisticsMapper.applyToProfile(profile, stats)
        }
        collectionBookRepository.recordCompletion(
            category = category,
            creditsEarned = creditsEarned,
            xpEarned = xpEarned,
            completedAtMs = timestampMs,
        )
        return SecretPlaceCategoryStatisticsMapper.fromProfile(updated)
    }

    companion object {
        @Volatile
        private var instance: SecretPlaceCategoryStatisticsManager? = null

        fun get(context: Context): SecretPlaceCategoryStatisticsManager =
            instance ?: synchronized(this) {
                instance ?: SecretPlaceCategoryStatisticsManager(context.applicationContext)
                    .also { instance = it }
            }
    }
}
