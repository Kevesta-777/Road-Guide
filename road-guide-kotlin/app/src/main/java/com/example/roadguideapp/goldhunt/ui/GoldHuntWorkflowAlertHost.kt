package com.example.roadguideapp.goldhunt.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.roadguideapp.R
import com.example.roadguideapp.goldhunt.GoldHuntController
import com.example.roadguideapp.goldhunt.GoldHuntWorkflowAlert

@Composable
internal fun GoldHuntWorkflowAlertHost(
    goldHunt: GoldHuntController,
) {
    val alert = goldHunt.pendingWorkflowAlert ?: return
    val message = when (alert) {
        GoldHuntWorkflowAlert.Intro -> stringResource(R.string.gold_hunt_workflow_intro)
        GoldHuntWorkflowAlert.FirstStarCollected -> stringResource(R.string.gold_hunt_workflow_first_star)
        GoldHuntWorkflowAlert.FirstFlowerCollected -> stringResource(R.string.gold_hunt_workflow_first_flower)
        GoldHuntWorkflowAlert.FirstCrystalCollected -> stringResource(R.string.gold_hunt_workflow_first_crystal)
        GoldHuntWorkflowAlert.FirstGiftCollected -> stringResource(R.string.gold_hunt_workflow_first_gift)
        GoldHuntWorkflowAlert.FirstSecretDiscovered -> stringResource(R.string.gold_hunt_workflow_first_secret)
        is GoldHuntWorkflowAlert.TreasureHoardAnnounced ->
            stringResource(R.string.gold_hunt_workflow_hoard_announced, alert.regionName)
    }
    val imageRes = when (alert) {
        GoldHuntWorkflowAlert.Intro -> R.drawable.gold_hunt_workflow_intro
        GoldHuntWorkflowAlert.FirstStarCollected -> R.drawable.gold_hunt_workflow_first_star
        GoldHuntWorkflowAlert.FirstFlowerCollected -> R.drawable.gold_hunt_workflow_first_flower
        GoldHuntWorkflowAlert.FirstCrystalCollected -> R.drawable.gold_hunt_workflow_first_crystal
        GoldHuntWorkflowAlert.FirstGiftCollected -> R.drawable.gold_hunt_workflow_first_gift
        GoldHuntWorkflowAlert.FirstSecretDiscovered -> R.drawable.gold_hunt_workflow_first_secret
        else -> null
    }
    AlertDialog(
        onDismissRequest = { goldHunt.clearWorkflowAlert() },
        text = {
            Column {
                if (imageRes != null) {
                    Image(
                        painter = painterResource(imageRes),
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Text(text = message)
            }
        },
        confirmButton = {
            TextButton(onClick = { goldHunt.clearWorkflowAlert() }) {
                Text(text = stringResource(R.string.apple_ok))
            }
        },
    )
}
