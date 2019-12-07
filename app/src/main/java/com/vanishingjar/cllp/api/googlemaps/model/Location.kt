package com.vanishingjar.cllp.api.googlemaps.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by Niraj Sanghvi on 2019-12-06.
 */
@Parcelize
data class Location(
    @SerializedName("lat") var latitude: Double,
    @SerializedName("lng") var longitude: Double
) : Parcelable