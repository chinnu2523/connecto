package com.connecto.store.admin

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Sanctioned Android Enterprise Device Admin / Device Owner receiver for Our Store.
 * When provisioned as Device Owner (e.g. via `adb shell dpm set-device-owner`),
 * Our Store can perform silent, zero-prompt PackageInstaller installs & updates.
 */
class StoreDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.i("OurStoreAdmin", "StoreDeviceAdminReceiver enabled")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.i("OurStoreAdmin", "StoreDeviceAdminReceiver disabled")
    }
}
