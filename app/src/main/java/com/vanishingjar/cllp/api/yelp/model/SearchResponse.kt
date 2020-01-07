package com.vanishingjar.cllp.api.yelp.model

data class SearchResponse(
    val total: Int,
    val businesses: List<Business>
)