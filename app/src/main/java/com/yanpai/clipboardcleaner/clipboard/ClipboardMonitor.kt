package com.yanpai.clipboardcleaner.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

/**
 * 系统剪贴板读写封装。
 */
class ClipboardMonitor(context: Context) {

    private val manager: ClipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    // SAM 接口实例，确保 add/remove 引用的是同一个对象
    private var primaryClipListener: ClipboardManager.OnPrimaryClipChangedListener? = null

    /**
     * 读取剪贴板文本内容。非文本内容返回 null。
     */
    fun readText(): String? {
        val clip = manager.primaryClip ?: return null
        if (clip.itemCount == 0) return null
        val item = clip.getItemAt(0)
        return item.text?.toString()
    }

    /**
     * 将文本写入系统剪贴板。
     */
    fun writeText(text: String) {
        val clip = ClipData.newPlainText("cleaned", text)
        manager.setPrimaryClip(clip)
    }

    /**
     * 注册剪贴板变化监听器。
     */
    fun setOnChangeListener(listener: () -> Unit) {
        removeChangeListener() // 防止重复注册
        primaryClipListener = ClipboardManager.OnPrimaryClipChangedListener { listener() }
        manager.addPrimaryClipChangedListener(primaryClipListener!!)
    }

    /**
     * 移除剪贴板变化监听器。
     */
    fun removeChangeListener() {
        primaryClipListener?.let { manager.removePrimaryClipChangedListener(it) }
        primaryClipListener = null
    }
}
