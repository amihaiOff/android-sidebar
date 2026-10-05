package com.personal.sidebar

import android.content.Context
import com.personal.sidebar.model.ItemType
import com.personal.sidebar.model.SidebarConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Which screen edge the handle lives on. */
@Serializable
enum class Edge { LEFT, RIGHT }

/** Persists the on/off flag and the full [SidebarConfig] (as JSON). */
object Settings {
    private const val PREFS = "sidebar_prefs"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_CONFIG = "config_json"
    private const val KEY_RECENTS = "recents"
    private const val MAX_RECENTS = 8

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** True once the user has turned the sidebar on; used to re-arm after reboot. */
    fun enabled(context: Context): Boolean = prefs(context).getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun config(context: Context): SidebarConfig {
        val raw = prefs(context).getString(KEY_CONFIG, null) ?: return SidebarConfig()
        val cfg = runCatching { json.decodeFromString<SidebarConfig>(raw) }
            .getOrDefault(SidebarConfig())
        // One-time migration: the panel used to default to 0.85 (too opaque to
        // show the backdrop blur). The slider produces arbitrary floats, so an
        // exact 0.85 can only be that old untouched default — nudge it to the
        // new frosty default so existing installs get the lighter look.
        return if (cfg.panel.opacity == 0.85f) {
            cfg.copy(panel = cfg.panel.copy(opacity = 0.6f))
        } else {
            cfg
        }
    }

    fun setConfig(context: Context, config: SidebarConfig) {
        prefs(context).edit().putString(KEY_CONFIG, json.encodeToString(config)).apply()
    }

    /** Read-modify-write of the stored config (e.g. edits made from the panel). */
    fun updateConfig(context: Context, transform: (SidebarConfig) -> SidebarConfig) {
        setConfig(context, transform(config(context)))
    }

    /**
     * Removes any package no longer in [installed] from the config: loose app
     * tiles, and folder/group memberships. A folder/group left with no apps and
     * no links is dropped. Links and installed apps are untouched. Rail groups
     * aren't pruned: the rail just skips apps that aren't installed, so a
     * starter app installed later (or a reinstall) shows up again. Returns true
     * if anything changed. [installed] must be the real launchable set — callers
     * must not pass an empty set (a failed query) or everything would be pruned.
     */
    fun pruneMissing(context: Context, installed: Set<String>): Boolean {
        if (installed.isEmpty()) return false
        val cfg = config(context)
        val newItems = cfg.items.mapNotNull { item ->
            when (item.type) {
                ItemType.APP -> if (item.packageName != null && item.packageName !in installed) null else item
                ItemType.FOLDER, ItemType.GROUP -> {
                    val pkgs = item.packages.filter { it in installed }
                    when {
                        pkgs.size == item.packages.size -> item
                        pkgs.isEmpty() && item.links.isEmpty() -> null
                        else -> item.copy(packages = pkgs)
                    }
                }
                else -> item
            }
        }
        return if (newItems != cfg.items) {
            setConfig(context, cfg.copy(items = newItems))
            true
        } else false
    }

    /** Recently launched packages, most-recent first. */
    fun recents(context: Context): List<String> =
        prefs(context).getString(KEY_RECENTS, "").orEmpty().split(",").filter { it.isNotBlank() }

    fun addRecent(context: Context, packageName: String) {
        val updated = (listOf(packageName) + recents(context).filter { it != packageName }).take(MAX_RECENTS)
        prefs(context).edit().putString(KEY_RECENTS, updated.joinToString(",")).apply()
    }
}
