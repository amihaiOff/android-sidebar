package com.personal.sidebar

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.personal.sidebar.service.SidebarService
import com.personal.sidebar.ui.theme.SidebarTheme
import com.personal.sidebar.util.Permissions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SidebarTheme {
                Scaffold { padding ->
                    OnboardingScreen(Modifier.padding(padding))
                }
            }
        }
    }
}

@Composable
private fun OnboardingScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var overlayGranted by remember { mutableStateOf(Permissions.canDrawOverlays(context)) }
    var batteryExempt by remember { mutableStateOf(Permissions.isIgnoringBatteryOptimizations(context)) }
    var running by remember { mutableStateOf(Settings.enabled(context)) }
    var edge by remember { mutableStateOf(Settings.edge(context)) }

    // Re-check permission status whenever we return from a Settings screen.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                overlayGranted = Permissions.canDrawOverlays(context)
                batteryExempt = Permissions.isIgnoringBatteryOptimizations(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* result ignored: the notification is cosmetic, not required */ }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text(
            text = "Sidebar",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "A swipe-in launcher on the edge of your screen.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
        )

        // 1. Overlay permission (required).
        PermissionCard(
            index = "1",
            title = "Draw over other apps",
            description = "Required. Lets the handle and panel appear on top of anything.",
            granted = overlayGranted,
        ) {
            if (!overlayGranted) {
                Button(onClick = { context.startActivity(Permissions.overlaySettingsIntent(context)) }) {
                    Text("Grant")
                }
            }
        }

        // 2. Notifications (optional, Android 13+).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionCard(
                index = "2",
                title = "Notifications",
                description = "Optional. Allows the quiet ongoing notification the service uses.",
                granted = null,
            ) {
                OutlinedButton(onClick = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                    Text("Allow")
                }
            }
        }

        // 3. Battery optimization (recommended for OEM killers).
        PermissionCard(
            index = "3",
            title = "Ignore battery optimization",
            description = "Recommended. Stops aggressive OEMs from killing the handle.",
            granted = batteryExempt,
        ) {
            if (!batteryExempt) {
                OutlinedButton(onClick = { context.startActivity(Permissions.batteryOptimizationIntent(context)) }) {
                    Text("Open")
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Edge selector.
        Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Handle side", style = MaterialTheme.typography.titleMedium)
                Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = edge == Edge.LEFT,
                        onClick = { edge = Edge.LEFT; applyEdge(context, Edge.LEFT, running) },
                        label = { Text("Left") },
                    )
                    FilterChip(
                        selected = edge == Edge.RIGHT,
                        onClick = { edge = Edge.RIGHT; applyEdge(context, Edge.RIGHT, running) },
                        label = { Text("Right") },
                    )
                }
            }
        }

        // Enable switch.
        Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Enable sidebar", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (overlayGranted) "Start the edge handle." else "Grant the overlay permission first.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = running,
                    enabled = overlayGranted,
                    onCheckedChange = { on ->
                        running = on
                        Settings.setEnabled(context, on)
                        if (on) SidebarService.start(context) else SidebarService.stop(context)
                    },
                )
            }
        }
    }
}

private fun applyEdge(context: Context, edge: Edge, running: Boolean) {
    Settings.setEdge(context, edge)
    if (running) SidebarService.updateEdge(context)
}

@Composable
private fun PermissionCard(
    index: String,
    title: String,
    description: String,
    granted: Boolean?,
    action: @Composable () -> Unit,
) {
    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (granted != null) {
                Icon(
                    imageVector = if (granted) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (granted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text("$index. $title", style = MaterialTheme.typography.titleMedium)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(12.dp))
            action()
        }
    }
}
