package com.example.roadguideapp.goldhunt.ui

import androidx.compose.ui.graphics.Color

internal data class GoldHuntScoreBarColors(
    val board: Color,
    val border: Color,
    val text: Color,
    val textMuted: Color,
    val divider: Color,
)

internal object GoldHuntScoreBarTheme {
    fun colors(isDarkAppearance: Boolean): GoldHuntScoreBarColors =
        if (isDarkAppearance) dark() else day()

    /** Day: warm yellow glass board, brown ink. */
    private fun day() = GoldHuntScoreBarColors(
        board = Color(0x80FFF59D),
        border = Color(0x996B4E2E),
        text = Color(0xFF4E342E),
        textMuted = Color(0xFF6D4C41),
        divider = Color(0x664E342E),
    )

    /** Night: deep curiosity blue glass board, golden text. */
    private fun dark() = GoldHuntScoreBarColors(
        board = Color(0x801E3A8A),
        border = Color(0x994FC3F7),
        text = Color(0xFFFFEB3B),
        textMuted = Color(0xFFFFF59D),
        divider = Color(0x66FFEB3B),
    )
}
