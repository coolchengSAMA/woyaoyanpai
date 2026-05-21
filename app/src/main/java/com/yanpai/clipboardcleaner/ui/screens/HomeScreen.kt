package com.yanpai.clipboardcleaner.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yanpai.clipboardcleaner.ui.components.ClipboardResultCard
import com.yanpai.clipboardcleaner.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: () -> Unit = {},
    onNavigateToNotebook: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val gutter = 16.dp

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    var buttonPressed by remember { mutableStateOf(false) }
    val buttonScale by animateFloatAsState(if (buttonPressed) 0.96f else 1f, label = "scale")
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect {
            buttonPressed = it is androidx.compose.foundation.interaction.PressInteraction.Press
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我要验牌") },
                actions = {
                    IconButton(onClick = onToggleDarkTheme) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = if (isDarkTheme) "切换亮色模式" else "切换深色模式",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(data, containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── 剪贴板输入区 ──
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = gutter, vertical = 6.dp),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Text(
                    text = uiState.clipboardText ?: "剪贴板为空 — 复制一段文字试试",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (uiState.clipboardText == null)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(14.dp),
                    maxLines = 8,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── 检测按钮 ──
            Button(
                onClick = { viewModel.detectAndClean() },
                modifier = Modifier.fillMaxWidth().padding(horizontal = gutter).height(50.dp).scale(buttonScale),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                interactionSource = interactionSource
            ) {
                Text("检测并验牌", style = MaterialTheme.typography.titleMedium)
            }

            // ── 状态提示 ──
            AnimatedVisibility(visible = uiState.noMatch, enter = fadeIn()) {
                Text(
                    text = "未识别到需要验牌的内容",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = gutter, vertical = 6.dp)
                )
            }

            // ── 验牌结果 ──
            uiState.detectResult?.let { result ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
                ) {
                    ClipboardResultCard(
                        result = result,
                        originalText = uiState.originalText ?: uiState.clipboardText ?: "",
                        isRead = uiState.isRead,
                        isSaved = uiState.isSaved,
                        onToggleRead = { viewModel.toggleRead() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 笔记本入口 ──
            Card(
                onClick = onNavigateToNotebook,
                modifier = Modifier.fillMaxWidth().padding(horizontal = gutter),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("打开笔记本", style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text("漫画 ${uiState.comicCount} · 视频 ${uiState.videoCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                    }
                    Text("→", style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 使用说明（仅新用户参考，可收起） ──
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = gutter),
                shape = MaterialTheme.shapes.small,
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("使用说明", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(Modifier.height(4.dp))
                    Text("1. 复制含混淆文字的内容\n2. 点「检测并验牌」自动复制到剪贴板\n3. 去任意输入框粘贴即可",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
