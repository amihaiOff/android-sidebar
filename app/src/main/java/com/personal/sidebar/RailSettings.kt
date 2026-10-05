package com.personal.sidebar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personal.sidebar.apps.AppInfo
import com.personal.sidebar.model.RailConfig
import com.personal.sidebar.model.RailFolderConfig
import com.personal.sidebar.ui.RailIcons
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.personal.sidebar.model.RailGroup
import java.util.UUID
import kotlin.math.roundToInt

// Settings for the "thumb rail" design: its look, and the groups in each folder.


private fun positionText(bias: Float): String = when {
    bias < 0.15f -> "Top"
    bias > 0.85f -> "Bottom"
    bias in 0.4f..0.6f -> "Middle"
    else -> "${(bias * 100).roundToInt()}%"
}

/** The rail's look and placement. */
@Composable
internal fun RailLookCard(rail: RailConfig, handleBias: Float, onChange: (RailConfig) -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(Modifier.padding(16.dp)) {
            SliderRow("Opacity", rail.opacity, 0.4f..1f, "${(rail.opacity * 100).roundToInt()}%") {
                onChange(rail.copy(opacity = it))
            }
            val position = rail.positionBias ?: handleBias
            SliderRow(
                "Sidebar position",
                position,
                0f..1f,
                if (rail.positionBias == null) "Level with handle" else positionText(position),
            ) {
                onChange(rail.copy(positionBias = it))
            }
            if (rail.positionBias != null) {
                TextButton(onClick = { onChange(rail.copy(positionBias = null)) }) { Text("Match handle") }
            }
            SliderRow(
                "Icon size",
                rail.iconDp.toFloat(),
                RailConfig.ICON_MIN.toFloat()..RailConfig.ICON_MAX.toFloat(),
                "${rail.iconDp} dp",
            ) {
                onChange(rail.copy(iconDp = it.roundToInt()))
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Show app names", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                Switch(checked = rail.showLabels, onCheckedChange = { onChange(rail.copy(showLabels = it)) })
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Drag to split screen", style = MaterialTheme.typography.labelLarge)
                    Text(
                        "On large screens (a foldable's inner screen), long-press an app and drag it out of the sidebar to open it next to the current app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = rail.dragToSplit, onCheckedChange = { onChange(rail.copy(dragToSplit = it)) })
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Themed icons", style = MaterialTheme.typography.labelLarge)
                    Text(
                        "Warm monochrome icons. Off shows the system's icons, including your icon theme.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = rail.themedIcons, onCheckedChange = { onChange(rail.copy(themedIcons = it)) })
            }
        }
    }
}

/**
 * One card per rail folder: its name and icon (edit), order (up/down), delete,
 * and its groups with edit/remove and "Add group". Then "Add folder".
 */
@Composable
internal fun RailFoldersSection(
    rail: RailConfig,
    appMap: Map<String, AppInfo>?,
    onChange: (RailConfig) -> Unit,
    onEditFolder: (String?) -> Unit,
    onEditGroup: (String, String?) -> Unit,
) {
    var confirmDelete by remember { mutableStateOf<RailFolderConfig?>(null) }
    val folders = rail.folders
    folders.forEachIndexed { index, folder ->
        val groups = folder.groups
        Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Column(Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(RailIcons.get(folder.icon).outlined, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        folder.title.ifBlank { "Untitled" },
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { onChange(rail.copy(folders = folders.move(index, index - 1))) }, enabled = index > 0) {
                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move up")
                    }
                    IconButton(onClick = { onChange(rail.copy(folders = folders.move(index, index + 1))) }, enabled = index < folders.lastIndex) {
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move down")
                    }
                    IconButton(onClick = { onEditFolder(folder.id) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Rename folder")
                    }
                }
                Column(Modifier.padding(end = 12.dp)) {
                    if (folder.recent) {
                        val group = groups.firstOrNull()
                        Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(group?.title?.ifBlank { null } ?: "Untitled", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "Filled with your recent apps (phone-wide with Usage access).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            IconButton(onClick = { onEditGroup(folder.id, group?.id) }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Rename")
                            }
                        }
                    } else {
                        if (groups.isEmpty()) {
                            Text(
                                "No groups yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                        groups.forEach { group ->
                            Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(group.title.ifBlank { "Untitled" }, style = MaterialTheme.typography.bodyLarge)
                                    val names = group.packages.mapNotNull { appMap?.get(it)?.label }
                                    Text(
                                        if (names.isEmpty()) "No apps" else names.joinToString(" · "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                    )
                                }
                                IconButton(onClick = { onEditGroup(folder.id, group.id) }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = {
                                    onChange(rail.withGroups(folder.id, groups.filter { it.id != group.id }))
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove")
                                }
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!folder.recent) {
                            OutlinedButton(onClick = { onEditGroup(folder.id, null) }) { Text("Add group") }
                        }
                        if (folders.size > 1) {
                            TextButton(onClick = { confirmDelete = folder }) { Text("Delete folder") }
                        }
                    }
                }
            }
        }
    }
    if (folders.size < RailConfig.MAX_FOLDERS) {
        Button(onClick = { onEditFolder(null) }, modifier = Modifier.padding(vertical = 6.dp)) { Text("Add folder") }
    } else {
        Text(
            "The rail holds up to ${RailConfig.MAX_FOLDERS} folders.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
        )
    }

    confirmDelete?.let { folder ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("Delete \"${folder.title.ifBlank { "Untitled" }}\"?") },
            text = { Text("Its groups are deleted too. The apps themselves aren't affected.") },
            confirmButton = {
                TextButton(onClick = {
                    onChange(rail.copy(folders = folders.filter { it.id != folder.id }))
                    confirmDelete = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text("Cancel") } },
        )
    }
}

private fun <T> List<T>.move(from: Int, to: Int): List<T> {
    if (to !in indices) return this
    return toMutableList().apply { add(to, removeAt(from)) }
}

/** Create or edit a rail folder: its name and its icon. */
@Composable
internal fun RailFolderEditScreen(
    modifier: Modifier,
    existing: RailFolderConfig?,
    onSave: (title: String, icon: String) -> Unit,
    onCancel: () -> Unit,
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var icon by remember { mutableStateOf(existing?.icon ?: "folder") }
    Box(modifier.fillMaxSize()) {
        SubScreen(
            title = if (existing == null) "New folder" else "Edit folder",
            trailingLabel = "Save",
            trailingEnabled = title.isNotBlank(),
            onBack = onCancel,
            onTrailing = { onSave(title.trim(), icon) },
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Folder name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            )
            Text(
                "Icon",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 8.dp, top = 12.dp, bottom = 4.dp),
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(56.dp),
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            ) {
                items(RailIcons.all, key = { it.key }) { entry ->
                    val selected = entry.key == icon
                    Box(
                        Modifier
                            .padding(4.dp)
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .clickable { icon = entry.key },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            if (selected) entry.filled else entry.outlined,
                            contentDescription = entry.key,
                            tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Create or edit a rail group: its title and (except for Recent, which fills
 * itself) its apps, in the order they were picked.
 */
@Composable
internal fun RailGroupEditScreen(
    modifier: Modifier,
    folder: RailFolderConfig,
    existing: RailGroup?,
    onSave: (RailGroup) -> Unit,
    onDelete: (() -> Unit)?,
    onCancel: () -> Unit,
) {
    val pickApps = !folder.recent
    val all = if (pickApps) rememberAllApps() else emptyList()
    var title by remember { mutableStateOf(existing?.title ?: "") }
    val selected = remember { mutableStateListOf<String>().apply { existing?.packages?.let { addAll(it) } } }

    Box(modifier.fillMaxSize()) {
        SubScreen(
            title = "${folder.title} · " + (if (existing == null) "new group" else "edit group"),
            trailingLabel = "Save",
            trailingEnabled = all != null,
            onBack = onCancel,
            onTrailing = {
                onSave(
                    RailGroup(
                        id = existing?.id ?: UUID.randomUUID().toString(),
                        title = title.trim(),
                        packages = selected.toList(),
                    )
                )
            },
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Group title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            )
            if (onDelete != null) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDelete) { Text("Delete group") }
                }
            }
            when {
                !pickApps -> Text(
                    "This group lists your most recently used apps automatically.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                )
                all == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                else -> AppMultiSelectList(
                    all = all,
                    isSelected = { selected.contains(it) },
                    onToggle = { if (!selected.remove(it)) selected.add(it) },
                )
            }
        }
    }
}
