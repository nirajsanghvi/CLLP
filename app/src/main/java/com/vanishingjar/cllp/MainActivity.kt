package com.vanishingjar.cllp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = "Command Line for Light Phone"

        if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(arrayOf(Manifest.permission.SEND_SMS), 1001)
        }

        if (!NotificationManagerCompat.getEnabledListenerPackages(this).contains(applicationContext.packageName)) {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Enable Notification Access")
            builder.setMessage("In order to function, this app requires notification access to be able to receive your SMS commands. Please enable notification access for CLLP.")
            builder.setPositiveButton("OK"){dialog, which ->
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
            builder.setNegativeButton("Cancel"){dialog, which ->
                finish()
            }
            builder.show()
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
    }
}
