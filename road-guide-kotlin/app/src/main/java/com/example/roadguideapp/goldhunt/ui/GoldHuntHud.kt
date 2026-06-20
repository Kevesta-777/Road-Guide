package com.example.roadguideapp.goldhunt.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roadguideapp.R
import com.example.roadguideapp.goldhunt.GoldHuntController
import com.example.roadguideapp.map.MapZoomGradeState
import kotlinx.coroutines.delay

@Composable
internal fun GoldHuntHud(
    goldHunt: GoldHuntController,
    isDarkAppearance: Boolean,
    bottomPadding: androidx.compose.ui.unit.Dp,
    zoomGrade: MapZoomGradeState? = null,
    onOpenExplorerProfile: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    if (!goldHunt.isActive) return

    LaunchedEffect(goldHunt.lastCreditsDelta) {
        if (goldHunt.lastCreditsDelta > 0) {
            delay(2_500)
            goldHunt.clearCreditsDelta()
        }
    }

    Box(modifier = modifier) {
        GoldHuntScoreBar(
            profile = goldHunt.profile,
            valueLog = goldHunt.treasureValueLog,
            isDarkAppearance = isDarkAppearance,
            zoomGrade = zoomGrade,
            onOpenExplorerProfile = onOpenExplorerProfile,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = GoldHuntHudLayout.horizontalInset,
                    end = GoldHuntHudLayout.horizontalInset +
                        GoldHuntHudLayout.topRightChromeReserve,
                ),
        )

        AnimatedVisibility(
            visible = goldHunt.lastCreditsDelta > 0,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomPadding + 8.dp),
        ) {
            Text(
                text = stringResource(R.string.gold_hunt_credits_earned, goldHunt.lastCreditsDelta),
                color = Color(0xFFFFD60A),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(
                        color = Color(0xCC1C1C1E),
                        shape = RoundedCornerShape(10.dp),
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}
