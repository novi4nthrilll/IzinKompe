package com.izinkompe.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.izinkompe.data.datastore.AppPreferences
import com.izinkompe.service.AppMonitorService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Receives BOOT_COMPLETED broadcast to restart the monitoring service after device reboot.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {

            Log.d("BootReceiver", "Boot completed, checking if service should restart")

            val prefs = AppPreferences(context)
            CoroutineScope(Dispatchers.IO).launch {
                val isEnabled = prefs.isServiceEnabled.first()
                if (isEnabled) {
                    Log.d("BootReceiver", "Service was enabled, restarting...")
                    AppMonitorService.start(context)
                }
            }
        }
    }
}
