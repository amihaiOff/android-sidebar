package com.personal.sidebar.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import com.personal.sidebar.Edge
import com.personal.sidebar.model.SidebarConfig
import com.personal.sidebar.ui.SidebarPanel

/** The panel window spans this fraction of the screen width (capped in dp), so
 *  it's clearly a side panel with a gap beside it — and the frost/tint only
 *  affect that strip, never the whole screen. */
private const val PANEL_WIDTH_FRACTION = 0.72f
private const val PANEL_MAX_WIDTH_DP = 340

/**
 * Owns the on-demand panel window. [show] inflates a Compose overlay (with a
 * full [OverlayViewHost] behind it); dismissal animates out and then [hide]
 * removes the window entirely — nothing stays mounted while hidden, which is the
 * core battery guarantee. The window is only as wide as the panel and pinned to
 * the active edge, so the backdrop blur and tint are confined to the panel. It's
 * touch-modal (taps elsewhere don't leak to apps behind) and dismisses on an
 * outside tap or the back gesture.
 */
class PanelController(private val context: Context) {

    private val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var composeView: ComposeView? = null
    private var host: OverlayViewHost? = null
    private var dismissTrigger: (() -> Unit)? = null

    val isShowing: Boolean get() = composeView != null

    @SuppressLint("ClickableViewAccessibility")
    fun show(config: SidebarConfig) {
        if (composeView != null) return

        val newHost = OverlayViewHost()
        val view = ComposeView(context).apply {
            setContent {
                SidebarPanel(
                    edge = config.handle.edge,
                    items = config.items,
                    panel = config.panel,
                    registerDismiss = { dismissTrigger = it },
                    onDismissed = { hide() },
                )
            }
            // Focusable so the hardware/gesture back press reaches us.
            isFocusableInTouchMode = true
            setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    dismissTrigger?.invoke()
                    true
                } else {
                    false
                }
            }
            // Tap anywhere outside the (panel-sized) window → dismiss.
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_OUTSIDE) {
                    dismissTrigger?.invoke()
                    true
                } else {
                    false
                }
            }
        }

        // Wire lifecycle/owners BEFORE the view attaches to the window.
        newHost.attach(view)
        windowManager.addView(view, panelParams(config.handle.edge, config.panel.blurDp))
        view.requestFocus()

        composeView = view
        host = newHost
    }

    fun hide() {
        val view = composeView ?: return
        composeView = null
        dismissTrigger = null
        host?.detach()
        host = null
        runCatching { windowManager.removeView(view) }
    }

    private fun panelParams(edge: Edge, blurDp: Int): WindowManager.LayoutParams {
        val metrics = context.resources.displayMetrics
        val density = metrics.density
        val width = (metrics.widthPixels * PANEL_WIDTH_FRACTION)
            .toInt()
            .coerceAtMost((PANEL_MAX_WIDTH_DP * density).toInt())
        val params = WindowManager.LayoutParams(
            width,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            // Focusable (back key) + touch-modal so outside taps don't reach apps
            // behind; FLAG_WATCH_OUTSIDE_TOUCH delivers ACTION_OUTSIDE so we can
            // dismiss on an outside tap.
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT,
        )
        params.gravity = Gravity.TOP or if (edge == Edge.LEFT) Gravity.START else Gravity.END
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        // Real backdrop blur (frosted glass) on Android 12+, when the device
        // supports cross-window blur. Confined to this (panel-sized) window.
        if (blurDp > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && windowManager.isCrossWindowBlurEnabled) {
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
            params.blurBehindRadius = (blurDp * density).toInt()
        }
        return params
    }
}
