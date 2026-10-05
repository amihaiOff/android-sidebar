package com.personal.sidebar.apps

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Process
import android.graphics.drawable.Drawable
import com.personal.sidebar.Settings
import com.personal.sidebar.model.Design
import com.personal.sidebar.model.SidebarConfig
import java.util.concurrent.ConcurrentHashMap
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

    // Panel-side caches. The panel only shows a small curated subset, so it
    // resolves icons/labels for just those packages instead of enumerating the
    // whole drawer (an icon decode per installed app — the old multi-second
    // stall). [infoCache] holds resolved AppInfo per package; [launchableCache]
    // is the cheap set of launchable package names used to filter recents.
    private val infoCache = ConcurrentHashMap<String, AppInfo>()
    @Volatile private var launchableCache: Set<String>? = null

    suspend fun load(context: Context, refresh: Boolean = false): List<AppInfo> {
        cache?.let { if (!refresh) return it }
        return mutex.withLock {
            cache?.let { if (!refresh) return it }
            val apps = withContext(Dispatchers.IO) { query(context.applicationContext) }
            cache = apps
            // Share the full result with the panel-side caches so a Settings
            // load also warms the panel.
            launchableCache = apps.mapTo(LinkedHashSet()) { it.packageName }
            apps.forEach { infoCache[it.packageName] = it }
            apps
        }
    }

    /** All launchable apps keyed by package name, for resolving curated items. */
    suspend fun map(context: Context, refresh: Boolean = false): Map<String, AppInfo> =
        load(context, refresh).associateBy { it.packageName }

    /**
     * The set of launchable package names — cheap (no icon/label decode). Used to
     * filter recents to installed, launchable apps without loading the drawer.
     */
    suspend fun launchablePackages(context: Context): Set<String> {
        launchableCache?.let { return it }
        return withContext(Dispatchers.IO) {
            val ctx = context.applicationContext
            val pm = ctx.packageManager
            val self = ctx.packageName
            val set = LinkedHashSet<String>()
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            for (ri in pm.queryIntentActivities(intent, 0)) {
                val pkg = ri.activityInfo?.packageName ?: continue
                if (pkg != self) set.add(pkg)
            }
            runCatching {
                for (info in pm.getInstalledApplications(0)) {
                    val pkg = info.packageName
                    if (pkg == self || pkg in set) continue
                    if (pm.getLaunchIntentForPackage(pkg) != null) set.add(pkg)
                }
            }
            set.also { launchableCache = it }
        }
    }

    /**
     * Resolves icon + label for only [packages], caching each. Unresolvable
     * packages (uninstalled, no launcher entry) are simply omitted. This is what
     * the panel calls — a handful of curated apps rather than the whole drawer.
     */
    suspend fun infoFor(context: Context, packages: Collection<String>): Map<String, AppInfo> {
        if (packages.isEmpty()) return emptyMap()
        return withContext(Dispatchers.IO) {
            val pm = context.applicationContext.packageManager
            val result = LinkedHashMap<String, AppInfo>()
            for (pkg in packages) {
                if (pkg in result) continue
                val info = infoCache[pkg] ?: loadOne(context, pm, pkg)?.also { infoCache[pkg] = it }
                if (info != null) result[pkg] = info
            }
            result
        }
    }

    /**
     * Synchronous, non-blocking read of already-resolved icons for [packages]
     * from the in-memory cache (no IO). Lets the panel render a complete first
     * frame off a warm cache — no loading spinner, no content swap/jump. Missing
     * packages are simply omitted; the async [infoFor] fills any gaps.
     */
    fun cachedInfoFor(packages: Collection<String>): Map<String, AppInfo> {
        if (packages.isEmpty()) return emptyMap()
        val result = LinkedHashMap<String, AppInfo>()
        for (pkg in packages) infoCache[pkg]?.let { result[pkg] = it }
        return result
    }

    /**
     * Pre-resolves everything the panel will show for [config] (curated packages
     * + recents, for whichever design is active) so the panel opens against a
     * warm cache instead of a spinner. Safe to call repeatedly — cached entries
     * are no-ops.
     */
    suspend fun warm(context: Context, config: SidebarConfig) {
        val infos = infoFor(context, neededPackages(config, recentPackages(context, MAX_PANEL_RECENTS)))
        if (config.design == Design.RAIL) {
            withContext(Dispatchers.Default) {
                com.personal.sidebar.ui.prewarmRailIcons(context, config.rail, infos.values)
            }
        }
    }

    /** Most-recent apps: phone-wide with Usage access, else the sidebar's own launches. */
    suspend fun recentPackages(context: Context, limit: Int): List<String> {
        val launchable = launchablePackages(context)
        return withContext(Dispatchers.IO) {
            Recents.recentApps(context, launchable, limit)
                .ifEmpty { Settings.recents(context).filter { it in launchable }.take(limit) }
        }
    }

    /** Packages the active design resolves icons for: its curated apps + recents. */
    fun neededPackages(config: SidebarConfig, recents: List<String>): Set<String> = buildSet {
        when (config.design) {
            Design.GLASS -> for (item in config.items) {
                item.packageName?.let { add(it) }
                addAll(item.packages)
            }
            Design.RAIL -> config.rail.folders.forEach { f -> f.groups.forEach { addAll(it.packages) } }
        }
        addAll(recents)
    }

    /** The most recents any design shows. */
    const val MAX_PANEL_RECENTS = 6

    /**
     * The app's launcher entry as launchers see it. Its icon is what the home
     * screen shows, so system icon themes (e.g. Samsung Galaxy Themes / Theme
     * Park icon packs) apply to the sidebar too.
     */
    private fun launcherInfo(context: Context, pkg: String?): List<LauncherActivityInfo> = runCatching {
        context.getSystemService(LauncherApps::class.java).getActivityList(pkg, Process.myUserHandle())
    }.getOrDefault(emptyList())

    private fun LauncherActivityInfo.toAppInfo(context: Context) =
        AppInfo(label.toString(), applicationInfo.packageName, IconPacks.apply(context, componentName, getIcon(0)))

    private fun loadOne(context: Context, pm: PackageManager, pkg: String): AppInfo? {
        launcherInfo(context, pkg).firstOrNull()?.let { return runCatching { it.toAppInfo(context) }.getOrNull() }
        val launch = pm.getLaunchIntentForPackage(pkg) ?: return null
        val cmp = launch.component
        return runCatching {
            val activity = cmp?.let { pm.getActivityInfo(it, 0) }
            if (activity != null) {
                AppInfo(activity.loadLabel(pm).toString(), pkg, IconPacks.apply(context, cmp, activity.loadIcon(pm)))
            } else {
                val app = pm.getApplicationInfo(pkg, 0)
                AppInfo(app.loadLabel(pm).toString(), pkg, IconPacks.apply(context, cmp, app.loadIcon(pm)))
            }
        }.getOrNull()
    }

    fun invalidate() {
        cache = null
        launchableCache = null
        infoCache.clear()
        com.personal.sidebar.ui.PanelIcons.clear()
        IconPacks.invalidate()
    }

    private fun query(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val self = context.packageName
        val byPackage = LinkedHashMap<String, AppInfo>()

        // Primary path: launcher entries as the home screen sees them (icons
        // include any system icon theme).
        for (info in launcherInfo(context, null)) {
            val pkg = info.applicationInfo.packageName
            if (pkg == self || byPackage.containsKey(pkg)) continue
            runCatching { byPackage[pkg] = info.toAppInfo(context) }
        }

        // Anything else with a launcher entry.
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        for (ri in pm.queryIntentActivities(intent, 0)) {
            val pkg = ri.activityInfo?.packageName ?: continue
            if (pkg == self || byPackage.containsKey(pkg)) continue
            val cn = android.content.ComponentName(pkg, ri.activityInfo.name)
            byPackage[pkg] = AppInfo(ri.loadLabel(pm).toString(), pkg, IconPacks.apply(context, cn, ri.loadIcon(pm)))
        }

        // Secondary path: installed packages that are launchable but the launcher
        // category query didn't surface — notably PWAs installed as WebAPKs
        // (org.chromium.webapk.*), which some launchers hide from the app drawer.
        runCatching {
            for (info in pm.getInstalledApplications(0)) {
                val pkg = info.packageName
                if (pkg == self || byPackage.containsKey(pkg)) continue
                val launch = pm.getLaunchIntentForPackage(pkg) ?: continue
                val cmp = launch.component
                val label = info.loadLabel(pm).toString()
                val icon = runCatching {
                    cmp?.let { pm.getActivityInfo(it, 0).loadIcon(pm) }
                }.getOrNull() ?: info.loadIcon(pm)
                byPackage[pkg] = AppInfo(label, pkg, IconPacks.apply(context, cmp, icon))
            }
        }

        return byPackage.values.sortedBy { it.label.lowercase() }
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
