package com.personal.sidebar.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.personal.sidebar.MainActivity
import com.personal.sidebar.R
import com.personal.sidebar.Settings
import com.personal.sidebar.SidebarApp
import com.personal.sidebar.apps.AppRepository
import com.personal.sidebar.overlay.EdgeHandle
import com.personal.sidebar.overlay.PanelController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground service that hosts the edge handle and the on-demand panel. It runs
 * as a foreground service purely so the OS keeps the (idle) handle window alive;
 * it holds no wakelocks and does no background work.
 */
class SidebarService : Service() {

    private lateinit var edgeHandle: EdgeHandle
    private lateinit var panel: PanelController
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        startForegroundInternal()
        panel = PanelController(this)
        edgeHandle = EdgeHandle(this) {
            if (!panel.isShowing) panel.show(Settings.config(this))
        }
        showHandle()
        // Pre-resolve the panel's icons now so the first open is instant instead
        // of showing the loading spinner while it enumerates apps.
        warm()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_REFRESH -> {
                // Config changed while running: re-place the handle, drop any open panel.
                panel.hide()
                showHandle()
                warm() // curated set may have changed — re-warm for an instant open
            }
            ACTION_SHOW_PANEL -> if (!panel.isShowing) panel.show(Settings.config(this))
        }
        return START_STICKY
    }

    override fun onDestroy() {
        if (::panel.isInitialized) panel.hide()
        if (::edgeHandle.isInitialized) edgeHandle.hide()
        scope.cancel()
        super.onDestroy()
    }

    /** Shows the edge handle, unless the user opens the sidebar only via gestures. */
    private fun showHandle() {
        val handle = Settings.config(this).handle
        if (handle.visible) edgeHandle.show(handle) else edgeHandle.hide()
    }

    private fun warm() {
        scope.launch {
            runCatching {
                // Reconcile the config against installed apps first — prunes
                // anything uninstalled while the process was dead (no broadcast).
                val installed = AppRepository.launchablePackages(applicationContext)
                Settings.pruneMissing(applicationContext, installed)
                AppRepository.warm(applicationContext, Settings.config(applicationContext))
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundInternal() {
        val tapIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val notification: Notification = NotificationCompat.Builder(this, SidebarApp.CHANNEL_ID)
            .setContentTitle(getString(R.string.notif_title))
            .setContentText(getString(R.string.notif_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(tapIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 42
        const val ACTION_REFRESH = "com.personal.sidebar.REFRESH"
        const val ACTION_SHOW_PANEL = "com.personal.sidebar.SHOW_PANEL"

        fun start(context: Context) {
            // May throw if the OS blocks starting a foreground service from the
            // background (e.g. an OEM-blocked boot broadcast); don't crash.
            runCatching {
                ContextCompat.startForegroundService(
                    context, Intent(context, SidebarService::class.java)
                )
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, SidebarService::class.java))
        }

        /** Open the panel now (from [com.personal.sidebar.ShowSidebarActivity]). */
        fun showPanel(context: Context) {
            runCatching {
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, SidebarService::class.java).setAction(ACTION_SHOW_PANEL),
                )
            }
        }

        /** Re-read config and re-place the handle (call after any settings change). */
        fun refresh(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, SidebarService::class.java).setAction(ACTION_REFRESH),
            )
        }
    }
}
