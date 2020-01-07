package com.vanishingjar.cllp.api.yelp.model

import com.google.gson.annotations.SerializedName

data class Location (
    val address1: String?,
    val address2: String?,
    val address3: String?,
    val city: String?,
    val state: String?,
    @SerializedName("zip_code") val zip: String?
) {
    val fullAddress: String
        get() {
            val fullAddress = address1 +
                    if (address2.isNullOrEmpty()) {""} else {", $address2"} +
                    if (address3.isNullOrEmpty()) {""} else {", $address3"} +
                    ", $city"

            return if (fullAddress.isEmpty()) "N/A" else fullAddress
        }
}