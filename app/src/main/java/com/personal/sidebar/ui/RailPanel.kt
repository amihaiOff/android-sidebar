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
import androidx.compose.foundation.layout.height
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
import com.personal.sidebar.model.HandleConfig
import com.personal.sidebar.model.RailConfig
import com.personal.sidebar.model.RailFolderConfig
import com.personal.sidebar.model.defaultRailFolders
import com.personal.sidebar.model.RailGroup
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import androidx.compose.runtime.withFrameNanos
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

    /** Rail height with all its items: padding, [count] folders, divider, settings, gaps. */
    fun naturalHeight(count: Int): Dp =
        RailPadding * 2 + Button * (count + 1) + (DividerMargin * 2 + 1.dp) + ItemGap * (count + 1)

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
private const val GENIE_OPEN = 1f

/** Material "emphasized" curves: opening decelerates, closing accelerates. */
private val OpenEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
private val CloseEasing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
private const val OPEN_MS = 300
private const val CLOSE_MS = 200

/**
 * Drives the drawer's motion. [genie] runs 0 (closed: shrunk into the selected
 * button) → 1 (open) in one continuous animation; see [Drawer] for how it maps
 * to the drawer's transform.
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
    /** Folder ids, top to bottom. */
    private val folderIds: List<String>,
    initial: String,
    private val onSelect: (String) -> Unit,
) {
    private val count = folderIds.size
    private fun indexOf(id: String) = folderIds.indexOf(id).coerceAtLeast(0)
    val genie = Animatable(GENIE_CLOSED)
    val anchor = Animatable(Rail.anchorFromBottom(indexOf(initial), count).value)
    /** Slide-in of the whole sidebar (rail + drawer): 0 = gone, 1 = shown. */
    val enter = Animatable(0f)
    /** Cross-fade progress from [previous] to [selected]. */
    val swap = Animatable(1f)

    var selected by mutableStateOf(initial)
        private set
    var previous by mutableStateOf<String?>(null)
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

    private fun anchorOf(folder: String) = Rail.anchorFromBottom(indexOf(folder), count).value

    // One uninterrupted animation each way (the old two-stage pinch paused
    // between its stages), scaled so a reversal mid-way keeps its pace.
    private suspend fun expand() {
        drawerShown = true
        val ms = (OPEN_MS * (GENIE_OPEN - genie.value)).toInt().coerceAtLeast(1)
        genie.animateTo(GENIE_OPEN, tween(ms, easing = OpenEasing))
    }

    private suspend fun collapse() {
        drawerShown = false
        val ms = (CLOSE_MS * genie.value).toInt().coerceAtLeast(1)
        genie.animateTo(GENIE_CLOSED, tween(ms, easing = CloseEasing))
    }

    /** First appearance: slide the rail in and open the drawer on [selected]. */
    fun show() {
        // Outside [run], so a tap mid-entrance doesn't freeze the slide-in.
        scope.launch { enter.animateTo(1f, tween(170, easing = Rail.SwitchEasing)) }
        run { expand() }
    }

    private fun select(folder: String) {
        if (folder == selected) return
        selected = folder
        onSelect(folder)
    }

    fun tap(folder: String) {
        if (dismissing) return
        when {
            // Closed (or closing): pick the folder, then open.
            !drawerShown -> run {
                previous = null
                swap.snapTo(1f)
                select(folder)
                // Mid-collapse the sliver is still visible: glide it over.
                if (genie.value > GENIE_CLOSED) {
                    launch { anchor.animateTo(anchorOf(folder), tween(150, easing = Rail.GenieEasing)) }
                } else {
                    anchor.snapTo(anchorOf(folder))
                }
                expand()
            }
            // The active folder: close.
            folder == selected -> run { collapse() }
            // Another folder: glide over and cross-fade, drawer stays open.
            else -> run {
                swapDirection = if (indexOf(folder) > indexOf(selected)) 1 else -1
                previous = selected
                select(folder)
                swap.snapTo(0f)
                if (genie.value < GENIE_OPEN) launch { expand() }
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
                // Overlap the rail's exit with the drawer's close, so closing is
                // one motion rather than two in a row.
                val exit = launch {
                    delay((CLOSE_MS * genie.value * 0.5f).toLong())
                    enter.animateTo(0f, tween(180, easing = CloseEasing))
                }
                if (drawerShown || genie.value > GENIE_CLOSED) collapse()
                exit.join()
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
    /** Target drawer height (x) and drawer top (y), in container px. */
    var target by mutableStateOf<IntOffset?>(null)
    val height = Animatable(0f)
    val top = Animatable(0f)
    var ready by mutableStateOf(false)
    /** Where the rail sits in the container (its height never changes). */
    var railTop by mutableStateOf(0)
    var railHeight by mutableStateOf(0)

    /** The selected button's centre ([anchorPx] up from the rail's bottom), in drawer coordinates. */
    fun neckCenterInDrawer(anchorPx: Float): Float = railTop + railHeight - anchorPx - top.value
}

/**
 * How the app cells look and behave. [drag], when set, makes a long-press start
 * a system drag of that app (for split screen); null = tap only.
 */
private data class CellStyle(
    val iconSize: Dp,
    val showLabels: Boolean,
    val themed: Boolean,
    val drag: ((AppInfo) -> DragAndDropTransferData?)? = null,
)

/**
 * The platform's (hidden) "drag an app" clip type and its pending-intent extra,
 * as the system launcher sends them. In that form the system's own split-screen
 * drop targets accept the drag (as One UI does).
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
    handle: HandleConfig,
    registerDismiss: (() -> Unit) -> Unit,
    onDismissed: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mirror = edge == Edge.RIGHT
    // Always at least one folder (settings won't delete the last one).
    val folders = rail.folders.ifEmpty { defaultRailFolders() }
    val folderIds = remember(folders) { folders.map { it.id } }
    val folderById = remember(folders) { folders.associateBy { it.id } }

    val motion = remember {
        val initial = rail.openingFolder()?.takeIf { it.id in folderIds }?.id ?: folderIds.first()
        RailMotion(scope, folderIds, initial) { id ->
            Settings.updateConfig(context) { it.copy(rail = it.rail.copy(selectedFolder = id)) }
        }
    }
    val dismiss = remember { { motion.dismiss(quick = false, onDone = onDismissed) } }
    val actions = remember { PanelActions(context) { motion.dismiss(quick = true, onDone = onDismissed) } }
    LaunchedEffect(Unit) {
        registerDismiss(dismiss)
        // Let the first (heaviest) frames — building the window and UI — go by
        // while still invisible, so the animation doesn't start with a jump.
        withFrameNanos { }
        withFrameNanos { }
        motion.show()
    }

    // Seed from the warm cache so the first frame has icons; refine async.
    val seedRecents = remember { Settings.recents(context).take(RECENTS) }
    var recents by remember { mutableStateOf(seedRecents) }
    var appMap by remember { mutableStateOf(AppRepository.cachedInfoFor(neededPackages(folders, seedRecents))) }
    LaunchedEffect(Unit) {
        val recent = AppRepository.recentPackages(context, RECENTS)
        val map = AppRepository.infoFor(context, neededPackages(folders, recent))
        val iconPx = (rail.iconDp.coerceIn(RailConfig.ICON_MIN, RailConfig.ICON_MAX) * context.resources.displayMetrics.density).roundToInt()
        withContext(Dispatchers.Default) { map.values.forEach { PanelIcons.get(it, iconPx, railIconTint(rail)) } }
        // Swapping contents mid-animation would stutter it: apply once the
        // drawer is open (unless the cache was cold and there's nothing yet).
        if (appMap.isNotEmpty()) snapshotFlow { motion.genie.value >= GENIE_OPEN }.first { it }
        if (recent != recents) recents = recent
        if (map != appMap) appMap = map
    }

    // Drag-to-split: only where split screen makes sense (large screens).
    val largeScreen = LocalConfiguration.current.smallestScreenWidthDp >= 600
    val splitEnabled = rail.dragToSplit && largeScreen && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    /** The app being dragged, while a drag is in progress. */
    var dragging by remember { mutableStateOf<String?>(null) }

    // Stable across recompositions, so the app cells don't all recompose.
    val cell = remember(rail, splitEnabled) {
        CellStyle(
            iconSize = rail.iconDp.coerceIn(RailConfig.ICON_MIN, RailConfig.ICON_MAX).dp,
            showLabels = rail.showLabels,
            themed = rail.themedIcons,
            drag = if (splitEnabled) { app ->
                @Suppress("NewApi")
                appDragData(context, app)?.also { dragging = app.packageName }
            } else null,
        )
    }
    val density = LocalDensity.current
    val iconPx = with(density) { cell.iconSize.roundToPx() }
    val iconFor: (AppInfo) -> ImageBitmap = remember(iconPx, rail) {
        { app -> PanelIcons.get(app, iconPx, railIconTint(rail)) }
    }

    // The system's split-screen drop targets take the drop (they sit above this
    // window). The sidebar only listens for the drag ending, to get out of the
    // way once the app is opening; it never accepts the drop itself.
    val splitTarget = remember {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean = false
            override fun onEnded(event: DragAndDropEvent) {
                if (event.toAndroidDragEvent().result) motion.dismiss(quick = true, onDone = onDismissed)
                dragging = null
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
        val railNatural = Rail.naturalHeight(folders.size)
        val scale = (availH / railNatural).coerceIn(0.5f, 1f)
        val drawerWidth = minOf(Rail.DrawerWidth, availW / scale - Rail.RailWidth - Rail.Gap)
            .coerceAtLeast(120.dp)
        val slide = with(density) { (start + Rail.RailWidth).toPx() }

        // Open level with the edge handle (the trigger area): centre the rail on
        // the handle's centre, kept on screen. The drawer grows upward from the
        // rail's bottom, so it may use all the room above it.
        val railHeight = railNatural * scale
        val handleCenter = remember(handle) {
            // Same placement maths as EdgeHandle (its window starts below the status bar).
            val dm = context.resources.displayMetrics
            val screenDp = dm.heightPixels / dm.density
            val len = handle.lengthDp.toFloat().coerceAtMost(screenDp)
            ((screenDp - len) * handle.verticalBias.coerceIn(0f, 1f) + len / 2f).dp
        }
        val statusTop = with(density) { WindowInsets.statusBars.getTop(this).toDp() }
        val lowest = (maxHeight - bottom).coerceAtLeast(top + railHeight)
        val railBottom = rail.positionBias?.let { bias ->
            // Set in settings: 0 = rail at the top of the screen, 1 = at the bottom.
            top + railHeight + (lowest - top - railHeight) * bias.coerceIn(0f, 1f)
        } ?: (statusTop + handleCenter + railHeight / 2).coerceIn(top + railHeight, lowest)
        // The container spans the usable screen height; the rail sits at its
        // own place inside it and the drawer is laid out around that.
        val usable = (maxHeight - top - bottom).coerceAtLeast(railHeight)
        val railTopInContainer = (railBottom - railHeight - top).coerceAtLeast(0.dp)

        CompositionLocalProvider(LocalDensity provides Density(density.density * scale, density.fontScale)) {
            val drawerSize = remember { DrawerSize() }
            val interactive by remember { derivedStateOf { motion.genie.value >= GENIE_OPEN } }
            val selected = motion.selected
            val previous = motion.previous
            val selectedFolder = folderById.getValue(selected)

            RailContainer(
                mirror = mirror,
                drawerWidth = drawerWidth,
                selectedIndex = folderIds.indexOf(selected),
                folderCount = folders.size,
                railTop = railTopInContainer / scale,
                size = drawerSize,
                modifier = Modifier
                    // Absolute: the handle's side is physical, not locale-relative.
                    .align(if (mirror) AbsoluteAlignment.TopRight else AbsoluteAlignment.TopLeft)
                    .absolutePadding(
                        left = if (mirror) 0.dp else start / scale,
                        right = if (mirror) start / scale else 0.dp,
                        top = top / scale,
                    )
                    .height(usable / scale)
                    // One layer for the whole group, so overlapping parts (neck
                    // over rail and drawer) don't show seams at partial opacity.
                    .graphicsLayer {
                        val e = motion.enter.value
                        alpha = rail.opacity.coerceIn(0.4f, 1f) * e
                        translationX = (1f - e) * slide * (if (mirror) 1f else -1f)
                        compositingStrategy = CompositingStrategy.Offscreen
                    },
                drawer = {
                    Drawer(mirror = mirror, motion = motion, drawerSize = drawerSize) {
                        // The incoming folder first: its height sizes the drawer.
                        key(selected) {
                            FolderContent(
                                folder = selectedFolder,
                                recents = recents,
                                appMap = appMap,
                                interactive = interactive,
                                cell = cell,
                                iconFor = iconFor,
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
                        val previousFolder = previous?.let { folderById[it] }
                        if (previousFolder != null && previous != selected) {
                            key(previous) {
                                FolderContent(
                                    folder = previousFolder,
                                    recents = recents,
                                    appMap = appMap,
                                    interactive = false,
                                    cell = cell,
                                    iconFor = iconFor,
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
                        folders = folders,
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

/** Themed-icon colours (glyph, tile), or null for the system's own icons. */
private fun railIconTint(rail: RailConfig): Pair<Int, Int>? =
    if (rail.themedIcons) Rail.Accent.toArgb() to Rail.ThemedTile.toArgb() else null

/**
 * Pre-renders the rail's icons in the background (called by the service when
 * it warms the app cache), so the panel's first frames don't have to.
 */
internal fun prewarmRailIcons(context: android.content.Context, rail: RailConfig, apps: Collection<AppInfo>) {
    val px = (rail.iconDp.coerceIn(RailConfig.ICON_MIN, RailConfig.ICON_MAX) * context.resources.displayMetrics.density).roundToInt()
    val tint = railIconTint(rail)
    apps.forEach { runCatching { PanelIcons.get(it, px, tint) } }
}

/** Packages to resolve icons for: every group's apps plus recents. */
private fun neededPackages(folders: List<RailFolderConfig>, recents: List<String>): Set<String> =
    buildSet {
        folders.forEach { f -> f.groups.forEach { addAll(it.packages) } }
        addAll(recents)
    }

/**
 * Lays out the rail and drawer side by side within the usable screen height.
 * The rail keeps its own height at [railTop]. The drawer starts level with the
 * rail's top and grows downward to fit its contents (always reaching past the
 * selected button so the neck connects); if that would run off the bottom it
 * moves up as far as needed, and only beyond the full height does it scroll.
 * Size changes animate, so switching folders resizes smoothly. The rail is
 * placed last so it (and the neck it draws) sits above the drawer.
 */
@Composable
private fun RailContainer(
    mirror: Boolean,
    drawerWidth: Dp,
    selectedIndex: Int,
    folderCount: Int,
    railTop: Dp,
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
    Layout(content = { drawer(); rail() }, modifier = modifier) { measurables, constraints ->
        val (drawerM, railM) = measurables
        val railW = Rail.RailWidth.roundToPx()
        val gap = Rail.Gap.roundToPx()
        val dw = drawerWidth.roundToPx()
        val railNat = Rail.naturalHeight(folderCount).roundToPx()
        val full = if (constraints.hasBoundedHeight) maxOf(constraints.maxHeight, railNat) else railNat
        val railY = railTop.roundToPx().coerceIn(0, full - railNat)
        if (size.railTop != railY) size.railTop = railY
        if (size.railHeight != railNat) size.railHeight = railNat

        val neckTop = railY + railNat - (Rail.anchorFromBottom(selectedIndex, folderCount) + Rail.Button / 2).roundToPx()
        val neckBottom = neckTop + Rail.Button.roundToPx()
        val clear = (Rail.DrawerRadius + Rail.Fillet).roundToPx()
        // Level with the first button: the top folder's neck meets a square corner.
        val startTop = railY + Rail.RailPadding.roundToPx()
        val natural = drawerM.maxIntrinsicHeight(dw).coerceAtMost(full)
        var drawerHeight = maxOf(natural, neckBottom + clear - startTop).coerceAtMost(full)
        var targetTop = startTop
        if (targetTop + drawerHeight > full) targetTop = (full - drawerHeight).coerceAtLeast(0)
        drawerHeight = drawerHeight.coerceAtMost(full - targetTop)
        val target = IntOffset(drawerHeight, targetTop)
        if (size.target != target) size.target = target

        val height = if (size.ready) size.height.value.roundToInt() else drawerHeight
        val drawerTop = if (size.ready) size.top.value.roundToInt() else targetTop
        val railP = railM.measure(Constraints.fixed(railW, railNat))
        val drawerP = drawerM.measure(Constraints.fixed(dw, height.coerceAtLeast(0)))

        val totalW = railW + gap + dw
        layout(totalW, full) {
            drawerP.place(if (mirror) 0 else railW + gap, drawerTop)
            railP.place(if (mirror) totalW - railW else 0, railY)
        }
    }
}

// ---- Rail -------------------------------------------------------------------

@Composable
private fun FolderRail(
    folders: List<RailFolderConfig>,
    selected: String,
    drawerShown: Boolean,
    mirror: Boolean,
    motion: RailMotion,
    drawerSize: DrawerSize,
    onFolder: (String) -> Unit,
    onSettings: () -> Unit,
) {
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
                        drawNeck(top, neck, upperFillet = (top - (drawerSize.top.value - drawerSize.railTop)).coerceIn(0f, Rail.Fillet.toPx()))
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
            val active = drawerShown && f.id == selected
            val tint by animateColorAsState(
                if (active) Rail.Accent else Rail.TextSecondary,
                tween(Rail.SWITCH_MS, easing = Rail.SwitchEasing),
                label = "folderTint",
            )
            RailButton(
                icon = RailIcons.get(f.icon).let { if (active) it.filled else it.outlined },
                tint = tint,
                pressColor = null,
                description = f.title,
                onClick = { onFolder(f.id) },
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
 * The drawer surface: open/close transform, background, and a tap absorber.
 * Its [content] is one [FolderContent] per visible folder, stacked; only the
 * first (incoming) one decides the drawer's height.
 *
 * Opening grows the drawer out of the selected button: it starts as a sliver
 * the button's height at the rail edge, stretches out sideways, and unfolds
 * vertically slightly behind. It's purely a layer transform (scale, offset,
 * alpha), so the GPU animates it without redrawing the drawer each frame.
 */
@Composable
private fun Drawer(mirror: Boolean, motion: RailMotion, drawerSize: DrawerSize, content: @Composable () -> Unit) {
    val pullIn = 16.dp
    Layout(
        content = content,
        modifier = Modifier
            .graphicsLayer {
                val p = motion.genie.value.coerceIn(GENIE_CLOSED, GENIE_OPEN)
                val h = size.height.coerceAtLeast(1f)
                val oy = drawerSize.neckCenterInDrawer(motion.anchor.value.dp.toPx())
                val across = (p / 0.75f).coerceIn(0f, 1f)        // width leads…
                val down = ((p - 0.15f) / 0.85f).coerceIn(0f, 1f) // …height follows
                scaleX = lerp(0.12f, 1f, across)
                scaleY = lerp((Rail.Button.toPx() / h).coerceAtMost(1f), 1f, down)
                translationX = lerp(pullIn.toPx(), 0f, across) * (if (mirror) 1f else -1f)
                transformOrigin = TransformOrigin(if (mirror) 1f else 0f, (oy / h).coerceIn(0f, 1f))
                alpha = (p * 3f).coerceAtMost(1f)
            }
            .drawBehind {
                // The rail-side top corner squares off as the neck reaches the
                // drawer's top edge (the top folder), continuously while gliding.
                val neckTop = drawerSize.neckCenterInDrawer(motion.anchor.value.dp.toPx()) - (Rail.Button / 2).toPx()
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
    folder: RailFolderConfig,
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
    ) {
        Text(
            text = folder.title,
            color = Rail.TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.01).em,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 6.dp, bottom = 10.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Scrolls only when the drawer is at the screen's full height.
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            val groups = folder.groups
            if (folder.recent) {
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
        // Contents sit at the top; any extra height (the drawer reaching up to
        // the selected button) is left empty at the bottom.
        Spacer(Modifier.weight(1f))
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
                onLongPress = { held = false; drag(app)?.let { startTransfer(it) } },
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

@Composable
private fun EmptyNote(text: String) {
    Text(text, color = Rail.TextTertiary, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp))
}
