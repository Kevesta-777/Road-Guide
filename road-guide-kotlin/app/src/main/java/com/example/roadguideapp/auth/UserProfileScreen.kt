package com.example.roadguideapp.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.roadguideapp.R

@Composable
internal fun UserProfileScreen(
    identifier: String,
    profileId: String,
    abbreviation: String,
    friendsCount: Int,
    onBack: () -> Unit,
    onResetCredentials: () -> Unit,
    onCreateNewAccount: () -> Unit,
    onSignOut: () -> Unit,
    onMyQrCode: () -> Unit,
    onAddFriend: () -> Unit,
    onFriendsList: () -> Unit,
    onCompanionFinder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetTheme = rememberAuthSheetTheme()

    AuthPageScaffold(
        title = stringResource(R.string.auth_profile_title),
        subtitle = stringResource(R.string.auth_profile_subtitle),
        onBack = onBack,
        modifier = modifier,
        sheetTheme = sheetTheme,
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
            modifier = Modifier.padding(bottom = 16.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}
