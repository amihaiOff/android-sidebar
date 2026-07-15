package com.personal.sidebar.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import com.personal.sidebar.Edge
import kotlin.math.hypot

/**
 * The always-present but idle trigger: a thin translucent pill hugging the
 * screen edge. It is a single tiny window that renders once and does nothing
 * until touched — no polling, no timers, negligible battery. A tap or an inward
 * swipe fires [onTrigger]; touches outside the strip pass through to whatever is
 * underneath.
 */
class EdgeHandle(
    private val context: Context,
    private val onTrigger: () -> Unit,
) {
    private val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var view: View? = null

    @SuppressLint("ClickableViewAccessibility")
    fun show(edge: Edge) {
        hide()
        val density = context.resources.displayMetrics.density
        val widthPx = (22 * density).toInt()
        val heightPx = (150 * density).toInt()
        val thresholdPx = 24 * density
        val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

        val handle = View(context).apply {
            background = pill(edge, density)
            alpha = 0.9f
        }

        var downX = 0f
        var downY = 0f
        var downTime = 0L
        var triggered = false
        handle.setOnTouchListener { _, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = e.rawX; downY = e.rawY; downTime = e.eventTime; triggered = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!triggered) {
                        val inward = if (edge == Edge.LEFT) e.rawX - downX else downX - e.rawX
                        if (inward > thresholdPx) {
                            triggered = true
                            onTrigger()
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!triggered) {
                        val dist = hypot(e.rawX - downX, e.rawY - downY)
                        val duration = e.eventTime - downTime
                        if (dist < touchSlop && duration < 250) onTrigger() // treat as a tap
                    }
                    true
                }
                else -> false
            }
        }

        val params = WindowManager.LayoutParams(
            widthPx,
            heightPx,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.CENTER_VERTICAL or
                if (edge == Edge.LEFT) Gravity.START else Gravity.END
        }

        windowManager.addView(handle, params)
        view = handle
    }

    fun hide() {
        view?.let { runCatching { windowManager.removeView(it) } }
        view = null
    }

    /** A rounded tab, flush to the edge and rounded on the inner side. */
    private fun pill(edge: Edge, density: Float): GradientDrawable {
        val r = 14 * density
        val radii = if (edge == Edge.LEFT) {
            // round top-right & bottom-right
            floatArrayOf(0f, 0f, r, r, r, r, 0f, 0f)
        } else {
            // round top-left & bottom-left
            floatArrayOf(r, r, 0f, 0f, 0f, 0f, r, r)
        }
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = radii
            setColor(Color.argb(140, 76, 91, 212)) // translucent indigo
        }
    }
}
