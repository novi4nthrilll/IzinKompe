package com.izinkompe

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.izinkompe.data.db.IzinKompeDatabase
import com.izinkompe.data.datastore.AppPreferences

class IzinKompeApp : Application() {

    lateinit var database: IzinKompeDatabase
        private set

    lateinit var preferences: AppPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        database = IzinKompeDatabase.getDatabase(this)
        preferences = AppPreferences(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.service_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.service_channel_desc)
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "izinkompe_monitor"
    }
}
