package com.personal.sidebar.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.personal.sidebar.MainActivity
import com.personal.sidebar.Settings
import com.personal.sidebar.apps.AppRepository

// Pieces shared by every panel design: launching things, and icon rendering.

/**
 * Rendered icon bitmaps, kept for the life of the process so opening the panel
 * doesn't re-render every icon on the main thread (which made the opening
 * animation stutter). The service pre-renders them in the background; cleared
 * with the app cache when apps or the icon theme change.
 */
internal object PanelIcons {
    private val cache = java.util.concurrent.ConcurrentHashMap<String, androidx.compose.ui.graphics.ImageBitmap>()

    /** [app]'s icon at [px]: the system icon, or a [tint] (fg to bg ARGB) themed tile. */
    fun get(app: com.personal.sidebar.apps.AppInfo, px: Int, tint: Pair<Int, Int>?): androidx.compose.ui.graphics.ImageBitmap =
        cache.getOrPut("${app.packageName}:$px:${tint?.first}:${tint?.second}") {
            // A private copy: the service renders these off the main thread,
            // and drawing mutates a drawable's bounds.
            val icon = app.icon.constantState?.newDrawable()?.mutate() ?: app.icon
            val bmp = if (tint != null) {
                tintedIconBitmap(icon, px, tint.first, tint.second)
            } else {
                // The icon exactly as the system (and its icon theme) draws it.
                icon.toBitmap(px, px)
            }
            bmp.prepareToDraw()
            bmp.asImageBitmap()
        }

    fun clear() = cache.clear()
}

/**
 * What a tap in any panel design does. Each action dismisses the panel via
 * [dismiss] after starting its target.
 */
internal class PanelActions(private val context: Context, private val dismiss: () -> Unit) {

    fun launchApp(pkg: String) {
        Settings.addRecent(context, pkg)
        AppRepository.launch(context, pkg)
        dismiss()
    }

    /**
     * Opens [pkg] next to the app on screen (split screen), where the system
     * supports it; otherwise it simply opens.
     */
    fun launchAppSplit(pkg: String) {
        Settings.addRecent(context, pkg)
        context.packageManager.getLaunchIntentForPackage(pkg)?.let { launch ->
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
            runCatching { context.startActivity(launch) }
        }
        dismiss()
    }

    fun openSettings() {
        context.startActivity(
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        dismiss()
    }

    // Open a web link / PWA. Order of preference:
    //  1. an explicit "Open with" target the user picked;
    //  2. an installed web app (WebAPK) that specifically claims this URL — so a
    //     Chrome-installed PWA opens standalone instead of in a browser tab;
    //  3. the system's prefer-non-browser flag; then 4. the default browser.
    fun openLink(url: String, pkg: String?) {
        val uri = Uri.parse(url)
        val base = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        var launched = false
        if (!pkg.isNullOrBlank()) {
            launched = runCatching { context.startActivity(Intent(base).setPackage(pkg)) }.isSuccess
        }
        if (!launched) {
            val webApp = webAppHandlerFor(context, uri)
            if (webApp != null) {
                launched = runCatching { context.startActivity(Intent(base).setPackage(webApp)) }.isSuccess
            }
        }
        if (!launched && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val nonBrowser = Intent(base).addFlags(Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER)
            launched = runCatching { context.startActivity(nonBrowser) }.isSuccess
        }
        if (!launched) runCatching { context.startActivity(base) }
        dismiss()
    }
}

/**
 * Renders an emoji as a themed (monochrome, accent-tinted) tile so emoji link
 * icons match the themed app icons instead of staying full-colour. Same
 * luminance-silhouette treatment used for generated app monochromes.
 */
internal fun themedEmojiBitmap(emoji: String, sizePx: Int, fg: Int, bg: Int): android.graphics.Bitmap {
    val src = android.graphics.Bitmap.createBitmap(sizePx, sizePx, android.graphics.Bitmap.Config.ARGB_8888)
    val sc = android.graphics.Canvas(src)
    val tp = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        textSize = sizePx * 0.72f
        textAlign = android.graphics.Paint.Align.CENTER
    }
    val fm = tp.fontMetrics
    sc.drawText(emoji, sizePx / 2f, sizePx / 2f - (fm.ascent + fm.descent) / 2f, tp)

    val out = android.graphics.Bitmap.createBitmap(sizePx, sizePx, android.graphics.Bitmap.Config.ARGB_8888)
    val c = android.graphics.Canvas(out)
    val radius = sizePx * 0.22f
    val rect = android.graphics.RectF(0f, 0f, sizePx.toFloat(), sizePx.toFloat())
    c.drawRoundRect(rect, radius, radius, android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { color = bg })
    c.clipPath(android.graphics.Path().apply { addRoundRect(rect, radius, radius, android.graphics.Path.Direction.CW) })
    val n = sizePx * sizePx
    val px = IntArray(n)
    src.getPixels(px, 0, sizePx, 0, 0, sizePx, sizePx)
    val fr = (fg ushr 16) and 0xFF
    val fgn = (fg ushr 8) and 0xFF
    val fb = fg and 0xFF
    for (i in 0 until n) {
        val p = px[i]
        val a = (p ushr 24) and 0xFF
        if (a == 0) { px[i] = 0; continue }
        val r = (p ushr 16) and 0xFF
        val g = (p ushr 8) and 0xFF
        val bl = p and 0xFF
        val lum = (0.299 * r + 0.587 * g + 0.114 * bl) / 255.0
        val outA = (a * (0.25 + 0.75 * lum)).toInt().coerceIn(0, 255)
        px[i] = (outA shl 24) or (fr shl 16) or (fgn shl 8) or fb
    }
    src.setPixels(px, 0, sizePx, 0, 0, sizePx, sizePx)
    c.drawBitmap(src, 0f, 0f, null)
    return out
}


/**
 * Renders an app icon tinted to the system theme (a themed icon). Uses the
 * adaptive icon's real monochrome layer when the app ships one (Android 13+);
 * otherwise *generates* a monochrome from the icon's own artwork (luminance
 * silhouette) so every icon recolours uniformly, not just the ones with a
 * monochrome layer. Returns null only when the OS is too old for the palette.
 */
internal fun themedIconBitmap(src: android.graphics.drawable.Drawable, sizePx: Int, fg: Int, bg: Int): android.graphics.Bitmap? {
    if (android.os.Build.VERSION.SDK_INT < 31) return null
    return tintedIconBitmap(src, sizePx, fg, bg)
}

/**
 * [themedIconBitmap] with fixed colours: a [fg]-coloured monochrome of [src] on a
 * rounded [bg] tile. Works on any API level (the real monochrome layer is only
 * used on Android 13+).
 */
internal fun tintedIconBitmap(src: android.graphics.drawable.Drawable, sizePx: Int, fg: Int, bg: Int): android.graphics.Bitmap {
    val bmp = android.graphics.Bitmap.createBitmap(sizePx, sizePx, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bmp)
    val radius = sizePx * 0.22f
    val rect = android.graphics.RectF(0f, 0f, sizePx.toFloat(), sizePx.toFloat())
    canvas.drawRoundRect(rect, radius, radius, android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { color = bg })
    val clip = android.graphics.Path().apply { addRoundRect(rect, radius, radius, android.graphics.Path.Direction.CW) }
    canvas.clipPath(clip)

    val adaptive = src as? android.graphics.drawable.AdaptiveIconDrawable
    val realMono = if (android.os.Build.VERSION.SDK_INT >= 33) adaptive?.monochrome else null
    val bleed = (sizePx * 0.25f).toInt() // adaptive layers are 1.5x the visible mask
    if (realMono != null) {
        realMono.mutate()
        realMono.setTint(fg)
        realMono.setBounds(-bleed, -bleed, sizePx + bleed, sizePx + bleed)
        realMono.draw(canvas)
    } else {
        // Generate a monochrome from the icon's foreground (or the whole icon).
        val layer = adaptive?.foreground ?: src
        val tmp = android.graphics.Bitmap.createBitmap(sizePx, sizePx, android.graphics.Bitmap.Config.ARGB_8888)
        val tc = android.graphics.Canvas(tmp)
        val b = if (adaptive != null) bleed else 0
        layer.setBounds(-b, -b, sizePx + b, sizePx + b)
        layer.draw(tc)
        val n = sizePx * sizePx
        val px = IntArray(n)
        tmp.getPixels(px, 0, sizePx, 0, 0, sizePx, sizePx)
        val fr = (fg ushr 16) and 0xFF
        val fgn = (fg ushr 8) and 0xFF
        val fb = fg and 0xFF
        for (i in 0 until n) {
            val p = px[i]
            val a = (p ushr 24) and 0xFF
            if (a == 0) { px[i] = 0; continue }
            val r = (p ushr 16) and 0xFF
            val g = (p ushr 8) and 0xFF
            val bl = p and 0xFF
            val lum = (0.299 * r + 0.587 * g + 0.114 * bl) / 255.0
            // Keep the icon's shape (alpha) while letting brighter areas read
            // stronger, so detail survives instead of a flat blob.
            val outA = (a * (0.25 + 0.75 * lum)).toInt().coerceIn(0, 255)
            px[i] = (outA shl 24) or (fr shl 16) or (fgn shl 8) or fb
        }
        tmp.setPixels(px, 0, sizePx, 0, 0, sizePx, sizePx)
        canvas.drawBitmap(tmp, 0f, 0f, null)
    }
    return bmp
}

/**
 * Finds an installed *web app* (a WebAPK / PWA) whose package specifically claims
 * [uri], as opposed to a general-purpose browser. Returns its package name, or
 * null if only browsers (or nothing) handle it. Works by diffing the handlers of
 * the real URL against the handlers of a host no app would claim — whatever's
 * left over is a URL-specific handler, i.e. the installed web app.
 */
internal fun webAppHandlerFor(context: android.content.Context, uri: android.net.Uri): String? {
    val pm = context.packageManager
    val self = context.packageName
    fun handlers(u: android.net.Uri): Set<String> = runCatching {
        pm.queryIntentActivities(
            android.content.Intent(android.content.Intent.ACTION_VIEW, u)
                .addCategory(android.content.Intent.CATEGORY_BROWSABLE),
            0,
        ).mapNotNull { it.activityInfo?.packageName }.toSet()
    }.getOrDefault(emptySet())

    val specific = handlers(uri)
    if (specific.isEmpty()) return null
    val browsers = handlers(android.net.Uri.parse("https://no-app-claims-this-host.example/"))
    return (specific - browsers - self).firstOrNull()
}
