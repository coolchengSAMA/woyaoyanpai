# CLAUDE.md - 我要验牌

## 项目概述

一款 Android 剪贴板自动检测清理工具。用户从社交平台复制含混淆汉字的内容后，App 自动删除汉字并提取纯文本（群号/番号），写回剪贴板并保存到分类笔记本。

- 最低 Android 10 (API 29)
- 包名 `com.yanpai.clipboardcleaner`
- 界面语言：中文

## 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose + Material 3 (BOM 2024.02.00)
- **架构**: MVVM (ViewModel + StateFlow)
- **数据库**: Room (SQLite)
- **导航**: Navigation Compose
- **构建**: Gradle 8.9, AGP 8.2.2, JDK 17+

## 项目结构

```
app/src/main/java/com/yanpai/clipboardcleaner/
├── MainActivity.kt          # 唯一 Activity，管理剪贴板监听生命周期
├── YanPaiApp.kt             # Application 类，初始化数据库
├── clipboard/               # 核心算法
│   ├── CleanerPatterns.kt   # 正则规则 + 字符清理函数
│   ├── ContentDetector.kt   # 检测入口：依次走规则，返回分类+结果
│   └── ClipboardMonitor.kt  # 系统剪贴板读写封装
├── data/                    # Room 数据库
│   ├── NoteEntry.kt         # 实体：id, original, cleaned, category, created_at, updated_at, is_read, is_pinned, deleted_at
│   ├── NoteDao.kt           # DAO：CRUD + 去重查询 + 计数 + 回收站 + 批量操作
│   ├── ThemePresetEntity.kt # 主题预设实体（6 色 + 背景图片 + 显示模式）
│   ├── ThemePresetDao.kt    # 主题预设 DAO
│   └── AppDatabase.kt       # 数据库单例（版本 5，含 4 次迁移）
├── viewmodel/
│   ├── HomeViewModel.kt     # 主页状态：剪贴板检测、保存、计数、自动清理
│   ├── NotebookViewModel.kt # 笔记本状态：分类、搜索、已阅、回收站、批量、导出/导入
│   └── ThemeViewModel.kt    # 主题状态：预设管理、ColorScheme 生成、背景图片
├── ui/
│   ├── theme/               # Color.kt, Theme.kt, Type.kt — Theme.kt 支持背景图片渲染
│   ├── screens/
│   │   ├── HomeScreen.kt        # 主页：剪贴板显示 + 检测按钮 + 结果卡片
│   │   ├── NotebookScreen.kt    # 笔记本：TabRow + HorizontalPager + 回收站 + 批量
│   │   ├── SettingsScreen.kt    # 设置：数据管理入口 + 外观与主题入口
│   │   ├── DataManagementScreen.kt # 数据管理：笔记本备份 + 缓存清理
│   │   └── ThemeSettingsScreen.kt # 主题列表 + 编辑器 + 颜色选择器 + 背景图片 + 自由模式预览
│   ├── components/
│   │   ├── NoteCard.kt          # 笔记卡片（已阅勾选 + 复制 + 删除 + 置顶 + 多选）
│   │   ├── ClipboardResultCard.kt # 检测结果卡片
│   │   ├── CategoryTab.kt       # 分类标签组件
│   │   └── EmptyState.kt        # 空列表占位
│   └── navigation/
│       └── NavGraph.kt      # 路由：HOME / NOTEBOOK / SETTINGS
```

## 核心业务规则

### 检测优先级（ContentDetector）
1. 规则 1A：去汉字 → 删所有非数字字符 → 剩余数字长度 6-7 → 漫画
2. 规则 1B：去汉字 → 匹配 `[Jj][Mm]\d{5,6}` → 漫画
3. 规则 2：去汉字 → 去空格 → 分隔符统一为 `-` → 匹配 `字母{3,5}-?\d{3,5}` → 统一输出 `字母-数字` → 视频
4. 不匹配 → 忽略（不保存，保护隐私）

### 笔记本去重
- 唯一索引：`(cleaned, category)` 
- 重复内容 → 更新 `updated_at`，保留 `is_read` 不变
- 新内容 → 新建记录，`is_read = false`

### 剪贴板监听
- `onResume`：注册监听 + 双重检测（即时 + 500ms Handler 延迟）
- `onPause`：移除监听
- `ClipboardMonitor` 内部存储 `OnPrimaryClipChangedListener` 实例，`removeChangeListener()` 无参数，防止 SAM 转换导致移除失败
- 死循环防护：`lastCleanedText` 缓存上次清理结果，匹配则跳过
- Android 10+ 限制：后台无法读剪贴板，仅前台工作

### 深色模式
- 默认跟随系统（`Configuration.UI_MODE_NIGHT_MASK`），启动时读取 SharedPreferences 检查用户之前是否手动切换过
- 手动切换写入 `SharedPreferences`（key: `dark`），重启 App 保持
- 状态由 MainActivity 持有，通过 NavGraph 传递

### 主题自定义系统（v1.0.2）
- 6 色用户可配置：`primary`（主题色）、`secondary`（标签色）、`tertiary`（选中色）、`background`（页面背景）、`surface`（卡片背景）、`error`（警告色）
- ThemePresetEntity 存储每套预设的亮色/深色各 6 色 + 背景图片路径 + 显示模式
- ThemeViewModel 管理预设列表、当前激活预设、生成 `ColorScheme`
- `colorSchemeFor(isDark)` 从当前预设生成 ColorScheme，MainActivity 中 `remember(isDarkTheme, activePresetId, allPresets)` 监听变化
- `ClipboardCleanerTheme` 接收 `ColorScheme` 参数（而非 `darkTheme: Boolean`）
- 内置两套预设"默认蓝色"和"暗金色"，`isBuiltIn=true` 不可删除
- 编辑器：亮色/深色 Tab、色板 + hex 输入、预览卡片、恢复默认、背景图片设置

### 数据管理（v1.0.3）
- 设置页新增"数据管理"入口，子页面模式与主题相同
- 笔记本数据：导出/导入 JSON 备份（SAF 选择器，复用 NotebookViewModel）
- 缓存管理：扫描 `filesDir/bg_*.jpg` 孤立文件，显示大小，一键清理

### 已知注意事项
- 笔记本列表**不要用 `Crossfade` 动画包裹**——Room Flow 每次 emit 新列表都会触发 250ms 渐变动画，导致标记已阅/置顶时全部卡片闪烁
- `SnapshotFlow` / `derivedStateOf` 可以优化重组粒度
- ThemeViewModel 与 NotebookViewModel 在 Activity 级别共享，通过 `viewModel(viewModelStoreOwner = activity)` 获取

### 背景图片
- 4 种显示模式（ContentScale）：缩放(Fit)、裁切(Crop)、拉伸(FillBounds)、自由(None+graphicsLayer)
- 自由模式：全屏弹窗，双指缩放+拖拽手势，`detectTransformGestures`
- 图片通过 SAF 系统选择器选取，复制到 app 内部存储
- 图片路径和变换参数存储在 ThemePresetEntity 中

### 笔记本功能
- 搜索：实时过滤 `cleaned` + `original` 字段
- 回收站：`deleted_at` 软删除，7 天 `autoCleanTrash()`
- 批量操作：长按进入多选模式，底部操作栏
- 置顶：`is_pinned` 字段，`ORDER BY is_pinned DESC`
- 导出/导入：JSON 格式，SAF 文件选择器（`ActivityResultContracts.CreateDocument` / `OpenDocument`）

### 数据库迁移历史
- v1→v2: 添加 `is_pinned`, `deleted_at` 列
- v2→v3: 创建 `theme_presets` 表（12 个颜色列）
- v3→v4: 添加 `light_bg_image`, `dark_bg_image` 列
- v4→v5: 添加 `bg_scale_mode`, `bg_offset_x/y`, `bg_scale` 列

## 构建

Debug：Android Studio → Run（绿色三角）
Release：Build → Generate Signed Bundle / APK → APK
密钥：`yanpai.jks`（项目根目录），alias `yanpai`，密码见 `local.properties`

Release APK 启用 R8 混淆 + 资源裁剪，体积约 5-8MB。

## 自动化测试

项目包含 ADB 驱动脚本，用于在真机上自动化操作 App：

```bash
bash .claude/skills/run-woyaoyanpai/driver.sh launch     # 启动 App
bash .claude/skills/run-woyaoyanpai/driver.sh detect "文本" # 设剪贴板 → 检测 → 截图
bash .claude/skills/run-woyaoyanpai/driver.sh test        # 完整测试流程
```

截图保存在 `screenshots/` 目录。需要 Android 设备已连接且开启 USB 调试。


## 编码约定

- 所有 `_uiState.value.copy()` 用命名参数防止 `it` 冲突
- ViewModel 通过 `viewModel(viewModelStoreOwner = activity)` 在 Activity 级别共享
- 页面导航无过渡动画（`EnterTransition.None`）
- 笔记时间格式化：`formatTime()` 使用线程安全的 `java.time.format.DateTimeFormatter` 全局单例，NoteCard 中 `remember(entry.updatedAt)` 缓存
- LazyColumn 使用 `key = { it.id }` 稳定键
- 多个 Room Flow 需同时更新时用 `combine()` 合并，避免竞态
- `ClipboardMonitor` 的 `setOnChangeListener` / `removeChangeListener` 不加参数，内部管理 SAM 实例

## 添加新检测规则

1. 在 `CleanerPatterns.kt` 添加正则和提取函数
2. 在 `ContentDetector.kt` 的 `detect()` 中添加调用
3. 如需新分类 → 更新 `NoteEntry.category`、`NotebookUiState`、UI 标签等

## 添加新页面

1. 在 `ui/screens/` 创建 Composable
2. 在 `NavGraph.kt` 添加路由和 composable
3. 如需 ViewModel → 在 `viewmodel/` 创建，通过 `viewModel(viewModelStoreOwner = activity)` 获取
