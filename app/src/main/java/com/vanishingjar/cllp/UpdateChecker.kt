package com.vanishingjar.cllp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import com.google.firebase.analytics.FirebaseAnalytics
import com.vanishingjar.cllp.api.github.GitHubResponse
import com.vanishingjar.cllp.api.github.GitHubService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class UpdateChecker {
    companion object {
        private const val gitHubUser = "nirajsanghvi"
        private const val gitHubRepo = "CLLP"

        fun checkForNewVersion(context: Context, prefs: SharedPreferences) {
            val analytics = FirebaseAnalytics.getInstance(context)

            val interceptor = HttpLoggingInterceptor()
            interceptor.apply { interceptor.level = HttpLoggingInterceptor.Level.BODY }
            val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

            val retrofit = Retrofit.Builder()
                .client(client)
                .baseUrl("https://api.github.com/repos/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service: GitHubService = retrofit.create<GitHubService>(GitHubService::class.java)
            val call = service.getLatestRelease(gitHubUser, gitHubRepo)

            call.enqueue(object: Callback<GitHubResponse> {
                override fun onResponse(call: Call<GitHubResponse>, response: Response<GitHubResponse>) {
                    response.body()?.let { ghResponse ->
                        if (ghResponse.assets.isNotEmpty()) {
                            if (BuildConfig.VERSION_NAME != ghResponse.name) {
                                //Avoid spamming notifications for the same version in case the user didn't update after the last notification for some reason, use a 3 day cool-off period
                                val lastUpdateNotification = prefs.getLong("lastUpdateNotification", 0)
                                val lastVersion = prefs.getString("lastUpdateDetectedVersion", "")
                                if (ghResponse.name == lastVersion && System.currentTimeMillis() - lastUpdateNotification < 3 * DateUtils.DAY_IN_MILLIS) {
                                    return
                                }

                                notifyUpdateIsAvailable(context, ghResponse.name, ghResponse.htmlUrl)
                                prefs.edit {
                                    putLong("lastUpdateNotification", System.currentTimeMillis())
                                    putString("lastUpdateDetectedVersion", ghResponse.name)
                                }

                                val bundle = Bundle()
                                bundle.putString("currentInstalledVersion", BuildConfig.VERSION_NAME)
                                bundle.putString("newDetectedVersion", ghResponse.name)
                                analytics.logEvent("gitHubUpdateNotif", bundle)
                            }
                        }
                    } ?: run {
                        val bundle = Bundle()
                        bundle.putString("currentInstalledVersion", BuildConfig.VERSION_NAME)
                        analytics.logEvent("gitHubEmptyResponse", bundle)
                    }
                }

                override fun onFailure(call: Call<GitHubResponse>, t: Throwable) {
                    Log.d("UpdateChecker", "Unable to retrieve latest app version info from GitHub")
                    val bundle = Bundle()
                    bundle.putString("currentInstalledVersion", BuildConfig.VERSION_NAME)
                    analytics.logEvent("gitHubFailure", bundle)
                }
            })
        }

        private fun notifyUpdateIsAvailable(context: Context, newVersion: String, releaseUrl: String) {
            val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            setupNotificationChannel(notifManager)

            val notifIntent = Intent(Intent.ACTION_VIEW, Uri.parse(releaseUrl))
            val pendingIntent = PendingIntent.getActivity(context, 0, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val notification = NotificationCompat.Builder(context, "CLLP001")
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setAutoCancel(true)
                .setContentTitle("CLLP app update available")
                .setContentText("Tap to update CLLP to version $newVersion")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            notifManager.notify(1, notification)
        }

        private fun setupNotificationChannel(notifManager: NotificationManager) {
            if (SDK_INT >= Build.VERSION_CODES.O) {
                val notifChannel = NotificationChannel("CLLP001", "Update notifications", NotificationManager.IMPORTANCE_DEFAULT)
                notifManager.createNotificationChannel(notifChannel)
            }
        }
    }
}