package com.example.roadguideapp.goldhunt.profile.xp

import org.junit.Assert.assertEquals
import org.junit.Test

class XpTransactionMapperTest {
    @Test
    fun entityDomainRoundTrip_preservesFields() {
        val domain = XpTransaction(
            id = "tx-1",
            timestampMs = 1_700_000_000_000L,
            source = XpSource.TREASURE_COLLECTION,
            xpAwarded = 25L,
            description = "Collected star treasure",
            metadataJson = """{"treasureId":"t-42"}""",
        )
        val roundTrip = XpTransactionMapper.toDomain(XpTransactionMapper.toEntity(domain))
        assertEquals(domain, roundTrip)
    }

    @Test
    fun parseSource_unknownValue_fallsBackToAchievement() {
        val entity = XpTransactionEntity(
            id = "tx-2",
            timestampMs = 0L,
            source = "FUTURE_SOURCE",
            xpAwarded = 10L,
            description = "",
        )
        assertEquals(XpSource.ACHIEVEMENT, XpTransactionMapper.toDomain(entity).source)
    }
}
