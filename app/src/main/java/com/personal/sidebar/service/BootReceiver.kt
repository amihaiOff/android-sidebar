package com.personal.sidebar.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings as AndroidSettings
import com.personal.sidebar.Settings

/**
 * Re-arms the sidebar after a reboot or an app update, but only if the user had
 * it enabled and the overlay permission is still granted.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        if (Settings.enabled(context) && AndroidSettings.canDrawOverlays(context)) {
            SidebarService.start(context)
        }
    }
}
