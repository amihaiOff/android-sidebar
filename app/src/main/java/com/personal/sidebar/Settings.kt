package com.personal.sidebar

import android.content.Context
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

    /** Recently launched packages, most-recent first. */
    fun recents(context: Context): List<String> =
        prefs(context).getString(KEY_RECENTS, "").orEmpty().split(",").filter { it.isNotBlank() }

    fun addRecent(context: Context, packageName: String) {
        val updated = (listOf(packageName) + recents(context).filter { it != packageName }).take(MAX_RECENTS)
        prefs(context).edit().putString(KEY_RECENTS, updated.joinToString(",")).apply()
    }
}
