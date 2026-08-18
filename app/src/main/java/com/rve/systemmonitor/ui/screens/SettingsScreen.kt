package com.rve.systemmonitor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rve.systemmonitor.R
import com.rve.systemmonitor.ui.components.ExitUntilCollapsedMediumTopAppBar
import com.rve.systemmonitor.ui.components.haptic.hapticClickable

private data class SettingsItem(val title: String, val subtitle: String, val iconRes: Int, val onClick: () -> Unit)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToApp: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToMonitoring: () -> Unit,
    onNavigateToOverlay: () -> Unit,
    onNavigateToRustLibrary: () -> Unit,
    onNavigateToAbout: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current

    val settingsItems = remember(context, onNavigateToApp, onNavigateToAppearance, onNavigateToMonitoring, onNavigateToOverlay, onNavigateToRustLibrary, onNavigateToAbout) {
        listOf(
            SettingsItem(
                title = context.getString(R.string.settings_item_app),
                subtitle = context.getString(R.string.settings_item_app_desc),
                iconRes = R.drawable.build_filled,
                onClick = onNavigateToApp,
            ),
            SettingsItem(
                title = context.getString(R.string.settings_item_appearance),
                subtitle = context.getString(R.string.settings_item_appearance_desc),
                iconRes = R.drawable.brightness_medium_filled,
                onClick = onNavigateToAppearance,
            ),
            SettingsItem(
                title = context.getString(R.string.settings_item_overlay),
                subtitle = context.getString(R.string.settings_item_overlay_desc),
                iconRes = R.drawable.layers_filled,
                onClick = onNavigateToOverlay,
            ),
            SettingsItem(
                title = context.getString(R.string.settings_item_monitoring),
                subtitle = context.getString(R.string.settings_item_monitoring_desc),
                iconRes = R.drawable.dvr_filled,
                onClick = onNavigateToMonitoring,
            ),
            SettingsItem(
                title = context.getString(R.string.settings_item_rust_library),
                subtitle = context.getString(R.string.settings_item_rust_library_desc),
                iconRes = R.drawable.ic_rust_logo,
                onClick = onNavigateToRustLibrary,
            ),
            SettingsItem(
                title = context.getString(R.string.settings_item_about),
                subtitle = context.getString(R.string.settings_item_about_desc),
                iconRes = R.drawable.info_filled,
                onClick = onNavigateToAbout,
            ),
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ExitUntilCollapsedMediumTopAppBar(
                title = stringResource(R.string.title_settings),
                onNavigateBack = onNavigateBack,
                scrollBehavior = scrollBehavior,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            itemsIndexed(settingsItems) { index, item ->
                val shape = when (index) {
                    0 -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                    settingsItems.lastIndex -> RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                    else -> RoundedCornerShape(8.dp)
                }

                SettingsMenuItem(
                    title = item.title,
                    subtitle = item.subtitle,
                    icon = painterResource(item.iconRes),
                    shape = shape,
                    onClick = item.onClick,
                )
            }
        }
    }
}

@Composable
private fun SettingsMenuItem(title: String, subtitle: String, icon: Painter, shape: Shape, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .hapticClickable(onClick = onClick)
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Icon(
                painter = painterResource(R.drawable.arrow_back_ios_new),
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer(rotationZ = 180f),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
