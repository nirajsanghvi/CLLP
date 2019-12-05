package com.vanishingjar.cllp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(arrayOf(Manifest.permission.SEND_SMS), 1001)
        }

        if (!Settings.Secure.getString(this.contentResolver, "enabled_notification_listeners").contains(applicationContext.packageName)) {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Enable Notification Access")
            builder.setMessage("In order to function, this app requires notification access to be able to receive your SMS commands. Please enable notification access for Command Line for LP.")
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
