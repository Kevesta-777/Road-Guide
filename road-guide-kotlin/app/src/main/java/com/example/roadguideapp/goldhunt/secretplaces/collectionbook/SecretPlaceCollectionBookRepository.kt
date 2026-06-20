package com.example.roadguideapp.goldhunt.secretplaces.collectionbook

import android.content.Context
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileRepository
import com.example.roadguideapp.goldhunt.secretplaces.SecretPlace
import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class SecretPlaceCollectionBookRepository private constructor(context: Context) {
    private val database = GoldHuntDatabase.get(context.applicationContext)
    private val collectionBookDao = database.secretPlaceCollectionBookDao()
    private val profileRepository = ExplorerProfileRepository.get(context.applicationContext)
    private val writeMutex = Mutex()

    suspend fun load(): SecretPlaceCollectionBook = writeMutex.withLock {
        ensureSeededUnlocked()
        reconcileFromProfileStatsUnlocked()
        buildCollectionBook()
    }

    suspend fun recordDiscovery(
        category: SecretPlaceCategory,
        discoveredAtMs: Long,
    ): SecretPlaceCollectionBookEntry? = writeMutex.withLock {
        ensureSeededUnlocked()
        recordDiscoveryUnlocked(category, discoveredAtMs)
    }

    suspend fun recordCompletion(
        place: SecretPlace,
        creditsEarned: Int,
        xpEarned: Long,
        completedAtMs: Long,
    ): SecretPlaceCollectionBookEntry? = writeMutex.withLock {
        ensureSeededUnlocked()
        recordCompletionUnlocked(place, creditsEarned, xpEarned, completedAtMs)
    }

    suspend fun recordCompletion(
        category: SecretPlaceCategory,
        creditsEarned: Int,
        xpEarned: Long,
        completedAtMs: Long,
    ): SecretPlaceCollectionBookEntry? = writeMutex.withLock {
        ensureSeededUnlocked()
        recordCompletionUnlocked(category, creditsEarned, xpEarned, completedAtMs)
    }

    private suspend fun recordDiscoveryUnlocked(
        category: SecretPlaceCategory,
        discoveredAtMs: Long,
    ): SecretPlaceCollectionBookEntry? {
        val template = SecretPlaceCollectionBookCatalog.templateForCategory(category) ?: return null
        val existing = collectionBookDao.get(category.id)
        val discoveredCount = (existing?.discoveredCount ?: 0) + 1
        val completedCount = existing?.completedCount ?: 0
        val firstDiscoveredAtMs = existing?.firstDiscoveredAtMs?.takeIf { it > 0L } ?: discoveredAtMs
        val entity = SecretPlaceCollectionBookMapper.toEntity(
            category = category,
            status = SecretPlaceCollectionBookMapper.resolveStatus(discoveredCount, completedCount),
            firstDiscoveredAtMs = firstDiscoveredAtMs,
            completedAtMs = existing?.completedAtMs,
            totalCreditsEarned = existing?.totalCreditsEarned ?: 0,
            totalXpEarned = existing?.totalXpEarned ?: 0L,
            discoveredCount = discoveredCount,
            completedCount = completedCount,
            minimumExplorerLevel = template.minimumExplorerLevel,
            achievementKey = existing?.achievementKey ?: template.achievementKey,
            storyFragmentId = existing?.storyFragmentId ?: template.storyFragmentKey,
            timestampMs = discoveredAtMs,
        )
        collectionBookDao.upsert(entity)
        return SecretPlaceCollectionBookMapper.toDomain(template, entity)
    }

    private suspend fun recordCompletionUnlocked(
        category: SecretPlaceCategory,
        creditsEarned: Int,
        xpEarned: Long,
        completedAtMs: Long,
    ): SecretPlaceCollectionBookEntry? {
        val template = SecretPlaceCollectionBookCatalog.templateForCategory(category) ?: return null
        val existing = collectionBookDao.get(category.id)
        val discoveredCount = maxOf(existing?.discoveredCount ?: 0, 1)
        val completedCount = (existing?.completedCount ?: 0) + 1
        val firstDiscoveredAtMs = existing?.firstDiscoveredAtMs?.takeIf { it > 0L } ?: completedAtMs
        val completedAt = existing?.completedAtMs?.takeIf { it > 0L } ?: completedAtMs
        val entity = SecretPlaceCollectionBookMapper.toEntity(
            category = category,
            status = SecretPlaceCollectionBookMapper.resolveStatus(discoveredCount, completedCount),
            firstDiscoveredAtMs = firstDiscoveredAtMs,
            completedAtMs = completedAt,
            totalCreditsEarned = (existing?.totalCreditsEarned ?: 0) + creditsEarned.coerceAtLeast(0),
            totalXpEarned = (existing?.totalXpEarned ?: 0L) + xpEarned.coerceAtLeast(0L),
            discoveredCount = discoveredCount,
            completedCount = completedCount,
            minimumExplorerLevel = existing?.minimumExplorerLevel ?: template.minimumExplorerLevel,
            achievementKey = existing?.achievementKey ?: template.achievementKey,
            storyFragmentId = existing?.storyFragmentId ?: template.storyFragmentKey,
            timestampMs = completedAtMs,
        )
        collectionBookDao.upsert(entity)
        return SecretPlaceCollectionBookMapper.toDomain(template, entity)
    }

    private suspend fun recordCompletionUnlocked(
        place: SecretPlace,
        creditsEarned: Int,
        xpEarned: Long,
        completedAtMs: Long,
    ): SecretPlaceCollectionBookEntry? {
        val template = SecretPlaceCollectionBookCatalog.templateForCategory(place.category) ?: return null
        val existing = collectionBookDao.get(place.category.id)
        val discoveredCount = maxOf(existing?.discoveredCount ?: 0, 1)
        val completedCount = (existing?.completedCount ?: 0) + 1
        val firstDiscoveredAtMs = existing?.firstDiscoveredAtMs?.takeIf { it > 0L } ?: completedAtMs
        val completedAt = existing?.completedAtMs?.takeIf { it > 0L } ?: completedAtMs
        val achievementKey = place.resolvedAchievementKey ?: template.achievementKey
        val storyFragmentId = place.resolvedStoryFragmentId ?: template.storyFragmentKey
        val entity = SecretPlaceCollectionBookMapper.toEntity(
            category = place.category,
            status = SecretPlaceCollectionBookMapper.resolveStatus(discoveredCount, completedCount),
            firstDiscoveredAtMs = firstDiscoveredAtMs,
            completedAtMs = completedAt,
            totalCreditsEarned = (existing?.totalCreditsEarned ?: 0) + creditsEarned.coerceAtLeast(0),
            totalXpEarned = (existing?.totalXpEarned ?: 0L) + xpEarned.coerceAtLeast(0L),
            discoveredCount = discoveredCount,
            completedCount = completedCount,
            minimumExplorerLevel = place.resolvedMinimumExplorerLevel,
            achievementKey = achievementKey,
            storyFragmentId = storyFragmentId,
            timestampMs = completedAtMs,
        )
        collectionBookDao.upsert(entity)
        return SecretPlaceCollectionBookMapper.toDomain(template, entity)
    }

    private suspend fun ensureSeededUnlocked() {
        val existingKeys = collectionBookDao.all().map { it.catalogKey }.toSet()
        val now = System.currentTimeMillis()
        val seeds = SecretPlaceCollectionBookCatalog.displayTemplates
            .filter { it.category.id !in existingKeys }
            .map { template ->
                SecretPlaceCollectionBookMapper.toEntity(
                    category = template.category,
                    status = SecretPlaceCollectionBookCatalog.initialStatus(),
                    firstDiscoveredAtMs = null,
                    completedAtMs = null,
                    totalCreditsEarned = 0,
                    totalXpEarned = 0L,
                    discoveredCount = 0,
                    completedCount = 0,
                    minimumExplorerLevel = template.minimumExplorerLevel,
                    achievementKey = template.achievementKey,
                    storyFragmentId = template.storyFragmentKey,
                    timestampMs = now,
                )
            }
        if (seeds.isNotEmpty()) {
            collectionBookDao.upsertAll(seeds)
        }
    }

    private suspend fun reconcileFromProfileStatsUnlocked() {
        val profile = profileRepository.loadProfile()
        val stats = profile.secretPlaceCategoryStatistics
        if (stats.discoveredCount <= 0 && stats.completedCount <= 0) return
        val now = System.currentTimeMillis()
        for (template in SecretPlaceCollectionBookCatalog.displayTemplates) {
            val category = template.category
            val discoveredCount = stats.discoveredCountFor(category)
            val completedCount = stats.completedCountFor(category)
            if (discoveredCount <= 0 && completedCount <= 0) continue
            val existing = collectionBookDao.get(category.id)
            val mergedDiscovered = maxOf(existing?.discoveredCount ?: 0, discoveredCount)
            val mergedCompleted = maxOf(existing?.completedCount ?: 0, completedCount)
            collectionBookDao.upsert(
                SecretPlaceCollectionBookMapper.toEntity(
                    category = category,
                    status = SecretPlaceCollectionBookMapper.resolveStatus(
                        mergedDiscovered,
                        mergedCompleted,
                    ),
                    firstDiscoveredAtMs = existing?.firstDiscoveredAtMs,
                    completedAtMs = existing?.completedAtMs,
                    totalCreditsEarned = existing?.totalCreditsEarned ?: 0,
                    totalXpEarned = existing?.totalXpEarned ?: 0L,
                    discoveredCount = mergedDiscovered,
                    completedCount = mergedCompleted,
                    minimumExplorerLevel = template.minimumExplorerLevel,
                    achievementKey = existing?.achievementKey ?: template.achievementKey,
                    storyFragmentId = existing?.storyFragmentId ?: template.storyFragmentKey,
                    timestampMs = now,
                ),
            )
        }
    }

    private suspend fun buildCollectionBook(): SecretPlaceCollectionBook {
        val entities = collectionBookDao.all().associateBy { it.catalogKey }
        val entries = SecretPlaceCollectionBookCatalog.displayTemplates.map { template ->
            SecretPlaceCollectionBookMapper.toDomain(template, entities[template.category.id])
        }
        return SecretPlaceCollectionBook(
            entries = entries,
            progress = SecretPlaceCollectionBookMapper.buildProgress(entries),
        )
    }

    companion object {
        @Volatile
        private var instance: SecretPlaceCollectionBookRepository? = null

        fun get(context: Context): SecretPlaceCollectionBookRepository =
            instance ?: synchronized(this) {
                instance ?: SecretPlaceCollectionBookRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
