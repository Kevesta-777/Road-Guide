package com.example.roadguideapp.goldhunt.achievements.journal.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.roadguideapp.R
import com.example.roadguideapp.auth.AuthGroupedCard
import com.example.roadguideapp.auth.AuthNavDivider
import com.example.roadguideapp.auth.AuthPageTopBar
import com.example.roadguideapp.auth.AuthSectionLabel
import com.example.roadguideapp.map.AppleMapsSheetGestures
import com.example.roadguideapp.map.AppleMapsSheetTheme

private val StickyHeaderMinHeight = 52.dp

@Composable
internal fun AchievementJournalSheetContent(
    state: AchievementJournalUiState,
    isLoading: Boolean,
    sheetTheme: AppleMapsSheetTheme,
    scrollState: ScrollState,
    contentScrollEnabled: Boolean,
    sheetGestures: AppleMapsSheetGestures,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    var stickyHeaderHeightPx = remember { mutableIntStateOf(0) }
    val stickyHeaderHeight = with(density) {
        maxOf(stickyHeaderHeightPx.intValue.toDp(), StickyHeaderMinHeight)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(sheetTheme.sheetSurface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    sheetGestures.scrollContent(
                        scrollState = scrollState,
                        scrollEnabled = contentScrollEnabled,
                    ),
                )
                .padding(top = stickyHeaderHeight)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = sheetTheme.accent)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    AchievementJournalProgressCard(state = state, sheetTheme = sheetTheme)
                    AchievementJournalSection(
                        title = stringResource(R.string.achievement_journal_section_completed),
                        entries = state.completedEntries,
                        sheetTheme = sheetTheme,
                        accent = Color(0xFF22C55E),
                    )
                    AchievementJournalSection(
                        title = stringResource(R.string.achievement_journal_section_incomplete),
                        entries = state.incompleteEntries,
                        sheetTheme = sheetTheme,
                        accent = Color(0xFFFFD60A),
                    )
                    AchievementJournalSection(
                        title = stringResource(R.string.achievement_journal_section_locked),
                        entries = state.lockedEntries,
                        sheetTheme = sheetTheme,
                        accent = sheetTheme.secondaryText,
                    )
                    AchievementJournalSection(
                        title = stringResource(R.string.achievement_journal_section_hidden),
                        entries = state.hiddenEntries,
                        sheetTheme = sheetTheme,
                        accent = sheetTheme.secondaryText.copy(alpha = 0.7f),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .zIndex(1f)
                .background(sheetTheme.sheetSurface)
                .then(sheetGestures.chromeDrag)
                .onSizeChanged { stickyHeaderHeightPx.intValue = it.height },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                AuthPageTopBar(
                    title = stringResource(R.string.achievement_journal_title),
                    sheetTheme = sheetTheme,
                    onClose = onClose,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun AchievementJournalProgressCard(
    state: AchievementJournalUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    val goldAccent = Color(0xFFFFD60A)
    val cardBrush = Brush.linearGradient(
        colors = listOf(
            sheetTheme.summaryCardBackground,
            sheetTheme.summaryCardBackground.copy(alpha = 0.88f),
        ),
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(cardBrush)
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Text(
                text = stringResource(R.string.achievement_journal_summary),
                color = sheetTheme.onAccent.copy(alpha = 0.82f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.completedProgressLabel,
                color = sheetTheme.onAccent,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { state.completionProgressFraction.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = goldAccent,
                trackColor = sheetTheme.onAccent.copy(alpha = 0.22f),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(
                    R.string.achievement_journal_completion_percentage,
                    state.completionPercentageLabel,
                ),
                color = sheetTheme.onAccent.copy(alpha = 0.92f),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.incompleteProgressLabel,
                color = sheetTheme.onAccent.copy(alpha = 0.9f),
                fontSize = 14.sp,
            )
            Text(
                text = state.lockedProgressLabel,
                color = sheetTheme.onAccent.copy(alpha = 0.9f),
                fontSize = 14.sp,
            )
            Text(
                text = state.hiddenProgressLabel,
                color = sheetTheme.onAccent.copy(alpha = 0.9f),
                fontSize = 14.sp,
            )
            state.secretProgressLabel?.let { secretProgress ->
                Text(
                    text = secretProgress,
                    color = sheetTheme.onAccent.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
private fun AchievementJournalSection(
    title: String,
    entries: List<AchievementJournalEntryUiState>,
    sheetTheme: AppleMapsSheetTheme,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(text = title, sheetTheme = sheetTheme)
        AuthGroupedCard(sheetTheme = sheetTheme) {
            entries.forEachIndexed { index, entry ->
                if (index > 0) {
                    AuthNavDivider(sheetTheme = sheetTheme)
                }
                AchievementJournalEntryRow(
                    entry = entry,
                    sheetTheme = sheetTheme,
                    accent = accent,
                )
            }
        }
    }
}

@Composable
private fun AchievementJournalEntryRow(
    entry: AchievementJournalEntryUiState,
    sheetTheme: AppleMapsSheetTheme,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val contentAlpha = when {
        entry.isHidden -> 0.55f
        entry.isLocked -> 0.72f
        else -> 1f
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(contentAlpha)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.displayName,
                    color = sheetTheme.primaryText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = entry.categoryLabel,
                    color = sheetTheme.secondaryText,
                    fontSize = 12.sp,
                )
            }
            AchievementJournalStatusBadge(
                label = entry.statusLabel,
                accent = accent,
                sheetTheme = sheetTheme,
            )
        }
        if (entry.description.isNotBlank()) {
            Text(
                text = entry.description,
                color = sheetTheme.secondaryText,
                fontSize = 13.sp,
            )
        }
        AchievementJournalDetailLine(
            label = stringResource(R.string.achievement_journal_field_progress),
            value = entry.progressLabel,
            sheetTheme = sheetTheme,
        )
        if (!entry.isHidden) {
            LinearProgressIndicator(
                progress = { entry.progressFraction.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = accent,
                trackColor = sheetTheme.secondaryText.copy(alpha = 0.18f),
            )
        }
        AchievementJournalDetailLine(
            label = stringResource(R.string.achievement_journal_field_rewards),
            value = entry.rewardPreviewLabel,
            sheetTheme = sheetTheme,
        )
        if (entry.isCompleted) {
            AchievementJournalDetailLine(
                label = stringResource(R.string.achievement_journal_field_completion_date),
                value = entry.completionDateLabel,
                sheetTheme = sheetTheme,
            )
        }
        entry.minimumLevelLabel?.let { minimumLevel ->
            AchievementJournalDetailLine(
                label = stringResource(R.string.achievement_journal_field_minimum_level),
                value = minimumLevel,
                sheetTheme = sheetTheme,
            )
        }
    }
}

@Composable
private fun AchievementJournalDetailLine(
    label: String,
    value: String,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            color = sheetTheme.secondaryText,
            fontSize = 13.sp,
            modifier = Modifier.weight(0.42f),
        )
        Text(
            text = value,
            color = sheetTheme.primaryText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.58f),
        )
    }
}

@Composable
private fun AchievementJournalStatusBadge(
    label: String,
    accent: Color,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        color = accent,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(accent.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
