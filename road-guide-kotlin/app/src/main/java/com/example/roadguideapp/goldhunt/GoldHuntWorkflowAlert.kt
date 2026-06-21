package com.example.roadguideapp.goldhunt

internal sealed class GoldHuntWorkflowAlert {
    data object Intro : GoldHuntWorkflowAlert()
    data object FirstStarCollected : GoldHuntWorkflowAlert()
    data object FirstFlowerCollected : GoldHuntWorkflowAlert()
    data object FirstCrystalCollected : GoldHuntWorkflowAlert()
    data object FirstGiftCollected : GoldHuntWorkflowAlert()
    data object FirstSecretDiscovered : GoldHuntWorkflowAlert()
    data class TreasureHoardAnnounced(val regionName: String) : GoldHuntWorkflowAlert()
}
