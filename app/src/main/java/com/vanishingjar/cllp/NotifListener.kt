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
import android.text.format.DateUtils
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

        if (sbn !== null && sbn.packageName == Telephony.Sms.getDefaultSmsPackage(this)) {

//            if (sbn.notification.flags.or(Notification.FLAG_GROUP_SUMMARY) != 0) {
//                //Ignore group summary notifications
//                return
//            }

            //if (sbn.notification.extras.getCharSequence(Notification.EXTRA_TITLE) == "Voicemail") {

            val textMsg = sbn.notification.extras.getCharSequence(Notification.EXTRA_TEXT)

            if (!textMsg.isNullOrEmpty()) {
                val cllpRegex = Regex("cllp", RegexOption.IGNORE_CASE)
                val mapsWalkRegex = Regex("(.*)\\s+walkto\\s+(.*)", RegexOption.IGNORE_CASE)
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
                            val orig = walkMatches.groups[1]?.value
                            val dest = walkMatches.groups[2]?.value
                            cancelNotification(sbn.key)
                        }
                    }
                    mapsTransitRegex.matches(textMsg) -> {
                        val transitMatches = mapsTransitRegex.matchEntire(textMsg)
                        if (transitMatches?.groups?.size == 3) {
                            val orig = transitMatches.groups[1]?.value
                            val dest = transitMatches.groups[2]?.value
                            cancelNotification(sbn.key)
                        }
                    }
                    mapsDriveRegex.matches(textMsg) -> {
                        val driveMatches = mapsDriveRegex.matchEntire(textMsg)
                        if (driveMatches?.groups?.size == 3) {
                            val orig = driveMatches.groups[1]?.value
                            val dest = driveMatches.groups[2]?.value
                            cancelNotification(sbn.key)
                        }
                    }
                    yelpRegex.matches(textMsg) -> {
                        val yelpMatches = yelpRegex.matchEntire(textMsg)
                        if (yelpMatches?.groups?.size == 3) {
                            val orig = yelpMatches.groups[1]?.value
                            val dest = yelpMatches.groups[2]?.value
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
                        //Use the commented section to determine IDs for calendars you're interested in

//                        // Projection array. Creating indices for this array instead of doing dynamic lookups improves performance.
//                        val EVENT_PROJECTION: Array<String> = arrayOf(
//                            CalendarContract.Calendars._ID,                     // 0
//                            CalendarContract.Calendars.ACCOUNT_NAME,            // 1
//                            CalendarContract.Calendars.ACCOUNT_TYPE,            // 2
//                            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,   // 3
//                            CalendarContract.Calendars.OWNER_ACCOUNT            // 4
//                        )
//
//                        // The indices for the projection array above.
//                        val PROJECTION_ID_INDEX: Int = 0
//                        val PROJECTION_ACCOUNT_NAME_INDEX: Int = 1
//                        val PROJECTION_ACCOUNT_TYPE_INDEX: Int = 2
//                        val PROJECTION_DISPLAY_NAME_INDEX: Int = 3
//                        val PROJECTION_OWNER_ACCOUNT_INDEX: Int = 4
//
//                        // Run query
//                        val uri: Uri = CalendarContract.Calendars.CONTENT_URI
//                        val selection: String = "((${CalendarContract.Calendars.ACCOUNT_NAME} = ?) AND (" +
//                                "${CalendarContract.Calendars.ACCOUNT_TYPE} = ?) AND (" +
//                                "${CalendarContract.Calendars.OWNER_ACCOUNT} = ?) AND (" +
//                                "${CalendarContract.Calendars.VISIBLE} = 1))"
//                        val selectionArgs: Array<String> = arrayOf("email@gmail.com", "com.google", "email@gmail.com")
//                        if (checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
//                            && checkSelfPermission(Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
//                            val cur: Cursor? = contentResolver.query(
//                                uri,
//                                EVENT_PROJECTION,
//                                null,
//                                null,
//                                null
//                            )
//
//                            // Use the cursor to step through the returned records
//                            if (cur != null) {
//                                while (cur.moveToNext()) {
//                                    // Get the field values
//                                    val calID: Long = cur.getLong(PROJECTION_ID_INDEX)
//                                    val displayName: String =
//                                        cur.getString(PROJECTION_DISPLAY_NAME_INDEX)
//                                    val accountName: String =
//                                        cur.getString(PROJECTION_ACCOUNT_NAME_INDEX)
//                                    val accountType: String =
//                                        cur.getString(PROJECTION_ACCOUNT_TYPE_INDEX)
//                                    val ownerName: String =
//                                        cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX)
//                                    // Do something with the values...
//                                }
//
//                                cur.close()
//                            }
//                        }

                        // Projection array. Creating indices for this array instead of doing dynamic lookups improves performance.
                        val EVENT_PROJECTION: Array<String> = arrayOf(
                            CalendarContract.Events.TITLE,               // 0
                            CalendarContract.Events.DTSTART,             // 1
                            CalendarContract.Events.DTEND,               // 2
                            CalendarContract.Events.EVENT_LOCATION       // 3
                        )

                        // The indices for the projection array above.
                        val PROJECTION_TITLE_INDEX: Int = 0
                        val PROJECTION_DTSTART_INDEX: Int = 1
                        val PROJECTION_DTEND_INDEX: Int = 2
                        val PROJECTION_LOCATION_INDEX: Int = 3

                        // Run query
                        val uri: Uri = CalendarContract.Events.CONTENT_URI
                        val selection: String = "(((${CalendarContract.Events.CALENDAR_ID} = ?) OR " +
                                "(${CalendarContract.Events.CALENDAR_ID} = ?)) AND " +
                                "((${CalendarContract.Events.DTSTART} > ?) AND " +
                                "(${CalendarContract.Events.DTSTART} < ?)))"
                        val now = System.currentTimeMillis()
                        val numDays = now + DateUtils.DAY_IN_MILLIS * 3
                        val selectionArgs: Array<String> = arrayOf("4", "23", now.toString(), numDays.toString())
                        if (checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
                            && checkSelfPermission(Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
                            val cur: Cursor? = contentResolver.query(
                                uri,
                                EVENT_PROJECTION,
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
                                    val eventTitle: String = cur.getString(PROJECTION_TITLE_INDEX)
                                    val startTime: Long = cur.getLong(PROJECTION_DTSTART_INDEX)
                                    val endTime: Long = cur.getLong(PROJECTION_DTEND_INDEX)
                                    val location: String = cur.getString(PROJECTION_LOCATION_INDEX)
                                    var dateString = dateFormatter.format(Date(startTime))

                                    if ((endTime - startTime) > DateUtils.DAY_IN_MILLIS) {
                                        dateString += " - " + dateFormatter.format(Date(endTime))
                                    } else {
                                        dateString += " - " + endTimeFormatter.format(Date(endTime))
                                    }

                                    SmsManager.getDefault().sendTextMessage("+12177662670", null,
                                        eventTitle + "\n" +
                                                dateString + "\n" +
                                                location,
                                        null, null)

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
                        SmsManager.getDefault().sendTextMessage("+12177662670", null,
                            "CLLP Help (1/2):\n" +
                                    "Map: <origin> walkto|transitto|driveto <destination>\n" +
                                    "Yelp: <address> yelpme <search>",
                            null, null)
                        SmsManager.getDefault().sendTextMessage("+12177662670", null,
                            "CLLP Help (2/2):\n" +
                                    "Get cal events: calagenda\n" +
                                    "Add event: addtocal <title> on <datetime> at <location>",
                            null, null)
                        cancelNotification(sbn.key)
                    }
                }
            }
            //}
        }
    }
}