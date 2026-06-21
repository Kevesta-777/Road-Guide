package com.example.roadguideapp.goldhunt.panorama.targets

/**
 * All hidden targets for one panorama hunt.
 * Supports single-target hunts today; [targets] may contain multiple entries.
 */
internal data class PanoramaHiddenTargetSet(
    val huntId: String,
    val targets: List<PanoramaHiddenTarget>,
) {
    init {
        require(huntId.isNotBlank()) { "huntId must not be blank" }
        require(targets.isNotEmpty()) { "targets must not be empty" }
        require(targets.all { it.huntId == huntId }) {
            "all targets must belong to the same hunt"
        }
        require(targets.map { it.slotIndex }.distinct().size == targets.size) {
            "slotIndex values must be unique"
        }
    }

    val primary: PanoramaHiddenTarget
        get() = targets.first { it.slotIndex == targets.minOf { t -> t.slotIndex } }

    val animatedTargets: List<PanoramaHiddenTarget>
        get() = targets.filter { it.isAnimated }

    val legendaryTargets: List<PanoramaHiddenTarget>
        get() = targets.filter { it.isLegendary }
}
