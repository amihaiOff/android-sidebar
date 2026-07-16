package com.personal.sidebar.overlay

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Outline
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.Gravity
import android.view.KeyEvent
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import com.personal.sidebar.Edge
import com.personal.sidebar.model.SidebarConfig
import com.personal.sidebar.ui.SidebarPanel

/**
 * A fully transparent window background whose [getOutline] is a rounded rect
 * with the given per-corner [radii]. The window's background blur is clipped to
 * this outline, giving the panel rounded (blurred) corners.
 */
private class RoundedOutlineDrawable(private val radii: FloatArray) : Drawable() {
    override fun draw(canvas: Canvas) { /* transparent */ }

    override fun getOutline(outline: Outline) {
        val b = bounds
        if (b.isEmpty) return
        val path = Path().apply {
            addRoundRect(
                b.left.toFloat(), b.top.toFloat(), b.right.toFloat(), b.bottom.toFloat(),
                radii, Path.Direction.CW,
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            outline.setPath(path)
        } else {
            @Suppress("DEPRECATION")
            outline.setConvexPath(path)
        }
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}

    @Deprecated("Deprecated in Java", ReplaceWith("PixelFormat.TRANSLUCENT"))
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}

/** The panel window spans this fraction of the screen width (capped in dp). */
private const val PANEL_WIDTH_FRACTION = 0.72f
private const val PANEL_MAX_WIDTH_DP = 340

/**
 * Owns the on-demand panel. The panel is hosted in a [Dialog] typed as a system
 * overlay — crucially, a Dialog has a real [android.view.Window], so we can use
 * [android.view.Window.setBackgroundBlurRadius], which blurs the backdrop ONLY
 * within the window's bounds. The window is sized to the panel, so the frosted
 * blur (and every other effect) is confined to the panel — nothing leaks to the
 * rest of the screen. (A plain WindowManager overlay can't do this: its only
 * blur option, FLAG_BLUR_BEHIND, blurs the entire screen behind the window.)
 *
 * Nothing stays mounted while hidden — the Dialog is dismissed on close.
 */
class PanelController(private val context: Context) {

    private var dialog: Dialog? = null
    private var host: OverlayViewHost? = null
    private var dismissTrigger: (() -> Unit)? = null

    val isShowing: Boolean get() = dialog != null

    @SuppressLint("ClickableViewAccessibility")
    fun show(config: SidebarConfig) {
        if (dialog != null) return

        val edge = config.handle.edge
        val newHost = OverlayViewHost()
        val view = ComposeView(context).apply {
            setContent {
                SidebarPanel(
                    edge = edge,
                    items = config.items,
                    panel = config.panel,
                    folder = config.folder,
                    group = config.group,
                    registerDismiss = { dismissTrigger = it },
                    onDismissed = { hide() },
                )
            }
        }
        newHost.attach(view)

        val metrics = context.resources.displayMetrics
        val density = metrics.density
        val width = (metrics.widthPixels * PANEL_WIDTH_FRACTION)
            .toInt()
            .coerceAtMost((PANEL_MAX_WIDTH_DP * density).toInt())

        val d = Dialog(context, android.R.style.Theme_Translucent_NoTitleBar)
        d.setContentView(view)
        d.setCancelable(true)
        d.setCanceledOnTouchOutside(true) // tap outside the panel to dismiss
        d.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                dismissTrigger?.invoke() // animate out; teardown on completion
                true
            } else {
                false
            }
        }
        d.setOnDismissListener {
            host?.detach()
            host = null
            dismissTrigger = null
            dialog = null
        }

        d.window?.apply {
            setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
            setWindowAnimations(0) // Compose drives the slide; no dialog scale/fade
            // Draw edge-to-edge with transparent system bars, so the panel's
            // blur/tint fill the status- and nav-bar regions instead of the
            // dialog painting an opaque (black) bar there.
            WindowCompat.setDecorFitsSystemWindows(this, false)
            statusBarColor = Color.TRANSPARENT
            navigationBarColor = Color.TRANSPARENT
            // Transparent background whose OUTLINE is a proper rounded rect, so
            // the backdrop blur is clipped to rounded corners. (A GradientDrawable
            // with per-corner radii reports only a rectangular outline, which is
            // why the corner setting had no visible effect on the blur.)
            val r = config.panel.cornerDp * density
            val radii = if (edge == Edge.LEFT) {
                floatArrayOf(0f, 0f, r, r, r, r, 0f, 0f)
            } else {
                floatArrayOf(r, r, 0f, 0f, 0f, 0f, r, r)
            }
            setBackgroundDrawable(RoundedOutlineDrawable(radii))
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND) // never dim the rest of the screen
            setDimAmount(0f)
            addFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            )
            val lp = attributes
            lp.width = width
            lp.height = WindowManager.LayoutParams.MATCH_PARENT
            lp.gravity = Gravity.TOP or if (edge == Edge.LEFT) Gravity.START else Gravity.END
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                lp.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                lp.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            attributes = lp
            // Backdrop blur CONFINED to the window bounds (Android 12+).
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                windowManager.isCrossWindowBlurEnabled &&
                config.panel.blurDp > 0
            ) {
                setBackgroundBlurRadius((config.panel.blurDp * density).toInt())
            }
        }

        runCatching { d.show() }
            .onSuccess { dialog = d; host = newHost }
            .onFailure { newHost.detach() }
    }

    fun hide() {
        // Teardown happens in the dialog's dismiss listener.
        runCatching { dialog?.dismiss() }
    }

    private val windowManager: WindowManager
        get() = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
}
