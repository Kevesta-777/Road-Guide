package com.example.roadguideapp.map

import org.maplibre.android.geometry.LatLng

/**
 * Decodes Valhalla encoded polylines (precision 6), matching Headway web
 * [decodePolyline](https://github.com/headwaymaps/headway/blob/main/services/frontend/www-app/src/utils/decodePolyline.ts).
 */
internal object ValhallaPolylineDecoder {

    fun decode(encoded: String, precision: Int = 6): List<LatLng> {
        if (encoded.isEmpty()) return emptyList()
        val factor = Math.pow(10.0, precision.toDouble())
        var index = 0
        var lat = 0
        var lng = 0
        val coordinates = ArrayList<LatLng>()
        while (index < encoded.length) {
            var shift = 0
            var result = 0
            var byte: Int
            do {
                byte = encoded[index++].code - 63
                result = result or ((byte and 0x1f) shl shift)
                shift += 5
            } while (byte >= 0x20)
            val latitudeChange = if ((result and 1) != 0) (result shr 1).inv() else result shr 1
            shift = 0
            result = 0
            do {
                byte = encoded[index++].code - 63
                result = result or ((byte and 0x1f) shl shift)
                shift += 5
            } while (byte >= 0x20)
            val longitudeChange = if ((result and 1) != 0) (result shr 1).inv() else result shr 1
            lat += latitudeChange
            lng += longitudeChange
            coordinates.add(LatLng(lat / factor, lng / factor))
        }
        return coordinates
    }
}
