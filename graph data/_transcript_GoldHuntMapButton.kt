package com.example.roadguideapp.goldhunt.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.roadguideapp.R
import com.example.roadguideapp.map.AppleMapsSheetTheme

/** Diamond map control frame for Gold Hunt — bottom-left stack. */
private val GoldHuntCrystalShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f
    val inset = minOf(w, h) * 0.06f
    moveTo(cx, inset)
    lineTo(w - inset, cy)
    lineTo(cx, h - inset)
    lineTo(inset, cy)
    close()
}

@Composable
internal fun GoldHuntMapButton(
    sheetTheme: AppleMapsSheetTheme,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val crystalBrush = if (isActive) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFF8DC),
                Color(0xFFFFD54F),
                Color(0xFFB8860B),
                Color(0xFF8B6914),
            ),
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF8FCFF),
                sheetTheme.mapControlGlass.copy(alpha = 0.95f),
                Color(0xFFD0E4F5),
                Color(0xFFA8C8E8),
            ),
        )
    }
    val borderColor = if (isActive) Color(0xFFFFE082) else Color(0xFFE8F4FF)

    Box(
        modifier = modifier
            .size(52.dp)
            .shadow(8.dp, GoldHuntCrystalShape, clip = false)
            .clip(GoldHuntCrystalShape)
            .background(crystalBrush)
            .border(1.5.dp, borderColor, GoldHuntCrystalShape),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = GoldHuntCrystalShape,
            color = Color.Transparent,
            shadowElevation = 0.dp,
        ) {
            IconButton(
                onClick = onClick,
                modifier = Modifier.fillMaxSize(),
            ) {
                Image(
                    painter = painterResource(R.drawable.gold_hunt_button),
                    contentDescription = stringResource(R.string.gold_hunt_title),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}
