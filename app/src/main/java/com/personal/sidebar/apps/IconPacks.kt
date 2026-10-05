package com.personal.sidebar.apps

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import com.personal.sidebar.Settings
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

/**
 * Third-party icon packs (the ADW / Nova format nearly every pack and launcher
 * uses). Launchers apply these themselves; the system icon never changes, so
 * the sidebar reads the chosen pack directly: its `appfilter.xml` maps app
 * components to drawables in the pack. Apps the pack doesn't cover get the
 * pack's fallback treatment (icon back / mask / overlay / scale) if it has one.
 */
object IconPacks {
    /** Intent actions icon packs declare so launchers can find them. */
    private val ACTIONS = listOf(
        "org.adw.launcher.THEMES",
        "com.novalauncher.THEME",
        "com.teslacoilsw.launcher.THEME",
        "com.anddoes.launcher.THEME",
        "com.gau.go.launcherex.theme",
        "com.dlto.atom.launcher.THEME",
    )

    data class Pack(val packageName: String, val label: String)

    /** Installed icon packs, by name. */
    fun installed(context: Context): List<Pack> {
        val pm = context.packageManager
        return ACTIONS.flatMap { action ->
            runCatching { pm.queryIntentActivities(Intent(action), PackageManager.GET_META_DATA) }.getOrDefault(emptyList())
        }
            .mapNotNull { ri -> ri.activityInfo?.applicationInfo }
            .distinctBy { it.packageName }
            .map { Pack(it.packageName, it.loadLabel(pm).toString()) }
            .sortedBy { it.label.lowercase() }
    }

    private class Loaded(
        val pkg: String,
        val res: Resources,
        /** "pkg/fully.qualified.Activity" → drawable name. */
        val map: Map<String, String>,
        val backs: List<String>,
        val mask: String?,
        val upon: String?,
        val scale: Float,
    )

    /** The loaded pack, keyed by its package; [UNSET] until the setting is read. */
    @Volatile private var loaded: Any? = UNSET
    private object UNSET

    /** Forget the loaded pack (the choice changed, or apps did). */
    fun invalidate() { loaded = UNSET }

    private fun current(context: Context): Loaded? {
        val l = loaded
        if (l !== UNSET) return l as Loaded?
        synchronized(this) {
            if (loaded === UNSET) {
                val pkg = Settings.config(context).iconPack
                loaded = pkg?.let { runCatching { load(context, it) }.getOrNull() }
            }
            return loaded as Loaded?
        }
    }

    /** [component]'s icon from the chosen pack, or [original] if there's no pack. */
    fun apply(context: Context, component: ComponentName?, original: Drawable): Drawable {
        val pack = current(context) ?: return original
        return runCatching {
            component?.let { pack.map[key(it.packageName, it.className)] }
                ?.let { drawable(pack, it) }
                ?: fallback(pack, original)
        }.getOrNull() ?: original
    }

    private fun key(pkg: String, cls: String): String =
        "$pkg/${if (cls.startsWith(".")) pkg + cls else cls}"

    private fun drawable(pack: Loaded, name: String): Drawable? {
        val id = pack.res.getIdentifier(name, "drawable", pack.pkg).takeIf { it != 0 }
            ?: pack.res.getIdentifier(name, "mipmap", pack.pkg).takeIf { it != 0 }
            ?: return null
        @Suppress("DEPRECATION")
        return pack.res.getDrawableForDensity(id, android.util.DisplayMetrics.DENSITY_XXXHIGH, null)
            ?: pack.res.getDrawable(id, null)
    }

    /** The pack's generic treatment for apps it has no icon for. */
    private fun fallback(pack: Loaded, original: Drawable): Drawable? {
        val back = pack.backs.firstOrNull()?.let { drawable(pack, it) } ?: return null
        val size = 192
        val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(out)
        // The app's own icon, scaled down into the pack's frame…
        val inner = (size * pack.scale).toInt().coerceIn(1, size)
        val off = (size - inner) / 2f
        c.drawBitmap((original.constantState?.newDrawable()?.mutate() ?: original).toBitmap(inner, inner), off, off, null)
        // …cut by the mask, set on the background, with the overlay on top.
        pack.mask?.let { drawable(pack, it) }?.let { m ->
            c.drawBitmap(m.toBitmap(size, size), 0f, 0f, Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT) })
        }
        c.drawBitmap(back.toBitmap(size, size), 0f, 0f, Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OVER) })
        pack.upon?.let { drawable(pack, it) }?.let { c.drawBitmap(it.toBitmap(size, size), 0f, 0f, null) }
        return BitmapDrawable(null as Resources?, out)
    }

    private fun load(context: Context, pkg: String): Loaded {
        val res = context.packageManager.getResourcesForApplication(pkg)
        val map = HashMap<String, String>()
        val backs = ArrayList<String>()
        var mask: String? = null
        var upon: String? = null
        var scale = 1f

        val xmlId = res.getIdentifier("appfilter", "xml", pkg)
        val parser: XmlPullParser = if (xmlId != 0) {
            res.getXml(xmlId)
        } else {
            XmlPullParserFactory.newInstance().newPullParser().apply {
                setInput(res.assets.open("appfilter.xml"), "UTF-8")
            }
        }
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                fun attr(name: String): String? = parser.getAttributeValue(null, name)
                when (parser.name) {
                    "item" -> {
                        val comp = attr("component")
                        val draw = attr("drawable")
                        if (comp != null && draw != null) {
                            // "ComponentInfo{pkg/cls}"
                            val inner = comp.substringAfter('{', "").substringBefore('}', "")
                            val p = inner.substringBefore('/', "")
                            val cls = inner.substringAfter('/', "")
                            if (p.isNotEmpty() && cls.isNotEmpty()) map.putIfAbsent(key(p, cls), draw)
                        }
                    }
                    "iconback" -> (0 until parser.attributeCount).forEach { i -> backs += parser.getAttributeValue(i) }
                    "iconmask" -> mask = attr("img1")
                    "iconupon" -> upon = attr("img1")
                    "scale" -> scale = attr("factor")?.toFloatOrNull() ?: 1f
                }
            }
            event = parser.next()
        }
        return Loaded(pkg, res, map, backs, mask, upon, scale)
    }
}
