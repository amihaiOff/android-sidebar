package com.personal.sidebar

import android.content.Context

/** Which screen edge the handle lives on. */
enum class Edge { LEFT, RIGHT }

/** Tiny SharedPreferences wrapper for the two things we persist. */
object Settings {
    private const val PREFS = "sidebar_prefs"
    private const val KEY_EDGE = "edge"
    private const val KEY_ENABLED = "enabled"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun edge(context: Context): Edge =
        if (prefs(context).getString(KEY_EDGE, Edge.RIGHT.name) == Edge.LEFT.name) Edge.LEFT else Edge.RIGHT

    fun setEdge(context: Context, edge: Edge) {
        prefs(context).edit().putString(KEY_EDGE, edge.name).apply()
    }

    /** True once the user has turned the sidebar on; used to re-arm after reboot. */
    fun enabled(context: Context): Boolean = prefs(context).getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }
}
