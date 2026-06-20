package com.example.roadguideapp.goldhunt.relics.journal.ui

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
internal fun LegendaryRelicJournalSheetContent(
    state: LegendaryRelicJournalUiState,
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
                    LegendaryRelicJournalProgressCard(state = state, sheetTheme = sheetTheme)
                    LegendaryRelicJournalEntriesSection(state = state, sheetTheme = sheetTheme)
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
                    title = stringResource(R.string.legendary_relic_journal_title),
                    sheetTheme = sheetTheme,
                    onClose = onClose,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun LegendaryRelicJournalProgressCard(
    state: LegendaryRelicJournalUiState,
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
                text = stringResource(R.string.legendary_relic_journal_summary),
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
            Text(
                text = state.completionPercentageLabel,
                color = sheetTheme.onAccent.copy(alpha = 0.9f),
                fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { state.completionProgressFraction.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF22C55E),
                trackColor = sheetTheme.onAccent.copy(alpha = 0.22f),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.piecesFoundProgressLabel,
                color = sheetTheme.onAccent.copy(alpha = 0.92f),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { state.pieceProgressFraction.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = goldAccent,
                trackColor = sheetTheme.onAccent.copy(alpha = 0.22f),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.piecesMissingProgressLabel,
                color = sheetTheme.onAccent.copy(alpha = 0.92f),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { state.piecesMissingProgressFraction.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = sheetTheme.onAccent.copy(alpha = 0.55f),
                trackColor = sheetTheme.onAccent.copy(alpha = 0.22f),
            )
            if (state.hiddenDiscoveryProgressFraction > 0f || state.hiddenDiscoveredProgressLabel.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = state.hiddenDiscoveredProgressLabel,
                    color = sheetTheme.onAccent.copy(alpha = 0.92f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(
                    R.string.legendary_relic_journal_total_rewards,
                    state.totalRewardsLabel,
                ),
                color = sheetTheme.onAccent.copy(alpha = 0.9f),
                fontSize = 14.sp,
            )
            Text(
                text = stringResource(
                    R.string.legendary_relic_journal_total_xp,
                    state.totalXpLabel,
                ),
                color = sheetTheme.onAccent.copy(alpha = 0.9f),
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun LegendaryRelicJournalEntriesSection(
    state: LegendaryRelicJournalUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.legendary_relic_journal_entries),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            state.entries.forEachIndexed { index, entry ->
                if (index > 0) {
                    AuthNavDivider(sheetTheme = sheetTheme)
                }
                LegendaryRelicJournalEntryRow(entry = entry, sheetTheme = sheetTheme)
            }
        }
    }
}

@Composable
private fun LegendaryRelicJournalEntryRow(
    entry: LegendaryRelicJournalEntryUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    val contentAlpha = when {
        entry.isHiddenMasked || entry.isLocked -> 0.72f
        entry.isMissing -> 0.82f
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
            Text(
                text = entry.displayName,
                color = sheetTheme.primaryText,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
            )
            LegendaryRelicJournalStatusBadge(
                label = entry.statusLabel,
                entry = entry,
                sheetTheme = sheetTheme,
            )
        }
        Text(
            text = entry.description,
            color = sheetTheme.secondaryText,
            fontSize = 14.sp,
        )
        LegendaryRelicJournalDetailLine(
            label = stringResource(R.string.legendary_relic_journal_field_category),
            value = entry.categoryLabel,
            sheetTheme = sheetTheme,
        )
        LegendaryRelicJournalDetailLine(
            label = stringResource(R.string.legendary_relic_journal_field_rarity),
            value = entry.rarityLabel,
            sheetTheme = sheetTheme,
        )
        LegendaryRelicJournalDetailLine(
            label = stringResource(R.string.legendary_relic_journal_field_progress),
            value = entry.progressLabel,
            sheetTheme = sheetTheme,
        )
        if (!entry.isHiddenMasked) {
            LinearProgressIndicator(
                progress = { entry.progressFraction.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = sheetTheme.accent,
                trackColor = sheetTheme.secondaryText.copy(alpha = 0.2f),
            )
        }
        LegendaryRelicJournalDetailLine(
            label = stringResource(R.string.legendary_relic_journal_field_pieces_found),
            value = entry.piecesFoundLabel,
            sheetTheme = sheetTheme,
        )
        LegendaryRelicJournalDetailLine(
            label = stringResource(R.string.legendary_relic_journal_field_pieces_missing),
            value = entry.piecesMissingLabel,
            sheetTheme = sheetTheme,
        )
        if (entry.isCompleted) {
            LegendaryRelicJournalDetailLine(
                label = stringResource(R.string.legendary_relic_journal_field_completion_date),
                value = entry.completionDateLabel,
                sheetTheme = sheetTheme,
            )
        }
        if (entry.isLocked) {
            LegendaryRelicJournalDetailLine(
                label = stringResource(R.string.legendary_relic_journal_field_minimum_level),
                value = entry.minimumExplorerLevelLabel,
                sheetTheme = sheetTheme,
            )
        }
        LegendaryRelicJournalDetailLine(
            label = stringResource(R.string.legendary_relic_journal_field_completion_rewards),
            value = entry.rewardsLabel,
            sheetTheme = sheetTheme,
        )
        LegendaryRelicJournalDetailLine(
            label = stringResource(R.string.legendary_relic_journal_field_completion_xp),
            value = entry.xpLabel,
            sheetTheme = sheetTheme,
        )
        LegendaryRelicJournalDetailLine(
            label = stringResource(R.string.legendary_relic_journal_field_reward_status),
            value = entry.rewardsGrantedLabel,
            sheetTheme = sheetTheme,
        )
        entry.badgeLabel?.let { badge ->
            LegendaryRelicJournalDetailLine(
                label = stringResource(R.string.legendary_relic_journal_field_badge),
                value = badge,
                sheetTheme = sheetTheme,
            )
        }
        entry.titleLabel?.let { title ->
            LegendaryRelicJournalDetailLine(
                label = stringResource(R.string.legendary_relic_journal_field_title),
                value = title,
                sheetTheme = sheetTheme,
            )
        }
        entry.powerLabel?.let { power ->
            LegendaryRelicJournalDetailLine(
                label = stringResource(R.string.legendary_relic_journal_field_power),
                value = power,
                sheetTheme = sheetTheme,
            )
        }
        entry.storyFragmentLabel?.let { storyFragment ->
            LegendaryRelicJournalDetailLine(
                label = stringResource(R.string.legendary_relic_journal_field_story_fragment),
                value = storyFragment,
                sheetTheme = sheetTheme,
            )
        }
        entry.chainUnlocksLabel?.let { chainUnlocks ->
            Text(
                text = stringResource(R.string.legendary_relic_journal_field_chain_unlocks),
                color = sheetTheme.secondaryText,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = chainUnlocks,
                color = sheetTheme.primaryText,
                fontSize = 12.sp,
            )
        }
        entry.storyPreview?.let { story ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.legendary_relic_story_section),
                color = sheetTheme.secondaryText,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = story.progressLabel,
                color = sheetTheme.primaryText,
                fontSize = 12.sp,
            )
            if (story.chapters.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.legendary_relic_story_chapters),
                    color = sheetTheme.secondaryText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp),
                )
                story.chapters.forEach { chapter ->
                    LegendaryRelicJournalStoryChapterRow(chapter = chapter, sheetTheme = sheetTheme)
                }
            }
            if (story.loreEntries.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.legendary_relic_story_lore_entries),
                    color = sheetTheme.secondaryText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp),
                )
                story.loreEntries.forEach { lore ->
                    LegendaryRelicJournalStoryLoreRow(lore = lore, sheetTheme = sheetTheme)
                }
            }
            story.completionStory?.let { completion ->
                Text(
                    text = stringResource(R.string.legendary_relic_story_completion_story),
                    color = sheetTheme.secondaryText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp),
                )
                LegendaryRelicJournalStoryCompletionRow(
                    completion = completion,
                    sheetTheme = sheetTheme,
                )
            }
        }
        if (entry.pieces.isNotEmpty() && !entry.isHiddenMasked) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.legendary_relic_journal_field_piece_list),
                color = sheetTheme.secondaryText,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            entry.pieces.forEach { piece ->
                LegendaryRelicJournalPieceRow(piece = piece, sheetTheme = sheetTheme)
            }
        }
    }
}

@Composable
private fun LegendaryRelicJournalPieceRow(
    piece: LegendaryRelicJournalPieceUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (piece.isMissing) 0.78f else 1f)
            .padding(top = 4.dp),
    ) {
        Text(
            text = stringResource(
                R.string.legendary_relic_journal_piece_row,
                piece.pieceNumber,
                piece.statusLabel,
            ),
            color = sheetTheme.primaryText,
            fontSize = 13.sp,
        )
        if (!piece.isMissing) {
            Text(
                text = stringResource(
                    R.string.legendary_relic_journal_piece_details,
                    piece.discoveryDateLabel,
                    piece.sourceLabel,
                ),
                color = sheetTheme.secondaryText,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun LegendaryRelicJournalStoryChapterRow(
    chapter: LegendaryRelicJournalStoryChapterUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (chapter.unlocked) 1f else 0.78f)
            .padding(top = 4.dp),
    ) {
        Text(
            text = "${chapter.title} · ${chapter.statusLabel}",
            color = sheetTheme.primaryText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = chapter.body,
            color = sheetTheme.secondaryText,
            fontSize = 11.sp,
        )
        Text(
            text = chapter.unlockDateLabel,
            color = sheetTheme.secondaryText,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun LegendaryRelicJournalStoryLoreRow(
    lore: LegendaryRelicJournalStoryLoreUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (lore.unlocked) 1f else 0.78f)
            .padding(top = 4.dp),
    ) {
        Text(
            text = "${lore.title} · ${lore.triggerLabel} · ${lore.statusLabel}",
            color = sheetTheme.primaryText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = lore.body,
            color = sheetTheme.secondaryText,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun LegendaryRelicJournalStoryCompletionRow(
    completion: LegendaryRelicJournalStoryCompletionUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (completion.unlocked) 1f else 0.78f)
            .padding(top = 4.dp),
    ) {
        Text(
            text = "${completion.title} · ${completion.statusLabel}",
            color = sheetTheme.primaryText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = completion.body,
            color = sheetTheme.secondaryText,
            fontSize = 11.sp,
        )
        Text(
            text = completion.storyFragmentLabel,
            color = sheetTheme.secondaryText,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun LegendaryRelicJournalStatusBadge(
    label: String,
    entry: LegendaryRelicJournalEntryUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    val background = when {
        entry.isCompleted -> Color(0xFF22C55E).copy(alpha = 0.18f)
        entry.isInProgress -> Color(0xFFFFD60A).copy(alpha = 0.18f)
        entry.isHiddenMasked -> sheetTheme.secondaryText.copy(alpha = 0.16f)
        entry.isLocked -> sheetTheme.secondaryText.copy(alpha = 0.14f)
        else -> sheetTheme.secondaryText.copy(alpha = 0.12f)
    }
    val textColor = when {
        entry.isCompleted -> Color(0xFF22C55E)
        entry.isInProgress -> Color(0xFFFFD60A)
        else -> sheetTheme.secondaryText
    }
    Text(
        text = label,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = textColor,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun LegendaryRelicJournalDetailLine(
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
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            color = sheetTheme.primaryText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
    }
}
