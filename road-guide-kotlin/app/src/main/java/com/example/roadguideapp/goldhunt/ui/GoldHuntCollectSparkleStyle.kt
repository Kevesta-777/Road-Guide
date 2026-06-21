package com.example.roadguideapp.goldhunt.ui

import androidx.compose.ui.graphics.Color
import com.example.roadguideapp.goldhunt.treasure.TreasureType

internal enum class GoldHuntCollectSparkleStyle {
    STAR,
    FLOWER,
    CRYSTAL,
    GIFT,
    SECRET,
    ;

    companion object {
        fun fromTreasureType(type: TreasureType): GoldHuntCollectSparkleStyle = when (type) {
            TreasureType.STAR, TreasureType.HINT -> STAR
            TreasureType.FLOWER -> FLOWER
            TreasureType.CRYSTAL -> CRYSTAL
            TreasureType.GIFT -> GIFT
        }
    }
}

internal data class GoldHuntCollectSparklePalette(
    val ringPrimary: Color,
    val ringSecondary: Color,
    val core: Color,
    val particleColors: List<Color>,
)

internal object GoldHuntCollectSparklePalettes {
    private val gold = Color(0xFFFFD60A)
    private val goldSoft = Color(0xFFFFE082)
    private val rose = Color(0xFFE11D48)
    private val roseSoft = Color(0xFFFB7185)
    private val blue = Color(0xFF38BDF8)
    private val blueSoft = Color(0xFF7DD3FC)
    private val green = Color(0xFF4ADE80)
    private val greenSoft = Color(0xFF86EFAC)

    fun forStyle(style: GoldHuntCollectSparkleStyle): GoldHuntCollectSparklePalette = when (style) {
        GoldHuntCollectSparkleStyle.STAR -> GoldHuntCollectSparklePalette(
            ringPrimary = gold,
            ringSecondary = goldSoft,
            core = Color(0xFFFFF8DC),
            particleColors = listOf(gold, goldSoft, Color(0xFFFFC107)),
        )
        GoldHuntCollectSparkleStyle.FLOWER -> GoldHuntCollectSparklePalette(
            ringPrimary = rose,
            ringSecondary = roseSoft,
            core = Color(0xFFFFE4E8),
            particleColors = listOf(rose, roseSoft, Color(0xFFF43F5E)),
        )
        GoldHuntCollectSparkleStyle.CRYSTAL -> GoldHuntCollectSparklePalette(
            ringPrimary = blue,
            ringSecondary = blueSoft,
            core = Color(0xFFE0F7FF),
            particleColors = listOf(blue, blueSoft, Color(0xFF0EA5E9)),
        )
        GoldHuntCollectSparkleStyle.GIFT -> GoldHuntCollectSparklePalette(
            ringPrimary = green,
            ringSecondary = greenSoft,
            core = Color(0xFFDCFCE7),
            particleColors = listOf(green, greenSoft, Color(0xFF22C55E)),
        )
        GoldHuntCollectSparkleStyle.SECRET -> GoldHuntCollectSparklePalette(
            ringPrimary = gold,
            ringSecondary = blue,
            core = Color(0xFFFFFFFF),
            particleColors = listOf(gold, rose, blue, green, goldSoft, roseSoft, blueSoft, greenSoft),
        )
    }
}
