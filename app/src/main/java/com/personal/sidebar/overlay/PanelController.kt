package com.personal.sidebar.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.KeyEvent
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import com.personal.sidebar.Edge
import com.personal.sidebar.ui.SidebarPanel

/**
 * Owns the on-demand panel window. [show] inflates a Compose overlay (with a
 * full [OverlayViewHost] behind it); dismissal animates out and then [hide]
 * removes the window entirely — nothing stays mounted while hidden, which is the
 * core battery guarantee.
 */
class PanelController(private val context: Context) {

    private val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var composeView: ComposeView? = null
    private var host: OverlayViewHost? = null
    private var dismissTrigger: (() -> Unit)? = null

    val isShowing: Boolean get() = composeView != null

    fun show(edge: Edge) {
        if (composeView != null) return

        val newHost = OverlayViewHost()
        val view = ComposeView(context).apply {
            setContent {
                SidebarPanel(
                    edge = edge,
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
        }

        // Wire lifecycle/owners BEFORE the view attaches to the window.
        newHost.attach(view)
        windowManager.addView(view, panelParams())
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

    private fun panelParams(): WindowManager.LayoutParams {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            // No FLAG_NOT_FOCUSABLE: the panel is modal and needs key focus.
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        )
        params.gravity = Gravity.TOP or Gravity.START
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        return params
    }
}
