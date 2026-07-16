package com.personal.sidebar.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** A launchable app: everything the panel needs to draw and start it. */
data class AppInfo(
    val label: String,
    val packageName: String,
    val icon: Drawable,
)

/**
 * Enumerates launchable apps via PackageManager. Results are cached in memory so
 * re-opening the panel is instant; call [load] with refresh=true to rebuild
 * (e.g. after installing/removing an app). No polling — this only runs when the
 * panel is opened.
 */
object AppRepository {
    private val mutex = Mutex()
    @Volatile private var cache: List<AppInfo>? = null

    suspend fun load(context: Context, refresh: Boolean = false): List<AppInfo> {
        cache?.let { if (!refresh) return it }
        return mutex.withLock {
            cache?.let { if (!refresh) return it }
            val apps = withContext(Dispatchers.IO) { query(context.applicationContext) }
            cache = apps
            apps
        }
    }

    /** All launchable apps keyed by package name, for resolving curated items. */
    suspend fun map(context: Context, refresh: Boolean = false): Map<String, AppInfo> =
        load(context, refresh).associateBy { it.packageName }

    fun invalidate() { cache = null }

    private fun query(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = pm.queryIntentActivities(intent, 0)
        val self = context.packageName
        return resolved.asSequence()
            .mapNotNull { ri ->
                val pkg = ri.activityInfo?.packageName ?: return@mapNotNull null
                if (pkg == self) return@mapNotNull null // don't list ourselves
                AppInfo(
                    label = ri.loadLabel(pm).toString(),
                    packageName = pkg,
                    icon = ri.loadIcon(pm),
                )
            }
            // One entry per package (some apps expose several launcher activities).
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    /** Launches an app by package name. Safe to call from an overlay/service. */
    fun launch(context: Context, packageName: String): Boolean {
        val launch = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: return false
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launch)
        return true
    }
}
