package com.personal.sidebar

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.sidebar.model.FolderConfig
import com.personal.sidebar.model.PanelConfig
import com.personal.sidebar.ui.drawSideShadows
import kotlin.math.roundToInt

private val LAB_SWATCHES = listOf(
    0xFF000000.toInt(), 0xFF1A237E.toInt(), 0xFF004D40.toInt(), 0xFF3E2723.toInt(),
    0xFF4C5BD4.toInt(), 0xFF2196F3.toInt(), 0xFF9C27B0.toInt(), 0xFFFFFFFF.toInt(),
)

/** Grey tint from a brightness setting (0 = near-black, 1 = white). */
private fun labTint(brightness: Float): Color {
    val c = (16 + brightness.coerceIn(0f, 1f) * 239).toInt().coerceIn(0, 255)
    return Color(c, c, c)
}

private fun labWithRgb(argb: Int, rgb: Int): Int =
    (argb.toLong() and 0xFF000000L).toInt() or (rgb and 0x00FFFFFF)

/**
 * The single place to tune the panel + folder look. Every control edits a live
 * [PanelConfig] / [FolderConfig] reflected in the preview; committed on back.
 */
@Composable
internal fun GlassLabScreen(
    modifier: Modifier,
    panel: PanelConfig,
    folder: FolderConfig,
    onChange: (PanelConfig, FolderConfig) -> Unit,
    onBack: () -> Unit,
) {
    var p by remember { mutableStateOf(panel) }
    var f by remember { mutableStateOf(folder) }
    // Apply every change immediately (persisted live), so it takes effect right
    // away and is never lost by leaving the screen without going back.
    fun setP(np: PanelConfig) { p = np; onChange(np, f) }
    fun setF(nf: FolderConfig) { f = nf; onChange(p, nf) }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Text("Panel & glass", style = MaterialTheme.typography.titleLarge)
        }
        Text(
            "Everything that controls the panel and folder look lives here. Changes " +
                "apply immediately and the preview updates live. Effects stay inside " +
                "the panel — nothing touches the rest of the screen.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
        )

        GlassPreview(p, f)

        Spacer(Modifier.height(16.dp))
        SectionLabel("Panel")
        LabSlider("Frost (blur)", p.blurDp.toFloat(), 0f..80f, "${p.blurDp} dp") { setP(p.copy(blurDp = it.toInt())) }
        LabSlider("Tint opacity", p.opacity, 0.1f..1f, "${(p.opacity * 100).roundToInt()}%") { setP(p.copy(opacity = it)) }
        LabSlider("Brightness", p.brightness, 0f..1f, "${(p.brightness * 100).roundToInt()}%") { setP(p.copy(brightness = it)) }
        LabSlider("Edge stroke", p.edgeDp, 0f..4f, "${p.edgeDp.roundToInt()} dp") { setP(p.copy(edgeDp = it)) }
        LabSlider("Corner radius", p.cornerDp.toFloat(), 0f..48f, "${p.cornerDp} dp") { setP(p.copy(cornerDp = it.roundToInt())) }
        ToggleRow("Show app names", p.showLabels) { setP(p.copy(showLabels = it)) }
        Text("Background", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LAB_SWATCHES.forEach { rgb ->
                ColorDot(
                    rgb = rgb,
                    selected = (p.scrimColor and 0x00FFFFFF) == (rgb and 0x00FFFFFF),
                    onClick = { setP(p.copy(scrimColor = labWithRgb(p.scrimColor, rgb))) },
                )
            }
        }
        LabSlider("Background dim", p.scrimAlpha, 0f..0.85f, "${(p.scrimAlpha * 100).roundToInt()}%") { setP(p.copy(scrimAlpha = it)) }

        Spacer(Modifier.height(16.dp))
        SectionLabel("Folder")
        LabSlider("Folder opacity", f.opacity, 0.2f..1f, "${(f.opacity * 100).roundToInt()}%") { setF(f.copy(opacity = it)) }
        LabSlider("Folder brightness", f.brightness, 0f..1f, "${(f.brightness * 100).roundToInt()}%") { setF(f.copy(brightness = it)) }
        LabSlider("Folder edge", f.edgeDp, 0f..4f, "${f.edgeDp.roundToInt()} dp") { setF(f.copy(edgeDp = it)) }
        LabSlider("Folder columns", f.columns.toFloat(), 2f..5f, "${f.columns}") { setF(f.copy(columns = it.roundToInt())) }
        LabSlider("Folder corners", f.cornerDp.toFloat(), 0f..32f, "${f.cornerDp} dp") { setF(f.copy(cornerDp = it.roundToInt())) }
        Text("Folder shadow", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
        LabSlider("Shadow top", f.shadowTopDp, 0f..30f, "${f.shadowTopDp.roundToInt()} dp") { setF(f.copy(shadowTopDp = it)) }
        LabSlider("Shadow bottom", f.shadowBottomDp, 0f..30f, "${f.shadowBottomDp.roundToInt()} dp") { setF(f.copy(shadowBottomDp = it)) }
        LabSlider("Shadow left", f.shadowLeftDp, 0f..30f, "${f.shadowLeftDp.roundToInt()} dp") { setF(f.copy(shadowLeftDp = it)) }
        LabSlider("Shadow right", f.shadowRightDp, 0f..30f, "${f.shadowRightDp.roundToInt()} dp") { setF(f.copy(shadowRightDp = it)) }
    }
}

@Composable
private fun GlassPreview(p: PanelConfig, f: FolderConfig) {
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(20.dp)),
    ) {
        val density = LocalDensity.current
        val boxWpx = with(density) { maxWidth.toPx() }
        val boxHpx = with(density) { maxHeight.toPx() }
        val shape = RoundedCornerShape(topStart = p.cornerDp.dp, bottomStart = p.cornerDp.dp)
        val previewBlur = minOf(p.blurDp, 40)

        // Muted "wallpaper" behind the panel. Stays sharp outside the panel —
        // the blur is confined to the panel region, exactly like the real overlay.
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
            // Blurred wallpaper slice aligned under the panel (frosted backdrop).
            Box(
                Modifier
                    .matchParentSize()
                    .then(if (previewBlur > 0) Modifier.blur(previewBlur.dp) else Modifier)
                    .drawBehind { drawScene(pos, boxWpx, boxHpx) }
            )
            // Background scrim, then the translucent panel tint.
            Box(Modifier.matchParentSize().background(Color(p.scrimColor).copy(alpha = p.scrimAlpha.coerceIn(0f, 1f))))
            Box(Modifier.matchParentSize().background(labTint(p.brightness).copy(alpha = p.opacity.coerceIn(0.1f, 1f))))

            Column(
                Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Folder circles row (centered; the first is "open").
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally)) {
                    PreviewFolderCircle(f, "📁", selected = true)
                    PreviewFolderCircle(f, "🎮", selected = false)
                    PreviewFolderCircle(f, "🎵", selected = false)
                }
                // Open folder contents, directly below (no divider).
                PreviewFolderExpanded(f)
                Spacer(Modifier.height(18.dp))
                // Loose apps row.
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(4) { AppPlaceholder(showLabel = p.showLabels) }
                }
            }
        }
    }
}

@Composable
private fun PreviewFolderCircle(f: FolderConfig, emoji: String, selected: Boolean) {
    val tint = labTint(f.brightness).copy(alpha = f.opacity.coerceIn(0f, 1f))
    val elevation = (maxOf(f.shadowTopDp, f.shadowBottomDp, f.shadowLeftDp, f.shadowRightDp) * 0.6f)
        .coerceIn(0f, 16f).dp
    Surface(
        modifier = Modifier.size(52.dp),
        shape = CircleShape,
        color = tint,
        shadowElevation = elevation,
        border = if (f.edgeDp > 0f) BorderStroke(f.edgeDp.dp, Color.White.copy(alpha = if (selected) 0.75f else 0.4f)) else null,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(emoji, fontSize = 24.sp)
        }
    }
}

@Composable
private fun PreviewFolderExpanded(f: FolderConfig) {
    val tint = labTint(f.brightness).copy(alpha = f.opacity.coerceIn(0f, 1f))
    val shape = RoundedCornerShape(f.cornerDp.dp)
    Surface(
        shape = shape,
        color = tint,
        shadowElevation = 0.dp,
        border = if (f.edgeDp > 0f) BorderStroke(f.edgeDp.dp, Color.White.copy(alpha = 0.4f)) else null,
        modifier = Modifier.fillMaxWidth().drawBehind { drawSideShadows(f, f.cornerDp.dp.toPx()) },
    ) {
        Column(Modifier.padding(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(f.columns.coerceIn(2, 5)) { AppPlaceholder() }
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
private fun AppPlaceholder(showLabel: Boolean = true) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.3f))
        )
        if (showLabel) {
            Spacer(Modifier.height(5.dp))
            Box(
                Modifier
                    .size(width = 24.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.45f))
            )
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 2.dp))
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
