package com.example.roadguideapp.map

import org.json.JSONArray
import org.json.JSONObject

internal data class PlaceReview(
    val authorName: String,
    val rating: Int,
    val text: String,
    val createdAt: String = "",
)

internal data class BusinessPoiMetadata(
    val phone: String = "",
    val website: String = "",
    val hours: String = "",
    val city: String = "",
    val state: String = "",
    val averageRating: Double? = null,
    val reviews: List<PlaceReview> = emptyList(),
) {
    fun toJsonObject(): JSONObject {
        val reviewsJson = JSONArray()
        reviews.forEach { review ->
            reviewsJson.put(
                JSONObject()
                    .put("authorName", review.authorName)
                    .put("rating", review.rating)
                    .put("text", review.text)
                    .put("createdAt", review.createdAt),
            )
        }
        return JSONObject()
            .put("phone", phone)
            .put("website", website)
            .put("hours", hours)
            .put("city", city)
            .put("state", state)
            .also { json ->
                averageRating?.let { json.put("averageRating", it) }
            }
            .put("reviews", reviewsJson)
    }

    companion object {
        fun fromJsonObject(json: JSONObject?): BusinessPoiMetadata {
            if (json == null) return BusinessPoiMetadata()
            val reviewsJson = json.optJSONArray("reviews") ?: JSONArray()
            val reviews = buildList {
                for (index in 0 until reviewsJson.length()) {
                    val item = reviewsJson.optJSONObject(index) ?: continue
                    add(
                        PlaceReview(
                            authorName = item.optString("authorName"),
                            rating = item.optInt("rating", 0).coerceIn(0, 5),
                            text = item.optString("text"),
                            createdAt = item.optString("createdAt"),
                        ),
                    )
                }
            }
            val averageRating = json.optDouble("averageRating").takeIf {
                json.has("averageRating") && !json.isNull("averageRating")
            }
            return BusinessPoiMetadata(
                phone = json.optString("phone"),
                website = json.optString("website"),
                hours = json.optString("hours"),
                city = json.optString("city"),
                state = json.optString("state"),
                averageRating = averageRating,
                reviews = reviews,
            )
        }

        fun averageFromReviews(reviews: List<PlaceReview>): Double? {
            if (reviews.isEmpty()) return null
            return reviews.map { it.rating.toDouble() }.average()
        }
    }
}

internal data class PlaceBusinessPhoto(
    val id: String,
    val url: String,
    val caption: String,
)

internal data class PlaceBusinessDetail(
    val description: String,
    val photos: List<PlaceBusinessPhoto>,
    val metadata: BusinessPoiMetadata,
    val name: String,
    val address: String,
) {
    val hasContent: Boolean
        get() = description.isNotBlank() ||
            photos.isNotEmpty() ||
            metadata.phone.isNotBlank() ||
            metadata.website.isNotBlank() ||
            metadata.hours.isNotBlank() ||
            metadata.city.isNotBlank() ||
            metadata.state.isNotBlank()
}
