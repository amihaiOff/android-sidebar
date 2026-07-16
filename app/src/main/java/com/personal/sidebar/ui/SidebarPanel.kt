package com.personal.sidebar.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
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
internal fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSideShadows(style: FolderConfig, cornerPx: Float) {
    val left = style.shadowLeftDp.dp.toPx()
    val top = style.shadowTopDp.dp.toPx()
    val right = style.shadowRightDp.dp.toPx()
    val bottom = style.shadowBottomDp.dp.toPx()
    if (left <= 0f && top <= 0f && right <= 0f && bottom <= 0f) return

    val w = size.width
    val h = size.height
    val corner = cornerPx
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
                else -> PanelContent(
                    apps = items.filter { it.type == ItemType.APP },
                    folders = items.filter { it.type == ItemType.FOLDER },
                    appMap = appMap,
                    style = folder,
                    onLaunch = onLaunch,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PanelContent(
    apps: List<SidebarItem>,
    folders: List<SidebarItem>,
    appMap: Map<String, AppInfo>,
    style: FolderConfig,
    onLaunch: (String) -> Unit,
) {
    var openKey by remember { mutableStateOf<String?>(null) }
    var pivotX by remember { mutableStateOf(0.5f) }
    var widthPx by remember { mutableStateOf(1f) }
    val centers = remember { mutableStateMapOf<String, Float>() }
    val dimActive = openKey != null

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .onSizeChanged { widthPx = it.width.toFloat().coerceAtLeast(1f) },
    ) {
        // Loose apps grid.
        if (apps.isNotEmpty()) {
            val dim by animateFloatAsState(if (dimActive) 0.35f else 1f, label = "appsDim")
            AppGrid(apps, COLUMNS, appMap, Modifier.alpha(dim), onLaunch)
        }

        if (folders.isNotEmpty()) {
            if (apps.isNotEmpty()) Spacer(Modifier.height(14.dp))

            // Row of folder circles (wraps if there are many).
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                folders.forEach { f ->
                    val key = f.key()
                    val isOpen = openKey == key
                    val dim by animateFloatAsState(if (dimActive && !isOpen) 0.4f else 1f, label = "circleDim")
                    FolderCircle(
                        folder = f,
                        style = style,
                        selected = isOpen,
                        modifier = Modifier
                            .alpha(dim)
                            .onGloballyPositioned { c ->
                                centers[key] = c.positionInParent().x + c.size.width / 2f
                            },
                        onClick = {
                            pivotX = ((centers[key] ?: (widthPx / 2f)) / widthPx).coerceIn(0f, 1f)
                            openKey = if (isOpen) null else key
                        },
                    )
                }
            }

            // The "line" under the circles; folders open below it.
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.15f))
            )

            val open = folders.firstOrNull { it.key() == openKey }
            var shown by remember { mutableStateOf<SidebarItem?>(null) }
            LaunchedEffect(openKey) { if (open != null) shown = open }
            AnimatedVisibility(
                visible = open != null,
                enter = fadeIn() + scaleIn(transformOrigin = TransformOrigin(pivotX, 0f), initialScale = 0.5f) +
                    expandVertically(expandFrom = Alignment.Top),
                exit = fadeOut() + scaleOut(transformOrigin = TransformOrigin(pivotX, 0f), targetScale = 0.5f) +
                    shrinkVertically(shrinkTowards = Alignment.Top),
            ) {
                shown?.let { FolderExpanded(it, style, appMap, onLaunch) }
            }
        }
    }
}

/** A folder as a glass circle showing its emoji, with a label beneath. */
@Composable
private fun FolderCircle(
    folder: SidebarItem,
    style: FolderConfig,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val tint = panelColor(style.brightness).copy(alpha = style.opacity.coerceIn(0f, 1f))
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .drawBehind { drawSideShadows(style, size.minDimension / 2f) }
                .clip(CircleShape)
                .background(tint)
                .then(
                    if (style.edgeDp > 0f) {
                        Modifier.border(style.edgeDp.dp, Color.White.copy(alpha = if (selected) 0.75f else 0.4f), CircleShape)
                    } else {
                        Modifier
                    }
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            val glyph = folder.emoji?.takeIf { it.isNotBlank() } ?: folder.name?.take(1) ?: "📁"
            Text(glyph, fontSize = 26.sp)
        }
        if (!folder.name.isNullOrBlank()) {
            Text(
                folder.name,
                color = LabelSecondary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp).width(58.dp),
            )
        }
    }
}

/** The open folder's apps, in a nested-glass card below the line. */
@Composable
private fun FolderExpanded(
    folder: SidebarItem,
    style: FolderConfig,
    appMap: Map<String, AppInfo>,
    onLaunch: (String) -> Unit,
) {
    val tint = panelColor(style.brightness).copy(alpha = style.opacity.coerceIn(0f, 1f))
    val shape = RoundedCornerShape(style.cornerDp.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .drawBehind { drawSideShadows(style, style.cornerDp.dp.toPx()) },
        shape = shape,
        color = tint,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        border = if (style.edgeDp > 0f) BorderStroke(style.edgeDp.dp, Color.White.copy(alpha = 0.4f)) else null,
    ) {
        Column(Modifier.padding(10.dp)) {
            AppGrid(
                apps = folder.packages.map { SidebarItem.app(it) },
                cols = style.columns.coerceIn(2, 5),
                appMap = appMap,
                modifier = Modifier,
                onLaunch = onLaunch,
            )
        }
    }
}

/** A non-lazy icon grid used for both loose apps and folder contents. */
@Composable
private fun AppGrid(
    apps: List<SidebarItem>,
    cols: Int,
    appMap: Map<String, AppInfo>,
    modifier: Modifier,
    onLaunch: (String) -> Unit,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        apps.chunked(cols).forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                rowItems.forEach { item ->
                    Box(Modifier.weight(1f)) {
                        val app = item.packageName?.let { appMap[it] }
                        if (app != null) {
                            AppTile(
                                label = app.label,
                                bitmap = remember(app.packageName) { app.icon.toBitmap(144, 144).asImageBitmap() },
                            ) { onLaunch(app.packageName) }
                        }
                    }
                }
                repeat(cols - rowItems.size) { Box(Modifier.weight(1f)) }
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
