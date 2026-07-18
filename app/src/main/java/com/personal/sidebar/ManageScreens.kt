package com.personal.sidebar

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.Color
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

/** Apps that can open an https URL (browsers + any URL handler), for the link
 *  "Open with" picker. */
@Composable
internal fun rememberBrowsers(): List<AppInfo> {
    val context = LocalContext.current
    return remember {
        val pm = context.packageManager
        val probe = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com"))
        runCatching {
            pm.queryIntentActivities(probe, 0).mapNotNull { ri ->
                val pkg = ri.activityInfo?.packageName ?: return@mapNotNull null
                AppInfo(ri.loadLabel(pm).toString(), pkg, ri.loadIcon(pm))
            }.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
        }.getOrDefault(emptyList())
    }
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
                // Keep folders/groups and still-selected existing apps; append new apps.
                val kept = config.items.filter { it.type != ItemType.APP || it.packageName in selectedSet }
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
    asGroup: Boolean,
    onSave: (SidebarItem) -> Unit,
    onDelete: (() -> Unit)?,
    onCancel: () -> Unit,
) {
    val all = rememberAllApps()
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var iconKey by remember { mutableStateOf(existing?.iconKey) }
    var colorArgb by remember { mutableStateOf(existing?.colorArgb) }
    var iconQuery by remember { mutableStateOf("") }
    val selected = remember { mutableStateListOf<String>().apply { existing?.packages?.let { addAll(it) } } }
    val links = remember { mutableStateListOf<SidebarItem>().apply { existing?.links?.let { addAll(it) } } }
    var showLinkDialog by remember { mutableStateOf(false) }
    val noun = if (asGroup) "group" else "folder"

    Box(modifier.fillMaxSize()) {
        SubScreen(
            title = (if (existing == null) "New " else "Edit ") + noun,
            trailingLabel = "Save",
            // Name is optional for folders (the icon is the identity); groups
            // still want a title. Needs at least one member (app or link).
            trailingEnabled = all != null && (selected.isNotEmpty() || links.isNotEmpty()) && (!asGroup || name.isNotBlank()),
            onBack = onCancel,
            onTrailing = {
                onSave(
                    (if (asGroup) SidebarItem.group(name.trim(), selected.toList())
                    else SidebarItem.folder(name.trim(), selected.toList(), existing?.emoji, iconKey, colorArgb))
                        .copy(links = links.toList())
                )
            },
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(if (asGroup) "Group title" else "Folder name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            )

            if (!asGroup) {
                // Icon picker: an outline icon coloured to the theme (or a picked
                // colour). Replaces the old emoji field.
                OutlinedTextField(
                    value = iconQuery,
                    onValueChange = { iconQuery = it },
                    label = { Text("Search icons") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                )
                val tint = colorArgb?.let { Color(it) } ?: MaterialTheme.colorScheme.primary
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier.fillMaxWidth().height(150.dp).padding(horizontal = 8.dp),
                ) {
                    items(FolderIcons.search(iconQuery), key = { it.key }) { entry ->
                        val isSel = iconKey == entry.key
                        Box(
                            Modifier
                                .padding(4.dp)
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.Transparent)
                                .clickable { iconKey = if (isSel) null else entry.key },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(entry.icon, contentDescription = entry.key, tint = tint, modifier = Modifier.size(26.dp))
                        }
                    }
                }
                // Colour row: "auto" (theme) first, then the palette.
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    FOLDER_ICON_COLORS.forEach { c ->
                        val isSel = colorArgb == c
                        val ring = if (isSel) MaterialTheme.colorScheme.onSurface else Color.White.copy(alpha = 0.25f)
                        Box(
                            Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (c == null) MaterialTheme.colorScheme.surfaceVariant else Color(c))
                                .border(if (isSel) 3.dp else 1.dp, ring, CircleShape)
                                .clickable { colorArgb = c },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (c == null) Text("A", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Links in this folder/group.
            Row(
                Modifier.fillMaxWidth().padding(start = 8.dp, end = 4.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Links", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                TextButton(onClick = { showLinkDialog = true }) { Text("Add link") }
            }
            links.forEachIndexed { i, link ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val glyph = link.emoji?.takeIf { it.isNotBlank() }
                    if (glyph != null) {
                        Text(glyph, modifier = Modifier.width(28.dp))
                    } else {
                        Icon(Icons.Outlined.Language, contentDescription = null, modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(6.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Column(Modifier.weight(1f)) {
                        Text(link.name?.ifBlank { null } ?: "Link", style = MaterialTheme.typography.bodyMedium)
                        Text(link.url ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                    IconButton(onClick = { links.removeAt(i) }) { Icon(Icons.Filled.Close, contentDescription = "Remove link") }
                }
            }

            if (onDelete != null) {
                TextButton(onClick = onDelete, modifier = Modifier.padding(start = 4.dp)) {
                    Text("Delete $noun", color = MaterialTheme.colorScheme.error)
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

        if (showLinkDialog) {
            var lEmoji by remember { mutableStateOf("") }
            var lName by remember { mutableStateOf("") }
            var lUrl by remember { mutableStateOf("https://") }
            val urlOk = lUrl.trim().length > "https://".length && lUrl.contains(".")
            AlertDialog(
                onDismissRequest = { showLinkDialog = false },
                title = { Text("Add link") },
                text = {
                    Column {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(lEmoji, { lEmoji = it.take(4) }, label = { Text("Emoji") }, singleLine = true, modifier = Modifier.width(92.dp))
                            OutlinedTextField(lName, { lName = it }, label = { Text("Label") }, singleLine = true, modifier = Modifier.weight(1f))
                        }
                        OutlinedTextField(lUrl, { lUrl = it }, label = { Text("URL") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = lName.isNotBlank() && urlOk,
                        onClick = {
                            val u = lUrl.trim().let { if (it.startsWith("http://") || it.startsWith("https://")) it else "https://$it" }
                            links.add(SidebarItem.link(lName.trim(), u, lEmoji.trim().ifBlank { null }))
                            showLinkDialog = false
                        },
                    ) { Text("Add") }
                },
                dismissButton = { TextButton(onClick = { showLinkDialog = false }) { Text("Cancel") } },
            )
        }
    }
}

/**
 * Add or edit a web link / PWA tile. A link opens a URL via ACTION_VIEW — if a
 * PWA (WebAPK) is installed for that URL, Android launches it; otherwise the
 * browser opens. The optional emoji becomes the tile icon (a globe otherwise).
 */
@Composable
internal fun LinkEditScreen(
    modifier: Modifier,
    existing: SidebarItem?,
    onSave: (SidebarItem) -> Unit,
    onDelete: (() -> Unit)?,
    onCancel: () -> Unit,
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var emoji by remember { mutableStateOf(existing?.emoji ?: "") }
    var url by remember { mutableStateOf(existing?.url ?: "https://") }
    var target by remember { mutableStateOf(existing?.targetPackage) }
    val browsers = rememberBrowsers()

    fun normalizedUrl(): String {
        val u = url.trim()
        return if (u.startsWith("http://") || u.startsWith("https://")) u else "https://$u"
    }
    val urlValid = url.trim().let { it.length > "https://".length && it.contains(".") }

    Box(modifier.fillMaxSize()) {
        SubScreen(
            title = if (existing == null) "Add link" else "Edit link",
            trailingLabel = "Save",
            trailingEnabled = urlValid && name.isNotBlank(),
            onBack = onCancel,
            onTrailing = { onSave(SidebarItem.link(name.trim(), normalizedUrl(), emoji.trim().ifBlank { null }, target)) },
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        label = { Text("Label") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                )
                Text(
                    "Opens the URL. If you've installed it as a PWA (added to home screen), Android opens the PWA; otherwise it opens in your browser.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp),
                )

                // "Open with" — force the URL into a specific app. Useful when a
                // PWA was installed via a browser (e.g. Vivaldi) that opens it
                // standalone: picking that browser makes the tile launch it like
                // the home-screen icon does.
                Text(
                    "Open with",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                )
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = target == null,
                        onClick = { target = null },
                        label = { Text("System default") },
                    )
                    browsers.forEach { app ->
                        FilterChip(
                            selected = target == app.packageName,
                            onClick = { target = app.packageName },
                            label = { Text(app.label) },
                        )
                    }
                }

                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text("Delete link", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
