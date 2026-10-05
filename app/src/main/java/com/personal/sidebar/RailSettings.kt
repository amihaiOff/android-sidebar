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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Work
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.personal.sidebar.apps.AppInfo
import com.personal.sidebar.model.RailConfig
import com.personal.sidebar.model.RailFolder
import com.personal.sidebar.model.RailGroup
import java.util.UUID
import kotlin.math.roundToInt

// Settings for the "thumb rail" design: its look, and the groups in each folder.

internal fun RailFolder.settingsIcon(): ImageVector = when (this) {
    RailFolder.RECENT -> Icons.Outlined.History
    RailFolder.MEDIA -> Icons.Outlined.PlayCircle
    RailFolder.PRODUCTIVITY -> Icons.Outlined.Work
    RailFolder.AI -> Icons.Outlined.AutoAwesome
    RailFolder.TOOLS -> Icons.Outlined.Build
}

/** The rail's look: overall opacity and themed icons. */
@Composable
internal fun RailLookCard(rail: RailConfig, onChange: (RailConfig) -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(Modifier.padding(16.dp)) {
            SliderRow("Opacity", rail.opacity, 0.4f..1f, "${(rail.opacity * 100).roundToInt()}%") {
                onChange(rail.copy(opacity = it))
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

/** One card per rail folder, listing its groups with edit/remove and "Add group". */
@Composable
internal fun RailFoldersSection(
    rail: RailConfig,
    appMap: Map<String, AppInfo>?,
    onChange: (RailConfig) -> Unit,
    onEditGroup: (RailFolder, String?) -> Unit,
) {
    RailFolder.entries.forEach { folder ->
        val groups = rail.groupsOf(folder)
        Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(folder.settingsIcon(), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text(folder.title, style = MaterialTheme.typography.titleMedium)
                }
                if (folder == RailFolder.RECENT) {
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
                        IconButton(onClick = { onEditGroup(folder, group?.id) }) {
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
                            IconButton(onClick = { onEditGroup(folder, group.id) }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = {
                                onChange(rail.withGroups(folder, groups.filter { it.id != group.id }))
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remove")
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { onEditGroup(folder, null) }) { Text("Add group") }
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
    folder: RailFolder,
    existing: RailGroup?,
    onSave: (RailGroup) -> Unit,
    onDelete: (() -> Unit)?,
    onCancel: () -> Unit,
) {
    val pickApps = folder != RailFolder.RECENT
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
