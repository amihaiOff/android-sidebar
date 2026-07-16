package com.personal.sidebar

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.personal.sidebar.model.PanelConfig
import kotlin.math.roundToInt

private val LAB_SWATCHES = listOf(
    0xFF000000.toInt(), 0xFF1A237E.toInt(), 0xFF004D40.toInt(), 0xFF3E2723.toInt(),
    0xFF4C5BD4.toInt(), 0xFF2196F3.toInt(), 0xFF9C27B0.toInt(), 0xFFFFFFFF.toInt(),
)

/** Panel tint grey from the brightness setting (0 = near-black, 1 = white). */
private fun labTint(brightness: Float): Color {
    val c = (16 + brightness.coerceIn(0f, 1f) * 239).toInt().coerceIn(0, 255)
    return Color(c, c, c)
}

private fun labWithRgb(argb: Int, rgb: Int): Int =
    (argb.toLong() and 0xFF000000L).toInt() or (rgb and 0x00FFFFFF)

/**
 * The single place to tune the frosted-glass panel. Every control edits a live
 * [PanelConfig] reflected in the preview; changes are committed on back.
 */
@Composable
internal fun GlassLabScreen(
    modifier: Modifier,
    panel: PanelConfig,
    onCommit: (PanelConfig) -> Unit,
    onBack: () -> Unit,
) {
    var p by remember { mutableStateOf(panel) }
    fun done() { onCommit(p); onBack() }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { done() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Text("Panel & glass", style = MaterialTheme.typography.titleLarge)
        }
        Text(
            "Everything that controls the panel's frosted-glass look lives here. " +
                "The preview updates live and is applied when you go back.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
        )

        GlassPreview(p)

        Spacer(Modifier.height(16.dp))
        LabSlider("Frost (blur)", p.blurDp.toFloat(), 0f..80f, "${p.blurDp} dp") { p = p.copy(blurDp = it.toInt()) }
        LabSlider("Tint opacity", p.opacity, 0.1f..1f, "${(p.opacity * 100).roundToInt()}%") { p = p.copy(opacity = it) }
        LabSlider("Brightness", p.brightness, 0f..1f, "${(p.brightness * 100).roundToInt()}%") { p = p.copy(brightness = it) }
        LabSlider("Edge stroke", p.edgeDp, 0f..4f, "${p.edgeDp.roundToInt()} dp") { p = p.copy(edgeDp = it) }

        Spacer(Modifier.height(12.dp))
        Text("Background", style = MaterialTheme.typography.labelLarge)
        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LAB_SWATCHES.forEach { rgb ->
                ColorDot(
                    rgb = rgb,
                    selected = (p.scrimColor and 0x00FFFFFF) == (rgb and 0x00FFFFFF),
                    onClick = { p = p.copy(scrimColor = labWithRgb(p.scrimColor, rgb)) },
                )
            }
        }
        LabSlider("Background dim", p.scrimAlpha, 0f..0.85f, "${(p.scrimAlpha * 100).roundToInt()}%") { p = p.copy(scrimAlpha = it) }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            Text(
                "Note: live blur needs Android 12+. Below that only tint + edge render.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@Composable
private fun GlassPreview(p: PanelConfig) {
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(20.dp)),
    ) {
        val density = LocalDensity.current
        val boxWpx = with(density) { maxWidth.toPx() }
        val boxHpx = with(density) { maxHeight.toPx() }
        val shape = RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp)
        val previewBlur = minOf(p.blurDp, 40)

        // Muted "wallpaper" behind the panel.
        Box(Modifier.matchParentSize().drawBehind { drawScene(Offset.Zero, boxWpx, boxHpx) })

        var pos by remember { mutableStateOf(Offset.Zero) }
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxWidth(0.62f)
                .fillMaxSize()
                .onGloballyPositioned { pos = it.positionInParent() }
                .clip(shape)
                .border(p.edgeDp.dp, Color.White.copy(alpha = 0.3f), shape),
        ) {
            // Blurred wallpaper slice.
            Box(
                Modifier
                    .matchParentSize()
                    .then(if (previewBlur > 0) Modifier.blur(previewBlur.dp) else Modifier)
                    .drawBehind { drawScene(pos, boxWpx, boxHpx) }
            )
            // Background scrim, then the panel tint.
            Box(Modifier.matchParentSize().background(Color(p.scrimColor).copy(alpha = p.scrimAlpha.coerceIn(0f, 1f))))
            Box(Modifier.matchParentSize().background(labTint(p.brightness).copy(alpha = p.opacity.coerceIn(0.1f, 1f))))

            Column(
                Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PreviewFolder(p)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(3) { AppPlaceholder() }
                }
            }
        }
    }
}

/** A mock of the nested-glass folder inside the preview. */
@Composable
private fun PreviewFolder(p: PanelConfig) {
    val folderTint = labTint(p.brightness).copy(alpha = (p.opacity + 0.28f).coerceIn(0.4f, 1f))
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = folderTint,
        shadowElevation = 12.dp,
        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier
                    .size(width = 54.dp, height = 6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.5f))
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { AppPlaceholder() }
            }
        }
    }
}

private fun DrawScope.drawScene(origin: Offset, wpx: Float, hpx: Float) {
    drawRect(
        brush = Brush.linearGradient(
            listOf(Color(0xFF1F2933), Color(0xFF323D46), Color(0xFF29343C)),
            start = Offset(0f, 0f) - origin,
            end = Offset(wpx, hpx) - origin,
        ),
        size = size,
    )
    drawCircle(Color(0x554FC3F7), radius = 70.dp.toPx(), center = Offset(0.22f * wpx, 0.30f * hpx) - origin)
    drawCircle(Color(0x55FF8A65), radius = 92.dp.toPx(), center = Offset(0.74f * wpx, 0.38f * hpx) - origin)
    drawCircle(Color(0x5581C784), radius = 80.dp.toPx(), center = Offset(0.48f * wpx, 0.84f * hpx) - origin)
}

@Composable
private fun AppPlaceholder() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.3f))
        )
        Spacer(Modifier.height(5.dp))
        Box(
            Modifier
                .size(width = 24.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.45f))
        )
    }
}

@Composable
private fun ColorDot(rgb: Int, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(32.dp),
        shape = RoundedCornerShape(50),
        color = Color(rgb or 0xFF000000.toInt()),
        border = if (selected) BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface) else BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
        onClick = onClick,
    ) {}
}

@Composable
private fun LabSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    valueLabel: String,
    onChange: (Float) -> Unit,
) {
    Column(Modifier.padding(top = 6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(valueLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Slider(value = value.coerceIn(range.start, range.endInclusive), onValueChange = onChange, valueRange = range)
    }
}
