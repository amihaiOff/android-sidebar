package com.personal.sidebar

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.personal.sidebar.service.SidebarService
import com.personal.sidebar.util.Permissions

/**
 * An invisible entry point that just opens the sidebar panel, for gesture apps
 * (e.g. Good Lock's One Hand Operation+ "Open app") and launcher shortcuts. It
 * shows no UI of its own: it asks the service to show the panel and finishes.
 * If the sidebar isn't set up yet it opens the settings instead.
 */
class ShowSidebarActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Settings.enabled(this) && Permissions.canDrawOverlays(this)) {
            SidebarService.showPanel(this)
        } else {
            startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
    }

    companion object {
        /** The separate "Open sidebar" entry in the app list (an alias of this activity). */
        private fun alias(context: Context) = ComponentName(context, "com.personal.sidebar.OpenSidebarLauncher")

        fun launcherEntryShown(context: Context): Boolean =
            context.packageManager.getComponentEnabledSetting(alias(context)) !=
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED

        fun setLauncherEntryShown(context: Context, shown: Boolean) {
            context.packageManager.setComponentEnabledSetting(
                alias(context),
                if (shown) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP,
            )
        }
    }
}
