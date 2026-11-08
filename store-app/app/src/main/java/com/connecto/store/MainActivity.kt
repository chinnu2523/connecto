package com.connecto.store

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.connecto.store.notifications.StoreNotificationHelper
import com.connecto.store.ui.StoreApp
import com.connecto.store.viewmodel.StoreViewModel

class MainActivity : FragmentActivity() {

    private lateinit var viewModel: StoreViewModel
    private var packageReceiver: BroadcastReceiver? = null
    private var localInstallReceiver: BroadcastReceiver? = null

    // Deep-link target state from notifications
    private var targetPackage by mutableStateOf<String?>(null)
    private var targetSection by mutableStateOf<String?>(null)

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.checkAndNotifyAllUpdates(force = true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        StoreNotificationHelper.initNotificationChannel(this)

        // Ensure autonomous background update checking job and alarm are scheduled
        com.connecto.store.notifications.StoreUpdateJobService.schedule(this)
        com.connecto.store.notifications.StoreUpdateAlarmReceiver.scheduleNext(this)

        viewModel = StoreViewModel(application)

        // Request notification permission on Android 13+ if not already granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        handleDeepLink(intent)

        // Register system package change receiver so Our Store immediately detects installs/updates
        val pkgFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        }

        packageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                viewModel.refreshInstallStates()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(packageReceiver, pkgFilter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(packageReceiver, pkgFilter)
        }

        // Register internal receiver for PackageInstaller session completion
        val localFilter = IntentFilter("com.connecto.store.PACKAGE_INSTALLED").apply {
            addAction("com.connecto.store.PACKAGE_INSTALL_FAILED")
        }

        localInstallReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                viewModel.refreshInstallStates()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(localInstallReceiver, localFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(localInstallReceiver, localFilter)
        }

        setContent {
            StoreApp(
                vm = viewModel,
                initialPackageName = targetPackage,
                initialSectionName = targetSection,
                onTargetHandled = {
                    targetPackage = null
                    targetSection = null
                }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        intent?.getStringExtra("OPEN_PACKAGE")?.let { pkg ->
            targetPackage = pkg
        }
        intent?.getStringExtra("OPEN_SECTION")?.let { sec ->
            targetSection = sec
        }
    }

    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) {
            viewModel.onActivityResumed(this)
        }
    }

    override fun onStop() {
        super.onStop()
        // When user leaves app or closes from background, trigger background scan after 10s
        com.connecto.store.notifications.StoreUpdateAlarmReceiver.scheduleNext(this, delayMs = 10_000L)
    }

    override fun onDestroy() {
        super.onDestroy()
        packageReceiver?.let {
            try { unregisterReceiver(it) } catch (_: Exception) {}
        }
        localInstallReceiver?.let {
            try { unregisterReceiver(it) } catch (_: Exception) {}
        }
    }
}
