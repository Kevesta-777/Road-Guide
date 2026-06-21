package com.example.roadguideapp.goldhunt

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.roadguideapp.goldhunt.profile.PlayerProfile
import com.example.roadguideapp.goldhunt.repository.DiscoveryTickResult
import com.example.roadguideapp.goldhunt.repository.GoldHuntRepository
import com.example.roadguideapp.goldhunt.repository.SecretPlaceDiscoveryResult
import com.example.roadguideapp.goldhunt.secretplace.SecretPlaceCollectOutcome
import com.example.roadguideapp.goldhunt.secretplace.SecretPlaceSpec
import com.example.roadguideapp.goldhunt.secretplace.SecretPlaceTapCollector
import com.example.roadguideapp.goldhunt.treasure.TreasureCollectOutcome
import com.example.roadguideapp.goldhunt.treasure.TreasureHoardCatalog
import com.example.roadguideapp.goldhunt.treasure.TreasureHoardRegion
import com.example.roadguideapp.goldhunt.treasure.TreasureSpec
import com.example.roadguideapp.goldhunt.treasure.TreasureTapCollector
import com.example.roadguideapp.goldhunt.treasure.TreasureType
import com.example.roadguideapp.goldhunt.treasure.TreasureViewportLogState
import com.example.roadguideapp.goldhunt.treasure.TreasureViewportNumberLogState

internal enum class GoldHuntEntryPhase {
    Idle,
    Zooming,
    Revealing,
    Complete,
}

@Stable
internal class GoldHuntController(context: Context) {
    private val appContext = context.applicationContext
    private val repository by lazy { GoldHuntRepository.get(appContext) }

    companion object {
        private const val TAG = "GoldHuntController"
    }

    var isActive by mutableStateOf(false)
        private set

    var profile by mutableStateOf<PlayerProfile?>(null)
        private set

    var lastCreditsDelta by mutableIntStateOf(0)
        private set

    var treasureRevision by mutableIntStateOf(0)
        private set

    var secretRevision by mutableIntStateOf(0)
        private set

    var clusterRevision by mutableIntStateOf(0)
        private set

    var treasureValueLog by mutableStateOf<TreasureViewportLogState?>(null)
        private set

    var treasureNumberLog by mutableStateOf<TreasureViewportNumberLogState?>(null)
        private set

    var initialized by mutableStateOf(false)
        private set

    var pendingWorkflowAlert by mutableStateOf<GoldHuntWorkflowAlert?>(null)
        private set

    var entryPhase by mutableStateOf(GoldHuntEntryPhase.Idle)
        private set

    var treasurePopInScale by mutableFloatStateOf(1f)
        private set

    var treasureAppearProgress by mutableFloatStateOf(1f)
        private set

    var zoomTreasureRevealNonce by mutableIntStateOf(0)
        private set

    var zoomTreasureDisappearNonce by mutableIntStateOf(0)
        private set

    var isTreasureDisappearing by mutableStateOf(false)
        private set

    var treasureDisappearProgress by mutableFloatStateOf(0f)
        private set

    var hiddenTreasureRevision by mutableIntStateOf(0)
        private set

    private val hiddenTreasureIds = mutableSetOf<String>()
    private var pendingHoardSecretPlace: SecretPlaceSpec? = null
    private var pendingHoardToActivate: TreasureHoardRegion? = null

    fun clearWorkflowAlert() {
        when (pendingWorkflowAlert) {
            GoldHuntWorkflowAlert.FirstSecretDiscovered -> {
                pendingWorkflowAlert = null
                announceTreasureHoard()
            }
            is GoldHuntWorkflowAlert.TreasureHoardAnnounced -> {
                pendingWorkflowAlert = null
                activatePendingTreasureHoard()
            }
            else -> pendingWorkflowAlert = null
        }
    }

    suspend fun initialize() {
        GoldHuntPreferences.clearPersistedMode(appContext)
        if (initialized) return
        try {
            repository.ensureInitialized()
            profile = repository.loadProfile()
            initialized = true
            isActive = false
            if (isActive) {
                markTreasureDirty()
                markSecretDirty()
                markClusterDirty()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gold Hunt initialize failed; mode disabled until next launch", e)
            initialized = false
            isActive = false
            GoldHuntPreferences.clearPersistedMode(appContext)
        }
    }

    fun enterMode() {
        if (!initialized) return
        isActive = true
        if (!GoldHuntPreferences.isIntroShown(appContext)) {
            pendingWorkflowAlert = GoldHuntWorkflowAlert.Intro
            GoldHuntPreferences.setIntroShown(appContext)
        }
        entryPhase = GoldHuntEntryPhase.Zooming
        markTreasureDirty()
        markSecretDirty()
        markClusterDirty()
    }

    fun exitMode() {
        isActive = false
        GoldHuntPreferences.clearPersistedMode(appContext)
        lastCreditsDelta = 0
        treasureValueLog = null
        treasureNumberLog = null
        entryPhase = GoldHuntEntryPhase.Idle
        treasurePopInScale = 1f
        resetTreasureAppearVisuals()
        pendingWorkflowAlert = null
        clearHiddenTreasures()
        pendingHoardSecretPlace = null
        pendingHoardToActivate = null
        treasureRevision++
        secretRevision++
        clusterRevision++
    }

    fun onClustersDiscovered(count: Int) {
        if (count <= 0) return
        clusterRevision++
    }

    /** Called when the app or map activity shuts down so mode cannot linger. */
    fun shutdown() {
        exitMode()
    }

    fun advanceEntryToRevealing() {
        if (entryPhase == GoldHuntEntryPhase.Zooming) {
            entryPhase = GoldHuntEntryPhase.Revealing
        }
    }

    fun advanceEntryToComplete() {
        entryPhase = GoldHuntEntryPhase.Complete
        treasurePopInScale = 1f
    }

    fun updateTreasurePopInScale(scale: Float) {
        treasurePopInScale = scale
    }

    fun triggerZoomThresholdTreasureReveal() {
        isTreasureDisappearing = false
        treasureDisappearProgress = 0f
        treasureAppearProgress = 0f
        treasurePopInScale = GoldHuntZoomTreasureRevealTransition.START_SCALE
        zoomTreasureRevealNonce++
        markTreasureDirty()
    }

    fun triggerZoomThresholdTreasureDisappear() {
        if (isTreasureDisappearing) return
        isTreasureDisappearing = true
        treasureDisappearProgress = 0f
        zoomTreasureDisappearNonce++
    }

    fun updateTreasureDisappearProgress(progress: Float) {
        treasureDisappearProgress = progress.coerceIn(0f, 1f)
    }

    fun completeTreasureDisappear() {
        isTreasureDisappearing = false
        treasureDisappearProgress = 0f
        resetTreasureAppearVisuals()
        markTreasureDirty()
    }

    fun updateTreasureAppearProgress(progress: Float) {
        val clamped = progress.coerceIn(0f, 1f)
        treasureAppearProgress = clamped
        val eased = GoldHuntEntryTransition.easeOutBack(clamped)
        treasurePopInScale = GoldHuntZoomTreasureRevealTransition.START_SCALE +
            (1f - GoldHuntZoomTreasureRevealTransition.START_SCALE) * eased
    }

    fun resetTreasureAppearVisuals() {
        treasureAppearProgress = 1f
        treasurePopInScale = 1f
        isTreasureDisappearing = false
        treasureDisappearProgress = 0f
    }

    fun hideTreasureForCollect(treasureId: String) {
        if (hiddenTreasureIds.add(treasureId)) {
            hiddenTreasureRevision++
            markTreasureDirty()
        }
    }

    fun revealTreasureAfterFailedCollect(treasureId: String) {
        if (hiddenTreasureIds.remove(treasureId)) {
            hiddenTreasureRevision++
            markTreasureDirty()
        }
    }

    fun isTreasureHiddenFromOverlay(treasureId: String): Boolean =
        treasureId in hiddenTreasureIds || repository.isTreasureCollectedCached(treasureId)

    fun clearHiddenTreasures() {
        if (hiddenTreasureIds.isNotEmpty()) {
            hiddenTreasureIds.clear()
            hiddenTreasureRevision++
        }
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

    fun markClusterDirty() {
        clusterRevision++
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
                evaluateSecretPlaceMilestones(spec)
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
                        fogDirty = false,
                        roadsOverlayDirty = false,
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
        if (result.clusterCompleted) {
            markClusterDirty()
        }
    }

    private suspend fun evaluateSecretPlaceMilestones(spec: SecretPlaceSpec) {
        val secrets = repository.collectedSecretPlaceCount()
        pendingHoardSecretPlace = if (
            secrets <= GoldHuntConfig.HOARD_AT_SECRET_PLACE_MAX_DISCOVERIES
        ) {
            spec
        } else {
            null
        }
        if (secrets == 1 && !GoldHuntPreferences.isFirstSecretAlertShown(appContext)) {
            pendingWorkflowAlert = GoldHuntWorkflowAlert.FirstSecretDiscovered
            GoldHuntPreferences.setFirstSecretAlertShown(appContext)
        } else {
            announceTreasureHoard()
        }
    }

    private fun announceTreasureHoard() {
        val hoard = buildPendingTreasureHoard()
        pendingHoardToActivate = hoard
        GoldHuntPreferences.clearActiveHoard(appContext)
        markTreasureDirty()
        pendingWorkflowAlert = GoldHuntWorkflowAlert.TreasureHoardAnnounced(hoard.regionName)
    }

    private fun buildPendingTreasureHoard(): TreasureHoardRegion {
        val spec = pendingHoardSecretPlace
        pendingHoardSecretPlace = null
        return spec?.let { TreasureHoardCatalog.fromSecretPlace(it) }
            ?: runCatching {
                if (initialized) {
                    TreasureHoardCatalog.rollRegion(appContext, repository.playRegion())
                } else {
                    null
                }
            }.getOrNull()
            ?: TreasureHoardCatalog.rollRegionWithoutPlayRegion(appContext)
    }

    private fun activatePendingTreasureHoard() {
        val hoard = pendingHoardToActivate ?: return
        pendingHoardToActivate = null
        GoldHuntPreferences.setActiveHoard(appContext, hoard)
        markTreasureDirty()
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
            TreasureType.CRYSTAL -> {
                val crystals = repository.collectedTreasureCount(TreasureType.CRYSTAL)
                if (crystals == 1 && !GoldHuntPreferences.isFirstCrystalAlertShown(appContext)) {
                    pendingWorkflowAlert = GoldHuntWorkflowAlert.FirstCrystalCollected
                    GoldHuntPreferences.setFirstCrystalAlertShown(appContext)
                }
            }
            TreasureType.GIFT -> {
                val gifts = repository.collectedTreasureCount(TreasureType.GIFT)
                if (gifts == 1 && !GoldHuntPreferences.isFirstGiftAlertShown(appContext)) {
                    pendingWorkflowAlert = GoldHuntWorkflowAlert.FirstGiftCollected
                    GoldHuntPreferences.setFirstGiftAlertShown(appContext)
                }
                if (gifts >= GoldHuntConfig.SECRET_UNLOCK_MIN_GIFTS) {
                    markSecretDirty()
                }
            }
            else -> Unit
        }
        markTreasureDirty()
    }

    fun repository(): GoldHuntRepository = repository
}
