package com.example.roadguideapp.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.roadguideapp.R

@Composable
internal fun CompanionFinderScreen(
    onBack: () -> Unit,
    onOfferRide: () -> Unit,
    onBrowseRides: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetTheme = rememberAuthSheetTheme()

    // Subscription gating disabled until subscription page is implemented.
    // val context = LocalContext.current
    // var canOfferRide by remember { mutableStateOf<Boolean?>(null) }
    // var planName by remember { mutableStateOf("") }
    // var showPremiumDialog by remember { mutableStateOf(false) }
    //
    // LaunchedEffect(Unit) {
    //     val token = OfflineAuthStore.sessionToken(context)
    //     if (token.isNullOrBlank()) {
    //         canOfferRide = false
    //         return@LaunchedEffect
    //     }
    //     when (val result = withContext(Dispatchers.IO) { SubscriptionClient.getStatus(token) }) {
    //         is SubscriptionClient.StatusResult.Success -> {
    //             canOfferRide = result.status.canOfferRide
    //             planName = result.status.planName
    //         }
    //         is SubscriptionClient.StatusResult.Failure -> {
    //             canOfferRide = false
    //         }
    //     }
    // }
    //
    // if (showPremiumDialog) {
    //     AlertDialog(
    //         onDismissRequest = { showPremiumDialog = false },
    //         icon = { AuthIconBadge(icon = Icons.Outlined.Star, sheetTheme = sheetTheme) },
    //         title = { Text(stringResource(R.string.subscription_premium_required_title)) },
    //         text = { Text(stringResource(R.string.subscription_premium_required_message)) },
    //         confirmButton = {
    //             TextButton(onClick = { showPremiumDialog = false }) {
    //                 Text(stringResource(R.string.companion_picker_confirm))
    //             }
    //         },
    //     )
    // }

    AuthPageScaffold(
        title = stringResource(R.string.companion_title),
        subtitle = stringResource(R.string.companion_subtitle),
        onBack = onBack,
        modifier = modifier,
        sheetTheme = sheetTheme,
    ) {
        // if (canOfferRide == true && planName.isNotBlank()) {
        //     Text(
        //         text = stringResource(R.string.subscription_active_plan, planName),
        //         color = sheetTheme.secondaryText,
        //         fontSize = 13.sp,
        //     )
        //     Spacer(modifier = Modifier.height(8.dp))
        // }
        AuthOptionCard(
            icon = Icons.Outlined.DirectionsCar,
            title = stringResource(R.string.companion_offer_title),
            subtitle = stringResource(R.string.companion_offer_subtitle),
            sheetTheme = sheetTheme,
            onClick = onOfferRide,
            // onClick = {
            //     if (canOfferRide == true) {
            //         onOfferRide()
            //     } else {
            //         showPremiumDialog = true
            //     }
            // },
        )
        Spacer(modifier = Modifier.height(12.dp))
        AuthOptionCard(
            icon = Icons.Outlined.Search,
            title = stringResource(R.string.companion_browse_title),
            subtitle = stringResource(R.string.companion_compare_hint),
            sheetTheme = sheetTheme,
            onClick = onBrowseRides,
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}
