package com.vanishingjar.cllp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.telephony.SmsManager
import androidx.appcompat.app.AlertDialog
import androidx.preference.*
import com.vanishingjar.cllp.customviews.CalendarListPreference
import com.vanishingjar.cllp.customviews.CalendarMultiListPreference

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)

        UpdateChecker.checkForNewVersion(activity!!, prefs)

        val enableSmsSummaryProvider = Preference.SummaryProvider<SwitchPreferenceCompat> { preference ->
            if (preference.isChecked) {
                "CLLP is ready to respond to SMS commands"
            } else {
                "CLLP is ignoring SMS commands"
            }
        }
        findPreference<SwitchPreferenceCompat>("enableReadSms")?.summaryProvider = enableSmsSummaryProvider

        val phNumSummaryProvider = Preference.SummaryProvider<EditTextPreference> { preference ->
            if (preference.text.isNullOrEmpty()) {
                "The number to use for texting (Ex: 3105551234)"
            } else {
                preference.text
            }
        }
        findPreference<EditTextPreference>("phoneNumber")?.summaryProvider = phNumSummaryProvider

        val googleMapsSummaryProvider = Preference.SummaryProvider<EditTextPreference> { preference ->
            if (preference.text.isNullOrEmpty()) {
                "To use Google Maps, please provide your API key"
            } else {
                "Google Maps API key is active"
            }
        }
        findPreference<EditTextPreference>("googleMapsKey")?.summaryProvider = googleMapsSummaryProvider

        val yelpSummaryProvider = Preference.SummaryProvider<EditTextPreference> { preference ->
            if (preference.text.isNullOrEmpty()) {
                "To use Yelp, please provide your API key"
            } else {
                "Yelp API key is active"
            }
        }

        findPreference<EditTextPreference>("yelpKey")?.summaryProvider = yelpSummaryProvider

        val calAgendaSummaryProvider = Preference.SummaryProvider<CalendarMultiListPreference> { preference ->
            if (preference.values.isEmpty()) {
                "Select which calendar(s) to include when you request your upcoming events"
            } else {
                preference.values.size.toString() + " calendar(s) selected"
            }
        }
        findPreference<CalendarMultiListPreference>("calAgenda")?.summaryProvider = calAgendaSummaryProvider

        val calAddSummaryProvider = Preference.SummaryProvider<CalendarListPreference> { preference ->
            if (preference.entry.isNullOrEmpty()) {
                "Select a default calendar for new events"
            } else {
                var projection = arrayOf(
                    CalendarContract.Calendars.CALENDAR_TIME_ZONE
                )
                var selection = "(" + CalendarContract.Calendars._ID + " = ?)"
                var selectionArgs = arrayOf(preference.value)

                if (activity?.checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
                    && activity?.checkSelfPermission(Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
                ) {
                    val cr = activity!!.contentResolver
                    val cursor = cr.query(
                        CalendarContract.Calendars.CONTENT_URI,
                        projection,
                        selection,
                        selectionArgs,
                        null
                    )
                    while (cursor!!.moveToNext()) {
                        val timezone = cursor.getString(0)

                        prefs.edit().putString("calTimeZone", timezone).apply()
                    }

                    cursor.close()
                }

                preference.entry.toString()
            }
        }
        findPreference<CalendarListPreference>("calAdd")?.summaryProvider = calAddSummaryProvider

        findPreference<Preference>("testText")?.setOnPreferenceClickListener { preference ->
            if (preference.isEnabled) {
                SmsManager.getDefault().sendTextMessage(prefs.getString("phoneNumber", ""), null, "Test text message from CLLP!", null, null)

                val builder = AlertDialog.Builder(activity!!)
                builder.setTitle("Did it work?")
                builder.setMessage("You should receive your text within a few seconds. If it didn't work, double check that you've entered your number correctly and if you still have trouble, try including the plus sign and country code (Ex: +13105551234).")
                builder.setPositiveButton("OK"){dialog, which ->

                }
                builder.show()
            }

            true
        }

        findPreference<Preference>("mapsHelp")?.setOnPreferenceClickListener { preference ->
            val url = "https://developers.google.com/maps/documentation/directions/get-api-key"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)

            true
        }

        findPreference<Preference>("yelpHelp")?.setOnPreferenceClickListener { preference ->
            val url = "https://www.yelp.com/developers/faq"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)

            true
        }

        checkCalendarAccess()

        findPreference<Preference>("calEnable")?.setOnPreferenceClickListener { preference ->
            requestPermissions(arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR), 1000)

            true
        }

        findPreference<Preference>("sendFeedback")?.setOnPreferenceClickListener { preference ->
            val feedbackSubjectLine = "CLLP Android App Feedback"
            val feedbackEmailBody = "\n\n\n\n" +
                    "=== Device information === \n" +
                    "OS Version: ${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT}) \n" +
                    "Device: ${Build.DEVICE} \n" +
                    "Model: ${Build.MODEL} (${Build.PRODUCT}) \n" +
                    "CLLP app version: ${BuildConfig.VERSION_NAME}"
            val feedbackEmailAddress = arrayOf("vanishingjar" + "@gmail.com")

            val mailIntent = Intent(Intent.ACTION_SENDTO)
            mailIntent.data = Uri.parse("mailto:") // only email apps should handle this
            mailIntent.putExtra(Intent.EXTRA_SUBJECT, feedbackSubjectLine)
            mailIntent.putExtra(Intent.EXTRA_TEXT, feedbackEmailBody)
            mailIntent.putExtra(Intent.EXTRA_EMAIL, feedbackEmailAddress)

            if (mailIntent.resolveActivity(activity!!.packageManager) != null) {
                startActivity(mailIntent)
            } else {
                val generalSendIntent = Intent(Intent.ACTION_SEND)
                generalSendIntent.putExtra(Intent.EXTRA_SUBJECT, feedbackSubjectLine)
                generalSendIntent.putExtra(Intent.EXTRA_TEXT, feedbackEmailBody)
                generalSendIntent.putExtra(Intent.EXTRA_EMAIL, feedbackEmailAddress)
                if (generalSendIntent.resolveActivity(activity!!.packageManager) != null) {
                    startActivity(generalSendIntent)
                }
            }

            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1000) {
            checkCalendarAccess()
        }
    }

    private fun checkCalendarAccess() {
        if (activity?.checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_DENIED
            || activity?.checkSelfPermission(Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_DENIED) {
            findPreference<CalendarMultiListPreference>("calAgenda")?.isEnabled = false
            findPreference<CalendarListPreference>("calAdd")?.isEnabled = false
        } else {
            val calCat = findPreference<PreferenceCategory>("calendar")
            val calPermission = findPreference<Preference>("calEnable")
            calCat?.removePreference(calPermission)

            findPreference<CalendarMultiListPreference>("calAgenda")?.isEnabled = true
            findPreference<CalendarMultiListPreference>("calAgenda")?.populateList()

            findPreference<CalendarListPreference>("calAdd")?.isEnabled = true
            findPreference<CalendarListPreference>("calAdd")?.populateList()
        }
    }

}