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
                val pkg = intent?.data?.schemeSpecificPart
                if (pkg == packageName) return
                AppRepository.invalidate()
                val app = this@SidebarApp
                // A genuine uninstall (not the remove half of an app UPDATE, which
                // carries EXTRA_REPLACING) — prune the package from the config.
                val uninstalled = intent?.action == Intent.ACTION_PACKAGE_REMOVED &&
                    !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                scope.launch {
                    // Prune the uninstalled app (invalidate() cleared the cache, so
                    // launchablePackages re-queries the post-uninstall set).
                    if (uninstalled) {
                        runCatching { Settings.pruneMissing(app, AppRepository.launchablePackages(app)) }
                    }
                    // Re-warm the panel's icons so the next open stays instant.
                    if (Settings.enabled(app)) {
                        runCatching { AppRepository.warm(app, Settings.config(app).items) }
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
