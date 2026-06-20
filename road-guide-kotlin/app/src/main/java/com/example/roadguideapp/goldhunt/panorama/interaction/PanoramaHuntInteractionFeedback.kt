package com.example.roadguideapp.goldhunt.panorama.interaction

import android.content.Context
import android.widget.Toast
import com.example.roadguideapp.R

internal object PanoramaHuntInteractionFeedback {
    fun showToast(context: Context, result: PanoramaHuntTapResult) {
        val message = when (result) {
            is PanoramaHuntTapResult.Miss ->
                context.getString(R.string.panorama_hunt_tap_miss)
            is PanoramaHuntTapResult.Hit ->
                context.getString(
                    R.string.panorama_hunt_tap_hit,
                    result.remainingCount,
                )
            is PanoramaHuntTapResult.AlreadyCollected ->
                context.getString(R.string.panorama_hunt_tap_already_collected)
            is PanoramaHuntTapResult.HuntComplete ->
                context.getString(R.string.panorama_hunt_tap_complete)
            is PanoramaHuntTapResult.PuzzleTap ->
                context.getString(R.string.panorama_hunt_tap_puzzle)
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
