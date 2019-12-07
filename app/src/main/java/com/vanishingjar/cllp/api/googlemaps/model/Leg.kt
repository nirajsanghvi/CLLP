package com.vanishingjar.cllp.api.googlemaps.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by Niraj Sanghvi on 2019-12-06.
 */
@Parcelize
data class Leg(
    @SerializedName("arrival_time") var arrivalTime: TransitTime,
    @SerializedName("departure_time") var departureTime: TransitTime,
    @SerializedName("distance") var distance: Distance,
    @SerializedName("duration") var duration: Duration,
    @SerializedName("start_address") var startAddress: String,
    @SerializedName("start_location") var startLocation: Location,
    @SerializedName("end_address") var endAddress: String,
    @SerializedName("end_location") var endLocation: Location,
    @SerializedName("steps") var steps: ArrayList<Step>
) : Parcelable