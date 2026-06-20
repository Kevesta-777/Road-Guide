package com.example.roadguideapp.goldhunt.relics.hidden

internal object HiddenRelicDiscoveryMapper {
    fun toDomain(entity: HiddenRelicDiscoveryEntity): HiddenRelicDiscoveryRecord? {
        val visibility = RelicVisibility.fromId(entity.visibility) ?: return null
        return HiddenRelicDiscoveryRecord(
            relicId = entity.relicId,
            visibility = visibility,
            revealedAtMs = entity.revealedAtMs,
            firstPieceId = entity.firstPieceId,
            storyFragmentKey = entity.storyFragmentKey,
            storyFragmentRecorded = entity.storyFragmentRecorded,
            achievementKey = entity.achievementKey,
            achievementRecorded = entity.achievementRecorded,
        )
    }

    fun toEntity(
        record: HiddenRelicDiscoveryRecord,
        updatedAtMs: Long = System.currentTimeMillis(),
    ): HiddenRelicDiscoveryEntity = HiddenRelicDiscoveryEntity(
        relicId = record.relicId,
        visibility = record.visibility.id,
        revealedAtMs = record.revealedAtMs,
        firstPieceId = record.firstPieceId,
        storyFragmentKey = record.storyFragmentKey,
        storyFragmentRecorded = record.storyFragmentRecorded,
        achievementKey = record.achievementKey,
        achievementRecorded = record.achievementRecorded,
        updatedAtMs = updatedAtMs,
    )
}
