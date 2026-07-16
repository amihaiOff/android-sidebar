package com.personal.sidebar.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
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
import com.personal.sidebar.model.ItemType
import com.personal.sidebar.model.SidebarItem

// Fixed dark palette to match the iOS-style translucent launcher look.
// A lighter tint (vs near-black) so, at a lower opacity, the backdrop blur
// reads as frosted glass instead of a dark block.
private val PanelBase = Color(0xFF2C2C2E)
// Folder cards float above the panel in a clearly lighter grey + drop shadow.
private val FolderCardBg = Color(0xFF55555C)
private val LabelPrimary = Color(0xFFF2F2F7)
private val LabelSecondary = Color(0xFFC7C7CF)
private const val COLUMNS = 4

/**
 * Full-screen overlay content: a translucent scrim plus a dark rounded panel
 * that springs in from [edge]. Renders the user's curated [items]: loose apps
 * as an icon grid and folders as expandable dropdown sections. All curation
 * happens in app settings — the panel is view-only. [panelOpacity] controls how
 * see-through the panel background is.
 */
@Composable
fun SidebarPanel(
    edge: Edge,
    items: List<SidebarItem>,
    panelOpacity: Float,
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

    val alignment = if (edge == Edge.LEFT) Alignment.CenterStart else Alignment.CenterEnd

    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(transition, enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { dismiss() }
            )
        }

        AnimatedVisibility(
            visibleState = transition,
            modifier = Modifier.align(alignment),
            enter = slideInHorizontally(
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
            ) { full -> if (edge == Edge.LEFT) -full else full } + fadeIn(),
            exit = slideOutHorizontally(
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            ) { full -> if (edge == Edge.LEFT) -full else full } + fadeOut(),
        ) {
            PanelCard(edge = edge, items = items, appMap = appMap, opacity = panelOpacity) { pkg ->
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
    opacity: Float,
    onLaunch: (String) -> Unit,
) {
    val shape = if (edge == Edge.LEFT) {
        RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
    } else {
        RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)
    }
    val systemPadding = WindowInsets.safeDrawing.asPaddingValues()
    // Which folders are expanded (defaults to expanded); ephemeral per open.
    val expanded = remember { mutableStateMapOf<String, Boolean>() }

    Box(
        modifier = Modifier
            .width(360.dp)
            .fillMaxHeight()
            .clip(shape)
            .background(PanelBase.copy(alpha = opacity.coerceIn(0.35f, 1f))),
    ) {
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
                    items.forEachIndexed { index, entry ->
                        if (entry.type == ItemType.FOLDER) {
                            val fkey = entry.key()
                            item(span = { GridItemSpan(maxLineSpan) }, key = "f$index") {
                                FolderSection(
                                    folder = entry,
                                    appMap = appMap,
                                    isExpanded = expanded[fkey] ?: true,
                                    onToggle = { expanded[fkey] = !(expanded[fkey] ?: true) },
                                    onLaunch = onLaunch,
                                )
                            }
                        } else {
                            item(key = "a$index") {
                                val app = entry.packageName?.let { appMap[it] }
                                if (app != null) {
                                    AppTile(
                                        label = app.label,
                                        bitmap = remember(app.packageName) { app.icon.toBitmap(144, 144).asImageBitmap() },
                                    ) { onLaunch(app.packageName) }
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
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onLaunch: (String) -> Unit,
) {
    // The whole folder is one floating card (drop shadow), with the dropdown
    // title INSIDE it at the top and the member apps below — like the image.
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = FolderCardBg,
        shadowElevation = 16.dp,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
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
                    val members = folder.packages.mapNotNull { appMap[it] }
                    members.chunked(COLUMNS).forEach { rowApps ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            rowApps.forEach { app ->
                                Box(Modifier.weight(1f)) {
                                    AppTile(
                                        label = app.label,
                                        bitmap = remember(app.packageName) { app.icon.toBitmap(144, 144).asImageBitmap() },
                                    ) { onLaunch(app.packageName) }
                                }
                            }
                            repeat(COLUMNS - rowApps.size) { Box(Modifier.weight(1f)) }
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
