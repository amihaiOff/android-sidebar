package com.personal.sidebar.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.personal.sidebar.Edge
import com.personal.sidebar.apps.AppInfo
import com.personal.sidebar.apps.AppRepository
import com.personal.sidebar.model.FolderConfig
import com.personal.sidebar.model.ItemType
import com.personal.sidebar.model.PanelConfig
import com.personal.sidebar.model.SidebarItem

private val LabelPrimary = Color(0xFFF2F2F7)
private val LabelSecondary = Color(0xFFC7C7CF)

/** Panel tint grey derived from the brightness setting (0 = near-black, 1 = white). */
private fun panelColor(brightness: Float): Color {
    val c = (16 + brightness.coerceIn(0f, 1f) * 239).toInt().coerceIn(0, 255)
    return Color(c, c, c)
}

/**
 * Draws a soft drop shadow that follows the card's rounded corners, extended
 * per side (top/bottom/left/right). The card's own rounded rect is clipped out
 * so a translucent folder isn't darkened underneath — only the outer bleed
 * shows.
 */
internal fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSideShadows(style: FolderConfig) {
    val left = style.shadowLeftDp.dp.toPx()
    val top = style.shadowTopDp.dp.toPx()
    val right = style.shadowRightDp.dp.toPx()
    val bottom = style.shadowBottomDp.dp.toPx()
    if (left <= 0f && top <= 0f && right <= 0f && bottom <= 0f) return

    val w = size.width
    val h = size.height
    val corner = style.cornerDp.dp.toPx()
    val blur = 8.dp.toPx()

    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.argb(150, 0, 0, 0)
        maskFilter = android.graphics.BlurMaskFilter(blur, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }
    val cardPath = android.graphics.Path().apply {
        addRoundRect(0f, 0f, w, h, corner, corner, android.graphics.Path.Direction.CW)
    }
    drawIntoCanvas { canvas ->
        val nc = canvas.nativeCanvas
        val save = nc.save()
        nc.clipOutPath(cardPath) // don't paint under the (possibly translucent) card
        nc.drawRoundRect(-left, -top, w + right, h + bottom, corner, corner, paint)
        nc.restoreToCount(save)
    }
}
private const val COLUMNS = 4

/**
 * Full-screen overlay content: a translucent scrim plus a dark rounded panel
 * that springs in from [edge]. Renders the user's curated [items]: loose apps
 * as an icon grid and folders as expandable dropdown sections. All curation
 * happens in app settings — the panel is view-only. [panel] carries the
 * appearance settings (tint, opacity, background scrim).
 */
@Composable
fun SidebarPanel(
    edge: Edge,
    items: List<SidebarItem>,
    panel: PanelConfig,
    folder: FolderConfig,
    registerDismiss: (() -> Unit) -> Unit,
    onDismissed: () -> Unit,
) {
    val context = LocalContext.current
    val transition = remember { MutableTransitionState(false).apply { targetState = true } }
    val dismiss = remember { { transition.targetState = false } }

    LaunchedEffect(Unit) { registerDismiss(dismiss) }
    LaunchedEffect(transition.currentState, transition.isIdle) {
        if (transition.isIdle && !transition.currentState) onDismissed()
    }

    var appMap by remember { mutableStateOf<Map<String, AppInfo>?>(null) }
    LaunchedEffect(Unit) { appMap = AppRepository.map(context) }

    // The window is already sized to the panel, so the card fills it and slides
    // in from the active edge. No full-screen scrim — the background stays
    // behind the panel only.
    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visibleState = transition,
            modifier = Modifier.fillMaxSize(),
            enter = slideInHorizontally(
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
            ) { full -> if (edge == Edge.LEFT) -full else full } + fadeIn(),
            exit = slideOutHorizontally(
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            ) { full -> if (edge == Edge.LEFT) -full else full } + fadeOut(),
        ) {
            PanelCard(
                edge = edge,
                items = items,
                appMap = appMap,
                panel = panel,
                folder = folder,
            ) { pkg ->
                AppRepository.launch(context, pkg)
                dismiss()
            }
        }
    }
}

@Composable
private fun PanelCard(
    edge: Edge,
    items: List<SidebarItem>,
    appMap: Map<String, AppInfo>?,
    panel: PanelConfig,
    folder: FolderConfig,
    onLaunch: (String) -> Unit,
) {
    val corner = panel.cornerDp.dp
    val shape = if (edge == Edge.LEFT) {
        RoundedCornerShape(topEnd = corner, bottomEnd = corner)
    } else {
        RoundedCornerShape(topStart = corner, bottomStart = corner)
    }
    val systemPadding = WindowInsets.safeDrawing.asPaddingValues()
    // One folder open at a time; when open, everything else dims (focus).
    var openKey by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape),
    ) {
        // Background layer (behind the panel tint), confined to the panel.
        Box(
            Modifier
                .matchParentSize()
                .background(Color(panel.scrimColor).copy(alpha = panel.scrimAlpha.coerceIn(0f, 1f)))
        )
        // Panel tint layer.
        Box(
            Modifier
                .matchParentSize()
                .background(panelColor(panel.brightness).copy(alpha = panel.opacity.coerceIn(0.12f, 1f)))
        )
        // Glass edge — thin white stroke catching the light.
        if (panel.edgeDp > 0f) {
            Box(Modifier.matchParentSize().border(panel.edgeDp.dp, Color.White.copy(alpha = 0.3f), shape))
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(
                    top = systemPadding.calculateTopPadding() + 16.dp,
                    bottom = systemPadding.calculateBottomPadding() + 12.dp,
                    start = 14.dp,
                    end = 14.dp,
                )
        ) {
            when {
                appMap == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LabelPrimary)
                }
                items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No apps yet.\nAdd some in Sidebar settings.",
                        textAlign = TextAlign.Center,
                        color = LabelSecondary,
                    )
                }
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(COLUMNS),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val dimActive = openKey != null
                    items.forEachIndexed { index, entry ->
                        if (entry.type == ItemType.FOLDER) {
                            val fkey = entry.key()
                            val isOpen = openKey == fkey
                            item(span = { GridItemSpan(maxLineSpan) }, key = "f$index") {
                                val dim by animateFloatAsState(if (dimActive && !isOpen) 0.35f else 1f, label = "dim")
                                Box(Modifier.alpha(dim)) {
                                    FolderSection(
                                        folder = entry,
                                        appMap = appMap,
                                        style = folder,
                                        isExpanded = isOpen,
                                        onToggle = { openKey = if (isOpen) null else fkey },
                                        onLaunch = onLaunch,
                                    )
                                }
                            }
                        } else {
                            item(key = "a$index") {
                                val dim by animateFloatAsState(if (dimActive) 0.35f else 1f, label = "dim")
                                val app = entry.packageName?.let { appMap[it] }
                                if (app != null) {
                                    Box(Modifier.alpha(dim)) {
                                        AppTile(
                                            label = app.label,
                                            bitmap = remember(app.packageName) { app.icon.toBitmap(144, 144).asImageBitmap() },
                                        ) { onLaunch(app.packageName) }
                                    }
                                } else {
                                    Box(Modifier.size(1.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderSection(
    folder: SidebarItem,
    appMap: Map<String, AppInfo>,
    style: FolderConfig,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onLaunch: (String) -> Unit,
) {
    // Nested glass: a closer layer than the panel — its own (usually more
    // opaque) tint + a bright edge + per-side drop shadows give it depth.
    val folderTint = panelColor(style.brightness).copy(alpha = style.opacity.coerceIn(0f, 1f))
    val shape = RoundedCornerShape(style.cornerDp.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .drawBehind { drawSideShadows(style) },
        shape = shape,
        color = folderTint,
        shadowElevation = 0.dp, // custom per-side shadows below
        tonalElevation = 0.dp,
        border = if (style.edgeDp > 0f) BorderStroke(style.edgeDp.dp, Color.White.copy(alpha = 0.4f)) else null,
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = folder.name ?: "Folder",
                    color = LabelPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = LabelSecondary,
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                // Manual (non-lazy) grid so it can nest inside the outer grid.
                Column(
                    modifier = Modifier.padding(top = 2.dp, bottom = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    val cols = style.columns.coerceIn(2, 5)
                    val members = folder.packages.mapNotNull { appMap[it] }
                    members.chunked(cols).forEach { rowApps ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            rowApps.forEach { app ->
                                Box(Modifier.weight(1f)) {
                                    AppTile(
                                        label = app.label,
                                        bitmap = remember(app.packageName) { app.icon.toBitmap(144, 144).asImageBitmap() },
                                    ) { onLaunch(app.packageName) }
                                }
                            }
                            repeat(cols - rowApps.size) { Box(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppTile(label: String, bitmap: ImageBitmap, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = label,
            modifier = Modifier.size(50.dp).clip(RoundedCornerShape(12.dp)),
        )
        Text(
            text = label,
            color = LabelPrimary,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 5.dp),
        )
    }
}

/** Stable identity for a folder's expand/collapse state. */
private fun SidebarItem.key(): String = when (type) {
    ItemType.APP -> "app:${packageName}"
    ItemType.FOLDER -> "folder:${name}:${packages.joinToString(",")}"
}
