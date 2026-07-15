package com.personal.sidebar.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.personal.sidebar.Edge
import com.personal.sidebar.apps.AppInfo
import com.personal.sidebar.apps.AppRepository
import com.personal.sidebar.ui.theme.SidebarTheme

/**
 * Full-screen overlay content: a translucent scrim plus a rounded card that
 * springs in from [edge]. Tapping the scrim, pressing back, or launching an app
 * dismisses it; [onDismissed] fires only after the exit animation finishes so
 * the host can safely remove the window.
 *
 * [registerDismiss] hands the host a function to trigger dismissal from outside
 * Compose (e.g. the back key).
 */
@Composable
fun SidebarPanel(
    edge: Edge,
    registerDismiss: (() -> Unit) -> Unit,
    onDismissed: () -> Unit,
) {
    SidebarTheme {
        val context = LocalContext.current
        val transition = remember { MutableTransitionState(false).apply { targetState = true } }
        val dismiss = remember { { transition.targetState = false } }

        // Expose the dismiss trigger to the host and tear down once fully hidden.
        LaunchedEffect(Unit) { registerDismiss(dismiss) }
        LaunchedEffect(transition.currentState, transition.isIdle) {
            if (transition.isIdle && !transition.currentState) onDismissed()
        }

        var apps by remember { mutableStateOf<List<AppInfo>?>(null) }
        LaunchedEffect(Unit) { apps = AppRepository.load(context) }

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
                PanelCard(edge = edge, apps = apps) { pkg ->
                    AppRepository.launch(context, pkg)
                    dismiss()
                }
            }
        }
    }
}

@Composable
private fun PanelCard(edge: Edge, apps: List<AppInfo>?, onLaunch: (String) -> Unit) {
    // Round only the corners facing away from the edge for a "side sheet" look.
    val shape = if (edge == Edge.LEFT) {
        RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
    } else {
        RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)
    }
    val systemPadding = WindowInsets.safeDrawing.asPaddingValues()

    Surface(
        modifier = Modifier
            .width(340.dp)
            .fillMaxHeight(),
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
            Text(
                text = "Apps",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp),
            )

            when {
                apps == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                apps.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No launchable apps found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(apps, key = { it.packageName }) { app ->
                        AppTile(app, onLaunch)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppTile(app: AppInfo, onLaunch: (String) -> Unit) {
    val bitmap = remember(app.packageName) {
        app.icon.toBitmap(width = 144, height = 144).asImageBitmap()
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onLaunch(app.packageName) }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = app.label,
            modifier = Modifier.size(52.dp),
        )
        Text(
            text = app.label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
