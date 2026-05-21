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
│   ├── NoteEntry.kt         # 实体：id, original, cleaned, category, created_at, updated_at, is_read
│   ├── NoteDao.kt           # DAO：CRUD + 去重查询 + 计数
│   └── AppDatabase.kt       # 数据库单例
├── viewmodel/
│   ├── HomeViewModel.kt     # 主页状态：剪贴板检测、保存、计数
│   └── NotebookViewModel.kt # 笔记本状态：分类列表、已阅、删除、复制
├── ui/
│   ├── theme/               # Color.kt, Theme.kt, Type.kt
│   ├── screens/
│   │   ├── HomeScreen.kt    # 主页：剪贴板显示 + 检测按钮 + 结果卡片
│   │   └── NotebookScreen.kt # 笔记本：TabRow 切换分类 + LazyColumn 列表
│   ├── components/
│   │   ├── NoteCard.kt          # 笔记卡片（已阅勾选 + 复制 + 删除）
│   │   ├── ClipboardResultCard.kt # 检测结果卡片
│   │   ├── CategoryTab.kt       # 分类标签组件（含 TabItem 数据类）
│   │   └── EmptyState.kt        # 空列表占位
│   └── navigation/
│       └── NavGraph.kt      # 路由：HOME / NOTEBOOK，无过渡动画
└── res/
    ├── values/strings.xml    # 中文字符串
    ├── values/themes.xml     # 窗口黑色背景
    └── drawable/             # 矢量图标
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
- 深色主题：暗黄 `#B8860B` + 纯黑 `#000000`
- 状态由 MainActivity 持有，通过 NavGraph → HomeScreen → NotebookScreen 传递

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
