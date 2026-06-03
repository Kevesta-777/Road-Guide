package com.example.roadguideapp.auth

import android.widget.Toast
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.roadguideapp.R
import com.example.roadguideapp.map.AppleMapsSheetGestures
import com.example.roadguideapp.map.AppleMapsSheetTheme
import com.example.roadguideapp.map.BusinessDetailEditContent
import com.example.roadguideapp.map.BusinessPoiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val ProfileSheetStickyHeaderMinHeight = 52.dp

@Composable
internal fun UserProfileSheetContent(
    sheetTheme: AppleMapsSheetTheme,
    scrollState: ScrollState,
    contentScrollEnabled: Boolean,
    sheetGestures: AppleMapsSheetGestures,
    identifier: String,
    profileId: String,
    abbreviation: String,
    friendsCount: Int,
    selectedBusinessPoiId: String?,
    onClose: () -> Unit,
    onClearBusinessSelection: () -> Unit,
    onBusinessPoiSelected: (BusinessPoiClient.MyBusinessPoi) -> Unit,
    onResetCredentials: () -> Unit,
    onCreateNewAccount: () -> Unit,
    onSignOut: () -> Unit,
    onMyQrCode: () -> Unit,
    onAddFriend: () -> Unit,
    onFriendsList: () -> Unit,
    onCompanionFinder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isBusinessUser = remember { OfflineAuthStore.isBusinessUser(context) }
    var myBusinessPois by remember { mutableStateOf<List<BusinessPoiClient.MyBusinessPoi>>(emptyList()) }
    var loadingBusinessPois by remember { mutableStateOf(false) }

    LaunchedEffect(isBusinessUser) {
        if (!isBusinessUser) return@LaunchedEffect
        val token = OfflineAuthStore.sessionToken(context) ?: return@LaunchedEffect
        loadingBusinessPois = true
        when (val result = withContext(Dispatchers.IO) { BusinessPoiClient.listMyBusinessPois(token) }) {
            is BusinessPoiClient.ListMineResult.Success -> myBusinessPois = result.pois
            is BusinessPoiClient.ListMineResult.Failure -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
            }
        }
        loadingBusinessPois = false
    }

    val density = LocalDensity.current
    var stickyHeaderHeightPx by remember { mutableIntStateOf(0) }
    val stickyHeaderHeight = with(density) {
        maxOf(stickyHeaderHeightPx.toDp(), ProfileSheetStickyHeaderMinHeight)
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
                .padding(bottom = 16.dp),
        ) {
            if (selectedBusinessPoiId != null) {
                BusinessDetailEditContent(
                    poiId = selectedBusinessPoiId,
                    sheetTheme = sheetTheme,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    AuthProfileHero(
                        abbreviation = abbreviation,
                        title = identifier,
                        subtitle = stringResource(R.string.auth_profile_status_signed_in),
                        sheetTheme = sheetTheme,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    ProfileDetailsSection(
                        identifier = identifier,
                        profileId = profileId,
                        sheetTheme = sheetTheme,
                    )
                    ProfileFriendsSection(
                        friendsCount = friendsCount,
                        sheetTheme = sheetTheme,
                        onMyQrCode = onMyQrCode,
                        onAddFriend = onAddFriend,
                        onFriendsList = onFriendsList,
                        onCompanionFinder = onCompanionFinder,
                    )
                    ProfileAccountSection(
                        sheetTheme = sheetTheme,
                        onResetCredentials = onResetCredentials,
                        onCreateNewAccount = onCreateNewAccount,
                        onSignOut = onSignOut,
                    )

                    if (isBusinessUser) {
                        Spacer(modifier = Modifier.height(24.dp))
                        AuthSectionLabel(
                            text = stringResource(R.string.profile_my_business_pois),
                            sheetTheme = sheetTheme,
                        )
                        when {
                            loadingBusinessPois -> {
                                CircularProgressIndicator(modifier = Modifier.padding(vertical = 12.dp))
                            }
                            myBusinessPois.isEmpty() -> {
                                Text(
                                    text = stringResource(R.string.profile_my_business_pois_empty),
                                    color = sheetTheme.secondaryText,
                                )
                            }
                            else -> {
                                myBusinessPois.forEach { poi ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .clickable { onBusinessPoiSelected(poi) },
                                        shape = RoundedCornerShape(12.dp),
                                        color = sheetTheme.searchFieldFill,
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = poi.name,
                                                color = sheetTheme.primaryText,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                            if (poi.address.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = poi.address,
                                                    color = sheetTheme.secondaryText,
                                                    fontSize = 14.sp,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
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
                .onSizeChanged { stickyHeaderHeightPx = it.height },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                AuthPageTopBar(
                    title = if (selectedBusinessPoiId != null) {
                        stringResource(R.string.business_edit_title)
                    } else {
                        null
                    },
                    sheetTheme = sheetTheme,
                    onBack = if (selectedBusinessPoiId != null) onClearBusinessSelection else null,
                    onClose = onClose,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
