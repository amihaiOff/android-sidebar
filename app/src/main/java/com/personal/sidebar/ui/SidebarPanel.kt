package com.personal.sidebar.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.personal.sidebar.Edge
import com.personal.sidebar.apps.AppInfo
import com.personal.sidebar.apps.AppRepository
import com.personal.sidebar.model.ItemType
import com.personal.sidebar.model.SidebarItem
import com.personal.sidebar.ui.theme.SidebarTheme

/**
 * Full-screen overlay content: a translucent scrim plus a rounded card that
 * springs in from [edge]. It renders the user's curated [items] (apps and
 * folders); tapping a folder drills in, tapping an app launches it. Tapping the
 * scrim, pressing back, or launching an app dismisses it; [onDismissed] fires
 * only after the exit animation finishes so the host can safely remove the
 * window. [registerDismiss] hands the host a dismissal trigger for the back key.
 */
@Composable
fun SidebarPanel(
    edge: Edge,
    items: List<SidebarItem>,
    registerDismiss: (() -> Unit) -> Unit,
    onDismissed: () -> Unit,
) {
    SidebarTheme {
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
                        .background(Color.Black.copy(alpha = 0.45f))
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
                PanelCard(edge = edge, items = items, appMap = appMap) { pkg ->
                    AppRepository.launch(context, pkg)
                    dismiss()
                }
            }
        }
    }
}

@Composable
private fun PanelCard(
    edge: Edge,
    items: List<SidebarItem>,
    appMap: Map<String, AppInfo>?,
    onLaunch: (String) -> Unit,
) {
    val shape = if (edge == Edge.LEFT) {
        RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
    } else {
        RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)
    }
    val systemPadding = WindowInsets.safeDrawing.asPaddingValues()
    var openFolder by remember { mutableStateOf<SidebarItem?>(null) }

    Surface(
        modifier = Modifier.width(340.dp).fillMaxHeight(),
        shape = shape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        tonalElevation = 3.dp,
        shadowElevation = 12.dp,
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(
                    top = systemPadding.calculateTopPadding() + 20.dp,
                    bottom = systemPadding.calculateBottomPadding() + 12.dp,
                    start = 16.dp,
                    end = 16.dp,
                )
        ) {
            when {
                appMap == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No apps yet.\nAdd some in Sidebar settings.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                else -> Crossfade(targetState = openFolder, label = "folder") { folder ->
                    if (folder == null) {
                        AppGrid(
                            header = "Apps",
                            entries = items,
                            appMap = appMap,
                            onApp = onLaunch,
                            onFolder = { openFolder = it },
                        )
                    } else {
                        val members = folder.packages.map { SidebarItem.app(it) }
                        AppGrid(
                            header = folder.name ?: "Folder",
                            entries = members,
                            appMap = appMap,
                            onApp = onLaunch,
                            onFolder = {},
                            onBack = { openFolder = null },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppGrid(
    header: String,
    entries: List<SidebarItem>,
    appMap: Map<String, AppInfo>,
    onApp: (String) -> Unit,
    onFolder: (SidebarItem) -> Unit,
    onBack: (() -> Unit)? = null,
) {
    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            Text(
                text = header,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = if (onBack != null) 0.dp else 4.dp),
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(entries, key = { it.key() }) { entry ->
                if (entry.type == ItemType.FOLDER) {
                    FolderTile(entry, appMap) { onFolder(entry) }
                } else {
                    val app = entry.packageName?.let { appMap[it] }
                    if (app != null) AppTile(app.label, remember(app.packageName) { app.icon.toBitmap(144, 144).asImageBitmap() }) {
                        onApp(app.packageName)
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
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(bitmap = bitmap, contentDescription = label, modifier = Modifier.size(52.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun FolderTile(folder: SidebarItem, appMap: Map<String, AppInfo>, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val previews = folder.packages.mapNotNull { appMap[it] }.take(4)
        Surface(
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            if (previews.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp),
                    )
                }
            } else {
                // 2x2 mini grid of member icons.
                Column(
                    Modifier.fillMaxSize().padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    previews.chunked(2).forEach { row ->
                        Row(
                            Modifier.weight(1f).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            row.forEach { app ->
                                Image(
                                    bitmap = remember(app.packageName) { app.icon.toBitmap(72, 72).asImageBitmap() },
                                    contentDescription = null,
                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                )
                            }
                            if (row.size == 1) Box(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        Text(
            text = folder.name ?: "Folder",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

/** Stable list key for a curated entry. */
private fun SidebarItem.key(): String = when (type) {
    ItemType.APP -> "app:${packageName}"
    ItemType.FOLDER -> "folder:${name}:${packages.joinToString(",")}"
}
