package com.vanishingjar.cllp

import android.Manifest
import android.app.Notification
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.CalendarContract
import android.provider.Telephony
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.telephony.SmsManager
import android.text.TextUtils
import android.text.format.DateUtils
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.joestelmach.natty.Parser
import com.vanishingjar.cllp.api.googlemaps.GoogleMapsService
import com.vanishingjar.cllp.api.googlemaps.model.MapsResponse
import com.vanishingjar.cllp.api.weather.WeatherService
import com.vanishingjar.cllp.api.weather.model.WeatherResponse
import com.vanishingjar.cllp.api.wikipedia.WikiService
import com.vanishingjar.cllp.api.wikipedia.model.WikiSearchResponse
import com.vanishingjar.cllp.api.wikipedia.model.WikiSummaryResponse
import com.vanishingjar.cllp.api.yelp.YelpService
import com.vanishingjar.cllp.api.yelp.model.SearchResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.min


class NotifListener : NotificationListenerService() {
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
                if (prefs.getString("lastCommand", "")!!.contentEquals(textMsg) && (System.currentTimeMillis() - prefs.getLong("lastCommandExecutionTime", 0)) < DateUtils.SECOND_IN_MILLIS * 10) {
                    //Avoid processing duplicate notification triggers and spamming...ignore the same command coming in within a 10 second cool-off period
                    return
                }

                val cllpRegex = Regex("cllp", RegexOption.IGNORE_CASE)
                val mapsWalkRegex = Regex("(.*)\\s+walkto\\s+(.*)", RegexOption.IGNORE_CASE)
                val mapsBikeRegex = Regex("(.*)\\s+biketo\\s+(.*)", RegexOption.IGNORE_CASE)
                val mapsTransitRegex = Regex("(.*)\\s+transitto\\s+(.*)", RegexOption.IGNORE_CASE)
                val mapsDriveRegex = Regex("(.*)\\s+driveto\\s+(.*)", RegexOption.IGNORE_CASE)
                val yelpRegex = Regex("(.*)\\s+yelpme\\s+(.*)", RegexOption.IGNORE_CASE)
                val calAddRegex = Regex("addtocal\\s+(.*)\\s+on\\s+(.*)\\s+at\\s+(.*)", RegexOption.IGNORE_CASE)
                val calAgendaRegex = Regex("calagenda", RegexOption.IGNORE_CASE)
                val wikiRegex = Regex("wiki\\s+(.*)", RegexOption.IGNORE_CASE)
                val weatherRegex = Regex("weather\\s+(.*)", RegexOption.IGNORE_CASE)
                val helpRegex = Regex("helpme", RegexOption.IGNORE_CASE)

                when {
                    cllpRegex.matches(textMsg) -> {
                        //Ignore messages generated by this app
                        cancelNotification(sbn.key)
                        return
                    }
                    mapsWalkRegex.matches(textMsg) -> {
                        logCommandUsage("mapsWalking", textMsg.toString(), prefs)
                        val walkMatches = mapsWalkRegex.matchEntire(textMsg)
                        if (walkMatches?.groups?.size == 3) {
                            val orig = walkMatches.groups[1]?.value.toString()
                            val dest = walkMatches.groups[2]?.value.toString()
                            getDirections(orig, dest, "walking", prefs.getString("googleMapsKey", null))
                            cancelNotification(sbn.key)
                        }
                    }
                    mapsBikeRegex.matches(textMsg) -> {
                        logCommandUsage("mapsBiking", textMsg.toString(), prefs)
                        val bikeMatches = mapsBikeRegex.matchEntire(textMsg)
                        if (bikeMatches?.groups?.size == 3) {
                            val orig = bikeMatches.groups[1]?.value.toString()
                            val dest = bikeMatches.groups[2]?.value.toString()
                            getDirections(orig, dest, "bicycling", prefs.getString("googleMapsKey", null))
                            cancelNotification(sbn.key)
                        }
                    }
                    mapsTransitRegex.matches(textMsg) -> {
                        logCommandUsage("mapsTransit", textMsg.toString(), prefs)
                        val transitMatches = mapsTransitRegex.matchEntire(textMsg)
                        if (transitMatches?.groups?.size == 3) {
                            val orig = transitMatches.groups[1]?.value.toString()
                            val dest = transitMatches.groups[2]?.value.toString()
                            getDirections(orig, dest, "transit", prefs.getString("googleMapsKey", null))
                            cancelNotification(sbn.key)
                        }
                    }
                    mapsDriveRegex.matches(textMsg) -> {
                        logCommandUsage("mapsDriving", textMsg.toString(), prefs)
                        val driveMatches = mapsDriveRegex.matchEntire(textMsg)
                        if (driveMatches?.groups?.size == 3) {
                            val orig = driveMatches.groups[1]?.value.toString()
                            val dest = driveMatches.groups[2]?.value.toString()
                            getDirections(orig, dest, "driving", prefs.getString("googleMapsKey", null))
                            cancelNotification(sbn.key)
                        }
                    }
                    yelpRegex.matches(textMsg) -> {
                        logCommandUsage("yelpSearch", textMsg.toString(), prefs)
                        val yelpMatches = yelpRegex.matchEntire(textMsg)
                        if (yelpMatches?.groups?.size == 3) {
                            val orig = yelpMatches.groups[1]?.value.toString()
                            val searchTerm = yelpMatches.groups[2]?.value.toString()
                            getYelpSearchResults(orig, searchTerm, prefs.getString("yelpKey", null))
                            cancelNotification(sbn.key)
                        }
                    }
                    calAddRegex.matches(textMsg) -> {
                        logCommandUsage("calAgenda", textMsg.toString(), prefs)
                        val calAddMatches = calAddRegex.matchEntire(textMsg)
                        if (calAddMatches?.groups?.size == 4) {
                            val title = calAddMatches.groups[1]?.value
                            val datetime = calAddMatches.groups[2]?.value
                            val location = calAddMatches.groups[3]?.value
                            val timezoneName = prefs.getString("calTimeZone", "America/Los_Angeles")
                            val timezone = TimeZone.getTimeZone(timezoneName)
                            val calID = prefs.getString("calAdd", "")

                            if (!calID.isNullOrEmpty()) {
                                val parser = Parser(timezone)
                                val dateGroups = parser.parse(datetime)
                                if (dateGroups.isEmpty() || dateGroups[0].dates.isEmpty()) {
                                    sendTextMessage("CLLP Error: Could not understand the date you provided for the event.")
                                } else {
                                    var parsedDate = dateGroups[0].dates[0]

                                    //Make sure the date is in the future
                                    if (parsedDate.time < System.currentTimeMillis()) {
                                        val cal = Calendar.getInstance(timezone)
                                        cal.time = parsedDate
                                        cal.add(Calendar.YEAR, 1)
                                        parsedDate = cal.time
                                    }

                                    val values = ContentValues().apply {
                                        put(CalendarContract.Events.DTSTART, parsedDate.time)
                                        put(CalendarContract.Events.DTEND, parsedDate.time + DateUtils.HOUR_IN_MILLIS)
                                        put(CalendarContract.Events.TITLE, title)
                                        put(CalendarContract.Events.DESCRIPTION, "")
                                        put(CalendarContract.Events.CALENDAR_ID, calID)
                                        put(CalendarContract.Events.EVENT_TIMEZONE, timezoneName)
                                        put(CalendarContract.Events.EVENT_LOCATION, location)
                                    }

                                    contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)

                                    //val uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
                                    //val eventID = uri?.lastPathSegment?.toLong()

                                    sendTextMessage("CLLP Result: The event has been added to your calendar.")
                                }
                            } else {
                                sendTextMessage("CLLP Error: You need to define your default calendar in the CLLP app first.")
                            }

                            cancelNotification(sbn.key)
                        }
                    }
                    calAgendaRegex.matches(textMsg) -> {
                        logCommandUsage("calAdd", textMsg.toString(), prefs)

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
                                var resultMessage = "CLLP Agenda:\n\n"
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

                                    resultMessage += eventTitle + "\n" +
                                            dateString + "\n" +
                                            location + "\n\n"

                                    //Prevent sending too many texts
                                    eventCount += 1

                                    if (eventCount == eventLimit) break
                                }

                                sendTextMessage(resultMessage)

                                cur.close()
                            }
                        } else {
                            sendTextMessage("CLLP Error: You need to first enable calendar permissions and select your calendar(s) in the CLLP app.")
                        }

                        cancelNotification(sbn.key)
                    }
                    wikiRegex.matches(textMsg) -> {
                        logCommandUsage("wiki", textMsg.toString(), prefs)
                        val wikiMatches = wikiRegex.matchEntire(textMsg)
                        if (wikiMatches?.groups?.size == 2) {
                            val searchTerm = wikiMatches.groups[1]?.value.toString()
                            getWikiResults(searchTerm)
                            cancelNotification(sbn.key)
                        }
                    }
                    weatherRegex.matches(textMsg) -> {
                        logCommandUsage("weather", textMsg.toString(), prefs)
                        val weatherMatches = weatherRegex.matchEntire(textMsg)
                        if (weatherMatches?.groups?.size == 2) {
                            val location = weatherMatches.groups[1]?.value.toString()
                            getWeatherResults(location)
                            cancelNotification(sbn.key)
                        }
                    }
                    helpRegex.matches(textMsg) -> {
                        logCommandUsage("helpme", textMsg.toString(), prefs)

                        val helpText = "CLLP Help:\n\n" +
                                "Map: <origin> walkto|transitto|driveto|biketo <destination>\n\n" +
                                "Yelp: <current location> yelpme <search category or business name>\n\n" +
                                "Get cal events: calagenda\n\n" +
                                "Add cal event: addtocal <title> on <datetime> at <location>" +
                                "Search wiki: wiki <search term>"
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
                val newUrl = chain.request().url
                    .newBuilder()
                    .addQueryParameter("key", key)
                    .build()

                val newRequest = chain.request()
                    .newBuilder()
                    .url(newUrl)
                    .build()

                chain.proceed(newRequest)
            }

            //OkHttpClient for building http request url with API key query param
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
                            var resultMessage = "CLLP Results - Dist: " + firstResult.distance.text + ", Time: " + firstResult.duration.text + ", Steps: " + firstResult.steps.size + "\n\n"

                            if (firstResult.steps.size > 50) {
                                resultMessage += "The directions result has too many steps (" + firstResult.steps.size +"). Try breaking up your request into smaller chunks to prevent text overload."
                            } else {
                                for (step in firstResult.steps) {
                                    val transitPrefix = if (step.travelMode.toLowerCase() == "transit") "[" + step.transitDetails?.line?.completeName + "] " else ""
                                    val transitSuffix = if (step.travelMode.toLowerCase() == "transit") " from " + step.transitDetails?.departureStop?.name + ". Ride for " + step.transitDetails?.numStops + " stop(s) to " + step.transitDetails?.arrivalStop?.name else ""

                                    val instructionNoHtml = step.instructions.replace(Regex("<div.*?>"), ", ").replace(Regex("<.*?>"), "").replace("&nbsp;", " ")

                                    resultMessage += transitPrefix + instructionNoHtml + transitSuffix + " (" + step.distance.text + ")\n\n"
                                }
                            }

                            sendTextMessage(resultMessage)
                        }
                    }
                }

                override fun onFailure(call: Call<MapsResponse>, t: Throwable) {
                    sendTextMessage("CLLP Error: Google Maps API call failed. Double-check your API key and try again.")
                }
            })

        } ?: run {
            sendTextMessage("CLLP Error: Unable to fulfill request, the Google Maps API key is not setup in the CLLP app.")
        }
    }

    private fun getYelpSearchResults(origin: String, searchTerm: String, apiKey: String?) {
        apiKey?.let {key ->
            val authInterceptor = Interceptor { chain ->
                val newRequest = chain.request()
                    .newBuilder()
                    .addHeader("Authorization", "Bearer $key")
                    .build()

                chain.proceed(newRequest)
            }

            //OkHttpClient for building http request url with auth header
            val yelpClient = OkHttpClient().newBuilder()
                .addInterceptor(authInterceptor)
                .build()

            val retrofit = Retrofit.Builder()
                .client(yelpClient)
                .baseUrl("https://api.yelp.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service: YelpService = retrofit.create<YelpService>(YelpService::class.java)

            val call = service.getSearchResults(origin, searchTerm)

            call.enqueue(object: Callback<SearchResponse> {
                override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                    response.body()?.let {
                        if (it.businesses.isNotEmpty()) {
                            var resultMessage = "CLLP Yelp Results: \n\n"

                            it.businesses.take(5).forEach {bus ->
                                val stars = if (bus.rating != null) "★".repeat(bus.rating.toInt()) else "N/A"
                                val mileDistance = if (bus.distance != null) String.format("%.1f", bus.distance * 0.000621371192) + "mi" else "N/A"
                                resultMessage += bus.name +
                                        " (${bus.price}, $stars, ${bus.reviewCount} reviews)\n" +
                                        "${bus.location.fullAddress}\n" +
                                        "($mileDistance)\n" +
                                        "${bus.phone}\n\n"
                            }

                            sendTextMessage(resultMessage)
                        }
                    }
                }

                override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                    sendTextMessage("CLLP Error: Yelp API call failed. Double-check your API key and try again.")
                }
            })

        } ?: run {
            sendTextMessage("CLLP Error: Unable to fulfill request, the Yelp API key is not setup in the CLLP app.")
        }
    }

    private fun getWeatherResults(location: String) {
        val geocoder = Geocoder(this)
        val addresses = geocoder.getFromLocationName(location.trim(), 5)

        var lat = ""
        var lon = ""

        for (address in addresses) {
            if (address.hasLatitude() && address.hasLongitude()) {
                lat = address.latitude.toString()
                lon = address.longitude.toString()
                break
            }
        }

        if (lat.isEmpty() || lon.isEmpty()) {
            return
        }

        val retrofit = Retrofit.Builder()
            .client(OkHttpClient())
            .baseUrl("https://api.darksky.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service: WeatherService = retrofit.create<WeatherService>(WeatherService::class.java)

        // Note: If you forked the CLLP repo, you need to provide your own Dark Sky API key below
        val call = service.getWeatherResults(getString(R.string.dark_sky_api_key), lat, lon)

        call.enqueue(object: Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                response.body()?.let {
                    val feelsLikeTemp = if (it.currently?.temperature != null && it.currently.apparentTemperature != null &&
                        (abs(it.currently.temperature - it.currently.apparentTemperature) > 5.0)) "(feels like ${it.currently.apparentTemperature.toInt()})" else ""
                    var resultMessage = "CLLP Results: Currently ${it.currently?.summary ?: "<unknown>"} and ${it.currently?.temperature?.toInt() ?: "N/A"} degrees $feelsLikeTemp\n" +
                            "Next hour: ${it.minutely?.summary ?: "N/A"}\n24 hrs: ${it.hourly?.summary ?: "N/A"}\nThis week: ${it.daily?.summary ?: "N/A"}\n\n"

                    val timezone = TimeZone.getTimeZone(it.timezone)
                    val cal = Calendar.getInstance(timezone)

                    it.daily?.data?.let {days ->
                        for (day in days.take(3)) {
                            cal.clear()
                            cal.timeInMillis = day.time * 1000
                            val dayLabel = cal.getDisplayName(
                                Calendar.DAY_OF_WEEK,
                                Calendar.SHORT,
                                Locale.getDefault()
                            )

                            val wind =
                                if (day.windSpeed != null && day.windSpeed > 35.0) "wind speed: ${day.windSpeed}" else ""

                            resultMessage += "$dayLabel: ${day.weatherIcon} ${day.temperatureHigh?.toInt()} | ${day.temperatureLow?.toInt()} $wind\n"
                        }
                    }

                    sendTextMessage(resultMessage)
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                sendTextMessage("CLLP Error: Dark Sky API call failed.")
            }
        })
    }

    private fun getWikiResults(searchTerm: String) {
        val retrofit = Retrofit.Builder()
            .client(OkHttpClient())
            .baseUrl("https://en.wikipedia.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service: WikiService = retrofit.create<WikiService>(WikiService::class.java)

        val call = service.getSearchResults(searchTerm)

        call.enqueue(object: Callback<WikiSearchResponse> {
            override fun onResponse(call: Call<WikiSearchResponse>, response: Response<WikiSearchResponse>) {
                response.body()?.let {
                    if (it.query.search.isNotEmpty()) {
                        var additionalResults = ""
                        if (it.query.search.size > 1) {
                            additionalResults = " Other possible search results: " + it.query.search.subList(1, min(5, it.query.search.size - 1)).joinToString {searchResults -> "\"${searchResults.title}\""}
                        }

                        if (!it.query.searchinfo.suggestion.isNullOrEmpty()) {
                            additionalResults += " Did you mean \"${it.query.searchinfo.suggestion}\"?"
                        }

                        sendTextMessage("CLLP Results: Getting wiki summary for closest result \"${it.query.search[0].title}\".$additionalResults")
                        getWikiSummary(it.query.search[0].title, service)
                    } else if (!it.query.searchinfo.suggestion.isNullOrEmpty()) {
                        sendTextMessage("CLLP Warning: No exact match, so getting results for \"${it.query.searchinfo.suggestion}\" instead")
                        getWikiResults(it.query.searchinfo.suggestion)
                    } else {
                        sendTextMessage("CLLP Error: Couldn't find any articles or suggestions for that search term.")
                    }
                }
            }

            override fun onFailure(call: Call<WikiSearchResponse>, t: Throwable) {
                sendTextMessage("CLLP Error: Unable to search Wikipedia. Check your query and try again later.")
            }
        })
    }

    private fun getWikiSummary(title: String, service: WikiService) {
        val call = service.getSummary(title)

        call.enqueue(object: Callback<WikiSummaryResponse> {
            override fun onResponse(call: Call<WikiSummaryResponse>, response: Response<WikiSummaryResponse>) {
                response.body()?.let {
                    if (it.type != "standard") {
                        sendTextMessage("Unexpected result, no summary found for $title.")
                    } else {
                        sendTextMessage(it.extract ?: "No summary found for $title.")
                    }
                }
            }

            override fun onFailure(call: Call<WikiSummaryResponse>, t: Throwable) {
                sendTextMessage("CLLP Error: Could not get the Wikipedia summary for $title")
            }
        })
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

    private fun logCommandUsage(commandName: String, textMsgString: String, prefs: SharedPreferences) {
        prefs.edit(commit = true) {
            putString("lastCommand", textMsgString)
            putLong("lastCommandExecutionTime", System.currentTimeMillis())
        }

        UpdateChecker.checkForNewVersion(this, prefs)

        val analytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, commandName)
        analytics.logEvent("ranCommand", bundle)
    }
}