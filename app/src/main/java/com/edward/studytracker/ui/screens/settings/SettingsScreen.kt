package com.edward.studytracker.ui.screens.settings

import android.content.Context
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
                    "导出成功"
                } else {
                    "导出失败：${writeResult.exceptionOrNull()?.message ?: "无法写入文件"}"
                }
            }.onFailure { error ->
                statusMessage = "导出失败：${error.message ?: "未知错误"}"
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
                        text = "设置",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
            SettingsSection(title = "外观") {
                SettingsItem(
                    icon = Icons.Default.Settings,
                    title = "深色模式",
                    subtitle = "跟随系统",
                    onClick = { darkModeEnabled = !darkModeEnabled },
                    trailing = {
                        Switch(
                            checked = darkModeEnabled,
                            onCheckedChange = { darkModeEnabled = it }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsSection(title = "通知") {
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "学习提醒",
                    subtitle = "每日学习提醒",
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
                    title = "提示音",
                    subtitle = "答题时播放声音",
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

            SettingsSection(title = "数据") {
                SettingsItem(
                    icon = Icons.Default.Create,
                    title = "导出数据",
                    subtitle = "保存为 JSON 文件",
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
                    title = "导入数据",
                    subtitle = "覆盖本地数据",
                    onClick = {
                        if (!isProcessing) {
                            importLauncher.launch(arrayOf("application/json"))
                        }
                    }
                )

                if (isProcessing) {
                    Text(
                        text = "处理中...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsSection(title = "关于") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "版本",
                    subtitle = "Study Tracker 1.0.0",
                    onClick = { }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "重置统计",
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
            title = { Text("导入数据") },
            text = { Text("导入将覆盖本地数据，是否继续？") },
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
                                    statusMessage = "导入失败：${readResult.exceptionOrNull()?.message ?: "无法读取文件"}"
                                } else {
                                    val json = readResult.getOrNull().orEmpty()
                                    if (json.isBlank()) {
                                        statusMessage = "导入失败：文件为空"
                                    } else {
                                        val importResult = repository.importData(json)
                                        if (importResult.isSuccess) {
                                            updateCurrentProjectAfterImport(context, repository)
                                            statusMessage = "导入成功"
                                        } else {
                                            statusMessage = "导入失败：${importResult.exceptionOrNull()?.message ?: "未知错误"}"
                                        }
                                    }
                                }
                                isProcessing = false
                                showStatusDialog = true
                            }
                        }
                    }
                ) {
                    Text("继续")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImportConfirm = false
                        pendingImportUri = null
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }

    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("提示") },
            text = { Text(statusMessage) },
            confirmButton = {
                TextButton(onClick = { showStatusDialog = false }) {
                    Text("确定")
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
        } ?: throw IOException("无法打开输出流")
    }
}

private fun readTextFromUri(context: Context, uri: Uri): Result<String> {
    return runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            input.bufferedReader(Charsets.UTF_8).readText()
        } ?: throw IOException("无法打开输入流")
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
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
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
