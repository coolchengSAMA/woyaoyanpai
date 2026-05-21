package com.yanpai.clipboardcleaner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import android.content.Context
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yanpai.clipboardcleaner.data.ThemePresetEntity
import com.yanpai.clipboardcleaner.viewmodel.ThemeViewModel
import java.io.File
import java.util.UUID

// Material Design 500 色板
private val swatches = listOf(
    0xFFFFFBFE.toInt() to "白", 0xFF000000.toInt() to "黑",
    0xFFE53935.toInt() to "红", 0xFFD81B60.toInt() to "粉",
    0xFF8E24AA.toInt() to "紫", 0xFF5E35B1.toInt() to "深紫",
    0xFF3949AB.toInt() to "靛蓝", 0xFF1E88E5.toInt() to "蓝",
    0xFF039BE5.toInt() to "浅蓝", 0xFF00ACC1.toInt() to "青",
    0xFF00897B.toInt() to "深绿", 0xFF43A047.toInt() to "绿",
    0xFF7CB342.toInt() to "浅绿", 0xFFC0CA33.toInt() to "黄绿",
    0xFFFDD835.toInt() to "黄", 0xFFFFB300.toInt() to "琥珀",
    0xFFFB8C00.toInt() to "橙", 0xFFF4511E.toInt() to "深橙",
    0xFF6D4C41.toInt() to "棕", 0xFF757575.toInt() to "灰",
    0xFF546E7A.toInt() to "蓝灰"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    themeViewModel: ThemeViewModel?,
    onBack: () -> Unit,
    onEditPreset: (Long?) -> Unit
) {
    val presets by (themeViewModel?.allPresets?.collectAsState() ?: remember { mutableStateOf(emptyList()) })
    val activeId by (themeViewModel?.activePresetId?.collectAsState() ?: remember { mutableStateOf(1L) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("外观与主题") },
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
        floatingActionButton = {
            FloatingActionButton(onClick = { onEditPreset(null) }) {
                Icon(Icons.Filled.Add, "新建主题")
            }
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            items(presets, key = { it.id }) { preset ->
                val isActive = preset.id == activeId
                Card(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { themeViewModel?.setActivePreset(preset.id) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = isActive, onClick = { themeViewModel?.setActivePreset(preset.id) })
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(preset.name, style = MaterialTheme.typography.bodyLarge)
                                if (preset.isBuiltIn) {
                                    Spacer(Modifier.width(6.dp))
                                    Icon(Icons.Filled.Lock, null, Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            // 6 色预览
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(Color(preset.lightPrimary), Color(preset.lightSecondary),
                                    Color(preset.lightTertiary), Color(preset.lightBackground),
                                    Color(preset.lightSurface), Color(preset.lightError)
                                ).forEach { c ->
                                    Box(Modifier.size(16.dp).clip(CircleShape).background(c)
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape))
                                }
                            }
                        }
                        IconButton(onClick = { onEditPreset(preset.id) }) {
                            Icon(Icons.Outlined.Edit, "编辑",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ThemeEditorScreen(
    themeViewModel: ThemeViewModel,
    presetId: Long?,
    onBack: () -> Unit
) {
    val presets by themeViewModel.allPresets.collectAsState()
    val existing = presets.find { it.id == presetId }

    BackHandler { onBack() }

    val isBuiltIn = existing?.isBuiltIn == true
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var editingDark by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showFreeDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    // 6 个可配置颜色
    val lightColors = remember {
        listOf(
            mutableStateOf(Color(existing?.lightPrimary ?: 0xFF2196F3.toInt())),
            mutableStateOf(Color(existing?.lightSecondary ?: 0xFFE3F2FD.toInt())),
            mutableStateOf(Color(existing?.lightTertiary ?: 0xFF2196F3.toInt())),
            mutableStateOf(Color(existing?.lightBackground ?: 0xFFF5F5F5.toInt())),
            mutableStateOf(Color(existing?.lightSurface ?: 0xFFFFFFFF.toInt())),
            mutableStateOf(Color(existing?.lightError ?: 0xFFF44336.toInt()))
        )
    }
    val darkColors = remember {
        listOf(
            mutableStateOf(Color(existing?.darkPrimary ?: 0xFFB8860B.toInt())),
            mutableStateOf(Color(existing?.darkSecondary ?: 0xFF2A2A2A.toInt())),
            mutableStateOf(Color(existing?.darkTertiary ?: 0xFFB8860B.toInt())),
            mutableStateOf(Color(existing?.darkBackground ?: 0xFF000000.toInt())),
            mutableStateOf(Color(existing?.darkSurface ?: 0xFF1A1A1A.toInt())),
            mutableStateOf(Color(existing?.darkError ?: 0xFFF44336.toInt()))
        )
    }
    val currentColors = if (editingDark) darkColors else lightColors

    val colorLabels = listOf("主题色", "标签色", "选中色", "页面背景", "卡片背景", "警告色")
    val colorDescs = listOf("顶栏、按钮", "Tab 标签、「共验牌」背景", "已阅勾选、列表选中", "页面底色", "卡片、导航入口、对话框", "删除按钮、危险操作")

    // 背景图片
    val ctx = LocalContext.current
    var lightBgImage by remember { mutableStateOf(existing?.lightBackgroundImage) }
    var darkBgImage by remember { mutableStateOf(existing?.darkBackgroundImage) }
    val currentBgImage = if (editingDark) darkBgImage else lightBgImage

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { srcUri ->
            val file = File(ctx.filesDir, "bg_${UUID.randomUUID()}.jpg")
            try {
                ctx.contentResolver.openInputStream(srcUri)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
                val path = file.absolutePath
                if (editingDark) darkBgImage = path else lightBgImage = path
                existing?.let { themeViewModel.setBackgroundImage(it.id, editingDark, path) }
            } catch (_: Exception) {}
        }
    }

    fun removeBg() {
        if (editingDark) darkBgImage = null else lightBgImage = null
        existing?.let { themeViewModel.setBackgroundImage(it.id, editingDark, null) }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除主题") },
            text = { Text("确定要删除「${existing?.name ?: ""}」吗？") },
            confirmButton = {
                TextButton(onClick = {
                    existing?.let { themeViewModel.deletePreset(it.id) }
                    showDeleteDialog = false; onBack()
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("取消") } }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("恢复默认") },
            text = { Text("确定要恢复为默认主题配色吗？当前的自定义颜色和背景图片将被重置。") },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    lightBgImage = null; darkBgImage = null
                    lightColors[0].value = Color(0xFF2196F3.toInt())
                    lightColors[1].value = Color(0xFFE3F2FD.toInt())
                    lightColors[2].value = Color(0xFF2196F3.toInt())
                    lightColors[3].value = Color(0xFFF5F5F5.toInt())
                    lightColors[4].value = Color(0xFFFFFFFF.toInt())
                    lightColors[5].value = Color(0xFFF44336.toInt())
                    darkColors[0].value = Color(0xFFB8860B.toInt())
                    darkColors[1].value = Color(0xFF2A2A2A.toInt())
                    darkColors[2].value = Color(0xFFB8860B.toInt())
                    darkColors[3].value = Color(0xFF000000.toInt())
                    darkColors[4].value = Color(0xFF1A1A1A.toInt())
                    darkColors[5].value = Color(0xFFF44336.toInt())
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showResetDialog = false }) { Text("取消") } }
        )
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (isBuiltIn) "查看主题" else if (existing != null) "编辑主题" else "新建主题") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    actions = {
                        if (!isBuiltIn) {
                            IconButton(onClick = { showResetDialog = true }) {
                                Icon(Icons.Filled.Refresh, "恢复默认")
                            }
                            if (existing != null) {
                                IconButton(onClick = { showDeleteDialog = true }) {
                                    Icon(Icons.Outlined.DeleteOutline, "删除", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
            // 名称
            OutlinedTextField(
                value = name, onValueChange = { if (!isBuiltIn) name = it },
                label = { Text("主题名称") },
                singleLine = true,
                enabled = !isBuiltIn,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // 亮色/深色 Tab
            TabRow(selectedTabIndex = if (editingDark) 1 else 0,
                containerColor = MaterialTheme.colorScheme.secondary) {
                Tab(selected = !editingDark, onClick = { editingDark = false }, text = { Text("亮色模式") })
                Tab(selected = editingDark, onClick = { editingDark = true }, text = { Text("深色模式") })
            }

            Spacer(Modifier.height(16.dp))

            // 6 色设置
            currentColors.forEachIndexed { index, colorState ->
                var expanded by remember { mutableStateOf(false) }
                var hexText by remember(colorState.value) {
                    mutableStateOf(String.format("#%06X", 0xFFFFFF and colorState.value.toArgb()))
                }

                Card(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            Modifier.fillMaxWidth().let { if (!isBuiltIn) it.clickable { expanded = !expanded } else it },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(32.dp).clip(CircleShape).background(colorState.value)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(colorLabels[index], style = MaterialTheme.typography.bodyLarge)
                                Text(colorDescs[index], style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                            Text(hexText, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }

                        if (expanded) {
                            Spacer(Modifier.height(12.dp))

                            // 色板网格
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                swatches.forEach { (argb, label) ->
                                    val c = Color(argb)
                                    Box(
                                        Modifier.size(36.dp).clip(CircleShape).background(c).clickable {
                                            colorState.value = c
                                            hexText = String.format("#%06X", 0xFFFFFF and c.toArgb())
                                            expanded = false
                                        }.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (colorState.value == c) {
                                            Icon(Icons.Filled.Check, null, Modifier.size(16.dp),
                                                tint = if ((0.299 * c.red + 0.587 * c.green + 0.114 * c.blue) > 0.5f)
                                                    Color.Black else Color.White)
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            // 十六进制输入
                            OutlinedTextField(
                                value = hexText, onValueChange = { input ->
                                    hexText = input
                                    val cleaned = input.removePrefix("#")
                                    try {
                                        val argb = ("FF" + cleaned).toLong(16).toInt()
                                        colorState.value = Color(argb)
                                    } catch (_: Exception) {}
                                },
                                label = { Text("十六进制") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 背景图片（内置主题不允许修改）
            if (!isBuiltIn) Card(
                Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("背景图片", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    // 图片选择
                    if (currentBgImage != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(60.dp).clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                val bmp = remember { android.graphics.BitmapFactory.decodeFile(currentBgImage) }
                                if (bmp != null) {
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(File(currentBgImage!!).name, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.weight(1f))
                            TextButton(onClick = { removeBg() }) { Text("移除") }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("未设置", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.weight(1f))
                            Button(onClick = { imagePicker.launch("image/*") }) {
                                Text("选择图片")
                            }
                        }
                    }

                    // 显示模式
                    if (currentBgImage != null) {
                        Spacer(Modifier.height(12.dp))
                        var currentScaleMode by remember(existing?.id, existing?.backgroundScaleMode) { mutableStateOf(existing?.backgroundScaleMode ?: 0) }
                        Text("显示方式", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(0 to "缩放", 1 to "裁切", 2 to "拉伸", 3 to "自由").forEach { (mode, label) ->
                                val selected = currentScaleMode == mode
                                Button(
                                    onClick = {
                                        currentScaleMode = mode
                                        existing?.let { themeViewModel.setBackgroundScaleMode(it.id, mode) }
                                    },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surface,
                                        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                ) { Text(label, style = MaterialTheme.typography.bodySmall, maxLines = 1, softWrap = false) }
                            }
                        }

                        // 自由模式 - 调整位置按钮
                        if (currentScaleMode == 3) {
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(onClick = { showFreeDialog = true }) {
                                Text("调整图片位置与缩放")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // 预览卡片
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("预览", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth().height(48.dp)
                        .background(if (editingDark) darkColors[0].value else lightColors[0].value,
                            RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center) {
                        Text("主题色标题栏", color = contentColorFor(if (editingDark) darkColors[0].value else lightColors[0].value))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Box(Modifier.weight(1f).height(36.dp).padding(end = 4.dp)
                            .background(if (editingDark) darkColors[3].value else lightColors[3].value, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center) {
                            Text("背景", style = MaterialTheme.typography.labelMedium,
                                color = contentColorFor(if (editingDark) darkColors[3].value else lightColors[3].value))
                        }
                        Box(Modifier.weight(1f).height(36.dp).padding(start = 4.dp)
                            .background(if (editingDark) darkColors[4].value else lightColors[4].value, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center) {
                            Text("卡片", style = MaterialTheme.typography.labelMedium,
                                color = contentColorFor(if (editingDark) darkColors[4].value else lightColors[4].value))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth().height(36.dp)
                        .background(if (editingDark) darkColors[5].value else lightColors[5].value, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center) {
                        Text("错误/删除", color = contentColorFor(if (editingDark) darkColors[5].value else lightColors[5].value))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // 保存按钮（内置主题不可编辑）
            if (!isBuiltIn) {
                Button(onClick = {
                    if (name.isBlank()) return@Button
                    val lightList = lightColors.map { it.value }
                    val darkList = darkColors.map { it.value }
                    if (existing != null) {
                        themeViewModel.updatePreset(existing.copy(
                            name = name,
                            lightPrimary = lightList[0].toArgb(), lightSecondary = lightList[1].toArgb(),
                            lightTertiary = lightList[2].toArgb(), lightBackground = lightList[3].toArgb(),
                            lightSurface = lightList[4].toArgb(), lightError = lightList[5].toArgb(),
                            lightBackgroundImage = lightBgImage,
                            darkPrimary = darkList[0].toArgb(), darkSecondary = darkList[1].toArgb(),
                            darkTertiary = darkList[2].toArgb(), darkBackground = darkList[3].toArgb(),
                            darkSurface = darkList[4].toArgb(), darkError = darkList[5].toArgb(),
                            darkBackgroundImage = darkBgImage
                        ))
                    } else {
                        themeViewModel.createPreset(name, lightList, darkList, lightBgImage, darkBgImage)
                    }
                    onBack()
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("保存")
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // 自由模式弹窗
    if (showFreeDialog) {
        FreePositionDialog(
            imagePath = currentBgImage!!,
            initialOffsetX = existing?.bgOffsetX ?: 0f,
            initialOffsetY = existing?.bgOffsetY ?: 0f,
            initialScale = existing?.bgScale ?: 1f,
            onConfirm = { ox, oy, s ->
                existing?.let { themeViewModel.setBackgroundFreeTransform(it.id, ox, oy, s) }
                showFreeDialog = false
            },
            onCancel = { showFreeDialog = false }
        )
    }
} // Box
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FreePositionDialog(
    imagePath: String,
    initialOffsetX: Float,
    initialOffsetY: Float,
    initialScale: Float,
    onConfirm: (Float, Float, Float) -> Unit,
    onCancel: () -> Unit
) {
    var bitmap by remember(imagePath) { mutableStateOf<android.graphics.Bitmap?>(null) }
    var offsetX by remember { mutableStateOf(initialOffsetX) }
    var offsetY by remember { mutableStateOf(initialOffsetY) }
    var currentScale by remember { mutableStateOf(initialScale) }

    BackHandler { onCancel() }

    LaunchedEffect(imagePath) {
        bitmap = try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                BitmapFactory.decodeFile(imagePath)
            }
        } catch (_: Exception) { null }
    }

    Box(
        Modifier.fillMaxSize().pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
                currentScale = (currentScale * zoom).coerceIn(0.3f, 5f)
                offsetX += pan.x
                offsetY += pan.y
            }
        }
    ) {
        // 背景图片铺满全屏（与 App 实际渲染区域一致）
        Box(Modifier.fillMaxSize().background(Color(0xFF111111)), contentAlignment = Alignment.Center) {
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = currentScale; scaleY = currentScale
                            translationX = offsetX; translationY = offsetY
                        },
                    contentScale = ContentScale.Fit
                )
            } ?: Text("加载中...", color = Color.White)
        }

        // 顶栏（与 App 其他页面完全一致，遮挡效果真实体现）
        TopAppBar(
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
            title = { Text("调整位置与缩放") },
            navigationIcon = {
                IconButton(onClick = onCancel) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "取消")
                }
            },
            actions = {
                IconButton(onClick = { offsetX = 0f; offsetY = 0f; currentScale = 1f }) {
                    Icon(Icons.Filled.Refresh, "重置")
                }
                IconButton(onClick = { onConfirm(offsetX, offsetY, currentScale) }) {
                    Icon(Icons.Filled.Check, "确定", tint = Color(0xFF4CAF50))
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
}

private fun contentColorFor(color: Color): Color {
    val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
    return if (luminance > 0.5f) Color(0xFF000000) else Color(0xFFFFFFFF)
}
