# 我要验牌

> 本软件及其 GitHub 仓库完全由 AI（Claude Code）生成与管理，包括需求分析、架构设计、代码编写、文档撰写等全部环节。

一款 Android 剪贴板自动检测清理工具。社交平台上分享番号时常会掺杂无关汉字来绕开屏蔽，这款 App 能自动识别并删除汉字，提取纯文本，同时保存到分类笔记本。

<p align="center">
  <img src="https://img.shields.io/badge/platform-Android-blue" alt="Platform">
  <img src="https://img.shields.io/badge/minSdk-29-green" alt="Min SDK">
  <img src="https://img.shields.io/badge/version-1.0.2-orange" alt="Version">
</p>

## 功能

- 自动识别剪贴板中的混淆内容，删除汉字提取纯文本
- 漫画识别：6-7 位连续数字 / JM + 5-6 位数字
- 视频识别：番号格式（3-5 字母 + 3-5 数字），统一输出为 `字母-数字`
- 分类笔记本：漫画 / 视频，已阅标记、置顶、搜索、批量操作
- 回收站：软删除 + 7 天自动清理，支持恢复
- 数据导出/导入：JSON 备份，SAF 文件选择器
- 自定义主题：6 色可配置，多套预设，深色/浅色分别设置
- 自定义背景图片：缩放/裁切/拉伸/自由四种模式，自由模式支持手势调整
- 隐私保护：只保存匹配规则的内容，云端备份已关闭

## 使用方法

1. 在其他 App 复制含混淆文字的内容
2. 打开「我要验牌」→ 自动检测并复制
3. 切回原 App → 粘贴纯净文本
4. 笔记本中可查看、复制、管理历史记录

## 构建

```bash
# Debug
./gradlew assembleDebug

# Release (需要签名密钥)
./gradlew assembleRelease
```

要求 Android Studio + JDK 17+，最低支持 Android 10 (API 29)。

## 技术栈

- Kotlin + Jetpack Compose（Material 3）
- MVVM 架构（ViewModel + StateFlow）
- Room 数据库
- Navigation Compose

## 下载

最新安装包：[Releases](https://github.com/coolchengSAMA/woyaoyanpai/releases)

## 许可证

MIT License
