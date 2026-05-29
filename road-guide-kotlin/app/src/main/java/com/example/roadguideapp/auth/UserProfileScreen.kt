package com.example.roadguideapp.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roadguideapp.R
import com.example.roadguideapp.map.AppleMapsSheetTheme

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
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val sheetTheme = rememberAuthSheetTheme()

    AuthPageScaffold(
        title = stringResource(R.string.auth_profile_title),
        subtitle = stringResource(R.string.auth_profile_subtitle),
        onBack = onBack,
        modifier = modifier,
        sheetTheme = sheetTheme,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(sheetTheme.profileAvatarBg, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = abbreviation,
                color = sheetTheme.profileAvatarText,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        ProfileDetailRow(
            label = stringResource(R.string.auth_profile_identifier_label),
            value = identifier,
            sheetTheme = sheetTheme,
        )
        Spacer(modifier = Modifier.height(12.dp))
        ProfileDetailRow(
            label = stringResource(R.string.auth_profile_id_label),
            value = profileId,
            sheetTheme = sheetTheme,
            modifier = Modifier.clickable {
                clipboard.setText(AnnotatedString(profileId))
                Toast.makeText(context, R.string.auth_profile_id_copied, Toast.LENGTH_SHORT).show()
            },
        )
        Spacer(modifier = Modifier.height(12.dp))
        ProfileDetailRow(
            label = stringResource(R.string.auth_profile_status_label),
            value = stringResource(R.string.auth_profile_status_signed_in),
            sheetTheme = sheetTheme,
        )
        Spacer(modifier = Modifier.height(24.dp))
        AuthPrimaryButton(
            text = stringResource(R.string.friends_my_qr),
            onClick = onMyQrCode,
            sheetTheme = sheetTheme,
        )
        Spacer(modifier = Modifier.height(12.dp))
        AuthSecondaryButton(
            text = stringResource(R.string.friends_add),
            onClick = onAddFriend,
            sheetTheme = sheetTheme,
        )
        Spacer(modifier = Modifier.height(12.dp))
        AuthSecondaryButton(
            text = stringResource(R.string.friends_count, friendsCount),
            onClick = onFriendsList,
            sheetTheme = sheetTheme,
        )
        Spacer(modifier = Modifier.height(32.dp))
        AuthPrimaryButton(
            text = stringResource(R.string.auth_reset_credentials),
            onClick = onResetCredentials,
            sheetTheme = sheetTheme,
        )
        Spacer(modifier = Modifier.height(12.dp))
        AuthSecondaryButton(
            text = stringResource(R.string.auth_create_new_account),
            onClick = onCreateNewAccount,
            sheetTheme = sheetTheme,
        )
        Spacer(modifier = Modifier.height(12.dp))
        AuthSecondaryButton(
            text = stringResource(R.string.auth_sign_out),
            onClick = onSignOut,
            sheetTheme = sheetTheme,
        )
    }
}

@Composable
private fun ProfileDetailRow(
    label: String,
    value: String,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = sheetTheme.secondaryText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = sheetTheme.primaryText,
            fontSize = 17.sp,
        )
    }
}
