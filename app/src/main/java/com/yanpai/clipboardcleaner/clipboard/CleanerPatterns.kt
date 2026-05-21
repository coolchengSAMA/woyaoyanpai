package com.yanpai.clipboardcleaner.clipboard

/**
 * 所有正则表达式和字符处理规则。
 */
object CleanerPatterns {

    // 中文字符 Unicode 范围
    private val CHINESE_CHAR = Regex("[\\u4e00-\\u9fff\\u3400-\\u4dbf\\uf900-\\ufaff]")
    // 中文标点
    private val CHINESE_PUNCTUATION = Regex("[，。！？；：\"\"''【】《》（）…—～、\\u3000\\u3001\\u3002\\uff0c\\uff0e\\uff1a\\uff1b\\uff01\\uff1f]")
    // 全角数字 ０-９
    private val FULLWIDTH_DIGIT = Regex("[\\uff10-\\uff19]")
    // 全角字母 Ａ-Ｚ ａ-ｚ
    private val FULLWIDTH_LETTER = Regex("[\\uff21-\\uff3a\\uff41-\\uff5a]")

    // 规则 1B：JM + 5-6 位数字（前后加边界，防止长串匹配）
    private val JM_DIGITS = Regex("(?<![A-Za-z])[Jj][Mm]\\d{5,6}(?!\\d)")
    // 规则 2：3-5 字母 + 可选的连字符 + 3-5 数字（番号，前后加边界）
    private val VIDEO_CODE = Regex("(?<![A-Za-z])[A-Za-z]{3,5}-?\\d{3,5}(?!\\d)")

    /**
     * 删除所有中文字符和中文标点。
     */
    fun removeChinese(text: String): String {
        return text
            .replace(CHINESE_CHAR, "")
            .replace(CHINESE_PUNCTUATION, "")
            .replace(FULLWIDTH_DIGIT) { (it.value[0].code - 0xFF10 + '0'.code).toChar().toString() }
            .replace(FULLWIDTH_LETTER) { mr ->
                val code = mr.value[0].code
                val base = if (code in 0xFF21..0xFF3A) 'A'.code else 'a'.code
                (base + (code - if (code in 0xFF21..0xFF3A) 0xFF21 else 0xFF41)).toChar().toString()
            }
    }

    /**
     * 规则 1A：提取 6-7 位数字。
     * 去掉所有非数字字符后拼在一起判断，处理数字被符号/字母分隔的情况。
     * 排除 11 位手机号（1[3-9] 开头）。
     */
    fun extractComicDigits(text: String): String? {
        val digitsOnly = text.replace(Regex("\\D"), "")
        if (digitsOnly.length !in 6..7) return null
        return digitsOnly
    }

    /**
     * 规则 1B：检测 JM + 5-6 位数字。
     */
    fun extractJM(text: String): String? {
        return JM_DIGITS.find(text)?.value
    }

    /**
     * 规则 2：检测番号格式，统一输出为 "字母-数字"（如 IPX-123）。
     */
    fun extractVideoCode(text: String): String? {
        val normalized = text
            .replace(Regex("[\\s\\u3000]"), "")
            .replace(Regex("[_\\u2013\\u2014\\uff0d]"), "-")
        val match = VIDEO_CODE.find(normalized)?.value ?: return null
        // 统一格式：字母部分 + "-" + 数字部分
        val parts = match.split("-")
        return if (parts.size == 2) {
            "${parts[0]}-${parts[1]}"
        } else {
            // IPX123 → IPX-123
            val letters = match.takeWhile { it.isLetter() }
            val digits = match.drop(letters.length)
            "$letters-$digits"
        }
    }
}
