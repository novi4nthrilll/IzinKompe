package com.izinkompe.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

/**
 * Device Admin Receiver to prevent app uninstallation.
 * While active as device admin, the app cannot be uninstalled via normal means.
 */
class AdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.d("AdminReceiver", "Device admin enabled")
        Toast.makeText(context, "IzinKompe: Device Admin aktif", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.d("AdminReceiver", "Device admin disabled")
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "Menonaktifkan Device Admin akan membuat IzinKompe bisa di-uninstall. Lanjutkan?"
    }
}
