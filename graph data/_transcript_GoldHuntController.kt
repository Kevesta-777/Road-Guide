package com.example.roadguideapp.goldhunt

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.roadguideapp.goldhunt.profile.PlayerProfile
import com.example.roadguideapp.goldhunt.repository.GoldHuntRepository
import com.example.roadguideapp.goldhunt.repository.DiscoveryTickResult
import com.example.roadguideapp.goldhunt.treasure.TreasureCollectOutcome
import com.example.roadguideapp.goldhunt.treasure.TreasureSpec
import com.example.roadguideapp.goldhunt.treasure.TreasureType
import com.example.roadguideapp.goldhunt.treasure.TreasureTapCollector
import com.example.roadguideapp.goldhunt.treasure.TreasureViewportLogState
import com.example.roadguideapp.goldhunt.treasure.TreasureViewportNumberLogState
import com.example.roadguideapp.goldhunt.secretplace.SecretPlaceCollectOutcome
import com.example.roadguideapp.goldhunt.secretplace.SecretPlaceSpec
import com.example.roadguideapp.goldhunt.secretplace.SecretPlaceTapCollector
import com.example.roadguideapp.goldhunt.repository.SecretPlaceDiscoveryResult

@Stable
internal class GoldHuntController(context: Context) {
    private val appContext = context.applicationContext
    private val repository by lazy { GoldHuntRepository.get(appContext) }

    companion object {
        private const val TAG = "GoldHuntController"
    }

    var isActive by mutableStateOf(GoldHuntPreferences.isModeEnabled(appContext))
        private set

    var profile by mutableStateOf<PlayerProfile?>(null)
        private set

    var lastCreditsDelta by mutableIntStateOf(0)
        private set

    var treasureRevision by mutableIntStateOf(0)
        private set

    var secretRevision by mutableIntStateOf(0)
        private set

    var treasureValueLog by mutableStateOf<TreasureViewportLogState?>(null)
        private set

    var treasureNumberLog by mutableStateOf<TreasureViewportNumberLogState?>(null)
        private set

    var initialized by mutableStateOf(false)
        private set

    var pendingWorkflowAlert by mutableStateOf<GoldHuntWorkflowAlert?>(null)
        private set

    fun clearWorkflowAlert() {
        pendingWorkflowAlert = null
    }

    suspend fun initialize() {
        if (initialized) return
        try {
            repository.ensureInitialized()
            profile = repository.loadProfile()
            initialized = true
            if (isActive) {
                markTreasureDirty()
                markSecretDirty()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gold Hunt initialize failed; mode disabled until next launch", e)
            initialized = false
            isActive = false
            GoldHuntPreferences.setModeEnabled(appContext, false)
        }
    }

    fun enterMode() {
        isActive = true
        GoldHuntPreferences.setModeEnabled(appContext, true)
        if (!GoldHuntPreferences.isIntroShown(appContext)) {
            pendingWorkflowAlert = GoldHuntWorkflowAlert.Intro
            GoldHuntPreferences.setIntroShown(appContext)
        }
        markTreasureDirty()
        markSecretDirty()
    }

    fun exitMode() {
        isActive = false
        GoldHuntPreferences.setModeEnabled(appContext, false)
        lastCreditsDelta = 0
        treasureValueLog = null
        treasureNumberLog = null
        treasureRevision++
        secretRevision++
    }

    fun updateTreasureValueLog(state: TreasureViewportLogState?) {
        treasureValueLog = state
    }

    fun updateTreasureNumberLog(state: TreasureViewportNumberLogState?) {
        treasureNumberLog = state
    }

    suspend fun refreshProfile() {
        profile = repository.loadProfile()
    }

    fun onCreditsEarned(amount: Int) {
        if (amount > 0) lastCreditsDelta = amount
    }

    fun clearCreditsDelta() {
        lastCreditsDelta = 0
    }

    fun markTreasureDirty() {
        treasureRevision++
    }

    fun markSecretDirty() {
        secretRevision++
    }

    suspend fun collectSecretPlaceOnTap(
        spec: SecretPlaceSpec,
        mapZoom: Double,
    ): SecretPlaceCollectOutcome {
        if (!initialized || !isActive) return SecretPlaceCollectOutcome.NotReady
        return try {
            val outcome = SecretPlaceTapCollector.collectOnTap(repository, spec, mapZoom)
            if (outcome is SecretPlaceCollectOutcome.Discovered) {
                applySecretDiscoveryTick(
                    SecretPlaceDiscoveryResult(
                        creditsEarned = outcome.credits,
                        newlyDiscovered = true,
                        overlayDirty = true,
                    ),
                )
                if (outcome.credits > 0) {
                    onCreditsEarned(outcome.credits)
                }
            }
            outcome
        } catch (e: Exception) {
            Log.e(TAG, "collectSecretPlaceOnTap failed", e)
            SecretPlaceCollectOutcome.NotReady
        }
    }

    suspend fun applySecretDiscoveryTick(result: SecretPlaceDiscoveryResult) {
        if (result.newlyDiscovered) {
            refreshProfile()
        }
        if (result.overlayDirty) {
            markSecretDirty()
            markTreasureDirty()
        }
    }

    suspend fun collectTreasureOnTap(spec: TreasureSpec): TreasureCollectOutcome {
        if (!initialized || !isActive) return TreasureCollectOutcome.NotReady
        return try {
            val outcome = TreasureTapCollector.collectOnTap(repository, spec)
            if (outcome is TreasureCollectOutcome.Collected) {
                applyTreasureCollectTick(
                    DiscoveryTickResult(
                        treasureOverlayDirty = true,
                        creditsEarned = outcome.credits,
                        newlyExplored = true,
                        collectedTreasureType = outcome.type,
                    ),
                )
            }
            outcome
        } catch (e: Exception) {
            Log.e(TAG, "collectTreasureOnTap failed", e)
            TreasureCollectOutcome.NotReady
        }
    }

    suspend fun applyTreasureCollectTick(result: DiscoveryTickResult) {
        if (result.newlyExplored) {
            if (result.creditsEarned > 0) {
                onCreditsEarned(result.creditsEarned)
            }
            refreshProfile()
            result.collectedTreasureType?.let { evaluateTreasureMilestones(it) }
        }
        if (result.treasureOverlayDirty) {
            markTreasureDirty()
        }
    }

    private suspend fun evaluateTreasureMilestones(type: TreasureType) {
        when (type) {
            TreasureType.STAR -> {
                val stars = repository.collectedTreasureCount(TreasureType.STAR)
                if (stars == 1 && !GoldHuntPreferences.isFirstStarAlertShown(appContext)) {
                    pendingWorkflowAlert = GoldHuntWorkflowAlert.FirstStarCollected
                    GoldHuntPreferences.setFirstStarAlertShown(appContext)
                }
            }
            TreasureType.FLOWER -> {
                val flowers = repository.collectedTreasureCount(TreasureType.FLOWER)
                if (flowers == 1 && !GoldHuntPreferences.isFirstFlowerAlertShown(appContext)) {
                    pendingWorkflowAlert = GoldHuntWorkflowAlert.FirstFlowerCollected
                    GoldHuntPreferences.setFirstFlowerAlertShown(appContext)
                }
            }
            else -> Unit
        }
        markTreasureDirty()
    }

    fun repository(): GoldHuntRepository = repository
}
