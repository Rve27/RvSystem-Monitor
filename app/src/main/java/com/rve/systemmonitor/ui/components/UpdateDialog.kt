package com.rve.systemmonitor.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.rve.systemmonitor.R
import com.rve.systemmonitor.domain.model.GitHubRelease
import com.rve.systemmonitor.ui.components.haptic.rememberHapticOnClick
import com.rve.systemmonitor.ui.viewmodel.UpdateUiState
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UpdateDialog(
    uiState: UpdateUiState,
    onDownload: (GitHubRelease) -> Unit,
    onCancelDownload: () -> Unit,
    onDismiss: () -> Unit,
    onPauseUpdates: (Int) -> Unit = {},
) {
    val context = LocalContext.current

    when (uiState) {
        is UpdateUiState.UpdateAvailable -> {
            var showPauseDialog by remember { mutableStateOf(false) }

            UpdateDialogSurface(
                iconRes = R.drawable.download_2_filled,
                title = stringResource(R.string.update_available_title),
                subtitle = uiState.release.name.ifBlank { uiState.release.tagName },
                onDismiss = onDismiss,
                secondaryAction = {
                    OutlinedButton(
                        onClick = rememberHapticOnClick { showPauseDialog = true },
                        modifier = Modifier.weight(1f),
                        shapes = ButtonDefaults.shapes(),
                    ) {
                        Text(stringResource(R.string.button_pause))
                    }
                    OutlinedButton(
                        onClick = rememberHapticOnClick(onDismiss),
                        modifier = Modifier.weight(1f),
                        shapes = ButtonDefaults.shapes(),
                    ) {
                        Text(stringResource(R.string.button_later))
                    }
                },
                primaryAction = {
                    Button(
                        onClick = rememberHapticOnClick { onDownload(uiState.release) },
                        modifier = Modifier.weight(1f),
                        shapes = ButtonDefaults.shapes(),
                    ) {
                        Text(stringResource(R.string.button_download))
                    }
                },
            ) {
                ReleaseSummary(release = uiState.release)
            }

            if (showPauseDialog) {
                PauseUpdatesDialog(
                    onDismiss = { showPauseDialog = false },
                    onConfirm = { hours ->
                        showPauseDialog = false
                        onPauseUpdates(hours)
                    },
                )
            }
        }

        is UpdateUiState.Downloading -> {
            val progress = uiState.progress.coerceIn(0f, 1f)
            var showCancelConfirmation by remember { mutableStateOf(false) }

            UpdateDialogSurface(
                iconRes = R.drawable.update_rounded,
                title = stringResource(R.string.downloading_update_title),
                subtitle = stringResource(R.string.downloading_update_subtitle),
                canDismiss = false,
                onBlockedDismiss = { showCancelConfirmation = true },
                primaryAction = {
                    OutlinedButton(
                        onClick = rememberHapticOnClick { showCancelConfirmation = true },
                        modifier = Modifier.fillMaxWidth(),
                        shapes = ButtonDefaults.shapes(),
                    ) {
                        Text(stringResource(R.string.button_cancel))
                    }
                },
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LinearWavyProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.download_progress_label),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            if (showCancelConfirmation) {
                CancelDownloadConfirmationDialog(
                    onDismiss = { showCancelConfirmation = false },
                    onConfirm = {
                        showCancelConfirmation = false
                        onCancelDownload()
                    },
                )
            }
        }

        is UpdateUiState.ReadyToInstall -> {
            UpdateDialogSurface(
                iconRes = R.drawable.apk_install_filled,
                title = stringResource(R.string.update_ready_title),
                subtitle = stringResource(R.string.update_ready_subtitle),
                onDismiss = onDismiss,
                secondaryAction = {
                    OutlinedButton(
                        onClick = rememberHapticOnClick(onDismiss),
                        modifier = Modifier.weight(1f),
                        shapes = ButtonDefaults.shapes(),
                    ) {
                        Text(stringResource(R.string.button_later))
                    }
                },
                primaryAction = {
                    Button(
                        onClick = rememberHapticOnClick {
                            installApk(context, uiState.file)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shapes = ButtonDefaults.shapes(),
                    ) {
                        Text(stringResource(R.string.button_install))
                    }
                },
            ) {
                StatusMessage(
                    title = uiState.file.name,
                    description = stringResource(R.string.install_description),
                )
            }
        }

        is UpdateUiState.Error -> {
            UpdateDialogSurface(
                iconRes = R.drawable.close_rounded,
                title = stringResource(R.string.update_failed_title),
                subtitle = stringResource(R.string.update_failed_subtitle),
                isError = true,
                onDismiss = onDismiss,
                primaryAction = {
                    Button(
                        onClick = rememberHapticOnClick(onDismiss),
                        modifier = Modifier.fillMaxWidth(),
                        shapes = ButtonDefaults.shapes(),
                    ) {
                        Text(stringResource(R.string.button_close))
                    }
                },
            ) {
                StatusMessage(
                    title = stringResource(R.string.error_details_title),
                    description = uiState.message,
                    isError = true,
                )
            }
        }

        else -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpdateDialogSurface(
    iconRes: Int,
    title: String,
    subtitle: String,
    canDismiss: Boolean = true,
    isError: Boolean = false,
    onDismiss: () -> Unit = {},
    onBlockedDismiss: () -> Unit = {},
    secondaryAction: (@Composable RowScope.() -> Unit)? = null,
    primaryAction: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        confirmValueChange = { sheetValue ->
            if (sheetValue == SheetValue.Hidden && !canDismiss) {
                onBlockedDismiss()
                false
            } else {
                true
            }
        },
    )

    ModalBottomSheet(
        onDismissRequest = {
            if (canDismiss) {
                onDismiss()
            } else {
                onBlockedDismiss()
            }
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(30.dp),
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            content()

            if (primaryAction != null || secondaryAction != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    secondaryAction?.invoke(this)
                    primaryAction?.invoke(this)
                }
            }
        }
    }
}

@Composable
private fun CancelDownloadConfirmationDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.cancel_download_dialog_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.cancel_download_dialog_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = rememberHapticOnClick(onDismiss),
                        modifier = Modifier.weight(1f),
                        shapes = ButtonDefaults.shapes(),
                    ) {
                        Text(stringResource(R.string.button_keep))
                    }
                    Button(
                        onClick = rememberHapticOnClick(onConfirm),
                        modifier = Modifier.weight(1f),
                        shapes = ButtonDefaults.shapes(),
                    ) {
                        Text(stringResource(R.string.button_cancel))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReleaseSummary(release: GitHubRelease) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VersionPill(text = release.tagName)
            selectedApkName(release)?.let { VersionPill(text = it) }
        }

        val releaseNotes = release.body.trim()
        if (releaseNotes.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.whats_new_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = releaseNotes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .heightIn(max = 180.dp)
                        .verticalScroll(rememberScrollState()),
                )
            }
        } else {
            StatusMessage(
                title = stringResource(R.string.whats_new_title),
                description = stringResource(R.string.no_changelog_message),
            )
        }
    }
}

@Composable
private fun VersionPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.tertiary)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onTertiary,
        )
    }
}

@Composable
private fun StatusMessage(title: String, description: String, isError: Boolean = false) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isError) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.surfaceContainer,
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun selectedApkName(release: GitHubRelease): String? {
    return release.assets.find { it.name == "app-github-release.apk" }?.name
}

fun installApk(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PauseUpdatesDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    val options = listOf(
        3 to stringResource(R.string.pause_3_hours),
        6 to stringResource(R.string.pause_6_hours),
        12 to stringResource(R.string.pause_12_hours),
        24 to stringResource(R.string.pause_24_hours),
    )

    var selectedOption by remember { mutableIntStateOf(options.first().first) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.pause_for_dialog_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Column(verticalArrangement = Arrangement.spacedBy((-6).dp)) {
                    options.forEachIndexed { index, (hours, label) ->
                        val shape: Shape = when (index) {
                            0 -> (ButtonGroupDefaults.connectedMiddleButtonShapes().shape as RoundedCornerShape)
                                .copy(topStart = CornerSize(100), topEnd = CornerSize(100))

                            options.lastIndex -> (ButtonGroupDefaults.connectedMiddleButtonShapes().shape as RoundedCornerShape)
                                .copy(bottomStart = CornerSize(100), bottomEnd = CornerSize(100))

                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes().shape
                        }

                        val hapticAction = rememberHapticOnClick { selectedOption = hours }

                        ToggleButton(
                            checked = selectedOption == hours,
                            onCheckedChange = { if (it) hapticAction() },
                            shapes = ToggleButtonShapes(
                                shape = shape,
                                pressedShape = shape,
                                checkedShape = ButtonGroupDefaults.connectedButtonCheckedShape,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { role = Role.RadioButton },
                        ) {
                            Text(label)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = rememberHapticOnClick(onDismiss),
                        shapes = ButtonDefaults.shapes(),
                    ) {
                        Text(stringResource(R.string.button_cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = rememberHapticOnClick { onConfirm(selectedOption) },
                        shapes = ButtonDefaults.shapes(),
                    ) {
                        Text(stringResource(R.string.button_pause))
                    }
                }
            }
        }
    }
}
