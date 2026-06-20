package com.example.roadguideapp.goldhunt.achievements.notifications.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.roadguideapp.goldhunt.achievements.notifications.AchievementNotificationCenter
import com.example.roadguideapp.goldhunt.achievements.notifications.AchievementNotificationSchema
import com.example.roadguideapp.goldhunt.ui.GoldHuntHudLayout
import kotlinx.coroutines.delay

@Composable
internal fun AchievementNotificationHost(
    center: AchievementNotificationCenter = AchievementNotificationCenter.get(),
    isGoldHuntActive: Boolean,
    modifier: Modifier = Modifier,
) {
    val notification = center.queue.head
    val trailingCount = center.queue.trailingCount

    LaunchedEffect(notification?.notificationId) {
        if (notification == null) return@LaunchedEffect
        delay(AchievementNotificationSchema.AUTO_DISMISS_MS)
        center.dismissCurrent()
    }

    val topPadding = if (isGoldHuntActive) {
        GoldHuntHudLayout.scoreBarTopPadding +
            GoldHuntHudLayout.scoreBarHeight +
            10.dp
    } else {
        12.dp
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                top = topPadding,
                start = GoldHuntHudLayout.horizontalInset,
                end = GoldHuntHudLayout.horizontalInset +
                    if (isGoldHuntActive) GoldHuntHudLayout.topRightChromeReserve else 0.dp,
            )
            .zIndex(12f),
        contentAlignment = Alignment.TopCenter,
    ) {
        AnimatedVisibility(
            visible = notification != null,
            enter = fadeIn() + slideInVertically { fullHeight -> -fullHeight / 2 },
            exit = fadeOut() + slideOutVertically { fullHeight -> -fullHeight / 2 },
        ) {
            if (notification != null) {
                AchievementNotificationPopup(
                    notification = notification,
                    trailingCount = trailingCount,
                    onDismiss = center::dismissCurrent,
                )
            }
        }
    }
}
