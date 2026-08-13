package com.rve.systemmonitor.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.rve.systemmonitor.BuildConfig
import com.rve.systemmonitor.R
import com.rve.systemmonitor.ui.components.haptic.rememberHapticOnClick
import com.rve.systemmonitor.ui.viewmodel.SetupViewModel
import com.rve.systemmonitor.utils.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class SetupStep {
    OverlayPermission,
    Updates,
    Theme,
    BackupRestore,
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SetupScreen(viewModel: SetupViewModel = hiltViewModel(), onSetupCompleted: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val autoUpdateEnabled by viewModel.autoUpdateEnabled.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val setupSteps = remember {
        buildList {
            add(SetupStep.OverlayPermission)
            if (BuildConfig.ENABLE_UPDATER) {
                add(SetupStep.Updates)
            }
            add(SetupStep.Theme)
            add(SetupStep.BackupRestore)
        }
    }
    var setupStep by remember { mutableStateOf(setupSteps.first()) }
    var isOverlayPermissionGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var hasImportedBackup by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val importSuccessMessage = stringResource(R.string.backup_import_success)
    val importErrorMessage = stringResource(R.string.backup_import_failed)

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            viewModel.importSettingsFromFile(context, uri) { success ->
                if (success) {
                    hasImportedBackup = true
                }
                scope.launch {
                    snackbarHostState.showSnackbar(if (success) importSuccessMessage else importErrorMessage)
                }
            }
        }
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.cat))
    var isPlaying by remember { mutableStateOf(false) }
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
        iterations = LottieConstants.IterateForever,
    )

    val slowSpatialSpec = MaterialTheme.motionScheme.slowSpatialSpec<IntOffset>()
    val slowEffectsSpec = MaterialTheme.motionScheme.slowEffectsSpec<Float>()

    LaunchedEffect(Unit) {
        delay(400)
        isPlaying = true
    }

    BackHandler(enabled = setupStep != setupSteps.first()) {
        val currentIndex = setupSteps.indexOf(setupStep)
        if (currentIndex > 0) {
            setupStep = setupSteps[currentIndex - 1]
        }
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isOverlayPermissionGranted = Settings.canDrawOverlays(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(280.dp),
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 16.dp, bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    StepIndicator(
                        currentStep = setupSteps.indexOf(setupStep),
                        totalSteps = setupSteps.size,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    AnimatedContent(
                        targetState = setupStep,
                        transitionSpec = {
                            val initialIndex = setupSteps.indexOf(initialState)
                            val targetIndex = setupSteps.indexOf(targetState)
                            if (targetIndex > initialIndex) {
                                (slideInHorizontally(animationSpec = slowSpatialSpec) { it } + fadeIn(animationSpec = slowEffectsSpec))
                                    .togetherWith(
                                        slideOutHorizontally(animationSpec = slowSpatialSpec) {
                                            -it
                                        } + fadeOut(animationSpec = slowEffectsSpec),
                                    )
                            } else {
                                (slideInHorizontally(animationSpec = slowSpatialSpec) { -it } + fadeIn(animationSpec = slowEffectsSpec))
                                    .togetherWith(
                                        slideOutHorizontally(animationSpec = slowSpatialSpec) {
                                            it
                                        } + fadeOut(animationSpec = slowEffectsSpec),
                                    )
                            }
                        },
                        label = "Setup Step Content",
                        modifier = Modifier.fillMaxWidth(),
                    ) { step ->
                        when (step) {
                            SetupStep.OverlayPermission -> OverlayPermissionContent(
                                isOverlayPermissionGranted = isOverlayPermissionGranted,
                            )

                            SetupStep.Updates -> UpdatesContent(
                                autoUpdateEnabled = autoUpdateEnabled,
                                onAutoUpdateChanged = viewModel::setAutoUpdateEnabled,
                            )

                            SetupStep.Theme -> ThemeContent(
                                selectedTheme = themeMode,
                                onThemeSelected = viewModel::setThemeMode,
                            )

                            SetupStep.BackupRestore -> BackupRestoreContent(
                                onRestoreClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    val buttonText = when (setupStep) {
                        SetupStep.OverlayPermission -> if (isOverlayPermissionGranted) {
                            stringResource(R.string.setup_continue)
                        } else {
                            stringResource(R.string.setup_grant_permission)
                        }

                        SetupStep.Updates -> stringResource(R.string.setup_continue)

                        SetupStep.Theme -> stringResource(R.string.setup_continue)

                        SetupStep.BackupRestore -> if (hasImportedBackup) {
                            stringResource(R.string.setup_finish)
                        } else {
                            stringResource(R.string.setup_skip)
                        }
                    }

                    val onButtonClick = when (setupStep) {
                        SetupStep.OverlayPermission -> {
                            {
                                if (isOverlayPermissionGranted) {
                                    setupStep = if (BuildConfig.ENABLE_UPDATER) SetupStep.Updates else SetupStep.Theme
                                } else {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        "package:${context.packageName}".toUri(),
                                    )
                                    context.startActivity(intent)
                                }
                            }
                        }

                        SetupStep.Updates -> {
                            {
                                setupStep = SetupStep.Theme
                            }
                        }

                        SetupStep.Theme -> {
                            {
                                setupStep = SetupStep.BackupRestore
                            }
                        }

                        SetupStep.BackupRestore -> {
                            {
                                isPlaying = false
                                viewModel.completeSetup()
                                onSetupCompleted()
                            }
                        }
                    }

                    Button(
                        onClick = rememberHapticOnClick(onButtonClick),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text(
                            text = buttonText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    if (setupStep == SetupStep.OverlayPermission && !isOverlayPermissionGranted) {
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.material3.TextButton(
                            onClick = rememberHapticOnClick {
                                setupStep = if (BuildConfig.ENABLE_UPDATER) SetupStep.Updates else SetupStep.Theme
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.setup_skip),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    val footerText = when (setupStep) {
                        SetupStep.OverlayPermission -> if (isOverlayPermissionGranted) {
                            stringResource(R.string.setup_permission_granted_footer)
                        } else {
                            stringResource(R.string.setup_permission_required_footer)
                        }

                        SetupStep.Updates -> stringResource(R.string.setup_updates_footer)

                        SetupStep.Theme -> stringResource(R.string.setup_theme_footer)

                        SetupStep.BackupRestore -> stringResource(R.string.setup_backup_footer)
                    }

                    Text(
                        text = footerText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalSteps) { index ->
            val isSelected = index == currentStep
            val width by animateDpAsState(
                targetValue = if (isSelected) 32.dp else 8.dp,
                animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                label = "Step Indicator Width",
            )
            val color by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                },
                animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                label = "Step Indicator Color",
            )

            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}

@Composable
private fun OverlayPermissionContent(isOverlayPermissionGranted: Boolean) {
    SetupStepContent(
        title = stringResource(R.string.setup_overlay_title),
        description = stringResource(R.string.setup_overlay_description),
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun UpdatesContent(autoUpdateEnabled: Boolean, onAutoUpdateChanged: (Boolean) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }

    SetupStepContent(
        title = stringResource(R.string.setup_updates_title),
        description = stringResource(R.string.setup_updates_description),
        action = {
            Card(
                onClick = rememberHapticOnClick { onAutoUpdateChanged(!autoUpdateEnabled) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                interactionSource = interactionSource,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
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
                                .background(MaterialTheme.colorScheme.tertiaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.update_rounded),
                                contentDescription = stringResource(R.string.cd_update_icon),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                        }

                        Column {
                            Text(
                                text = stringResource(R.string.setup_auto_update),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = stringResource(R.string.setup_check_on_startup),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Switch(
                        checked = autoUpdateEnabled,
                        onCheckedChange = onAutoUpdateChanged,
                        colors = SwitchDefaults.colors(
                            checkedIconColor = MaterialTheme.colorScheme.primary,
                        ),
                        interactionSource = interactionSource,
                        thumbContent = {
                            Crossfade(
                                targetState = autoUpdateEnabled,
                                animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
                                label = "Setup Auto Update Switch Icon",
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
        },
    )
}

@Composable
private fun SetupStepContent(title: String, description: String, action: (@Composable ColumnScope.() -> Unit)? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        if (action != null) {
            Spacer(modifier = Modifier.height(32.dp))
            action()
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThemeContent(selectedTheme: ThemeMode, onThemeSelected: (ThemeMode) -> Unit) {
    SetupStepContent(
        title = stringResource(R.string.setup_theme_title),
        description = stringResource(R.string.setup_theme_description),
        action = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ThemeMode.entries.forEach { theme ->
                    val isSelected = selectedTheme == theme
                    Card(
                        onClick = rememberHapticOnClick { onThemeSelected(theme) },
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = if (isSelected) {
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            CardDefaults.cardColors()
                        },
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                painter = painterResource(
                                    when (theme) {
                                        ThemeMode.LIGHT -> R.drawable.light_mode
                                        ThemeMode.DARK -> R.drawable.dark_mode
                                        ThemeMode.SYSTEM -> R.drawable.brightness_medium_filled
                                    },
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = when (theme) {
                                    ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                                    ThemeMode.DARK -> stringResource(R.string.theme_dark)
                                    ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun BackupRestoreContent(onRestoreClick: () -> Unit) {
    SetupStepContent(
        title = stringResource(R.string.setup_backup_title),
        description = stringResource(R.string.setup_backup_description),
        action = {
            Card(
                onClick = rememberHapticOnClick(onRestoreClick),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
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
                    Text(
                        text = stringResource(R.string.setup_restore),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
    )
}
