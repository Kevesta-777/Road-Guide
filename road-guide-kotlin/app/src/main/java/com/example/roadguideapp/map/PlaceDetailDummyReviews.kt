package com.example.roadguideapp.map

/** Placeholder ratings shown in the place detail sheet until a real review pipeline exists. */
internal object PlaceDetailDummyReviews {
    const val averageRating = 4.3

    val reviews: List<PlaceReview> = listOf(
        PlaceReview(
            authorName = "Alex M.",
            rating = 5,
            text = "Great spot with friendly staff and a clean atmosphere.",
        ),
        PlaceReview(
            authorName = "Jordan K.",
            rating = 4,
            text = "Solid experience overall. Would visit again.",
        ),
        PlaceReview(
            authorName = "Sam R.",
            rating = 5,
            text = "Exactly what I was looking for in this area.",
        ),
    )
}
