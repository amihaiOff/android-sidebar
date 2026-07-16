package com.personal.sidebar

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.personal.sidebar.model.HandleConfig
import com.personal.sidebar.model.ItemType
import com.personal.sidebar.model.SidebarConfig
import com.personal.sidebar.model.SidebarItem
import com.personal.sidebar.service.SidebarService
import com.personal.sidebar.ui.theme.SidebarTheme
import com.personal.sidebar.util.Permissions

/** In-app navigation without a nav library — three simple destinations. */
private sealed interface Screen {
    data object Home : Screen
    data object AddApps : Screen
    data object GlassLab : Screen
    data class FolderEdit(val index: Int?, val isGroup: Boolean = false) : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SidebarTheme { SidebarRoot() } }
    }
}

@Composable
private fun SidebarRoot() {
    val context = LocalContext.current
    var config by remember { mutableStateOf(Settings.config(context)) }
    var running by remember { mutableStateOf(Settings.enabled(context)) }
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }

    fun commit(new: SidebarConfig) {
        config = new
        Settings.setConfig(context, new)
        if (running) SidebarService.refresh(context)
    }

    // Persist without re-arming the handle — for panel/folder look changes,
    // which are read fresh each time the panel opens (no handle refresh needed).
    fun persist(new: SidebarConfig) {
        config = new
        Settings.setConfig(context, new)
    }

    Scaffold { padding ->
        val mod = Modifier.padding(padding)
        when (val s = screen) {
            Screen.Home -> HomeScreen(
                modifier = mod,
                config = config,
                running = running,
                onConfigChange = ::commit,
                onRunningChange = { on ->
                    running = on
                    Settings.setEnabled(context, on)
                    if (on) SidebarService.start(context) else SidebarService.stop(context)
                },
                onAddApps = { screen = Screen.AddApps },
                onNewFolder = { screen = Screen.FolderEdit(null, isGroup = false) },
                onNewGroup = { screen = Screen.FolderEdit(null, isGroup = true) },
                onOpenGlassLab = { screen = Screen.GlassLab },
                onEditFolder = { index -> screen = Screen.FolderEdit(index) },
                onRemoveItem = { index ->
                    commit(config.copy(items = config.items.filterIndexed { i, _ -> i != index }))
                },
                onReorder = { newItems -> commit(config.copy(items = newItems)) },
            )

            Screen.AddApps -> AddAppsScreen(
                modifier = mod,
                config = config,
                onDone = { newItems -> commit(config.copy(items = newItems)); screen = Screen.Home },
                onCancel = { screen = Screen.Home },
            )

            Screen.GlassLab -> GlassLabScreen(
                modifier = mod,
                panel = config.panel,
                folder = config.folder,
                onChange = { newPanel, newFolder ->
                    persist(config.copy(panel = newPanel, folder = newFolder))
                },
                onBack = { screen = Screen.Home },
            )

            is Screen.FolderEdit -> {
                val idx = s.index
                val existingItem = idx?.let { config.items.getOrNull(it) }
                FolderEditScreen(
                    modifier = mod,
                    existing = existingItem,
                    asGroup = existingItem?.type == ItemType.GROUP || (idx == null && s.isGroup),
                    onSave = { folder ->
                        val items = config.items.toMutableList()
                        if (idx == null) items.add(folder) else items[idx] = folder
                        commit(config.copy(items = items)); screen = Screen.Home
                    },
                    onDelete = if (idx != null) {
                        {
                            commit(config.copy(items = config.items.filterIndexed { i, _ -> i != idx }))
                            screen = Screen.Home
                        }
                    } else null,
                    onCancel = { screen = Screen.Home },
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    modifier: Modifier,
    config: SidebarConfig,
    running: Boolean,
    onConfigChange: (SidebarConfig) -> Unit,
    onRunningChange: (Boolean) -> Unit,
    onAddApps: () -> Unit,
    onNewFolder: () -> Unit,
    onNewGroup: () -> Unit,
    onOpenGlassLab: () -> Unit,
    onEditFolder: (Int) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onReorder: (List<SidebarItem>) -> Unit,
) {
    val context = LocalContext.current
    var overlayGranted by remember { mutableStateOf(Permissions.canDrawOverlays(context)) }
    var batteryExempt by remember { mutableStateOf(Permissions.isIgnoringBatteryOptimizations(context)) }
    var usageGranted by remember { mutableStateOf(Permissions.hasUsageAccess(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                overlayGranted = Permissions.canDrawOverlays(context)
                batteryExempt = Permissions.isIgnoringBatteryOptimizations(context)
                usageGranted = Permissions.hasUsageAccess(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    val handle = config.handle
    fun setHandle(h: HandleConfig) = onConfigChange(config.copy(handle = h))

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text("Sidebar", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "A swipe-in launcher on the edge of your screen.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
        )

        // --- Permissions -----------------------------------------------------
        PermissionCard("Draw over other apps", "Required. Shows the handle and panel on top.", overlayGranted) {
            if (!overlayGranted) Button(onClick = { context.startActivity(Permissions.overlaySettingsIntent(context)) }) { Text("Grant") }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionCard("Notifications", "Optional. Allows the quiet ongoing notification.", null) {
                OutlinedButton(onClick = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) { Text("Allow") }
            }
        }
        PermissionCard("Ignore battery optimization", "Recommended. Stops OEMs killing the handle.", batteryExempt) {
            if (!batteryExempt) OutlinedButton(onClick = { context.startActivity(Permissions.batteryOptimizationIntent(context)) }) { Text("Open") }
        }
        PermissionCard("Usage access", "Optional. Shows your phone-wide recent apps in the panel.", usageGranted) {
            if (!usageGranted) OutlinedButton(onClick = { context.startActivity(Permissions.usageAccessIntent()) }) { Text("Open") }
        }

        Spacer(Modifier.height(8.dp))
        SectionTitle("Appearance")
        Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Column(Modifier.padding(16.dp)) {
                HandlePreview(handle)
                Spacer(Modifier.height(16.dp))

                LabeledRow("Side") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(handle.edge == Edge.LEFT, { setHandle(handle.copy(edge = Edge.LEFT)) }, { Text("Left") })
                        FilterChip(handle.edge == Edge.RIGHT, { setHandle(handle.copy(edge = Edge.RIGHT)) }, { Text("Right") })
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("Color", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SWATCHES.forEach { rgb ->
                        ColorSwatch(
                            rgb = rgb,
                            selected = (handle.colorArgb and 0x00FFFFFF) == (rgb and 0x00FFFFFF),
                            onClick = { setHandle(handle.copy(colorArgb = withRgb(handle.colorArgb, rgb))) },
                        )
                    }
                }

                SliderRow("Opacity", alphaOf(handle.colorArgb).toFloat(), 40f..255f) {
                    setHandle(handle.copy(colorArgb = withAlpha(handle.colorArgb, it.toInt())))
                }
                SliderRow("Width", handle.widthDp.toFloat(), 8f..48f, "${handle.widthDp} dp") {
                    setHandle(handle.copy(widthDp = it.toInt()))
                }
                SliderRow("Length", handle.lengthDp.toFloat(), 60f..400f, "${handle.lengthDp} dp") {
                    setHandle(handle.copy(lengthDp = it.toInt()))
                }
                SliderRow("Position", handle.verticalBias, 0f..1f, positionLabel(handle.verticalBias)) {
                    setHandle(handle.copy(verticalBias = it))
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "The panel's frosted-glass look (blur, tint, edge, background) is tuned in the lab.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onOpenGlassLab) { Text("Panel & glass…") }
            }
        }

        // --- Apps & folders --------------------------------------------------
        Spacer(Modifier.height(8.dp))
        SectionTitle("Apps & folders")
        Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Column(Modifier.padding(16.dp)) {
                if (config.items.isEmpty()) {
                    Text(
                        "No apps added yet. Use the buttons below to choose which apps appear in the panel.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        "Long-press the handle to drag and reorder.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    ReorderableItems(
                        items = config.items,
                        appMap = rememberAppMap(),
                        onEditFolder = onEditFolder,
                        onRemove = onRemoveItem,
                        onReorder = onReorder,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onAddApps) { Text("Add apps") }
                    OutlinedButton(onClick = onNewGroup) { Text("New group") }
                    OutlinedButton(onClick = onNewFolder) { Text("New folder") }
                }
            }
        }

        // --- Enable ----------------------------------------------------------
        Spacer(Modifier.height(8.dp))
        Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Enable sidebar", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (overlayGranted) "Start the edge handle." else "Grant the overlay permission first.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = running, enabled = overlayGranted, onCheckedChange = onRunningChange)
            }
        }
    }
}

// ---- Appearance building blocks -------------------------------------------

private val SWATCHES = listOf(
    0xFF4C5BD4.toInt(), 0xFF2196F3.toInt(), 0xFF009688.toInt(), 0xFF4CAF50.toInt(),
    0xFFFF9800.toInt(), 0xFFF44336.toInt(), 0xFF9C27B0.toInt(), 0xFF607D8B.toInt(),
    0xFFFFFFFF.toInt(), 0xFF111111.toInt(),
)

private fun alphaOf(argb: Int): Int = (argb ushr 24) and 0xFF
private fun withAlpha(argb: Int, alpha: Int): Int = (alpha shl 24) or (argb and 0x00FFFFFF)
private fun withRgb(argb: Int, rgb: Int): Int = (argb.toLong() and 0xFF000000L).toInt() or (rgb and 0x00FFFFFF)

private fun positionLabel(bias: Float): String = when {
    bias < 0.2f -> "Top"
    bias > 0.8f -> "Bottom"
    bias in 0.4f..0.6f -> "Center"
    else -> "${(bias * 100).toInt()}%"
}

@Composable
private fun HandlePreview(handle: HandleConfig) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        val horiz = if (handle.edge == Edge.LEFT) -1f else 1f
        val vert = handle.verticalBias * 2f - 1f
        val previewShape = if (handle.edge == Edge.LEFT) {
            RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
        } else {
            RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
        }
        Box(
            Modifier
                .align(BiasAlignment(horiz, vert))
                .width(handle.widthDp.dp)
                .height((handle.lengthDp * 0.28f).dp.coerceAtLeastDp())
                .clip(previewShape)
                .background(Color(handle.colorArgb)),
        )
    }
}

// Keep the preview pill from vanishing at tiny lengths.
private fun androidx.compose.ui.unit.Dp.coerceAtLeastDp() = if (value < 12f) 12.dp else this

@Composable
private fun ColorSwatch(rgb: Int, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(32.dp),
        shape = RoundedCornerShape(50),
        color = Color(rgb or 0xFF000000.toInt()),
        border = if (selected) androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface) else null,
        onClick = onClick,
    ) {}
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    valueLabel: String? = null,
    onChange: (Float) -> Unit,
) {
    Column(Modifier.padding(top = 8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            if (valueLabel != null) Text(valueLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Slider(value = value.coerceIn(range.start, range.endInclusive), onValueChange = onChange, valueRange = range)
    }
}

@Composable
private fun LabeledRow(label: String, content: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(60.dp))
        content()
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp, start = 2.dp))
}

/**
 * A vertically draggable list of the curated items. Long-press the drag handle
 * to pick a row up; it follows the finger and swaps with neighbours as it
 * crosses their midpoints. The new order is committed once on drag end (not on
 * every micro-swap), so the running sidebar isn't refreshed mid-drag.
 */
@Composable
private fun ReorderableItems(
    items: List<SidebarItem>,
    appMap: Map<String, com.personal.sidebar.apps.AppInfo>?,
    onEditFolder: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onReorder: (List<SidebarItem>) -> Unit,
) {
    // Working copy so drags mutate locally; synced from source when not dragging.
    val working = remember { mutableStateListOf<SidebarItem>().apply { addAll(items) } }
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }
    var rowHeight by remember { mutableStateOf(0f) }

    LaunchedEffect(items) {
        if (draggingIndex == null) {
            working.clear(); working.addAll(items)
        }
    }

    Column(Modifier.fillMaxWidth()) {
        working.forEachIndexed { index, item ->
            key(itemKey(item)) {
                val currentIndex by rememberUpdatedState(index)
                val count by rememberUpdatedState(working.size)
                val isDragging = draggingIndex == index
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { if (rowHeight == 0f) rowHeight = it.height.toFloat() }
                        .zIndex(if (isDragging) 1f else 0f)
                        .graphicsLayer { translationY = if (isDragging) dragOffset else 0f }
                        .background(
                            if (isDragging) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                            RoundedCornerShape(12.dp),
                        )
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Filled.DragHandle,
                        contentDescription = "Reorder",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .pointerInput(Unit) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { draggingIndex = currentIndex; dragOffset = 0f },
                                    onDragEnd = {
                                        draggingIndex = null; dragOffset = 0f
                                        onReorder(working.toList())
                                    },
                                    onDragCancel = {
                                        draggingIndex = null; dragOffset = 0f
                                        onReorder(working.toList())
                                    },
                                    onDrag = { change, amount ->
                                        change.consume()
                                        dragOffset += amount.y
                                        val cur = draggingIndex
                                        val h = if (rowHeight > 0f) rowHeight else 1f
                                        if (cur != null) {
                                            if (dragOffset > h / 2 && cur < count - 1) {
                                                working.add(cur + 1, working.removeAt(cur))
                                                draggingIndex = cur + 1; dragOffset -= h
                                            } else if (dragOffset < -h / 2 && cur > 0) {
                                                working.add(cur - 1, working.removeAt(cur))
                                                draggingIndex = cur - 1; dragOffset += h
                                            }
                                        }
                                    },
                                )
                            },
                    )
                    if (item.type == ItemType.FOLDER || item.type == ItemType.GROUP) {
                        val glyph = item.emoji?.takeIf { it.isNotBlank() }
                        when {
                            glyph != null -> Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) { Text(glyph, fontSize = 24.sp) }
                            item.type == ItemType.GROUP -> Icon(Icons.Filled.Dashboard, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                            else -> Icon(Icons.Filled.Folder, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.name ?: if (item.type == ItemType.GROUP) "Group" else "Folder", style = MaterialTheme.typography.bodyLarge)
                            val kind = if (item.type == ItemType.GROUP) "group" else "folder"
                            Text("${item.packages.size} apps · $kind", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onEditFolder(currentIndex) }) { Icon(Icons.Filled.Edit, contentDescription = "Edit") }
                    } else {
                        val app = item.packageName?.let { appMap?.get(it) }
                        AppIconSmall(item.packageName, appMap, 36)
                        Spacer(Modifier.width(12.dp))
                        Text(app?.label ?: item.packageName ?: "Unknown", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    }
                    IconButton(onClick = { onRemove(currentIndex) }) { Icon(Icons.Filled.Delete, contentDescription = "Remove") }
                }
            }
        }
    }
}

/** Stable identity for reorder/keying. */
private fun itemKey(item: SidebarItem): String = when (item.type) {
    ItemType.APP -> "app:${item.packageName}"
    ItemType.FOLDER -> "folder:${item.name}:${item.packages.joinToString(",")}"
    ItemType.GROUP -> "group:${item.name}:${item.packages.joinToString(",")}"
}

@Composable
private fun PermissionCard(title: String, description: String, granted: Boolean?, action: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (granted != null) {
                Icon(
                    imageVector = if (granted) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (granted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(12.dp))
            action()
        }
    }
}
