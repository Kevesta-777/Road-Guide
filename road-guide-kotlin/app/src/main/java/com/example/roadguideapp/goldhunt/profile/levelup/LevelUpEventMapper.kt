package com.example.roadguideapp.goldhunt.profile.levelup

internal object LevelUpEventMapper {
    fun toDomain(entity: LevelUpEventEntity): LevelUpEvent = LevelUpEvent(
        id = entity.id,
        previousLevel = entity.previousLevel,
        newLevel = entity.newLevel,
        timestampMs = entity.timestampMs,
        lifetimeXpAtLevelUp = entity.lifetimeXpAtLevelUp,
        extensionJson = entity.extensionJson,
        schemaVersion = entity.schemaVersion,
    )

    fun toEntity(event: LevelUpEvent): LevelUpEventEntity = LevelUpEventEntity(
        id = event.id,
        previousLevel = event.previousLevel,
        newLevel = event.newLevel,
        timestampMs = event.timestampMs,
        lifetimeXpAtLevelUp = event.lifetimeXpAtLevelUp,
        extensionJson = event.extensionJson,
        schemaVersion = event.schemaVersion,
    )
}
