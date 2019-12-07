package com.vanishingjar.cllp.api.googlemaps.model

import android.content.Context
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Created by Niraj Sanghvi on 2019-12-06.
 */
@Parcelize
data class TransitTime(
    @SerializedName("text") var text: String,
    @SerializedName("value") var value: Long
) : Parcelable {

    val withinNextHour: Boolean
        get() = isWithinNextHour(Date(value))

    val minutesUntil: Int
        get() {
            val now = System.currentTimeMillis()
            val timestampMillis = value * 1000 // timestamp is in seconds
            val difference = timestampMillis - now
            val differenceSeconds = (difference / 1000).toInt()
            return differenceSeconds / 60
        }

    fun getFormattedTimeUntil(context: Context): String {
        val minutes = minutesUntil
        return if (minutes < 60) {
            minutes.toString()
        } else {
            val hours = (minutes / 60).toString()
            val mins = String.format("%02d", minutes % 60)
            String.format("%s:%s", hours, mins)
        }
    }

    private fun isWithinNextHour(date: Date): Boolean {
        val hourMillis = 60 * 60 * 1000
        val timestamp = date.time * 1000 // time is in seconds, transform it to milliseconds
        val now = System.currentTimeMillis()
        return timestamp - now <= hourMillis
    }
}