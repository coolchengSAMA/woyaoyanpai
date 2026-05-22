package com.yanpai.clipboardcleaner.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yanpai.clipboardcleaner.viewmodel.NotebookViewModel
import com.yanpai.clipboardcleaner.viewmodel.ThemeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(
    onBack: () -> Unit,
    themeViewModel: ThemeViewModel?,
    notebookViewModel: NotebookViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }
    var cacheStats by remember { mutableStateOf<Pair<Int, Long>?>(null) }
    val notebookUiState by notebookViewModel.uiState.collectAsState()

    LaunchedEffect(notebookUiState.toastMessage) {
        notebookUiState.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            notebookViewModel.clearToast()
        }
    }

    // 刷新缓存统计
    LaunchedEffect(Unit) {
        cacheStats = themeViewModel?.getCacheStats()
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { notebookViewModel.exportToJson(it) }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { notebookViewModel.importFromJson(it) }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清理缓存") },
            text = { Text("确定要删除 ${cacheStats?.first ?: 0} 个未使用的缓存文件吗？") },
            confirmButton = {
                TextButton(onClick = {
                    themeViewModel?.clearCache()
                    cacheStats = themeViewModel?.getCacheStats()
                    showClearDialog = false
                    scope.launch { snackbarHostState.showSnackbar("缓存已清理") }
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("取消") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据管理") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(data, containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface)
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(top = 12.dp)
        ) {
            // 笔记本数据
            Card(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("笔记本数据", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("导出备份为 JSON 文件，重装 App 后可导入恢复。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(Modifier.height(12.dp))
                    Row {
                        Button(onClick = { exportLauncher.launch("woyaoyanpai_backup.json") }) {
                            Text("导出备份")
                        }
                        Spacer(Modifier.height(0.dp))
                        OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json")) },
                            modifier = Modifier.padding(start = 12.dp)) {
                            Text("导入恢复")
                        }
                    }
                }
            }

            // 缓存管理
            Card(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("缓存管理", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    val (count, bytes) = cacheStats ?: Pair(0, 0L)
                    if (count > 0) {
                        val sizeStr = when {
                            bytes < 1024 -> "${bytes} B"
                            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
                            else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
                        }
                        Text("当前缓存：$sizeStr（$count 个文件）",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { showClearDialog = true }) { Text("清理缓存") }
                    } else {
                        Text("暂无缓存文件", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}
