package com.vanishingjar.cllp.api.yelp.model

import com.google.gson.annotations.SerializedName

data class Business (
    val rating: Float?,
    val price: String?,
    val phone: String?,
    val id: String?,
    val alias: String?,
    @SerializedName("review_count") val reviewCount: Int,
    @SerializedName("is_closed") val isClosed: Boolean,
    val name: String?,
    val location: Location,
    val distance: Float?
)