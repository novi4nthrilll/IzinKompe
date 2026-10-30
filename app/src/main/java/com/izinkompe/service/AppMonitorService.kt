package com.izinkompe.service

import android.app.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.izinkompe.IzinKompeApp
import com.izinkompe.R
import com.izinkompe.ui.MainActivity
import com.izinkompe.ui.lock.LockActivity
import com.izinkompe.util.ScheduleChecker
import kotlinx.coroutines.*
import java.util.Calendar

/**
 * Foreground service that monitors the foreground app using UsageStatsManager.
 * When a locked app is detected during an active study session, it launches LockActivity.
 */
class AppMonitorService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var monitorJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        private const val TAG = "AppMonitorService"
        private const val NOTIFICATION_ID = 1001
        private const val POLL_INTERVAL_MS = 800L // 800ms polling

        fun start(context: Context) {
            val intent = Intent(context, AppMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, AppMonitorService::class.java))
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        acquireWakeLock()
        startMonitoring()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        monitorJob?.cancel()
        serviceScope.cancel()
        releaseWakeLock()
        Log.d(TAG, "Service destroyed")
    }

    private fun startMonitoring() {
        monitorJob?.cancel()
        monitorJob = serviceScope.launch {
            val app = application as IzinKompeApp
            val db = app.database
            val prefs = app.preferences

            while (isActive) {
                try {
                    // Get current schedule
                    val schedule = db.scheduleDao().getScheduleOnce()
                    val isSessionActive = ScheduleChecker.isSessionActive(schedule)

                    if (isSessionActive) {
                        // Check if temporarily unlocked
                        val isUnlocked = prefs.isCurrentlyUnlocked()
                        if (!isUnlocked) {
                            // Get the current foreground app
                            val foregroundPackage = getForegroundPackage()
                            if (foregroundPackage != null && foregroundPackage != packageName) {
                                val isSettings = foregroundPackage == "com.android.settings"
                                val isLocked = isSettings || db.lockedAppDao().isAppLocked(foregroundPackage)
                                if (isLocked) {
                                    Log.d(TAG, "Blocked app detected: $foregroundPackage")
                                    launchLockScreen(foregroundPackage)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in monitoring loop", e)
                }

                delay(POLL_INTERVAL_MS)
            }
        }
    }

    /**
     * Gets the package name of the most recently resumed activity using UsageStatsManager.
     */
    private fun getForegroundPackage(): String? {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return null

        val endTime = System.currentTimeMillis()
        val startTime = endTime - 5000 // Look back 5 seconds

        val events = usageStatsManager.queryEvents(startTime, endTime)
        var lastResumedPackage: String? = null
        val event = UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                lastResumedPackage = event.packageName
            }
        }

        return lastResumedPackage
    }

    private fun launchLockScreen(blockedPackage: String) {
        val intent = Intent(this, LockActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(LockActivity.EXTRA_BLOCKED_PACKAGE, blockedPackage)
        }
        startActivity(intent)
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, IzinKompeApp.CHANNEL_ID)
            .setContentTitle(getString(R.string.service_notification_title))
            .setContentText(getString(R.string.service_notification_text))
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "IzinKompe::MonitorWakeLock"
        ).apply {
            acquire(10 * 60 * 1000L) // 10 minutes, will re-acquire
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }
}
