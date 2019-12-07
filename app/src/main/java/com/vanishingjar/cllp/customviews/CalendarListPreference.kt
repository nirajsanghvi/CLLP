package com.vanishingjar.cllp.customviews

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.util.AttributeSet
import androidx.preference.ListPreference
import java.util.*

class CalendarListPreference(
    context: Context,
    attrs: AttributeSet?
) : ListPreference(context, attrs) {
    //var cr: ContentResolver? = null
    //var cursor: Cursor? = null
    var projection = arrayOf(
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
        CalendarContract.Calendars.IS_PRIMARY,
        CalendarContract.Calendars._ID
    )
    var selection = "(" + CalendarContract.Calendars.VISIBLE + " = ?)"
    var selectionArgs = arrayOf("1")

    init {
        populateList()
    }

    fun populateList() {
        val entries: MutableList<CharSequence> =
            ArrayList()
        val entriesValues: MutableList<CharSequence> =
            ArrayList()
        if (context.checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
            && context.checkSelfPermission(Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
        ) {
            val cr = context.contentResolver
            val cursor = cr.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )
            while (cursor!!.moveToNext()) {
                var name = cursor.getString(0)
                val primary = cursor.getString(1)
                val id = cursor.getString(2)

                if (primary == "1") {
                    entries.add(0, name)
                } else {
                    entries.add(name)
                }
                entriesValues.add(id)
            }

            cursor.close()

            setEntries(entries.toTypedArray())
            entryValues = entriesValues.toTypedArray()
        }
    }
}