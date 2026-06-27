package com.edward.studytracker.ui.screens.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.edward.studytracker.R
import com.edward.studytracker.data.PreferencesManager
import com.edward.studytracker.data.ProjectRepository
import kotlinx.coroutines.launch
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    repository: ProjectRepository
) {
    var darkModeEnabled by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var showImportConfirm by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { PreferencesManager(context) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            isProcessing = true
            val result = repository.exportData()
            result.onSuccess { json ->
                val writeResult = writeTextToUri(context, uri, json)
                statusMessage = if (writeResult.isSuccess) {
                    context.getString(R.string.status_export_success)
                } else {
                    context.getString(
                        R.string.status_export_failed,
                        writeResult.exceptionOrNull()?.message ?: context.getString(R.string.error_cannot_write_file)
                    )
                }
            }.onFailure { error ->
                statusMessage = context.getString(
                    R.string.status_export_failed,
                    error.message ?: context.getString(R.string.error_unknown)
                )
            }
            isProcessing = false
            showStatusDialog = true
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        pendingImportUri = uri
        showImportConfirm = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            SettingsSection(title = stringResource(R.string.section_appearance)) {
                SettingsItem(
                    icon = Icons.Default.Settings,
                    title = stringResource(R.string.settings_dark_mode),
                    subtitle = stringResource(R.string.settings_follow_system),
                    onClick = { darkModeEnabled = !darkModeEnabled },
                    trailing = {
                        Switch(
                            checked = darkModeEnabled,
                            onCheckedChange = { darkModeEnabled = it }
                        )
                    }
                )

                Divider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                val currentLanguageLabel = when (prefs.language) {
                    "system" -> stringResource(R.string.settings_language_system)
                    "en" -> stringResource(R.string.settings_language_en)
                    "zh" -> stringResource(R.string.settings_language_zh)
                    else -> prefs.language
                }

                SettingsItem(
                    iconPainter = painterResource(R.drawable.ic_translate),
                    title = stringResource(R.string.settings_language),
                    subtitle = currentLanguageLabel,
                    onClick = { showLanguageDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsSection(title = stringResource(R.string.section_notifications)) {
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = stringResource(R.string.settings_study_reminder),
                    subtitle = stringResource(R.string.settings_daily_reminder),
                    onClick = { notificationsEnabled = !notificationsEnabled },
                    trailing = {
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                    }
                )

                Divider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = stringResource(R.string.settings_sound),
                    subtitle = stringResource(R.string.settings_sound_subtitle),
                    onClick = { soundEnabled = !soundEnabled },
                    trailing = {
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { soundEnabled = it }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsSection(title = stringResource(R.string.section_data)) {
                SettingsItem(
                    icon = Icons.Default.Create,
                    title = stringResource(R.string.settings_export_data),
                    subtitle = stringResource(R.string.settings_export_subtitle),
                    onClick = {
                        if (!isProcessing) {
                            exportLauncher.launch(buildExportFileName())
                        }
                    }
                )

                Divider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                SettingsItem(
                    icon = Icons.Default.Add,
                    title = stringResource(R.string.settings_import_data),
                    subtitle = stringResource(R.string.settings_import_subtitle),
                    onClick = {
                        if (!isProcessing) {
                            importLauncher.launch(arrayOf("application/json"))
                        }
                    }
                )

                if (isProcessing) {
                    Text(
                        text = stringResource(R.string.settings_processing),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsSection(title = stringResource(R.string.section_about)) {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.settings_version),
                    subtitle = stringResource(R.string.settings_version_value),
                    onClick = { }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.settings_reset_statistics),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
    }

    if (showImportConfirm) {
        AlertDialog(
            onDismissRequest = {
                showImportConfirm = false
                pendingImportUri = null
            },
            title = { Text(stringResource(R.string.dialog_import_title)) },
            text = { Text(stringResource(R.string.dialog_import_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val uri = pendingImportUri
                        showImportConfirm = false
                        pendingImportUri = null
                        if (uri != null) {
                            scope.launch {
                                isProcessing = true
                                val readResult = readTextFromUri(context, uri)
                                if (readResult.isFailure) {
                                    statusMessage = context.getString(
                                        R.string.status_import_failed,
                                        readResult.exceptionOrNull()?.message ?: context.getString(R.string.error_cannot_read_file)
                                    )
                                } else {
                                    val json = readResult.getOrNull().orEmpty()
                                    if (json.isBlank()) {
                                        statusMessage = context.getString(R.string.status_import_file_empty)
                                    } else {
                                        val importResult = repository.importData(json)
                                        if (importResult.isSuccess) {
                                            updateCurrentProjectAfterImport(context, repository)
                                            statusMessage = context.getString(R.string.status_import_success)
                                        } else {
                                            statusMessage = context.getString(
                                                R.string.status_import_failed,
                                                importResult.exceptionOrNull()?.message ?: context.getString(R.string.error_unknown)
                                            )
                                        }
                                    }
                                }
                                isProcessing = false
                                showStatusDialog = true
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.action_continue))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImportConfirm = false
                        pendingImportUri = null
                    }
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text(stringResource(R.string.dialog_notice_title)) },
            text = { Text(statusMessage) },
            confirmButton = {
                TextButton(onClick = { showStatusDialog = false }) {
                    Text(stringResource(R.string.action_confirm))
                }
            }
        )
    }

    if (showLanguageDialog) {
        val languages = listOf("system", "en", "zh")
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.settings_language)) },
            text = {
                Column {
                    languages.forEach { lang ->
                        val text = when (lang) {
                            "system" -> stringResource(R.string.settings_language_system)
                            "en" -> stringResource(R.string.settings_language_en)
                            "zh" -> stringResource(R.string.settings_language_zh)
                            else -> lang
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showLanguageDialog = false
                                    if (prefs.language != lang) {
                                        prefs.language = lang
                                        context.findActivity()?.recreate()
                                    }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.RadioButton(
                                selected = (prefs.language == lang),
                                onClick = {
                                    showLanguageDialog = false
                                    if (prefs.language != lang) {
                                        prefs.language = lang
                                        context.findActivity()?.recreate()
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = text, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

private fun buildExportFileName(): String {
    val now = System.currentTimeMillis()
    val formatted = DateFormat.format("yyyyMMdd_HHmm", now).toString()
    return "studytracker_backup_$formatted.json"
}

private fun writeTextToUri(context: Context, uri: Uri, content: String): Result<Unit> {
    return runCatching {
        context.contentResolver.openOutputStream(uri)?.use { output ->
            output.write(content.toByteArray(Charsets.UTF_8))
            output.flush()
        } ?: throw IOException(context.getString(R.string.error_open_output_stream))
    }
}

private fun readTextFromUri(context: Context, uri: Uri): Result<String> {
    return runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            input.bufferedReader(Charsets.UTF_8).readText()
        } ?: throw IOException(context.getString(R.string.error_open_input_stream))
    }
}

private suspend fun updateCurrentProjectAfterImport(
    context: Context,
    repository: ProjectRepository
) {
    val prefs = PreferencesManager(context)
    val projects = repository.getAllProjectsSync()
    prefs.currentProjectId = projects.firstOrNull()?.id ?: -1
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    iconPainter: androidx.compose.ui.graphics.painter.Painter? = null,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.size(16.dp))
        } else if (iconPainter != null) {
            Icon(
                painter = iconPainter,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.size(16.dp))
        } else {
            Spacer(modifier = Modifier.size(40.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        trailing?.invoke()
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
