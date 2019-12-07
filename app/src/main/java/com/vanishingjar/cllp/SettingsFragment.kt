package com.vanishingjar.cllp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import androidx.appcompat.app.AlertDialog
import androidx.preference.*
import com.vanishingjar.cllp.customviews.CalendarListPreference
import com.vanishingjar.cllp.customviews.CalendarMultiListPreference

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)

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
                "The number used for texting with country code (Ex: +13105551234)"
            } else {
                preference.text
            }
        }

        findPreference<EditTextPreference>("phoneNumber")?.summaryProvider = phNumSummaryProvider

        val googleMapsSummaryProvider = Preference.SummaryProvider<EditTextPreference> { preference ->
            if (preference.text.isNullOrEmpty()) {
                "To use Google Maps, please provide an API key"
            } else {
                "Google Maps API key is active"
            }
        }

        findPreference<EditTextPreference>("googleMapsKey")?.summaryProvider = googleMapsSummaryProvider

        val yelpSummaryProvider = Preference.SummaryProvider<EditTextPreference> { preference ->
            if (preference.text.isNullOrEmpty()) {
                "To use Yelp, please provide an API key"
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
                preference.entry.toString()
            }
        }

        findPreference<CalendarListPreference>("calAdd")?.summaryProvider = calAddSummaryProvider

        findPreference<Preference>("testText")?.setOnPreferenceClickListener { preference ->
            if (preference.isEnabled) {
                SmsManager.getDefault().sendTextMessage(prefs.getString("phoneNumber", ""), null, "Test text message from CLLP!", null, null)

                val builder = AlertDialog.Builder(activity!!)
                builder.setTitle("Did it work?")
                builder.setMessage("You should receive your text within a few seconds. If it didn't work, double check that you've formatted your number correctly, including the plus sign and country code (Ex: +13105551234).")
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