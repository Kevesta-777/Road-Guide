package com.example.roadguideapp.map

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
internal fun NearbyBrowseSheetContent(
    sheetTheme: AppleMapsSheetTheme,
    scrollState: ScrollState,
    contentScrollEnabled: Boolean,
    sheetGestures: AppleMapsSheetGestures,
    category: AppleNearbyShortcut,
    results: List<PeliasSearchResult>,
    loading: Boolean,
    errorMessage: String?,
    filterState: NearbyResultsFilter.State,
    availableChains: List<String>,
    pickHoursByGid: Map<String, String>,
    scopeOptions: List<NearbyScopeOption>,
    searchContext: NearbySearchContext,
    onScopeSelected: (NearbySearchContext) -> Unit,
    onFilterChange: (NearbyResultsFilter.State) -> Unit,
    onResultSelected: (PeliasSearchResult) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var stickyHeaderHeightPx by remember(category.labelRes) { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val stickyHeaderHeight = with(density) { stickyHeaderHeightPx.toDp() }

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
                .padding(bottom = 12.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            NearbyCategoryResultsContent(
                sheetTheme = sheetTheme,
                categoryLabel = stringResource(category.labelRes),
                results = results,
                loading = loading,
                errorMessage = errorMessage,
                filterState = filterState,
                availableChains = availableChains,
                pickHoursByGid = pickHoursByGid,
                scopeOptions = scopeOptions,
                selectedSearchContext = searchContext,
                onScopeSelected = onScopeSelected,
                onFilterChange = onFilterChange,
                onResultSelected = onResultSelected,
                modifier = Modifier.padding(horizontal = 0.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .zIndex(1f)
                .background(sheetTheme.stickyHeaderSurface)
                .then(sheetGestures.chromeDrag)
                .onSizeChanged { stickyHeaderHeightPx = it.height },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(sheetGestures.grabberDrag),
            ) {
                AppleMapsSheetGrabber(grabberColor = sheetTheme.grabber)
            }
            NearbyCategorySearchHeader(
                sheetTheme = sheetTheme,
                categoryLabel = stringResource(category.labelRes),
                onClose = onClose,
            )
        }
    }
}
