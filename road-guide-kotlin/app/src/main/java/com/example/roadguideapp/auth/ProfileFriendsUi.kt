package com.example.roadguideapp.auth

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roadguideapp.R
import com.example.roadguideapp.map.AppleMapsSheetTheme

@Composable
internal fun ProfileDetailsSection(
    identifier: String,
    profileId: String,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.profile_section_details),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            AuthInfoRow(
                label = stringResource(R.string.auth_profile_identifier_label),
                value = identifier,
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            AuthInfoRow(
                label = stringResource(R.string.auth_profile_id_label),
                value = profileId,
                sheetTheme = sheetTheme,
                modifier = Modifier.clickable {
                    clipboard.setText(AnnotatedString(profileId))
                    Toast.makeText(context, R.string.auth_profile_id_copied, Toast.LENGTH_SHORT).show()
                },
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            AuthInfoRow(
                label = stringResource(R.string.auth_profile_status_label),
                value = stringResource(R.string.auth_profile_status_signed_in),
                sheetTheme = sheetTheme,
            )
        }
    }
}

@Composable
internal fun ProfileFriendsSection(
    friendsCount: Int,
    sheetTheme: AppleMapsSheetTheme,
    onMyQrCode: () -> Unit,
    onAddFriend: () -> Unit,
    onFriendsList: () -> Unit,
    onCompanionFinder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(24.dp))
        AuthSectionLabel(
            text = stringResource(R.string.profile_section_friends),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            AuthNavRow(
                icon = Icons.Outlined.DirectionsCar,
                title = stringResource(R.string.companion_title),
                subtitle = stringResource(R.string.companion_subtitle),
                sheetTheme = sheetTheme,
                onClick = onCompanionFinder,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            AuthNavRow(
                icon = Icons.Outlined.QrCode2,
                title = stringResource(R.string.friends_my_qr),
                subtitle = stringResource(R.string.friends_my_qr_subtitle),
                sheetTheme = sheetTheme,
                onClick = onMyQrCode,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            AuthNavRow(
                icon = Icons.Outlined.PersonAdd,
                title = stringResource(R.string.friends_add),
                subtitle = stringResource(R.string.friends_add_menu_subtitle),
                sheetTheme = sheetTheme,
                onClick = onAddFriend,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            AuthNavRow(
                icon = Icons.Outlined.Group,
                title = stringResource(R.string.friends_list_title),
                subtitle = stringResource(R.string.friends_count, friendsCount),
                sheetTheme = sheetTheme,
                onClick = onFriendsList,
            )
        }
    }
}

@Composable
internal fun ProfileAccountSection(
    sheetTheme: AppleMapsSheetTheme,
    onResetCredentials: () -> Unit,
    onCreateNewAccount: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(24.dp))
        AuthSectionLabel(
            text = stringResource(R.string.profile_section_account),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            AuthNavRow(
                icon = Icons.Outlined.Refresh,
                title = stringResource(R.string.auth_reset_credentials),
                subtitle = null,
                sheetTheme = sheetTheme,
                onClick = onResetCredentials,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            AuthNavRow(
                icon = Icons.Outlined.Badge,
                title = stringResource(R.string.auth_create_new_account),
                subtitle = null,
                sheetTheme = sheetTheme,
                onClick = onCreateNewAccount,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            AuthNavRow(
                icon = Icons.Outlined.Logout,
                title = stringResource(R.string.auth_sign_out),
                subtitle = null,
                sheetTheme = sheetTheme,
                onClick = onSignOut,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.auth_offline_notice),
            color = sheetTheme.tertiaryText,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}
