package com.personal.sidebar.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.withContext
import com.personal.sidebar.Edge
import com.personal.sidebar.MainActivity
import com.personal.sidebar.Settings
import com.personal.sidebar.apps.AppInfo
import com.personal.sidebar.apps.AppRepository
import com.personal.sidebar.model.FolderConfig
import com.personal.sidebar.model.GroupConfig
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
/**
 * Draws a soft *inner* shadow hugging the inside of the card's rounded edge, so
 * the card reads as sunken into the panel. A blurred stroke is drawn along the
 * edge and clipped to the card's interior, leaving only the inward bleed.
 */
internal fun androidx.compose.ui.graphics.drawscope.DrawScope.drawInnerShadow(cornerPx: Float, depthDp: Float) {
    val depth = depthDp.dp.toPx()
    if (depth <= 0f) return
    val w = size.width
    val h = size.height
    val path = android.graphics.Path().apply {
        addRoundRect(0f, 0f, w, h, cornerPx, cornerPx, android.graphics.Path.Direction.CW)
    }
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = depth
        color = android.graphics.Color.argb(150, 0, 0, 0)
        maskFilter = android.graphics.BlurMaskFilter(depth, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }
    drawIntoCanvas { canvas ->
        val nc = canvas.nativeCanvas
        val save = nc.save()
        nc.clipPath(path) // keep only the inward half of the blurred stroke
        nc.drawRoundRect(0f, 0f, w, h, cornerPx, cornerPx, paint)
        nc.restoreToCount(save)
    }
}

/**
 * Draws a soft *outward* drop shadow that follows the card's rounded corners, so
 * the card reads as a glass pane floating above the panel. The card's own rounded
 * rect is clipped out (it may be translucent), leaving only the outer bleed; a
 * slight downward offset makes the lift feel natural.
 */
internal fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGroupDropShadow(cornerPx: Float, extentDp: Float) {
    val e = extentDp.dp.toPx()
    if (e <= 0f) return
    val w = size.width
    val h = size.height
    val dy = e * 0.35f
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.argb(140, 0, 0, 0)
        maskFilter = android.graphics.BlurMaskFilter(e, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }
    val cardPath = android.graphics.Path().apply {
        addRoundRect(0f, 0f, w, h, cornerPx, cornerPx, android.graphics.Path.Direction.CW)
    }
    drawIntoCanvas { canvas ->
        val nc = canvas.nativeCanvas
        val save = nc.save()
        nc.clipOutPath(cardPath) // keep the shadow outside the (translucent) card
        nc.drawRoundRect(0f, dy, w, h + dy, cornerPx, cornerPx, paint)
        nc.restoreToCount(save)
    }
}

/** Themed-icon rendering settings, provided down the panel tree. */
internal data class IconStyle(val themed: Boolean, val fg: Int, val bg: Int)
internal val LocalIconStyle = androidx.compose.runtime.staticCompositionLocalOf { IconStyle(false, 0, 0) }

/**
 * Renders an app icon tinted to the system theme (a themed icon). Uses the
 * adaptive icon's real monochrome layer when the app ships one (Android 13+);
 * otherwise *generates* a monochrome from the icon's own artwork (luminance
 * silhouette) so every icon recolours uniformly, not just the ones with a
 * monochrome layer. Returns null only when the OS is too old for the palette.
 */
internal fun themedIconBitmap(src: android.graphics.drawable.Drawable, sizePx: Int, fg: Int, bg: Int): android.graphics.Bitmap? {
    if (android.os.Build.VERSION.SDK_INT < 31) return null
    val bmp = android.graphics.Bitmap.createBitmap(sizePx, sizePx, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bmp)
    val radius = sizePx * 0.22f
    val rect = android.graphics.RectF(0f, 0f, sizePx.toFloat(), sizePx.toFloat())
    canvas.drawRoundRect(rect, radius, radius, android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { color = bg })
    val clip = android.graphics.Path().apply { addRoundRect(rect, radius, radius, android.graphics.Path.Direction.CW) }
    canvas.clipPath(clip)

    val adaptive = src as? android.graphics.drawable.AdaptiveIconDrawable
    val realMono = if (android.os.Build.VERSION.SDK_INT >= 33) adaptive?.monochrome else null
    val bleed = (sizePx * 0.25f).toInt() // adaptive layers are 1.5x the visible mask
    if (realMono != null) {
        realMono.mutate()
        realMono.setTint(fg)
        realMono.setBounds(-bleed, -bleed, sizePx + bleed, sizePx + bleed)
        realMono.draw(canvas)
    } else {
        // Generate a monochrome from the icon's foreground (or the whole icon).
        val layer = adaptive?.foreground ?: src
        val tmp = android.graphics.Bitmap.createBitmap(sizePx, sizePx, android.graphics.Bitmap.Config.ARGB_8888)
        val tc = android.graphics.Canvas(tmp)
        val b = if (adaptive != null) bleed else 0
        layer.setBounds(-b, -b, sizePx + b, sizePx + b)
        layer.draw(tc)
        val n = sizePx * sizePx
        val px = IntArray(n)
        tmp.getPixels(px, 0, sizePx, 0, 0, sizePx, sizePx)
        val fr = (fg ushr 16) and 0xFF
        val fgn = (fg ushr 8) and 0xFF
        val fb = fg and 0xFF
        for (i in 0 until n) {
            val p = px[i]
            val a = (p ushr 24) and 0xFF
            if (a == 0) { px[i] = 0; continue }
            val r = (p ushr 16) and 0xFF
            val g = (p ushr 8) and 0xFF
            val bl = p and 0xFF
            val lum = (0.299 * r + 0.587 * g + 0.114 * bl) / 255.0
            // Keep the icon's shape (alpha) while letting brighter areas read
            // stronger, so detail survives instead of a flat blob.
            val outA = (a * (0.25 + 0.75 * lum)).toInt().coerceIn(0, 255)
            px[i] = (outA shl 24) or (fr shl 16) or (fgn shl 8) or fb
        }
        tmp.setPixels(px, 0, sizePx, 0, 0, sizePx, sizePx)
        canvas.drawBitmap(tmp, 0f, 0f, null)
    }
    return bmp
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
    group: GroupConfig,
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

    // Phone-wide recents when Usage access is granted; otherwise the apps most
    // recently launched from the sidebar itself.
    var recents by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(appMap) {
        val map = appMap ?: return@LaunchedEffect
        recents = withContext(kotlinx.coroutines.Dispatchers.IO) {
            com.personal.sidebar.apps.Recents.recentApps(context, map.keys, 4)
                .ifEmpty { Settings.recents(context) }
        }
    }

    val onLaunch: (String) -> Unit = { pkg ->
        Settings.addRecent(context, pkg)
        AppRepository.launch(context, pkg)
        dismiss()
    }
    val onOpenSettings: () -> Unit = {
        context.startActivity(
            android.content.Intent(context, MainActivity::class.java)
                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        dismiss()
    }
    // Open a web link / PWA. If a target app is set (e.g. the browser that
    // installed the PWA), the URL is sent straight to it so it opens standalone.
    // Otherwise we prefer any installed non-browser handler (a real WebAPK),
    // falling back to the default browser.
    val onOpenLink: (String, String?) -> Unit = { url, pkg ->
        val base = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
            .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        var launched = false
        if (!pkg.isNullOrBlank()) {
            val targeted = android.content.Intent(base).setPackage(pkg)
            launched = runCatching { context.startActivity(targeted) }.isSuccess
        }
        if (!launched && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // REQUIRE_NON_BROWSER throws if only browsers can handle the URL, so
            // this launches a WebAPK when present and lets us fall back otherwise.
            val nonBrowser = android.content.Intent(base)
                .addFlags(android.content.Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER)
            launched = runCatching { context.startActivity(nonBrowser) }.isSuccess
        }
        if (!launched) runCatching { context.startActivity(base) }
        dismiss()
    }

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
                group = group,
                recents = recents,
                onLaunch = onLaunch,
                onOpenLink = onOpenLink,
                onOpenSettings = onOpenSettings,
            )
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
    group: GroupConfig,
    recents: List<String>,
    onLaunch: (String) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val corner = panel.cornerDp.dp
    val shape = if (edge == Edge.LEFT) {
        RoundedCornerShape(topEnd = corner, bottomEnd = corner)
    } else {
        RoundedCornerShape(topStart = corner, bottomStart = corner)
    }
    val systemPadding = WindowInsets.safeDrawing.asPaddingValues()

    // Themed-icon colours from the system palette (Android 12+/13+), matching
    // the OS themed-icons look: a light accent glyph on a muted accent tile.
    val context = LocalContext.current
    val iconStyle = remember(panel.themedIcons) {
        if (panel.themedIcons && android.os.Build.VERSION.SDK_INT >= 31) {
            IconStyle(
                themed = true,
                fg = context.getColor(android.R.color.system_accent1_100),
                bg = context.getColor(android.R.color.system_accent2_800),
            )
        } else IconStyle(false, 0, 0)
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalIconStyle provides iconStyle) {
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
            Box(Modifier.weight(1f).fillMaxWidth()) {
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
                        loose = items.filter { it.type == ItemType.APP || it.type == ItemType.LINK },
                        groups = items.filter { it.type == ItemType.GROUP },
                        folders = items.filter { it.type == ItemType.FOLDER },
                        appMap = appMap,
                        style = folder,
                        groupStyle = group,
                        showLabels = panel.showLabels,
                        onLaunch = onLaunch,
                        onOpenLink = onOpenLink,
                    )
                }
            }
            // Bottom bar: a Settings gear, then recent apps.
            if (appMap != null) {
                BottomBar(recents = recents, appMap = appMap, showLabels = panel.showLabels, onLaunch = onLaunch, onOpenSettings = onOpenSettings)
            }
        }
    }
    }
}

@Composable
private fun BottomBar(
    recents: List<String>,
    appMap: Map<String, AppInfo>,
    showLabels: Boolean,
    onLaunch: (String) -> Unit,
    onOpenSettings: () -> Unit,
) {
    // Bare gear, left-aligned — same outline icon set and auto-theme colour as
    // the folders, with no background, border or shadow.
    val context = LocalContext.current
    val themeColor = remember {
        if (android.os.Build.VERSION.SDK_INT >= 31) Color(context.getColor(android.R.color.system_accent1_100))
        else Color(0xFFB9C3FF)
    }
    Box(Modifier.fillMaxWidth().padding(top = 2.dp), contentAlignment = Alignment.CenterStart) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable(onClick = onOpenSettings)
                .padding(6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = themeColor,
                modifier = Modifier.size(24.dp),
            )
        }
    }
    Box(Modifier.fillMaxWidth().padding(top = 8.dp).height(1.dp).background(Color.White.copy(alpha = 0.15f)))
    // Recent apps across the full width.
    Row(
        Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        val recentApps = recents.mapNotNull { appMap[it] }.take(4)
        recentApps.forEach { app ->
            Box(Modifier.weight(1f)) {
                AppTile(app = app, showLabel = showLabels) { onLaunch(app.packageName) }
            }
        }
        repeat(4 - recentApps.size) { Box(Modifier.weight(1f)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PanelContent(
    loose: List<SidebarItem>,
    groups: List<SidebarItem>,
    folders: List<SidebarItem>,
    appMap: Map<String, AppInfo>,
    style: FolderConfig,
    groupStyle: GroupConfig,
    showLabels: Boolean,
    onLaunch: (String) -> Unit,
    onOpenLink: (String, String?) -> Unit,
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
        // Folders at the top, laid out on the SAME 4-column grid as the groups
        // below so their tiles line up in columns. The open folder's contents
        // appear directly below (no divider between them).
        if (folders.isNotEmpty()) {
            // Spread across the full panel width — the same span as the group
            // cards below — so the first folder starts at the left edge and the
            // last ends at the right edge.
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                folders.chunked(COLUMNS).forEach { rowFolders ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        rowFolders.forEach { f ->
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
                        // Keep columns consistent when the last row isn't full.
                        repeat(COLUMNS - rowFolders.size) { Spacer(Modifier.width(66.dp)) }
                    }
                }
            }

            val open = folders.firstOrNull { it.key() == openKey }
            var shown by remember { mutableStateOf<SidebarItem?>(null) }
            LaunchedEffect(openKey) { if (open != null) shown = open }
            AnimatedVisibility(
                visible = open != null,
                // Scale + fade from the tapped icon (no vertical-expand clip, which
                // used to hide the card's drop shadow until the expand finished —
                // the shadow now scales in seamlessly with the card). ~20% faster.
                enter = fadeIn(animationSpec = tween(190)) +
                    scaleIn(animationSpec = tween(190), transformOrigin = TransformOrigin(pivotX, 0f), initialScale = 0.5f),
                exit = fadeOut(animationSpec = tween(150)) +
                    scaleOut(animationSpec = tween(150), transformOrigin = TransformOrigin(pivotX, 0f), targetScale = 0.5f),
            ) {
                Box(Modifier.padding(top = 8.dp)) {
                    shown?.let { FolderExpanded(it, style, appMap, showLabels, onLaunch) }
                }
            }
        }

        // Loose apps + links grid.
        if (loose.isNotEmpty()) {
            if (folders.isNotEmpty()) Spacer(Modifier.height(72.dp))
            val dim by animateFloatAsState(if (dimActive) 0.35f else 1f, label = "appsDim")
            AppGrid(loose, COLUMNS, appMap, showLabels, Modifier.alpha(dim), onLaunch, onOpenLink)
        }

        // Titled groups (inline sections, each in a subtly framed card).
        groups.forEach { g ->
            val dim by animateFloatAsState(if (dimActive) 0.35f else 1f, label = "groupDim")
            Spacer(Modifier.height(12.dp))
            val shape = RoundedCornerShape(groupStyle.cornerDp.dp)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(dim)
                    .drawBehind { drawGroupDropShadow(groupStyle.cornerDp.dp.toPx(), groupStyle.shadowDp) },
                shape = shape,
                color = Color.Transparent,
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
                border = if (groupStyle.borderDp > 0f) {
                    BorderStroke(groupStyle.borderDp.dp, Color.White.copy(alpha = groupStyle.borderBrightness.coerceIn(0f, 1f)))
                } else null,
            ) {
                Box {
                    Column(Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
                        if (!g.name.isNullOrBlank()) {
                            Text(
                                text = g.name,
                                color = LabelPrimary.copy(alpha = groupStyle.titleAlpha.coerceIn(0f, 1f)),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                textAlign = if (groupStyle.titleCenter) TextAlign.Center else TextAlign.Start,
                                modifier = if (groupStyle.titleCenter) {
                                    Modifier.fillMaxWidth().padding(bottom = 6.dp)
                                } else {
                                    Modifier.padding(start = 2.dp, bottom = 6.dp)
                                },
                            )
                        }
                        AppGrid(
                            items = g.packages.map { SidebarItem.app(it) },
                            cols = COLUMNS,
                            appMap = appMap,
                            showLabels = showLabels,
                            modifier = Modifier,
                            onLaunch = onLaunch,
                            tileAlign = Alignment.Start,
                        )
                    }
                    // Sunken look: an inner shadow hugging the group's edge.
                    if (groupStyle.insetDp > 0f) {
                        Box(Modifier.matchParentSize().drawBehind { drawInnerShadow(groupStyle.cornerDp.dp.toPx(), groupStyle.insetDp) })
                    }
                }
            }
        }
    }
}

/** A folder as a bare emoji (no tile/square behind it), with a label beneath. */
@Composable
private fun FolderCircle(
    folder: SidebarItem,
    style: FolderConfig,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val themeColor = remember {
        if (android.os.Build.VERSION.SDK_INT >= 31) Color(context.getColor(android.R.color.system_accent1_100))
        else Color(0xFFB9C3FF)
    }
    val glyphContent: @Composable () -> Unit = {
        val vector = com.personal.sidebar.FolderIcons.icon(folder.iconKey)
        if (vector != null) {
            val tint = folder.colorArgb?.let { Color(it) } ?: themeColor
            Icon(vector, contentDescription = folder.name, tint = tint, modifier = Modifier.size(40.dp))
        } else {
            val glyph = folder.emoji?.takeIf { it.isNotBlank() } ?: folder.name?.take(1) ?: "📁"
            Text(glyph, fontSize = 44.sp)
        }
    }
    Column(
        modifier = modifier.width(66.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (style.iconBackground) {
            // The optional styled tile (square) behind the icon, driven by the
            // Folder settings. Use a custom OUTWARD drop shadow (not Surface
            // elevation, which bleeds through the translucent tile as a faint
            // square behind the icon).
            val shadow = maxOf(style.shadowTopDp, style.shadowBottomDp, style.shadowLeftDp, style.shadowRightDp).coerceIn(0f, 24f)
            Surface(
                modifier = Modifier
                    .size(66.dp)
                    .drawBehind { drawGroupDropShadow(style.cornerDp.dp.toPx(), shadow) }
                    .clickable(onClick = onClick),
                shape = RoundedCornerShape(style.cornerDp.dp),
                color = panelColor(style.brightness).copy(alpha = style.opacity.coerceIn(0f, 1f)),
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
                border = if (style.edgeDp > 0f) BorderStroke(style.edgeDp.dp, Color.White.copy(alpha = if (selected) 0.75f else 0.4f)) else null,
            ) {
                Box(contentAlignment = Alignment.Center) { glyphContent() }
            }
        } else {
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center,
            ) { glyphContent() }
        }
        if (!folder.name.isNullOrBlank()) {
            Text(
                folder.name,
                color = LabelSecondary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp).width(66.dp),
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
    showLabels: Boolean,
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
                items = folder.packages.map { SidebarItem.app(it) },
                cols = style.columns.coerceIn(2, 5),
                appMap = appMap,
                showLabels = showLabels,
                modifier = Modifier,
                onLaunch = onLaunch,
            )
        }
    }
}

/** A non-lazy grid of apps and/or links (used for loose items, groups, folders). */
@Composable
private fun AppGrid(
    items: List<SidebarItem>,
    cols: Int,
    appMap: Map<String, AppInfo>,
    showLabels: Boolean,
    modifier: Modifier,
    onLaunch: (String) -> Unit,
    onOpenLink: (String, String?) -> Unit = { _, _ -> },
    tileAlign: Alignment.Horizontal = Alignment.CenterHorizontally,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.chunked(cols).forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                rowItems.forEach { item ->
                    Box(Modifier.weight(1f)) {
                        if (item.type == ItemType.LINK) {
                            LinkTile(item.name.orEmpty(), item.emoji, showLabels, tileAlign) {
                                item.url?.let { onOpenLink(it, item.targetPackage) }
                            }
                        } else {
                            val app = item.packageName?.let { appMap[it] }
                            if (app != null) {
                                AppTile(app = app, showLabel = showLabels, align = tileAlign) { onLaunch(app.packageName) }
                            }
                        }
                    }
                }
                repeat(cols - rowItems.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun Tile(label: String, showLabel: Boolean, align: Alignment.Horizontal = Alignment.CenterHorizontally, onClick: () -> Unit, icon: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 2.dp),
        horizontalAlignment = align,
    ) {
        Box(Modifier.size(50.dp), contentAlignment = Alignment.Center) { icon() }
        if (showLabel) {
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
}

@Composable
private fun AppTile(app: AppInfo, showLabel: Boolean = true, align: Alignment.Horizontal = Alignment.CenterHorizontally, onClick: () -> Unit) {
    val style = LocalIconStyle.current
    val bitmap = remember(app.packageName, style.themed) {
        (if (style.themed) themedIconBitmap(app.icon, 144, style.fg, style.bg) else null)
            ?.asImageBitmap()
            ?: app.icon.toBitmap(144, 144).asImageBitmap()
    }
    Tile(app.label, showLabel, align, onClick) {
        Image(bitmap = bitmap, contentDescription = app.label, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(12.dp)))
    }
}

@Composable
private fun LinkTile(label: String, emoji: String?, showLabel: Boolean, align: Alignment.Horizontal = Alignment.CenterHorizontally, onClick: () -> Unit) {
    Tile(label, showLabel, align, onClick) {
        if (!emoji.isNullOrBlank()) {
            Text(emoji, fontSize = 30.sp)
        } else {
            Box(
                Modifier.size(50.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Language, contentDescription = label, tint = LabelPrimary, modifier = Modifier.size(28.dp))
            }
        }
    }
}

/** Stable identity for a folder's expand/collapse state. */
private fun SidebarItem.key(): String = when (type) {
    ItemType.APP -> "app:${packageName}"
    ItemType.LINK -> "link:${name}:${url}"
    ItemType.FOLDER -> "folder:${name}:${packages.joinToString(",")}"
    ItemType.GROUP -> "group:${name}:${packages.joinToString(",")}"
}
