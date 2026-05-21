---
name: run-woyaoyanpai
description: Build, install, launch, and drive the 我要验牌 Android app. Use when asked to run, screenshot, test, or verify the app on an Android device.
---

# 我要验牌 — Run Skill

Android 剪贴板自动检测清理 App。通过 ADB 驱动：构建 → 安装 → 启动 → 交互 → 截图。

**所有路径相对于项目根目录。**

## 前置条件

- Android 设备已通过 USB 连接，已开启 USB 调试
- Android SDK 已安装（`$LOCALAPPDATA/Android/Sdk`）
- 设备上已安装该 App，或使用 `adb install` 安装

## 构建

**Android Studio（主要方式）：** Build → Build Bundle(s) / APK(s) → Build APK(s)

**命令行（需要 Gradle wrapper）：**
```bash
./gradlew assembleDebug
```

Debug APK 输出：`app/build/intermediates/apk/debug/app-debug.apk`
Release APK（签名+压缩）：`app/release/app-release.apk`

## 驱动脚本

使用 `driver.sh` 通过 ADB 操作 App：

```bash
# 运行完整测试流程
bash .claude/skills/run-woyaoyanpai/driver.sh test

# 单独命令
bash .claude/skills/run-woyaoyanpai/driver.sh install     # 安装 APK
bash .claude/skills/run-woyaoyanpai/driver.sh launch      # 启动 App
bash .claude/skills/run-woyaoyanpai/driver.sh detect      # 设置剪贴板 → 触发检测 → 截图
bash .claude/skills/run-woyaoyanpai/driver.sh notebook    # 打开笔记本 → 截图
bash .claude/skills/run-woyaoyanpai/driver.sh screenshot  # 截图
bash .claude/skills/run-woyaoyanpai/driver.sh devices     # 列出连接设备
```

截图保存在 `screenshots/` 目录。

## 测试剪贴板检测

```bash
# 1. 启动 App
bash .claude/skills/run-woyaoyanpai/driver.sh launch

# 2. 注入混淆文本并触发检测（模拟复制 3月5号0点2分3更4夜）
bash .claude/skills/run-woyaoyanpai/driver.sh detect "3月5号0点2分3更4夜"

# 3. 预期结果：剪贴板变为 350234，主页显示检测结果卡片（漫画分类）
```

## 测试其他功能

```bash
# 视频番号检测
bash .claude/skills/run-woyaoyanpai/driver.sh detect "I这P是X-番1号2啊3"

# 深色模式切换
bash .claude/skills/run-woyaoyanpai/driver.sh toggle-theme

# 笔记本视图
bash .claude/skills/run-woyaoyanpai/driver.sh notebook
bash .claude/skills/run-woyaoyanpai/driver.sh switch-tab
```

## 常见问题

**设备未识别：** `adb devices` 显示空 → 检查 USB 线、开发者选项、USB 调试授权。

**安装失败 INSTALL_FAILED_ABORTED：** 手机上弹出权限确认 → 点「允许」。如果是签名冲突 → 先 `adb uninstall com.yanpai.clipboardcleaner`。

**Release APK 无法 run-as 调试：** Release 版本不可调试，数据库无法直接读取。用 Debug 版本做测试。

**按钮点击位置不准：** 脚本中的点击坐标（630, 700）基于 1260x2800 分辨率。不同设备需调整坐标。
