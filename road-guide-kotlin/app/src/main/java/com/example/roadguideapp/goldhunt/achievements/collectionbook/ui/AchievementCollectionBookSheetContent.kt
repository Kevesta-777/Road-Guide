package com.example.roadguideapp.goldhunt.achievements.collectionbook.ui

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
internal fun AchievementCollectionBookSheetContent(
    state: AchievementCollectionBookUiState,
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
                    AchievementCollectionBookProgressCard(state = state, sheetTheme = sheetTheme)
                    AchievementCollectionBookStatisticsSection(state = state, sheetTheme = sheetTheme)
                    AchievementCollectionBookAchievementSection(
                        title = stringResource(R.string.achievement_collection_book_section_legendary),
                        entries = state.legendaryAchievements,
                        sheetTheme = sheetTheme,
                        accent = Color(0xFFFFD60A),
                    )
                    AchievementCollectionBookAchievementSection(
                        title = stringResource(R.string.achievement_collection_book_section_rare),
                        entries = state.rareAchievements,
                        sheetTheme = sheetTheme,
                        accent = Color(0xFF8B5CF6),
                    )
                    AchievementCollectionBookAchievementSection(
                        title = stringResource(R.string.achievement_collection_book_section_completed),
                        entries = state.completedAchievements,
                        sheetTheme = sheetTheme,
                        accent = Color(0xFF22C55E),
                    )
                    AchievementCollectionBookBadgeSection(
                        title = stringResource(R.string.achievement_collection_book_section_badges_unlocked),
                        entries = state.unlockedBadges,
                        sheetTheme = sheetTheme,
                        accent = Color(0xFF22C55E),
                    )
                    AchievementCollectionBookBadgeSection(
                        title = stringResource(R.string.achievement_collection_book_section_badges_locked),
                        entries = state.lockedBadges,
                        sheetTheme = sheetTheme,
                        accent = sheetTheme.secondaryText,
                        locked = true,
                    )
                    AchievementCollectionBookTitleSection(
                        title = stringResource(R.string.achievement_collection_book_section_titles_unlocked),
                        entries = state.unlockedTitles,
                        sheetTheme = sheetTheme,
                        accent = Color(0xFF22C55E),
                    )
                    AchievementCollectionBookTitleSection(
                        title = stringResource(R.string.achievement_collection_book_section_titles_locked),
                        entries = state.lockedTitles,
                        sheetTheme = sheetTheme,
                        accent = sheetTheme.secondaryText,
                        locked = true,
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
                    title = stringResource(R.string.achievement_collection_book_title),
                    sheetTheme = sheetTheme,
                    onClose = onClose,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun AchievementCollectionBookProgressCard(
    state: AchievementCollectionBookUiState,
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
                text = stringResource(R.string.achievement_collection_book_summary),
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
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = state.explorerLevelLabel,
                color = sheetTheme.onAccent.copy(alpha = 0.9f),
                fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.height(10.dp))
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
                    R.string.achievement_collection_book_completion_percentage,
                    state.completionPercentageLabel,
                ),
                color = sheetTheme.onAccent.copy(alpha = 0.92f),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun AchievementCollectionBookStatisticsSection(
    state: AchievementCollectionBookUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.achievement_collection_book_section_statistics),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            CollectionBookStatLine(
                label = stringResource(R.string.achievement_collection_book_stat_rare_legendary),
                value = state.rareLegendaryLabel,
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            CollectionBookStatLine(
                label = stringResource(R.string.achievement_collection_book_stat_badges),
                value = state.badgesProgressLabel,
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            CollectionBookStatLine(
                label = stringResource(R.string.achievement_collection_book_stat_titles),
                value = state.titlesProgressLabel,
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            CollectionBookStatLine(
                label = stringResource(R.string.achievement_collection_book_stat_story_relic),
                value = state.storyRelicProgressLabel,
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            CollectionBookStatLine(
                label = stringResource(R.string.achievement_collection_book_stat_rewards),
                value = state.rewardTotalsLabel,
                sheetTheme = sheetTheme,
            )
        }
    }
}

@Composable
private fun CollectionBookStatLine(
    label: String,
    value: String,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
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
private fun AchievementCollectionBookAchievementSection(
    title: String,
    entries: List<AchievementCollectionBookAchievementUiState>,
    sheetTheme: AppleMapsSheetTheme,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(text = title, sheetTheme = sheetTheme)
        AuthGroupedCard(sheetTheme = sheetTheme) {
            entries.forEachIndexed { index, entry ->
                if (index > 0) AuthNavDivider(sheetTheme = sheetTheme)
                AchievementCollectionBookAchievementRow(
                    entry = entry,
                    sheetTheme = sheetTheme,
                    accent = accent,
                )
            }
        }
    }
}

@Composable
private fun AchievementCollectionBookAchievementRow(
    entry: AchievementCollectionBookAchievementUiState,
    sheetTheme: AppleMapsSheetTheme,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
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
            CollectionBookChip(label = entry.rarityLabel, accent = accent)
        }
        CollectionBookDetailLine(
            label = stringResource(R.string.achievement_collection_book_field_rewards),
            value = entry.rewardHooksLabel,
            sheetTheme = sheetTheme,
        )
        if (entry.isCompleted) {
            CollectionBookDetailLine(
                label = stringResource(R.string.achievement_journal_field_completion_date),
                value = entry.completionDateLabel,
                sheetTheme = sheetTheme,
            )
        } else {
            CollectionBookDetailLine(
                label = stringResource(R.string.achievement_journal_field_progress),
                value = entry.progressLabel,
                sheetTheme = sheetTheme,
            )
            entry.minimumLevelLabel?.let { minimumLevel ->
                CollectionBookDetailLine(
                    label = stringResource(R.string.achievement_journal_field_minimum_level),
                    value = minimumLevel,
                    sheetTheme = sheetTheme,
                )
            }
        }
    }
}

@Composable
private fun AchievementCollectionBookBadgeSection(
    title: String,
    entries: List<AchievementCollectionBookBadgeUiState>,
    sheetTheme: AppleMapsSheetTheme,
    accent: Color,
    locked: Boolean = false,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(text = title, sheetTheme = sheetTheme)
        AuthGroupedCard(sheetTheme = sheetTheme) {
            entries.forEachIndexed { index, entry ->
                if (index > 0) AuthNavDivider(sheetTheme = sheetTheme)
                AchievementCollectionBookBadgeRow(
                    entry = entry,
                    sheetTheme = sheetTheme,
                    accent = accent,
                    locked = locked,
                )
            }
        }
    }
}

@Composable
private fun AchievementCollectionBookBadgeRow(
    entry: AchievementCollectionBookBadgeUiState,
    sheetTheme: AppleMapsSheetTheme,
    accent: Color,
    locked: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (locked) 0.62f else 1f)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = entry.displayName,
                color = sheetTheme.primaryText,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            CollectionBookChip(label = entry.rarityLabel, accent = accent)
        }
        CollectionBookDetailLine(
            label = stringResource(R.string.achievement_badge_field_unlock_date),
            value = entry.unlockDateLabel,
            sheetTheme = sheetTheme,
        )
    }
}

@Composable
private fun AchievementCollectionBookTitleSection(
    title: String,
    entries: List<AchievementCollectionBookTitleUiState>,
    sheetTheme: AppleMapsSheetTheme,
    accent: Color,
    locked: Boolean = false,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(text = title, sheetTheme = sheetTheme)
        AuthGroupedCard(sheetTheme = sheetTheme) {
            entries.forEachIndexed { index, entry ->
                if (index > 0) AuthNavDivider(sheetTheme = sheetTheme)
                AchievementCollectionBookTitleRow(
                    entry = entry,
                    sheetTheme = sheetTheme,
                    accent = accent,
                    locked = locked,
                )
            }
        }
    }
}

@Composable
private fun AchievementCollectionBookTitleRow(
    entry: AchievementCollectionBookTitleUiState,
    sheetTheme: AppleMapsSheetTheme,
    accent: Color,
    locked: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (locked) 0.62f else 1f)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = entry.displayName,
                color = sheetTheme.primaryText,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            if (entry.isActive) {
                CollectionBookChip(
                    label = stringResource(R.string.achievement_title_action_active),
                    accent = accent,
                )
            } else {
                CollectionBookChip(label = entry.rarityLabel, accent = accent)
            }
        }
        CollectionBookDetailLine(
            label = stringResource(R.string.achievement_title_field_unlock_date),
            value = entry.unlockDateLabel,
            sheetTheme = sheetTheme,
        )
    }
}

@Composable
private fun CollectionBookDetailLine(
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
private fun CollectionBookChip(
    label: String,
    accent: Color,
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
