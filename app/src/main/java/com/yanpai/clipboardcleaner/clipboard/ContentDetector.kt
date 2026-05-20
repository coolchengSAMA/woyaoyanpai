package com.yanpai.clipboardcleaner.clipboard

/**
 * 检测结果：包含分类和清理后的文本。
 */
data class DetectResult(
    val category: String,   // "comic" / "video" / "url"
    val cleaned: String     // 清理后的纯文本
)

/**
 * 剪贴板内容检测器：按四条规则依次匹配，返回分类和清理结果。
 * 不匹配任何规则返回 null。
 */
object ContentDetector {

    /**
     * 检测剪贴板文本，返回分类和清理后的内容。
     * 优先级：漫画 > 视频 > 网址
     */
    fun detect(rawText: String): DetectResult? {
        if (rawText.isBlank()) return null

        // 先去汉字和中文标点
        val stripped = CleanerPatterns.removeChinese(rawText).trim()

        // 如果去汉字后为空或只有空白，说明没有需要清理的内容
        if (stripped.isBlank()) return null

        // 规则 1A：6-7 位连续数字 → 漫画
        CleanerPatterns.extractComicDigits(stripped)?.let { digits ->
            return DetectResult(category = "comic", cleaned = digits)
        }

        // 规则 1B：JM + 5-6 位数字 → 漫画
        CleanerPatterns.extractJM(stripped)?.let { jm ->
            return DetectResult(category = "comic", cleaned = jm)
        }

        // 规则 2：番号格式 → 视频
        CleanerPatterns.extractVideoCode(stripped)?.let { code ->
            return DetectResult(category = "video", cleaned = code)
        }

        // 不匹配任何规则
        return null
    }
}
