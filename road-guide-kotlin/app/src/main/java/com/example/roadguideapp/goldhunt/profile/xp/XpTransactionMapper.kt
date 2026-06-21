package com.example.roadguideapp.goldhunt.profile.xp

internal object XpTransactionMapper {
    fun toDomain(entity: XpTransactionEntity): XpTransaction = XpTransaction(
        id = entity.id,
        timestampMs = entity.timestampMs,
        source = parseSource(entity.source),
        xpAwarded = entity.xpAwarded,
        description = entity.description,
        metadataJson = entity.metadataJson,
        schemaVersion = entity.schemaVersion,
    )

    fun toEntity(transaction: XpTransaction): XpTransactionEntity = XpTransactionEntity(
        id = transaction.id,
        timestampMs = transaction.timestampMs,
        source = transaction.source.name,
        xpAwarded = transaction.xpAwarded,
        description = transaction.description,
        metadataJson = transaction.metadataJson,
        schemaVersion = transaction.schemaVersion,
    )

    private fun parseSource(raw: String): XpSource =
        runCatching { XpSource.valueOf(raw) }.getOrDefault(XpSource.ACHIEVEMENT)
}
