package com.yanpai.clipboardcleaner.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yanpai.clipboardcleaner.data.NoteEntry
import com.yanpai.clipboardcleaner.ui.components.EmptyState
import com.yanpai.clipboardcleaner.ui.components.NoteCard
import com.yanpai.clipboardcleaner.viewmodel.NotebookViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotebookScreen(viewModel: NotebookViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }
    var showClearTrashDialog by remember { mutableStateOf(false) }
    var menuTarget by remember { mutableStateOf<NoteEntry?>(null) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { viewModel.exportToJson(it) }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.importFromJson(it) }
    }

    val pagerState = rememberPagerState(pageCount = { 2 })
    LaunchedEffect(pagerState.currentPage) {
        val tab = if (pagerState.currentPage == 0) "comic" else "video"
        if (tab != uiState.selectedTab) viewModel.selectTab(tab)
    }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    // 系统返回键：按优先级退出子模式
    BackHandler {
        when {
            uiState.isSelectionMode -> viewModel.exitSelectionMode()
            uiState.isSearching -> viewModel.toggleSearch()
            uiState.isTrashMode -> viewModel.leaveTrash()
            else -> onBack()
        }
    }

    // 清空确认
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清空确认") },
            text = { Text("确定要清空此分类的所有记录吗？\n\n删除后可在回收站恢复。") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearCategory(uiState.selectedTab); showClearDialog = false }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("取消") } }
        )
    }

    // 批量删除确认
    if (showBatchDeleteDialog) {
        val isTrash = uiState.isTrashMode
        AlertDialog(
            onDismissRequest = { showBatchDeleteDialog = false },
            title = { Text(if (isTrash) "永久删除" else "批量删除") },
            text = { Text(
                if (isTrash) "确定要永久删除选中的 ${uiState.selectedIds.size} 条记录吗？此操作不可恢复。"
                else "确定要删除选中的 ${uiState.selectedIds.size} 条记录吗？\n\n删除后可在回收站恢复。"
            ) },
            confirmButton = {
                TextButton(onClick = {
                    if (isTrash) viewModel.batchPermanentDelete() else viewModel.batchDelete()
                    showBatchDeleteDialog = false
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showBatchDeleteDialog = false }) { Text("取消") } }
        )
    }

    if (showClearTrashDialog) {
        val trashLabel = if (uiState.trashTab == "comic") "漫画" else "视频"
        AlertDialog(
            onDismissRequest = { showClearTrashDialog = false },
            title = { Text("清空回收站") },
            text = { Text("确定要清空回收站中「${trashLabel}」分类的所有记录吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearTrash(uiState.trashTab)
                    showClearTrashDialog = false
                }) {
                    Text("清空", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showClearTrashDialog = false }) { Text("取消") } }
        )
    }

    Scaffold(
        topBar = {
            if (uiState.isSearching) {
                // 搜索栏
                TopAppBar(
                    title = {
                        TextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("搜索...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.toggleSearch() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "关闭搜索")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            } else {
                // 普通标题栏
                TopAppBar(
                    title = { Text(if (uiState.isTrashMode) "回收站" else "笔记本") },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (uiState.isTrashMode) viewModel.leaveTrash() else onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    actions = {
                        if (uiState.isTrashMode) {
                            IconButton(onClick = { showClearTrashDialog = true }) { Icon(Icons.Filled.DeleteSweep, "清空回收站") }
                        } else {
                            IconButton(onClick = { viewModel.toggleSearch() }) { Icon(Icons.Filled.Search, "搜索") }
                            IconButton(onClick = { viewModel.enterTrash() }) { Icon(Icons.Filled.RestoreFromTrash, "回收站") }
                            IconButton(onClick = { showClearDialog = true }) { Icon(Icons.Filled.DeleteSweep, "清空") }
                            Box {
                                IconButton(onClick = { showOverflowMenu = true }) {
                                    Icon(Icons.Filled.MoreVert, "更多")
                                }
                                DropdownMenu(
                                    expanded = showOverflowMenu,
                                    onDismissRequest = { showOverflowMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("导出备份") },
                                        onClick = {
                                            showOverflowMenu = false
                                            exportLauncher.launch("woyaoyanpai_backup.json")
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("导入恢复") },
                                        onClick = {
                                            showOverflowMenu = false
                                            importLauncher.launch(arrayOf("application/json"))
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(data, containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface)
            }
        },
        bottomBar = {
            if (uiState.isSelectionMode) {
                BottomAppBar(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    TextButton(onClick = { viewModel.exitSelectionMode() }) { Text("取消") }
                    Spacer(Modifier.weight(1f))
                    Text("已选 ${uiState.selectedIds.size} 条", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { viewModel.batchMarkRead() }) { Icon(Icons.Filled.DoneAll, "标记已阅") }
                    IconButton(onClick = { showBatchDeleteDialog = true }) { Icon(Icons.Filled.Delete, "删除", tint = MaterialTheme.colorScheme.error) }
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            // 统计栏
            Text(
                text = "共验牌 ${uiState.totalCount} 条",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            if (uiState.isTrashMode) {
                val trashPagerState = rememberPagerState(pageCount = { 2 })
                LaunchedEffect(trashPagerState.currentPage) {
                    viewModel.selectTrashTab(if (trashPagerState.currentPage == 0) "comic" else "video")
                }
                TabRow(
                    selectedTabIndex = trashPagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(selected = trashPagerState.currentPage == 0,
                        onClick = { scope.launch { trashPagerState.animateScrollToPage(0) } },
                        text = { Text("漫画") })
                    Tab(selected = trashPagerState.currentPage == 1,
                        onClick = { scope.launch { trashPagerState.animateScrollToPage(1) } },
                        text = { Text("视频") })
                }
                HorizontalPager(state = trashPagerState, beyondBoundsPageCount = 0, modifier = Modifier.fillMaxSize()) { page ->
                    val cat = if (page == 0) "comic" else "video"
                    val list = uiState.trashEntries.filter { it.category == cat }
                    Crossfade(targetState = list, animationSpec = tween(250), label = "trash") { entries ->
                        if (entries.isEmpty()) {
                            Box(Modifier.fillMaxSize()) {
                                EmptyState(title = "回收站为空", subtitle = "删除后7天内可在此恢复", trash = true)
                            }
                        } else {
                            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                                items(entries, key = { it.id }) { entry ->
                                    NoteCard(
                                        entry = entry,
                                        isSelectionMode = uiState.isSelectionMode,
                                        isSelected = uiState.selectedIds.contains(entry.id),
                                        onToggleRead = {}, onCopy = {}, onPin = {},
                                        onDelete = { viewModel.permanentDelete(entry) },
                                        onLongPress = { viewModel.enterSelectionMode(entry.id) },
                                        onToggleSelect = { viewModel.toggleSelection(entry.id) },
                                        isTrash = true,
                                        onRestore = { viewModel.restoreEntry(entry) }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // 正常标签页
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(selected = pagerState.currentPage == 0, onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                        text = { Text("漫画 (${uiState.comicCount})") })
                    Tab(selected = pagerState.currentPage == 1, onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                        text = { Text("视频 (${uiState.videoCount})") })
                }

                HorizontalPager(state = pagerState, beyondBoundsPageCount = 0, modifier = Modifier.fillMaxSize()) { page ->
                    val list = if (page == 0) uiState.comicEntries else uiState.videoEntries
                    Crossfade(targetState = list, animationSpec = tween(250), label = "tab") { entries ->
                        if (entries.isEmpty()) {
                            Box(Modifier.fillMaxSize()) { EmptyState() }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                items(entries, key = { it.id }) { entry ->
                                    NoteCard(
                                        entry = entry,
                                        isSelectionMode = uiState.isSelectionMode,
                                        isSelected = uiState.selectedIds.contains(entry.id),
                                        onToggleRead = { viewModel.toggleRead(entry) },
                                        onCopy = {
                                            viewModel.copyToClipboard(entry.cleanedText)
                                            scope.launch { snackbarHostState.showSnackbar("已复制到剪贴板") }
                                        },
                                        onDelete = { viewModel.softDelete(entry) },
                                        onPin = { viewModel.togglePin(entry) },
                                        onLongPress = { viewModel.enterSelectionMode(entry.id) },
                                        onToggleSelect = { viewModel.toggleSelection(entry.id) }
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


