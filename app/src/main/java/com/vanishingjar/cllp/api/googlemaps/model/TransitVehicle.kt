package com.vanishingjar.cllp.api.googlemaps.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by Niraj Sanghvi on 2019-12-06.
 */
@Parcelize
data class TransitVehicle(
    @SerializedName("name") val name: String
) : Parcelable