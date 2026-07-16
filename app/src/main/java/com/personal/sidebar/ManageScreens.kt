package com.personal.sidebar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.personal.sidebar.apps.AppInfo
import com.personal.sidebar.apps.AppRepository
import com.personal.sidebar.model.ItemType
import com.personal.sidebar.model.SidebarConfig
import com.personal.sidebar.model.SidebarItem

@Composable
internal fun rememberAllApps(): List<AppInfo>? {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<AppInfo>?>(null) }
    LaunchedEffect(Unit) { apps = AppRepository.load(context) }
    return apps
}

@Composable
internal fun rememberAppMap(): Map<String, AppInfo>? {
    val context = LocalContext.current
    var map by remember { mutableStateOf<Map<String, AppInfo>?>(null) }
    LaunchedEffect(Unit) { map = AppRepository.map(context) }
    return map
}

@Composable
internal fun AppIconSmall(pkg: String?, appMap: Map<String, AppInfo>?, sizeDp: Int) {
    val app = pkg?.let { appMap?.get(it) }
    if (app != null) {
        Image(
            bitmap = remember(app.packageName) { app.icon.toBitmap(96, 96).asImageBitmap() },
            contentDescription = app.label,
            modifier = Modifier.size(sizeDp.dp),
        )
    } else {
        Box(
            Modifier
                .size(sizeDp.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
    }
}

@Composable
private fun SubScreen(
    title: String,
    trailingLabel: String,
    trailingEnabled: Boolean,
    onBack: () -> Unit,
    onTrailing: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            TextButton(onClick = onTrailing, enabled = trailingEnabled) { Text(trailingLabel) }
        }
        content()
    }
}

@Composable
private fun AppMultiSelectList(
    all: List<AppInfo>,
    isSelected: (String) -> Boolean,
    onToggle: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(all, query) {
        if (query.isBlank()) all else all.filter { it.label.contains(query, ignoreCase = true) }
    }
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search apps") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        )
        LazyColumn(Modifier.fillMaxSize()) {
            items(filtered, key = { it.packageName }) { app ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onToggle(app.packageName) }
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        bitmap = remember(app.packageName) { app.icon.toBitmap(96, 96).asImageBitmap() },
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(app.label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Checkbox(checked = isSelected(app.packageName), onCheckedChange = { onToggle(app.packageName) })
                }
            }
        }
    }
}

@Composable
internal fun AddAppsScreen(
    modifier: Modifier,
    config: SidebarConfig,
    onDone: (List<SidebarItem>) -> Unit,
    onCancel: () -> Unit,
) {
    val all = rememberAllApps()
    val existingAppPkgs = remember(config) {
        config.items.filter { it.type == ItemType.APP }.mapNotNull { it.packageName }
    }
    val selected = remember { mutableStateListOf<String>().apply { addAll(existingAppPkgs) } }

    Box(modifier.fillMaxSize()) {
        SubScreen(
            title = "Add apps",
            trailingLabel = "Done",
            trailingEnabled = all != null,
            onBack = onCancel,
            onTrailing = {
                val selectedSet = selected.toSet()
                val existingSet = existingAppPkgs.toSet()
                // Keep folders and still-selected existing apps in place; append new apps.
                val kept = config.items.filter { it.type == ItemType.FOLDER || it.packageName in selectedSet }
                val added = selected.filter { it !in existingSet }.map { SidebarItem.app(it) }
                onDone(kept + added)
            },
        ) {
            if (all == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                AppMultiSelectList(
                    all = all,
                    isSelected = { selected.contains(it) },
                    onToggle = { if (!selected.remove(it)) selected.add(it) },
                )
            }
        }
    }
}

@Composable
internal fun FolderEditScreen(
    modifier: Modifier,
    existing: SidebarItem?,
    onSave: (SidebarItem) -> Unit,
    onDelete: (() -> Unit)?,
    onCancel: () -> Unit,
) {
    val all = rememberAllApps()
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var emoji by remember { mutableStateOf(existing?.emoji ?: "") }
    val selected = remember { mutableStateListOf<String>().apply { existing?.packages?.let { addAll(it) } } }

    Box(modifier.fillMaxSize()) {
        SubScreen(
            title = if (existing == null) "New folder" else "Edit folder",
            trailingLabel = "Save",
            trailingEnabled = all != null && name.isNotBlank() && selected.isNotEmpty(),
            onBack = onCancel,
            onTrailing = { onSave(SidebarItem.folder(name.trim(), selected.toList(), emoji.trim().ifBlank { null })) },
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { emoji = it.take(4) },
                    label = { Text("Emoji") },
                    singleLine = true,
                    modifier = Modifier.width(96.dp),
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Folder name") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }
            if (onDelete != null) {
                TextButton(onClick = onDelete, modifier = Modifier.padding(start = 4.dp)) {
                    Text("Delete folder", color = MaterialTheme.colorScheme.error)
                }
            }
            if (all == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                AppMultiSelectList(
                    all = all,
                    isSelected = { selected.contains(it) },
                    onToggle = { if (!selected.remove(it)) selected.add(it) },
                )
            }
        }
    }
}
