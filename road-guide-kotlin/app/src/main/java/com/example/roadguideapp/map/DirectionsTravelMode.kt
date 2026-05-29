package com.example.roadguideapp.map

/** Travel costing passed to Headway Valhalla `/route`. */
internal enum class DirectionsTravelMode(val valhallaCosting: String) {
    Drive("auto"),
    Walk("pedestrian"),
    Bicycle("bicycle"),
    ;

    companion object {
        fun fromChipIndex(index: Int): DirectionsTravelMode = when (index) {
            1 -> Walk
            2 -> Bicycle
            else -> Drive
        }

        fun chipIndex(mode: DirectionsTravelMode): Int = when (mode) {
            Drive -> 0
            Walk -> 1
            Bicycle -> 2
        }
    }
}
