package com.vanishingjar.cllp.api.googlemaps.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by Niraj Sanghvi on 2019-12-06.
 */
@Parcelize
data class Step(
    @SerializedName("travel_mode") var travelMode: String,
    @SerializedName("distance") var distance: Distance,
    @SerializedName("duration") var duration: Duration,
    @SerializedName("start_location") var startLocation: Location,
    @SerializedName("end_location") var endLocation: Location,
    @SerializedName("html_instructions") var instructions: String,
    @SerializedName("transit_details") var transitDetails: TransitDetails?
) : Parcelable