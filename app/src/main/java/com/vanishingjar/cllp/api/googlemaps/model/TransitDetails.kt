package com.vanishingjar.cllp.api.googlemaps.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by Niraj Sanghvi on 2019-12-06.
 */
@Parcelize
data class TransitDetails(
    @SerializedName("arrival_stop") var arrivalStop: TransitStop,
    @SerializedName("arrival_time") var arrivalTime: TransitTime,
    @SerializedName("departure_stop") var departureStop: TransitStop,
    @SerializedName("departure_time") var departureTime: TransitTime,
    @SerializedName("headsign") var headSign: String,
    @SerializedName("line") var line: TransitLine
) : Parcelable