package com.personal.sidebar.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.core.graphics.drawable.toBitmap
import com.personal.sidebar.Edge
import com.personal.sidebar.Settings
import com.personal.sidebar.apps.AppInfo
import com.personal.sidebar.apps.AppRepository
import com.personal.sidebar.model.RailConfig
import com.personal.sidebar.model.RailFolder
import com.personal.sidebar.model.RailGroup
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.os.Build
import android.os.Process
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.rounded.VerticalSplit
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntSize

// ---- Design tokens (see the "Thumb rail" hand-off) --------------------------

private object Rail {
    val Surface = Color(0xFF1E1C17)
    val CellPress = Color(0xFF2A271F)
    val ButtonPress = Color(0xFF2F2C25)
    val Divider = Color(0xFF3C3930)
    val EmptyBorder = Color(0xFF4A453A)
    val TextPrimary = Color(0xFFF2EEE6)
    val TextSecondary = Color(0xFFB7B0A2)
    val TextTertiary = Color(0xFF7F786B)
    val Accent = Color(0xFFE8B86A)
    val ThemedTile = Color(0xFF3A3326)

    val RailWidth = 68.dp
    val RailRadius = 34.dp
    val RailPadding = 8.dp
    val Button = 52.dp
    val ItemGap = 4.dp
    val DividerMargin = 6.dp
    val Gap = 10.dp
    val DrawerWidth = 300.dp
    val DrawerRadius = 32.dp
    val Fillet = 10.dp
    val EdgeInset = 10.dp
    val BottomInset = 40.dp
    val TopFolderDrop = 8.dp
    val GroupListMax = 560.dp

    /** Rail height with all its items: padding, 5 folders, divider, settings, gaps. */
    val NaturalHeight: Dp = RailPadding * 2 + Button * 6 + (DividerMargin * 2 + 1.dp) + ItemGap * 6

    /** Distance from the rail's bottom to the centre of folder [index] (of [count]). */
    fun anchorFromBottom(index: Int, count: Int): Dp =
        RailPadding + Button + ItemGap + (DividerMargin * 2 + 1.dp) + ItemGap +
            Button / 2 + (Button + ItemGap) * (count - 1 - index)

    val GenieEasing = CubicBezierEasing(0.45f, 0f, 0.25f, 1f)
    val NeckEasing = CubicBezierEasing(0.2f, 0.9f, 0.25f, 1f)
    /** Folder switch: quick start, long gentle settle (Material "emphasized"). */
    val SwitchEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    const val SWITCH_MS = 340
    /** How far folder contents drift while cross-fading on a switch. */
    val SwitchDrift = 28.dp
}

private const val RECENTS = AppRepository.MAX_PANEL_RECENTS

/** Open/closed state of the drawer; the visible motion is [RailMotion.genie]. */
private const val GENIE_CLOSED = 0f
private const val GENIE_PINCH = 1f
private const val GENIE_OPEN = 2f

/**
 * Drives the drawer's motion. [genie] runs 0 (closed: a sliver squeezed into the
 * rail) → 1 (pinch: rail-side edge narrowed to the selected button) → 2 (open).
 * [anchor] is the selected button's centre, measured up from the drawer's
 * bottom, in dp; the neck and the genie follow it.
 *
 * Switching folders while open doesn't close the drawer: the neck glides to the
 * new button, the drawer resizes (see [RailContainer]) and the old contents
 * cross-fade into the new ones ([swap] 0 → 1, [previous] is the outgoing
 * folder). Each action cancels the previous one, so taps mid-animation retarget
 * smoothly from wherever things are.
 */
private class RailMotion(
    private val scope: CoroutineScope,
    initial: RailFolder,
    private val onSelect: (RailFolder) -> Unit,
) {
    private val count = RailFolder.entries.size
    val genie = Animatable(GENIE_CLOSED)
    val anchor = Animatable(Rail.anchorFromBottom(initial.ordinal, count).value)
    /** Slide-in of the whole sidebar (rail + drawer): 0 = gone, 1 = shown. */
    val enter = Animatable(0f)
    /** Cross-fade progress from [previous] to [selected]. */
    val swap = Animatable(1f)

    var selected by mutableStateOf(initial)
        private set
    var previous by mutableStateOf<RailFolder?>(null)
        private set
    /** +1 when the newly selected folder is below the previous one, -1 above. */
    var swapDirection by mutableStateOf(1)
        private set
    /** True while the drawer is open or opening; the active folder shows the neck. */
    var drawerShown by mutableStateOf(false)
        private set
    var dismissing by mutableStateOf(false)
        private set

    private var job: Job? = null

    private fun run(block: suspend CoroutineScope.() -> Unit) {
        job?.cancel()
        job = scope.launch { coroutineScope(block) }
    }

    private fun anchorOf(folder: RailFolder) = Rail.anchorFromBottom(folder.ordinal, count).value

    private suspend fun expand(firstMs: Int) {
        drawerShown = true
        if (genie.value < GENIE_PINCH) genie.animateTo(GENIE_PINCH, tween(firstMs, easing = Rail.GenieEasing))
        genie.animateTo(GENIE_OPEN, tween(150, easing = Rail.GenieEasing))
    }

    private suspend fun collapse() {
        if (genie.value > GENIE_PINCH) genie.animateTo(GENIE_PINCH, tween(120, easing = Rail.GenieEasing))
        drawerShown = false
        genie.animateTo(GENIE_CLOSED, tween(170, easing = Rail.GenieEasing))
    }

    /** First appearance: slide the rail in and open the drawer on [selected]. */
    fun show() {
        // Outside [run], so a tap mid-entrance doesn't freeze the slide-in.
        scope.launch { enter.animateTo(1f, tween(220, easing = Rail.GenieEasing)) }
        run { expand(firstMs = 150) }
    }

    private fun select(folder: RailFolder) {
        if (folder == selected) return
        selected = folder
        onSelect(folder)
    }

    fun tap(folder: RailFolder) {
        if (dismissing) return
        when {
            // Closed (or closing): pick the folder, then open.
            !drawerShown -> run {
                previous = null
                swap.snapTo(1f)
                select(folder)
                // Mid-collapse the sliver is still visible: glide it over.
                if (genie.value > GENIE_CLOSED) {
                    anchor.animateTo(anchorOf(folder), tween(150, easing = Rail.GenieEasing))
                } else {
                    anchor.snapTo(anchorOf(folder))
                }
                expand(firstMs = 140)
            }
            // The active folder: close.
            folder == selected -> run { collapse() }
            // Another folder: glide over and cross-fade, drawer stays open.
            else -> run {
                swapDirection = if (folder.ordinal > selected.ordinal) 1 else -1
                previous = selected
                select(folder)
                swap.snapTo(0f)
                if (genie.value < GENIE_OPEN) launch { expand(firstMs = 150) }
                launch { anchor.animateTo(anchorOf(folder), tween(Rail.SWITCH_MS, easing = Rail.SwitchEasing)) }
                swap.animateTo(1f, tween(Rail.SWITCH_MS, easing = LinearEasing))
                previous = null
            }
        }
    }

    /**
     * Animates everything away, then calls [onDone]. [quick] skips the genie
     * (used after launching something, so the overlay clears out of its way).
     */
    fun dismiss(quick: Boolean, onDone: () -> Unit) {
        if (dismissing) return
        dismissing = true
        run {
            if (quick) {
                enter.animateTo(0f, tween(140))
            } else {
                if (drawerShown || genie.value > GENIE_CLOSED) collapse()
                enter.animateTo(0f, tween(180, easing = Rail.GenieEasing))
            }
            onDone()
        }
    }
}

/**
 * The drawer's size, animated so a folder switch resizes smoothly. [target] is
 * written during measure; [height]/[top] chase it and are read back by measure.
 */
private class DrawerSize {
    /** Target container height (x) and drawer top inside it (y), in px. */
    var target by mutableStateOf<IntOffset?>(null)
    val height = Animatable(0f)
    val top = Animatable(0f)
    var ready by mutableStateOf(false)
}

/**
 * How the app cells look and behave. [drag], when set, makes a long-press start
 * a system drag of that app (for split screen); null = tap only.
 */
private data class CellStyle(
    val iconSize: Dp,
    val showLabels: Boolean,
    val themed: Boolean,
    val drag: ((AppInfo) -> DragAndDropTransferData)? = null,
)

/** Where a dragged app would land. */
private enum class SplitZone { SPLIT, CANCEL }

/**
 * The platform's (hidden) "drag an app" clip type and its pending-intent extra,
 * as the system launcher sends them. Matching that form lets a system whose
 * split-screen drop targets accept app drags take the drop itself; otherwise
 * the sidebar's own full-screen target handles it.
 */
private const val MIMETYPE_APP_ACTIVITY = "application/vnd.android.activity"
private const val EXTRA_PENDING_INTENT = "android.intent.extra.PENDING_INTENT"

/** A system drag of [app]'s launch activity. */
@RequiresApi(Build.VERSION_CODES.S)
private fun appDragData(context: android.content.Context, app: AppInfo): DragAndDropTransferData? {
    val launch = context.packageManager.getLaunchIntentForPackage(app.packageName) ?: return null
    val pending = PendingIntent.getActivity(
        context, app.packageName.hashCode(), launch,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
    val item = ClipData.Item(
        Intent()
            .putExtra(EXTRA_PENDING_INTENT, pending)
            .putExtra(Intent.EXTRA_USER, Process.myUserHandle())
    )
    val clip = ClipData(ClipDescription(app.label, arrayOf(MIMETYPE_APP_ACTIVITY)), item)
    return DragAndDropTransferData(clip, localState = app.packageName, flags = View.DRAG_FLAG_GLOBAL)
}

/**
 * The [com.personal.sidebar.model.Design.RAIL] panel: a vertical rail of folder
 * buttons near the bottom of the [edge], and a drawer that grows out of the
 * selected folder with that folder's groups of apps. Covers the whole screen
 * (undimmed) so a tap anywhere outside closes everything.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RailPanel(
    edge: Edge,
    rail: RailConfig,
    registerDismiss: (() -> Unit) -> Unit,
    onDismissed: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mirror = edge == Edge.RIGHT
    val groups = rail.groups

    val motion = remember {
        RailMotion(scope, rail.selected) { folder ->
            Settings.updateConfig(context) { it.copy(rail = it.rail.copy(selected = folder)) }
        }
    }
    val dismiss = remember { { motion.dismiss(quick = false, onDone = onDismissed) } }
    val actions = remember { PanelActions(context) { motion.dismiss(quick = true, onDone = onDismissed) } }
    LaunchedEffect(Unit) {
        registerDismiss(dismiss)
        motion.show()
    }

    // Seed from the warm cache so the first frame has icons; refine async.
    val seedRecents = remember { Settings.recents(context).take(RECENTS) }
    var recents by remember { mutableStateOf(seedRecents) }
    var appMap by remember { mutableStateOf(AppRepository.cachedInfoFor(neededPackages(groups, seedRecents))) }
    LaunchedEffect(Unit) {
        val recent = AppRepository.recentPackages(context, RECENTS)
        recents = recent
        appMap = AppRepository.infoFor(context, neededPackages(groups, recent))
    }

    // Drag-to-split: only where split screen makes sense (large screens).
    val largeScreen = LocalConfiguration.current.smallestScreenWidthDp >= 600
    val splitEnabled = rail.dragToSplit && largeScreen && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    /** The app being dragged, while a drag is in progress. */
    var dragging by remember { mutableStateOf<String?>(null) }
    var zone by remember { mutableStateOf<SplitZone?>(null) }
    var dragStarted by remember { mutableStateOf(false) }
    // If the system refused to start the drag, nothing else would reset it.
    LaunchedEffect(dragging) {
        if (dragging != null) {
            delay(1000)
            if (!dragStarted) { dragging = null; zone = null }
        }
    }
    val dragFade by animateFloatAsState(if (dragging != null && zone != SplitZone.CANCEL) 0.3f else 1f, label = "dragFade")

    val cell = CellStyle(
        iconSize = rail.iconDp.coerceIn(RailConfig.ICON_MIN, RailConfig.ICON_MAX).dp,
        showLabels = rail.showLabels,
        themed = rail.themedIcons,
        drag = if (splitEnabled) { app ->
            dragging = app.packageName
            dragStarted = false
            zone = null
            @Suppress("NewApi") appDragData(context, app)!!
        } else null,
    )
    val density = LocalDensity.current
    val iconBitmaps = remember { HashMap<String, ImageBitmap>() }
    fun iconFor(app: AppInfo) = iconBitmaps.getOrPut(app.packageName) {
        val px = with(density) { cell.iconSize.roundToPx() }
        if (cell.themed) {
            tintedIconBitmap(app.icon, px, Rail.Accent.toArgb(), Rail.ThemedTile.toArgb()).asImageBitmap()
        } else {
            // The icon exactly as the system (and its icon theme) draws it.
            app.icon.toBitmap(px, px).asImageBitmap()
        }
    }

    // The rail + drawer bounds in root (= window, the panel fills it) coordinates,
    // the same space as drag-event positions: dropping back on them cancels.
    val sidebarBounds = remember { arrayOf(Rect.Zero) }
    val splitTarget = remember {
        object : DragAndDropTarget {
            private var dropped = false
            private fun zoneAt(e: DragAndDropEvent): SplitZone {
                val slop = 24 * context.resources.displayMetrics.density
                return if (sidebarBounds[0].inflate(slop).contains(e.toAndroidDragEvent().let { Offset(it.x, it.y) })) SplitZone.CANCEL else SplitZone.SPLIT
            }
            override fun onStarted(event: DragAndDropEvent) { dropped = false; dragStarted = true }
            override fun onEntered(event: DragAndDropEvent) { zone = zoneAt(event) }
            override fun onMoved(event: DragAndDropEvent) { zone = zoneAt(event) }
            override fun onExited(event: DragAndDropEvent) { zone = null }
            override fun onDrop(event: DragAndDropEvent): Boolean {
                val pkg = dragging ?: return false
                if (zoneAt(event) != SplitZone.SPLIT) return false
                dropped = true
                actions.launchAppSplit(pkg)
                return true
            }
            override fun onEnded(event: DragAndDropEvent) {
                // Taken by the system's own split-screen drop targets (which sit
                // above this window): the app is opening, so get out of the way.
                if (!dropped && event.toAndroidDragEvent().result) {
                    motion.dismiss(quick = true, onDone = onDismissed)
                }
                dragging = null
                dragStarted = false
                zone = null
            }
        }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            // No dimming: the screen behind stays as it is. Taps outside the
            // rail/drawer (not consumed by them) still close the sidebar.
            .pointerInput(Unit) { detectTapGestures { dismiss() } }
            .then(
                if (splitEnabled) {
                    Modifier.dragAndDropTarget(
                        shouldStartDragAndDrop = { e ->
                            dragging != null && MIMETYPE_APP_ACTIVITY in e.mimeTypes()
                        },
                        target = splitTarget,
                    )
                } else Modifier
            ),
    ) {
        val safe = WindowInsets.safeDrawing
        val bottom = maxOf(
            Rail.BottomInset,
            with(density) { WindowInsets.navigationBars.getBottom(this).toDp() } + 16.dp,
        )
        val top = with(density) { WindowInsets.statusBars.getTop(this).toDp() } + 16.dp
        val start = Rail.EdgeInset + with(density) {
            (if (mirror) safe.getRight(this, LayoutDirection.Ltr) else safe.getLeft(this, LayoutDirection.Ltr)).toDp()
        }
        val availH = (maxHeight - top - bottom).coerceAtLeast(1.dp)
        val availW = (maxWidth - start - Rail.EdgeInset).coerceAtLeast(1.dp)
        // Short screens (a folded phone in landscape): shrink the whole sidebar
        // uniformly so the rail still fits instead of clipping.
        val scale = (availH / Rail.NaturalHeight).coerceIn(0.5f, 1f)
        val drawerWidth = minOf(Rail.DrawerWidth, availW / scale - Rail.RailWidth - Rail.Gap)
            .coerceAtLeast(120.dp)
        val slide = with(density) { (start + Rail.RailWidth).toPx() }

        // While dragging an app: where to drop it (behind the faded sidebar).
        if (dragging != null) SplitDropHint(mirror = mirror, active = zone == SplitZone.SPLIT)

        CompositionLocalProvider(LocalDensity provides Density(density.density * scale, density.fontScale)) {
            val drawerSize = remember { DrawerSize() }
            val interactive by remember { derivedStateOf { motion.genie.value >= GENIE_OPEN } }
            val selected = motion.selected
            val previous = motion.previous

            RailContainer(
                mirror = mirror,
                drawerWidth = drawerWidth,
                selectedIndex = selected.ordinal,
                maxHeight = availH / scale,
                size = drawerSize,
                modifier = Modifier
                    // Absolute: the handle's side is physical, not locale-relative.
                    .align(if (mirror) AbsoluteAlignment.BottomRight else AbsoluteAlignment.BottomLeft)
                    .absolutePadding(
                        left = if (mirror) 0.dp else start / scale,
                        right = if (mirror) start / scale else 0.dp,
                        bottom = bottom / scale,
                    )
                    // One layer for the whole group, so overlapping parts (neck
                    // over rail and drawer) don't show seams at partial opacity.
                    .onGloballyPositioned { sidebarBounds[0] = it.boundsInRoot() }
                    .graphicsLayer {
                        val e = motion.enter.value
                        alpha = rail.opacity.coerceIn(0.4f, 1f) * e * dragFade
                        translationX = (1f - e) * slide * (if (mirror) 1f else -1f)
                        compositingStrategy = CompositingStrategy.Offscreen
                    },
                drawer = {
                    Drawer(mirror = mirror, motion = motion) {
                        // The incoming folder first: its height sizes the drawer.
                        key(selected) {
                            FolderContent(
                                folder = selected,
                                groups = groups[selected].orEmpty(),
                                recents = recents,
                                appMap = appMap,
                                interactive = interactive,
                                cell = cell,
                                iconFor = ::iconFor,
                                onLaunch = actions::launchApp,
                                onAddApps = actions::openSettings,
                                modifier = Modifier.graphicsLayer {
                                    val s = motion.swap.value
                                    val t = Rail.SwitchEasing.transform(s)
                                    alpha = ((s - 0.35f) / 0.65f).coerceIn(0f, 1f) // after the old one is mostly gone
                                    translationY = -motion.swapDirection * Rail.SwitchDrift.toPx() * (1f - t)
                                },
                            )
                        }
                        if (previous != null && previous != selected) {
                            key(previous) {
                                FolderContent(
                                    folder = previous,
                                    groups = groups[previous].orEmpty(),
                                    recents = recents,
                                    appMap = appMap,
                                    interactive = false,
                                    cell = cell,
                                    iconFor = ::iconFor,
                                    onLaunch = {},
                                    onAddApps = {},
                                    modifier = Modifier.graphicsLayer {
                                        val s = motion.swap.value
                                        val t = Rail.SwitchEasing.transform(s)
                                        alpha = (1f - s / 0.4f).coerceIn(0f, 1f)
                                        translationY = motion.swapDirection * Rail.SwitchDrift.toPx() * t
                                    },
                                )
                            }
                        }
                    }
                },
                rail = {
                    FolderRail(
                        selected = selected,
                        drawerShown = motion.drawerShown,
                        mirror = mirror,
                        motion = motion,
                        drawerSize = drawerSize,
                        onFolder = motion::tap,
                        onSettings = actions::openSettings,
                    )
                },
            )
        }
    }
}

/** Packages to resolve icons for: every group's apps plus recents. */
private fun neededPackages(groups: Map<RailFolder, List<RailGroup>>, recents: List<String>): Set<String> =
    buildSet {
        groups.values.forEach { list -> list.forEach { addAll(it.packages) } }
        addAll(recents)
    }

/**
 * Lays out the rail and drawer side by side, bottom-aligned, both stretched to
 * the taller of the two (capped at [maxHeight]). The size animates towards its
 * target, so switching to a folder with more or fewer apps resizes smoothly.
 * The rail is placed last so it (and the neck it draws) sits above the drawer.
 */
@Composable
private fun RailContainer(
    mirror: Boolean,
    drawerWidth: Dp,
    selectedIndex: Int,
    maxHeight: Dp,
    size: DrawerSize,
    modifier: Modifier,
    drawer: @Composable () -> Unit,
    rail: @Composable () -> Unit,
) {
    LaunchedEffect(size) {
        snapshotFlow { size.target }.filterNotNull().collectLatest { t ->
            if (!size.ready) {
                size.height.snapTo(t.x.toFloat())
                size.top.snapTo(t.y.toFloat())
                size.ready = true
            } else {
                val spec = tween<Float>(Rail.SWITCH_MS, easing = Rail.SwitchEasing)
                coroutineScope {
                    launch { size.height.animateTo(t.x.toFloat(), spec) }
                    launch { size.top.animateTo(t.y.toFloat(), spec) }
                }
            }
        }
    }
    Layout(content = { drawer(); rail() }, modifier = modifier) { measurables, _ ->
        val (drawerM, railM) = measurables
        val railW = Rail.RailWidth.roundToPx()
        val gap = Rail.Gap.roundToPx()
        val dw = drawerWidth.roundToPx()
        val railNat = Rail.NaturalHeight.roundToPx()
        val maxH = maxOf(maxHeight.roundToPx(), railNat)
        val natural = drawerM.maxIntrinsicHeight(dw).coerceAtMost(maxH)
        val drop = Rail.TopFolderDrop.roundToPx()

        val targetHeight: Int
        val targetTop: Int
        if (selectedIndex == 0 && natural + drop <= railNat) {
            // Top folder: drawer starts level with the button; square corner.
            targetHeight = railNat; targetTop = drop
        } else if (selectedIndex == 0) {
            // Drawer taller than the rail: keep the top button clear of the
            // drawer's rounded corner so the neck's fillet has room.
            val clear = (Rail.DrawerRadius + Rail.Fillet - Rail.RailPadding).roundToPx()
            targetHeight = maxOf(natural, railNat + clear).coerceAtMost(maxH); targetTop = 0
        } else {
            targetHeight = maxOf(natural, railNat); targetTop = 0
        }
        val target = IntOffset(targetHeight, targetTop)
        if (size.target != target) size.target = target

        val height = if (size.ready) size.height.value.roundToInt() else targetHeight
        val drawerTop = if (size.ready) size.top.value.roundToInt() else targetTop
        val railP = railM.measure(Constraints.fixed(railW, height))
        val drawerP = drawerM.measure(Constraints.fixed(dw, (height - drawerTop).coerceAtLeast(0)))

        val totalW = railW + gap + dw
        layout(totalW, height) {
            drawerP.place(if (mirror) 0 else railW + gap, drawerTop)
            railP.place(if (mirror) totalW - railW else 0, 0)
        }
    }
}

// ---- Rail -------------------------------------------------------------------

private fun RailFolder.icon(active: Boolean): ImageVector = when (this) {
    RailFolder.RECENT -> if (active) Icons.Rounded.History else Icons.Outlined.History
    RailFolder.MEDIA -> if (active) Icons.Rounded.PlayCircle else Icons.Outlined.PlayCircle
    RailFolder.PRODUCTIVITY -> if (active) Icons.Rounded.Work else Icons.Outlined.Work
    RailFolder.AI -> if (active) Icons.Rounded.AutoAwesome else Icons.Outlined.AutoAwesome
    RailFolder.TOOLS -> if (active) Icons.Rounded.Build else Icons.Outlined.Build
}

@Composable
private fun FolderRail(
    selected: RailFolder,
    drawerShown: Boolean,
    mirror: Boolean,
    motion: RailMotion,
    drawerSize: DrawerSize,
    onFolder: (RailFolder) -> Unit,
    onSettings: () -> Unit,
) {
    val folders = RailFolder.entries
    // One neck that glides between buttons with the anchor, and grows/shrinks
    // as the drawer opens/closes.
    val neck by animateFloatAsState(
        targetValue = if (drawerShown) 1f else 0f,
        animationSpec = tween(260, easing = Rail.NeckEasing),
        label = "neck",
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRoundRect(Rail.Surface, cornerRadius = CornerRadius(Rail.RailRadius.toPx()))
                if (neck > 0f) {
                    val top = size.height - (motion.anchor.value.dp + Rail.Button / 2).toPx()
                    withTransform({ if (mirror) scale(-1f, 1f, pivot = Offset(size.width / 2f, 0f)) }) {
                        // The upper fillet flattens where the neck meets the drawer's top.
                        drawNeck(top, neck, upperFillet = (top - drawerSize.top.value).coerceIn(0f, Rail.Fillet.toPx()))
                    }
                }
            }
            // Taps on the rail's empty space shouldn't close the sidebar.
            .pointerInput(Unit) { detectTapGestures { } }
            .padding(vertical = Rail.RailPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Rail.ItemGap, Alignment.Bottom),
    ) {
        folders.forEach { f ->
            val active = drawerShown && f == selected
            val tint by animateColorAsState(
                if (active) Rail.Accent else Rail.TextSecondary,
                tween(Rail.SWITCH_MS, easing = Rail.SwitchEasing),
                label = "folderTint",
            )
            RailButton(
                icon = f.icon(active),
                tint = tint,
                pressColor = null,
                description = f.title,
                onClick = { onFolder(f) },
            )
        }
        Box(
            Modifier
                .padding(vertical = Rail.DividerMargin)
                .size(28.dp, 1.dp)
                .background(Rail.Divider)
        )
        RailButton(
            icon = Icons.Rounded.Tune,
            tint = Rail.TextSecondary,
            pressColor = Rail.ButtonPress,
            description = "Sidebar settings",
            onClick = onSettings,
        )
    }
}

@Composable
private fun RailButton(
    icon: ImageVector,
    tint: Color,
    pressColor: Color?,
    description: String,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        modifier = Modifier
            .size(Rail.Button)
            .clip(CircleShape)
            .background(if (pressed && pressColor != null) pressColor else Color.Transparent)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(24.dp))
    }
}

/**
 * The neck joining the selected button (top edge at [top], in rail
 * coordinates) to the drawer: a bar from the button's centre to 2dp inside the
 * drawer edge, with concave fillets where it meets the drawer. [upperFillet] is
 * the upper fillet's radius in px — it shrinks to 0 where the neck meets the
 * drawer's top edge. Drawn in left-edge orientation, scaled horizontally by
 * [progress] from the button centre.
 */
private fun DrawScope.drawNeck(top: Float, progress: Float, upperFillet: Float) {
    val cx = size.width / 2f
    val edgeX = size.width + Rail.Gap.toPx()
    val right = edgeX + 2.dp.toPx()
    val bottom = top + Rail.Button.toPx()
    val r = Rail.Fillet.toPx()
    val ur = upperFillet
    fun x(v: Float) = cx + (v - cx) * progress

    val path = Path().apply {
        moveTo(x(cx), top)
        if (ur > 0.5f) {
            lineTo(x(edgeX - ur), top)
            // Concave curve: arc of the circle centred (edgeX - ur, top - ur).
            arcTo(Rect(x(edgeX - 2 * ur), top - 2 * ur, x(edgeX), top), 90f, -90f, false)
            lineTo(x(right), top - ur)
        } else {
            lineTo(x(right), top)
        }
        lineTo(x(right), bottom + r)
        lineTo(x(edgeX), bottom + r)
        arcTo(Rect(x(edgeX - 2 * r), bottom, x(edgeX), bottom + 2 * r), 0f, -90f, false)
        lineTo(x(cx), bottom)
        close()
    }
    drawPath(path, Rail.Surface, alpha = (progress * 1.6f).coerceAtMost(1f))
}

// ---- Drawer -----------------------------------------------------------------

/**
 * The drawer surface: genie transform + clip, background, and a tap absorber.
 * Its [content] is one [FolderContent] per visible folder, stacked; only the
 * first (incoming) one decides the drawer's height.
 */
@Composable
private fun Drawer(mirror: Boolean, motion: RailMotion, content: @Composable () -> Unit) {
    val genieHalf = Rail.Button / 2
    val pullIn = 30.dp
    Layout(
        content = content,
        modifier = Modifier
            .graphicsLayer {
                // Closed → pinch: squeeze horizontally into the button.
                val p = motion.genie.value.coerceIn(GENIE_CLOSED, GENIE_OPEN)
                val t = p.coerceAtMost(GENIE_PINCH)
                val oy = size.height - motion.anchor.value.dp.toPx()
                scaleX = lerp(0.06f, 1f, t)
                translationX = lerp(pullIn.toPx(), 0f, t) * (if (mirror) 1f else -1f)
                transformOrigin = TransformOrigin(if (mirror) 1f else 0f, (oy / size.height).coerceIn(0f, 1f))
                alpha = (t * 2f).coerceAtMost(1f)
            }
            .drawWithContent {
                val p = motion.genie.value
                if (p >= GENIE_OPEN) {
                    drawContent()
                    return@drawWithContent
                }
                // Trapezoid clip: the rail-side edge narrows to the button's
                // height, the far edge follows once the near one is pinched.
                val oy = size.height - motion.anchor.value.dp.toPx()
                val half = genieHalf.toPx()
                val nearTop: Float
                val nearBottom: Float
                val farTop: Float
                val farBottom: Float
                if (p >= GENIE_PINCH) {
                    val t = p - GENIE_PINCH
                    nearTop = lerp(oy - half, 0f, t); nearBottom = lerp(oy + half, size.height, t)
                    farTop = 0f; farBottom = size.height
                } else {
                    nearTop = oy - half; nearBottom = oy + half
                    farTop = lerp(oy - half, 0f, p); farBottom = lerp(oy + half, size.height, p)
                }
                val near = if (mirror) size.width else 0f
                val far = if (mirror) 0f else size.width
                val clip = Path().apply {
                    moveTo(near, nearTop)
                    lineTo(far, farTop)
                    lineTo(far, farBottom)
                    lineTo(near, nearBottom)
                    close()
                }
                clipPath(clip) { this@drawWithContent.drawContent() }
            }
            .drawBehind {
                // (The hand-off's drop shadow is clipped away by the genie clip
                // in the reference too, so it's not drawn.)
                // The rail-side top corner squares off as the neck reaches the
                // drawer's top edge (the top folder), continuously while gliding.
                val neckTop = size.height - (motion.anchor.value.dp + Rail.Button / 2).toPx()
                val fillet = neckTop.coerceIn(0f, Rail.Fillet.toPx())
                val near = CornerRadius((neckTop - fillet).coerceIn(0f, Rail.DrawerRadius.toPx()))
                val r = CornerRadius(Rail.DrawerRadius.toPx())
                val rect = RoundRect(
                    rect = Rect(Offset.Zero, size),
                    topLeft = if (mirror) r else near,
                    topRight = if (mirror) near else r,
                    bottomRight = r,
                    bottomLeft = r,
                )
                drawPath(Path().apply { addRoundRect(rect) }, Rail.Surface)
            }
            // Taps on the drawer never close the sidebar; until it's fully open
            // they're simply ignored (cells are disabled).
            .pointerInput(Unit) { detectTapGestures { } }
            // Contents clip to the drawer while they drift during a switch.
            .clipToBounds(),
        measurePolicy = StackFirstSizes,
    )
}

/** Stacks children at the incoming size; intrinsics come from the first child only. */
private object StackFirstSizes : MeasurePolicy {
    override fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
        val placeables = measurables.map { it.measure(constraints) }
        val w = placeables.maxOfOrNull { it.width } ?: constraints.minWidth
        val h = placeables.maxOfOrNull { it.height } ?: constraints.minHeight
        return layout(w, h) { placeables.forEach { it.place(0, 0) } }
    }

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(measurables: List<IntrinsicMeasurable>, width: Int) =
        measurables.firstOrNull()?.maxIntrinsicHeight(width) ?: 0

    override fun IntrinsicMeasureScope.minIntrinsicHeight(measurables: List<IntrinsicMeasurable>, width: Int) =
        measurables.firstOrNull()?.minIntrinsicHeight(width) ?: 0

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(measurables: List<IntrinsicMeasurable>, height: Int) =
        measurables.firstOrNull()?.maxIntrinsicWidth(height) ?: 0

    override fun IntrinsicMeasureScope.minIntrinsicWidth(measurables: List<IntrinsicMeasurable>, height: Int) =
        measurables.firstOrNull()?.minIntrinsicWidth(height) ?: 0
}

/** One folder's title and groups, bottom-aligned near the thumb. */
@Composable
private fun FolderContent(
    folder: RailFolder,
    groups: List<RailGroup>,
    recents: List<String>,
    appMap: Map<String, AppInfo>,
    interactive: Boolean,
    cell: CellStyle,
    iconFor: (AppInfo) -> ImageBitmap,
    onLaunch: (String) -> Unit,
    onAddApps: () -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(
            text = folder.title,
            color = Rail.TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.01).em,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 6.dp),
        )
        // Pushes the groups down to the bottom, near the thumb.
        Spacer(Modifier.weight(1f))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = Rail.GroupListMax)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (folder == RailFolder.RECENT) {
                GroupSection(
                    title = groups.firstOrNull()?.title ?: "Today",
                    apps = recents.mapNotNull { appMap[it] },
                    emptyContent = { EmptyNote("No recent apps yet") },
                    interactive = interactive,
                    cell = cell,
                    iconFor = iconFor,
                    onLaunch = onLaunch,
                )
            } else {
                groups.forEach { group ->
                    key(group.id) {
                        GroupSection(
                            title = group.title,
                            apps = group.packages.mapNotNull { appMap[it] },
                            emptyContent = { EmptyCell(interactive, cell, onAddApps) },
                            interactive = interactive,
                            cell = cell,
                            iconFor = iconFor,
                            onLaunch = onLaunch,
                        )
                    }
                }
                if (groups.isEmpty()) EmptyCell(interactive, cell, onAddApps)
            }
        }
    }
}

@Composable
private fun GroupSection(
    title: String,
    apps: List<AppInfo>,
    emptyContent: @Composable () -> Unit,
    interactive: Boolean,
    cell: CellStyle,
    iconFor: (AppInfo) -> ImageBitmap,
    onLaunch: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Titles are edited in the app's settings, not here.
        if (title.isNotBlank()) {
            Text(
                text = title.uppercase(),
                color = Rail.TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.04.em,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            )
        }
        if (apps.isEmpty()) {
            emptyContent()
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                apps.chunked(3).forEach { row ->
                    Row(Modifier.fillMaxWidth()) {
                        row.forEach { app ->
                            Box(Modifier.weight(1f)) {
                                AppCell(app, iconFor(app), interactive, cell) { onLaunch(app.packageName) }
                            }
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppCell(
    app: AppInfo,
    icon: ImageBitmap,
    interactive: Boolean,
    cell: CellStyle,
    onClick: () -> Unit,
) {
    val label = app.label
    val interaction = remember { MutableInteractionSource() }
    var held by remember { mutableStateOf(false) }
    val pressed = interaction.collectIsPressedAsState().value || held
    val drag = cell.drag
    val iconPx = with(LocalDensity.current) { cell.iconSize.roundToPx() }
    val gestures = when {
        !interactive -> Modifier
        // Tap launches; long-press picks the app up as a system drag (drawn as
        // just its icon) for dropping into split screen.
        drag != null -> Modifier.dragAndDropSource(
            drawDragDecoration = {
                val left = ((size.width - iconPx) / 2f).roundToInt()
                drawImage(icon, dstOffset = IntOffset(left, 6.dp.roundToPx()), dstSize = IntSize(iconPx, iconPx))
            },
        ) {
            detectTapGestures(
                onPress = { held = true; tryAwaitRelease(); held = false },
                onTap = { onClick() },
                onLongPress = { held = false; startTransfer(drag(app)) },
            )
        }
        else -> Modifier.clickable(interactionSource = interaction, indication = null, onClick = onClick)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(if (pressed) 0.96f else 1f)
            .clip(RoundedCornerShape(20.dp))
            .background(if (pressed) Rail.CellPress else Color.Transparent)
            .then(gestures)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            bitmap = icon,
            contentDescription = label,
            // System icons keep their own (icon-theme) shape; themed ones sit
            // on the rail's rounded tile.
            modifier = Modifier
                .size(cell.iconSize)
                .then(if (cell.themed) Modifier.clip(RoundedCornerShape(cell.iconSize / 3)) else Modifier),
        )
        if (cell.showLabels) {
            Text(
                text = label,
                color = Rail.TextPrimary,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}

/** An empty group's placeholder; tapping it opens settings to add apps. */
@Composable
private fun EmptyCell(interactive: Boolean, cell: CellStyle, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .clickable(enabled = interactive, onClick = onClick)
                .padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                Modifier
                    .size(cell.iconSize)
                    .drawBehind {
                        val stroke = 1.5.dp.toPx()
                        val inset = stroke / 2f
                        drawRoundRect(
                            color = Rail.EmptyBorder,
                            topLeft = Offset(inset, inset),
                            size = Size(size.width - stroke, size.height - stroke),
                            cornerRadius = CornerRadius((cell.iconSize / 3).toPx() - inset),
                            style = Stroke(
                                width = stroke,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx())),
                            ),
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, tint = Rail.TextTertiary, modifier = Modifier.size(22.dp))
            }
            if (cell.showLabels) Text("Add apps", color = Rail.TextTertiary, fontSize = 13.sp, maxLines = 1)
        }
        Spacer(Modifier.weight(2f))
    }
}

/**
 * Shown while an app is being dragged: a target over the half of the screen
 * away from the sidebar. Dropping anywhere outside the sidebar opens the app in
 * split screen (the system picks the side); dropping back on it cancels.
 */
@Composable
private fun SplitDropHint(mirror: Boolean, active: Boolean) {
    val fill by animateColorAsState(
        if (active) Rail.Accent.copy(alpha = 0.28f) else Rail.Surface.copy(alpha = 0.72f),
        label = "splitFill",
    )
    Box(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(if (mirror) AbsoluteAlignment.CenterLeft else AbsoluteAlignment.CenterRight)
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
                .padding(16.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(fill)
                .border(2.dp, Rail.Accent.copy(alpha = if (active) 1f else 0.6f), RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Rounded.VerticalSplit, contentDescription = null, tint = Rail.Accent, modifier = Modifier.size(36.dp))
                Text("Drop to open in split screen", color = Rail.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text("Drop back on the sidebar to cancel", color = Rail.TextSecondary, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun EmptyNote(text: String) {
    Text(text, color = Rail.TextTertiary, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp))
}
