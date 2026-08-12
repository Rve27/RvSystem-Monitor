package com.rve.systemmonitor.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rve.systemmonitor.BuildConfig
import com.rve.systemmonitor.R
import com.rve.systemmonitor.ui.components.ExitUntilCollapsedMediumTopAppBar
import com.rve.systemmonitor.ui.components.haptic.hapticClickable
import com.rve.systemmonitor.ui.viewmodel.SettingsViewModel
import com.rve.systemmonitor.utils.AppLanguage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@Composable
fun AppSettingsScreen(viewModel: SettingsViewModel = hiltViewModel(), onNavigateBack: () -> Unit, onNavigateToSetup: () -> Unit) {
    val context = LocalContext.current
    val autoUpdateEnabled by viewModel.autoUpdateEnabled.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val useShizuku by viewModel.useShizuku.collectAsStateWithLifecycle()
    val isShizukuAvailable by viewModel.isShizukuAvailable.collectAsStateWithLifecycle()
    val hasShizukuPermission by viewModel.hasShizukuPermission.collectAsStateWithLifecycle()

    AppSettingsScreenContent(
        autoUpdateEnabled = autoUpdateEnabled,
        language = language,
        useShizuku = useShizuku,
        isShizukuAvailable = isShizukuAvailable,
        hasShizukuPermission = hasShizukuPermission,
        onAutoUpdateChange = { viewModel.setAutoUpdateEnabled(it) },
        onLanguageChange = { viewModel.setLanguage(it) },
        onUseShizukuChange = { viewModel.setUseShizuku(it) },
        onRefreshShizukuStatus = { viewModel.refreshShizukuState() },
        onRequestShizukuPermission = { viewModel.requestShizukuPermission() },
        onExportSettings = { uri, callback -> viewModel.exportSettingsToFile(context, uri, callback) },
        onImportSettings = { uri, callback -> viewModel.importSettingsFromFile(context, uri, callback) },
        onNavigateBack = onNavigateBack,
        onNavigateToSetup = onNavigateToSetup,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AppSettingsScreenContent(
    autoUpdateEnabled: Boolean,
    language: AppLanguage,
    useShizuku: Boolean,
    isShizukuAvailable: Boolean,
    hasShizukuPermission: Boolean,
    onAutoUpdateChange: (Boolean) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onUseShizukuChange: (Boolean) -> Unit,
    onRefreshShizukuStatus: () -> Unit,
    onRequestShizukuPermission: () -> Unit,
    onExportSettings: (Uri, (Boolean) -> Unit) -> Unit,
    onImportSettings: (Uri, (Boolean) -> Unit) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToSetup: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onRefreshShizukuStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val exportSuccessMessage = stringResource(R.string.backup_export_success)
    val exportErrorMessage = stringResource(R.string.backup_export_failed)
    val importSuccessMessage = stringResource(R.string.backup_import_success)
    val importErrorMessage = stringResource(R.string.backup_import_failed)

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            onExportSettings(uri) { success ->
                scope.launch {
                    snackbarHostState.showSnackbar(if (success) exportSuccessMessage else exportErrorMessage)
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            onImportSettings(uri) { success ->
                scope.launch {
                    snackbarHostState.showSnackbar(if (success) importSuccessMessage else importErrorMessage)
                }
            }
        }
    }

    val currentDate = remember { LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) }
    val backupFileName = "RvSystem-Monitor-$currentDate-backup-settings.json"

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ExitUntilCollapsedMediumTopAppBar(
                title = stringResource(R.string.title_app),
                onNavigateBack = onNavigateBack,
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item {
                LanguageSetting(
                    language = language,
                    onLanguageChange = onLanguageChange,
                )
            }

            if (BuildConfig.ENABLE_UPDATER) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = stringResource(R.string.label_updates),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp, start = 8.dp),
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .hapticClickable { onAutoUpdateChange(!autoUpdateEnabled) }
                                    .padding(horizontal = 20.dp, vertical = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.update_rounded),
                                            contentDescription = stringResource(R.string.cd_update_icon),
                                            tint = MaterialTheme.colorScheme.onSecondary,
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = stringResource(R.string.settings_check_for_updates),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            text = stringResource(R.string.settings_check_updates_description),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }

                                Switch(
                                    checked = autoUpdateEnabled,
                                    onCheckedChange = onAutoUpdateChange,
                                    colors = SwitchDefaults.colors(
                                        checkedIconColor = MaterialTheme.colorScheme.primary,
                                    ),
                                    thumbContent = {
                                        Crossfade(
                                            targetState = autoUpdateEnabled,
                                            animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
                                            label = "Auto Update Switch Icon",
                                        ) { enabled ->
                                            Icon(
                                                painter = painterResource(
                                                    if (enabled) R.drawable.check_rounded else R.drawable.close_rounded,
                                                ),
                                                contentDescription = null,
                                                modifier = Modifier.size(SwitchDefaults.IconSize),
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.label_shizuku),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp, start = 8.dp),
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .hapticClickable { onUseShizukuChange(!useShizuku) }
                                    .padding(horizontal = 20.dp, vertical = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_shizuku),
                                            contentDescription = stringResource(R.string.cd_shizuku_icon),
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(64.dp),
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = stringResource(R.string.settings_use_shizuku),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            text = stringResource(R.string.settings_use_shizuku_description),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }

                                Switch(
                                    checked = useShizuku,
                                    onCheckedChange = onUseShizukuChange,
                                    colors = SwitchDefaults.colors(
                                        checkedIconColor = MaterialTheme.colorScheme.primary,
                                    ),
                                    thumbContent = {
                                        Crossfade(
                                            targetState = useShizuku,
                                            animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
                                            label = "Shizuku Switch Icon",
                                        ) { enabled ->
                                            Icon(
                                                painter = painterResource(
                                                    if (enabled) R.drawable.check_rounded else R.drawable.close_rounded,
                                                ),
                                                contentDescription = null,
                                                modifier = Modifier.size(SwitchDefaults.IconSize),
                                            )
                                        }
                                    },
                                )
                            }

                            AnimatedVisibility(
                                visible = useShizuku,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut(),
                            ) {
                                Column {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 20.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp, vertical = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            val statusText = when {
                                                isShizukuAvailable && hasShizukuPermission ->
                                                    stringResource(R.string.shizuku_status_running_authorized)

                                                isShizukuAvailable ->
                                                    stringResource(R.string.shizuku_status_permission_required)

                                                else -> stringResource(R.string.shizuku_status_not_running)
                                            }
                                            val statusColor = when {
                                                isShizukuAvailable && hasShizukuPermission ->
                                                    Color(0xFF22C55E)

                                                isShizukuAvailable -> MaterialTheme.colorScheme.error

                                                else -> MaterialTheme.colorScheme.outline
                                            }

                                            AnimatedContent(
                                                targetState = statusText to statusColor,
                                                transitionSpec = {
                                                    fadeIn() togetherWith fadeOut()
                                                },
                                                label = "ShizukuStatusAnimation",
                                            ) { (text, color) ->
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .clip(CircleShape)
                                                            .background(color),
                                                    )
                                                    Text(
                                                        text = text,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = color,
                                                        fontWeight = FontWeight.Bold,
                                                    )
                                                }
                                            }
                                        }

                                        if (isShizukuAvailable && !hasShizukuPermission) {
                                            Button(
                                                onClick = onRequestShizukuPermission,
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                modifier = Modifier.height(32.dp),
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.button_grant_permission),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.label_backup),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp, start = 8.dp),
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .hapticClickable { exportLauncher.launch(backupFileName) }
                                    .padding(horizontal = 20.dp, vertical = 20.dp),
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
                                        painter = painterResource(R.drawable.backup_filled),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                    )
                                }

                                Column {
                                    Text(
                                        text = stringResource(R.string.settings_export_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = stringResource(R.string.settings_export_description),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .hapticClickable { importLauncher.launch(arrayOf("application/json", "*/*")) }
                                    .padding(horizontal = 20.dp, vertical = 20.dp),
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
                                        painter = painterResource(R.drawable.restore_page_filled),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                    )
                                }

                                Column {
                                    Text(
                                        text = stringResource(R.string.settings_import_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = stringResource(R.string.settings_import_description),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.label_testing),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp, start = 8.dp),
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .hapticClickable { onNavigateToSetup() }
                                .padding(horizontal = 20.dp, vertical = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.home_storage_gear_filled),
                                        contentDescription = stringResource(R.string.cd_setup_icon),
                                        tint = MaterialTheme.colorScheme.onSecondary,
                                    )
                                }

                                Column {
                                    Text(
                                        text = stringResource(R.string.settings_test_setup_flow),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = stringResource(R.string.settings_test_setup_description),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            Icon(
                                painter = painterResource(R.drawable.arrow_forward_ios_new),
                                contentDescription = stringResource(R.string.cd_open_setup_flow),
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageSetting(language: AppLanguage, onLanguageChange: (AppLanguage) -> Unit) {
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    val languageOptions = listOf(
        AppLanguage.SYSTEM to R.string.language_system,
        AppLanguage.ENGLISH to R.string.language_english,
        AppLanguage.RUSSIAN to R.string.language_russian,
        AppLanguage.UKRAINIAN to R.string.language_ukrainian,
        AppLanguage.CHINESE_SIMPLIFIED to R.string.language_chinese_simplified,
        AppLanguage.INDONESIAN to R.string.language_indonesian,
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.label_language),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp, start = 8.dp),
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .hapticClickable { showLanguageDialog = true }
                    .padding(horizontal = 20.dp, vertical = 20.dp),
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
                        painter = painterResource(R.drawable.translate),
                        contentDescription = stringResource(R.string.settings_language),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.settings_language_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Text(text = stringResource(R.string.settings_language))
            },
            text = {
                Column {
                    languageOptions.forEach { (option, labelRes) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .hapticClickable {
                                    showLanguageDialog = false
                                    onLanguageChange(option)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = language == option,
                                onClick = {
                                    showLanguageDialog = false
                                    onLanguageChange(option)
                                },
                            )
                            Text(
                                text = stringResource(labelRes),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(text = stringResource(R.string.button_close))
                }
            },
        )
    }
}

private fun languageLabelRes(language: AppLanguage): Int = when (language) {
    AppLanguage.SYSTEM -> R.string.language_system
    AppLanguage.ENGLISH -> R.string.language_english
    AppLanguage.RUSSIAN -> R.string.language_russian
    AppLanguage.UKRAINIAN -> R.string.language_ukrainian
    AppLanguage.CHINESE_SIMPLIFIED -> R.string.language_chinese_simplified
    AppLanguage.INDONESIAN -> R.string.language_indonesian
}
