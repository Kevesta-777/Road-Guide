package com.example.roadguideapp.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.roadguideapp.map.BusinessDetailEditContent
import com.example.roadguideapp.map.BusinessPoiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun UserProfileSheetContent(
    sheetTheme: AppleMapsSheetTheme,
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
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selectedBusinessPoiId != null) {
                IconButton(onClick = onClearBusinessSelection) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.auth_back),
                        tint = sheetTheme.primaryText,
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
            Text(
                text = if (selectedBusinessPoiId != null) {
                    stringResource(R.string.business_edit_title)
                } else {
                    stringResource(R.string.auth_profile_title)
                },
                color = sheetTheme.primaryText,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
            )
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.apple_close),
                    tint = sheetTheme.primaryText,
                )
            }
        }

        if (selectedBusinessPoiId != null) {
            BusinessDetailEditContent(
                poiId = selectedBusinessPoiId,
                sheetTheme = sheetTheme,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
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
            ProfileSheetRow(
                label = stringResource(R.string.auth_profile_identifier_label),
                value = identifier,
                sheetTheme = sheetTheme,
            )
            Spacer(modifier = Modifier.height(12.dp))
            ProfileSheetRow(
                label = stringResource(R.string.auth_profile_id_label),
                value = profileId,
                sheetTheme = sheetTheme,
                modifier = Modifier.clickable {
                    clipboard.setText(AnnotatedString(profileId))
                    Toast.makeText(context, R.string.auth_profile_id_copied, Toast.LENGTH_SHORT).show()
                },
            )
            Spacer(modifier = Modifier.height(12.dp))
            ProfileSheetRow(
                label = stringResource(R.string.auth_profile_status_label),
                value = stringResource(R.string.auth_profile_status_signed_in),
                sheetTheme = sheetTheme,
            )

            if (isBusinessUser) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.profile_my_business_pois),
                    color = sheetTheme.primaryText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(12.dp))
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

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = sheetTheme.divider)
            Spacer(modifier = Modifier.height(16.dp))
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
            Spacer(modifier = Modifier.height(24.dp))
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
}

@Composable
private fun ProfileSheetRow(
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
