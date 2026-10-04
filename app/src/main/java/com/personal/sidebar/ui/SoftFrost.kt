package com.personal.sidebar.ui

import android.app.WallpaperManager
import android.content.Context
import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.personal.sidebar.model.SoftFrostConfig
import java.util.function.Consumer
import kotlin.random.Random

/**
 * Whether the system's cross-window (hardware) blur is available right now.
 * Updates live: the OS can toggle it (battery saver etc.), and some OEMs —
 * notably Samsung One UI — never enable it for third-party apps at all.
 */
@Composable
fun rememberHardwareBlurAvailable(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false
    val context = LocalContext.current
    val wm = remember(context) { context.getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    var available by remember { mutableStateOf(wm.isCrossWindowBlurEnabled) }
    DisposableEffect(wm) {
        val listener = Consumer<Boolean> { available = it }
        runCatching { wm.addCrossWindowBlurEnabledListener(listener) }
        onDispose { runCatching { wm.removeCrossWindowBlurEnabledListener(listener) } }
    }
    return available
}

/** Dominant wallpaper colour (no permission needed), or null if unavailable. */
@Composable
fun rememberWallpaperColor(): Color? {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            WallpaperManager.getInstance(context)
                .getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
                ?.primaryColor?.toArgb()
                ?.let { Color(it) }
        }.getOrNull()
    }
}

/** A small tileable grain texture: random black/white specks of random alpha. */
private val grainTile: ImageBitmap by lazy {
    val n = 128
    val rnd = Random(7)
    val px = IntArray(n * n) {
        val a = rnd.nextInt(256)
        val rgb = if (rnd.nextBoolean()) 0xFFFFFF else 0x000000
        (a shl 24) or rgb
    }
    android.graphics.Bitmap.createBitmap(px, n, n, android.graphics.Bitmap.Config.ARGB_8888).asImageBitmap()
}

/**
 * The software "frosted glass" layers drawn over the panel tint when hardware
 * blur is unavailable. It can't see what's behind the panel (that would need
 * screen capture), so it fakes glass with its visual cues instead: a wallpaper-
 * coloured veil, a milky haze, a light sheen, an inner edge glow and fine grain.
 * Callers clip it to the panel shape.
 */
@Composable
fun SoftFrostLayers(cfg: SoftFrostConfig, modifier: Modifier = Modifier) {
    val wallpaper = rememberWallpaperColor()
    Box(modifier) {
        // Wallpaper tint: the panel picks up the colour "behind" it.
        if (wallpaper != null && cfg.wallpaperTint > 0f) {
            Box(Modifier.matchParentSize().background(wallpaper.copy(alpha = cfg.wallpaperTint * 0.5f)))
        }
        // Milky haze.
        if (cfg.haze > 0f) {
            Box(Modifier.matchParentSize().background(Color.White.copy(alpha = cfg.haze)))
        }
        Box(
            Modifier.matchParentSize().drawBehind {
                // Light diffusing through the glass: brighter at the top.
                if (cfg.sheen > 0f) {
                    drawRect(
                        Brush.verticalGradient(
                            0f to Color.White.copy(alpha = cfg.sheen),
                            0.45f to Color.Transparent,
                            1f to Color.White.copy(alpha = cfg.sheen * 0.35f),
                        )
                    )
                }
                // Inner glow along every edge (corners are clipped by the caller).
                if (cfg.glow > 0f) {
                    val w = 22.dp.toPx().coerceAtMost(minOf(size.width, size.height) / 2f)
                    val c = Color.White.copy(alpha = cfg.glow)
                    drawRect(Brush.verticalGradient(listOf(c, Color.Transparent), 0f, w), size = Size(size.width, w))
                    drawRect(
                        Brush.verticalGradient(listOf(Color.Transparent, c), size.height - w, size.height),
                        topLeft = Offset(0f, size.height - w), size = Size(size.width, w),
                    )
                    drawRect(Brush.horizontalGradient(listOf(c, Color.Transparent), 0f, w), size = Size(w, size.height))
                    drawRect(
                        Brush.horizontalGradient(listOf(Color.Transparent, c), size.width - w, size.width),
                        topLeft = Offset(size.width - w, 0f), size = Size(w, size.height),
                    )
                }
                // Fine grain — the main cue that reads as "frosted" rather than "tinted".
                if (cfg.grain > 0f) {
                    drawRect(
                        ShaderBrush(ImageShader(grainTile, TileMode.Repeated, TileMode.Repeated)),
                        alpha = cfg.grain.coerceIn(0f, 1f),
                    )
                }
            }
        )
    }
}
