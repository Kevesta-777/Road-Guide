package com.example.roadguideapp.goldhunt.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roadguideapp.R
import com.example.roadguideapp.goldhunt.profile.PlayerProfile
import com.example.roadguideapp.goldhunt.treasure.TreasureViewportLogState
import com.example.roadguideapp.map.MapZoomGradeState

@Composable
internal fun GoldHuntScoreBar(
    profile: PlayerProfile?,
    valueLog: TreasureViewportLogState?,
    isDarkAppearance: Boolean,
    zoomGrade: MapZoomGradeState? = null,
    onOpenExplorerProfile: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = remember(isDarkAppearance) { GoldHuntScoreBarTheme.colors(isDarkAppearance) }
    val totalCredits = profile?.totalCredits ?: 0
    val logLines = valueLog?.compactLogLines.orEmpty()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onOpenExplorerProfile != null) {
                    Modifier.clickable(onClick = onOpenExplorerProfile)
                } else {
                    Modifier
                },
            )
            .background(colors.board, RoundedCornerShape(6.dp))
            .border(1.dp, colors.border, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CreditsColumn(
                credits = totalCredits,
                colors = colors,
                modifier = Modifier.weight(1f),
            )
            LogColumn(
                lines = logLines,
                colors = colors,
                modifier = Modifier.weight(1f),
            )
        }
        if (zoomGrade != null) {
            HorizontalDivider(
                modifier = Modifier.padding(top = 6.dp, bottom = 4.dp),
                thickness = 1.dp,
                color = colors.divider,
            )
            ZoomStatusRow(state = zoomGrade, colors = colors)
        }
    }
}

@Composable
private fun ZoomStatusRow(
    state: MapZoomGradeState,
    colors: GoldHuntScoreBarColors,
) {
    val spanShort = state.groundSpanText
        .removePrefix("≈ ")
        .substringBefore(" across")
        .trim()
    val hint = state.hintText
    val line = buildString {
        append(state.zoomText)
        if (spanShort.isNotEmpty()) {
            append(" · ")
            append(spanShort)
        }
        if (!hint.isNullOrBlank()) {
            append(" · ")
            append(hint)
        }
    }
    Text(
        text = line,
        color = colors.textMuted,
        fontSize = 9.sp,
        lineHeight = 11.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun CreditsColumn(
    credits: Int,
    colors: GoldHuntScoreBarColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.gold_hunt_bar_total_credits),
            color = colors.text,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.treasure_star),
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = credits.toString(),
                color = colors.text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Image(
                painter = painterResource(R.drawable.treasure_crystal),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun LogColumn(
    lines: List<String>,
    colors: GoldHuntScoreBarColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
    ) {
        Text(
            text = stringResource(R.string.gold_hunt_bar_log),
            color = colors.text,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (lines.isEmpty()) {
            Text(
                text = stringResource(R.string.gold_hunt_bar_log_empty),
                color = colors.textMuted,
                fontSize = 10.sp,
                lineHeight = 12.sp,
            )
        } else {
            for (line in lines) {
                Text(
                    text = "$line,",
                    color = colors.text,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
