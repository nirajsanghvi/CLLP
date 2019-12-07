package com.vanishingjar.cllp

import android.Manifest
import android.app.Notification
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.IBinder
import android.provider.CalendarContract
import android.provider.Telephony
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.telephony.SmsManager
import android.text.TextUtils
import android.text.format.DateUtils
import androidx.preference.PreferenceManager
import com.vanishingjar.cllp.api.googlemaps.GoogleMapsService
import com.vanishingjar.cllp.api.googlemaps.model.MapsResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*


class NotifListener : NotificationListenerService() {
//    init {
//        val blah = "blah"
//    }

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        if (!prefs.getBoolean("enableReadSms", true)) {
            return
        }

        if (sbn !== null && sbn.packageName == Telephony.Sms.getDefaultSmsPackage(this)) {

            val textMsg = sbn.notification.extras.getCharSequence(Notification.EXTRA_TEXT)

            if (!textMsg.isNullOrEmpty()) {
                val cllpRegex = Regex("cllp", RegexOption.IGNORE_CASE)
                val mapsWalkRegex = Regex("(.*)\\s+walkto\\s+(.*)", RegexOption.IGNORE_CASE)
                val mapsBikeRegex = Regex("(.*)\\s+biketo\\s+(.*)", RegexOption.IGNORE_CASE)
                val mapsTransitRegex = Regex("(.*)\\s+transitto\\s+(.*)", RegexOption.IGNORE_CASE)
                val mapsDriveRegex = Regex("(.*)\\s+driveto\\s+(.*)", RegexOption.IGNORE_CASE)
                val yelpRegex = Regex("(.*)\\s+yelpme\\s+(.*)", RegexOption.IGNORE_CASE)
                val calAddRegex = Regex("addtocal\\s+(.*)\\s+on\\s+(.*)\\s+at\\s+(.*)", RegexOption.IGNORE_CASE)
                val calAgendaRegex = Regex("calagenda", RegexOption.IGNORE_CASE)
                val helpRegex = Regex("helpme", RegexOption.IGNORE_CASE)

                when {
                    cllpRegex.matches(textMsg) -> {
                        return
                    }
                    mapsWalkRegex.matches(textMsg) -> {
                        val walkMatches = mapsWalkRegex.matchEntire(textMsg)
                        if (walkMatches?.groups?.size == 3) {
                            val orig = walkMatches.groups[1]?.value.toString()
                            val dest = walkMatches.groups[2]?.value.toString()
                            getDirections(orig, dest, "walking", prefs.getString("googleMapsKey", null))
                            cancelNotification(sbn.key)
                        }
                    }
                    mapsBikeRegex.matches(textMsg) -> {
                        val bikeMatches = mapsBikeRegex.matchEntire(textMsg)
                        if (bikeMatches?.groups?.size == 3) {
                            val orig = bikeMatches.groups[1]?.value.toString()
                            val dest = bikeMatches.groups[2]?.value.toString()
                            getDirections(orig, dest, "bicycling", prefs.getString("googleMapsKey", null))
                            cancelNotification(sbn.key)
                        }
                    }
                    mapsTransitRegex.matches(textMsg) -> {
                        val transitMatches = mapsTransitRegex.matchEntire(textMsg)
                        if (transitMatches?.groups?.size == 3) {
                            val orig = transitMatches.groups[1]?.value.toString()
                            val dest = transitMatches.groups[2]?.value.toString()
                            getDirections(orig, dest, "transit", prefs.getString("googleMapsKey", null))
                            cancelNotification(sbn.key)
                        }
                    }
                    mapsDriveRegex.matches(textMsg) -> {
                        val driveMatches = mapsDriveRegex.matchEntire(textMsg)
                        if (driveMatches?.groups?.size == 3) {
                            val orig = driveMatches.groups[1]?.value.toString()
                            val dest = driveMatches.groups[2]?.value.toString()
                            getDirections(orig, dest, "driving", prefs.getString("googleMapsKey", null))
                            cancelNotification(sbn.key)
                        }
                    }
                    yelpRegex.matches(textMsg) -> {
                        val yelpMatches = yelpRegex.matchEntire(textMsg)
                        if (yelpMatches?.groups?.size == 3) {
                            val orig = yelpMatches.groups[1]?.value.toString()
                            val dest = yelpMatches.groups[2]?.value.toString()
                            cancelNotification(sbn.key)
                        }
                    }
                    calAddRegex.matches(textMsg) -> {
                        val calAddMatches = calAddRegex.matchEntire(textMsg)
                        if (calAddMatches?.groups?.size == 3) {
                            val datetime = calAddMatches.groups[1]?.value
                            val title = calAddMatches.groups[2]?.value
                            cancelNotification(sbn.key)
                        }
                    }
                    calAgendaRegex.matches(textMsg) -> {

                        // Projection array. Creating indices for this array instead of doing dynamic lookups improves performance.
                        val event_projection: Array<String> = arrayOf(
                            CalendarContract.Events.TITLE,               // 0
                            CalendarContract.Events.DTSTART,             // 1
                            CalendarContract.Events.DTEND,               // 2
                            CalendarContract.Events.EVENT_LOCATION       // 3
                        )

                        // The indices for the projection array above.
                        val projection_title_index = 0
                        val projection_dtstart_index = 1
                        val projection_dtend_index = 2
                        val projection_location_index = 3

                        // Run query
                        val uri: Uri = CalendarContract.Events.CONTENT_URI
                        val selection: String = "((${CalendarContract.Events.CALENDAR_ID} IN (" + TextUtils.join(",", prefs.getStringSet("calAgenda", emptySet())!!.asIterable()) + ")) AND " +
                                "((${CalendarContract.Events.DTSTART} > ?) AND " +
                                "(${CalendarContract.Events.DTSTART} < ?)))"
                        val now = System.currentTimeMillis()
                        val numDays = now + DateUtils.DAY_IN_MILLIS * 3
                        val selectionArgs: Array<String> = arrayOf(now.toString(), numDays.toString())
                        if (checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
                            && checkSelfPermission(Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
                            val cur: Cursor? = contentResolver.query(
                                uri,
                                event_projection,
                                selection,
                                selectionArgs,
                                CalendarContract.Events.DTSTART + " ASC"
                            )

                            // Use the cursor to step through the returned records
                            if (cur != null) {
                                val eventLimit = 5
                                var eventCount = 0
                                val dateFormatter = SimpleDateFormat("EEE MMM d, H:mm", Locale.US)
                                val endTimeFormatter = SimpleDateFormat("H:mm", Locale.US)
                                while (cur.moveToNext()) {
                                    // Get the field values
                                    val eventTitle: String = cur.getString(projection_title_index)
                                    val startTime: Long = cur.getLong(projection_dtstart_index)
                                    val endTime: Long = cur.getLong(projection_dtend_index)
                                    val location: String = cur.getString(projection_location_index)
                                    var dateString = dateFormatter.format(Date(startTime))

                                    dateString += if ((endTime - startTime) > DateUtils.DAY_IN_MILLIS) {
                                        " - " + dateFormatter.format(Date(endTime))
                                    } else {
                                        " - " + endTimeFormatter.format(Date(endTime))
                                    }

                                    val eventMessage = eventTitle + "\n" +
                                            dateString + "\n" +
                                            location

                                    sendTextMessage(eventMessage, true)

                                    //Prevent sending too many texts
                                    eventCount += 1

                                    if (eventCount == eventLimit) break
                                }

                                cur.close()
                            }
                        }

                        cancelNotification(sbn.key)
                    }
                    helpRegex.matches(textMsg) -> {
                        val helpText = "CLLP Help:\n\n" +
                                "Map: <origin> walkto|transitto|driveto <destination>\n\n" +
                                "Yelp: <address> yelpme <search>\n\n" +
                                "Get cal events: calagenda\n\n" +
                                "Add cal event: addtocal <title> on <datetime> at <location>"
                        sendTextMessage(helpText)

                        cancelNotification(sbn.key)
                    }
                }
            }
        }
    }

    private fun getDirections(origin: String, destination: String, mode: String, apiKey: String?) {
        apiKey?.let {key ->
            val authInterceptor = Interceptor { chain ->
                val newUrl = chain.request().url()
                    .newBuilder()
                    .addQueryParameter("key", key)
                    .build()

                val newRequest = chain.request()
                    .newBuilder()
                    .url(newUrl)
                    .build()

                chain.proceed(newRequest)
            }

            //OkhttpClient for building http request url
            val googleMapsClient = OkHttpClient().newBuilder()
                .addInterceptor(authInterceptor)
                .build()

            val retrofit = Retrofit.Builder()
                .client(googleMapsClient)
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service: GoogleMapsService = retrofit.create<GoogleMapsService>(GoogleMapsService::class.java)

            val call = service.getDirections(origin, destination, mode)

            call.enqueue(object: Callback<MapsResponse> {
                override fun onResponse(call: Call<MapsResponse>, response: Response<MapsResponse>) {
                    response.body()?.let {

                        if (it.routes.isNotEmpty() && it.routes[0].legs.isNotEmpty() && it.routes[0].legs[0].steps.isNotEmpty()) {
                            val firstResult = it.routes[0].legs[0]
                            var resultMessage = "Dist: " + firstResult.distance.text + ", Time: " + firstResult.duration.text + "\n\n"

                            if (firstResult.steps.size > 40) {
                                resultMessage += "The directions result has too many steps (" + firstResult.steps.size +"). Try breaking up your request into smaller chunks to prevent text overload."
                            } else {
                                for (step in firstResult.steps) {
                                    val instructionNoHtml = step.instructions.replace(Regex("<div.*?>"), ", ").replace(Regex("<.*?>"), "").replace("&nbsp;", " ")
                                    resultMessage += instructionNoHtml + " (" + step.distance.text + ")\n\n"
                                }
                            }

                            sendTextMessage(resultMessage)
                        }
                    }
                }

                override fun onFailure(call: Call<MapsResponse>, t: Throwable) {
                    sendTextMessage("Error: Google Maps API call failed. Double-check your API key and try again.")
                }

            })

        } ?: run {
            sendTextMessage("Unable to fulfill request, the Google Maps API key is not setup in the CLLP app.")
        }
    }

    private fun sendTextMessage(message: String, truncate: Boolean = false) {
        val smsManager = SmsManager.getDefault()
        val phoneNumber = PreferenceManager.getDefaultSharedPreferences(this).getString("phoneNumber", "")
        if (!phoneNumber.isNullOrEmpty()) {
            if (truncate) {
                smsManager.sendTextMessage(phoneNumber, null, message.take(140), null, null)
            } else {
                val dividedMessage = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(phoneNumber, null, dividedMessage, null, null)
            }
        }
    }
}