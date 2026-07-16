package com.personal.sidebar.apps

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process

/** Device-wide recently used apps, via UsageStatsManager (needs Usage access). */
object Recents {

    /** True if the user has granted the special "Usage access" permission. */
    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Most-recently-foregrounded packages (most recent first), limited to
     * [launchable] packages and excluding this app. Empty if no usage access.
     */
    fun recentApps(context: Context, launchable: Set<String>, limit: Int): List<String> {
        if (!hasUsageAccess(context)) return emptyList()
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val begin = end - 1000L * 60 * 60 * 24 * 3 // last 3 days
        val events = usm.queryEvents(begin, end)
        // Insertion order == chronological; re-inserting keeps the latest last.
        val order = LinkedHashMap<String, Long>()
        val e = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(e)
            if (e.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND || e.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                order.remove(e.packageName)
                order[e.packageName] = e.timeStamp
            }
        }
        val self = context.packageName
        return order.keys
            .reversed()
            .filter { it != self && it in launchable }
            .take(limit)
    }
}
