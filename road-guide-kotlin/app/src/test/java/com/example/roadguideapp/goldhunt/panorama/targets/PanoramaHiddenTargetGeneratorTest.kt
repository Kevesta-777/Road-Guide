package com.example.roadguideapp.goldhunt.panorama.targets

import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntGenerator
import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType
import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntWorldSeed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PanoramaHiddenTargetGeneratorTest {
    private val worldSeed = PanoramaHuntWorldSeed.DEFAULT
    private val secretPlaceId = "sp:v1:r:-0.5000:51.4000:L2:12:34:s0"

    @Test
    fun generate_sameHuntAndSlot_producesIdenticalTarget() {
        val hunt = huntFor("pano-stable")
        val first = PanoramaHiddenTargetGenerator.generate(hunt, slotIndex = 0)
        val second = PanoramaHiddenTargetGenerator.generate(hunt, slotIndex = 0)
        assertEquals(first, second)
    }

    @Test
    fun generate_differentSlots_produceDifferentPositions() {
        val hunt = huntFor("pano-slots", PanoramaHuntType.SECRET_CODE)
        val a = PanoramaHiddenTargetGenerator.generate(hunt, slotIndex = 0)
        val b = PanoramaHiddenTargetGenerator.generate(hunt, slotIndex = 1)
        assertNotEquals(a.bearing, b.bearing)
        assertNotEquals(a.pitch, b.pitch)
    }

    @Test
    fun generateSet_targetCountMatchesHuntType() {
        val symbolHunt = huntFor("pano-symbol", PanoramaHuntType.HIDDEN_SYMBOL)
        val codeHunt = huntFor("pano-code", PanoramaHuntType.SECRET_CODE)
        assertEquals(1, PanoramaHiddenTargetGenerator.generateSet(symbolHunt).targets.size)
        assertEquals(3, PanoramaHiddenTargetGenerator.generateSet(codeHunt).targets.size)
    }

    @Test
    fun generate_fieldsStayWithinSchemaBounds() {
        val hunt = huntFor("pano-bounds", PanoramaHuntType.OBJECT_HUNT)
        val target = PanoramaHiddenTargetGenerator.generate(hunt)
        assertTrue(target.bearing in 0f..360f)
        assertTrue(target.pitch in PanoramaHiddenTargetSchema.MIN_PITCH..PanoramaHiddenTargetSchema.MAX_PITCH)
        assertTrue(target.radius in PanoramaHiddenTargetSchema.MIN_RADIUS..PanoramaHiddenTargetSchema.MAX_RADIUS)
    }

    @Test
    fun generate_harderHunt_tendsTowardSmallerRadius() {
        val easy = huntFor("pano-radius-easy", PanoramaHuntType.HIDDEN_SYMBOL)
        val hard = huntFor("pano-radius-hard", PanoramaHuntType.RELIC_HUNT)
        val easyRadius = PanoramaHiddenTargetGenerator.generate(easy).radius
        val hardRadius = PanoramaHiddenTargetGenerator.generate(hard).radius
        assertTrue(easyRadius > hardRadius)
    }

    @Test
    fun generate_defaultTargetType_matchesHuntFamily() {
        val hunt = huntFor("pano-type", PanoramaHuntType.OBJECT_HUNT)
        val target = PanoramaHiddenTargetGenerator.generate(hunt)
        assertEquals(PanoramaHiddenTargetType.OBJECT, target.targetType)
    }

    @Test
    fun generateSet_supportsLegendaryRelicRoll() {
        val seenLegendary = (0 until 512).any { index ->
            val hunt = huntFor("pano-legendary-$index", PanoramaHuntType.RELIC_HUNT)
            PanoramaHiddenTargetGenerator.generate(hunt).isLegendary
        }
        assertTrue(seenLegendary)
    }

    @Test
    fun generateSet_supportsAnimatedTargets() {
        val seenAnimated = (0 until 256).any { index ->
            val hunt = huntFor("pano-animated-$index", PanoramaHuntType.PANORAMA_PUZZLE)
            PanoramaHiddenTargetGenerator.generateSet(hunt).animatedTargets.isNotEmpty()
        }
        assertTrue(seenAnimated)
    }

    @Test
    fun seedKey_isStableAcrossCalls() {
        val hunt = huntFor("pano-seed")
        assertEquals(
            PanoramaHiddenTargetGenerator.seedKey(hunt, 0),
            PanoramaHiddenTargetGenerator.seedKey(hunt, 0),
        )
    }

    @Test
    fun hitTest_focusesWhenViewMatchesTarget() {
        val hunt = huntFor("pano-hit")
        val target = PanoramaHiddenTargetGenerator.generate(hunt)
        assertTrue(
            PanoramaHiddenTargetHitTest.isFocused(
                target = target,
                viewYaw = target.bearing,
                viewPitch = target.pitch,
                fieldOfView = 90f,
            ),
        )
    }

    private fun huntFor(
        panoramaId: String,
        huntType: PanoramaHuntType? = null,
    ) = PanoramaHuntGenerator.generate(
        panoramaId = panoramaId,
        secretPlaceId = secretPlaceId,
        worldSeed = worldSeed,
    ).let { hunt ->
        if (huntType == null || hunt.huntType == huntType) {
            hunt
        } else {
            hunt.copy(
                huntType = huntType,
                difficulty = huntType.difficulty,
            )
        }
    }
}
