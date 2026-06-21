package com.example.roadguideapp.goldhunt.ui

import androidx.compose.ui.unit.dp

/** Layout constants so the score bar does not cover map chrome or the scale ruler. */
internal object GoldHuntHudLayout {
    /** Width reserved for Choose Map / My Location column (44dp control + margins). */
    val topRightChromeReserve = 60.dp

    val horizontalInset = 16.dp
    val scoreBarTopPadding = 6.dp

    /** Main three-column row + zoom status row inside the parchment bar. */
    val scoreBarHeight = 88.dp

    val scaleRulerGapBelowBar = 12.dp

    /** Top padding for the scale ruler column when Gold Hunt is active. */
    val scaleRulerTopBelowScoreBar =
        scoreBarTopPadding + scoreBarHeight + scaleRulerGapBelowBar
}
