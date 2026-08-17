package com.personal.sidebar

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.personal.sidebar.apps.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SidebarApp : Application() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notif_channel_name),
            NotificationManager.IMPORTANCE_MIN,
        ).apply {
            description = getString(R.string.notif_text)
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        // Drop the cached app list whenever an app is installed / removed /
        // updated, so the picker and the panel pick up the change automatically.
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.data?.schemeSpecificPart == packageName) return
                AppRepository.invalidate()
                // Re-warm the panel's icons in the background so the next open
                // stays instant instead of falling back to the loading spinner.
                if (Settings.enabled(this@SidebarApp)) {
                    scope.launch {
                        runCatching { AppRepository.warm(applicationContext, Settings.config(this@SidebarApp).items) }
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        registerReceiver(receiver, filter)
    }

    companion object {
        const val CHANNEL_ID = "sidebar_handle"
    }
}
