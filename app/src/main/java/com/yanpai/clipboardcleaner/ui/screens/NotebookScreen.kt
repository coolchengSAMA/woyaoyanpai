package com.yanpai.clipboardcleaner.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yanpai.clipboardcleaner.data.NoteEntry
import com.yanpai.clipboardcleaner.ui.components.EmptyState
import com.yanpai.clipboardcleaner.ui.components.NoteCard
import com.yanpai.clipboardcleaner.viewmodel.NotebookViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotebookScreen(
    viewModel: NotebookViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }
    var pendingDeleteEntry by remember { mutableStateOf<NoteEntry?>(null) }

    val pagerState = rememberPagerState(pageCount = { 2 })

    // 同步页面位置到 ViewModel
    val targetTab = if (pagerState.currentPage == 0) "comic" else "video"
    if (targetTab != uiState.selectedTab) {
        viewModel.selectTab(targetTab)
    }

    // 单条删除二次确认
    pendingDeleteEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { pendingDeleteEntry = null },
            title = { Text("删除确认") },
            text = { Text("确定要删除「${entry.cleanedText}」这条记录吗？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteEntry(entry)
                    pendingDeleteEntry = null
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteEntry = null }) {
                    Text("取消")
                }
            }
        )
    }

    // 清空确认弹窗
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清空确认") },
            text = { Text("确定要清空此分类的所有记录吗？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearCategory(uiState.selectedTab)
                    showClearDialog = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("笔记本") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = "清空")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text("漫画(${uiState.comicCount})") }
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text("视频(${uiState.videoCount})") }
                )
            }

            HorizontalPager(
                state = pagerState,
                beyondBoundsPageCount = 0,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val entries = if (page == 0) uiState.comicEntries else uiState.videoEntries
                if (entries.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        EmptyState()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)
                    ) {
                        items(
                            items = entries,
                            key = { it.id }
                        ) { entry ->
                            NoteCard(
                                entry = entry,
                                onToggleRead = { viewModel.toggleRead(entry) },
                                onCopy = {
                                    viewModel.copyToClipboard(entry.cleanedText)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("已复制到剪贴板")
                                    }
                                },
                                onDelete = { pendingDeleteEntry = entry }
                            )
                        }
                    }
                }
            }
        }
    }
}
