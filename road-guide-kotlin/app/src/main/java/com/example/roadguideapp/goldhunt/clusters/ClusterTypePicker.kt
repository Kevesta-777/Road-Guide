package com.example.roadguideapp.goldhunt.clusters

import com.example.roadguideapp.goldhunt.treasure.TreasureHash

/** Picks a [ClusterType] using configured [ClusterType.rarityWeight] values. */
internal object ClusterTypePicker {
    fun pick(seed: String): ClusterType {
        val roll = TreasureHash.unitFraction("$seed:type")
        val total = ClusterCatalog.totalRarityWeight().toDouble()
        if (total <= 0.0) return ClusterType.ROADSIDE_CACHE
        var cumulative = 0.0
        for (type in ClusterCatalog.displayOrder) {
            cumulative += type.rarityWeight / total
            if (roll < cumulative) return type
        }
        return ClusterCatalog.displayOrder.last()
    }
}
