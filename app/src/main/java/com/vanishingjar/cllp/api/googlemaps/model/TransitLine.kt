package com.vanishingjar.cllp.api.googlemaps.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by Niraj Sanghvi on 2019-12-06.
 */
@Parcelize
data class TransitLine(
    @SerializedName("color") var color: String?,
    @SerializedName("name") var name: String?,
    @SerializedName("short_name") var shortName: String?,
    @SerializedName("text_color") var textColor: String?,
    @SerializedName("vehicle") var vehicle: TransitVehicle
) : Parcelable {

    val shortNameOrName: String
        get() {
            shortName?.let { return it }
            name?.let { return it }
            return vehicle.name
        }
}