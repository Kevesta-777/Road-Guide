package com.example.roadguideapp.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BadgeRed = Color(0xFFE53935)

/**
 * Circular layers-style control with optional numeric badge (reference map app UI).
 */
@Composable
internal fun MapLayersMapSettingsButton(
    onClick: () -> Unit,
    layersContentDescription: String,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0,
) {
    Box(modifier = modifier) {
        MapCircleControlButton(
            onClick = onClick,
            modifier = Modifier.align(Alignment.Center),
        ) {
            Icon(
                imageVector = Icons.Outlined.Layers,
                contentDescription = layersContentDescription,
                tint = MapControlIconTint,
            )
        }
        if (badgeCount > 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(20.dp),
                shape = CircleShape,
                color = BadgeRed,
                shadowElevation = 2.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = badgeCount.coerceAtMost(99).toString(),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
