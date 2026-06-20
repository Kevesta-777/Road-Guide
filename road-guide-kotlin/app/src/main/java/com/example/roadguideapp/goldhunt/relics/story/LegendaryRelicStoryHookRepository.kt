package com.example.roadguideapp.goldhunt.relics.story

import android.content.Context
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicCatalog
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicDefinition
import com.example.roadguideapp.goldhunt.relics.RelicSchema
import com.example.roadguideapp.goldhunt.relics.hidden.RelicVisibility
import com.example.roadguideapp.goldhunt.relics.progress.LegendaryRelicProgressGrantResult
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardHookEntity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal data class LegendaryRelicStoryHookRecordResult(
    val chapterRecorded: Boolean = false,
    val loreRecorded: Boolean = false,
)

internal class LegendaryRelicStoryHookRepository private constructor(context: Context) {
    private val dao = GoldHuntDatabase.get(context.applicationContext).legendaryRelicRewardHookDao()
    private val writeMutex = Mutex()

    suspend fun recordPieceStoryHooks(
        grantResult: LegendaryRelicProgressGrantResult,
        timestampMs: Long = System.currentTimeMillis(),
    ): LegendaryRelicStoryHookRecordResult = writeMutex.withLock {
        if (!grantResult.isNewGrant) return@withLock LegendaryRelicStoryHookRecordResult()
        val relicId = grantResult.relicId ?: return@withLock LegendaryRelicStoryHookRecordResult()
        val pieceNumber = grantResult.piecesCollected
        if (pieceNumber <= 0) return@withLock LegendaryRelicStoryHookRecordResult()

        val definition = LegendaryRelicCatalog.findById(relicId)
        val relic = definition?.let { LegendaryRelicCatalog.relicFromDefinition(it) }
            ?: return@withLock LegendaryRelicStoryHookRecordResult()
        if (!RelicSchema.CategoryGates.supportsStoryFragments(relic.category)) {
            return@withLock LegendaryRelicStoryHookRecordResult()
        }

        val chapterRecorded = recordHook(
            eventId = LegendaryRelicStorySchema.chapterEventId(relicId, pieceNumber),
            relicId = relicId,
            hookType = LegendaryRelicStorySchema.HookTypes.CHAPTER,
            hookKey = RelicSchema.StoryFragmentKeys.chapterForRelic(relicId, pieceNumber),
            grantedAtMs = timestampMs,
        )
        val loreRecorded = if (pieceNumber == 1) {
            recordHook(
                eventId = LegendaryRelicStorySchema.loreEventId(relicId, 0),
                relicId = relicId,
                hookType = LegendaryRelicStorySchema.HookTypes.LORE,
                hookKey = RelicSchema.StoryFragmentKeys.loreForRelic(relicId, 0),
                grantedAtMs = timestampMs,
            )
        } else {
            false
        }
        LegendaryRelicStoryHookRecordResult(
            chapterRecorded = chapterRecorded,
            loreRecorded = loreRecorded,
        )
    }

    suspend fun recordCompletionLoreHook(
        relic: LegendaryRelic,
        definition: LegendaryRelicDefinition?,
        timestampMs: Long = System.currentTimeMillis(),
    ): Boolean = writeMutex.withLock {
        if (!RelicSchema.CategoryGates.supportsStoryFragments(relic.category)) return@withLock false
        val visibility = if (definition?.relicId == relic.relicId) {
            RelicVisibilityResolverForStory.visibilityFor(definition)
        } else {
            RelicVisibility.VISIBLE
        }
        val loreIndex = if (visibility == RelicVisibility.HIDDEN) 2 else 1
        recordHook(
            eventId = LegendaryRelicStorySchema.loreEventId(relic.relicId, loreIndex),
            relicId = relic.relicId,
            hookType = LegendaryRelicStorySchema.HookTypes.LORE,
            hookKey = RelicSchema.StoryFragmentKeys.loreForRelic(relic.relicId, loreIndex),
            grantedAtMs = timestampMs,
        )
    }

    private suspend fun recordHook(
        eventId: String,
        relicId: String,
        hookType: String,
        hookKey: String,
        grantedAtMs: Long,
    ): Boolean {
        val inserted = dao.insert(
            LegendaryRelicRewardHookEntity(
                eventId = eventId,
                relicId = relicId,
                hookType = hookType,
                hookKey = hookKey,
                grantedAtMs = grantedAtMs,
            ),
        )
        return inserted >= 0L
    }

    companion object {
        @Volatile
        private var instance: LegendaryRelicStoryHookRepository? = null

        fun get(context: Context): LegendaryRelicStoryHookRepository =
            instance ?: synchronized(this) {
                instance ?: LegendaryRelicStoryHookRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}

private object RelicVisibilityResolverForStory {
    fun visibilityFor(definition: LegendaryRelicDefinition): RelicVisibility =
        if (definition.category == com.example.roadguideapp.goldhunt.relics.RelicCategory.HIDDEN) {
            RelicVisibility.HIDDEN
        } else {
            RelicVisibility.VISIBLE
        }
}
