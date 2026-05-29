package com.example.roadguideapp.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FriendQrPayloadTest {

    private val sampleId = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"

    @Test
    fun encode_decode_roundTrip() {
        val encoded = FriendQrPayload.encode(sampleId, "John Doe")
        assertNotNull(encoded)
        val decoded = FriendQrPayload.decode(encoded!!)
        assertNotNull(decoded)
        assertEquals(sampleId, decoded!!.profileId)
        assertEquals("John Doe", decoded.displayName)
    }

    @Test
    fun decode_rejectsInvalidScheme() {
        assertNull(FriendQrPayload.decode("https://example.com"))
    }

    @Test
    fun decode_rejectsInvalidProfileId() {
        val bad = "roadguide://friend/v1?pid=not-a-uuid&name=x"
        assertNull(FriendQrPayload.decode(bad))
    }

    @Test
    fun isValidProfileId_acceptsUuid() {
        assert(OfflineFriendsStore.isValidProfileId(sampleId))
    }
}
