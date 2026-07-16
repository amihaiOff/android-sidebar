package com.personal.sidebar

import android.os.Build
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Developer "lab" to visualise a glassmorphism card (Method 1: RenderEffect
 * backdrop blur on Android 12+). Tune blur radius, tint alpha and edge stroke
 * live over a colourful background, and read off the Compose snippet.
 */
@Composable
internal fun GlassLabScreen(
    modifier: Modifier,
    initialBlur: Float,
    initialTintAlpha: Float,
    initialStroke: Float,
    onApply: (blur: Float, tintAlpha: Float, stroke: Float) -> Unit,
    onBack: () -> Unit,
) {
    var blur by remember { mutableFloatStateOf(initialBlur) }
    var tintAlpha by remember { mutableFloatStateOf(initialTintAlpha) }
    var stroke by remember { mutableFloatStateOf(initialStroke) }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Text("Glassmorphism lab", style = MaterialTheme.typography.titleLarge)
        }
        Text(
            "Live RenderEffect blur + white tint + edge stroke over a colourful background. " +
                "Glass reads best over high-contrast content — a flat colour makes it invisible.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
        )

        GlassPreview(blur = blur, tintAlpha = tintAlpha, stroke = stroke)

        Spacer(Modifier.height(16.dp))
        LabSlider("Blur radius", blur, 0f..40f, "${blur.roundToInt()} px") { blur = it }
        LabSlider("Tint alpha", tintAlpha, 0f..0.6f, "${(tintAlpha * 100).roundToInt()}%") { tintAlpha = it }
        LabSlider("Edge stroke", stroke, 0f..4f, "${stroke.roundToInt()} dp") { stroke = it }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onApply(blur, tintAlpha, stroke) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Apply to sidebar") }
        Text(
            "Applies as: frost = blur, panel opacity = tint alpha (white tint), edge = stroke.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )

        Spacer(Modifier.height(16.dp))
        CodeReference(blur = blur, tintAlpha = tintAlpha, stroke = stroke)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            Text(
                "Note: live blur needs Android 12+. Below that only the tint + stroke render.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@Composable
private fun GlassPreview(blur: Float, tintAlpha: Float, stroke: Float) {
    val colors = listOf(
        Color(0xFF7C4DFF), Color(0xFF2196F3), Color(0xFF00E5FF),
        Color(0xFF00E676), Color(0xFFFFEB3B), Color(0xFFFF5252),
    )
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(20.dp)),
    ) {
        val density = LocalDensity.current
        val boxWpx = with(density) { maxWidth.toPx() }
        val boxHpx = with(density) { maxHeight.toPx() }
        val shape = RoundedCornerShape(24.dp)

        // Sharp colourful backdrop spanning the whole preview.
        Box(
            Modifier.matchParentSize().background(
                Brush.linearGradient(colors, start = Offset(0f, 0f), end = Offset(boxWpx, boxHpx))
            )
        )

        // The glass card. We know its position within this box, so we draw the
        // SAME gradient inside it — shifted to line up with the backdrop — and
        // blur it. That gives a true frosted-glass slice of what's behind.
        var pos by remember { mutableStateOf(Offset.Zero) }
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.7f)
                .height(150.dp)
                .onGloballyPositioned { pos = it.positionInParent() }
                .clip(shape)
                .border(stroke.dp, Color.White.copy(alpha = 0.35f), shape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .matchParentSize()
                    .then(if (blur > 0f) Modifier.blur(blur.dp) else Modifier)
                    .drawBehind {
                        drawRect(
                            brush = Brush.linearGradient(
                                colors,
                                start = Offset(-pos.x, -pos.y),
                                end = Offset(boxWpx - pos.x, boxHpx - pos.y),
                            ),
                            size = size,
                        )
                    }
            )
            // Frosting tint — pure white at low alpha.
            Box(Modifier.matchParentSize().background(Color.White.copy(alpha = tintAlpha)))
            Text("Glass", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
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

@Composable
private fun CodeReference(blur: Float, tintAlpha: Float, stroke: Float) {
    val snippet = """
        Box(
          Modifier
            .graphicsLayer {
              renderEffect = RenderEffect.createBlurEffect(
                ${blur.roundToInt()}f, ${blur.roundToInt()}f, Shader.TileMode.CLAMP
              ).asComposeRenderEffect()
            }
            .background(Color.White.copy(alpha = ${(tintAlpha * 100).roundToInt()} / 100f))
            .border(${stroke.roundToInt()}.dp, Color.White.copy(alpha = 0.3f), shape)
        )
    """.trimIndent()
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            snippet,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(12.dp),
        )
    }
}
